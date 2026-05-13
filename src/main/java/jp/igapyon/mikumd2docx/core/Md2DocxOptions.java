package jp.igapyon.mikumd2docx.core;

public class Md2DocxOptions {
    private ImageLoader imageLoader;

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public interface ImageLoader {
        ImageAsset load(String path);
    }
}
