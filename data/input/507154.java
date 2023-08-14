@TestTargetClass(java.sql.Types.class)
public class TypesTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Field testing",
        method = "!Constants",
        args = {}
    )
    public void testPublicStatics() {
        HashMap<String, Integer> thePublicStatics = new HashMap<String, Integer>();
        thePublicStatics.put("BOOLEAN", new Integer(16));
        thePublicStatics.put("DATALINK", new Integer(70));
        thePublicStatics.put("REF", new Integer(2006));
        thePublicStatics.put("CLOB", new Integer(2005));
        thePublicStatics.put("BLOB", new Integer(2004));
        thePublicStatics.put("ARRAY", new Integer(2003));
        thePublicStatics.put("STRUCT", new Integer(2002));
        thePublicStatics.put("DISTINCT", new Integer(2001));
        thePublicStatics.put("JAVA_OBJECT", new Integer(2000));
        thePublicStatics.put("OTHER", new Integer(1111));
        thePublicStatics.put("NULL", new Integer(0));
        thePublicStatics.put("LONGVARBINARY", new Integer(-4));
        thePublicStatics.put("VARBINARY", new Integer(-3));
        thePublicStatics.put("BINARY", new Integer(-2));
        thePublicStatics.put("TIMESTAMP", new Integer(93));
        thePublicStatics.put("TIME", new Integer(92));
        thePublicStatics.put("DATE", new Integer(91));
        thePublicStatics.put("LONGVARCHAR", new Integer(-1));
        thePublicStatics.put("VARCHAR", new Integer(12));
        thePublicStatics.put("CHAR", new Integer(1));
        thePublicStatics.put("DECIMAL", new Integer(3));
        thePublicStatics.put("NUMERIC", new Integer(2));
        thePublicStatics.put("DOUBLE", new Integer(8));
        thePublicStatics.put("REAL", new Integer(7));
        thePublicStatics.put("FLOAT", new Integer(6));
        thePublicStatics.put("BIGINT", new Integer(-5));
        thePublicStatics.put("INTEGER", new Integer(4));
        thePublicStatics.put("SMALLINT", new Integer(5));
        thePublicStatics.put("TINYINT", new Integer(-6));
        thePublicStatics.put("BIT", new Integer(-7));
        Class<?> typesClass;
        try {
            typesClass = Class.forName("java.sql.Types");
        } catch (ClassNotFoundException e) {
            fail("java.sql.Types class not found!");
            return;
        } 
        Field[] theFields = typesClass.getDeclaredFields();
        int requiredModifier = Modifier.PUBLIC + Modifier.STATIC
                + Modifier.FINAL;
        int countPublicStatics = 0;
        for (Field element : theFields) {
            String fieldName = element.getName();
            int theMods = element.getModifiers();
            if (Modifier.isPublic(theMods) && Modifier.isStatic(theMods)) {
                try {
                    Object fieldValue = element.get(null);
                    Object expectedValue = thePublicStatics.get(fieldName);
                    if (expectedValue == null) {
                        fail("Field " + fieldName + " missing!");
                    } 
                    assertEquals("Field " + fieldName + " value mismatch: ",
                            expectedValue, fieldValue);
                    assertEquals("Field " + fieldName + " modifier mismatch: ",
                            requiredModifier, theMods);
                    countPublicStatics++;
                } catch (IllegalAccessException e) {
                    fail("Illegal access to Field " + fieldName);
                } 
            } 
        } 
    } 
} 
