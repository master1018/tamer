public final class GLUtils {
    static {
        nativeClassInit();
    }
    private GLUtils() {
    }
    public static int getInternalFormat(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("getInternalFormat can't be used with a null Bitmap");
        }
        int result = native_getInternalFormat(bitmap);
        if (result < 0) {
            throw new IllegalArgumentException("Unknown internalformat");
        }
        return result;
    }
    public static int getType(Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("getType can't be used with a null Bitmap");
        }
        int result = native_getType(bitmap);
        if (result < 0) {
            throw new IllegalArgumentException("Unknown type");
        }
        return result;
    }
    public static void texImage2D(int target, int level, int internalformat,
            Bitmap bitmap, int border) {
        if (bitmap == null) {
            throw new NullPointerException("texImage2D can't be used with a null Bitmap");
        }
        if (native_texImage2D(target, level, internalformat, bitmap, -1, border)!=0) {
            throw new IllegalArgumentException("invalid Bitmap format");
        }
    }
    public static void texImage2D(int target, int level, int internalformat,
            Bitmap bitmap, int type, int border) {
        if (bitmap == null) {
            throw new NullPointerException("texImage2D can't be used with a null Bitmap");
        }
        if (native_texImage2D(target, level, internalformat, bitmap, type, border)!=0) {
            throw new IllegalArgumentException("invalid Bitmap format");
        }
    }
    public static void texImage2D(int target, int level, Bitmap bitmap,
            int border) {
        if (bitmap == null) {
            throw new NullPointerException("texImage2D can't be used with a null Bitmap");
        }
        if (native_texImage2D(target, level, -1, bitmap, -1, border)!=0) {
            throw new IllegalArgumentException("invalid Bitmap format");
        }
    }
    public static void texSubImage2D(int target, int level, int xoffset, int yoffset,
            Bitmap bitmap) {
        if (bitmap == null) {
            throw new NullPointerException("texSubImage2D can't be used with a null Bitmap");
        }
        int type = getType(bitmap);
        if (native_texSubImage2D(target, level, xoffset, yoffset, bitmap, -1, type)!=0) {
            throw new IllegalArgumentException("invalid Bitmap format");
        }
    }
    public static void texSubImage2D(int target, int level, int xoffset, int yoffset,
            Bitmap bitmap, int format, int type) {
        if (bitmap == null) {
            throw new NullPointerException("texSubImage2D can't be used with a null Bitmap");
        }
        if (native_texSubImage2D(target, level, xoffset, yoffset, bitmap, format, type)!=0) {
            throw new IllegalArgumentException("invalid Bitmap format");
        }
    }
    native private static void nativeClassInit();
    native private static int native_getInternalFormat(Bitmap bitmap);
    native private static int native_getType(Bitmap bitmap);
    native private static int native_texImage2D(int target, int level, int internalformat,
            Bitmap bitmap, int type, int border);
    native private static int native_texSubImage2D(int target, int level, int xoffset, int yoffset,
            Bitmap bitmap, int format, int type);
}
