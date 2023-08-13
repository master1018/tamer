public class StackImpl {
    private Object[] data = new Object[3] ;
    private int top = -1 ;
    public final boolean empty() {
        return top == -1;
    }
    public final Object peek() {
        if (empty())
            throw new EmptyStackException();
        return data[ top ];
    }
    public final Object pop() {
        Object obj = peek() ;
        data[top] = null ;
        top-- ;
        return obj;
    }
    private void ensure()
    {
        if (top == (data.length-1)) {
            int newSize = 2*data.length ;
            Object[] newData = new Object[ newSize ] ;
            System.arraycopy( data, 0, newData, 0, data.length ) ;
            data = newData ;
        }
    }
    public final Object push( Object item ) {
        ensure() ;
        top++ ;
        data[top] = item;
        return item;
    }
}
