public class CalendarActivity extends Activity implements Navigator {
    private static final long INITIAL_HEAP_SIZE = 4*1024*1024;
    private static final long ANIMATION_DURATION = 400;
    protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";
    private ContentResolver mContentResolver;
    protected ProgressBar mProgressBar;
    protected ViewSwitcher mViewSwitcher;
    protected Animation mInAnimationForward;
    protected Animation mOutAnimationForward;
    protected Animation mInAnimationBackward;
    protected Animation mOutAnimationBackward;
    EventLoader mEventLoader;
    Time mSelectedDay = new Time();
     GestureDetector mGestureDetector;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_CHANGED)
                    || action.equals(Intent.ACTION_DATE_CHANGED)
                    || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                eventsChanged();
            }
        }
    };
    private ContentObserver mObserver = new ContentObserver(new Handler())
    {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            eventsChanged();
        }
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        VMRuntime.getRuntime().setMinimumHeapSize(INITIAL_HEAP_SIZE);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        mContentResolver = getContentResolver();
        mInAnimationForward = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        mOutAnimationForward = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        mInAnimationBackward = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        mOutAnimationBackward = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        mGestureDetector = new GestureDetector(this, new CalendarGestureListener());
        mEventLoader = new EventLoader(this);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        Time time = new Time();
        time.set(savedInstanceState.getLong(BUNDLE_KEY_RESTORE_TIME));
        view.setSelectedDay(time);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        long timeMillis = Utils.timeFromIntentInMillis(intent);
        if (timeMillis > 0) {
            Time time = new Time();
            time.set(timeMillis);
            goTo(time, false);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mEventLoader.startBackgroundThread();
        eventsChanged();
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        view.updateIs24HourFormat();
        view.restartCurrentTimeUpdates();
        view = (CalendarView) mViewSwitcher.getNextView();
        view.updateIs24HourFormat();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter);
        mContentResolver.registerContentObserver(Calendar.Events.CONTENT_URI,
                true, mObserver);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(BUNDLE_KEY_RESTORE_TIME, getSelectedTimeInMillis());
    }
    @Override
    protected void onPause() {
        super.onPause();
        mContentResolver.unregisterContentObserver(mObserver);
        unregisterReceiver(mIntentReceiver);
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        view.cleanup();
        view = (CalendarView) mViewSwitcher.getNextView();
        view.cleanup();
        mEventLoader.stopBackgroundThread();
    }
    void startProgressSpinner() {
        mProgressBar.setVisibility(View.VISIBLE);
    }
    void stopProgressSpinner() {
        mProgressBar.setVisibility(View.GONE);
    }
    public void goTo(Time time, boolean animate) {
        if (animate) {
            CalendarView current = (CalendarView) mViewSwitcher.getCurrentView();
            if (current.getSelectedTime().before(time)) {
                mViewSwitcher.setInAnimation(mInAnimationForward);
                mViewSwitcher.setOutAnimation(mOutAnimationForward);
            } else {
                mViewSwitcher.setInAnimation(mInAnimationBackward);
                mViewSwitcher.setOutAnimation(mOutAnimationBackward);
            }
        }
        CalendarView next = (CalendarView) mViewSwitcher.getNextView();
        next.setSelectedDay(time);
        next.reloadEvents();
        mViewSwitcher.showNext();
        next.requestFocus();
    }
    public long getSelectedTimeInMillis() {
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        return view.getSelectedTimeInMillis();
    }
    public long getSelectedTime() {
        return getSelectedTimeInMillis();
    }
    public void goToToday() {
        mSelectedDay.set(System.currentTimeMillis());
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        view.setSelectedDay(mSelectedDay);
        view.reloadEvents();
    }
    public boolean getAllDay() {
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        return view.mSelectionAllDay;
    }
    void eventsChanged() {
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        view.clearCachedEvents();
        view.reloadEvents();
    }
    Event getSelectedEvent() {
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        return view.getSelectedEvent();
    }
    boolean isEventSelected() {
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        return view.isEventSelected();
    }
    Event getNewEvent() {
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        return view.getNewEvent();
    }
    public CalendarView getNextView() {
        return (CalendarView) mViewSwitcher.getNextView();
    }
    public View switchViews(boolean forward, float xOffSet, float width) {
        float progress = Math.abs(xOffSet) / width;
        if (progress > 1.0f) {
            progress = 1.0f;
        }
        float inFromXValue, inToXValue;
        float outFromXValue, outToXValue;
        if (forward) {
            inFromXValue = 1.0f - progress;
            inToXValue = 0.0f;
            outFromXValue = -progress;
            outToXValue = -1.0f;
        } else {
            inFromXValue = progress - 1.0f;
            inToXValue = 0.0f;
            outFromXValue = progress;
            outToXValue = 1.0f;
        }
        TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);
        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue,
                Animation.ABSOLUTE, 0.0f,
                Animation.ABSOLUTE, 0.0f);
        long duration = (long) (ANIMATION_DURATION * (1.0f - progress));
        inAnimation.setDuration(duration);
        outAnimation.setDuration(duration);
        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        view.cleanup();
        mViewSwitcher.showNext();
        view = (CalendarView) mViewSwitcher.getCurrentView();
        view.requestFocus();
        view.reloadEvents();
        return view;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuHelper.onPrepareOptionsMenu(this, menu);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (! MenuHelper.onCreateOptionsMenu(menu)) {
            return false;
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MenuHelper.onOptionsItemSelected(this, item, this)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.onTouchEvent(ev);
    }
    class CalendarGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
            view.doSingleTapUp(ev);
            return true;
        }
        @Override
        public void onLongPress(MotionEvent ev) {
            CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
            view.doLongPress(ev);
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
            view.doScroll(e1, e2, distanceX, distanceY);
            return true;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
            view.doFling(e1, e2, velocityX, velocityY);
            return true;
        }
        @Override
        public boolean onDown(MotionEvent ev) {
            CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
            view.doDown(ev);
            return true;
        }
    }
}
