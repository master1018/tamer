public class FailFastIterator {
    public static void main(String[] args) throws Exception {
        List orig = new ArrayList(100);
        for (int i=0; i<100; i++)
            orig.add(new Integer(i));
        List copy = new ArrayList(orig);
        try {
            ListIterator i = copy.listIterator();
            i.next();
            copy.remove(99);
            copy.add(new Integer(99));
            i.remove();
            throw new Exception("remove: iterator didn't fail fast");
        } catch(ConcurrentModificationException e) {
        }
        if (!copy.equals(orig))
            throw new Exception("remove: iterator didn't fail fast enough");
        try {
            ListIterator i = copy.listIterator();
            i.next();
            copy.remove(99);
            copy.add(new Integer(99));
            i.set(new Integer(666));
            throw new Exception("set: iterator didn't fail fast");
        } catch(ConcurrentModificationException e) {
        }
        if (!copy.equals(orig))
            throw new Exception("set: iterator didn't fail fast enough");
        try {
            ListIterator i = copy.listIterator();
            copy.remove(99);
            copy.add(new Integer(99));
            i.add(new Integer(666));
            throw new Exception("add: iterator didn't fail fast");
        } catch(ConcurrentModificationException e) {
        }
        if (!copy.equals(orig))
            throw new Exception("add: iterator didn't fail fast enough");
    }
}
