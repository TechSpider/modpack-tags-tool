package dev.tagstool.model;

public record TagAuditResult(
        String type,
        String message,
        TagId tagId
) {}
