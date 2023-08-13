public class GrowableIntArray {
    int[] array;
    int size;
    int cellSize;
    public GrowableIntArray(int cellSize, int initialSize) {
        array = new int[initialSize];
        size = 0;
        this.cellSize = cellSize;
    }
    private int getNextCellIndex() {
        int oldSize = size;
        size += cellSize;
        if (size >= array.length) {
            growArray();
        }
        return oldSize;
    }
    public int[] getArray() {
        return array;
    }
    public int[] getSizedArray() {
        return Arrays.copyOf(array, getSize());
    }
    public final int getNextIndex() {
        return getNextCellIndex() / cellSize;
    }
    protected final int getCellIndex(int cellIndex) {
        return cellSize * cellIndex;
    }
    public final int getInt(int cellIndex) {
        return array[cellIndex];
    }
    public final void addInt(int i) {
        int nextIndex = getNextIndex();
        array[nextIndex] = i;
    }
    public final int getSize() {
        return size / cellSize;
    }
    public void clear() {
        size = 0;
    }
    protected void growArray() {
        int newSize = Math.max(array.length * 2, 10);
        int[] oldArray = array;
        array = new int[newSize];
        System.arraycopy(oldArray, 0, array, 0, oldArray.length);
    }
}
