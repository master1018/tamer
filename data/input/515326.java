public class IconFactory {
    public static final int COLOR_RED     = SWT.COLOR_DARK_RED;
    public static final int COLOR_GREEN   = SWT.COLOR_DARK_GREEN;
    public static final int COLOR_BLUE    = SWT.COLOR_DARK_BLUE;
    public static final int COLOR_DEFAULT = SWT.COLOR_BLACK;
    public static final int SHAPE_CIRCLE  = 'C';
    public static final int SHAPE_RECT    = 'R';
    public static final int SHAPE_DEFAULT = SHAPE_CIRCLE;
    private static IconFactory sInstance;
    private HashMap<String, Image> mIconMap = new HashMap<String, Image>();
    private HashMap<String, ImageDescriptor> mImageDescMap = new HashMap<String, ImageDescriptor>();
    private IconFactory() {
    }
    public static synchronized IconFactory getInstance() {
        if (sInstance == null) {
            sInstance = new IconFactory();
        }
        return sInstance;
    }
    public void Dispose() {
        for (Image icon : mIconMap.values()) {
            if (icon != null) {
                icon.dispose();
            }
        }
        mIconMap.clear();
    }
    public Image getIcon(String osName) {
        return getIcon(osName, COLOR_DEFAULT, SHAPE_DEFAULT);
    }
    public Image getIcon(String osName, int color, int shape) {
        String key = Character.toString((char) shape) + Integer.toString(color) + osName;
        Image icon = mIconMap.get(key);
        if (icon == null && !mIconMap.containsKey(key)) {
            ImageDescriptor id = getImageDescriptor(osName, color, shape);
            if (id != null) {
                icon = id.createImage();
            }
            mIconMap.put(key, icon);
        }
        return icon;
    }
    public ImageDescriptor getImageDescriptor(String osName) {
        return getImageDescriptor(osName, COLOR_DEFAULT, SHAPE_DEFAULT);
    }
    public ImageDescriptor getImageDescriptor(String osName, int color, int shape) {
        String key = Character.toString((char) shape) + Integer.toString(color) + osName;
        ImageDescriptor id = mImageDescMap.get(key);
        if (id == null && !mImageDescMap.containsKey(key)) {
            id = AdtPlugin.imageDescriptorFromPlugin(
                    AdtPlugin.PLUGIN_ID,
                    String.format("/icons/%1$s.png", osName)); 
            if (id == null) {
                id = new LetterImageDescriptor(osName.charAt(0), color, shape);
            }
            mImageDescMap.put(key, id);
        }
        return id;
    }
    private static class LetterImageDescriptor extends ImageDescriptor {
        private final char mLetter;
        private final int mColor;
        private final int mShape;
        public LetterImageDescriptor(char letter, int color, int shape) {
            mLetter = letter;
            mColor = color;
            mShape = shape;
        }
        @Override
        public ImageData getImageData() {
            final int SX = 15;
            final int SY = 15;
            final int RX = 4;
            final int RY = 4;
            Display display = Display.getCurrent();
            if (display == null) {
                return null;
            }
            Image image = new Image(display, SX, SY);
            image.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            GC gc = new GC(image);
            gc.setAdvanced(true);
            gc.setAntialias(SWT.ON);
            gc.setTextAntialias(SWT.ON);
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            if (mShape == SHAPE_CIRCLE) {
                gc.fillOval(0, 0, SX - 1, SY - 1);
            } else if (mShape == SHAPE_RECT) {
                gc.fillRoundRectangle(0, 0, SX - 1, SY - 1, RX, RY);
            }
            gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
            gc.setLineWidth(1);
            if (mShape == SHAPE_CIRCLE) {
                gc.drawOval(0, 0, SX - 1, SY - 1);
            } else if (mShape == SHAPE_RECT) {
                gc.drawRoundRectangle(0, 0, SX - 1, SY - 1, RX, RY);
            }
            Font font = display.getSystemFont();
            FontData[] fds = font.getFontData();
            fds[0].setStyle(SWT.BOLD);
            fds[0].setHeight((int) ((SY + 1) * 3./4. * 72./display.getDPI().y));
            font = new Font(display, fds);
            gc.setFont(font);
            gc.setForeground(display.getSystemColor(mColor));
            int ofx = 0;
            int ofy = 0;
            if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
                ofx = +1;
                ofy = -1;
            }
            String s = Character.toString(mLetter).toUpperCase();
            Point p = gc.textExtent(s);
            int tx = (SX + ofx - p.x) / 2;
            int ty = (SY + ofy - p.y) / 2;
            gc.drawText(s, tx, ty, true );
            font.dispose();
            gc.dispose();
            ImageData data = image.getImageData();
            image.dispose();
            return data;
        }
    }
}
