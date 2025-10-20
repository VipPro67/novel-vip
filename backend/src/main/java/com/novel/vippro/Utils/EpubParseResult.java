package com.novel.vippro.Utils;

import lombok.Data;
import java.util.List;

@Data
public class EpubParseResult {
    private String title;
    private String author;
    private byte[] coverImage; // may be null
    private String coverImageName;
    private List<EpubChapterDTO> chapters;
}
