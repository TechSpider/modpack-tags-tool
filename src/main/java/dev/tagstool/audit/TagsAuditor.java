package dev.tagstool.audit;

import dev.tagstool.model.TagAuditResult;
import dev.tagstool.model.TagDefinition;
import dev.tagstool.model.TagId;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TagsAuditor {
    public List<TagAuditResult> audit(List<TagDefinition> definitions) {
        List<TagAuditResult> results = new ArrayList<>();

        results.addAll(checkReplacementConflicts(definitions)); //Check for any tags whose definitions may be potentially replaced
        results.addAll(checkMissingReferences(definitions)); //Check for any tags referencing undefined or empty tags

        results.forEach(tagAuditResult -> log.warn("{}: {}", tagAuditResult.type(), tagAuditResult.message()));

        return results;
    }

    private List<TagAuditResult> checkReplacementConflicts(List<TagDefinition> definitions) {
        Map<TagId, List<TagDefinition>> tagDefinitionsById = definitions.stream().collect(Collectors.groupingBy(TagDefinition::id));

        List<TagAuditResult> tagAuditResults = new ArrayList<>();

        tagDefinitionsById.forEach((id, tagDefinitions) -> {
            if (tagDefinitions.size() > 1) {
               tagDefinitions.forEach(tagDefinition -> {
                    if (tagDefinition.replace()) {
                        String sourceDescription = tagDefinition.jsonPath() == null ? tagDefinition.sourcePath().toString().replace('\\', '/') : tagDefinition.sourcePath().getFileName() + "!" + tagDefinition.jsonPath();
                        tagAuditResults.add(new TagAuditResult("REPLACEMENT CONFLICT", "Some definitions of " + id.toString() + " are potentially replaced by " + sourceDescription, id));
                    }
               });
            }
        });

        return tagAuditResults;
    }

    private List<TagAuditResult> checkMissingReferences(List<TagDefinition> definitions) {
        Map<TagId, List<TagDefinition>> tagDefinitionsById = definitions.stream().collect(Collectors.groupingBy(TagDefinition::id));

        List<TagAuditResult> tagAuditResults = new ArrayList<>();

        definitions.forEach(tagDefinition -> {
            tagDefinition.definitionTags().forEach(tagEntry -> {
                if (!tagEntry.isTagReference() || tagEntry.id().startsWith("#minecraft:") || !tagEntry.id().contains(":") || !tagEntry.isRequired()) {
                    return;
                }

                String referencedId = tagEntry.id().substring(1);
                String[] parts = referencedId.split(":", 2);

                String namespace = parts[0];
                String path = parts[1];

                TagId referencedTagId = new TagId(
                        namespace,
                        path,
                        tagDefinition.id().type()
                );

                if (!tagDefinitionsById.containsKey(referencedTagId)) {
                    tagAuditResults.add(new TagAuditResult("MISSING REFERENCE", "Tag " + referencedTagId + " is referenced by " + tagDefinition.id() + " but has no definition in the scanned files", tagDefinition.id()));
                }
            });
        });

        return  tagAuditResults;
    }

}
