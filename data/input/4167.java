public class ThreadGroupIterator implements Iterator<ThreadGroupReference> {
    private final Stack<Iterator<ThreadGroupReference>> stack
                        = new Stack<Iterator<ThreadGroupReference>>();
    public ThreadGroupIterator(List<ThreadGroupReference> tgl) {
        push(tgl);
    }
    public ThreadGroupIterator(ThreadGroupReference tg) {
        List<ThreadGroupReference> tgl = new ArrayList<ThreadGroupReference>();
        tgl.add(tg);
        push(tgl);
    }
    private Iterator<ThreadGroupReference> top() {
        return stack.peek();
    }
    private void push(List<ThreadGroupReference> tgl) {
        stack.push(tgl.iterator());
        while (!stack.isEmpty() && !top().hasNext()) {
            stack.pop();
        }
    }
    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }
    @Override
    public ThreadGroupReference next() {
        return nextThreadGroup();
    }
    public ThreadGroupReference nextThreadGroup() {
        ThreadGroupReference tg = top().next();
        push(tg.threadGroups());
        return tg;
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
