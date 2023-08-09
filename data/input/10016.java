public class EnsureCapacity {
    public static void main(String[] args) {
        testStringBuilder();
        testStringBuffer();
    }
    private static void checkCapacity(int before, int after) {
        if (before != after) {
            throw new RuntimeException("capacity is expected to be unchanged: " +
                "before=" + before + " after=" + after);
        }
    }
    private static void testStringBuilder() {
        StringBuilder sb = new StringBuilder("abc");
        int cap = sb.capacity();
        sb.ensureCapacity(Integer.MIN_VALUE);
        checkCapacity(cap, sb.capacity());
        try {
            char[] str = {'a', 'b', 'c', 'd'};
            sb.append(str, 0, Integer.MIN_VALUE + 10);
            throw new RuntimeException("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException ex) {
        }
    }
    private static void testStringBuffer() {
        StringBuffer sb = new StringBuffer("abc");
        int cap = sb.capacity();
        sb.ensureCapacity(Integer.MIN_VALUE);
        checkCapacity(cap, sb.capacity());
        try {
            char[] str = {'a', 'b', 'c', 'd'};
            sb.append(str, 0, Integer.MIN_VALUE + 10);
            throw new RuntimeException("IndexOutOfBoundsException not thrown");
        } catch (IndexOutOfBoundsException ex) {
        }
    }
}
