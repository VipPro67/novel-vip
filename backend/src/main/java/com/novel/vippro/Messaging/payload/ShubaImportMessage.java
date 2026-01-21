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
public class ShubaImportMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID jobId;
    private UUID userId;
    private UUID novelId;
    private UUID novelSourceId;
    private Integer startChapter;
    private Integer endChapter;
    private Boolean fullImport;
}
