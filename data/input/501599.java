public class DefaultFileComparator implements Comparator, Serializable {
    public static final Comparator DEFAULT_COMPARATOR = new DefaultFileComparator();
    public static final Comparator DEFAULT_REVERSE = new ReverseComparator(DEFAULT_COMPARATOR);
    public int compare(Object obj1, Object obj2) {
        File file1 = (File)obj1;
        File file2 = (File)obj2;
        return file1.compareTo(file2);
    }
}
