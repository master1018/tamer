@TestTargetClass(ComponentName.class)
public class ComponentNameTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentName",
            args = {android.content.Context.class, java.lang.Class.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentName",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentName",
            args = {android.content.Context.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ComponentName",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "NullPointerException is not expected.")
    public void testConstructor() {
        new ComponentName("com.android.app", "com.android.app.InstrumentationTestActivity");
        try {
            new ComponentName((String) null, (String) null);
            fail("ComponentName's constructor (String, Stirng) can not accept null input values.");
        } catch (NullPointerException e) {
        }
        new ComponentName(mContext, "ActivityTestCase");
        try {
            new ComponentName((Context) null, "ActivityTestCase");
            fail("class name is null, the constructor should throw a exception");
        } catch (NullPointerException e) {
        }
        try {
            new ComponentName(mContext, (String) null);
            fail("Constructor should not accept null class name.");
        } catch (NullPointerException e) {
        }
        new ComponentName(mContext, this.getClass());
        try {
            new ComponentName(mContext, (Class<?>) null);
            fail("If class name is null, contructor should throw a exception");
        } catch (NullPointerException e) {
        }
        try {
            new ComponentName((Parcel) null);
            fail("Constructor should not accept null Parcel input.");
        } catch (NullPointerException e) {
        }
        final Parcel parcel = Parcel.obtain();
        final ComponentName componentName = getComponentName();
        componentName.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        new ComponentName(parcel);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "flattenToString",
        args = {}
    )
    public void testFlattenToString() {
        assertEquals("com.android.cts.stub/android.content.cts.ComponentNameTest",
                getComponentName().flattenToString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getShortClassName",
        args = {}
    )
    public void testGetShortClassName() {
        String actual = getComponentName().getShortClassName();
        assertEquals("android.content.cts.ComponentNameTest", actual);
        ComponentName componentName = new ComponentName("com.android.view",
                "com.android.view.View");
        final String className = componentName.getClassName();
        assertEquals("com.android.view.View", className);
        actual = componentName.getShortClassName();
        assertEquals(".View", actual);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFromParcel",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    public void testReadFromParcel() {
        ComponentName expected = getComponentName();
        final Parcel parcel1 = Parcel.obtain();
        expected.writeToParcel(parcel1, 0);
        parcel1.setDataPosition(0);
        ComponentName actual = ComponentName.readFromParcel(parcel1);
        assertEquals(expected, actual);
        final Parcel parcel2 = Parcel.obtain();
        expected = ComponentName.readFromParcel(parcel2);
        assertNull(expected);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPackageName",
        args = {}
    )
    public void testGetPackageName() {
        final String actual = getComponentName().getPackageName();
        assertEquals("com.android.cts.stub", actual);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "unflattenFromString",
        args = {java.lang.String.class}
    )
    public void testUnflattenFromString() {
        final ComponentName componentName = getComponentName();
        final String flattenString = getComponentName().flattenToString();
        assertNotNull(flattenString);
        ComponentName actual = ComponentName.unflattenFromString(flattenString);
        assertEquals(componentName, actual);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "flattenToShortString",
        args = {}
    )
    public void testFlattenToShortString() {
        String actual = getComponentName().flattenToShortString();
        assertEquals("com.android.cts.stub/android.content.cts.ComponentNameTest", actual);
        final ComponentName componentName = new ComponentName("com.android.view",
                "com.android.view.View");
        final String falttenString = componentName.flattenToString();
        assertEquals("com.android.view/com.android.view.View", falttenString);
        actual = componentName.flattenToShortString();
        assertEquals("com.android.view/.View", actual);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        final ComponentName componentName1 = getComponentName();
        ComponentName componentName2 = new ComponentName(componentName1.getPackageName(),
                componentName1.getShortClassName());
        assertTrue(componentName1.equals(componentName2));
        componentName2 = new ComponentName(componentName1.getPackageName(),
                componentName1.getShortClassName() + "different name");
        assertFalse(componentName1.equals(componentName2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertNotNull(getComponentName().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toShortString",
        args = {}
    )
    public void testToShortString() {
        final String shortString = getComponentName().toShortString();
        assertEquals("{com.android.cts.stub/android.content.cts.ComponentNameTest}", shortString);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getClassName",
        args = {}
    )
    public void testGetClassName() {
        final String className = getComponentName().getClassName();
        assertEquals("android.content.cts.ComponentNameTest", className);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        final ComponentName componentName = getComponentName();
        final int hashCode1 = componentName.hashCode();
        assertFalse(0 == hashCode1);
        final ComponentName componentName2 = new ComponentName(componentName.getPackageName(),
                componentName.getClassName());
        final int hashCode2 = componentName2.hashCode();
        assertEquals(hashCode1, hashCode2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.content.ComponentName.class, android.os.Parcel.class}
    )
    public void testWriteToParcel() {
        final ComponentName componentName = getComponentName();
        Parcel parcel = Parcel.obtain();
        ComponentName.writeToParcel(componentName, parcel);
        parcel.setDataPosition(0);
        assertFalse(0 == parcel.dataAvail());
        assertEquals("com.android.cts.stub", parcel.readString());
        assertEquals("android.content.cts.ComponentNameTest", parcel.readString());
        parcel = Parcel.obtain();
        ComponentName.writeToParcel(null, parcel);
        assertEquals(0, parcel.dataAvail());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        assertEquals(0, getComponentName().describeContents());
    }
    private ComponentName getComponentName() {
        final ComponentName componentName = new ComponentName(mContext, this.getClass());
        return componentName;
    }
}
