public class GrowablePointArray extends GrowableIntArray
{
        private static final int POINT_SIZE = 2;
        public GrowablePointArray(int initialSize)
        {
                super(POINT_SIZE, initialSize);
        }
        public final int getX(int index)
        {
                return array[getCellIndex(index)];
        }
        public final int getY(int index)
        {
                return array[getCellIndex(index) + 1];
        }
        public final void setX(int index, int x)
        {
                array[getCellIndex(index)] = x;
        }
        public final void setY(int index, int y)
        {
                array[getCellIndex(index) + 1] = y;
        }
}
