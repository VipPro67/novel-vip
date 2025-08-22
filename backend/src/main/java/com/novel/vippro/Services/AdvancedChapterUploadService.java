package com.novel.vippro.Services;

import com.novel.vippro.DTO.Chapter.CreateChapterDTO;
import com.novel.vippro.DTO.Chapter.UploadChapterResult;
import com.novel.vippro.Models.Chapter;
import com.novel.vippro.Repository.ChapterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdvancedChapterUploadService {

    private final ChapterService chapterService;      
    private final ChapterRepository chapterRepository;

    private static final Pattern NUM_PREFIX = Pattern.compile("^(\\d{1,6})"); 
    private static final Set<String> MD_EXT = Set.of("md", "markdown");
    private static final Set<String> HTML_EXT = Set.of("html", "htm");
    private static final String DOCX = "docx";

    public enum IngestFormat { TXT, MD, HTML, DOCX }

    // === Public API ==========================================================

    @Transactional
    public UploadChapterResult uploadOneAdvanced(
            UUID novelId,
            MultipartFile file,
            Integer chapterNumberParam,  
            String titleParam,            
            boolean overwrite
    ) {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        try {
            IngestFormat fmt = detectFormat(originalName, file.getContentType());

            Integer chapterNo = (chapterNumberParam != null)
                    ? chapterNumberParam
                    : extractChapterNumber(originalName).orElseThrow(() ->
                        new IllegalArgumentException("Missing chapterNumber and filename does not start with a number: " + originalName));

            Chapter existing = chapterRepository.findByNovelIdAndChapterNumber(novelId, chapterNo);
            if (existing != null && !overwrite) {
                return new UploadChapterResult(chapterNo, null, false,
                        "Chapter exists. Set overwrite=true to replace.", originalName);
            }
            if (existing != null && overwrite) {
                chapterRepository.delete(existing);
            }

            ConversionResult conv = switch (fmt) {
                case TXT -> parseTxtToHtml(file.getInputStream(), titleParam);
                case MD -> parseMarkdownToHtml(file.getInputStream(), titleParam);
                case HTML -> parseHtmlToSanitizedHtml(file.getInputStream(), titleParam);
                case DOCX -> parseDocxToHtml(file.getInputStream(), titleParam);
            };

            CreateChapterDTO dto = new CreateChapterDTO();
            dto.setNovelId(novelId);
            dto.setChapterNumber(chapterNo);
            dto.setTitle(conv.title() != null ? conv.title() : ("Chapter " + chapterNo));
            dto.setContentHtml(conv.html()); 

            chapterService.createChapter(dto); 

            return new UploadChapterResult(chapterNo, dto.getTitle(), true, "Created", originalName);

        } catch (Exception e) {
            return new UploadChapterResult(null, null, false,
                    "Failed: " + e.getMessage(), originalName);
        }
    }

    @Transactional
    public List<UploadChapterResult> uploadManyAdvanced(
            UUID novelId,
            List<MultipartFile> files,
            boolean overwrite
    ) {
        List<UploadChapterResult> out = new ArrayList<>();
        for (MultipartFile f : files) {
            out.add(uploadOneAdvanced(novelId, f, null, null, overwrite));
        }
        return out;
    }

    // === Detection / Parsing ================================================

    private IngestFormat detectFormat(String filename, String contentType) {
        String ext = extensionOf(filename).toLowerCase(Locale.ROOT);
        if (MD_EXT.contains(ext)) return IngestFormat.MD;
        if (HTML_EXT.contains(ext)) return IngestFormat.HTML;
        if (DOCX.equals(ext)) return IngestFormat.DOCX;

        if (contentType != null) {
            if (contentType.contains("markdown")) return IngestFormat.MD;
            if (contentType.contains("html")) return IngestFormat.HTML;
            if (contentType.contains("officedocument.wordprocessingml.document")) return IngestFormat.DOCX;
        }
        return IngestFormat.TXT;
    }

    private Optional<Integer> extractChapterNumber(String filename) {
        if (filename == null) return Optional.empty();
        String name = stripExt(filename.trim());
        Matcher m = NUM_PREFIX.matcher(name);
        if (m.find()) {
            return Optional.of(Integer.parseInt(m.group(1)));
        }
        return Optional.empty();
    }

    private String extensionOf(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1) : "";
    }

    private String stripExt(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(0, i) : filename;
    }

    // === Converters =========================================================

    private record ConversionResult(String html, String title) {}

    /** TXT: first non-empty line as title if titleParam is null; rest is content. */
    private ConversionResult parseTxtToHtml(InputStream is, String titleParam) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String first = r.readLine();
            String derivedTitle = (titleParam != null && !titleParam.isBlank())
                    ? titleParam
                    : (first != null ? first.trim() : null);

            StringBuilder body = new StringBuilder();
            if (derivedTitle != null && first != null && first.trim().equals(derivedTitle)) {
            } else if (first != null) {
                body.append(first).append("\n");
            }
            String line;
            while ((line = r.readLine()) != null) {
                body.append(line).append("\n");
            }

            String html = textToHtml(body.toString());
            return new ConversionResult(sanitizeHtml(html), derivedTitle);
        }
    }

    /** Markdown → HTML; derive title from first ATX heading if missing. */
    private ConversionResult parseMarkdownToHtml(InputStream is, String titleParam) throws IOException {
        String md = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String derivedTitle = titleParam;
        if (derivedTitle == null || derivedTitle.isBlank()) {
            try (BufferedReader r = new BufferedReader(new StringReader(md))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().startsWith("#")) {
                        derivedTitle = line.replaceFirst("^#+\\s*", "").trim();
                        break;
                    }
                }
            }
        }
        Parser parser = Parser.builder().build();
        Node doc = parser.parse(md);
        String html = HtmlRenderer.builder().build().render(doc);
        return new ConversionResult(sanitizeHtml(html), derivedTitle);
    }

    /** HTML: sanitize; derive title from first <h1> or <title> if missing. */
    private ConversionResult parseHtmlToSanitizedHtml(InputStream is, String titleParam) throws IOException {
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        String sanitized = sanitizeHtml(raw);
        String derivedTitle = titleParam;
        if (derivedTitle == null || derivedTitle.isBlank()) {
            Document d = Jsoup.parse(sanitized);
            String h1 = d.selectFirst("h1") != null ? d.selectFirst("h1").text() : null;
            if (h1 != null && !h1.isBlank()) derivedTitle = h1;
            if ((derivedTitle == null || derivedTitle.isBlank()) && d.title() != null) {
                derivedTitle = d.title().trim();
            }
        }
        return new ConversionResult(sanitized, derivedTitle);
    }

    /** DOCX → HTML with docx4j; derive title from first heading if missing. */
    private ConversionResult parseDocxToHtml(InputStream is, String titleParam) throws Exception {
        WordprocessingMLPackage pkg = WordprocessingMLPackage.load(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        org.docx4j.convert.out.HTMLSettings htmlSettings = new org.docx4j.convert.out.HTMLSettings();
        htmlSettings.setWmlPackage(pkg);

        Docx4J.toHTML(htmlSettings, baos, Docx4J.FLAG_EXPORT_PREFER_XSL);

        String html = baos.toString(StandardCharsets.UTF_8);
        String sanitized = sanitizeHtml(html);

        String derivedTitle = titleParam;
        if (derivedTitle == null || derivedTitle.isBlank()) {
            Document d = Jsoup.parse(sanitized);
            for (int h = 1; h <= 3 && (derivedTitle == null || derivedTitle.isBlank()); h++) {
                var el = d.selectFirst("h" + h);
                if (el != null && !el.text().isBlank()) derivedTitle = el.text().trim();
            }
            if (derivedTitle == null || derivedTitle.isBlank()) {
                var p = d.selectFirst("p");
                if (p != null && !p.text().isBlank()) derivedTitle = p.text().trim();
            }
        }
        return new ConversionResult(sanitized, derivedTitle);
    }

    // === HTML helpers =======================================================

    private String textToHtml(String text) {
        if (text == null) return "";
        String escaped = org.apache.commons.text.StringEscapeUtils.escapeHtml4(text);
        String[] paragraphs = escaped.split("\\R{2,}");
        StringBuilder html = new StringBuilder();
        for (String p : paragraphs) {
            html.append("<p>").append(p.replaceAll("\\R", "<br/>")).append("</p>");
        }
        return html.toString();
    }

    private String sanitizeHtml(String rawHtml) {
        if (rawHtml == null) return "";
        Safelist safelist = Safelist.relaxed()
                .addAttributes("a", "target", "rel")
                .preserveRelativeLinks(true);
        String cleaned = Jsoup.clean(rawHtml, safelist);
        return cleaned.replace("<a ", "<a target=\"_blank\" rel=\"noopener noreferrer\" ");
    }
}
