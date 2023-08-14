public final class Collapser {
    private Collapser() {}
    public interface Collapsible<T> {
        public boolean collapseWith(T t);
        public boolean shouldCollapseWith(T t);
    }
    public static <T extends Collapsible<T>> void collapseList(ArrayList<T> list) {
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            T iItem = list.get(i);
            if (iItem != null) {
                for (int j = i + 1; j < listSize; j++) {
                    T jItem = list.get(j);
                    if (jItem != null) {
                        if (iItem.shouldCollapseWith(jItem)) {
                            iItem.collapseWith(jItem);
                            list.set(j, null);
                        }
                    }
                }
            }
        }
        Iterator<T> itr = list.iterator();
        while (itr.hasNext()) {
            if (itr.next() == null) {
                itr.remove();
            }
        }
    }
}
