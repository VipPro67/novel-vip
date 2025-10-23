package com.novel.vippro.Messaging.payload;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterAudioMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID chapterId;
    private UUID jobId;
    private UUID novelId;
    private String novelSlug;
    private Integer chapterNumber;
    private UUID userId;
}
