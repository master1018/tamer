public class ZeroInitCap {
    public static void main(String[] argv) {
        Map map = new WeakHashMap(0);
        map.put("a","b");
    }
}
