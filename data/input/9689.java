public class HasNextAfterException {
    public static void main(String[] args) {
        List list = new ArrayList();
        ListIterator i = list.listIterator();
        try {
        i.previous();
        }
        catch (NoSuchElementException e) {
        }
        if (i.hasNext()) {
            throw new RuntimeException(
               "ListIterator.hasNext() returns true for an empty "
                + "List after ListIterator.previous().");
        }
    }
}
