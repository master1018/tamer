@TestTargetClass(android.database.CharArrayBuffer.class)
public class CharArrayBufferTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "CharArrayBuffer",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "CharArrayBuffer",
            args = {char[].class}
        )
    })
    public void testCharArrayBuffer() {
        CharArrayBuffer charArrayBuffer;
        charArrayBuffer = new CharArrayBuffer(0);
        assertEquals(0, charArrayBuffer.data.length);
        charArrayBuffer.data = new char[100];
        assertEquals(100, charArrayBuffer.data.length);
        assertEquals(100, (new CharArrayBuffer(100)).data.length);
        assertNull((new CharArrayBuffer(null)).data);
        char[] expectedData = new char[100];
        assertSame(expectedData, (new CharArrayBuffer(expectedData)).data);
    }
}
