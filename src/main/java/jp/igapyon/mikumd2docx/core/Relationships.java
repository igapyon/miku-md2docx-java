package jp.igapyon.mikumd2docx.core;

import java.util.ArrayList;
import java.util.List;
import jp.igapyon.mikumsofficecore.OpcRelationship;
import jp.igapyon.mikumsofficecore.OpcRelationships;

final class Relationships {
    static final String REL_HYPERLINK = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
    static final String REL_IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
    private static final String REL_STYLES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
    private static final String REL_NUMBERING = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering";
    private static final String REL_SETTINGS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings";

    private Relationships() {
    }

    static String documentRelsXml(List<Relationship> relationships) {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rIdStyles", REL_STYLES, "styles.xml"));
        rels.add(new OpcRelationship("rIdNumbering", REL_NUMBERING, "numbering.xml"));
        rels.add(new OpcRelationship("rIdSettings", REL_SETTINGS, "settings.xml"));
        for (Relationship rel : relationships) {
            if (rel.targetMode == null) {
                rels.add(new OpcRelationship(rel.id, rel.type, rel.target));
            } else {
                rels.add(new OpcRelationship(rel.id, rel.type, rel.target, rel.targetMode));
            }
        }
        return OpcRelationships.buildOpcRelationshipsXml(rels);
    }
}
