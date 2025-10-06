package com.novel.vippro.DTO.Novel;

/**
 * Projection returned by search suggestion endpoints regardless of the underlying engine.
 */
public record SearchSuggestion(String id, String title) {
}
