public class ViewGroupChildrenTest extends ActivityInstrumentationTestCase<ViewGroupChildren> {
    private ViewGroup mGroup;
    public ViewGroupChildrenTest() {
        super("com.android.frameworks.coretests", ViewGroupChildren.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final ViewGroupChildren a = getActivity();
        mGroup = (ViewGroup) a.findViewById(R.id.group);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mGroup);
    }
    @MediumTest
    public void testStartsEmpty() throws Exception {
        assertEquals("A ViewGroup should have no child by default", 0, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testAddChild() throws Exception {
        View view = createView("1");
        mGroup.addView(view);
        assertEquals(1, mGroup.getChildCount());
        ViewAsserts.assertGroupIntegrity(mGroup);
        ViewAsserts.assertGroupContains(mGroup, view);
    }
    @UiThreadTest
    @MediumTest
    public void testAddChildAtFront() throws Exception {
        for (int i = 0; i < 24; i++) {
            View view = createView(String.valueOf(i + 1));
            mGroup.addView(view);
        }
        View view = createView("X");
        mGroup.addView(view, 0);
        assertEquals(25, mGroup.getChildCount());
        ViewAsserts.assertGroupIntegrity(mGroup);
        ViewAsserts.assertGroupContains(mGroup, view);
        assertSame(view, mGroup.getChildAt(0));
    }
    @UiThreadTest
    @MediumTest
    public void testAddChildInMiddle() throws Exception {
        for (int i = 0; i < 24; i++) {
            View view = createView(String.valueOf(i + 1));
            mGroup.addView(view);
        }
        View view = createView("X");
        mGroup.addView(view, 12);
        assertEquals(25, mGroup.getChildCount());
        ViewAsserts.assertGroupIntegrity(mGroup);
        ViewAsserts.assertGroupContains(mGroup, view);
        assertSame(view, mGroup.getChildAt(12));
    }
    @UiThreadTest
    @MediumTest
    public void testAddChildren() throws Exception {
        for (int i = 0; i < 24; i++) {
            View view = createView(String.valueOf(i + 1));
            mGroup.addView(view);
            ViewAsserts.assertGroupContains(mGroup, view);
        }
        assertEquals(24, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChild() throws Exception {
        View view = createView("1");
        mGroup.addView(view);
        ViewAsserts.assertGroupIntegrity(mGroup);
        mGroup.removeView(view);
        ViewAsserts.assertGroupIntegrity(mGroup);
        ViewAsserts.assertGroupNotContains(mGroup, view);
        assertEquals(0, mGroup.getChildCount());
        assertNull(view.getParent());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChildren() throws Exception {
        final View[] views = new View[24];
        for (int i = 0; i < views.length; i++) {
            views[i] = createView(String.valueOf(i + 1));
            mGroup.addView(views[i]);
        }
        for (int i = views.length - 1; i >= 0; i--) {
            mGroup.removeViewAt(i);
            ViewAsserts.assertGroupIntegrity(mGroup);
            ViewAsserts.assertGroupNotContains(mGroup, views[i]);
            assertNull(views[i].getParent());
        }
        assertEquals(0, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChildrenBulk() throws Exception {
        final View[] views = new View[24];
        for (int i = 0; i < views.length; i++) {
            views[i] = createView(String.valueOf(i + 1));
            mGroup.addView(views[i]);
        }
        mGroup.removeViews(6, 7);
        ViewAsserts.assertGroupIntegrity(mGroup);
        for (int i = 6; i < 13; i++) {
            ViewAsserts.assertGroupNotContains(mGroup, views[i]);
            assertNull(views[i].getParent());
        }
        assertEquals(17, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChildrenBulkAtFront() throws Exception {
        final View[] views = new View[24];
        for (int i = 0; i < views.length; i++) {
            views[i] = createView(String.valueOf(i + 1));
            mGroup.addView(views[i]);
        }
        mGroup.removeViews(0, 7);
        ViewAsserts.assertGroupIntegrity(mGroup);
        for (int i = 0; i < 7; i++) {
            ViewAsserts.assertGroupNotContains(mGroup, views[i]);
            assertNull(views[i].getParent());
        }
        assertEquals("8", ((TextView) mGroup.getChildAt(0)).getText());
        assertEquals(17, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChildrenBulkAtEnd() throws Exception {
        final View[] views = new View[24];
        for (int i = 0; i < views.length; i++) {
            views[i] = createView(String.valueOf(i + 1));
            mGroup.addView(views[i]);
        }
        mGroup.removeViews(17, 7);
        ViewAsserts.assertGroupIntegrity(mGroup);
        for (int i = 17; i < 24; i++) {
            ViewAsserts.assertGroupNotContains(mGroup, views[i]);
            assertNull(views[i].getParent());
        }
        assertEquals("17", ((TextView) mGroup.getChildAt(mGroup.getChildCount() - 1)).getText());
        assertEquals(17, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChildAtFront() throws Exception {
        final View[] views = new View[24];
        for (int i = 0; i < views.length; i++) {
            views[i] = createView(String.valueOf(i + 1));
            mGroup.addView(views[i]);
        }
        mGroup.removeViewAt(0);
        ViewAsserts.assertGroupIntegrity(mGroup);
        ViewAsserts.assertGroupNotContains(mGroup, views[0]);
        assertNull(views[0].getParent());
        assertEquals(views.length - 1, mGroup.getChildCount());
    }
    @UiThreadTest
    @MediumTest
    public void testRemoveChildInMiddle() throws Exception {
        final View[] views = new View[24];
        for (int i = 0; i < views.length; i++) {
            views[i] = createView(String.valueOf(i + 1));
            mGroup.addView(views[i]);
        }
        mGroup.removeViewAt(12);
        ViewAsserts.assertGroupIntegrity(mGroup);
        ViewAsserts.assertGroupNotContains(mGroup, views[12]);
        assertNull(views[12].getParent());
        assertEquals(views.length - 1, mGroup.getChildCount());        
    }
    private TextView createView(String text) {
        TextView view = new TextView(getActivity());
        view.setText(text);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return view;
    }
}
