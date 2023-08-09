public class LinkifyTest extends AndroidTestCase {
    @SmallTest
    public void testNothing() throws Exception {
        TextView tv;
        tv = new TextView(getContext());
        tv.setText("Hey, foo@google.com, call 415-555-1212.");
        assertFalse(tv.getMovementMethod() instanceof LinkMovementMethod);
        assertTrue(tv.getUrls().length == 0);
    }
    @MediumTest
    public void testNormal() throws Exception {
        TextView tv;
        tv = new TextView(getContext());
        tv.setAutoLinkMask(Linkify.ALL);
        tv.setText("Hey, foo@google.com, call 415-555-1212.");
        assertTrue(tv.getMovementMethod() instanceof LinkMovementMethod);
        assertTrue(tv.getUrls().length == 2);
    }
    @SmallTest
    public void testUnclickable() throws Exception {
        TextView tv;
        tv = new TextView(getContext());
        tv.setAutoLinkMask(Linkify.ALL);
        tv.setLinksClickable(false);
        tv.setText("Hey, foo@google.com, call 415-555-1212.");
        assertFalse(tv.getMovementMethod() instanceof LinkMovementMethod);
        assertTrue(tv.getUrls().length == 2);
    }
}
