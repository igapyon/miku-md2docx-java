package jp.igapyon.mikumd2docx.core;

public class Md2DocxOptions {
    private ImageLoader imageLoader;
    private byte[] templateDocx;

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public byte[] getTemplateDocx() {
        return templateDocx;
    }

    public void setTemplateDocx(byte[] templateDocx) {
        this.templateDocx = templateDocx;
    }

    public interface ImageLoader {
        ImageAsset load(String path);
    }
}
