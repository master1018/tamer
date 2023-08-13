public final class Test6852574 extends AbstractTest {
    public static void main(String[] args) {
        new Test6852574().test(true);
    }
    protected Object getObject() {
        return Data.FIRST;
    }
    protected Object getAnotherObject() {
        return Data.SECOND;
    }
    public enum Data {
        FIRST {
            @Override
            public String toString() {
                return "1";
            }
        },
        SECOND {
            @Override
            public String toString() {
                return "2";
            }
        }
    }
}
