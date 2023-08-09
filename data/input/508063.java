public class AccessibilityTestService extends AccessibilityService {
    private static final String LOG_TAG = "AccessibilityTestService";
    private static final String CLASS_NAME = "foo.bar.baz.Test";
    private static final String PACKAGE_NAME = "foo.bar.baz";
    private static final String TEXT = "Some stuff";
    private static final String BEFORE_TEXT = "Some other stuff";
    private static final String CONTENT_DESCRIPTION = "Content description";
    private static final int ITEM_COUNT = 10;
    private static final int CURRENT_ITEM_INDEX = 1;
    private static final int INTERRUPT_INVOCATION_TYPE = 0x00000200;
    private static final int FROM_INDEX = 1;
    private static final int ADDED_COUNT = 2;
    private static final int REMOVED_COUNT = 1;
    private static final int NOTIFICATION_TIMEOUT_MILLIS = 80;
    private int mReceivedResult;
    private Timer mTimer = new Timer();
    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE;
        info.notificationTimeout = NOTIFICATION_TIMEOUT_MILLIS;
        info.flags &= AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    testAccessibilityEventDispatching();
                    testInterrupt();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error in testing Accessibility feature", e);
                }
            }
        }, 1000);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        assert(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED == event.getEventType());
        assert(event != null);
        assert(event.getEventTime() > 0);
        assert(CLASS_NAME.equals(event.getClassName()));
        assert(PACKAGE_NAME.equals(event.getPackageName()));
        assert(1 == event.getText().size());
        assert(TEXT.equals(event.getText().get(0)));
        assert(BEFORE_TEXT.equals(event.getBeforeText()));
        assert(event.isChecked());
        assert(CONTENT_DESCRIPTION.equals(event.getContentDescription()));
        assert(ITEM_COUNT == event.getItemCount());
        assert(CURRENT_ITEM_INDEX == event.getCurrentItemIndex());
        assert(event.isEnabled());
        assert(event.isPassword());
        assert(FROM_INDEX == event.getFromIndex());
        assert(ADDED_COUNT == event.getAddedCount());
        assert(REMOVED_COUNT == event.getRemovedCount());
        assert(event.getParcelableData() != null);
        assert(1 == ((Notification) event.getParcelableData()).icon);
        mReceivedResult = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
    }
    @Override
    public void onInterrupt() {
        mReceivedResult = INTERRUPT_INVOCATION_TYPE;
    }
   public void testAccessibilityEventDispatching() throws Exception {
       AccessibilityEvent event =
           AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
       assert(event != null);
       event.setClassName(CLASS_NAME);
       event.setPackageName(PACKAGE_NAME);
       event.getText().add(TEXT);
       event.setBeforeText(BEFORE_TEXT);
       event.setChecked(true);
       event.setContentDescription(CONTENT_DESCRIPTION);
       event.setItemCount(ITEM_COUNT);
       event.setCurrentItemIndex(CURRENT_ITEM_INDEX);
       event.setEnabled(true);
       event.setPassword(true);
       event.setFromIndex(FROM_INDEX);
       event.setAddedCount(ADDED_COUNT);
       event.setRemovedCount(REMOVED_COUNT);
       event.setParcelableData(new Notification(1, "Foo", 1234));
       AccessibilityManager.getInstance(this).sendAccessibilityEvent(event);
       assert(mReceivedResult == event.getEventType());
       Log.i(LOG_TAG, "AccessibilityTestService#testAccessibilityEventDispatching: Success");
   }
   public void testInterrupt() throws Exception {
       AccessibilityManager.getInstance(this).interrupt();
       assert(INTERRUPT_INVOCATION_TYPE == mReceivedResult);
       Log.i(LOG_TAG, "AccessibilityTestService#testInterrupt: Success");
   }
}
