public class ComodifiedRemoveAllElements {
    public static void main(String[] args) {
        Vector v = new Vector();
        v.addElement(null);
        Iterator it = v.iterator();
        v.removeAllElements();
        try{
            it.next();
        } catch (ConcurrentModificationException cme) {
            return;
        }
        throw new RuntimeException(
                  "Vector.RemoveAllElements() modCount increment failed.");
    }
}
