package jp.igapyon.mikumd2docx.core;

public class ImageAsset {
    private final String path;
    private final byte[] data;

    public ImageAsset(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }
}
