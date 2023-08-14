public class AppletResourceLoader {
    public static Image getImage(URL url) {
        return AppletViewer.getCachedImage(url);
    }
    public static Ref getImageRef(URL url) {
        return AppletViewer.getCachedImageRef(url);
    }
    public static void flushImages() {
        AppletViewer.flushImageCache();
    }
}
