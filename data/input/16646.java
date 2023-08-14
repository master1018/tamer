public final class SecurityWarning {
    private SecurityWarning() {
    }
    public static Dimension getSize(Window window) {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        if (window.getWarningString() == null) {
            throw new IllegalArgumentException(
                    "The window must have a non-null warning string.");
        }
        return AWTAccessor.getWindowAccessor().getSecurityWarningSize(window);
    }
    public static void setPosition(Window window, Point2D point,
            float alignmentX, float alignmentY)
    {
        if (window == null) {
            throw new NullPointerException(
                    "The window argument should not be null.");
        }
        if (window.getWarningString() == null) {
            throw new IllegalArgumentException(
                    "The window must have a non-null warning string.");
        }
        if (point == null) {
            throw new NullPointerException(
                    "The point argument must not be null");
        }
        if (alignmentX < 0.0f || alignmentX > 1.0f) {
            throw new IllegalArgumentException(
                    "alignmentX must be in the range [0.0f ... 1.0f].");
        }
        if (alignmentY < 0.0f || alignmentY > 1.0f) {
            throw new IllegalArgumentException(
                    "alignmentY must be in the range [0.0f ... 1.0f].");
        }
        AWTAccessor.getWindowAccessor().setSecurityWarningPosition(window,
                point, alignmentX, alignmentY);
    }
}
