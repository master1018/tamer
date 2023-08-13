public class ContainsValue {
    static public void main (String[] args) {
        Map map = new TreeMap();
        if (map.containsValue ("gemutlichkeit"))
            throw new RuntimeException("containsValue optimistic (non-null)");
        if (map.containsValue (null))
            throw new RuntimeException("containsValue optimistic (null)");
        map.put("a", null);
        map.put("b", "gemutlichkeit");
        if (!map.containsValue ("gemutlichkeit"))
            throw new RuntimeException("containsValue pessimistic (non-null)");
        if (!map.containsValue (null))
            throw new RuntimeException("containsValue pessimistic (null)");
    }
}
