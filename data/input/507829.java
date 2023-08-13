@TestTargetClass(Annotation.class)
public class AnnotationTest extends AndroidTestCase {
    private static final String KEY1 = "name";
    private static final String KEY2 = "family name";
    private static final String VALUE1 = "John";
    private static final String VALUE2 = "Smith";
    private static final int NOFLAG = 0;
    private Annotation mAnnotation;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAnnotation = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Annotation",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testConstructor() {
        new Annotation(KEY1, VALUE1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getValue",
        args = {}
    )
    public void testGetValue() {
        mAnnotation = new Annotation(KEY1, VALUE1);
        assertEquals(VALUE1, mAnnotation.getValue());
        mAnnotation = new Annotation(KEY2, VALUE2);
        assertEquals(VALUE2, mAnnotation.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getKey",
        args = {}
    )
    public void testGetKey() {
        mAnnotation = new Annotation(KEY1, VALUE1);
        assertEquals(KEY1, mAnnotation.getKey());
        mAnnotation = new Annotation(KEY2, VALUE2);
        assertEquals(KEY2, mAnnotation.getKey());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        mAnnotation = new Annotation(KEY1, VALUE1);
        assertTrue(mAnnotation.getSpanTypeId() != 0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Annotation",
            args = {Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        )
    })
    public void testWriteToParcel() {
        Parcel dest = Parcel.obtain();
        mAnnotation = new Annotation(KEY1, VALUE1);
        mAnnotation.writeToParcel(dest, NOFLAG);
        dest.setDataPosition(0);
        Annotation out = new Annotation(dest);
        assertEquals(out.getKey(), mAnnotation.getKey());
        assertEquals(out.getValue(), mAnnotation.getValue());
        assertEquals(0, out.describeContents());
    }
}
