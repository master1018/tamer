public class StaticFieldTest {
    private static byte   byteField;
    private static short  shortField;
    private static char   charField;
    private static int    intField;
    private static long   longField;
    private static float  floatField;
    private static double doubleField;
    private static String stringField;
    private static Field getAccessibleField(String name) throws NoSuchFieldException {
        Field f = StaticFieldTest.class.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }
    public static void main(String[] args) throws Exception {
        Field byteField   = getAccessibleField("byteField");
        Field shortField  = getAccessibleField("shortField");
        Field charField   = getAccessibleField("charField");
        Field intField    = getAccessibleField("intField");
        Field longField   = getAccessibleField("longField");
        Field floatField  = getAccessibleField("floatField");
        Field doubleField = getAccessibleField("doubleField");
        Field stringField = getAccessibleField("stringField");
        byteField.setByte    (null, (byte) 77);
        shortField.setShort  (null, (short) 77);
        charField.setChar    (null, (char) 77);
        intField.setInt      (null, (int) 77);
        longField.setLong    (null, (long) 77);
        floatField.setFloat  (null, (float) 77);
        doubleField.setDouble(null, (double) 77);
        String myString = "Hello, world";
        stringField.set      (null, myString);
        if (byteField.getByte(null)     != 77) throw new RuntimeException("Test failed");
        if (shortField.getShort(null)   != 77) throw new RuntimeException("Test failed");
        if (charField.getChar(null)     != 77) throw new RuntimeException("Test failed");
        if (intField.getInt(null)       != 77) throw new RuntimeException("Test failed");
        if (longField.getLong(null)     != 77) throw new RuntimeException("Test failed");
        if (floatField.getFloat(null)   != 77) throw new RuntimeException("Test failed");
        if (doubleField.getDouble(null) != 77) throw new RuntimeException("Test failed");
        if (stringField.get(null)       != myString) throw new RuntimeException("Test failed");
    }
}
