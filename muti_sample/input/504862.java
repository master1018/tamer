@TestTargetClass(ContextThemeWrapper.class)
public class ContextThemeWrapperTest extends AndroidTestCase {
    private static final int SYSTEM_DEFAULT_THEME = 0;
    private static class MocContextThemeWrapper extends ContextThemeWrapper {
        public boolean isOnApplyThemeResourceCalled;
        public MocContextThemeWrapper(Context base, int themeres) {
            super(base, themeres);
        }
        @Override
        protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
            isOnApplyThemeResourceCalled = true;
            super.onApplyThemeResource(theme, resid, first);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors.",
            method = "ContextThemeWrapper",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors.",
            method = "ContextThemeWrapper",
            args = {android.content.Context.class, int.class}
        )
    })
    @ToBeFixed(bug="1695243", explanation="Javadocs need update for the constructors.")
    public void testConstructor() {
        new ContextThemeWrapper();
        new ContextThemeWrapper(getContext(), R.style.TextAppearance);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test setTheme.",
            method = "setTheme",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getTheme.",
            method = "getTheme",
            args = {}
        )
    })
    public void testAccessTheme() {
        Context context = getContext();
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(
                context, SYSTEM_DEFAULT_THEME);
        contextThemeWrapper.setTheme(R.style.TextAppearance);
        TypedArray ta =
            contextThemeWrapper.getTheme().obtainStyledAttributes(R.styleable.TextAppearance);
        assertEqualsTextAppearanceStyle(ta);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getSystemService.",
            method = "getSystemService",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test onApplyThemeResource.",
            method = "onApplyThemeResource",
            args = {android.content.res.Resources.Theme.class, int.class, boolean.class}
        )
    })
    public void testGetSystemService() {
        Context context = getContext();
        int themeres = R.style.TextAppearance;
        MocContextThemeWrapper contextThemeWrapper = new MocContextThemeWrapper(context, themeres);
        contextThemeWrapper.getTheme();
        assertTrue(contextThemeWrapper.isOnApplyThemeResourceCalled);
        assertEquals(context.getSystemService(Context.ACTIVITY_SERVICE),
                contextThemeWrapper.getSystemService(Context.ACTIVITY_SERVICE));
        assertNotSame(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                contextThemeWrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test attachBaseContext.",
        method = "attachBaseContext",
        args = {android.content.Context.class}
    )
    public void testAttachBaseContext() {
        assertTrue((new ContextThemeWrapper() {
            public boolean test() {
                try {
                    attachBaseContext(new ContextThemeWrapper(getContext(),
                            R.style.TextAppearance));
                } catch(IllegalStateException e) {
                    fail("test attachBaseContext fail");
                }
                try {
                    attachBaseContext(new ContextThemeWrapper());
                    fail("test attachBaseContext fail");
                } catch(IllegalStateException e) {
                }
                return true;
            }
        }).test());
    }
    private void assertEqualsTextAppearanceStyle(TypedArray ta) {
        final int defValue = -1;
        Resources.Theme expected = getContext().getResources().newTheme();
        expected.setTo(getContext().getTheme());
        expected.applyStyle(R.style.TextAppearance, true);
        TypedArray expectedTa = expected.obtainStyledAttributes(R.styleable.TextAppearance);
        assertEquals(expectedTa.getIndexCount(), ta.getIndexCount());
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColor, defValue),
                ta.getColor(R.styleable.TextAppearance_textColor, defValue));
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColorHint, defValue),
                ta.getColor(R.styleable.TextAppearance_textColorHint, defValue));
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColorLink, defValue),
                ta.getColor(R.styleable.TextAppearance_textColorLink, defValue));
        assertEquals(expectedTa.getColor(R.styleable.TextAppearance_textColorHighlight, defValue),
                ta.getColor(R.styleable.TextAppearance_textColorHighlight, defValue));
        assertEquals(expectedTa.getDimension(R.styleable.TextAppearance_textSize, defValue),
                ta.getDimension(R.styleable.TextAppearance_textSize, defValue));
        assertEquals(expectedTa.getInt(R.styleable.TextAppearance_textStyle, defValue),
                ta.getInt(R.styleable.TextAppearance_textStyle, defValue));
    }
}
