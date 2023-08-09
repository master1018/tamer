public class ComodifiedRemove {
    public static
    void main(String[] args) {
        List list = new LinkedList();
        Object o1 = new Integer(1);
        list.add(o1);
        ListIterator e = list.listIterator();
        e.next();
        Object o2 = new Integer (2);
        list.add(o2);
        try{
            e.remove();
        } catch (ConcurrentModificationException cme) {
            return;
        }
        throw new RuntimeException(
            "LinkedList ListIterator.remove() comodification check failed.");
    }
}
