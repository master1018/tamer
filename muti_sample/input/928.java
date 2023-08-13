public class CheckProvider {
    public static void main(String[] args) {
        Class<?> c = AsynchronousChannelProvider.provider().getClass();
        String expected = args[0];
        String actual = c.getName();
        if (!actual.equals(expected))
            throw new RuntimeException("Provider is of type '" + actual +
                "', expected '" + expected + "'");
    }
}
