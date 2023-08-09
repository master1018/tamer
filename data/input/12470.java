public class Remove {
    public static
    void main(String[] args) {
        LinkedList list = new LinkedList();
        ListIterator e = list.listIterator();
        Object o = new Integer(1);
        e.add(o);
        e.previous();
        e.next();
        e.remove();
        e.add(o);
        if (!o.equals(list.get(0)))
            throw new RuntimeException("LinkedList ListIterator remove failed.");
    }
}
