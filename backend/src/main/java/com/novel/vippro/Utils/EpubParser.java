package com.novel.vippro.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EpubParser {

    // Helper class to store Table of Content items
    private static class TocItem {
        String title;
        String fullPath; // path/to/file.xhtml
        String fragment; // the part after # (e.g., "chapter1")

        public TocItem(String title, String fullPath, String fragment) {
            this.title = title;
            this.fullPath = fullPath;
            this.fragment = fragment;
        }
    }

    public static EpubParseResult parse(byte[] epubBytes) throws IOException {
        EpubParseResult result = new EpubParseResult();
        List<EpubChapterDTO> finalChapters = new ArrayList<>();
        Map<String, byte[]> files = new HashMap<>();

        // 1. Unzip content
        try (ByteArrayInputStream bais = new ByteArrayInputStream(epubBytes);
             ZipInputStream zis = new ZipInputStream(bais)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                files.put(entry.getName(), zis.readAllBytes());
            }
        }

        // 2. Find Root File (OPF)
        String rootfilePath = getRootFilePath(files);
        if (rootfilePath == null) return result; // Invalid EPUB

        // Base path for resolving relative links (e.g., OEBPS/)
        String opfBasePath = "";
        int lastSlash = rootfilePath.lastIndexOf('/');
        if (lastSlash > 0) opfBasePath = rootfilePath.substring(0, lastSlash + 1);

        // 3. Parse OPF to get Metadata, Spine, and TOC reference
        byte[] packageDoc = files.get(rootfilePath);
        if (packageDoc == null) return result;

        List<String> spineHrefs = new ArrayList<>();
        Map<String, String> manifestIdToHref = new HashMap<>();
        String tocId = null; // ID of the NCX or Nav file

        try {
            Document doc = parseXml(packageDoc);

            // Metadata
            NodeList titles = doc.getElementsByTagName("dc:title");
            if (titles.getLength() > 0) result.setTitle(titles.item(0).getTextContent());
            NodeList creators = doc.getElementsByTagName("dc:creator");
            if (creators.getLength() > 0) result.setAuthor(creators.item(0).getTextContent());

            // Manifest
            NodeList items = doc.getElementsByTagName("item");
            for (int i = 0; i < items.getLength(); i++) {
                Node n = items.item(i);
                String id = getAttr(n, "id");
                String href = getAttr(n, "href");
                String properties = getAttr(n, "properties"); // For EPUB3 nav
                
                if (id != null && href != null) {
                    manifestIdToHref.put(id, href);
                    // Check for TOC (EPUB 3 uses properties="nav", EPUB 2 uses spine toc="id")
                    if (properties != null && properties.contains("nav")) {
                        tocId = id;
                    }
                }
            }

            // Spine
            NodeList spineTag = doc.getElementsByTagName("spine");
            if (spineTag.getLength() > 0) {
                // Try to get EPUB2 NCX ID from spine attribute
                String spineToc = getAttr(spineTag.item(0), "toc");
                if (tocId == null && spineToc != null) {
                    tocId = spineToc;
                }

                NodeList itemrefs = doc.getElementsByTagName("itemref");
                for (int i = 0; i < itemrefs.getLength(); i++) {
                    String idref = getAttr(itemrefs.item(i), "idref");
                    if (idref != null && manifestIdToHref.containsKey(idref)) {
                        spineHrefs.add(manifestIdToHref.get(idref));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }

        // 4. Parse Table of Contents (TOC)
        // We map FilePath -> List of Logical Chapters (TocItems) inside that file
        Map<String, List<TocItem>> fileToTocItems = new LinkedHashMap<>();
        
        if (tocId != null && manifestIdToHref.containsKey(tocId)) {
            String tocHref = manifestIdToHref.get(tocId);
            String fullTocPath = opfBasePath + tocHref;
            byte[] tocData = files.get(fullTocPath);
            
            if (tocData != null) {
                List<TocItem> tocItems = parseToc(tocData, opfBasePath); // Helper method below
                for (TocItem item : tocItems) {
                    // Resolve relative paths
                    // The parseToc helper returns paths relative to OPF base or absolute? 
                    // Let's normalize everything to the keys in the 'files' map.
                    String absPath = resolvePath(opfBasePath, item.fullPath);
                    fileToTocItems.computeIfAbsent(absPath, k -> new ArrayList<>()).add(item);
                }
            }
        }

        // 5. Iterate Spine and Extract Content
        int globalChapterIdx = 1;

        for (String href : spineHrefs) {
            String fullPath = opfBasePath + href;
            byte[] fileData = files.get(fullPath);
            if (fileData == null) continue;

            // Skip Nav/TOC files if they are in the spine (optional, depends on preference)
            if (fullPath.toLowerCase().contains("nav.x") || fullPath.toLowerCase().contains(".ncx")) continue;

            String html = new String(fileData, StandardCharsets.UTF_8);
            org.jsoup.nodes.Document doc = Jsoup.parse(html);

            // Check if this file has multiple TOC entries
            List<TocItem> tocEntries = fileToTocItems.get(fullPath);

            if (tocEntries == null || tocEntries.isEmpty()) {
                // CASE A: No TOC entry for this file. Treat whole file as one chapter.
                EpubChapterDTO chap = new EpubChapterDTO();
                chap.setChapterNumber(globalChapterIdx++);
                
                String title = extractTitleFromHtml(doc);
                chap.setTitle(title.isEmpty() ? "Chapter " + chap.getChapterNumber() : title);
                chap.setContentHtml(extractContent(doc.body()));
                finalChapters.add(chap);
            } else {
                // CASE B: One or more TOC entries. We might need to split.
                // If there is only 1 entry and it has NO fragment (#), it's the whole file.
                if (tocEntries.size() == 1 && (tocEntries.get(0).fragment == null || tocEntries.get(0).fragment.isEmpty())) {
                    EpubChapterDTO chap = new EpubChapterDTO();
                    chap.setChapterNumber(globalChapterIdx++);
                    chap.setTitle(tocEntries.get(0).title);
                    chap.setContentHtml(extractContent(doc.body()));
                    finalChapters.add(chap);
                } else {
                    // CASE C: Multiple entries or fragments. Split content.
                    List<EpubChapterDTO> splitChapters = splitFileByToc(doc, tocEntries, globalChapterIdx);
                    finalChapters.addAll(splitChapters);
                    globalChapterIdx += splitChapters.size();
                }
            }
        }

        // Handle Cover Image (Keep existing logic)
        for (var e : List.of("cover.jpg", "cover.jpeg", "images/cover.jpg", "cover.png", "OEBPS/images/cover.jpg")) {
            if (files.containsKey(e)) {
                result.setCoverImage(files.get(e));
                result.setCoverImageName(e);
                break;
            }
        }

        result.setChapters(finalChapters);
        return result;
    }

    /**
     * Splits a single HTML document into multiple chapters based on TOC anchors.
     */
    private static List<EpubChapterDTO> splitFileByToc(org.jsoup.nodes.Document doc, List<TocItem> tocItems, int startIdx) {
        List<EpubChapterDTO> chapters = new ArrayList<>();
        
        // We rely on the order of tocItems. 
        // Iterate through the body elements. When we hit an ID that matches the NEXT chapter, we switch.
        
        Elements allElements = doc.body().select("*"); // Select all elements to find IDs
        StringBuilder currentBuffer = new StringBuilder();
        
        // Prepare queue of fragments to look for
        int currentTocIndex = 0;
        TocItem currentItem = tocItems.get(0);
        
        // If the first TOC item points to a specific ID (not top of file), 
        // any content BEFORE that ID is technically "prologue" or belongs to previous. 
        // For simplicity, we attach it to the first chapter found.

        boolean findingNext = false;
        String nextId = (tocItems.size() > 1) ? tocItems.get(1).fragment : null;

        for (Element element : allElements) {
            String elId = element.id();
            
            // Check if we hit the start of the NEXT chapter
            if (nextId != null && elId != null && elId.equals(nextId)) {
                // Save current chapter
                EpubChapterDTO chap = new EpubChapterDTO();
                chap.setChapterNumber(startIdx + currentTocIndex);
                chap.setTitle(currentItem.title);
                chap.setContentHtml(currentBuffer.toString()); // Save what we have so far
                chapters.add(chap);

                // Reset buffer
                currentBuffer = new StringBuilder();
                
                // Advance to next TOC item
                currentTocIndex++;
                if (currentTocIndex < tocItems.size()) {
                    currentItem = tocItems.get(currentTocIndex);
                    // Determine the ID that ends this new chapter
                    if (currentTocIndex + 1 < tocItems.size()) {
                        nextId = tocItems.get(currentTocIndex + 1).fragment;
                    } else {
                        nextId = null; // Last chapter, goes to end of file
                    }
                }
            }
            
            // Collect content (Only <p> tags as per your requirement)
            if (element.tagName().equalsIgnoreCase("p")) {
                currentBuffer.append(element.outerHtml());
            }
        }

        // Save the final buffer
        if (currentBuffer.length() > 0 || currentTocIndex < tocItems.size()) {
            EpubChapterDTO chap = new EpubChapterDTO();
            chap.setChapterNumber(startIdx + currentTocIndex);
            chap.setTitle(currentItem.title);
            chap.setContentHtml(currentBuffer.toString());
            chapters.add(chap);
        }

        return chapters;
    }

    // --- Helpers ---

    private static String extractContent(Element body) {
        StringBuilder sb = new StringBuilder();
        for (Element p : body.select("p")) {
            sb.append(p.outerHtml());
        }
        return sb.toString();
    }

    private static String extractTitleFromHtml(org.jsoup.nodes.Document doc) {
        Element h = doc.selectFirst("h1, h2, h3, title");
        return h != null ? h.text() : "";
    }

    private static List<TocItem> parseToc(byte[] data, String opfBasePath) {
        List<TocItem> items = new ArrayList<>();
        try {
            String xml = new String(data, StandardCharsets.UTF_8);
            // Quick naive check for NCX vs HTML
            if (xml.contains("<navMap")) {
                // NCX Parsing (Old Standard)
                Document doc = parseXml(data);
                NodeList navPoints = doc.getElementsByTagName("navPoint");
                for (int i = 0; i < navPoints.getLength(); i++) {
                    ElementWrapper np = new ElementWrapper(navPoints.item(i));
                    String label = np.childText("navLabel", "text");
                    String src = np.childAttr("content", "src");
                    if (label != null && src != null) {
                        String[] parts = src.split("#");
                        items.add(new TocItem(label, parts[0], parts.length > 1 ? parts[1] : null));
                    }
                }
            } else {
                // HTML/NAV Parsing (EPUB 3)
                org.jsoup.nodes.Document doc = Jsoup.parse(xml);
                Elements links = doc.select("nav[epub:type=toc] a, nav[role=doc-toc] a, nav a"); 
                // Fallback: just grab all links if specific roles missing
                
                for (Element a : links) {
                    String href = a.attr("href");
                    String text = a.text();
                    if (!href.isEmpty()) {
                        String[] parts = href.split("#");
                        items.add(new TocItem(text, parts[0], parts.length > 1 ? parts[1] : null));
                    }
                }
            }
        } catch (Exception ignored) {}
        return items;
    }
    
    // Helper to resolve paths relative to OEBPS folder
    private static String resolvePath(String base, String relative) {
        // Simple resolution logic
        if (base.isEmpty()) return relative;
        return base + relative;
    }

    private static String getRootFilePath(Map<String, byte[]> files) {
        try {
            byte[] container = files.get("META-INF/container.xml");
            if (container == null) return null;
            Document doc = parseXml(container);
            NodeList roots = doc.getElementsByTagName("rootfile");
            if (roots.getLength() > 0) {
                return roots.item(0).getAttributes().getNamedItem("full-path").getNodeValue();
            }
        } catch (Exception e) {}
        return null;
    }

    private static Document parseXml(byte[] data) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(data));
    }

    private static String getAttr(Node n, String attr) {
        if (n == null || n.getAttributes() == null) return null;
        Node a = n.getAttributes().getNamedItem(attr);
        return a != null ? a.getNodeValue() : null;
    }

    // Mini wrapper for XML parsing (optional, cleans up code)
    private static class ElementWrapper {
        Node node;
        public ElementWrapper(Node n) { this.node = n; }
        public String childText(String parentTag, String childTag) {
            // Very basic traversal for NCX structure
            if (node.getNodeType() != Node.ELEMENT_NODE) return null;
            NodeList kids = ((org.w3c.dom.Element) node).getElementsByTagName(parentTag);
            if (kids.getLength() > 0) {
                 NodeList sub = ((org.w3c.dom.Element) kids.item(0)).getElementsByTagName(childTag);
                 if (sub.getLength() > 0) return sub.item(0).getTextContent();
            }
            return null;
        }
        public String childAttr(String tag, String attr) {
             NodeList kids = ((org.w3c.dom.Element) node).getElementsByTagName(tag);
             if (kids.getLength() > 0) return getAttr(kids.item(0), attr);
             return null;
        }
    }
}