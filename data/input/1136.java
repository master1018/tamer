public class Test6859338 {
    static Object[] o = new Object[] { new Object(), null };
    public static void main(String[] args) {
        int total = 0;
        try {
            for (int i = 0; i < 40000; i++) {
                int limit = o.length;
                if (i < 20000) limit = 1;
                for (int j = 0; j < limit; j++) {
                    total += o[j].hashCode();
                }
            }
        } catch (NullPointerException e) {
        }
    }
}
