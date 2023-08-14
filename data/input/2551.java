public final class Test6531597 extends AbstractTest {
    public static void main(String[] args) {
        new Test6531597().test(true);
    }
    protected Object getObject() {
        return new Object[] {
                new byte[] {0, 1, 2},
                new short[] {0, 1, 2, 3, 4},
                new int[] {0, 1, 2, 3, 4, 5, 6},
                new long[] {0, 1, 2, 3, 4, 5, 6, 7, 8},
                new float[] {0.0f, 1.1f, 2.2f},
                new double[] {0.0, 1.1, 2.2, 3.3, 4.4},
                new char[] {'a', 'b', 'c'},
                new boolean[] {true, false},
        };
    }
}
