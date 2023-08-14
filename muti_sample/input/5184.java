public class Tester {
        private String[] values;
        private int count;
        String foo() {
                int i = Integer.MAX_VALUE-1;
                String s;
                try {
                    s = values[i];
                } catch (Throwable e) {
                    s = "";
                }
                return s;
        }
        public static void main(String[] args) {
                Tester t = new Tester();
                String s = t.foo();
        }
}
