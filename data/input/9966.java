public class HeadTailTypeError {
    public static void main(String argv[]) throws Exception {
        try{
            SortedMap m = new TreeMap();
            m.headMap(new Object());
            throw new Exception("headMap, natural ordering");
        } catch (ClassCastException e) {
        }
        try{
            SortedMap m = new TreeMap();
            m.tailMap(new Object());
            throw new Exception("tailMap, natural ordering");
        } catch (ClassCastException e) {
        }
        try{
            SortedMap m = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            m.headMap(new Integer(0));
            throw new Exception("headMap, explicit comparator");
        } catch (ClassCastException e) {
        }
        try{
            SortedMap m = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            m.tailMap(new Integer(0));
            throw new Exception("tailMap, explicit comparator");
        } catch (ClassCastException e) {
        }
        try{
            SortedSet m = new TreeSet();
            m.headSet(new Object());
            throw new Exception("headSet, natural ordering");
        } catch (ClassCastException e) {
        }
        try{
            SortedSet m = new TreeSet();
            m.tailSet(new Object());
            throw new Exception("tailSet, natural ordering");
        } catch (ClassCastException e) {
        }
        try{
            SortedSet m = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            m.headSet(new Integer(0));
            throw new Exception("headSet, explicit comparator");
        } catch (ClassCastException e) {
        }
        try{
            SortedSet m = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            m.tailSet(new Integer(0));
            throw new Exception("tailSet, explicit comparator");
        } catch (ClassCastException e) {
        }
        try{
            SortedMap m = new TreeMap();
            m.headMap(null);
            throw new Exception("(null endpoint)headMap, natural ordering");
        } catch (NullPointerException e) {
        }
        try{
            SortedMap m = new TreeMap();
            m.tailMap(null);
            throw new Exception("(null endpoint)tailMap, natural ordering");
        } catch (NullPointerException e) {
        }
        try{
            SortedMap m = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            m.headMap(null);
            throw new Exception("(null endpoint)headMap, explicit comparator");
        } catch (NullPointerException e) {
        }
        try{
            SortedMap m = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            m.tailMap(null);
            throw new Exception("(null endpoint)tailMap, explicit comparator");
        } catch (NullPointerException e) {
        }
        try{
            SortedSet m = new TreeSet();
            m.headSet(null);
            throw new Exception("(null endpoint)headSet, natural ordering");
        } catch (NullPointerException e) {
        }
        try{
            SortedSet m = new TreeSet();
            m.tailSet(null);
            throw new Exception("(null endpoint)tailSet, natural ordering");
        } catch (NullPointerException e) {
        }
        try{
            SortedSet m = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            m.headSet(null);
            throw new Exception("(null endpoint)headSet, explicit comparator");
        } catch (NullPointerException e) {
        }
        try{
            SortedSet m = new TreeSet(String.CASE_INSENSITIVE_ORDER);
            m.tailSet(null);
            throw new Exception("(null endpoint)tailSet, explicit comparator");
        } catch (NullPointerException e) {
        }
        SortedMap m = new TreeMap();
        m.headMap(new Integer(0));
        m.tailMap(new Integer(0));
        m = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        m.headMap("llama");
        m.tailMap("llama");
        SortedSet s = new TreeSet();
        s.headSet(new Integer(0));
        s.tailSet(new Integer(0));
        s = new TreeSet(String.CASE_INSENSITIVE_ORDER);
        s.headSet("drama");
        s.tailSet("drama");
    }
}
