public class SubMapClear {
    public static void main(String[] args) {
        SortedSet treeSet = new TreeSet();
        for (int i = 1; i <=10; i++)
            treeSet.add(new Integer(i));
        Set subSet = treeSet.subSet(new Integer(4),new Integer(10));
        subSet.clear();  
        int a[] = new int[] {1, 2, 3, 10};
        Set s = new TreeSet();
        for (int i = 0; i < a.length; i++)
            s.add(new Integer(a[i]));
        if (!treeSet.equals(s))
            throw new RuntimeException(treeSet.toString());
    }
}
