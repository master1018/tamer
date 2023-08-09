public class NullComparator {
    public static void main(String[] args) throws Exception {
        List list = new ArrayList(100);
        for (int i=0; i<100; i++)
            list.add(new Integer(i));
        List sorted = new ArrayList(list);
        Collections.shuffle(list);
        Object a[] = list.toArray();
        Arrays.sort(a, null);
        if (!Arrays.asList(a).equals(sorted))
            throw new Exception("Arrays.sort");
        a = list.toArray();
        Arrays.sort(a, 0, 100, null);
        if (!Arrays.asList(a).equals(sorted))
            throw new Exception("Arrays.sort(from, to)");
        if (Arrays.binarySearch(a, new Integer(69)) != 69)
            throw new Exception("Arrays.binarySearch");
        List tmp = new ArrayList(list);
        Collections.sort(tmp, null);
        if (!tmp.equals(sorted))
            throw new Exception("Collections.sort");
        if (Collections.binarySearch(tmp, new Integer(69)) != 69)
            throw new Exception("Collections.binarySearch");
        if (!Collections.min(list, null).equals(new Integer(0)))
            throw new Exception("Collections.min");
        if (!Collections.max(list, null).equals(new Integer(99)))
            throw new Exception("Collections.max");
    }
}
