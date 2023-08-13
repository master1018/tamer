public final class TestArray extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <array class=\"java.lang.Number\">\n"
            + "  <byte>-111</byte>\n"
            + "  <long>1111</long>\n"
            + " </array>\n"
            + " <array length=\"3\">\n"
            + "  <void index=\"1\">\n"
            + "   <string>Hello, world!</string>\n"
            + "  </void>\n"
            + " </array>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestArray().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        Number[] numbers = getArray(Number.class, 2, decoder.readObject());
        if (!numbers[0].equals(Byte.valueOf("-111"))) { 
            throw new Error("unexpected byte value");
        }
        if (!numbers[1].equals(Long.valueOf("1111"))) { 
            throw new Error("unexpected long value");
        }
        Object[] objects = getArray(Object.class, 3, decoder.readObject());
        if (objects[0] != null) {
            throw new Error("unexpected first value");
        }
        if (!objects[1].equals("Hello, world!")) { 
            throw new Error("unexpected string value");
        }
        if (objects[2] != null) {
            throw new Error("unexpected last value");
        }
    }
    private static <T> T[] getArray(Class<T> component, int length, Object object) {
        Class type = object.getClass();
        if (!type.isArray()) {
            throw new Error("array expected");
        }
        if (!type.getComponentType().equals(component)) {
            throw new Error("unexpected component type");
        }
        if (length != Array.getLength(object)) {
            throw new Error("unexpected array length");
        }
        return (T[]) object;
    }
}
