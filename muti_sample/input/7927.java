public class SubMap {
    public static void main(String args[]) throws Exception {
        SortedMap m = new TreeMap();
        m.put(new Integer(1), new Integer(1));
        m.put(new Integer(2), new Integer(2));
        m.put(new Integer(3), new Integer(3));
        SortedMap m2 = m.subMap(new Integer(2), new Integer(2));
        boolean exc = false;
        try {
            m2.firstKey();
        } catch(NoSuchElementException e) {
            exc = true;
        }
        if (!exc)
            throw new Exception("first key");
        exc = false;
        try {
            m2.lastKey();
        } catch(NoSuchElementException e) {
            exc = true;
        }
        if (!exc)
            throw new Exception("last key");
        SortedMap m3 = m.subMap(new Integer(2), new Integer(3));
        if (!m3.firstKey().equals(new Integer(2)))
            throw new Exception("first key wrong");
        if (!m3.lastKey().equals(new Integer(2)))
            throw new Exception("last key wrong");
        SortedSet s = new TreeSet();
        s.add(new Integer(1));
        s.add(new Integer(2));
        s.add(new Integer(3));
        SortedSet s2 = s.subSet(new Integer(2), new Integer(2));
        exc = false;
        try {
            s2.first();
        } catch(NoSuchElementException e) {
            exc = true;
        }
        if (!exc)
            throw new Exception("first element");
        exc = false;
        try {
            s2.last();
        } catch(NoSuchElementException e) {
            exc = true;
        }
        if (!exc)
            throw new Exception("last element");
        SortedSet s3 = s.subSet(new Integer(2), new Integer(3));
        if (!s3.first().equals(new Integer(2)))
            throw new Exception("first element wrong");
        if (!s3.last().equals(new Integer(2)))
            throw new Exception("last element wrong");
    }
}
