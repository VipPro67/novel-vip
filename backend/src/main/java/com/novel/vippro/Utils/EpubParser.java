package com.novel.vippro.Utils;

import org.w3c.dom.Document;
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

    public static EpubParseResult parse(byte[] epubBytes) throws IOException {
        EpubParseResult result = new EpubParseResult();
        List<EpubChapterDTO> chapters = new ArrayList<>();

        Map<String, byte[]> files = new HashMap<>();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(epubBytes);
             ZipInputStream zis = new ZipInputStream(bais)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                byte[] data = zis.readAllBytes();
                files.put(entry.getName(), data);
            }
        }

        // Attempt to read container to find rootfile
        String rootfilePath = "";
        try {
            byte[] container = files.get("META-INF/container.xml");
            if (container != null) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                try (InputStream is = new ByteArrayInputStream(container)) {
                    Document doc = db.parse(is);
                    NodeList roots = doc.getElementsByTagName("rootfile");
                    if (roots.getLength() > 0) {
                        rootfilePath = roots.item(0).getAttributes().getNamedItem("full-path").getNodeValue();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // parse package document
        byte[] packageDoc = files.get(rootfilePath.isEmpty() ? "content.opf" : rootfilePath);
        if (packageDoc != null) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                try (InputStream is = new ByteArrayInputStream(packageDoc)) {
                    Document doc = db.parse(is);
                    // title
                    NodeList titles = doc.getElementsByTagName("dc:title");
                    if (titles.getLength() > 0) result.setTitle(titles.item(0).getTextContent());
                    NodeList creators = doc.getElementsByTagName("dc:creator");
                    if (creators.getLength() > 0) result.setAuthor(creators.item(0).getTextContent());

                    // manifest and spine
                    Map<String, String> manifestHrefById = new HashMap<>();
                    NodeList items = doc.getElementsByTagName("item");
                    for (int i = 0; i < items.getLength(); i++) {
                        var n = items.item(i);
                        var idAttr = n.getAttributes().getNamedItem("id");
                        var hrefAttr = n.getAttributes().getNamedItem("href");
                        if (idAttr != null && hrefAttr != null) {
                            manifestHrefById.put(idAttr.getNodeValue(), hrefAttr.getNodeValue());
                        }
                    }

                    List<String> spineOrder = new ArrayList<>();
                    NodeList itemrefs = doc.getElementsByTagName("itemref");
                    for (int i = 0; i < itemrefs.getLength(); i++) {
                        var n = itemrefs.item(i);
                        var idref = n.getAttributes().getNamedItem("idref");
                        if (idref != null) {
                            String href = manifestHrefById.get(idref.getNodeValue());
                            if (href != null) spineOrder.add(href);
                        }
                    }

                    int idx = 1;
                    // detect nav base name to avoid saving nav/toc as a chapter
                    String navBaseName = null;
                    if (files.keySet().stream().anyMatch(k -> k.toLowerCase().contains("nav") && (k.toLowerCase().endsWith(".xhtml") || k.toLowerCase().endsWith(".html")))) {
                        String navKey = files.keySet().stream()
                                .filter(k -> k.toLowerCase().contains("nav") && (k.toLowerCase().endsWith(".xhtml") || k.toLowerCase().endsWith(".html")))
                                .findFirst().orElse(null);
                        if (navKey != null) navBaseName = navKey.contains("/") ? navKey.substring(navKey.lastIndexOf('/') + 1) : navKey;
                    }

                    for (String href : spineOrder) {
                    String hrefLower = href.toLowerCase();
                    String hrefBase = href.contains("/") ? href.substring(href.lastIndexOf('/') + 1) : href;
                    if ((navBaseName != null && hrefBase.equalsIgnoreCase(navBaseName))
                            || hrefLower.contains("nav")
                            || hrefLower.contains("toc")) {
                        continue;
                    }

                    String base = "";
                    int lastSlash = rootfilePath.lastIndexOf('/');
                    if (lastSlash > 0) base = rootfilePath.substring(0, lastSlash + 1);
                    String fullPath = base + href;
                    byte[] itemData = files.getOrDefault(fullPath, files.get(href));
                    if (itemData == null) continue;

                    String html = new String(itemData, StandardCharsets.UTF_8);
                    org.jsoup.nodes.Document doc2 = org.jsoup.Jsoup.parse(html);

                    // extract only <p> tags
                    StringBuilder contentBuilder = new StringBuilder();
                    for (org.jsoup.nodes.Element p : doc2.select("p")) {
                        contentBuilder.append(p.outerHtml());
                    }
                    String content = contentBuilder.toString().trim();

                    EpubChapterDTO chap = new EpubChapterDTO();
                    chap.setChapterNumber(idx++);
                    // use first heading or fallback to file name
                    String title = Optional.ofNullable(doc2.selectFirst("h1, h2"))
                            .map(org.jsoup.nodes.Element::text)
                            .filter(t -> !t.isBlank())
                            .orElse(href);
                    chap.setTitle(title);
                    chap.setContentHtml(content.isEmpty() ? html : content);
                    chapters.add(chap);
                }

                }
            } catch (Exception ignored) {
            }
        }

        // try to find common cover image names if present
        for (var e : List.of("cover.jpg", "cover.jpeg", "images/cover.jpg", "cover.png")) {
            if (files.containsKey(e)) {
                result.setCoverImage(files.get(e));
                result.setCoverImageName(e);
                break;
            }
        }

        // attempt to parse navigation (nav.xhtml / nav.html) to extract titles
        Map<String, String> navTitles = new HashMap<>();
        try {
            // find a nav file candidate
            String navKey = files.keySet().stream()
                    .filter(k -> k.toLowerCase().contains("nav") && (k.toLowerCase().endsWith(".xhtml") || k.toLowerCase().endsWith(".html")))
                    .findFirst().orElse(null);
            if (navKey != null) {
                byte[] navData = files.get(navKey);
                if (navData != null) {
                    org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(new String(navData, StandardCharsets.UTF_8));
                    // look for <a href="...">Title</a>
                    for (org.jsoup.nodes.Element a : doc.select("a[href]")) {
                        String href = a.attr("href").trim();
                        String text = a.text().trim();
                        if (!href.isEmpty() && !text.isEmpty()) {
                            // href in nav may be relative; keep last segment
                            String normalized = href.contains("#") ? href.substring(0, href.indexOf('#')) : href;
                            normalized = normalized.replaceFirst("^./", "");
                            navTitles.put(normalized, text);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // apply nav titles to chapters where possible
        for (EpubChapterDTO chap : chapters) {
            String href = chap.getTitle(); // previously set to href
            String key = href;
            if (key.contains("/")) key = key.substring(key.lastIndexOf('/') + 1);
            if (navTitles.containsKey(href)) {
                chap.setTitle(navTitles.get(href));
            } else if (navTitles.containsKey(key)) {
                chap.setTitle(navTitles.get(key));
            }
        }

        result.setChapters(chapters);
        return result;
    }
}
