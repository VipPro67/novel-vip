package com.novel.vippro.Services;

import com.novel.vippro.DTO.NovelSource.ShubaChapterDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShubaNovelCrawlerService {

    private final FlareSolverrService flareSolverrService;

    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MS = 2000;

    private String sessionId = null;

    /**
     * Fetch HTML with retry logic using FlareSolverr
     */
    private Document fetchWithRetry(String url) throws IOException {
        Exception lastException = null;

        // Ensure we have a session
        if (sessionId == null) {
            try {
                sessionId = flareSolverrService.createSession();
            } catch (Exception e) {
                log.warn("Failed to create FlareSolverr session, will proceed without session: {}", e.getMessage());
            }
        }

        for (int attempt = 0; attempt < RETRY_COUNT; attempt++) {
            try {
                log.debug("Fetching {} via FlareSolverr (attempt {}/{})", url, attempt + 1, RETRY_COUNT);
                String html = flareSolverrService.fetchHtml(url, sessionId);
                return Jsoup.parse(html, url);

            } catch (Exception e) {
                lastException = e;
                log.warn("Fetch attempt {} failed: {}. Retrying in {}ms...",
                        attempt + 1, e.getMessage(), RETRY_DELAY_MS);

                if (attempt < RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * (attempt + 1)); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry delay", ie);
                    }
                }
            }
        }

        throw new IOException("Failed to fetch " + url + " after " + RETRY_COUNT + " attempts", lastException);
    }

    /**
     * Fetch novel metadata from 69shuba
     */
    public NovelMetadata fetchNovelMetadata(String novelUrl) throws IOException {
        log.info("Fetching novel metadata from: {}", novelUrl);

        Document doc = fetchWithRetry(novelUrl);

        NovelMetadata metadata = new NovelMetadata();

        // Extract title
        Element titleElement = doc.selectFirst("h1");
        if (titleElement != null) {
            metadata.setTitle(titleElement.text().trim());
        }

        // Extract author
        Element authorElement = doc.selectFirst(".author, .info .author a, meta[property='og:novel:author']");
        if (authorElement != null) {
            metadata.setAuthor(authorElement.text().trim());
        }

        // Extract description
        Element descElement = doc.selectFirst(".intro, .description, #intro");
        if (descElement != null) {
            metadata.setDescription(descElement.html().trim());
        }

        // Extract chapter list URL (usually in the same page or linked)
        String chapterListUrl = novelUrl;
        if (!chapterListUrl.contains("/catalogue") && !chapterListUrl.endsWith(".html")) {
            chapterListUrl = novelUrl + "/";
        }
        metadata.setChapterListUrl(chapterListUrl);

        log.info("Fetched metadata for novel: {}", metadata.getTitle());
        return metadata;
    }

    /**
     * Fetch chapter list from 69shuba
     */
    public List<ChapterInfo> fetchChapterList(String chapterListUrl) throws IOException {
        log.info("Fetching chapter list from: {}", chapterListUrl);

        Document doc = fetchWithRetry(chapterListUrl);

        List<ChapterInfo> chapters = new ArrayList<>();

        Elements chapterLinks = doc.select("div#catalog ul li a");

        for (Element link : chapterLinks) {
            String href = link.attr("abs:href");
            String title = link.text().trim();

            if (href.isEmpty() || title.isEmpty()) {
                continue;
            }

            // Extract chapter number from title like "第57章 意炁" -> 57
            int chapterNumber = extractChapterNumberFromTitle(title);
            if (chapterNumber <= 0) {
                log.warn("Could not extract chapter number from title: {}", title);
                continue;
            }

            ChapterInfo chapter = new ChapterInfo();
            chapter.setChapterNumber(chapterNumber);
            chapter.setTitle(title);
            chapter.setChapterUrl(href);
            chapter.setSourceChapterId(extractChapterId(href));
            chapters.add(chapter);
        }

        log.info("Found {} chapters", chapters.size());
        return chapters;
    }

    /**
     * Fetch a single chapter content
     */
    public ShubaChapterDTO fetchChapter(ChapterInfo chapterInfo) throws IOException {
        log.info("Fetching chapter {}: {} from {}",
                chapterInfo.getChapterNumber(), chapterInfo.getTitle(), chapterInfo.getChapterUrl());

        Document doc = fetchWithRetry(chapterInfo.getChapterUrl());

        // 1. TARGET: The screenshot shows the content is inside 'div.txtnav'
        Element contentElement = doc.selectFirst(".txtnav");

        if (contentElement == null) {
            // Fallback selectors just in case
            contentElement = doc.selectFirst("#content, .content, .chapter-content");
        }

        if (contentElement == null) {
            throw new IOException("Could not find chapter content");
        }

        // 2. CLEAN UP: Remove the specific garbage elements seen in the DOM tree
        // h1 -> Removes the "第12章..." header
        // .txtinfo -> Removes the metadata line
        // #txtright -> Removes the top right navigation/links
        contentElement.select("h1, .txtinfo, #txtright, script, style, .ad").remove();

        // 3. EXTRACTION:
        // The text in the image is "loose" (Direct text nodes separated by <br>).
        // We get the HTML to preserve the <br> tags for formatting.
        String contentHtml = contentElement.html();

        // 4. CLEANUP TEXT:
        // The screenshot shows "&emsp;" (tab spaces) which might clutter the output.
        contentHtml = contentHtml.replace("&emsp;", " ").trim();
        String title = chapterInfo.getTitle(); // e.g., "第12章 金光术"
        if (contentHtml.startsWith(title)) {
            contentHtml = contentHtml.substring(title.length()).trim();
        }
        contentHtml = normalizeContent(contentHtml);

        ShubaChapterDTO chapter = new ShubaChapterDTO();
        chapter.setChapterNumber(chapterInfo.getChapterNumber());
        chapter.setTitle(chapterInfo.getTitle());
        chapter.setContentHtml(contentHtml);
        chapter.setSourceChapterId(chapterInfo.getSourceChapterId());

        return chapter;
    }

    /**
     * Fetch multiple chapters with rate limiting
     */
    public List<ShubaChapterDTO> fetchChapters(List<ChapterInfo> chapterInfos, int startIndex, int endIndex) {
        List<ShubaChapterDTO> chapters = new ArrayList<>();

        int start = Math.max(0, startIndex);
        int end = Math.min(chapterInfos.size(), endIndex);

        for (int i = start; i < end; i++) {
            try {
                ShubaChapterDTO chapter = fetchChapter(chapterInfos.get(i));
                chapters.add(chapter);

                // Rate limiting: wait 1-2 seconds between requests
                Thread.sleep(1000 + (long) (Math.random() * 1000));

            } catch (IOException e) {
                log.error("Failed to fetch chapter {}: {}", chapterInfos.get(i).getChapterNumber(), e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Chapter fetch interrupted");
                break;
            }
        }

        return chapters;
    }

    /**
     * Clean up resources (destroy FlareSolverr session)
     */
    public void cleanup() {
        if (sessionId != null) {
            try {
                flareSolverrService.destroySession(sessionId);
                sessionId = null;
            } catch (Exception e) {
                log.warn("Failed to destroy FlareSolverr session: {}", e.getMessage());
            }
        }
    }

    private String extractChapterId(String url) {
        // Extract chapter ID from URL (usually numeric)
        Pattern pattern = Pattern.compile("/(\\d+)\\.html?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
    }

    private int extractChapterNumberFromTitle(String title) {
        // Extract chapter number from Chinese format like "第57章 意炁" -> 57
        // Pattern: 第 + numbers + 章
        Pattern pattern = Pattern.compile("第(\\d+)章");
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse chapter number from: {}", matcher.group(1));
            }
        }
        return -1;
    }

    private String normalizeContent(String html) {
        // Split by <br> tags and wrap in <p> tags if not already
        if (!html.contains("<p>")) {
            String[] lines = html.split("<br\\s*/?>|<br>");
            StringBuilder normalized = new StringBuilder();
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    normalized.append("<p>").append(trimmed).append("</p>\n");
                }
            }
            return normalized.toString();
        }
        return html;
    }

    // Inner classes for metadata
    @lombok.Data
    public static class NovelMetadata {
        private String title;
        private String author;
        private String description;
        private String chapterListUrl;
    }

    @lombok.Data
    public static class ChapterInfo {
        private int chapterNumber;
        private String title;
        private String chapterUrl;
        private String sourceChapterId;
    }
}
