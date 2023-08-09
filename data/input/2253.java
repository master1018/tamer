public class GrowableRectArray extends GrowableIntArray {
    private static final int RECT_SIZE = 4;
    public GrowableRectArray(int initialSize) {
        super(RECT_SIZE, initialSize);
    }
    public final void setX(int index, int x) {
        array[getCellIndex(index)] = x;
    }
    public final void setY(int index, int y) {
        array[getCellIndex(index) + 1] = y;
    }
    public final void setWidth(int index, int width) {
        array[getCellIndex(index) + 2] = width;
    }
    public final void setHeight(int index, int height) {
        array[getCellIndex(index) + 3] = height;
    }
    public final int getX(int index) {
        return array[getCellIndex(index)];
    }
    public final int getY(int index) {
        return array[getCellIndex(index) + 1];
    }
    public final int getWidth(int index) {
        return array[getCellIndex(index) + 2];
    }
    public final int getHeight(int index) {
        return array[getCellIndex(index) + 3];
    }
    public final void translateRects(int x, int y) {
        for (int i = 0; i < getSize(); i++) {
            setX(i, getX(i) + x);
            setY(i, getY(i) + y);
        }
    }
}
