package com.novel.vippro.Repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.novel.vippro.Models.NovelDocument;

import java.util.UUID;

@Repository
public interface NovelSearchRepository extends ElasticsearchRepository<NovelDocument, UUID> {
}
