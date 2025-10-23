package com.novel.vippro.Messaging;

import com.novel.vippro.Messaging.payload.ChapterAudioMessage;
import com.novel.vippro.Messaging.payload.EpubImportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(AsyncTaskPublisher.class)
public class NoOpAsyncTaskPublisher implements AsyncTaskPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NoOpAsyncTaskPublisher.class);

    @Override
    public void publishEpubImport(EpubImportMessage message) {
        logger.warn("No async task publisher configured. EPUB job {} will not be processed.", message.getJobId());
    }

    @Override
    public void publishChapterAudio(ChapterAudioMessage message) {
        logger.warn("No async task publisher configured. Chapter {} audio job dropped.", message.getChapterId());
    }
}
