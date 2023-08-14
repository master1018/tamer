public class ReferenceChain {
    JavaHeapObject      obj;    
    ReferenceChain      next;   
    public ReferenceChain(JavaHeapObject obj, ReferenceChain next) {
        this.obj = obj;
        this.next = next;
    }
    public JavaHeapObject getObj() {
        return obj;
    }
    public ReferenceChain getNext() {
        return next;
    }
    public int getDepth() {
        int count = 1;
        ReferenceChain tmp = next;
        while (tmp != null) {
            count++;
            tmp = tmp.next;
        }
        return count;
    }
}
