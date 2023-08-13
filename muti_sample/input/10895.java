public class BufferQueue
{
    private LinkedList list = new LinkedList();
    public void enqueue(ByteBufferWithInfo item)
    {
        list.addLast(item);
    }
    public ByteBufferWithInfo dequeue() throws NoSuchElementException
    {
        return (ByteBufferWithInfo)list.removeFirst();
    }
    public int size()
    {
        return list.size();
    }
    public void push(ByteBufferWithInfo item)
    {
        list.addFirst(item);
    }
}
