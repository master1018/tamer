public class TestEnumSubclassNull {
    public static void main(String[] args) {
        new TestEditor(Operation.class).testJava(null);
    }
    public enum Operation {
        PLUS {
            public int run(int i, int j) {
                return i + j;
            }
        },
        MINUS {
            public int run(int i, int j) {
                return i - j;
            }
        };
        public abstract int run(int i, int j);
    }
}
