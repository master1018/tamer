public class Iteration {
    public static void main(String[] args) {
        String s = "iatrogenic";
        Map m = new WeakHashMap();
        m.put(s, "cucumber");
        Iterator i = m.keySet().iterator();
        if (i.hasNext() != i.hasNext())
            throw new RuntimeException("hasNext advances iterator");
    }
}
