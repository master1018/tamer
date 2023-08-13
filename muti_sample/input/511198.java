public class ViewAsserts {
    private ViewAsserts() {}
    static public void assertOnScreen(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        int y = xy[1] - xyRoot[1];
        assertTrue("view should have positive y coordinate on screen",
                y  >= 0);
        assertTrue("view should have y location on screen less than drawing "
                + "height of root view",
                y <= view.getRootView().getHeight());
    }
    static public void assertOffScreenBelow(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        int y = xy[1] - xyRoot[1];
        assertTrue("view should have y location on screen greater than drawing "
                + "height of origen view (" + y + " is not greater than "
                + origin.getHeight() + ")",
                y > origin.getHeight());
    }
    static public void assertOffScreenAbove(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        int y = xy[1] - xyRoot[1];
        assertTrue("view should have y location less than that of origin view",
                y < 0);
    }
    static public void assertHasScreenCoordinates(View origin, View view, int x, int y) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        assertEquals("x coordinate", x, xy[0] - xyRoot[0]);
        assertEquals("y coordinate", y, xy[1] - xyRoot[1]);
    }
    static public void assertBaselineAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstTop = xy[1] + first.getBaseline();
        second.getLocationOnScreen(xy);
        int secondTop = xy[1] + second.getBaseline();
        assertEquals("views are not baseline aligned", firstTop, secondTop);
    }
    static public void assertRightAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstRight = xy[0] + first.getMeasuredWidth();
        second.getLocationOnScreen(xy);
        int secondRight = xy[0] + second.getMeasuredWidth();
        assertEquals("views are not right aligned", firstRight, secondRight);
    }
    static public void assertRightAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstRight = xy[0] + first.getMeasuredWidth();
        second.getLocationOnScreen(xy);
        int secondRight = xy[0] + second.getMeasuredWidth();
        assertEquals("views are not right aligned", Math.abs(firstRight - secondRight), margin);
    }
    static public void assertLeftAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstLeft = xy[0];
        second.getLocationOnScreen(xy);
        int secondLeft = xy[0];
        assertEquals("views are not left aligned", firstLeft, secondLeft);
    }
    static public void assertLeftAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstLeft = xy[0];
        second.getLocationOnScreen(xy);
        int secondLeft = xy[0];
        assertEquals("views are not left aligned", Math.abs(firstLeft - secondLeft), margin);
    }
    static public void assertBottomAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstBottom = xy[1] + first.getMeasuredHeight();
        second.getLocationOnScreen(xy);
        int secondBottom = xy[1] + second.getMeasuredHeight();
        assertEquals("views are not bottom aligned", firstBottom, secondBottom);
    }
    static public void assertBottomAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstBottom = xy[1] + first.getMeasuredHeight();
        second.getLocationOnScreen(xy);
        int secondBottom = xy[1] + second.getMeasuredHeight();
        assertEquals("views are not bottom aligned", Math.abs(firstBottom - secondBottom), margin);
    }
    static public void assertTopAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstTop = xy[1];
        second.getLocationOnScreen(xy);
        int secondTop = xy[1];
        assertEquals("views are not top aligned", firstTop, secondTop);
    }
    static public void assertTopAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstTop = xy[1];
        second.getLocationOnScreen(xy);
        int secondTop = xy[1];
        assertEquals("views are not top aligned", Math.abs(firstTop - secondTop), margin);
    }
    static public void assertHorizontalCenterAligned(View reference, View test) {
        int[] xy = new int[2];
        reference.getLocationOnScreen(xy);
        int referenceLeft = xy[0];
        test.getLocationOnScreen(xy);
        int testLeft = xy[0];
        int center = (reference.getMeasuredWidth() - test.getMeasuredWidth()) / 2;
        int delta = testLeft - referenceLeft;
        assertEquals("views are not horizontally center aligned", center, delta);
    }
    static public void assertVerticalCenterAligned(View reference, View test) {
        int[] xy = new int[2];
        reference.getLocationOnScreen(xy);
        int referenceTop = xy[1];
        test.getLocationOnScreen(xy);
        int testTop = xy[1];
        int center = (reference.getMeasuredHeight() - test.getMeasuredHeight()) / 2;
        int delta = testTop - referenceTop;
        assertEquals("views are not vertically center aligned", center, delta);
    }
    static public void assertGroupIntegrity(ViewGroup parent) {
        final int count = parent.getChildCount();
        assertTrue("child count should be >= 0", count >= 0);
        for (int i = 0; i < count; i++) {
            assertNotNull("group should not contain null children", parent.getChildAt(i));
            assertSame(parent, parent.getChildAt(i).getParent());
        }
    }
    static public void assertGroupContains(ViewGroup parent, View child) {
        final int count = parent.getChildCount();
        assertTrue("Child count should be >= 0", count >= 0);
        boolean found = false;
        for (int i = 0; i < count; i++) {
            if (parent.getChildAt(i) == child) {
                if (!found) {
                    found = true;
                } else {
                    assertTrue("child " + child + " is duplicated in parent", false);
                }
            }
        }
        assertTrue("group does not contain " + child, found);
    }
    static public void assertGroupNotContains(ViewGroup parent, View child) {
        final int count = parent.getChildCount();
        assertTrue("Child count should be >= 0", count >= 0);
        for (int i = 0; i < count; i++) {
            if (parent.getChildAt(i) == child) {
                assertTrue("child " + child + " is found in parent", false);
            }
        }
    }
}
