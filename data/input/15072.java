public class BinarySearchNullComparator {
    public static void main (String args[]) throws Exception {
        List list = Arrays.asList(new String[] {"I", "Love", "You"});
        int result = Collections.binarySearch(list, "You", null);
        if (result != 2)
            throw new Exception("Result: " + result);
    }
}
