public class Lists {
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }
}
