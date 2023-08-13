public class CalendarView extends View
        implements View.OnCreateContextMenuListener, View.OnClickListener {
    private static float mScale = 0; 
    private static final long INVALID_EVENT_ID = -1; 
    private boolean mOnFlingCalled;
    private long mLastPopupEventID;
    protected CalendarApplication mCalendarApp;
    protected CalendarActivity mParentActivity;
    private static final String[] CALENDARS_PROJECTION = new String[] {
        Calendars._ID,          
        Calendars.ACCESS_LEVEL, 
        Calendars.OWNER_ACCOUNT, 
    };
    private static final int CALENDARS_INDEX_ACCESS_LEVEL = 1;
    private static final int CALENDARS_INDEX_OWNER_ACCOUNT = 2;
    private static final String CALENDARS_WHERE = Calendars._ID + "=%d";
    private static final String[] ATTENDEES_PROJECTION = new String[] {
        Attendees._ID,                      
        Attendees.ATTENDEE_RELATIONSHIP,    
    };
    private static final int ATTENDEES_INDEX_RELATIONSHIP = 1;
    private static final String ATTENDEES_WHERE = Attendees.EVENT_ID + "=%d";
    private static float SMALL_ROUND_RADIUS = 3.0F;
    private static final int FROM_NONE = 0;
    private static final int FROM_ABOVE = 1;
    private static final int FROM_BELOW = 2;
    private static final int FROM_LEFT = 4;
    private static final int FROM_RIGHT = 8;
    private static final int ACCESS_LEVEL_NONE = 0;
    private static final int ACCESS_LEVEL_DELETE = 1;
    private static final int ACCESS_LEVEL_EDIT = 2;
    private static int HORIZONTAL_SCROLL_THRESHOLD = 50;
    private ContinueScroll mContinueScroll = new ContinueScroll();
    static private class DayHeader{
        int cell;
        String dateString;
    }
    private DayHeader[] dayHeaders = new DayHeader[32];
    Time mBaseDate;
    private Time mCurrentTime;
    private static final int UPDATE_CURRENT_TIME_DELAY = 300000;
    private UpdateCurrentTime mUpdateCurrentTime = new UpdateCurrentTime();
    private int mTodayJulianDay;
    private Typeface mBold = Typeface.DEFAULT_BOLD;
    private int mFirstJulianDay;
    private int mLastJulianDay;
    private int mMonthLength;
    private int mFirstDate;
    private int[] mEarliestStartHour;    
    private boolean[] mHasAllDayEvent;   
    private String mDetailedView = CalendarPreferenceActivity.DEFAULT_DETAILED_VIEW;
    private long mLastReloadMillis;
    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private int mSelectionDay;        
    private int mSelectionHour;
    boolean mSelectionAllDay;
    private int mCellWidth;
    private Rect mRect = new Rect();
    private RectF mRectF = new RectF();
    private Rect mSrcRect = new Rect();
    private Rect mDestRect = new Rect();
    private Paint mPaint = new Paint();
    private Paint mPaintBorder = new Paint();
    private Paint mEventTextPaint = new Paint();
    private Paint mSelectionPaint = new Paint();
    private Path mPath = new Path();
    protected boolean mDrawTextInEventRect;
    private int mStartDay;
    private PopupWindow mPopup;
    private View mPopupView;
    private static final int POPUP_DISMISS_DELAY = 3000;
    private DismissPopup mDismissPopup = new DismissPopup();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private boolean mRedrawScreen = true;
    private boolean mRemeasure = true;
    private final EventLoader mEventLoader;
    protected final EventGeometry mEventGeometry;
    private static final int DAY_GAP = 1;
    private static final int HOUR_GAP = 1;
    private static int SINGLE_ALLDAY_HEIGHT = 20;
    private static int MAX_ALLDAY_HEIGHT = 72;
    private static int ALLDAY_TOP_MARGIN = 3;
    private static int MAX_ALLDAY_EVENT_HEIGHT = 18;
    private static final int ALL_DAY_TEXT_TOP_MARGIN = 0;
    private static final int NORMAL_TEXT_TOP_MARGIN = 2;
    private static final int HOURS_LEFT_MARGIN = 2;
    private static final int HOURS_RIGHT_MARGIN = 4;
    private static final int HOURS_MARGIN = HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN;
    private static int CURRENT_TIME_LINE_HEIGHT = 2;
    private static int CURRENT_TIME_LINE_BORDER_WIDTH = 1;
    private static int CURRENT_TIME_MARKER_INNER_WIDTH = 6;
    private static int CURRENT_TIME_MARKER_HEIGHT = 6;
    private static int CURRENT_TIME_MARKER_WIDTH = 8;
    private static int CURRENT_TIME_LINE_SIDE_BUFFER = 1;
     static final int MINUTES_PER_HOUR = 60;
     static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * 24;
     static final int MILLIS_PER_MINUTE = 60 * 1000;
     static final int MILLIS_PER_HOUR = (3600 * 1000);
     static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * 24;
    private static int NORMAL_FONT_SIZE = 12;
    private static int EVENT_TEXT_FONT_SIZE = 12;
    private static int HOURS_FONT_SIZE = 12;
    private static int AMPM_FONT_SIZE = 9;
    private static int MIN_CELL_WIDTH_FOR_TEXT = 27;
    private static final int MAX_EVENT_TEXT_LEN = 500;
    private static float MIN_EVENT_HEIGHT = 15.0F;  
    private static int mSelectionColor;
    private static int mPressedColor;
    private static int mSelectedEventTextColor;
    private static int mEventTextColor;
    private static int mWeek_saturdayColor;
    private static int mWeek_sundayColor;
    private static int mCalendarDateBannerTextColor;
    private static int mCalendarAllDayBackground;
    private static int mCalendarAmPmLabel;
    private static int mCalendarDateBannerBackground;
    private static int mCalendarDateSelected;
    private static int mCalendarGridAreaBackground;
    private static int mCalendarGridAreaSelected;
    private static int mCalendarGridLineHorizontalColor;
    private static int mCalendarGridLineVerticalColor;
    private static int mCalendarHourBackground;
    private static int mCalendarHourLabel;
    private static int mCalendarHourSelected;
    private static int mCurrentTimeMarkerColor;
    private static int mCurrentTimeMarkerBorderColor;
    private int mViewStartX;
    private int mViewStartY;
    private int mMaxViewStartY;
    private int mBitmapHeight;
    private int mViewHeight;
    private int mViewWidth;
    private int mGridAreaHeight;
    private int mCellHeight;
    private int mScrollStartY;
    private int mPreviousDirection;
    private int mPreviousDistanceX;
    private int mHoursTextHeight;
    private int mEventTextAscent;
    private int mEventTextHeight;
    private int mAllDayHeight;
    private int mBannerPlusMargin;
    private int mMaxAllDayEvents;
    protected int mNumDays = 7;
    private int mNumHours = 10;
    private int mHoursWidth;
    private int mDateStrWidth;
    private int mFirstCell;
    private int mFirstHour = -1;
    private int mFirstHourOffset;
    private String[] mHourStrs;
    private String[] mDayStrs;
    private String[] mDayStrs2Letter;
    private boolean mIs24HourFormat;
    private float[] mCharWidths = new float[MAX_EVENT_TEXT_LEN];
    private ArrayList<Event> mSelectedEvents = new ArrayList<Event>();
    private boolean mComputeSelectedEvents;
    private Event mSelectedEvent;
    private Event mPrevSelectedEvent;
    private Rect mPrevBox = new Rect();
    protected final Resources mResources;
    private String mAmString;
    private String mPmString;
    private DeleteEventHelper mDeleteEventHelper;
    private ContextMenuHandler mContextMenuHandler = new ContextMenuHandler();
    private static final int TOUCH_MODE_INITIAL_STATE = 0;
    private static final int TOUCH_MODE_DOWN = 1;
    private static final int TOUCH_MODE_VSCROLL = 0x20;
    private static final int TOUCH_MODE_HSCROLL = 0x40;
    private int mTouchMode = TOUCH_MODE_INITIAL_STATE;
    private static final int SELECTION_HIDDEN = 0;
    private static final int SELECTION_PRESSED = 1;
    private static final int SELECTION_SELECTED = 2;
    private static final int SELECTION_LONGPRESS = 3;
    private int mSelectionMode = SELECTION_HIDDEN;
    private boolean mScrolling = false;
    private String mDateRange;
    private TextView mTitleTextView;
    public CalendarView(CalendarActivity activity) {
        super(activity);
        if (mScale == 0) {
            mScale = getContext().getResources().getDisplayMetrics().density;
            if (mScale != 1) {
                SINGLE_ALLDAY_HEIGHT *= mScale;
                MAX_ALLDAY_HEIGHT *= mScale;
                ALLDAY_TOP_MARGIN *= mScale;
                MAX_ALLDAY_EVENT_HEIGHT *= mScale;
                NORMAL_FONT_SIZE *= mScale;
                EVENT_TEXT_FONT_SIZE *= mScale;
                HOURS_FONT_SIZE *= mScale;
                AMPM_FONT_SIZE *= mScale;
                MIN_CELL_WIDTH_FOR_TEXT *= mScale;
                MIN_EVENT_HEIGHT *= mScale;
                HORIZONTAL_SCROLL_THRESHOLD *= mScale;
                CURRENT_TIME_MARKER_HEIGHT *= mScale;
                CURRENT_TIME_MARKER_WIDTH *= mScale;
                CURRENT_TIME_LINE_HEIGHT *= mScale;
                CURRENT_TIME_LINE_BORDER_WIDTH *= mScale;
                CURRENT_TIME_MARKER_INNER_WIDTH *= mScale;
                CURRENT_TIME_LINE_SIDE_BUFFER *= mScale;
                SMALL_ROUND_RADIUS *= mScale;
            }
        }
        mResources = activity.getResources();
        mEventLoader = activity.mEventLoader;
        mEventGeometry = new EventGeometry();
        mEventGeometry.setMinEventHeight(MIN_EVENT_HEIGHT);
        mEventGeometry.setHourGap(HOUR_GAP);
        mParentActivity = activity;
        mCalendarApp = (CalendarApplication) mParentActivity.getApplication();
        mDeleteEventHelper = new DeleteEventHelper(activity, false );
        mLastPopupEventID = INVALID_EVENT_ID;
        init(activity);
    }
    private void init(Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClickable(true);
        setOnCreateContextMenuListener(this);
        mStartDay = Utils.getFirstDayOfWeek();
        mCurrentTime = new Time();
        long currentTime = System.currentTimeMillis();
        mCurrentTime.set(currentTime);
        postDelayed(mUpdateCurrentTime,
                UPDATE_CURRENT_TIME_DELAY - (currentTime % UPDATE_CURRENT_TIME_DELAY));
        mTodayJulianDay = Time.getJulianDay(currentTime, mCurrentTime.gmtoff);
        mWeek_saturdayColor = mResources.getColor(R.color.week_saturday);
        mWeek_sundayColor = mResources.getColor(R.color.week_sunday);
        mCalendarDateBannerTextColor = mResources.getColor(R.color.calendar_date_banner_text_color);
        mCalendarAllDayBackground = mResources.getColor(R.color.calendar_all_day_background);
        mCalendarAmPmLabel = mResources.getColor(R.color.calendar_ampm_label);
        mCalendarDateBannerBackground = mResources.getColor(R.color.calendar_date_banner_background);
        mCalendarDateSelected = mResources.getColor(R.color.calendar_date_selected);
        mCalendarGridAreaBackground = mResources.getColor(R.color.calendar_grid_area_background);
        mCalendarGridAreaSelected = mResources.getColor(R.color.calendar_grid_area_selected);
        mCalendarGridLineHorizontalColor = mResources.getColor(R.color.calendar_grid_line_horizontal_color);
        mCalendarGridLineVerticalColor = mResources.getColor(R.color.calendar_grid_line_vertical_color);
        mCalendarHourBackground = mResources.getColor(R.color.calendar_hour_background);
        mCalendarHourLabel = mResources.getColor(R.color.calendar_hour_label);
        mCalendarHourSelected = mResources.getColor(R.color.calendar_hour_selected);
        mSelectionColor = mResources.getColor(R.color.selection);
        mPressedColor = mResources.getColor(R.color.pressed);
        mSelectedEventTextColor = mResources.getColor(R.color.calendar_event_selected_text_color);
        mEventTextColor = mResources.getColor(R.color.calendar_event_text_color);
        mCurrentTimeMarkerColor = mResources.getColor(R.color.current_time_marker);
        mCurrentTimeMarkerBorderColor = mResources.getColor(R.color.current_time_marker_border);
        mEventTextPaint.setColor(mEventTextColor);
        mEventTextPaint.setTextSize(EVENT_TEXT_FONT_SIZE);
        mEventTextPaint.setTextAlign(Paint.Align.LEFT);
        mEventTextPaint.setAntiAlias(true);
        int gridLineColor = mResources.getColor(R.color.calendar_grid_line_highlight_color);
        Paint p = mSelectionPaint;
        p.setColor(gridLineColor);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(2.0f);
        p.setAntiAlias(false);
        p = mPaint;
        p.setAntiAlias(true);
        mPaintBorder.setColor(0xffc8c8c8);
        mPaintBorder.setStyle(Style.STROKE);
        mPaintBorder.setAntiAlias(true);
        mPaintBorder.setStrokeWidth(2.0f);
        mDayStrs = new String[14];
        mDayStrs2Letter = new String[14];
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            int index = i - Calendar.SUNDAY;
            mDayStrs[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_MEDIUM);
            mDayStrs[index + 7] = mDayStrs[index];
            mDayStrs2Letter[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_SHORT);
            if (mDayStrs2Letter[index].equals(mDayStrs[index])) {
                mDayStrs2Letter[index] = DateUtils.getDayOfWeekString(i, DateUtils.LENGTH_SHORTEST);
            }
            mDayStrs2Letter[index + 7] = mDayStrs2Letter[index];
        }
        p.setTextSize(NORMAL_FONT_SIZE);
        p.setTypeface(mBold);
        String[] dateStrs = {" 28", " 30"};
        mDateStrWidth = computeMaxStringWidth(0, dateStrs, p);
        mDateStrWidth += computeMaxStringWidth(0, mDayStrs, p);
        p.setTextSize(HOURS_FONT_SIZE);
        p.setTypeface(null);
        updateIs24HourFormat();
        mAmString = DateUtils.getAMPMString(Calendar.AM);
        mPmString = DateUtils.getAMPMString(Calendar.PM);
        String[] ampm = {mAmString, mPmString};
        p.setTextSize(AMPM_FONT_SIZE);
        mHoursWidth = computeMaxStringWidth(mHoursWidth, ampm, p);
        mHoursWidth += HOURS_MARGIN;
        LayoutInflater inflater;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopupView = inflater.inflate(R.layout.bubble_event, null);
        mPopupView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mPopup = new PopupWindow(context);
        mPopup.setContentView(mPopupView);
        Resources.Theme dialogTheme = getResources().newTheme();
        dialogTheme.applyStyle(android.R.style.Theme_Dialog, true);
        TypedArray ta = dialogTheme.obtainStyledAttributes(new int[] {
            android.R.attr.windowBackground });
        mPopup.setBackgroundDrawable(ta.getDrawable(0));
        ta.recycle();
        mPopupView.setOnClickListener(this);
        mBaseDate = new Time();
        long millis = System.currentTimeMillis();
        mBaseDate.set(millis);
        mEarliestStartHour = new int[mNumDays];
        mHasAllDayEvent = new boolean[mNumDays];
        mNumHours = context.getResources().getInteger(R.integer.number_of_hours);
        mTitleTextView = (TextView) mParentActivity.findViewById(R.id.title);
    }
    public void onClick(View v) {
        if (v == mPopupView) {
            switchViews(true );
        }
    }
    public void updateIs24HourFormat() {
        mIs24HourFormat = DateFormat.is24HourFormat(mParentActivity);
        mHourStrs = mIs24HourFormat ? CalendarData.s24Hours : CalendarData.s12HoursNoAmPm;
    }
    long getSelectedTimeInMillis() {
        Time time = new Time(mBaseDate);
        time.setJulianDay(mSelectionDay);
        time.hour = mSelectionHour;
        return time.normalize(true );
    }
    Time getSelectedTime() {
        Time time = new Time(mBaseDate);
        time.setJulianDay(mSelectionDay);
        time.hour = mSelectionHour;
        time.normalize(true );
        return time;
    }
    int getSelectedMinutesSinceMidnight() {
        return mSelectionHour * MINUTES_PER_HOUR;
    }
    public void setSelectedDay(Time time) {
        mBaseDate.set(time);
        mSelectionHour = mBaseDate.hour;
        mSelectedEvent = null;
        mPrevSelectedEvent = null;
        long millis = mBaseDate.toMillis(false );
        mSelectionDay = Time.getJulianDay(millis, mBaseDate.gmtoff);
        mSelectedEvents.clear();
        mComputeSelectedEvents = true;
        mFirstHour = -1;
        recalc();
        mTitleTextView.setText(mDateRange);
        mSelectionMode = SELECTION_SELECTED;
        mRedrawScreen = true;
        mRemeasure = true;
        invalidate();
    }
    public Time getSelectedDay() {
        Time time = new Time(mBaseDate);
        time.setJulianDay(mSelectionDay);
        time.hour = mSelectionHour;
        time.normalize(true );
        return time;
    }
    private void recalc() {
        if (mNumDays == 7) {
            int dayOfWeek = mBaseDate.weekDay;
            int diff = dayOfWeek - mStartDay;
            if (diff != 0) {
                if (diff < 0) {
                    diff += 7;
                }
                mBaseDate.monthDay -= diff;
                mBaseDate.normalize(true );
            }
        }
        final long start = mBaseDate.toMillis(false );
        long end = start;
        mFirstJulianDay = Time.getJulianDay(start, mBaseDate.gmtoff);
        mLastJulianDay = mFirstJulianDay + mNumDays - 1;
        mMonthLength = mBaseDate.getActualMaximum(Time.MONTH_DAY);
        mFirstDate = mBaseDate.monthDay;
        int flags = DateUtils.FORMAT_SHOW_YEAR;
        if (DateFormat.is24HourFormat(mParentActivity)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        if (mNumDays > 1) {
            mBaseDate.monthDay += mNumDays - 1;
            end = mBaseDate.toMillis(true );
            mBaseDate.monthDay -= mNumDays - 1;
            flags |= DateUtils.FORMAT_NO_MONTH_DAY;
        } else {
            flags |= DateUtils.FORMAT_SHOW_WEEKDAY
                    | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
        }
        mDateRange = DateUtils.formatDateRange(mParentActivity, start, end, flags);
    }
    void setDetailedView(String detailedView) {
        mDetailedView = detailedView;
    }
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        mViewWidth = width;
        mViewHeight = height;
        int gridAreaWidth = width - mHoursWidth;
        mCellWidth = (gridAreaWidth - (mNumDays * DAY_GAP)) / mNumDays;
        Paint p = new Paint();
        p.setTextSize(NORMAL_FONT_SIZE);
        int bannerTextHeight = (int) Math.abs(p.ascent());
        p.setTextSize(HOURS_FONT_SIZE);
        mHoursTextHeight = (int) Math.abs(p.ascent());
        p.setTextSize(EVENT_TEXT_FONT_SIZE);
        float ascent = -p.ascent();
        mEventTextAscent = (int) Math.ceil(ascent);
        float totalHeight = ascent + p.descent();
        mEventTextHeight = (int) Math.ceil(totalHeight);
        if (mNumDays > 1) {
            mBannerPlusMargin = bannerTextHeight + 14;
        } else {
            mBannerPlusMargin = 0;
        }
        remeasure(width, height);
    }
    private void remeasure(int width, int height) {
        for (int day = 0; day < mNumDays; day++) {
            mEarliestStartHour[day] = 25;  
            mHasAllDayEvent[day] = false;
        }
        int maxAllDayEvents = 0;
        ArrayList<Event> events = mEvents;
        int len = events.size();
        for (int ii = 0; ii < len; ii++) {
            Event event = events.get(ii);
            if (event.startDay > mLastJulianDay || event.endDay < mFirstJulianDay)
                continue;
            if (event.allDay) {
                int max = event.getColumn() + 1;
                if (maxAllDayEvents < max) {
                    maxAllDayEvents = max;
                }
                int daynum = event.startDay - mFirstJulianDay;
                int durationDays = event.endDay - event.startDay + 1;
                if (daynum < 0) {
                    durationDays += daynum;
                    daynum = 0;
                }
                if (daynum + durationDays > mNumDays) {
                    durationDays = mNumDays - daynum;
                }
                for (int day = daynum; durationDays > 0; day++, durationDays--) {
                    mHasAllDayEvent[day] = true;
                }
            } else {
                int daynum = event.startDay - mFirstJulianDay;
                int hour = event.startTime / 60;
                if (daynum >= 0 && hour < mEarliestStartHour[daynum]) {
                    mEarliestStartHour[daynum] = hour;
                }
                daynum = event.endDay - mFirstJulianDay;
                hour = event.endTime / 60;
                if (daynum < mNumDays && hour < mEarliestStartHour[daynum]) {
                    mEarliestStartHour[daynum] = hour;
                }
            }
        }
        mMaxAllDayEvents = maxAllDayEvents;
        mFirstCell = mBannerPlusMargin;
        int allDayHeight = 0;
        if (maxAllDayEvents > 0) {
            if (maxAllDayEvents == 1) {
                allDayHeight = SINGLE_ALLDAY_HEIGHT;
            } else {
                allDayHeight = maxAllDayEvents * MAX_ALLDAY_EVENT_HEIGHT;
                if (allDayHeight > MAX_ALLDAY_HEIGHT) {
                    allDayHeight = MAX_ALLDAY_HEIGHT;
                }
            }
            mFirstCell = mBannerPlusMargin + allDayHeight + ALLDAY_TOP_MARGIN;
        } else {
            mSelectionAllDay = false;
        }
        mAllDayHeight = allDayHeight;
        mGridAreaHeight = height - mFirstCell;
        mCellHeight = (mGridAreaHeight - ((mNumHours + 1) * HOUR_GAP)) / mNumHours;
        int usedGridAreaHeight = (mCellHeight + HOUR_GAP) * mNumHours + HOUR_GAP;
        int bottomSpace = mGridAreaHeight - usedGridAreaHeight;
        mEventGeometry.setHourHeight(mCellHeight);
        mBitmapHeight = HOUR_GAP + 24 * (mCellHeight + HOUR_GAP) + bottomSpace;
        if ((mBitmap == null || mBitmap.getHeight() < mBitmapHeight) && width > 0 &&
                mBitmapHeight > 0) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            mBitmap = Bitmap.createBitmap(width, mBitmapHeight, Bitmap.Config.RGB_565);
            mCanvas = new Canvas(mBitmap);
        }
        mMaxViewStartY = mBitmapHeight - mGridAreaHeight;
        if (mFirstHour == -1) {
            initFirstHour();
            mFirstHourOffset = 0;
        }
        if (mFirstHourOffset >= mCellHeight + HOUR_GAP) {
            mFirstHourOffset = mCellHeight + HOUR_GAP - 1;
        }
        mViewStartY = mFirstHour * (mCellHeight + HOUR_GAP) - mFirstHourOffset;
        int eventAreaWidth = mNumDays * (mCellWidth + DAY_GAP);
        if (mSelectedEvent != null && mLastPopupEventID != mSelectedEvent.id) {
            mPopup.dismiss();
        }
        mPopup.setWidth(eventAreaWidth - 20);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    }
    private void initView(CalendarView view) {
        view.mSelectionHour = mSelectionHour;
        view.mSelectedEvents.clear();
        view.mComputeSelectedEvents = true;
        view.mFirstHour = mFirstHour;
        view.mFirstHourOffset = mFirstHourOffset;
        view.remeasure(getWidth(), getHeight());
        view.mSelectedEvent = null;
        view.mPrevSelectedEvent = null;
        view.mStartDay = mStartDay;
        if (view.mEvents.size() > 0) {
            view.mSelectionAllDay = mSelectionAllDay;
        } else {
            view.mSelectionAllDay = false;
        }
        view.mRedrawScreen = true;
        view.recalc();
    }
    private void switchViews(boolean trackBallSelection) {
        Event selectedEvent = mSelectedEvent;
        mPopup.dismiss();
        mLastPopupEventID = INVALID_EVENT_ID;
        if (mNumDays > 1) {
            if (trackBallSelection) {
                if (selectedEvent == null) {
                    long startMillis = getSelectedTimeInMillis();
                    long endMillis = startMillis + DateUtils.HOUR_IN_MILLIS;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setClassName(mParentActivity, EditEvent.class.getName());
                    intent.putExtra(EVENT_BEGIN_TIME, startMillis);
                    intent.putExtra(EVENT_END_TIME, endMillis);
                    mParentActivity.startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI,
                            selectedEvent.id);
                    intent.setData(eventUri);
                    intent.setClassName(mParentActivity, EventInfoActivity.class.getName());
                    intent.putExtra(EVENT_BEGIN_TIME, selectedEvent.startMillis);
                    intent.putExtra(EVENT_END_TIME, selectedEvent.endMillis);
                    mParentActivity.startActivity(intent);
                }
            } else {
                if (mSelectedEvents.size() == 1) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI,
                            selectedEvent.id);
                    intent.setData(eventUri);
                    intent.setClassName(mParentActivity, EventInfoActivity.class.getName());
                    intent.putExtra(EVENT_BEGIN_TIME, selectedEvent.startMillis);
                    intent.putExtra(EVENT_END_TIME, selectedEvent.endMillis);
                    mParentActivity.startActivity(intent);
                } else {
                    long millis = getSelectedTimeInMillis();
                    Utils.startActivity(mParentActivity, mDetailedView, millis);
                }
            }
        } else {
            if (selectedEvent == null) {
                long startMillis = getSelectedTimeInMillis();
                long endMillis = startMillis + DateUtils.HOUR_IN_MILLIS;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(mParentActivity, EditEvent.class.getName());
                intent.putExtra(EVENT_BEGIN_TIME, startMillis);
                intent.putExtra(EVENT_END_TIME, endMillis);
                mParentActivity.startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, selectedEvent.id);
                intent.setData(eventUri);
                intent.setClassName(mParentActivity, EventInfoActivity.class.getName());
                intent.putExtra(EVENT_BEGIN_TIME, selectedEvent.startMillis);
                intent.putExtra(EVENT_END_TIME, selectedEvent.endMillis);
                mParentActivity.startActivity(intent);
            }
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mScrolling = false;
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
                    switchViews(true );
                } else {
                    mSelectionMode = SELECTION_LONGPRESS;
                    mRedrawScreen = true;
                    invalidate();
                    performLongClick();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                if (event.isTracking() && !event.isCanceled()) {
                    mPopup.dismiss();
                    mParentActivity.finish();
                    return true;
                }
                break;
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
        mScrolling = false;
        boolean redraw;
        int selectionDay = mSelectionDay;
        switch (keyCode) {
        case KeyEvent.KEYCODE_DEL:
            Event selectedEvent = mSelectedEvent;
            if (selectedEvent == null) {
                return false;
            }
            mPopup.dismiss();
            mLastPopupEventID = INVALID_EVENT_ID;
            long begin = selectedEvent.startMillis;
            long end = selectedEvent.endMillis;
            long id = selectedEvent.id;
            mDeleteEventHelper.delete(begin, end, id, -1);
            return true;
        case KeyEvent.KEYCODE_ENTER:
            switchViews(true );
            return true;
        case KeyEvent.KEYCODE_BACK:
            if (event.getRepeatCount() == 0) {
                event.startTracking();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (mSelectedEvent != null) {
                mSelectedEvent = mSelectedEvent.nextLeft;
            }
            if (mSelectedEvent == null) {
                mLastPopupEventID = INVALID_EVENT_ID;
                selectionDay -= 1;
            }
            redraw = true;
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (mSelectedEvent != null) {
                mSelectedEvent = mSelectedEvent.nextRight;
            }
            if (mSelectedEvent == null) {
                mLastPopupEventID = INVALID_EVENT_ID;
                selectionDay += 1;
            }
            redraw = true;
            break;
        case KeyEvent.KEYCODE_DPAD_UP:
            if (mSelectedEvent != null) {
                mSelectedEvent = mSelectedEvent.nextUp;
            }
            if (mSelectedEvent == null) {
                mLastPopupEventID = INVALID_EVENT_ID;
                if (!mSelectionAllDay) {
                    mSelectionHour -= 1;
                    adjustHourSelection();
                    mSelectedEvents.clear();
                    mComputeSelectedEvents = true;
                }
            }
            redraw = true;
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            if (mSelectedEvent != null) {
                mSelectedEvent = mSelectedEvent.nextDown;
            }
            if (mSelectedEvent == null) {
                mLastPopupEventID = INVALID_EVENT_ID;
                if (mSelectionAllDay) {
                    mSelectionAllDay = false;
                } else {
                    mSelectionHour++;
                    adjustHourSelection();
                    mSelectedEvents.clear();
                    mComputeSelectedEvents = true;
                }
            }
            redraw = true;
            break;
        default:
            return super.onKeyDown(keyCode, event);
        }
        if ((selectionDay < mFirstJulianDay) || (selectionDay > mLastJulianDay)) {
            boolean forward;
            CalendarView view = mParentActivity.getNextView();
            Time date = view.mBaseDate;
            date.set(mBaseDate);
            if (selectionDay < mFirstJulianDay) {
                date.monthDay -= mNumDays;
                forward = false;
            } else {
                date.monthDay += mNumDays;
                forward = true;
            }
            date.normalize(true );
            view.mSelectionDay = selectionDay;
            initView(view);
            mTitleTextView.setText(view.mDateRange);
            mParentActivity.switchViews(forward, 0, 0);
            return true;
        }
        mSelectionDay = selectionDay;
        mSelectedEvents.clear();
        mComputeSelectedEvents = true;
        if (redraw) {
            mRedrawScreen = true;
            invalidate();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void resetSelectedHour() {
        if (mSelectionHour < mFirstHour + 1) {
            mSelectionHour = mFirstHour + 1;
            mSelectedEvent = null;
            mSelectedEvents.clear();
            mComputeSelectedEvents = true;
        } else if (mSelectionHour > mFirstHour + mNumHours - 3) {
            mSelectionHour = mFirstHour + mNumHours - 3;
            mSelectedEvent = null;
            mSelectedEvents.clear();
            mComputeSelectedEvents = true;
        }
    }
    private void initFirstHour() {
        mFirstHour = mSelectionHour - mNumHours / 2;
        if (mFirstHour < 0) {
            mFirstHour = 0;
        } else if (mFirstHour + mNumHours > 24) {
            mFirstHour = 24 - mNumHours;
        }
    }
    private void computeFirstHour() {
        mFirstHour = (mViewStartY + mCellHeight + HOUR_GAP - 1) / (mCellHeight + HOUR_GAP);
        mFirstHourOffset = mFirstHour * (mCellHeight + HOUR_GAP) - mViewStartY;
    }
    private void adjustHourSelection() {
        if (mSelectionHour < 0) {
            mSelectionHour = 0;
            if (mMaxAllDayEvents > 0) {
                mPrevSelectedEvent = null;
                mSelectionAllDay = true;
            }
        }
        if (mSelectionHour > 23) {
            mSelectionHour = 23;
        }
        if (mSelectionHour < mFirstHour + 1) {
            int daynum = mSelectionDay - mFirstJulianDay;
            if (mMaxAllDayEvents > 0 && mEarliestStartHour[daynum] > mSelectionHour
                    && mFirstHour > 0 && mFirstHour < 8) {
                mPrevSelectedEvent = null;
                mSelectionAllDay = true;
                mSelectionHour = mFirstHour + 1;
                return;
            }
            if (mFirstHour > 0) {
                mFirstHour -= 1;
                mViewStartY -= (mCellHeight + HOUR_GAP);
                if (mViewStartY < 0) {
                    mViewStartY = 0;
                }
                return;
            }
        }
        if (mSelectionHour > mFirstHour + mNumHours - 3) {
            if (mFirstHour < 24 - mNumHours) {
                mFirstHour += 1;
                mViewStartY += (mCellHeight + HOUR_GAP);
                if (mViewStartY > mBitmapHeight - mGridAreaHeight) {
                    mViewStartY = mBitmapHeight - mGridAreaHeight;
                }
                return;
            } else if (mFirstHour == 24 - mNumHours && mFirstHourOffset > 0) {
                mViewStartY = mBitmapHeight - mGridAreaHeight;
            }
        }
    }
    void clearCachedEvents() {
        mLastReloadMillis = 0;
    }
    private Runnable mCancelCallback = new Runnable() {
        public void run() {
            clearCachedEvents();
        }
    };
    void reloadEvents() {
        if (mParentActivity == null) {
            return;
        }
        mSelectedEvent = null;
        mPrevSelectedEvent = null;
        mSelectedEvents.clear();
        Time weekStart = new Time();
        weekStart.set(mBaseDate);
        weekStart.hour = 0;
        weekStart.minute = 0;
        weekStart.second = 0;
        long millis = weekStart.normalize(true );
        if (millis == mLastReloadMillis) {
            return;
        }
        mLastReloadMillis = millis;
        mParentActivity.startProgressSpinner();
        final ArrayList<Event> events = new ArrayList<Event>();
        mEventLoader.loadEventsInBackground(mNumDays, events, millis, new Runnable() {
            public void run() {
                mEvents = events;
                mRemeasure = true;
                mRedrawScreen = true;
                mComputeSelectedEvents = true;
                recalc();
                mParentActivity.stopProgressSpinner();
                invalidate();
            }
        }, mCancelCallback);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (mRemeasure) {
            remeasure(getWidth(), getHeight());
            mRemeasure = false;
        }
        if (mRedrawScreen && mCanvas != null) {
            doDraw(mCanvas);
            mRedrawScreen = false;
        }
        if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            canvas.save();
            if (mViewStartX > 0) {
                canvas.translate(mViewWidth - mViewStartX, 0);
            } else {
                canvas.translate(-(mViewWidth + mViewStartX), 0);
            }
            CalendarView nextView = mParentActivity.getNextView();
            nextView.mTouchMode = TOUCH_MODE_INITIAL_STATE;
            nextView.onDraw(canvas);
            canvas.restore();
            canvas.save();
            canvas.translate(-mViewStartX, 0);
        }
        if (mBitmap != null) {
            drawCalendarView(canvas);
        }
        drawAfterScroll(canvas);
        mComputeSelectedEvents = false;
        if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            canvas.restore();
        }
    }
    private void drawCalendarView(Canvas canvas) {
        Rect src = mSrcRect;
        Rect dest = mDestRect;
        src.top = mViewStartY;
        src.bottom = mViewStartY + mGridAreaHeight;
        src.left = 0;
        src.right = mViewWidth;
        dest.top = mFirstCell;
        dest.bottom = mViewHeight;
        dest.left = 0;
        dest.right = mViewWidth;
        canvas.save();
        canvas.clipRect(dest);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(mBitmap, src, dest, null);
        canvas.restore();
    }
    private void drawAfterScroll(Canvas canvas) {
        Paint p = mPaint;
        Rect r = mRect;
        if (mMaxAllDayEvents != 0) {
            drawAllDayEvents(mFirstJulianDay, mNumDays, r, canvas, p);
            drawUpperLeftCorner(r, canvas, p);
        }
        if (mNumDays > 1) {
            drawDayHeaderLoop(r, canvas, p);
        }
        if (!mIs24HourFormat) {
            drawAmPm(canvas, p);
        }
        if (!mScrolling && isFocused()) {
            updateEventDetails();
        }
    }
    private void drawUpperLeftCorner(Rect r, Canvas canvas, Paint p) {
        p.setColor(mCalendarHourBackground);
        r.top = mBannerPlusMargin;
        r.bottom = r.top + mAllDayHeight + ALLDAY_TOP_MARGIN;
        r.left = 0;
        r.right = mHoursWidth;
        canvas.drawRect(r, p);
    }
    private void drawDayHeaderLoop(Rect r, Canvas canvas, Paint p) {
        p.setColor(mCalendarDateBannerBackground);
        r.top = 0;
        r.bottom = mBannerPlusMargin;
        r.left = 0;
        r.right = mHoursWidth + mNumDays * (mCellWidth + DAY_GAP);
        canvas.drawRect(r, p);
        r.left = r.right;
        r.right = mViewWidth;
        p.setColor(mCalendarGridAreaBackground);
        canvas.drawRect(r, p);
        if (mSelectionMode != SELECTION_HIDDEN) {
            if (mNumDays > 1) {
                p.setColor(mCalendarDateSelected);
                r.top = 0;
                r.bottom = mBannerPlusMargin;
                int daynum = mSelectionDay - mFirstJulianDay;
                r.left = mHoursWidth + daynum * (mCellWidth + DAY_GAP);
                r.right = r.left + mCellWidth;
                canvas.drawRect(r, p);
            }
        }
        p.setTextSize(NORMAL_FONT_SIZE);
        p.setTextAlign(Paint.Align.CENTER);
        int x = mHoursWidth;
        int deltaX = mCellWidth + DAY_GAP;
        int cell = mFirstJulianDay;
        String[] dayNames;
        if (mDateStrWidth < mCellWidth) {
            dayNames = mDayStrs;
        } else {
            dayNames = mDayStrs2Letter;
        }
        p.setTypeface(mBold);
        p.setAntiAlias(true);
        for (int day = 0; day < mNumDays; day++, cell++) {
            drawDayHeader(dayNames[day + mStartDay], day, cell, x, canvas, p);
            x += deltaX;
        }
    }
    private void drawAmPm(Canvas canvas, Paint p) {
        p.setColor(mCalendarAmPmLabel);
        p.setTextSize(AMPM_FONT_SIZE);
        p.setTypeface(mBold);
        p.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.RIGHT);
        String text = mAmString;
        if (mFirstHour >= 12) {
            text = mPmString;
        }
        int y = mFirstCell + mFirstHourOffset + 2 * mHoursTextHeight + HOUR_GAP;
        int right = mHoursWidth - HOURS_RIGHT_MARGIN;
        canvas.drawText(text, right, y, p);
        if (mFirstHour < 12 && mFirstHour + mNumHours > 12) {
            text = mPmString;
            y = mFirstCell + mFirstHourOffset + (12 - mFirstHour) * (mCellHeight + HOUR_GAP)
                    + 2 * mHoursTextHeight + HOUR_GAP;
            canvas.drawText(text, right, y, p);
        }
    }
    private void drawCurrentTimeMarker(int top, Canvas canvas, Paint p) {
        top -= CURRENT_TIME_MARKER_HEIGHT / 2;
        p.setColor(mCurrentTimeMarkerColor);
        Paint.Style oldStyle = p.getStyle();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2.0f);
        Path mCurrentTimeMarker = mPath;
        mCurrentTimeMarker.reset();
        mCurrentTimeMarker.moveTo(0, top);
        mCurrentTimeMarker.lineTo(0, CURRENT_TIME_MARKER_HEIGHT + top);
        mCurrentTimeMarker.lineTo(CURRENT_TIME_MARKER_INNER_WIDTH, CURRENT_TIME_MARKER_HEIGHT + top);
        mCurrentTimeMarker.lineTo(CURRENT_TIME_MARKER_WIDTH, CURRENT_TIME_MARKER_HEIGHT / 2 + top);
        mCurrentTimeMarker.lineTo(CURRENT_TIME_MARKER_INNER_WIDTH, top);
        mCurrentTimeMarker.lineTo(0, top);
        canvas.drawPath(mCurrentTimeMarker, p);
        p.setStyle(oldStyle);
    }
    private void drawCurrentTimeLine(Rect r, int left, int top, Canvas canvas, Paint p) {
        p.setColor(mCurrentTimeMarkerBorderColor);
        r.top = top - CURRENT_TIME_LINE_HEIGHT / 2 - CURRENT_TIME_LINE_BORDER_WIDTH;
        r.bottom = top + CURRENT_TIME_LINE_HEIGHT / 2 + CURRENT_TIME_LINE_BORDER_WIDTH;
        r.left = left + CURRENT_TIME_LINE_SIDE_BUFFER;
        r.right = r.left + mCellWidth - 2 * CURRENT_TIME_LINE_SIDE_BUFFER;
        canvas.drawRect(r, p);
        p.setColor(mCurrentTimeMarkerColor);
        r.top = top - CURRENT_TIME_LINE_HEIGHT / 2;
        r.bottom = top + CURRENT_TIME_LINE_HEIGHT / 2;
        canvas.drawRect(r, p);
    }
    private void doDraw(Canvas canvas) {
        Paint p = mPaint;
        Rect r = mRect;
        int lineY = mCurrentTime.hour*(mCellHeight + HOUR_GAP)
            + ((mCurrentTime.minute * mCellHeight) / 60)
            + 1;
        drawGridBackground(r, canvas, p);
        drawHours(r, canvas, p);
        int x = mHoursWidth;
        int deltaX = mCellWidth + DAY_GAP;
        int cell = mFirstJulianDay;
        for (int day = 0; day < mNumDays; day++, cell++) {
            drawEvents(cell, x, HOUR_GAP, canvas, p);
            if(cell == mTodayJulianDay) {
                if(lineY >= mViewStartY && lineY < mViewStartY + mViewHeight - 2) {
                    drawCurrentTimeMarker(lineY, canvas, p);
                    drawCurrentTimeLine(r, x, lineY, canvas, p);
                }
            }
            x += deltaX;
        }
    }
    private void drawHours(Rect r, Canvas canvas, Paint p) {
        p.setColor(mCalendarHourBackground);
        r.top = 0;
        r.bottom = 24 * (mCellHeight + HOUR_GAP) + HOUR_GAP;
        r.left = 0;
        r.right = mHoursWidth;
        canvas.drawRect(r, p);
        r.top = r.bottom;
        r.bottom = mBitmapHeight;
        p.setColor(mCalendarGridAreaBackground);
        canvas.drawRect(r, p);
        if (mSelectionMode != SELECTION_HIDDEN && !mSelectionAllDay) {
            p.setColor(mCalendarHourSelected);
            r.top = mSelectionHour * (mCellHeight + HOUR_GAP);
            r.bottom = r.top + mCellHeight + 2 * HOUR_GAP;
            r.left = 0;
            r.right = mHoursWidth;
            canvas.drawRect(r, p);
            p.setColor(mCalendarGridAreaSelected);
            int daynum = mSelectionDay - mFirstJulianDay;
            r.left = mHoursWidth + daynum * (mCellWidth + DAY_GAP);
            r.right = r.left + mCellWidth;
            canvas.drawRect(r, p);
            Path path = mPath;
            r.top += HOUR_GAP;
            r.bottom -= HOUR_GAP;
            path.reset();
            path.addRect(r.left, r.top, r.right, r.bottom, Direction.CW);
            canvas.drawPath(path, mSelectionPaint);
            saveSelectionPosition(r.left, r.top, r.right, r.bottom);
        }
        p.setColor(mCalendarHourLabel);
        p.setTextSize(HOURS_FONT_SIZE);
        p.setTypeface(mBold);
        p.setTextAlign(Paint.Align.RIGHT);
        p.setAntiAlias(true);
        int right = mHoursWidth - HOURS_RIGHT_MARGIN;
        int y = HOUR_GAP + mHoursTextHeight;
        for (int i = 0; i < 24; i++) {
            String time = mHourStrs[i];
            canvas.drawText(time, right, y, p);
            y += mCellHeight + HOUR_GAP;
        }
    }
    private void drawDayHeader(String dateStr, int day, int cell, int x, Canvas canvas, Paint p) {
        float xCenter = x + mCellWidth / 2.0f;
        if (Utils.isSaturday(day, mStartDay)) {
            p.setColor(mWeek_saturdayColor);
        } else if (Utils.isSunday(day, mStartDay)) {
            p.setColor(mWeek_sundayColor);
        } else {
            p.setColor(mCalendarDateBannerTextColor);
        }
        int dateNum = mFirstDate + day;
        if (dateNum > mMonthLength) {
            dateNum -= mMonthLength;
        }
        String dateNumStr;
        if (dateNum < 10) {
            dateNumStr = "0" + dateNum;
        } else {
            dateNumStr = String.valueOf(dateNum);
        }
        DayHeader header = dayHeaders[day];
        if (header == null || header.cell != cell) {
            dayHeaders[day] = new DayHeader();
            dayHeaders[day].cell = cell;
            dayHeaders[day].dateString = getResources().getString(
                    R.string.weekday_day, dateStr, dateNumStr);
        }
        dateStr = dayHeaders[day].dateString;
        float y = mBannerPlusMargin - 7;
        canvas.drawText(dateStr, xCenter, y, p);
    }
    private void drawGridBackground(Rect r, Canvas canvas, Paint p) {
        Paint.Style savedStyle = p.getStyle();
        p.setColor(mCalendarGridAreaBackground);
        r.top = 0;
        r.bottom = mBitmapHeight;
        r.left = 0;
        r.right = mViewWidth;
        canvas.drawRect(r, p);
        p.setColor(mCalendarGridLineHorizontalColor);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(0);
        p.setAntiAlias(false);
        float startX = mHoursWidth;
        float stopX = mHoursWidth + (mCellWidth + DAY_GAP) * mNumDays;
        float y = 0;
        float deltaY = mCellHeight + HOUR_GAP;
        for (int hour = 0; hour <= 24; hour++) {
            canvas.drawLine(startX, y, stopX, y, p);
            y += deltaY;
        }
        p.setColor(mCalendarGridLineVerticalColor);
        float startY = 0;
        float stopY = HOUR_GAP + 24 * (mCellHeight + HOUR_GAP);
        float deltaX = mCellWidth + DAY_GAP;
        float x = mHoursWidth + mCellWidth;
        for (int day = 0; day < mNumDays; day++) {
            canvas.drawLine(x, startY, x, stopY, p);
            x += deltaX;
        }
        p.setStyle(savedStyle);
        p.setAntiAlias(true);
    }
    Event getSelectedEvent() {
        if (mSelectedEvent == null) {
            return getNewEvent(mSelectionDay, getSelectedTimeInMillis(),
                    getSelectedMinutesSinceMidnight());
        }
        return mSelectedEvent;
    }
    boolean isEventSelected() {
        return (mSelectedEvent != null);
    }
    Event getNewEvent() {
        return getNewEvent(mSelectionDay, getSelectedTimeInMillis(),
                getSelectedMinutesSinceMidnight());
    }
    static Event getNewEvent(int julianDay, long utcMillis,
            int minutesSinceMidnight) {
        Event event = Event.newInstance();
        event.startDay = julianDay;
        event.endDay = julianDay;
        event.startMillis = utcMillis;
        event.endMillis = event.startMillis + MILLIS_PER_HOUR;
        event.startTime = minutesSinceMidnight;
        event.endTime = event.startTime + MINUTES_PER_HOUR;
        return event;
    }
    private int computeMaxStringWidth(int currentMax, String[] strings, Paint p) {
        float maxWidthF = 0.0f;
        int len = strings.length;
        for (int i = 0; i < len; i++) {
            float width = p.measureText(strings[i]);
            maxWidthF = Math.max(width, maxWidthF);
        }
        int maxWidth = (int) (maxWidthF + 0.5);
        if (maxWidth < currentMax) {
            maxWidth = currentMax;
        }
        return maxWidth;
    }
    private void saveSelectionPosition(float left, float top, float right, float bottom) {
        mPrevBox.left = (int) left;
        mPrevBox.right = (int) right;
        mPrevBox.top = (int) top;
        mPrevBox.bottom = (int) bottom;
    }
    private Rect getCurrentSelectionPosition() {
        Rect box = new Rect();
        box.top = mSelectionHour * (mCellHeight + HOUR_GAP);
        box.bottom = box.top + mCellHeight + HOUR_GAP;
        int daynum = mSelectionDay - mFirstJulianDay;
        box.left = mHoursWidth + daynum * (mCellWidth + DAY_GAP);
        box.right = box.left + mCellWidth + DAY_GAP;
        return box;
    }
    private void drawAllDayEvents(int firstDay, int numDays,
            Rect r, Canvas canvas, Paint p) {
        p.setTextSize(NORMAL_FONT_SIZE);
        p.setTextAlign(Paint.Align.LEFT);
        Paint eventTextPaint = mEventTextPaint;
        r.top = mBannerPlusMargin;
        r.bottom = r.top + mAllDayHeight + ALLDAY_TOP_MARGIN;
        r.left = mHoursWidth;
        r.right = r.left + mNumDays * (mCellWidth + DAY_GAP);
        p.setColor(mCalendarAllDayBackground);
        canvas.drawRect(r, p);
        r.left = r.right;
        r.right = mViewWidth;
        p.setColor(mCalendarGridAreaBackground);
        canvas.drawRect(r, p);
        p.setColor(mCalendarGridLineVerticalColor);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(0);
        p.setAntiAlias(false);
        float startY = r.top;
        float stopY = r.bottom;
        float deltaX = mCellWidth + DAY_GAP;
        float x = mHoursWidth + mCellWidth;
        for (int day = 0; day <= mNumDays; day++) {
            canvas.drawLine(x, startY, x, stopY, p);
            x += deltaX;
        }
        p.setAntiAlias(true);
        p.setStyle(Style.FILL);
        int y = mBannerPlusMargin + ALLDAY_TOP_MARGIN;
        float left = mHoursWidth;
        int lastDay = firstDay + numDays - 1;
        ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        float drawHeight = mAllDayHeight;
        float numRectangles = mMaxAllDayEvents;
        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);
            if (!event.allDay)
                continue;
            int startDay = event.startDay;
            int endDay = event.endDay;
            if (startDay > lastDay || endDay < firstDay)
                continue;
            if (startDay < firstDay)
                startDay = firstDay;
            if (endDay > lastDay)
                endDay = lastDay;
            int startIndex = startDay - firstDay;
            int endIndex = endDay - firstDay;
            float height = drawHeight / numRectangles;
            if (height > MAX_ALLDAY_EVENT_HEIGHT) {
                height = MAX_ALLDAY_EVENT_HEIGHT;
            }
            event.left = left + startIndex * (mCellWidth + DAY_GAP) + 2;
            event.right = left + endIndex * (mCellWidth + DAY_GAP) + mCellWidth - 1;
            event.top = y + height * event.getColumn();
            event.bottom = event.top + height * 0.9f;
            RectF rf = drawAllDayEventRect(event, canvas, p, eventTextPaint);
            drawEventText(event, rf, canvas, eventTextPaint, ALL_DAY_TEXT_TOP_MARGIN);
            if (mSelectionAllDay && mComputeSelectedEvents) {
                if (startDay <= mSelectionDay && endDay >= mSelectionDay) {
                    mSelectedEvents.add(event);
                }
            }
        }
        if (mSelectionAllDay) {
            computeAllDayNeighbors();
            if (mSelectedEvent != null) {
                Event event = mSelectedEvent;
                RectF rf = drawAllDayEventRect(event, canvas, p, eventTextPaint);
                drawEventText(event, rf, canvas, eventTextPaint, ALL_DAY_TEXT_TOP_MARGIN);
            }
            float top = mBannerPlusMargin + 1;
            float bottom = top + mAllDayHeight + ALLDAY_TOP_MARGIN - 1;
            int daynum = mSelectionDay - mFirstJulianDay;
            left = mHoursWidth + daynum * (mCellWidth + DAY_GAP) + 1;
            float right = left + mCellWidth + DAY_GAP - 1;
            if (mNumDays == 1) {
                right -= 1;
            }
            Path path = mPath;
            path.reset();
            path.addRect(left, top, right, bottom, Direction.CW);
            canvas.drawPath(path, mSelectionPaint);
            saveSelectionPosition(0f, 0f, 0f, 0f);
        }
    }
    private void computeAllDayNeighbors() {
        int len = mSelectedEvents.size();
        if (len == 0 || mSelectedEvent != null) {
            return;
        }
        for (int ii = 0; ii < len; ii++) {
            Event ev = mSelectedEvents.get(ii);
            ev.nextUp = null;
            ev.nextDown = null;
            ev.nextLeft = null;
            ev.nextRight = null;
        }
        int startPosition = -1;
        if (mPrevSelectedEvent != null && mPrevSelectedEvent.allDay) {
            startPosition = mPrevSelectedEvent.getColumn();
        }
        int maxPosition = -1;
        Event startEvent = null;
        Event maxPositionEvent = null;
        for (int ii = 0; ii < len; ii++) {
            Event ev = mSelectedEvents.get(ii);
            int position = ev.getColumn();
            if (position == startPosition) {
                startEvent = ev;
            } else if (position > maxPosition) {
                maxPositionEvent = ev;
                maxPosition = position;
            }
            for (int jj = 0; jj < len; jj++) {
                if (jj == ii) {
                    continue;
                }
                Event neighbor = mSelectedEvents.get(jj);
                int neighborPosition = neighbor.getColumn();
                if (neighborPosition == position - 1) {
                    ev.nextUp = neighbor;
                } else if (neighborPosition == position + 1) {
                    ev.nextDown = neighbor;
                }
            }
        }
        if (startEvent != null) {
            mSelectedEvent = startEvent;
        } else {
            mSelectedEvent = maxPositionEvent;
        }
    }
    RectF drawAllDayEventRect(Event event, Canvas canvas, Paint p, Paint eventTextPaint) {
        if (mSelectedEvent == event) {
            mPrevSelectedEvent = event;
            p.setColor(mSelectionColor);
            eventTextPaint.setColor(mSelectedEventTextColor);
        } else {
            p.setColor(event.color);
            eventTextPaint.setColor(mEventTextColor);
        }
        RectF rf = mRectF;
        rf.top = event.top;
        rf.bottom = event.bottom;
        rf.left = event.left;
        rf.right = event.right;
        canvas.drawRoundRect(rf, SMALL_ROUND_RADIUS, SMALL_ROUND_RADIUS, p);
        rf.left += 2;
        rf.right -= 2;
        return rf;
    }
    private void drawEvents(int date, int left, int top, Canvas canvas, Paint p) {
        Paint eventTextPaint = mEventTextPaint;
        int cellWidth = mCellWidth;
        int cellHeight = mCellHeight;
        Rect selectionArea = mRect;
        selectionArea.top = top + mSelectionHour * (cellHeight + HOUR_GAP);
        selectionArea.bottom = selectionArea.top + cellHeight;
        selectionArea.left = left;
        selectionArea.right = selectionArea.left + cellWidth;
        ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        EventGeometry geometry = mEventGeometry;
        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);
            if (!geometry.computeEventRect(date, left, top, cellWidth, event)) {
                continue;
            }
            if (date == mSelectionDay && !mSelectionAllDay && mComputeSelectedEvents
                    && geometry.eventIntersectsSelection(event, selectionArea)) {
                mSelectedEvents.add(event);
            }
            RectF rf = drawEventRect(event, canvas, p, eventTextPaint);
            drawEventText(event, rf, canvas, eventTextPaint, NORMAL_TEXT_TOP_MARGIN);
        }
        if (date == mSelectionDay && !mSelectionAllDay && isFocused()
                && mSelectionMode != SELECTION_HIDDEN) {
            computeNeighbors();
            if (mSelectedEvent != null) {
                RectF rf = drawEventRect(mSelectedEvent, canvas, p, eventTextPaint);
                drawEventText(mSelectedEvent, rf, canvas, eventTextPaint, NORMAL_TEXT_TOP_MARGIN);
            }
        }
    }
    private void computeNeighbors() {
        int len = mSelectedEvents.size();
        if (len == 0 || mSelectedEvent != null) {
            return;
        }
        for (int ii = 0; ii < len; ii++) {
            Event ev = mSelectedEvents.get(ii);
            ev.nextUp = null;
            ev.nextDown = null;
            ev.nextLeft = null;
            ev.nextRight = null;
        }
        Event startEvent = mSelectedEvents.get(0);
        int startEventDistance1 = 100000;  
        int startEventDistance2 = 100000;  
        int prevLocation = FROM_NONE;
        int prevTop;
        int prevBottom;
        int prevLeft;
        int prevRight;
        int prevCenter = 0;
        Rect box = getCurrentSelectionPosition();
        if (mPrevSelectedEvent != null) {
            prevTop = (int) mPrevSelectedEvent.top;
            prevBottom = (int) mPrevSelectedEvent.bottom;
            prevLeft = (int) mPrevSelectedEvent.left;
            prevRight = (int) mPrevSelectedEvent.right;
            if (prevTop >= mPrevBox.bottom || prevBottom <= mPrevBox.top
                    || prevRight <= mPrevBox.left || prevLeft >= mPrevBox.right) {
                mPrevSelectedEvent = null;
                prevTop = mPrevBox.top;
                prevBottom = mPrevBox.bottom;
                prevLeft = mPrevBox.left;
                prevRight = mPrevBox.right;
            } else {
                if (prevTop < mPrevBox.top) {
                    prevTop = mPrevBox.top;
                }
                if (prevBottom > mPrevBox.bottom) {
                    prevBottom = mPrevBox.bottom;
                }
            }
        } else {
            prevTop = mPrevBox.top;
            prevBottom = mPrevBox.bottom;
            prevLeft = mPrevBox.left;
            prevRight = mPrevBox.right;
        }
        if (prevLeft >= box.right) {
            prevLocation = FROM_RIGHT;
            prevCenter = (prevTop + prevBottom) / 2;
        } else if (prevRight <= box.left) {
            prevLocation = FROM_LEFT;
            prevCenter = (prevTop + prevBottom) / 2;
        } else if (prevBottom <= box.top) {
            prevLocation = FROM_ABOVE;
            prevCenter = (prevLeft + prevRight) / 2;
        } else if (prevTop >= box.bottom) {
            prevLocation = FROM_BELOW;
            prevCenter = (prevLeft + prevRight) / 2;
        }
        for (int ii = 0; ii < len; ii++) {
            Event ev = mSelectedEvents.get(ii);
            int startTime = ev.startTime;
            int endTime = ev.endTime;
            int left = (int) ev.left;
            int right = (int) ev.right;
            int top = (int) ev.top;
            if (top < box.top) {
                top = box.top;
            }
            int bottom = (int) ev.bottom;
            if (bottom > box.bottom) {
                bottom = box.bottom;
            }
            if (false) {
                int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL
                        | DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
                if (DateFormat.is24HourFormat(mParentActivity)) {
                    flags |= DateUtils.FORMAT_24HOUR;
                }
                String timeRange = DateUtils.formatDateRange(mParentActivity,
                        ev.startMillis, ev.endMillis, flags);
                Log.i("Cal", "left: " + left + " right: " + right + " top: " + top
                        + " bottom: " + bottom + " ev: " + timeRange + " " + ev.title);
            }
            int upDistanceMin = 10000;     
            int downDistanceMin = 10000;   
            int leftDistanceMin = 10000;   
            int rightDistanceMin = 10000;  
            Event upEvent = null;
            Event downEvent = null;
            Event leftEvent = null;
            Event rightEvent = null;
            int distance1 = 0;
            int distance2 = 0;
            if (prevLocation == FROM_ABOVE) {
                if (left >= prevCenter) {
                    distance1 = left - prevCenter;
                } else if (right <= prevCenter) {
                    distance1 = prevCenter - right;
                }
                distance2 = top - prevBottom;
            } else if (prevLocation == FROM_BELOW) {
                if (left >= prevCenter) {
                    distance1 = left - prevCenter;
                } else if (right <= prevCenter) {
                    distance1 = prevCenter - right;
                }
                distance2 = prevTop - bottom;
            } else if (prevLocation == FROM_LEFT) {
                if (bottom <= prevCenter) {
                    distance1 = prevCenter - bottom;
                } else if (top >= prevCenter) {
                    distance1 = top - prevCenter;
                }
                distance2 = left - prevRight;
            } else if (prevLocation == FROM_RIGHT) {
                if (bottom <= prevCenter) {
                    distance1 = prevCenter - bottom;
                } else if (top >= prevCenter) {
                    distance1 = top - prevCenter;
                }
                distance2 = prevLeft - right;
            }
            if (distance1 < startEventDistance1
                    || (distance1 == startEventDistance1 && distance2 < startEventDistance2)) {
                startEvent = ev;
                startEventDistance1 = distance1;
                startEventDistance2 = distance2;
            }
            for (int jj = 0; jj < len; jj++) {
                if (jj == ii) {
                    continue;
                }
                Event neighbor = mSelectedEvents.get(jj);
                int neighborLeft = (int) neighbor.left;
                int neighborRight = (int) neighbor.right;
                if (neighbor.endTime <= startTime) {
                    if (neighborLeft < right && neighborRight > left) {
                        int distance = startTime - neighbor.endTime;
                        if (distance < upDistanceMin) {
                            upDistanceMin = distance;
                            upEvent = neighbor;
                        } else if (distance == upDistanceMin) {
                            int center = (left + right) / 2;
                            int currentDistance = 0;
                            int currentLeft = (int) upEvent.left;
                            int currentRight = (int) upEvent.right;
                            if (currentRight <= center) {
                                currentDistance = center - currentRight;
                            } else if (currentLeft >= center) {
                                currentDistance = currentLeft - center;
                            }
                            int neighborDistance = 0;
                            if (neighborRight <= center) {
                                neighborDistance = center - neighborRight;
                            } else if (neighborLeft >= center) {
                                neighborDistance = neighborLeft - center;
                            }
                            if (neighborDistance < currentDistance) {
                                upDistanceMin = distance;
                                upEvent = neighbor;
                            }
                        }
                    }
                } else if (neighbor.startTime >= endTime) {
                    if (neighborLeft < right && neighborRight > left) {
                        int distance = neighbor.startTime - endTime;
                        if (distance < downDistanceMin) {
                            downDistanceMin = distance;
                            downEvent = neighbor;
                        } else if (distance == downDistanceMin) {
                            int center = (left + right) / 2;
                            int currentDistance = 0;
                            int currentLeft = (int) downEvent.left;
                            int currentRight = (int) downEvent.right;
                            if (currentRight <= center) {
                                currentDistance = center - currentRight;
                            } else if (currentLeft >= center) {
                                currentDistance = currentLeft - center;
                            }
                            int neighborDistance = 0;
                            if (neighborRight <= center) {
                                neighborDistance = center - neighborRight;
                            } else if (neighborLeft >= center) {
                                neighborDistance = neighborLeft - center;
                            }
                            if (neighborDistance < currentDistance) {
                                downDistanceMin = distance;
                                downEvent = neighbor;
                            }
                        }
                    }
                }
                if (neighborLeft >= right) {
                    int center = (top + bottom) / 2;
                    int distance = 0;
                    int neighborBottom = (int) neighbor.bottom;
                    int neighborTop = (int) neighbor.top;
                    if (neighborBottom <= center) {
                        distance = center - neighborBottom;
                    } else if (neighborTop >= center) {
                        distance = neighborTop - center;
                    }
                    if (distance < rightDistanceMin) {
                        rightDistanceMin = distance;
                        rightEvent = neighbor;
                    } else if (distance == rightDistanceMin) {
                        int neighborDistance = neighborLeft - right;
                        int currentDistance = (int) rightEvent.left - right;
                        if (neighborDistance < currentDistance) {
                            rightDistanceMin = distance;
                            rightEvent = neighbor;
                        }
                    }
                } else if (neighborRight <= left) {
                    int center = (top + bottom) / 2;
                    int distance = 0;
                    int neighborBottom = (int) neighbor.bottom;
                    int neighborTop = (int) neighbor.top;
                    if (neighborBottom <= center) {
                        distance = center - neighborBottom;
                    } else if (neighborTop >= center) {
                        distance = neighborTop - center;
                    }
                    if (distance < leftDistanceMin) {
                        leftDistanceMin = distance;
                        leftEvent = neighbor;
                    } else if (distance == leftDistanceMin) {
                        int neighborDistance = left - neighborRight;
                        int currentDistance = left - (int) leftEvent.right;
                        if (neighborDistance < currentDistance) {
                            leftDistanceMin = distance;
                            leftEvent = neighbor;
                        }
                    }
                }
            }
            ev.nextUp = upEvent;
            ev.nextDown = downEvent;
            ev.nextLeft = leftEvent;
            ev.nextRight = rightEvent;
        }
        mSelectedEvent = startEvent;
    }
    private RectF drawEventRect(Event event, Canvas canvas, Paint p, Paint eventTextPaint) {
        int color = event.color;
        boolean declined = (event.selfAttendeeStatus == Attendees.ATTENDEE_STATUS_DECLINED);
        if (declined) {
            int alpha = color & 0xff000000;
            color &= 0x00ffffff;
            int red = (color & 0x00ff0000) >> 16;
            int green = (color & 0x0000ff00) >> 8;
            int blue = (color & 0x0000ff);
            color = ((red >> 1) << 16) | ((green >> 1) << 8) | (blue >> 1);
            color += 0x7F7F7F + alpha;
        }
        if (mSelectedEvent == event) {
            if (mSelectionMode == SELECTION_PRESSED) {
                mPrevSelectedEvent = event;
                p.setColor(mPressedColor); 
                eventTextPaint.setColor(mSelectedEventTextColor);
            } else if (mSelectionMode == SELECTION_SELECTED) {
                mPrevSelectedEvent = event;
                p.setColor(mSelectionColor);
                eventTextPaint.setColor(mSelectedEventTextColor);
            } else if (mSelectionMode == SELECTION_LONGPRESS) {
                p.setColor(mPressedColor); 
                eventTextPaint.setColor(mSelectedEventTextColor);
            } else {
                p.setColor(color);
                eventTextPaint.setColor(mEventTextColor);
            }
        } else {
            p.setColor(color);
            eventTextPaint.setColor(mEventTextColor);
        }
        RectF rf = mRectF;
        rf.top = event.top;
        rf.bottom = event.bottom;
        rf.left = event.left;
        rf.right = event.right - 1;
        canvas.drawRoundRect(rf, SMALL_ROUND_RADIUS, SMALL_ROUND_RADIUS, p);
        float[] hsv = new float[3];
        Color.colorToHSV(p.getColor(), hsv);
        hsv[1] = 1.0f;
        hsv[2] *= 0.75f;
        mPaintBorder.setColor(Color.HSVToColor(hsv));
        canvas.drawRoundRect(rf, SMALL_ROUND_RADIUS, SMALL_ROUND_RADIUS, mPaintBorder);
        rf.left += 2;
        rf.right -= 2;
        return rf;
    }
    private Pattern drawTextSanitizerFilter = Pattern.compile("[\t\n],");
    private String drawTextSanitizer(String string) {
        Matcher m = drawTextSanitizerFilter.matcher(string);
        string = m.replaceAll(",").replace('\n', ' ').replace('\n', ' ');
        return string;
    }
    private void drawEventText(Event event, RectF rf, Canvas canvas, Paint p, int topMargin) {
        if (!mDrawTextInEventRect) {
            return;
        }
        float width = rf.right - rf.left;
        float height = rf.bottom - rf.top;
        int lineHeight = mEventTextHeight + 1;
        if (width < MIN_CELL_WIDTH_FOR_TEXT || height <= lineHeight) {
            return;
        }
        String text = event.getTitleAndLocation();
        text = drawTextSanitizer(text);
        int len = text.length();
        if (len > MAX_EVENT_TEXT_LEN) {
            text = text.substring(0, MAX_EVENT_TEXT_LEN);
            len = MAX_EVENT_TEXT_LEN;
        }
        p.getTextWidths(text, mCharWidths);
        String fragment = text;
        float top = rf.top + mEventTextAscent + topMargin;
        int start = 0;
        while (start < len && height >= (lineHeight + 1)) {
            boolean lastLine = (height < 2 * lineHeight + 1);
            do {
                char c = text.charAt(start);
                if (c != ' ') break;
                start += 1;
            } while (start < len);
            float sum = 0;
            int end = start;
            for (int ii = start; ii < len; ii++) {
                char c = text.charAt(ii);
                if (c == ' ') {
                    end = ii;
                }
                sum += mCharWidths[ii];
                if (sum > width) {
                    if (end > start && !lastLine) {
                        fragment = text.substring(start, end);
                        start = end;
                        break;
                    }
                    fragment = text.substring(start, ii);
                    start = ii;
                    break;
                }
            }
            if (sum <= width) {
                fragment = text.substring(start, len);
                start = len;
            }
            canvas.drawText(fragment, rf.left + 1, top, p);
            top += lineHeight;
            height -= lineHeight;
        }
    }
    private void updateEventDetails() {
        if (mSelectedEvent == null || mSelectionMode == SELECTION_HIDDEN
                || mSelectionMode == SELECTION_LONGPRESS) {
            mPopup.dismiss();
            return;
        }
        if (mLastPopupEventID == mSelectedEvent.id) {
            return;
        }
        mLastPopupEventID = mSelectedEvent.id;
        getHandler().removeCallbacks(mDismissPopup);
        Event event = mSelectedEvent;
        TextView titleView = (TextView) mPopupView.findViewById(R.id.event_title);
        titleView.setText(event.title);
        ImageView imageView = (ImageView) mPopupView.findViewById(R.id.reminder_icon);
        imageView.setVisibility(event.hasAlarm ? View.VISIBLE : View.GONE);
        imageView = (ImageView) mPopupView.findViewById(R.id.repeat_icon);
        imageView.setVisibility(event.isRepeating ? View.VISIBLE : View.GONE);
        int flags;
        if (event.allDay) {
            flags = DateUtils.FORMAT_UTC | DateUtils.FORMAT_SHOW_DATE |
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL;
        } else {
            flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                    | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL
                    | DateUtils.FORMAT_CAP_NOON_MIDNIGHT;
        }
        if (DateFormat.is24HourFormat(mParentActivity)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        String timeRange = DateUtils.formatDateRange(mParentActivity,
                event.startMillis, event.endMillis, flags);
        TextView timeView = (TextView) mPopupView.findViewById(R.id.time);
        timeView.setText(timeRange);
        TextView whereView = (TextView) mPopupView.findViewById(R.id.where);
        final boolean empty = TextUtils.isEmpty(event.location);
        whereView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) whereView.setText(event.location);
        mPopup.showAtLocation(this, Gravity.BOTTOM | Gravity.LEFT, mHoursWidth, 5);
        postDelayed(mDismissPopup, POPUP_DISMISS_DELAY);
    }
    void doDown(MotionEvent ev) {
        mTouchMode = TOUCH_MODE_DOWN;
        mViewStartX = 0;
        mOnFlingCalled = false;
        getHandler().removeCallbacks(mContinueScroll);
    }
    void doSingleTapUp(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        Event selectedEvent = mSelectedEvent;
        int selectedDay = mSelectionDay;
        int selectedHour = mSelectionHour;
        boolean validPosition = setSelectionFromPosition(x, y);
        if (!validPosition) {
            return;
        }
        mSelectionMode = SELECTION_SELECTED;
        mRedrawScreen = true;
        invalidate();
        boolean launchNewView = false;
        if (mSelectedEvent != null) {
            launchNewView = true;
        } else if (mSelectedEvent == null && selectedDay == mSelectionDay
                && selectedHour == mSelectionHour) {
            launchNewView = true;
        }
        if (launchNewView) {
            switchViews(false );
        }
    }
    void doLongPress(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        boolean validPosition = setSelectionFromPosition(x, y);
        if (!validPosition) {
            return;
        }
        mSelectionMode = SELECTION_LONGPRESS;
        mRedrawScreen = true;
        invalidate();
        performLongClick();
    }
    void doScroll(MotionEvent e1, MotionEvent e2, float deltaX, float deltaY) {
        int distanceX = (int) e1.getX() - (int) e2.getX();
        int distanceY = (int) e1.getY() - (int) e2.getY();
        if (mTouchMode == TOUCH_MODE_DOWN) {
            int absDistanceX = Math.abs(distanceX);
            int absDistanceY = Math.abs(distanceY);
            mScrollStartY = mViewStartY;
            mPreviousDistanceX = 0;
            mPreviousDirection = 0;
            if (absDistanceX >= 2 * absDistanceY) {
                mTouchMode = TOUCH_MODE_HSCROLL;
                mViewStartX = distanceX;
                initNextView(-mViewStartX);
            } else {
                mTouchMode = TOUCH_MODE_VSCROLL;
            }
        } else if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            mViewStartX = distanceX;
            if (distanceX != 0) {
                int direction = (distanceX > 0) ? 1 : -1;
                if (direction != mPreviousDirection) {
                    initNextView(-mViewStartX);
                    mPreviousDirection = direction;
                }
            }
            if (distanceX >= HORIZONTAL_SCROLL_THRESHOLD) {
                if (mPreviousDistanceX < HORIZONTAL_SCROLL_THRESHOLD) {
                    CalendarView view = mParentActivity.getNextView();
                    mTitleTextView.setText(view.mDateRange);
                }
            } else if (distanceX <= -HORIZONTAL_SCROLL_THRESHOLD) {
                if (mPreviousDistanceX > -HORIZONTAL_SCROLL_THRESHOLD) {
                    CalendarView view = mParentActivity.getNextView();
                    mTitleTextView.setText(view.mDateRange);
                }
            } else {
                if (mPreviousDistanceX >= HORIZONTAL_SCROLL_THRESHOLD
                        || mPreviousDistanceX <= -HORIZONTAL_SCROLL_THRESHOLD) {
                    mTitleTextView.setText(mDateRange);
                }
            }
            mPreviousDistanceX = distanceX;
        }
        if ((mTouchMode & TOUCH_MODE_VSCROLL) != 0) {
            mViewStartY = mScrollStartY + distanceY;
            if (mViewStartY < 0) {
                mViewStartY = 0;
            } else if (mViewStartY > mMaxViewStartY) {
                mViewStartY = mMaxViewStartY;
            }
            computeFirstHour();
        }
        mScrolling = true;
        if (mSelectionMode != SELECTION_HIDDEN) {
            mSelectionMode = SELECTION_HIDDEN;
            mRedrawScreen = true;
        }
        invalidate();
    }
    void doFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        mTouchMode = TOUCH_MODE_INITIAL_STATE;
        mSelectionMode = SELECTION_HIDDEN;
        mOnFlingCalled = true;
        int deltaX = (int) e2.getX() - (int) e1.getX();
        int distanceX = Math.abs(deltaX);
        int deltaY = (int) e2.getY() - (int) e1.getY();
        int distanceY = Math.abs(deltaY);
        if ((distanceX >= HORIZONTAL_SCROLL_THRESHOLD) && (distanceX > distanceY)) {
            boolean switchForward = initNextView(deltaX);
            CalendarView view = mParentActivity.getNextView();
            mTitleTextView.setText(view.mDateRange);
            mParentActivity.switchViews(switchForward, mViewStartX, mViewWidth);
            mViewStartX = 0;
            return;
        }
        mContinueScroll.init((int) velocityY / 20);
        post(mContinueScroll);
    }
    private boolean initNextView(int deltaX) {
        CalendarView view = mParentActivity.getNextView();
        Time date = view.mBaseDate;
        date.set(mBaseDate);
        boolean switchForward;
        if (deltaX > 0) {
            date.monthDay -= mNumDays;
            view.mSelectionDay = mSelectionDay - mNumDays;
            switchForward = false;
        } else {
            date.monthDay += mNumDays;
            view.mSelectionDay = mSelectionDay + mNumDays;
            switchForward = true;
        }
        date.normalize(true );
        initView(view);
        view.layout(getLeft(), getTop(), getRight(), getBottom());
        view.reloadEvents();
        return switchForward;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mParentActivity.mGestureDetector.onTouchEvent(ev);
            return true;
        case MotionEvent.ACTION_MOVE:
            mParentActivity.mGestureDetector.onTouchEvent(ev);
            return true;
        case MotionEvent.ACTION_UP:
            mParentActivity.mGestureDetector.onTouchEvent(ev);
            if (mOnFlingCalled) {
                return true;
            }
            if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
                mTouchMode = TOUCH_MODE_INITIAL_STATE;
                if (Math.abs(mViewStartX) > HORIZONTAL_SCROLL_THRESHOLD) {
                    mParentActivity.switchViews(mViewStartX > 0, mViewStartX, mViewWidth);
                    mViewStartX = 0;
                    return true;
                } else {
                    recalc();
                    mTitleTextView.setText(mDateRange);
                    invalidate();
                    mViewStartX = 0;
                }
            }
            if (mScrolling) {
                mScrolling = false;
                resetSelectedHour();
                mRedrawScreen = true;
                invalidate();
            }
            return true;
        case MotionEvent.ACTION_CANCEL:
            mParentActivity.mGestureDetector.onTouchEvent(ev);
            mScrolling = false;
            resetSelectedHour();
            return true;
        default:
            if (mParentActivity.mGestureDetector.onTouchEvent(ev)) {
                return true;
            }
            return super.onTouchEvent(ev);
        }
    }
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        MenuItem item;
        if (mSelectionMode != SELECTION_LONGPRESS) {
            mSelectionMode = SELECTION_LONGPRESS;
            mRedrawScreen = true;
            invalidate();
        }
        final long startMillis = getSelectedTimeInMillis();
        int flags = DateUtils.FORMAT_SHOW_TIME
                | DateUtils.FORMAT_CAP_NOON_MIDNIGHT
                | DateUtils.FORMAT_SHOW_WEEKDAY;
        final String title = DateUtils.formatDateTime(mParentActivity, startMillis, flags);
        menu.setHeaderTitle(title);
        int numSelectedEvents = mSelectedEvents.size();
        if (mNumDays == 1) {
            if (numSelectedEvents >= 1) {
                item = menu.add(0, MenuHelper.MENU_EVENT_VIEW, 0, R.string.event_view);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_info_details);
                int accessLevel = getEventAccessLevel(mParentActivity, mSelectedEvent);
                if (accessLevel == ACCESS_LEVEL_EDIT) {
                    item = menu.add(0, MenuHelper.MENU_EVENT_EDIT, 0, R.string.event_edit);
                    item.setOnMenuItemClickListener(mContextMenuHandler);
                    item.setIcon(android.R.drawable.ic_menu_edit);
                    item.setAlphabeticShortcut('e');
                }
                if (accessLevel >= ACCESS_LEVEL_DELETE) {
                    item = menu.add(0, MenuHelper.MENU_EVENT_DELETE, 0, R.string.event_delete);
                    item.setOnMenuItemClickListener(mContextMenuHandler);
                    item.setIcon(android.R.drawable.ic_menu_delete);
                }
                item = menu.add(0, MenuHelper.MENU_EVENT_CREATE, 0, R.string.event_create);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_add);
                item.setAlphabeticShortcut('n');
            } else {
                item = menu.add(0, MenuHelper.MENU_EVENT_CREATE, 0, R.string.event_create);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_add);
                item.setAlphabeticShortcut('n');
            }
        } else {
            if (numSelectedEvents >= 1) {
                item = menu.add(0, MenuHelper.MENU_EVENT_VIEW, 0, R.string.event_view);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_info_details);
                int accessLevel = getEventAccessLevel(mParentActivity, mSelectedEvent);
                if (accessLevel == ACCESS_LEVEL_EDIT) {
                    item = menu.add(0, MenuHelper.MENU_EVENT_EDIT, 0, R.string.event_edit);
                    item.setOnMenuItemClickListener(mContextMenuHandler);
                    item.setIcon(android.R.drawable.ic_menu_edit);
                    item.setAlphabeticShortcut('e');
                }
                if (accessLevel >= ACCESS_LEVEL_DELETE) {
                    item = menu.add(0, MenuHelper.MENU_EVENT_DELETE, 0, R.string.event_delete);
                    item.setOnMenuItemClickListener(mContextMenuHandler);
                    item.setIcon(android.R.drawable.ic_menu_delete);
                }
                item = menu.add(0, MenuHelper.MENU_EVENT_CREATE, 0, R.string.event_create);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_add);
                item.setAlphabeticShortcut('n');
                item = menu.add(0, MenuHelper.MENU_DAY, 0, R.string.show_day_view);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_day);
                item.setAlphabeticShortcut('d');
                item = menu.add(0, MenuHelper.MENU_AGENDA, 0, R.string.show_agenda_view);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_agenda);
                item.setAlphabeticShortcut('a');
            } else {
                item = menu.add(0, MenuHelper.MENU_EVENT_CREATE, 0, R.string.event_create);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_add);
                item.setAlphabeticShortcut('n');
                item = menu.add(0, MenuHelper.MENU_DAY, 0, R.string.show_day_view);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_day);
                item.setAlphabeticShortcut('d');
                item = menu.add(0, MenuHelper.MENU_AGENDA, 0, R.string.show_agenda_view);
                item.setOnMenuItemClickListener(mContextMenuHandler);
                item.setIcon(android.R.drawable.ic_menu_agenda);
                item.setAlphabeticShortcut('a');
            }
        }
        mPopup.dismiss();
    }
    private class ContextMenuHandler implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MenuHelper.MENU_EVENT_VIEW: {
                    if (mSelectedEvent != null) {
                        long id = mSelectedEvent.id;
                        Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(eventUri);
                        intent.setClassName(mParentActivity, EventInfoActivity.class.getName());
                        intent.putExtra(EVENT_BEGIN_TIME, mSelectedEvent.startMillis);
                        intent.putExtra(EVENT_END_TIME, mSelectedEvent.endMillis);
                        mParentActivity.startActivity(intent);
                    }
                    break;
                }
                case MenuHelper.MENU_EVENT_EDIT: {
                    if (mSelectedEvent != null) {
                        long id = mSelectedEvent.id;
                        Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
                        Intent intent = new Intent(Intent.ACTION_EDIT);
                        intent.setData(eventUri);
                        intent.setClassName(mParentActivity, EditEvent.class.getName());
                        intent.putExtra(EVENT_BEGIN_TIME, mSelectedEvent.startMillis);
                        intent.putExtra(EVENT_END_TIME, mSelectedEvent.endMillis);
                        mParentActivity.startActivity(intent);
                    }
                    break;
                }
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
                    intent.putExtra(EditEvent.EVENT_ALL_DAY, mSelectionAllDay);
                    mParentActivity.startActivity(intent);
                    break;
                }
                case MenuHelper.MENU_EVENT_DELETE: {
                    if (mSelectedEvent != null) {
                        Event selectedEvent = mSelectedEvent;
                        long begin = selectedEvent.startMillis;
                        long end = selectedEvent.endMillis;
                        long id = selectedEvent.id;
                        mDeleteEventHelper.delete(begin, end, id, -1);
                    }
                    break;
                }
                default: {
                    return false;
                }
            }
            return true;
        }
    }
    private static int getEventAccessLevel(Context context, Event e) {
        ContentResolver cr = context.getContentResolver();
        int visibility = Calendars.NO_ACCESS;
        int relationship = Attendees.RELATIONSHIP_ORGANIZER;
        Cursor cursor = cr.query(ContentUris.withAppendedId(Events.CONTENT_URI, e.id),
                new String[] { Events.CALENDAR_ID },
                null ,
                null ,
                null );
        if (cursor == null) {
            return ACCESS_LEVEL_NONE;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return ACCESS_LEVEL_NONE;
        }
        cursor.moveToFirst();
        long calId = cursor.getLong(0);
        cursor.close();
        Uri uri = Calendars.CONTENT_URI;
        String where = String.format(CALENDARS_WHERE, calId);
        cursor = cr.query(uri, CALENDARS_PROJECTION, where, null, null);
        String calendarOwnerAccount = null;
        if (cursor != null) {
            cursor.moveToFirst();
            visibility = cursor.getInt(CALENDARS_INDEX_ACCESS_LEVEL);
            calendarOwnerAccount = cursor.getString(CALENDARS_INDEX_OWNER_ACCOUNT);
            cursor.close();
        }
        if (visibility < Calendars.CONTRIBUTOR_ACCESS) {
            return ACCESS_LEVEL_NONE;
        }
        if (e.guestsCanModify) {
            return ACCESS_LEVEL_EDIT;
        }
        if (!TextUtils.isEmpty(calendarOwnerAccount) &&
                calendarOwnerAccount.equalsIgnoreCase(e.organizer)) {
            return ACCESS_LEVEL_EDIT;
        }
        return ACCESS_LEVEL_DELETE;
    }
    private boolean setSelectionFromPosition(int x, int y) {
        if (x < mHoursWidth) {
            return false;
        }
        int day = (x - mHoursWidth) / (mCellWidth + DAY_GAP);
        if (day >= mNumDays) {
            day = mNumDays - 1;
        }
        day += mFirstJulianDay;
        int hour;
        if (y < mFirstCell + mFirstHourOffset) {
            mSelectionAllDay = true;
        } else {
            hour = (y - mFirstCell - mFirstHourOffset) / (mCellHeight + HOUR_GAP);
            hour += mFirstHour;
            mSelectionHour = hour;
            mSelectionAllDay = false;
        }
        mSelectionDay = day;
        findSelectedEvent(x, y);
        return true;
    }
    private void findSelectedEvent(int x, int y) {
        int date = mSelectionDay;
        int cellWidth = mCellWidth;
        ArrayList<Event> events = mEvents;
        int numEvents = events.size();
        int left = mHoursWidth + (mSelectionDay - mFirstJulianDay) * (cellWidth + DAY_GAP);
        int top = 0;
        mSelectedEvent = null;
        mSelectedEvents.clear();
        if (mSelectionAllDay) {
            float yDistance;
            float minYdistance = 10000.0f;  
            Event closestEvent = null;
            float drawHeight = mAllDayHeight;
            int yOffset = mBannerPlusMargin + ALLDAY_TOP_MARGIN;
            for (int i = 0; i < numEvents; i++) {
                Event event = events.get(i);
                if (!event.allDay) {
                    continue;
                }
                if (event.startDay <= mSelectionDay && event.endDay >= mSelectionDay) {
                    float numRectangles = event.getMaxColumns();
                    float height = drawHeight / numRectangles;
                    if (height > MAX_ALLDAY_EVENT_HEIGHT) {
                        height = MAX_ALLDAY_EVENT_HEIGHT;
                    }
                    float eventTop = yOffset + height * event.getColumn();
                    float eventBottom = eventTop + height;
                    if (eventTop < y && eventBottom > y) {
                        mSelectedEvents.add(event);
                        closestEvent = event;
                        break;
                    } else {
                        if (eventTop >= y) {
                            yDistance = eventTop - y;
                        } else {
                            yDistance = y - eventBottom;
                        }
                        if (yDistance < minYdistance) {
                            minYdistance = yDistance;
                            closestEvent = event;
                        }
                    }
                }
            }
            mSelectedEvent = closestEvent;
            return;
        }
        y += mViewStartY - mFirstCell;
        Rect region = mRect;
        region.left = x - 10;
        region.right = x + 10;
        region.top = y - 10;
        region.bottom = y + 10;
        EventGeometry geometry = mEventGeometry;
        for (int i = 0; i < numEvents; i++) {
            Event event = events.get(i);
            if (!geometry.computeEventRect(date, left, top, cellWidth, event)) {
                continue;
            }
            if (geometry.eventIntersectsSelection(event, region)) {
                mSelectedEvents.add(event);
            }
        }
        if (mSelectedEvents.size() > 0) {
            int len = mSelectedEvents.size();
            Event closestEvent = null;
            float minDist = mViewWidth + mViewHeight;  
            for (int index = 0; index < len; index++) {
                Event ev = mSelectedEvents.get(index);
                float dist = geometry.pointToEvent(x, y, ev);
                if (dist < minDist) {
                    minDist = dist;
                    closestEvent = ev;
                }
            }
            mSelectedEvent = closestEvent;
            int startDay = mSelectedEvent.startDay;
            int endDay = mSelectedEvent.endDay;
            if (mSelectionDay < startDay) {
                mSelectionDay = startDay;
            } else if (mSelectionDay > endDay) {
                mSelectionDay = endDay;
            }
            int startHour = mSelectedEvent.startTime / 60;
            int endHour;
            if (mSelectedEvent.startTime < mSelectedEvent.endTime) {
                endHour = (mSelectedEvent.endTime - 1) / 60;
            } else {
                endHour = mSelectedEvent.endTime / 60;
            }
            if (mSelectionHour < startHour) {
                mSelectionHour = startHour;
            } else if (mSelectionHour > endHour) {
                mSelectionHour = endHour;
            }
        }
    }
    private class ContinueScroll implements Runnable {
        int mSignDeltaY;
        int mAbsDeltaY;
        float mFloatDeltaY;
        long mFreeSpinTime;
        private static final float FRICTION_COEF = 0.7F;
        private static final long FREE_SPIN_MILLIS = 180;
        private static final int MAX_DELTA = 60;
        private static final int SCROLL_REPEAT_INTERVAL = 30;
        public void init(int deltaY) {
            mSignDeltaY = 0;
            if (deltaY > 0) {
                mSignDeltaY = 1;
            } else if (deltaY < 0) {
                mSignDeltaY = -1;
            }
            mAbsDeltaY = Math.abs(deltaY);
            if (mAbsDeltaY > MAX_DELTA) {
                mAbsDeltaY = MAX_DELTA;
            }
            mFloatDeltaY = mAbsDeltaY;
            mFreeSpinTime = System.currentTimeMillis() + FREE_SPIN_MILLIS;
        }
        public void run() {
            long time = System.currentTimeMillis();
            if (time > mFreeSpinTime) {
                if (mAbsDeltaY <= 10) {
                    mAbsDeltaY -= 2;
                } else {
                    mFloatDeltaY *= FRICTION_COEF;
                    mAbsDeltaY = (int) mFloatDeltaY;
                }
                if (mAbsDeltaY < 0) {
                    mAbsDeltaY = 0;
                }
            }
            if (mSignDeltaY == 1) {
                mViewStartY -= mAbsDeltaY;
            } else {
                mViewStartY += mAbsDeltaY;
            }
            if (mViewStartY < 0) {
                mViewStartY = 0;
                mAbsDeltaY = 0;
            } else if (mViewStartY > mMaxViewStartY) {
                mViewStartY = mMaxViewStartY;
                mAbsDeltaY = 0;
            }
            computeFirstHour();
            if (mAbsDeltaY > 0) {
                postDelayed(this, SCROLL_REPEAT_INTERVAL);
            } else {
                mScrolling = false;
                resetSelectedHour();
                mRedrawScreen = true;
            }
            invalidate();
        }
    }
    public void cleanup() {
        if (mPopup != null) {
            mPopup.dismiss();
        }
        mLastPopupEventID = INVALID_EVENT_ID;
        Handler handler = getHandler();
        if (handler != null) {
            handler.removeCallbacks(mDismissPopup);
            handler.removeCallbacks(mUpdateCurrentTime);
        }
        mRemeasure = false;
        mRedrawScreen = false;
    }
    public void restartCurrentTimeUpdates() {
        post(mUpdateCurrentTime);
    }
    @Override protected void onDetachedFromWindow() {
        cleanup();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        super.onDetachedFromWindow();
    }
    class DismissPopup implements Runnable {
        public void run() {
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    }
    class UpdateCurrentTime implements Runnable {
        public void run() {
            long currentTime = System.currentTimeMillis();
            mCurrentTime.set(currentTime);
            postDelayed(mUpdateCurrentTime,
                    UPDATE_CURRENT_TIME_DELAY - (currentTime % UPDATE_CURRENT_TIME_DELAY));
            mTodayJulianDay = Time.getJulianDay(currentTime, mCurrentTime.gmtoff);
            mRedrawScreen = true;
            invalidate();
        }
    }
}
