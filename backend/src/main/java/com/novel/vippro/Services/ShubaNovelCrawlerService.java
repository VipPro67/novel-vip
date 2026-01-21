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
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT_MS = 30000;
    
    /**
     * Fetch novel metadata from 69shuba
     */
    public NovelMetadata fetchNovelMetadata(String novelUrl) throws IOException {
        log.info("Fetching novel metadata from: {}", novelUrl);
        
        Document doc = Jsoup.connect(novelUrl)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .get();
        
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
        
        Document doc = Jsoup.connect(chapterListUrl)
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .get();
        
        List<ChapterInfo> chapters = new ArrayList<>();
        
        // Try different selectors for chapter links
        Elements chapterLinks = doc.select(".chapter-list a, .listmain a, #list a, .catalog a");
        
        int chapterNumber = 1;
        for (Element link : chapterLinks) {
            String href = link.attr("abs:href");
            String title = link.text().trim();
            
            if (!href.isEmpty() && !title.isEmpty()) {
                ChapterInfo chapter = new ChapterInfo();
                chapter.setChapterNumber(chapterNumber++);
                chapter.setTitle(title);
                chapter.setChapterUrl(href);
                chapter.setSourceChapterId(extractChapterId(href));
                chapters.add(chapter);
            }
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
        
        Document doc = Jsoup.connect(chapterInfo.getChapterUrl())
            .userAgent(USER_AGENT)
            .timeout(TIMEOUT_MS)
            .get();
        
        // Try different selectors for chapter content
        Element contentElement = doc.selectFirst("#content, .content, .chapter-content, .txtnav");
        
        if (contentElement == null) {
            throw new IOException("Could not find chapter content");
        }
        
        // Clean up content
        contentElement.select("script, style, .ad, .advertisement").remove();
        String contentHtml = contentElement.html().trim();
        
        // Convert to proper paragraphs if needed
        contentHtml = normalizeContent(contentHtml);
        
        ShubaChapterDTO chapter = new ShubaChapterDTO();
        chapter.setChapterNumber(chapterInfo.getChapterNumber());
        chapter.setTitle(chapterInfo.getTitle());
        chapter.setContentHtml(contentHtml);
        chapter.setSourceChapterId(chapterInfo.getSourceChapterId());
        
        log.info("Successfully fetched chapter {}", chapterInfo.getChapterNumber());
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
                Thread.sleep(1000 + (long)(Math.random() * 1000));
                
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
    
    private String extractChapterId(String url) {
        // Extract chapter ID from URL (usually numeric)
        Pattern pattern = Pattern.compile("/(\\d+)\\.html?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return url;
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
