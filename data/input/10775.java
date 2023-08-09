public class HeadTail {
    private int status;
    private Name head;
    private Name tail;
    public HeadTail(Name head, Name tail) {
        this(head, tail, 0);
    }
    public HeadTail(Name head, Name tail, int status) {
        this.status = status;
        this.head = head;
        this.tail = tail;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public Name getHead() {
        return this.head;
    }
    public Name getTail() {
        return this.tail;
    }
    public int getStatus() {
        return this.status;
    }
}
