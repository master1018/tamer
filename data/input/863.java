public abstract class CustomCursor extends Cursor {
    protected Image image;
    public CustomCursor(Image cursor, Point hotSpot, String name)
            throws IndexOutOfBoundsException {
        super(name);
        image = cursor;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Component c = new Canvas(); 
        MediaTracker tracker = new MediaTracker(c);
        tracker.addImage(cursor, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
        }
        int width = cursor.getWidth(c);
        int height = cursor.getHeight(c);
        if (tracker.isErrorAny() || width < 0 || height < 0) {
              hotSpot.x = hotSpot.y = 0;
        }
        Dimension nativeSize = toolkit.getBestCursorSize(width, height);
        if (nativeSize.width != width || nativeSize.height != height) {
            cursor = cursor.getScaledInstance(nativeSize.width,
                                              nativeSize.height,
                                              Image.SCALE_DEFAULT);
            width = nativeSize.width;
            height = nativeSize.height;
        }
        if (hotSpot.x >= width || hotSpot.y >= height || hotSpot.x < 0 || hotSpot.y < 0) {
            throw new IndexOutOfBoundsException("invalid hotSpot");
        }
        int[] pixels = new int[width * height];
        ImageProducer ip = cursor.getSource();
        PixelGrabber pg = new PixelGrabber(ip, 0, 0, width, height,
                                           pixels, 0, width);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
        createNativeCursor(image, pixels, width, height, hotSpot.x, hotSpot.y);
    }
    protected abstract void createNativeCursor(Image im,  int[] pixels,
                                               int width, int height,
                                               int xHotSpot, int yHotSpot);
}
