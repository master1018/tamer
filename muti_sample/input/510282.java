public class TouchUtils {
    @Deprecated
    public static void dragQuarterScreenDown(ActivityInstrumentationTestCase test) {
        dragQuarterScreenDown(test, test.getActivity());
    }
    public static void dragQuarterScreenDown(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int screenHeight = display.getHeight();
        int screenWidth = display.getWidth();
        final float x = screenWidth / 2.0f;
        final float fromY = screenHeight * 0.5f;
        final float toY = screenHeight * 0.75f;
        drag(test, x, x, fromY, toY, 4);
    }
    @Deprecated
    public static void dragQuarterScreenUp(ActivityInstrumentationTestCase test) {
        dragQuarterScreenUp(test, test.getActivity());
    }
    public static void dragQuarterScreenUp(InstrumentationTestCase test, Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int screenHeight = display.getHeight();
        int screenWidth = display.getWidth();
        final float x = screenWidth / 2.0f;
        final float fromY = screenHeight * 0.5f;
        final float toY = screenHeight * 0.25f;
        drag(test, x, x, fromY, toY, 4);
    }
    @Deprecated
    public static void scrollToBottom(ActivityInstrumentationTestCase test, ViewGroup v) {
        scrollToBottom(test, test.getActivity(), v);
    }
    public static void scrollToBottom(InstrumentationTestCase test, Activity activity,
            ViewGroup v) {
        View firstChild;
        int firstId = Integer.MIN_VALUE;
        int firstTop = Integer.MIN_VALUE; 
        int prevId;
        int prevTop; 
        do {
            prevId = firstId;
            prevTop = firstTop;
            TouchUtils.dragQuarterScreenUp(test, activity);
            firstChild = v.getChildAt(0);
            firstId = firstChild.getId();
            firstTop = firstChild.getTop(); 
        } while ((prevId != firstId) || (prevTop != firstTop));
    }
    @Deprecated
    public static void scrollToTop(ActivityInstrumentationTestCase test, ViewGroup v) {
        scrollToTop(test, test.getActivity(), v);
    }
    public static void scrollToTop(InstrumentationTestCase test, Activity activity, ViewGroup v) {
        View firstChild;
        int firstId = Integer.MIN_VALUE;
        int firstTop = Integer.MIN_VALUE; 
        int prevId;
        int prevTop; 
        do {
            prevId = firstId;
            prevTop = firstTop;
            TouchUtils.dragQuarterScreenDown(test, activity);
            firstChild = v.getChildAt(0);
            firstId = firstChild.getId();
            firstTop = firstChild.getTop(); 
        } while ((prevId != firstId) || (prevTop != firstTop));
    }
    @Deprecated
    public static void dragViewToBottom(ActivityInstrumentationTestCase test, View v) {
        dragViewToBottom(test, test.getActivity(), v, 4);
    }
    public static void dragViewToBottom(InstrumentationTestCase test, Activity activity, View v) {
        dragViewToBottom(test, activity, v, 4);
    }
    @Deprecated
    public static void dragViewToBottom(ActivityInstrumentationTestCase test, View v,
            int stepCount) {
        dragViewToBottom(test, test.getActivity(), v, stepCount);
    }
    public static void dragViewToBottom(InstrumentationTestCase test, Activity activity, View v,
            int stepCount) {
        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        final float x = xy[0] + (viewWidth / 2.0f);
        float fromY = xy[1] + (viewHeight / 2.0f);
        float toY = screenHeight - 1;
        drag(test, x, x, fromY, toY, stepCount);
    }
    public static void tapView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        final float x = xy[0] + (viewWidth / 2.0f);
        float y = xy[1] + (viewHeight / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        final int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE,
                x + (touchSlop / 2.0f), y + (touchSlop / 2.0f), 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
    }
    public static void touchAndCancelView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        final float x = xy[0] + (viewWidth / 2.0f);
        float y = xy[1] + (viewHeight / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        final int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_CANCEL,
                x + (touchSlop / 2.0f), y + (touchSlop / 2.0f), 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
    }
    public static void clickView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        final float x = xy[0] + (viewWidth / 2.0f);
        float y = xy[1] + (viewHeight / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        final int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE,
                x + (touchSlop / 2.0f), y + (touchSlop / 2.0f), 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Deprecated
    public static void longClickView(ActivityInstrumentationTestCase test, View v) {
        longClickView((InstrumentationTestCase) test, v);
    }
    public static void longClickView(InstrumentationTestCase test, View v) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        final float x = xy[0] + (viewWidth / 2.0f);
        float y = xy[1] + (viewHeight / 2.0f);
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        final int touchSlop = ViewConfiguration.get(v.getContext()).getScaledTouchSlop();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE,
                x + touchSlop / 2, y + touchSlop / 2, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        try {
            Thread.sleep((long)(ViewConfiguration.getLongPressTimeout() * 1.5f));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
    }
    @Deprecated
    public static void dragViewToTop(ActivityInstrumentationTestCase test, View v) {
        dragViewToTop((InstrumentationTestCase) test, v, 4);
    }
    @Deprecated
    public static void dragViewToTop(ActivityInstrumentationTestCase test, View v, int stepCount) {
        dragViewToTop((InstrumentationTestCase) test, v, stepCount);
    }
    public static void dragViewToTop(InstrumentationTestCase test, View v) {
        dragViewToTop(test, v, 4);
    }
    public static void dragViewToTop(InstrumentationTestCase test, View v, int stepCount) {
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        final float x = xy[0] + (viewWidth / 2.0f);
        float fromY = xy[1] + (viewHeight / 2.0f);
        float toY = 0;
        drag(test, x, x, fromY, toY, stepCount);
    }
    private static void getStartLocation(View v, int gravity, int[] xy) {
        v.getLocationOnScreen(xy);
        final int viewWidth = v.getWidth();
        final int viewHeight = v.getHeight();
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.TOP:
            break;
        case Gravity.CENTER_VERTICAL:
            xy[1] += viewHeight / 2;
            break;
        case Gravity.BOTTOM:
            xy[1] += viewHeight - 1;
            break;
        default:
        }
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
        case Gravity.LEFT:
            break;
        case Gravity.CENTER_HORIZONTAL:
            xy[0] += viewWidth / 2;
            break;
        case Gravity.RIGHT:
            xy[0] += viewWidth - 1;
            break;
        default:
        }
    }
    @Deprecated
    public static int dragViewBy(ActivityInstrumentationTestCase test, View v, int gravity,
            int deltaX, int deltaY) {
        return dragViewBy((InstrumentationTestCase) test, v, gravity, deltaX, deltaY);
    }
    @Deprecated
    public static int dragViewBy(InstrumentationTestCase test, View v, int gravity, int deltaX,
            int deltaY) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        final int fromX = xy[0];
        final int fromY = xy[1];
        int distance = (int) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        drag(test, fromX, fromX + deltaX, fromY, fromY + deltaY, distance);
        return distance;
    }
    @Deprecated
    public static int dragViewTo(ActivityInstrumentationTestCase test, View v, int gravity, int toX,
            int toY) {
        return dragViewTo((InstrumentationTestCase) test, v, gravity, toX, toY);
    }
    public static int dragViewTo(InstrumentationTestCase test, View v, int gravity, int toX,
            int toY) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        final int fromX = xy[0];
        final int fromY = xy[1];
        int deltaX = fromX - toX;
        int deltaY = fromY - toY;
        int distance = (int)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        drag(test, fromX, toX, fromY, toY, distance);
        return distance;
    }
    @Deprecated
    public static int dragViewToX(ActivityInstrumentationTestCase test, View v, int gravity,
            int toX) {
        return dragViewToX((InstrumentationTestCase) test, v, gravity, toX);
    }
    public static int dragViewToX(InstrumentationTestCase test, View v, int gravity, int toX) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        final int fromX = xy[0];
        final int fromY = xy[1];
        int deltaX = fromX - toX;
        drag(test, fromX, toX, fromY, fromY, deltaX);
        return deltaX;
    }
    @Deprecated
    public static int dragViewToY(ActivityInstrumentationTestCase test, View v, int gravity,
            int toY) {
        return dragViewToY((InstrumentationTestCase) test, v, gravity, toY);
    }
    public static int dragViewToY(InstrumentationTestCase test, View v, int gravity, int toY) {
        int[] xy = new int[2];
        getStartLocation(v, gravity, xy);
        final int fromX = xy[0];
        final int fromY = xy[1];
        int deltaY = fromY - toY;
        drag(test, fromX, fromX, fromY, toY, deltaY);
        return deltaY;
    }
    @Deprecated
    public static void drag(ActivityInstrumentationTestCase test, float fromX, float toX,
            float fromY, float toY, int stepCount) {
        drag((InstrumentationTestCase) test, fromX, toX, fromY, toY, stepCount);
    }
    public static void drag(InstrumentationTestCase test, float fromX, float toX, float fromY,
            float toY, int stepCount) {
        Instrumentation inst = test.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        float y = fromY;
        float x = fromX;
        float yStep = (toY - fromY) / stepCount;
        float xStep = (toX - fromX) / stepCount;
        MotionEvent event = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        for (int i = 0; i < stepCount; ++i) {
            y += yStep;
            x += xStep;
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
            inst.sendPointerSync(event);
            inst.waitForIdleSync();
        }
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
    }
}
