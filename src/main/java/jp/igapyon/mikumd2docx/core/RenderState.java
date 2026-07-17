package jp.igapyon.mikumd2docx.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class RenderState {
    final Md2DocxOptions options;
    final Md2DocxSummary summary = new Md2DocxSummary();
    final List<Relationship> relationships = new ArrayList<Relationship>();
    final Map<String, byte[]> imageMedia = new LinkedHashMap<String, byte[]>();
    final Map<String, List<String>> headingBookmarks = new LinkedHashMap<String, List<String>>();
    final Map<String, Integer> headingRenderCounts = new HashMap<String, Integer>();
    final Set<String> knownBookmarks = new LinkedHashSet<String>();
    final Set<String> referenceDefinitions = new LinkedHashSet<String>();
    final LoadedDocxTemplatePackage templatePackage;
    int nextRelId = 1;
    int nextDocPrId = 1;

    RenderState(Md2DocxOptions options) {
        this.options = options;
        this.templatePackage = options.getTemplateDocx() == null
                ? null
                : LoadedDocxTemplatePackage.load(options.getTemplateDocx());
    }

    String nextHeadingBookmark(String headingText) {
        List<String> candidates = headingBookmarks.get(headingText);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Integer index = headingRenderCounts.get(headingText);
        int current = index == null ? 0 : index.intValue();
        headingRenderCounts.put(headingText, current + 1);
        return current < candidates.size() ? candidates.get(current) : candidates.get(candidates.size() - 1);
    }

    String addRelationship(String type, String target, String targetMode) {
        String id = "rId" + nextRelId++;
        relationships.add(new Relationship(id, type, target, targetMode));
        return id;
    }
}
