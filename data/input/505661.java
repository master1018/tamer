public class NameFileComparator implements Comparator, Serializable {
    public static final Comparator NAME_COMPARATOR = new NameFileComparator();
    public static final Comparator NAME_REVERSE = new ReverseComparator(NAME_COMPARATOR);
    public static final Comparator NAME_INSENSITIVE_COMPARATOR = new NameFileComparator(IOCase.INSENSITIVE);
    public static final Comparator NAME_INSENSITIVE_REVERSE = new ReverseComparator(NAME_INSENSITIVE_COMPARATOR);
    public static final Comparator NAME_SYSTEM_COMPARATOR = new NameFileComparator(IOCase.SYSTEM);
    public static final Comparator NAME_SYSTEM_REVERSE = new ReverseComparator(NAME_SYSTEM_COMPARATOR);
    private final IOCase caseSensitivity;
    public NameFileComparator() {
        this.caseSensitivity = IOCase.SENSITIVE;
    }
    public NameFileComparator(IOCase caseSensitivity) {
        this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
    }
    public int compare(Object obj1, Object obj2) {
        File file1 = (File)obj1;
        File file2 = (File)obj2;
        return caseSensitivity.checkCompareTo(file1.getName(), file2.getName());
    }
}
