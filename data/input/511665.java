public abstract class CursorFactory {
    protected NativeCursor[] systemCursors = {
            null, null, null, null,
            null, null, null, null,
            null, null, null, null,
            null, null,
    };
    public abstract NativeCursor createCursor(int type);
    public NativeCursor getCursor(int type) {
        if (type >= 0 && type < systemCursors.length) {
            NativeCursor cursor = systemCursors[type];
            if (cursor == null) {
                cursor = createCursor(type);
                systemCursors[type] = cursor;
            }
            return cursor;
        }
        return null;
    }
    public abstract NativeCursor createCustomCursor(Image img, int xHotSpot, int yHotSpot);
    public abstract Dimension getBestCursorSize(int prefWidth, int prefHeight);
    public abstract int getMaximumCursorColors();
}
