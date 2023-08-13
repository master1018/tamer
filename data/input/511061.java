class GestureDetectorTestUtil {
    public static void testGestureDetector(InstrumentationTestCase testcase,
            GestureDetectorStubActivity activity) {
        View view = activity.getView();
        TouchUtils.clickView(testcase, view);
        TouchUtils.longClickView(testcase, view);
        TouchUtils.scrollToBottom(testcase, activity, activity.getViewGroup());
        TouchUtils.touchAndCancelView(testcase, view);
        int fromX = 1;
        int toX = 10;
        int fromY = 50;
        int toY = 100;
        int stepCount = 20;
        TouchUtils.drag(testcase, fromX, toX, fromY, toY, stepCount);
        InstrumentationTestCase.assertTrue(activity.isDown);
        InstrumentationTestCase.assertTrue(activity.isScroll);
        InstrumentationTestCase.assertTrue(activity.isFling);
        InstrumentationTestCase.assertTrue(activity.isSingleTapUp);
        InstrumentationTestCase.assertTrue(activity.onLongPress);
        InstrumentationTestCase.assertTrue(activity.onShowPress);
        InstrumentationTestCase.assertTrue(activity.onSingleTapConfirmed);
        InstrumentationTestCase.assertFalse(activity.onDoubleTap);
        InstrumentationTestCase.assertFalse(activity.onDoubleTapEvent);
        TouchUtils.tapView(testcase, view);
        TouchUtils.tapView(testcase, view);
        InstrumentationTestCase.assertTrue(activity.onDoubleTap);
        InstrumentationTestCase.assertTrue(activity.onDoubleTapEvent);
    }
}
