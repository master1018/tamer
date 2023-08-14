@TestTargetClass(InputSource.class)
public class InputSourceTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "InputSource",
        args = { }
    )
    public void testInputSource() {
        InputSource i = new InputSource();
        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "InputSource",
        args = { String.class }
    )
    public void testInputSourceString() {
        InputSource i = new InputSource("Foo");
        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertEquals("Foo", i.getSystemId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "InputSource",
        args = { InputStream.class }
    )
    public void testInputSourceInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        InputSource i = new InputSource(bais);
        assertEquals(bais, i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
        i = new InputSource((InputStream)null);
        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "InputSource",
        args = { Reader.class }
    )
    public void testInputSourceReader() {
        StringReader sr = new StringReader("Hello, world.");
        InputSource i = new InputSource(sr);
        assertNull(i.getByteStream());
        assertEquals(sr, i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
        i = new InputSource((Reader)null);
        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPublicId",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPublicId",
            args = {  }
        )
    })
    public void testSetPublicIdGetPublicId() {
        InputSource i = new InputSource();
        i.setPublicId("Foo");
        assertEquals("Foo", i.getPublicId());
        i.setPublicId(null);
        assertNull(i.getPublicId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSystemId",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSystemId",
            args = {  }
        )
    })
    public void testSetSystemIdGetSystemId() {
        InputSource i = new InputSource();
        i.setSystemId("Foo");
        assertEquals("Foo", i.getSystemId());
        i.setSystemId(null);
        assertNull(i.getSystemId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setByteStream",
            args = { InputStream.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getByteStream",
            args = {  }
        )
    })
    public void testSetByteStreamGetByteStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        InputSource i = new InputSource();
        i.setByteStream(bais);
        assertEquals(bais, i.getByteStream());
        i.setByteStream(null);
        assertNull(i.getByteStream());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEncoding",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEncoding",
            args = {  }
        )
    })
    public void testSetEncodingGetEncoding() {
        InputSource i = new InputSource();
        i.setEncoding("Klingon");
        assertEquals("Klingon", i.getEncoding());
        i.setEncoding(null);
        assertNull(i.getEncoding());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCharacterStream",
            args = { Reader.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCharacterStream",
            args = {  }
        )
    })
    public void testSetCharacterStreamGetCharacterStream() {
        StringReader sr = new StringReader("Hello, world.");
        InputSource i = new InputSource();
        i.setCharacterStream(sr);
        assertNull(i.getByteStream());
        assertEquals(sr, i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
        i.setCharacterStream(null);
        assertNull(i.getByteStream());
        assertNull(i.getCharacterStream());
        assertNull(i.getEncoding());
        assertNull(i.getPublicId());
        assertNull(i.getSystemId());
    }
}
