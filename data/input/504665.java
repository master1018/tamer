@TestTargetClass(PatternMatcher.class)
public class PatternMatcherTest extends TestCase {
    private PatternMatcher mPatternMatcher;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPatternMatcher = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "PatternMatcher",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor",
            method = "PatternMatcher",
            args = {java.lang.String.class, int.class}
        )
    })
    public void testConstructor() {
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_LITERAL);
        assertNotNull(mPatternMatcher);
        Parcel p = Parcel.obtain();
        p.writeString("test");
        p.writeInt(PatternMatcher.PATTERN_LITERAL);
        p.setDataPosition(0);
        mPatternMatcher = new PatternMatcher(p);
        assertNotNull(mPatternMatcher);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getType",
        method = "getType",
        args = {}
    )
    public void testGetType() {
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_LITERAL);
        assertEquals(PatternMatcher.PATTERN_LITERAL, mPatternMatcher.getType());
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_PREFIX);
        assertEquals(PatternMatcher.PATTERN_PREFIX, mPatternMatcher.getType());
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_SIMPLE_GLOB);
        assertEquals(PatternMatcher.PATTERN_SIMPLE_GLOB, mPatternMatcher.getType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getPath",
        method = "getPath",
        args = {}
    )
    public void testGetPath() {
        String expected1 = "test1";
        mPatternMatcher = new PatternMatcher(expected1, PatternMatcher.PATTERN_LITERAL);
        assertEquals(expected1, mPatternMatcher.getPath());
        String expected2 = "test2";
        mPatternMatcher = new PatternMatcher(expected2, PatternMatcher.PATTERN_LITERAL);
        assertEquals(expected2, mPatternMatcher.getPath());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test toString",
        method = "toString",
        args = {}
    )
    public void testToString() {
        String str = "test";
        String expected1 = "PatternMatcher{LITERAL: test}";
        String expected2 = "PatternMatcher{PREFIX: test}";
        String expected3 = "PatternMatcher{GLOB: test}";
        mPatternMatcher = new PatternMatcher(str, PatternMatcher.PATTERN_LITERAL);
        assertEquals(expected1, mPatternMatcher.toString());
        mPatternMatcher = new PatternMatcher(str, PatternMatcher.PATTERN_PREFIX);
        assertEquals(expected2, mPatternMatcher.toString());
        mPatternMatcher = new PatternMatcher(str, PatternMatcher.PATTERN_SIMPLE_GLOB);
        assertEquals(expected3, mPatternMatcher.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel",
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        String expected = "test1";
        mPatternMatcher = new PatternMatcher(expected, PatternMatcher.PATTERN_LITERAL);
        Parcel p = Parcel.obtain();
        mPatternMatcher.writeToParcel(p, 0);
        p.setDataPosition(0);
        assertEquals(expected, p.readString());
        assertEquals(PatternMatcher.PATTERN_LITERAL, p.readInt());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents",
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_LITERAL);
        assertEquals(0, mPatternMatcher.describeContents());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test match",
        method = "match",
        args = {java.lang.String.class}
    )
    public void testMatch() {
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_LITERAL);
        assertTrue(mPatternMatcher.match("test"));
        assertFalse(mPatternMatcher.match("test1"));
        mPatternMatcher = new PatternMatcher("test", PatternMatcher.PATTERN_PREFIX);
        assertTrue(mPatternMatcher.match("testHello"));
        assertFalse(mPatternMatcher.match("atestHello"));
        mPatternMatcher = new PatternMatcher("test", -1);
        assertFalse(mPatternMatcher.match("testHello"));
        assertFalse(mPatternMatcher.match("test"));
        assertFalse(mPatternMatcher.match("atestHello"));
        mPatternMatcher = new PatternMatcher("", PatternMatcher.PATTERN_SIMPLE_GLOB);
        assertTrue(mPatternMatcher.match(""));
        mPatternMatcher = new PatternMatcher("....", PatternMatcher.PATTERN_SIMPLE_GLOB);
        assertTrue(mPatternMatcher.match("test"));
        mPatternMatcher = new PatternMatcher("d*", PatternMatcher.PATTERN_SIMPLE_GLOB);
        assertFalse(mPatternMatcher.match("test"));
    }
}
