public class ImageHelper {
    public static Image loadImage(IImageLoader loader, Display display,
            String fileName, int width, int height, Color phColor) {
        Image img = null;
        if (loader != null) {
            img = loader.loadImage(fileName, display);
        }
        if (img == null) {
            Log.w("ddms", "Couldn't load " + fileName);
            if (width != -1 && height != -1) {
                return createPlaceHolderArt(display, width, height,
                        phColor != null ? phColor : display
                                .getSystemColor(SWT.COLOR_BLUE));
            }
            return null;
        }
        return img;
    }
    public static Image createPlaceHolderArt(Display display, int width,
            int height, Color color) {
        Image img = new Image(display, width, height);
        GC gc = new GC(img);
        gc.setForeground(color);
        gc.drawLine(0, 0, width, height);
        gc.drawLine(0, height - 1, width, -1);
        gc.dispose();
        return img;
    }
}
