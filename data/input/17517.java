public class GrowableByteArray
{
        byte[] array;
        int size;
        int cellSize;
        public GrowableByteArray(int cellSize, int initialSize)
        {
                array = new byte[initialSize];
                size = 0;
                this.cellSize = cellSize;
        }
        private int getNextCellIndex()
        {
                int oldSize = size;
                size += cellSize;
                if (size >= array.length)
                {
                        growArray();
                }
                return oldSize;
        }
        public byte[] getArray()
        {
                return array;
        }
        public byte[] getSizedArray()
        {
                return Arrays.copyOf(array, getSize());
        }
        public final int getByte(int index)
        {
                return array[getCellIndex(index)];
        }
        public final int getNextIndex()
        {
                return getNextCellIndex() / cellSize;
        }
        protected final int getCellIndex(int cellIndex)
        {
                return cellSize * cellIndex;
        }
        public final void addByte(byte i)
        {
            int nextIndex = getNextIndex();
            array[nextIndex] = i;
        }
        public final int getSize()
        {
                return size / cellSize;
        }
        public void clear()
        {
                size = 0;
        }
        protected void growArray()
        {
                int newSize = Math.max(array.length * 2, 10);
                byte[] oldArray = array;
                array = new byte[newSize];
                System.arraycopy(oldArray, 0, array, 0, oldArray.length);
        }
}
