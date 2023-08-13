public class ColorController {
    private static final int[] systemColors = { SWT.COLOR_BLUE, SWT.COLOR_RED,
        SWT.COLOR_GREEN, SWT.COLOR_CYAN, SWT.COLOR_MAGENTA, SWT.COLOR_DARK_BLUE,
        SWT.COLOR_DARK_RED, SWT.COLOR_DARK_GREEN, SWT.COLOR_DARK_YELLOW,
        SWT.COLOR_DARK_CYAN, SWT.COLOR_DARK_MAGENTA, SWT.COLOR_BLACK };
    private static RGB[] rgbColors = { new RGB(90, 90, 255), 
            new RGB(0, 240, 0), 
            new RGB(255, 0, 0), 
            new RGB(0, 255, 255), 
            new RGB(255, 80, 255), 
            new RGB(200, 200, 0), 
            new RGB(40, 0, 200), 
            new RGB(150, 255, 150), 
            new RGB(150, 0, 0), 
            new RGB(30, 150, 150), 
            new RGB(200, 200, 255), 
            new RGB(0, 120, 0), 
            new RGB(255, 150, 150), 
            new RGB(140, 80, 140), 
            new RGB(150, 100, 50), 
            new RGB(70, 70, 70), 
    };
    private static HashMap<Integer, Color> colorCache = new HashMap<Integer, Color>();
    private static HashMap<Integer, Image> imageCache = new HashMap<Integer, Image>();
    public ColorController() {
    }
    public static Color requestColor(Display display, RGB rgb) {
        return requestColor(display, rgb.red, rgb.green, rgb.blue);
    }
    public static Image requestColorSquare(Display display, RGB rgb) {
        return requestColorSquare(display, rgb.red, rgb.green, rgb.blue);
    }
    public static Color requestColor(Display display, int red, int green, int blue) {
        int key = (red << 16) | (green << 8) | blue;
        Color color = colorCache.get(key);
        if (color == null) {
            color = new Color(display, red, green, blue);
            colorCache.put(key, color);
        }
        return color;
    }
    public static Image requestColorSquare(Display display, int red, int green, int blue) {
        int key = (red << 16) | (green << 8) | blue;
        Image image = imageCache.get(key);
        if (image == null) {
            image = new Image(display, 8, 14);
            GC gc = new GC(image);
            Color color = requestColor(display, red, green, blue);
            gc.setBackground(color);
            gc.fillRectangle(image.getBounds());
            gc.dispose();
            imageCache.put(key, image);
        }
        return image;
    }
    public static void assignMethodColors(Display display, MethodData[] methods) {
        int nextColorIndex = 0;
        for (MethodData md : methods) {
            RGB rgb = rgbColors[nextColorIndex];
            if (++nextColorIndex == rgbColors.length)
                nextColorIndex = 0;
            Color color = requestColor(display, rgb);
            Image image = requestColorSquare(display, rgb);
            md.setColor(color);
            md.setImage(image);
            int fadedRed = 150 + rgb.red / 4;
            int fadedGreen = 150 + rgb.green / 4;
            int fadedBlue = 150 + rgb.blue / 4;
            RGB faded = new RGB(fadedRed, fadedGreen, fadedBlue);
            color = requestColor(display, faded);
            image = requestColorSquare(display, faded);
            md.setFadedColor(color);
            md.setFadedImage(image);
        }
    }
}
