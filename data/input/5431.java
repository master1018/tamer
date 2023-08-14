public class KeySetRemove {
    public static void main(String args[]) throws Exception {
        Map m[] = {new HashMap(), new TreeMap()};
        for (int i=0; i<m.length; i++) {
            m[i].put("bananas", null);
            if (!m[i].keySet().remove("bananas"))
                throw new Exception("Yes, we have no bananas: "+i);
        }
    }
}
