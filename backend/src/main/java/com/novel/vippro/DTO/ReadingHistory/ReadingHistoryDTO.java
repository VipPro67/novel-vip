package com.novel.vippro.DTO.ReadingHistory;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

import com.novel.vippro.DTO.Novel.NovelDTO;
import com.novel.vippro.DTO.base.BaseDTO;

@Data
public class ReadingHistoryDTO extends BaseDTO {
    private UUID userId;
    private NovelDTO novel;
    private int lastReadChapterIndex;
    private Instant lastReadAt;
}
