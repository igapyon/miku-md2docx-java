package jp.igapyon.mikumd2docx.core;

final class Relationship {
    final String id;
    final String type;
    final String target;
    final String targetMode;

    Relationship(String id, String type, String target, String targetMode) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.targetMode = targetMode;
    }
}
