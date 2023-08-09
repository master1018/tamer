@TestTargetClass(DialerKeyListener.class)
public class DialerKeyListenerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link DialerKeyListener}",
        method = "DialerKeyListener",
        args = {}
    )
    public void testConstructor() {
        new DialerKeyListener();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DialerKeyListener#lookup(KeyEvent, Spannable)}",
        method = "lookup",
        args = {android.view.KeyEvent.class, android.text.Spannable.class}
    )
    @ToBeFixed(bug="1371108", explanation="NPE is not expected.")
    public void testLookup() {
        MockDialerKeyListener mockDialerKeyListener = new MockDialerKeyListener();
        final int[] events = { KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_A };
        SpannableString span = new SpannableString(""); 
        for (int event: events) {
            KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, event);
            int keyChar = keyEvent.getNumber();
            if (keyChar != 0) {
                assertEquals(keyChar, mockDialerKeyListener.lookup(keyEvent, span));
            } else {
            }
        }
        try {
            mockDialerKeyListener.lookup(null, span);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DialerKeyListener#getInstance()}",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        assertNotNull(DialerKeyListener.getInstance());
        DialerKeyListener listener1 = DialerKeyListener.getInstance();
        DialerKeyListener listener2 = DialerKeyListener.getInstance();
        assertTrue(listener1 instanceof DialerKeyListener);
        assertTrue(listener2 instanceof DialerKeyListener);
        assertSame(listener1, listener2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DialerKeyListener#getAcceptedChars()}",
        method = "getAcceptedChars",
        args = {}
    )
    public void testGetAcceptedChars() {
        MockDialerKeyListener mockDialerKeyListener = new MockDialerKeyListener();
        TextMethodUtils.assertEquals(DialerKeyListener.CHARACTERS,
                mockDialerKeyListener.getAcceptedChars());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DialerKeyListener#getInputType()}",
        method = "getInputType",
        args = {}
    )
    public void testGetInputType() {
        DialerKeyListener listener = DialerKeyListener.getInstance();
        assertEquals(InputType.TYPE_CLASS_PHONE, listener.getInputType());
    }
    private class MockDialerKeyListener extends DialerKeyListener {
        protected char[] getAcceptedChars() {
            return super.getAcceptedChars();
        }
        protected int lookup(KeyEvent event, Spannable content) {
            return super.lookup(event, content);
        }
    }
}
