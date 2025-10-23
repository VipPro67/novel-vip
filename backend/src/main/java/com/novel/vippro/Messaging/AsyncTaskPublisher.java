package com.novel.vippro.Messaging;

import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;

public interface AsyncTaskPublisher {
    void publishEpubImport(EpubImportMessage message);

    void publishChapterAudio(ChapterAudioMessage message);
}
