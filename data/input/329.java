public class ListManager {
    public static void setPreferenceStyle(JList list) {
    }
    public static int getIndex(JList list, Point point) {
        int index = list.getUI().locationToIndex(list, point);
        return index;
    }
}
