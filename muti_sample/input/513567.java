@TestTargetClass(ObjectOutputStream.PutField.class) 
public class ObjectOutputStreamPutFieldTest extends junit.framework.TestCase {
    private final String FILENAME = 
        "/tests/api/java/io/testFields.ser";
    private final String DEPRECATED_FILENAME = 
        "/tests/api/java/io/testFieldsDeprecated.ser";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, byte.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, char.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, double.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies that content is written correctly to a stream.",
            method = "put",
            args = {java.lang.String.class, short.class}
        )
    })
    public void test_put() throws Exception {
        Support_GetPutFields toSerialize = new Support_GetPutFields();
        byte[] content;
        byte[] refContent;
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos;
        toSerialize.initTestValues();
        try {
            refContent = getRefContent(FILENAME);
            baos = new ByteArrayOutputStream(refContent.length);
            oos = new ObjectOutputStream(baos);
            oos.writeObject(toSerialize);
            content = baos.toByteArray();
            assertTrue("Serialization is not equal to reference platform.", 
                        Arrays.equals(content, refContent));
        }
        finally {
            if (oos != null) oos.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies the deprecated write(ObjectOutput) method.",
        method = "write",
        args = {java.io.ObjectOutput.class}
    )
    public void test_writeLjava_io_ObjectOutputStream() throws Exception {
        Support_GetPutFieldsDeprecated toSerialize = new Support_GetPutFieldsDeprecated();
        byte[] content;
        byte[] refContent;
        ObjectOutputStream oos = null;
        ByteArrayOutputStream baos;
        toSerialize.initTestValues();
        try {
            refContent = getRefContent(DEPRECATED_FILENAME);
            baos = new ByteArrayOutputStream(refContent.length);
            oos = new ObjectOutputStream(baos);
            oos.writeObject(toSerialize);
            content = baos.toByteArray();
            assertTrue("Serialization is not equal to reference platform.", 
                        Arrays.equals(content, refContent));
        }
        finally {
            if (oos != null) oos.close();
        }
    }
    private byte[] getRefContent(String path) throws Exception {
        int bytesRead;
        byte[] refContent;
        byte[] streamContent = new byte[2000];
        InputStream refStream = null;
        try {
            refStream = getClass().getResourceAsStream(path);
            bytesRead = refStream.read(streamContent);
            assertTrue("Test case implementation error: The byte array to " +
                       "store the reference file is too small.", 
                       (refStream.read() == -1));
            refContent = new byte[bytesRead];
            System.arraycopy(streamContent, 0, refContent, 0, bytesRead);
        }
        finally {
            if (refStream != null) refStream.close();
        }
        return refContent;
    }
}
