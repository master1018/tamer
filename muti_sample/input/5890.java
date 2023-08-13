public class AddAll {
    public static void main(String[] args) throws Exception {
        List t = new FooList();
        t.add("b"); t.add("a"); t.add("r");
        t.addAll(0, Arrays.asList(new String[] {"f","o","o"}));
        if (!t.equals(Arrays.asList(new String[] {"f","o","o","b","a","r"})))
            throw new Exception("addAll is broken");
    }
}
class FooList extends AbstractSequentialList {
    List a = new ArrayList();
    public int size() {
        return a.size();
    }
    public ListIterator listIterator(int index) {
        return a.listIterator(index);
    }
}
