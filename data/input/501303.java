@TestTargetClass(MsgHelp.class)
public class MsgHelpTest extends TestCase {
    public MsgHelpTest(String name) {
        super(name);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "format",
        args = {java.lang.String.class, java.lang.Object[].class}
    )        
    public void testFormatLjava_lang_String$Ljava_lang_Object() {
        assertEquals("empty", MsgHelp.format("empty", new Object[0]));
        assertEquals("<null>", MsgHelp.format("{0}", new Object[1]));
        assertEquals("<missing argument>", MsgHelp.format("{0}", new Object[0]));
        assertEquals("fixture {} fixture", MsgHelp.format("{0} \\{} {0}",
                new Object[] { "fixture" }));
        assertEquals("<null> fixture", MsgHelp.format("{0} {1}", new Object[] {
                null, "fixture" }));
        assertEquals("<null> fixture <missing argument>", MsgHelp.format(
                "{0} {1} {2}", new Object[] { null, "fixture" }));
        assertEquals("<null> fixture", MsgHelp.format("{0} {1}", new Object[] {
                null, "fixture", "extra" }));
        assertEquals("0 1 2 3 4 5 6 7 8 9", MsgHelp.format(
                "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9}", new Object[] { "0",
                        "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        assertEquals("9 8 7 6 5 4 3 2 1 0", MsgHelp.format(
                "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9}", new Object[] { "9",
                        "8", "7", "6", "5", "4", "3", "2", "1", "0" }));
        assertEquals("0 1 2 3 4 5 6 7 8 9 {10}", MsgHelp.format(
                "{0} {1} {2} {3} {4} {5} {6} {7} {8} {9} {10}",
                new Object[] { "0", "1", "2", "3", "4", "5", "6", "7", "8",
                        "9", "10" }));
        try {
            MsgHelp.format(null, new Object[0]);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
        try {
            MsgHelp.format("fixture", null);
            fail("No NPE");
        } catch (NullPointerException e) {
        }
    }
}