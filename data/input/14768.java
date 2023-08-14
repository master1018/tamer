public class ByteBuffer {
    protected byte elementData[];
    protected int elementCount;
    protected int capacityIncrement;
    public ByteBuffer(int initialCapacity, int capacityIncrement) {
        super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        this.elementData = new byte[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }
    public ByteBuffer(int initialCapacity) {
        this(initialCapacity, 0);
    }
    public ByteBuffer() {
        this(200);
    }
    public void trimToSize() {
        int oldCapacity = elementData.length;
        if (elementCount < oldCapacity) {
            byte oldData[] = elementData;
            elementData = new byte[elementCount];
            System.arraycopy(oldData, 0, elementData, 0, elementCount);
        }
    }
    private void ensureCapacityHelper(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            byte oldData[] = elementData;
            int newCapacity = (capacityIncrement > 0) ?
                (oldCapacity + capacityIncrement) : (oldCapacity * 2);
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = new byte[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, elementCount);
        }
    }
    public int capacity() {
        return elementData.length;
    }
    public int size() {
        return elementCount;
    }
    public boolean isEmpty() {
        return elementCount == 0;
    }
    public void append(byte value)
    {
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = value;
    }
    public void append( int value )
    {
        ensureCapacityHelper(elementCount + 4);
        doAppend( value ) ;
    }
    private void doAppend( int value )
    {
        int current = value ;
        for (int ctr=0; ctr<4; ctr++) {
            elementData[elementCount+ctr] = (byte)(current & 255) ;
            current = current >> 8 ;
        }
        elementCount += 4 ;
    }
    public void append( String value )
    {
        byte[] data = value.getBytes() ;
        ensureCapacityHelper( elementCount + data.length + 4 ) ;
        doAppend( data.length ) ;
        System.arraycopy( data, 0, elementData, elementCount, data.length ) ;
        elementCount += data.length ;
    }
    public byte[] toArray() {
        return elementData ;
    }
}
