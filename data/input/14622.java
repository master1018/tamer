public final class AWTUtilities {
    private AWTUtilities() {
    }
    public static enum Translucency {
        PERPIXEL_TRANSPARENT,
        TRANSLUCENT,
        PERPIXEL_TRANSLUCENT;
    }
    public static boolean isTranslucencySupported(Translucency translucencyKind) {
        switch (translucencyKind) {
            case PERPIXEL_TRANSPARENT:
                return isWindowShapingSupported();
            case TRANSLUCENT:
                return isWindowOpacitySupported();
            case PERPIXEL_TRANSLUCENT:
                return isWindowTranslucencySupported();
        }
        return false;
    }
    private static boolean isWindowOpacitySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowOpacitySupported();
    }
    public static void setWindowOpacity(Window window, float opacity) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        AWTAccessor.getWindowAccessor().setOpacity(window, opacity);
    }
    public static float getWindowOpacity(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        return AWTAccessor.getWindowAccessor().getOpacity(window);
    }
    public static boolean isWindowShapingSupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowShapingSupported();
    }
    public static Shape getWindowShape(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        return AWTAccessor.getWindowAccessor().getShape(window);
    }
    public static void setWindowShape(Window window, Shape shape) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        AWTAccessor.getWindowAccessor().setShape(window, shape);
    }
    private static boolean isWindowTranslucencySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        if (!((SunToolkit)curToolkit).isWindowTranslucencySupported()) {
            return false;
        }
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (isTranslucencyCapable(env.getDefaultScreenDevice()
                    .getDefaultConfiguration()))
        {
            return true;
        }
        GraphicsDevice[] devices = env.getScreenDevices();
        for (int i = 0; i < devices.length; i++) {
            GraphicsConfiguration[] configs = devices[i].getConfigurations();
            for (int j = 0; j < configs.length; j++) {
                if (isTranslucencyCapable(configs[j])) {
                    return true;
                }
            }
        }
        return false;
    }
    public static void setWindowOpaque(Window window, boolean isOpaque) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        if (!isOpaque && !isTranslucencySupported(Translucency.PERPIXEL_TRANSLUCENT)) {
            throw new UnsupportedOperationException(
                    "The PERPIXEL_TRANSLUCENT translucency kind is not supported");
        }
        AWTAccessor.getWindowAccessor().setOpaque(window, isOpaque);
    }
    public static boolean isWindowOpaque(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        return window.isOpaque();
    }
    public static boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        if (gc == null) {
            throw new NullPointerException("The gc argument should not be null");
        }
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isTranslucencyCapable(gc);
    }
    public static void setComponentMixingCutoutShape(Component component,
            Shape shape)
    {
        if (component == null) {
            throw new NullPointerException(
                    "The component argument should not be null.");
        }
        AWTAccessor.getComponentAccessor().setMixingCutoutShape(component,
                shape);
    }
}
