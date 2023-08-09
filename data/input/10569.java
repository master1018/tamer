public class ThreadIterator implements Iterator<ThreadReference> {
    Iterator<ThreadReference> it = null;
    ThreadGroupIterator tgi;
    public ThreadIterator(ThreadGroupReference tg) {
        tgi = new ThreadGroupIterator(tg);
    }
    public ThreadIterator(List<ThreadGroupReference> tgl) {
        tgi = new ThreadGroupIterator(tgl);
    }
    @Override
    public boolean hasNext() {
        while (it == null || !it.hasNext()) {
            if (!tgi.hasNext()) {
                return false; 
            }
            it = tgi.nextThreadGroup().threads().iterator();
        }
        return true;
    }
    @Override
    public ThreadReference next() {
        return it.next();
    }
    public ThreadReference nextThread() {
        return next();
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
