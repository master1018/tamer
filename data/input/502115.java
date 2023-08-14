public class MonthView extends View implements View.OnCreateContextMenuListener {
    private static final boolean PROFILE_LOAD_TIME = false;
    private static float mScale = 0; 
    private static int WEEK_GAP = 0;
    private static int MONTH_DAY_GAP = 1;
    private static float HOUR_GAP = 0f;
    private static float MIN_EVENT_HEIGHT = 4f;
    private static int MONTH_DAY_TEXT_SIZE = 20;
    private static int WEEK_BANNER_HEIGHT = 17;
    private static int WEEK_TEXT_SIZE = 15;
    private static int WEEK_TEXT_PADDING = 3;
    private static int EVENT_DOT_TOP_MARGIN = 5;
    private static int EVENT_DOT_LEFT_MARGIN = 7;
    private static int EVENT_DOT_W_H = 10;
    private static int EVENT_NUM_DAYS = 31;
    private static int TEXT_TOP_MARGIN = 7;
    private static int BUSY_BITS_WIDTH = 6;
    private static int BUSY_BITS_MARGIN = 4;
    private static int DAY_NUMBER_OFFSET = 10;
    private static int HORIZONTAL_FLING_THRESHOLD = 50;
    private int mCellHeight;
    private int mBorder;
    private boolean mLaunchDayView;
    private GestureDetector mGestureDetector;
    private String mDetailedView = CalendarPreferenceActivity.DEFAULT_DETAILED_VIEW;
    private Time mToday;
    private Time mViewCalendar;
    private Time mSavedTime = new Time();   
    private Time mOtherViewCalendar = new Time();
    private Time mTempTime = new Time();
    private DayOfMonthCursor mCursor;
    private Drawable mBoxSelected;
    private Drawable mBoxPressed;
    private Drawable mBoxLongPressed;
    private Drawable mEventDot;
    private int mCellWidth;
    private Resources mResources;
    private MonthActivity mParentActivity;
    private Navigator mNavigator;
    private final EventGeometry mEventGeometry;
    private Rect mRect = new Rect();
    private boolean[] eventDay = new boolean[31];
    private PopupWindow mPopup;
    private View mPopupView;
    private static final int POPUP_HEIGHT = 100;
    private int mPreviousPopupHeight;
    private static final int POPUP_DISMISS_DELAY = 3000;
    private DismissPopup mDismissPopup = new DismissPopup();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private boolean mRedrawScreen = true;
    private Rect mBitmapRect = new Rect();
    private RectF mRectF = new RectF();
    private boolean mAnimating;
    private boolean mShowWeekNumbers = false;
    private boolean mShowToast = false;
    private SparseArray<Bitmap> mDayBitmapCache = new SparseArray<Bitmap>(4);
    private ContextMenuHandler mContextMenuHandler = new ContextMenuHandler();
    private static final int SELECTION_HIDDEN = 0;
    private static final int SELECTION_PRESSED = 1;
    private static final int SELECTION_SELECTED = 2;
    private static final int SELECTION_LONGPRESS = 3;
    private static final int MODULO_SHIFT = 16;
    private int mSelectionMode = SELECTION_HIDDEN;
    private int mFirstJulianDay;
    private int mStartDay;
    private final EventLoader mEventLoader;
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private Drawable mTodayBackground;
    private int mMonthOtherMonthColor;
    private int mMonthWeekBannerColor;
    private int mMonthOtherMonthBannerColor;
    private int mMonthOtherMonthDayNumberColor;
    private int mMonthDayNumberColor;
    private int mMonthTodayNumberColor;
    private int mMonthSaturdayColor;
    private int mMonthSundayColor;
    private int mBusybitsColor;
    private int mMonthBgColor;
    public MonthView(MonthActivity activity, Navigator navigator) {
        super(activity);
        if (mScale == 0) {
            mScale = getContext().getResources().getDisplayMetrics().density;
           if (mScale != 1) {
                    WEEK_GAP *= mScale;
                    MONTH_DAY_GAP *= mScale;
                    HOUR_GAP *= mScale;
                    MONTH_DAY_TEXT_SIZE *= mScale;
                    WEEK_BANNER_HEIGHT *= mScale;
                    WEEK_TEXT_SIZE *= mScale;
                    WEEK_TEXT_PADDING *= mScale;
                    EVENT_DOT_TOP_MARGIN *= mScale;
                    EVENT_DOT_LEFT_MARGIN *= mScale;
                    EVENT_DOT_W_H *= mScale;
                    TEXT_TOP_MARGIN *= mScale;
                    HORIZONTAL_FLING_THRESHOLD *= mScale;
                    MIN_EVENT_HEIGHT *= mScale;
                    BUSY_BITS_WIDTH *= mScale;
                    BUSY_BITS_MARGIN *= mScale;
                    DAY_NUMBER_OFFSET *= mScale;
                }
            }
        mEventLoader = activity.mEventLoader;
        mNavigator = navigator;
        mEventGeometry = new EventGeometry();
        mEventGeometry.setMinEventHeight(MIN_EVENT_HEIGHT);
        mEventGeometry.setHourGap(HOUR_GAP);
        init(activity);
    }
    private void init(MonthActivity activity) {
        setFocusable(true);
        setClickable(true);
        setOnCreateContextMenuListener(this);
        mParentActivity = activity;
        mViewCalendar = new Time();
        long now = System.currentTimeMillis();
        mViewCalendar.set(now);
        mViewCalendar.monthDay = 1;
        long millis = mViewCalendar.normalize(true );
        mFirstJulianDay = Time.getJulianDay(millis, mViewCalendar.gmtoff);
        mStartDay = Utils.getFirstDayOfWeek();
        mViewCalendar.set(now);
        mCursor = new DayOfMonthCursor(mViewCalendar.year,  mViewCalendar.month,
                mViewCalendar.monthDay, mParentActivity.getStartDay());
        mToday = new Time();
        mToday.set(System.currentTimeMillis());
        mResources = activity.getResources();
        mBoxSelected = mResources.getDrawable(R.drawable.month_view_selected);
        mBoxPressed = mResources.getDrawable(R.drawable.month_view_pressed);
        mBoxLongPressed = mResources.getDrawable(R.drawable.month_view_longpress);
        mEventDot = mResources.getDrawable(R.drawable.event_dot);
        mTodayBackground = mResources.getDrawable(R.drawable.month_view_today_background);
        Resources res = getResources();
        mMonthOtherMonthColor = res.getColor(R.color.month_other_month);
        mMonthWeekBannerColor = res.getColor(R.color.month_week_banner);
        mMonthOtherMonthBannerColor = res.getColor(R.color.month_other_month_banner);
        mMonthOtherMonthDayNumberColor = res.getColor(R.color.month_other_month_day_number);
        mMonthDayNumberColor = res.getColor(R.color.month_day_number);
        mMonthTodayNumberColor = res.getColor(R.color.month_today_number);
        mMonthSaturdayColor = res.getColor(R.color.month_saturday);
        mMonthSundayColor = res.getColor(R.color.month_sunday);
        mBusybitsColor = res.getColor(R.color.month_busybits);
        mMonthBgColor = res.getColor(R.color.month_bgcolor);
        if (mShowToast) {
            LayoutInflater inflater;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPopupView = inflater.inflate(R.layout.month_bubble, null);
            mPopup = new PopupWindow(activity);
            mPopup.setContentView(mPopupView);
            Resources.Theme dialogTheme = getResources().newTheme();
            dialogTheme.applyStyle(android.R.style.Theme_Dialog, true);
            TypedArray ta = dialogTheme.obtainStyledAttributes(new int[] {
                android.R.attr.windowBackground });
            mPopup.setBackgroundDrawable(ta.getDrawable(0));
            ta.recycle();
        }
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                    float velocityY) {
                mLaunchDayView = false;
                mSelectionMode = SELECTION_HIDDEN;
                int distanceX = Math.abs((int) e2.getX() - (int) e1.getX());
                int distanceY = Math.abs((int) e2.getY() - (int) e1.getY());
                if (distanceY < HORIZONTAL_FLING_THRESHOLD || distanceY < distanceX) {
                    return false;
                }
                Time time = mOtherViewCalendar;
                time.set(mViewCalendar);
                if (velocityY < 0) {
                    time.month += 1;
                } else {
                    time.month -= 1;
                }
                time.normalize(true);
                mParentActivity.goTo(time, true);
                return true;
            }
            @Override
            public boolean onDown(MotionEvent e) {
                mLaunchDayView = true;
                return true;
            }
            public void setSelectedCell(MotionEvent e) {
                int x = (int) e.getX();
                int y = (int) e.getY();
                int row = (y - WEEK_GAP) / (WEEK_GAP + mCellHeight);
                int col = (x - mBorder) / (MONTH_DAY_GAP + mCellWidth);
                if (row > 5) {
                    row = 5;
                }
                if (col > 6) {
                    col = 6;
                }
                mCursor.setSelectedRowColumn(row, col);
            }
            @Override
            public void onShowPress(MotionEvent e) {
                setSelectedCell(e);
                mSelectionMode = SELECTION_PRESSED;
                mRedrawScreen = true;
                invalidate();
            }
            @Override
            public void onLongPress(MotionEvent e) {
                if (mLaunchDayView) {
                    mLaunchDayView = false;
                    mSelectionMode = SELECTION_LONGPRESS;
                    mRedrawScreen = true;
                    invalidate();
                    performLongClick();
                }
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                mLaunchDayView = false;
                if (mSelectionMode != SELECTION_HIDDEN) {
                    mSelectionMode = SELECTION_HIDDEN;
                    mRedrawScreen = true;
                    invalidate();
                }
                return true;
            }
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mLaunchDayView) {
                    setSelectedCell(e);
                    mSelectionMode = SELECTION_SELECTED;
                    mRedrawScreen = true;
                    invalidate();
                    mLaunchDayView = false;
                    int x = (int) e.getX();
                    int y = (int) e.getY();
                    long millis = getSelectedMillisFor(x, y);
                    Utils.startActivity(getContext(), mDetailedView, millis);
                }
                return true;
            }
        });
    }
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        MenuItem item;
        final long startMillis = getSelectedTimeInMillis();
        final int flags = DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_ABBREV_MONTH;
        final String title = DateUtils.formatDateTime(mParentActivity, startMillis, flags);
        menu.setHeaderTitle(title);
        item = menu.add(0, MenuHelper.MENU_DAY, 0, R.string.show_day_view);
        item.setOnMenuItemClickListener(mContextMenuHandler);
        item.setIcon(android.R.drawable.ic_menu_day);
        item.setAlphabeticShortcut('d');
        item = menu.add(0, MenuHelper.MENU_AGENDA, 0, R.string.show_agenda_view);
        item.setOnMenuItemClickListener(mContextMenuHandler);
        item.setIcon(android.R.drawable.ic_menu_agenda);
        item.setAlphabeticShortcut('a');
        item = menu.add(0, MenuHelper.MENU_EVENT_CREATE, 0, R.string.event_create);
        item.setOnMenuItemClickListener(mContextMenuHandler);
        item.setIcon(android.R.drawable.ic_menu_add);
        item.setAlphabeticShortcut('n');
    }
    private class ContextMenuHandler implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MenuHelper.MENU_DAY: {
                    long startMillis = getSelectedTimeInMillis();
                    Utils.startActivity(mParentActivity, DayActivity.class.getName(), startMillis);
                    break;
                }
                case MenuHelper.MENU_AGENDA: {
                    long startMillis = getSelectedTimeInMillis();
                    Utils.startActivity(mParentActivity, AgendaActivity.class.getName(), startMillis);
                    break;
                }
                case MenuHelper.MENU_EVENT_CREATE: {
                    long startMillis = getSelectedTimeInMillis();
                    long endMillis = startMillis + DateUtils.HOUR_IN_MILLIS;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(mParentActivity, EditEvent.class.getName());
                    intent.putExtra(EVENT_BEGIN_TIME, startMillis);
                    intent.putExtra(EVENT_END_TIME, endMillis);
                    mParentActivity.startActivity(intent);
                    break;
                }
                default: {
                    return false;
                }
            }
            return true;
        }
    }
    void reloadEvents() {
        Time monthStart = mTempTime;
        monthStart.set(mViewCalendar);
        monthStart.monthDay = 1;
        monthStart.hour = 0;
        monthStart.minute = 0;
        monthStart.second = 0;
        long millis = monthStart.normalize(true );
        int startDay = Time.getJulianDay(millis, monthStart.gmtoff);
        mParentActivity.startProgressSpinner();
        final long startMillis;
        if (PROFILE_LOAD_TIME) {
            startMillis = SystemClock.uptimeMillis();
        } else {
            startMillis = 0;
        }
        final ArrayList<Event> events = new ArrayList<Event>();
        mEventLoader.loadEventsInBackground(EVENT_NUM_DAYS, events, millis, new Runnable() {
            public void run() {
                mEvents = events;
                mRedrawScreen = true;
                mParentActivity.stopProgressSpinner();
                invalidate();
                int numEvents = events.size();
                for (int i = 0; i < EVENT_NUM_DAYS; i++) {
                    eventDay[i] = false;
                }
                for (int i = 0; i < numEvents; i++) {
                    Event event = events.get(i);
                    int startDay = event.startDay - mFirstJulianDay;
                    int endDay = event.endDay - mFirstJulianDay + 1;
                    if (startDay < 31 || endDay >= 0) {
                        if (startDay < 0) {
                            startDay = 0;
                        }
                        if (startDay > 31) {
                            startDay = 31;
                        }
                        if (endDay < 0) {
                            endDay = 0;
                        }
                        if (endDay > 31) {
                            endDay = 31;
                        }
                        for (int j = startDay; j < endDay; j++) {
                            eventDay[j] = true;
                        }
                    }
                }
            }
        }, null);
    }
    void animationStarted() {
        mAnimating = true;
    }
    void animationFinished() {
        mAnimating = false;
        mRedrawScreen = true;
        invalidate();
    }
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        drawingCalc(width, height);
        clearBitmapCache();
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearBitmapCache();
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mRedrawScreen) {
            if (mCanvas == null) {
                drawingCalc(getWidth(), getHeight());
            }
            if (mCanvas != null) {
                final Canvas bitmapCanvas = mCanvas;
                bitmapCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                doDraw(bitmapCanvas);
                mRedrawScreen = false;
            }
        }
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mBitmapRect, mBitmapRect, null);
        }
    }
    private void doDraw(Canvas canvas) {
        boolean isLandscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        Paint p = new Paint();
        Rect r = mRect;
        int columnDay1 = mCursor.getColumnOf(1);
        int day = mFirstJulianDay - columnDay1;
        int weekNum = 0;
        Calendar calendar = null;
        if (mShowWeekNumbers) {
            calendar = Calendar.getInstance();
            boolean noPrevMonth = (columnDay1 == 0);
            weekNum = getWeekOfYear(0, 0, noPrevMonth, calendar);
        }
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 7; column++) {
                drawBox(day, weekNum, row, column, canvas, p, r, isLandscape);
                day += 1;
            }
            if (mShowWeekNumbers) {
                weekNum += 1;
                if (weekNum >= 53) {
                    boolean inCurrentMonth = (day - mFirstJulianDay < 31);
                    weekNum = getWeekOfYear(row + 1, 0, inCurrentMonth, calendar);
                }
            }
        }
        drawGrid(canvas, p);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }
    private long getSelectedMillisFor(int x, int y) {
        int row = (y - WEEK_GAP) / (WEEK_GAP + mCellHeight);
        int column = (x - mBorder) / (MONTH_DAY_GAP + mCellWidth);
        if (column > 6) {
            column = 6;
        }
        DayOfMonthCursor c = mCursor;
        Time time = mTempTime;
        time.set(mViewCalendar);
        time.monthDay = 7 * row + column - c.getOffset() + 1;
        return time.normalize(true);
    }
    private Bitmap createBitmap(Drawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, mBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }
    private void clearBitmapCache() {
        recycleAndClearBitmapCache(mDayBitmapCache);
    }
    private void recycleAndClearBitmapCache(SparseArray<Bitmap> bitmapCache) {
        int size = bitmapCache.size();
        for(int i = 0; i < size; i++) {
            bitmapCache.valueAt(i).recycle();
        }
        bitmapCache.clear();
    }
    private void drawGrid(Canvas canvas, Paint p) {
        p.setColor(mMonthOtherMonthColor);
        p.setAntiAlias(false);
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        for (int row = 0; row < 6; row++) {
            int y = WEEK_GAP + row * (WEEK_GAP + mCellHeight) - 1;
            canvas.drawLine(0, y, width, y, p);
        }
        for (int column = 1; column < 7; column++) {
            int x = mBorder + column * (MONTH_DAY_GAP + mCellWidth) - 1;
            canvas.drawLine(x, WEEK_GAP, x, height, p);
        }
    }
    private void drawBox(int day, int weekNum, int row, int column, Canvas canvas, Paint p,
            Rect r, boolean isLandscape) {
        boolean drawSelection = false;
        if (mSelectionMode != SELECTION_HIDDEN) {
            drawSelection = mCursor.isSelected(row, column);
        }
        boolean withinCurrentMonth = mCursor.isWithinCurrentMonth(row, column);
        boolean isToday = false;
        int dayOfBox = mCursor.getDayAt(row, column);
        if (dayOfBox == mToday.monthDay && mCursor.getYear() == mToday.year
                && mCursor.getMonth() == mToday.month) {
            isToday = true;
        }
        int y = WEEK_GAP + row*(WEEK_GAP + mCellHeight);
        int x = mBorder + column*(MONTH_DAY_GAP + mCellWidth);
        r.left = x;
        r.top = y;
        r.right = x + mCellWidth;
        r.bottom = y + mCellHeight;
        if (column == 0) {
            r.left = -1;
        } else if (column == 6) {
            r.right += mBorder + 2;
        }
        if (row == 5) {
            r.bottom = getMeasuredHeight();
        }
        if (!withinCurrentMonth) {
            boolean firstDayOfNextmonth = isFirstDayOfNextMonth(row, column);
            r.top--;
            if (column != 0) {
                r.left--;
            }
            p.setStyle(Style.FILL);
            p.setColor(mMonthBgColor);
            canvas.drawRect(r, p);
        } else if (drawSelection) {
            if (mSelectionMode == SELECTION_SELECTED) {
                mBoxSelected.setBounds(r);
                mBoxSelected.draw(canvas);
            } else if (mSelectionMode == SELECTION_PRESSED) {
                mBoxPressed.setBounds(r);
                mBoxPressed.draw(canvas);
            } else {
                mBoxLongPressed.setBounds(r);
                mBoxLongPressed.draw(canvas);
            }
            drawEvents(day, canvas, r, p, false );
            if (!mAnimating) {
                updateEventDetails(day);
            }
        } else {
            if (isToday) {
                Drawable background = mTodayBackground;
                background.setBounds(r);
                background.draw(canvas);
            }
            drawEvents(day, canvas, r, p, !isToday );
        }
        if (mShowWeekNumbers && column == 0) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(mMonthWeekBannerColor);
            if (isLandscape) {
                int bottom = r.bottom;
                r.bottom = r.top + WEEK_BANNER_HEIGHT;
                r.left++;
                canvas.drawRect(r, p);
                r.bottom = bottom;
                r.left--;
            } else {
                int top = r.top;
                r.top = r.bottom - WEEK_BANNER_HEIGHT;
                r.left++;
                canvas.drawRect(r, p);
                r.top = top;
                r.left--;
            }
            p.setColor(mMonthOtherMonthBannerColor);
            p.setAntiAlias(true);
            p.setTypeface(null);
            p.setTextSize(WEEK_TEXT_SIZE);
            p.setTextAlign(Paint.Align.LEFT);
            int textX = r.left + WEEK_TEXT_PADDING;
            int textY;
            if (isLandscape) {
                textY = r.top + WEEK_BANNER_HEIGHT - WEEK_TEXT_PADDING;
            } else {
                textY = r.bottom - WEEK_TEXT_PADDING;
            }
            canvas.drawText(String.valueOf(weekNum), textX, textY, p);
        }
        p.setStyle(Paint.Style.FILL);
        p.setAntiAlias(true);
        p.setTypeface(null);
        p.setTextSize(MONTH_DAY_TEXT_SIZE);
        if (!withinCurrentMonth) {
            p.setColor(mMonthOtherMonthDayNumberColor);
        } else {
            if (isToday && !drawSelection) {
                p.setColor(mMonthTodayNumberColor);
            } else if (Utils.isSunday(column, mStartDay)) {
                p.setColor(mMonthSundayColor);
            } else if (Utils.isSaturday(column, mStartDay)) {
                p.setColor(mMonthSaturdayColor);
            } else {
                p.setColor(mMonthDayNumberColor);
            }
            p.setFakeBoldText(eventDay[day-mFirstJulianDay]);
        }
        p.setTextAlign(Paint.Align.CENTER);
        int textX = r.left + (r.right - BUSY_BITS_MARGIN - BUSY_BITS_WIDTH - r.left) / 2;
        int textY = (int) (r.top + p.getTextSize() + TEXT_TOP_MARGIN); 
        canvas.drawText(String.valueOf(mCursor.getDayAt(row, column)), textX, textY, p);
    }
    private void drawEvents(int date, Canvas canvas, Rect rect, Paint p, boolean drawBg) {
        int top = rect.top + TEXT_TOP_MARGIN + BUSY_BITS_MARGIN;
        int left = rect.right - BUSY_BITS_MARGIN - BUSY_BITS_WIDTH;
        Style oldStyle = p.getStyle();
        int oldColor = p.getColor();
        ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        EventGeometry geometry = mEventGeometry;
        if (drawBg) {
            RectF rf = mRectF;
            rf.left = left;
            rf.right = left + BUSY_BITS_WIDTH;
            rf.bottom = rect.bottom - BUSY_BITS_MARGIN;
            rf.top = top;
            p.setColor(mMonthBgColor);
            p.setStyle(Style.FILL);
            canvas.drawRect(rf, p);
        }
        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);
            if (!geometry.computeEventRect(date, left, top, BUSY_BITS_WIDTH, event)) {
                continue;
            }
            drawEventRect(rect, event, canvas, p);
        }
    }
    private RectF drawEventRect(Rect rect, Event event, Canvas canvas, Paint p) {
        p.setColor(mBusybitsColor);
        int left = rect.right - BUSY_BITS_MARGIN - BUSY_BITS_WIDTH;
        int bottom = rect.bottom - BUSY_BITS_MARGIN;
        RectF rf = mRectF;
        rf.top = event.top;
        rf.bottom = Math.min(event.bottom, bottom);
        rf.left = left;
        rf.right = left + BUSY_BITS_WIDTH;
        canvas.drawRect(rf, p);
        return rf;
    }
    private boolean isFirstDayOfNextMonth(int row, int column) {
        if (column == 0) {
            column = 6;
            row--;
        } else {
            column--;
        }
        return mCursor.isWithinCurrentMonth(row, column);
    }
    private int getWeekOfYear(int row, int column, boolean isWithinCurrentMonth,
            Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, mCursor.getDayAt(row, column));
        if (isWithinCurrentMonth) {
            calendar.set(Calendar.MONTH, mCursor.getMonth());
            calendar.set(Calendar.YEAR, mCursor.getYear());
        } else {
            int month = mCursor.getMonth();
            int year = mCursor.getYear();
            if (row < 2) {
                if (month == 0) {
                    year--;
                    month = 11;
                } else {
                    month--;
                }
            } else {
                if (month == 11) {
                    year++;
                    month = 0;
                } else {
                    month++;
                }
            }
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);
        }
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
    void setDetailedView(String detailedView) {
        mDetailedView = detailedView;
    }
    void setSelectedTime(Time time) {
        mSavedTime.set(time);
        mViewCalendar.set(time);
        mViewCalendar.monthDay = 1;
        long millis = mViewCalendar.normalize(true );
        mFirstJulianDay = Time.getJulianDay(millis, mViewCalendar.gmtoff);
        mViewCalendar.set(time);
        mCursor = new DayOfMonthCursor(time.year, time.month, time.monthDay,
                mCursor.getWeekStartDay());
        mRedrawScreen = true;
        invalidate();
    }
    public long getSelectedTimeInMillis() {
        Time time = mTempTime;
        time.set(mViewCalendar);
        time.month += mCursor.getSelectedMonthOffset();
        time.monthDay = mCursor.getSelectedDayOfMonth();
        time.second = mSavedTime.second;
        time.minute = mSavedTime.minute;
        time.hour = mSavedTime.hour;
        return time.normalize(true);
    }
    Time getTime() {
        return mViewCalendar;
    }
    public int getSelectionMode() {
        return mSelectionMode;
    }
    public void setSelectionMode(int selectionMode) {
        mSelectionMode = selectionMode;
    }
    private void drawingCalc(int width, int height) {
        mCellHeight = (height - (6 * WEEK_GAP)) / 6;
        mEventGeometry
                .setHourHeight((mCellHeight - BUSY_BITS_MARGIN * 2 - TEXT_TOP_MARGIN) / 24.0f);
        mCellWidth = (width - (6 * MONTH_DAY_GAP)) / 7;
        mBorder = (width - 6 * (mCellWidth + MONTH_DAY_GAP) - mCellWidth) / 2;
        if (mShowToast) {
            mPopup.dismiss();
            mPopup.setWidth(width - 20);
            mPopup.setHeight(POPUP_HEIGHT);
        }
        if (((mBitmap == null)
                    || mBitmap.isRecycled()
                    || (mBitmap.getHeight() != height)
                    || (mBitmap.getWidth() != width))
                && (width > 0) && (height > 0)) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        mBitmapRect.top = 0;
        mBitmapRect.bottom = height;
        mBitmapRect.left = 0;
        mBitmapRect.right = width;
    }
    private void updateEventDetails(int date) {
        if (!mShowToast) {
            return;
        }
        getHandler().removeCallbacks(mDismissPopup);
        ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        if (numEvents == 0) {
            mPopup.dismiss();
            return;
        }
        int eventIndex = 0;
        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);
            if (event.startDay > date || event.endDay < date) {
                continue;
            }
            if (eventIndex >= 4) {
                eventIndex += 1;
                continue;
            }
            int flags;
            boolean showEndTime = false;
            if (event.allDay) {
                int numDays = event.endDay - event.startDay;
                if (numDays == 0) {
                    flags = DateUtils.FORMAT_UTC | DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL;
                } else {
                    showEndTime = true;
                    flags = DateUtils.FORMAT_UTC | DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_ABBREV_ALL;
                }
            } else {
                flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
                if (DateFormat.is24HourFormat(mParentActivity)) {
                    flags |= DateUtils.FORMAT_24HOUR;
                }
            }
            String timeRange;
            if (showEndTime) {
                timeRange = DateUtils.formatDateRange(mParentActivity,
                        event.startMillis, event.endMillis, flags);
            } else {
                timeRange = DateUtils.formatDateRange(mParentActivity,
                        event.startMillis, event.startMillis, flags);
            }
            TextView timeView = null;
            TextView titleView = null;
            switch (eventIndex) {
                case 0:
                    timeView = (TextView) mPopupView.findViewById(R.id.time0);
                    titleView = (TextView) mPopupView.findViewById(R.id.event_title0);
                    break;
                case 1:
                    timeView = (TextView) mPopupView.findViewById(R.id.time1);
                    titleView = (TextView) mPopupView.findViewById(R.id.event_title1);
                    break;
                case 2:
                    timeView = (TextView) mPopupView.findViewById(R.id.time2);
                    titleView = (TextView) mPopupView.findViewById(R.id.event_title2);
                    break;
                case 3:
                    timeView = (TextView) mPopupView.findViewById(R.id.time3);
                    titleView = (TextView) mPopupView.findViewById(R.id.event_title3);
                    break;
            }
            timeView.setText(timeRange);
            titleView.setText(event.title);
            eventIndex += 1;
        }
        if (eventIndex == 0) {
            mPopup.dismiss();
            return;
        }
        View view;
        switch (eventIndex) {
            case 1:
                view = mPopupView.findViewById(R.id.item_layout1);
                view.setVisibility(View.GONE);
                view = mPopupView.findViewById(R.id.item_layout2);
                view.setVisibility(View.GONE);
                view = mPopupView.findViewById(R.id.item_layout3);
                view.setVisibility(View.GONE);
                view = mPopupView.findViewById(R.id.plus_more);
                view.setVisibility(View.GONE);
                break;
            case 2:
                view = mPopupView.findViewById(R.id.item_layout1);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout2);
                view.setVisibility(View.GONE);
                view = mPopupView.findViewById(R.id.item_layout3);
                view.setVisibility(View.GONE);
                view = mPopupView.findViewById(R.id.plus_more);
                view.setVisibility(View.GONE);
                break;
            case 3:
                view = mPopupView.findViewById(R.id.item_layout1);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout2);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout3);
                view.setVisibility(View.GONE);
                view = mPopupView.findViewById(R.id.plus_more);
                view.setVisibility(View.GONE);
                break;
            case 4:
                view = mPopupView.findViewById(R.id.item_layout1);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout2);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout3);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.plus_more);
                view.setVisibility(View.GONE);
                break;
            default:
                view = mPopupView.findViewById(R.id.item_layout1);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout2);
                view.setVisibility(View.VISIBLE);
                view = mPopupView.findViewById(R.id.item_layout3);
                view.setVisibility(View.VISIBLE);
                TextView tv = (TextView) mPopupView.findViewById(R.id.plus_more);
                tv.setVisibility(View.VISIBLE);
                String format = mResources.getString(R.string.plus_N_more);
                String plusMore = String.format(format, eventIndex - 4);
                tv.setText(plusMore);
                break;
        }
        if (eventIndex > 5) {
            eventIndex = 5;
        }
        int popupHeight = 20 * eventIndex + 15;
        mPopup.setHeight(popupHeight);
        if (mPreviousPopupHeight != popupHeight) {
            mPreviousPopupHeight = popupHeight;
            mPopup.dismiss();
        }
        mPopup.showAtLocation(this, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
        postDelayed(mDismissPopup, POPUP_DISMISS_DELAY);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        long duration = event.getEventTime() - event.getDownTime();
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_CENTER:
            if (mSelectionMode == SELECTION_HIDDEN) {
                break;
            }
            if (mSelectionMode == SELECTION_PRESSED) {
                mSelectionMode = SELECTION_SELECTED;
                mRedrawScreen = true;
                invalidate();
                break;
            }
            if (duration < ViewConfiguration.getLongPressTimeout()) {
                long millis = getSelectedTimeInMillis();
                Utils.startActivity(getContext(), mDetailedView, millis);
            } else {
                mSelectionMode = SELECTION_LONGPRESS;
                mRedrawScreen = true;
                invalidate();
                performLongClick();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mSelectionMode == SELECTION_HIDDEN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP
                    || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                mSelectionMode = SELECTION_SELECTED;
                mRedrawScreen = true;
                invalidate();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                mSelectionMode = SELECTION_PRESSED;
                mRedrawScreen = true;
                invalidate();
                return true;
            }
        }
        mSelectionMode = SELECTION_SELECTED;
        boolean redraw = false;
        Time other = null;
        switch (keyCode) {
        case KeyEvent.KEYCODE_ENTER:
            long millis = getSelectedTimeInMillis();
            Utils.startActivity(getContext(), mDetailedView, millis);
            return true;
        case KeyEvent.KEYCODE_DPAD_UP:
            if (mCursor.up()) {
                other = mOtherViewCalendar;
                other.set(mViewCalendar);
                other.month -= 1;
                other.monthDay = mCursor.getSelectedDayOfMonth();
                mCursor.down();
            }
            redraw = true;
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            if (mCursor.down()) {
                other = mOtherViewCalendar;
                other.set(mViewCalendar);
                other.month += 1;
                other.monthDay = mCursor.getSelectedDayOfMonth();
                mCursor.up();
            }
            redraw = true;
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (mCursor.left()) {
                other = mOtherViewCalendar;
                other.set(mViewCalendar);
                other.month -= 1;
                other.monthDay = mCursor.getSelectedDayOfMonth();
                mCursor.right();
            }
            redraw = true;
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (mCursor.right()) {
                other = mOtherViewCalendar;
                other.set(mViewCalendar);
                other.month += 1;
                other.monthDay = mCursor.getSelectedDayOfMonth();
                mCursor.left();
            }
            redraw = true;
            break;
        }
        if (other != null) {
            other.normalize(true );
            mNavigator.goTo(other, true);
        } else if (redraw) {
            mRedrawScreen = true;
            invalidate();
        }
        return redraw;
    }
    class DismissPopup implements Runnable {
        public void run() {
            mPopup.dismiss();
        }
    }
    void dismissPopup() {
        if (!mShowToast) {
            return;
        }
        if (mPopup != null) {
            mPopup.dismiss();
        }
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacks(mDismissPopup);
        }
    }
}
