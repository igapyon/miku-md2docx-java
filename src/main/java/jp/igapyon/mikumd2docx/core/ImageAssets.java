package jp.igapyon.mikumd2docx.core;

import java.util.Locale;

final class ImageAssets {
    private static final int DOC_BODY_WIDTH_EMU = 5943600;
    private static final int EMU_PER_PIXEL_AT_96_DPI = 9525;

    private ImageAssets() {
    }

    static String safeMediaName(int index, String path) {
        String file = path.replace('\\', '/');
        int slash = file.lastIndexOf('/');
        if (slash >= 0) {
            file = file.substring(slash + 1);
        }
        String ext = "bin";
        int dot = file.lastIndexOf('.');
        if (dot >= 0) {
            ext = file.substring(dot + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }
        if (ext.isEmpty()) {
            ext = "bin";
        }
        return "image-" + index + "." + ext;
    }

    static String contentTypeForExt(String ext) {
        if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            return "image/jpeg";
        }
        if ("gif".equals(ext)) {
            return "image/gif";
        }
        if ("webp".equals(ext)) {
            return "image/webp";
        }
        if ("png".equals(ext)) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    static ImageSize displaySizeForImage(byte[] data) {
        ImageSize natural = imageSize(data);
        int naturalWidth = (natural == null ? 320 : natural.width) * EMU_PER_PIXEL_AT_96_DPI;
        int naturalHeight = (natural == null ? 240 : natural.height) * EMU_PER_PIXEL_AT_96_DPI;
        int width = Math.min(naturalWidth, DOC_BODY_WIDTH_EMU);
        int height = (int) Math.round((double) width * naturalHeight / naturalWidth);
        return new ImageSize(width, height, width < naturalWidth);
    }

    private static ImageSize imageSize(byte[] data) {
        if (data.length >= 24 && data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4e && data[3] == 0x47) {
            return new ImageSize(readBe32(data, 16), readBe32(data, 20), false);
        }
        if (data.length >= 10 && data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46) {
            return new ImageSize(readLe16(data, 6), readLe16(data, 8), false);
        }
        if (data.length >= 4 && data[0] == (byte) 0xff && data[1] == (byte) 0xd8) {
            int offset = 2;
            while (offset + 9 <= data.length) {
                if (data[offset] != (byte) 0xff) {
                    return null;
                }
                int marker = data[offset + 1] & 0xff;
                int length = readBe16(data, offset + 2);
                if (marker >= 0xc0 && marker <= 0xc3) {
                    return new ImageSize(readBe16(data, offset + 7), readBe16(data, offset + 5), false);
                }
                offset += 2 + length;
            }
        }
        return null;
    }

    private static int readBe16(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }

    private static int readLe16(byte[] data, int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
    }

    private static int readBe32(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16) | ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff);
    }

    static final class ImageSize {
        final int width;
        final int height;
        final boolean resized;

        ImageSize(int width, int height, boolean resized) {
            this.width = width;
            this.height = height;
            this.resized = resized;
        }
    }
}
