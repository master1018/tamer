public class StatusBarService extends IStatusBar.Stub
{
    static final String TAG = "StatusBar";
    static final boolean SPEW = false;
    static final int EXPANDED_LEAVE_ALONE = -10000;
    static final int EXPANDED_FULL_OPEN = -10001;
    private static final int MSG_ANIMATE = 1000;
    private static final int MSG_ANIMATE_REVEAL = 1001;
    private static final int OP_ADD_ICON = 1;
    private static final int OP_UPDATE_ICON = 2;
    private static final int OP_REMOVE_ICON = 3;
    private static final int OP_SET_VISIBLE = 4;
    private static final int OP_EXPAND = 5;
    private static final int OP_TOGGLE = 6;
    private static final int OP_DISABLE = 7;
    private class PendingOp {
        IBinder key;
        int code;
        IconData iconData;
        NotificationData notificationData;
        boolean visible;
        int integer;
    }
    private class DisableRecord implements IBinder.DeathRecipient {
        String pkg;
        int what;
        IBinder token;
        public void binderDied() {
            Slog.i(TAG, "binder died for pkg=" + pkg);
            disable(0, token, pkg);
            token.unlinkToDeath(this, 0);
        }
    }
    public interface NotificationCallbacks {
        void onSetDisabled(int status);
        void onClearAll();
        void onNotificationClick(String pkg, String tag, int id);
        void onPanelRevealed();
    }
    private class ExpandedDialog extends Dialog {
        ExpandedDialog(Context context) {
            super(context, com.android.internal.R.style.Theme_Light_NoTitleBar);
        }
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (!down) {
                    StatusBarService.this.deactivate();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }
    final Context mContext;
    final Display mDisplay;
    StatusBarView mStatusBarView;
    int mPixelFormat;
    H mHandler = new H();
    Object mQueueLock = new Object();
    ArrayList<PendingOp> mQueue = new ArrayList<PendingOp>();
    NotificationCallbacks mNotificationCallbacks;
    HashMap<IBinder,StatusBarIcon> mIconMap = new HashMap<IBinder,StatusBarIcon>();
    ArrayList<StatusBarIcon> mIconList = new ArrayList<StatusBarIcon>();
    String[] mRightIconSlots;
    StatusBarIcon[] mRightIcons;
    LinearLayout mIcons;
    IconMerger mNotificationIcons;
    LinearLayout mStatusIcons;
    StatusBarIcon mMoreIcon;
    private UninstallReceiver mUninstallReceiver;
    NotificationViewList mNotificationData = new NotificationViewList();
    Dialog mExpandedDialog;
    ExpandedView mExpandedView;
    WindowManager.LayoutParams mExpandedParams;
    ScrollView mScrollView;
    View mNotificationLinearLayout;
    TextView mOngoingTitle;
    LinearLayout mOngoingItems;
    TextView mLatestTitle;
    LinearLayout mLatestItems;
    TextView mNoNotificationsTitle;
    TextView mSpnLabel;
    TextView mPlmnLabel;
    TextView mClearButton;
    View mExpandedContents;
    CloseDragHandle mCloseView;
    int[] mPositionTmp = new int[2];
    boolean mExpanded;
    boolean mExpandedVisible;
    DateView mDateView;
    TrackingView mTrackingView;
    WindowManager.LayoutParams mTrackingParams;
    int mTrackingPosition; 
    private Ticker mTicker;
    private View mTickerView;
    private boolean mTicking;
    int mEdgeBorder; 
    boolean mTracking;
    VelocityTracker mVelocityTracker;
    static final int ANIM_FRAME_DURATION = (1000/60);
    boolean mAnimating;
    long mCurAnimationTime;
    float mDisplayHeight;
    float mAnimY;
    float mAnimVel;
    float mAnimAccel;
    long mAnimLastTime;
    boolean mAnimatingReveal = false;
    int mViewDelta;
    int[] mAbsPos = new int[2];
    ArrayList<DisableRecord> mDisableRecords = new ArrayList<DisableRecord>();
    int mDisabled = 0;
    public StatusBarService(Context context) {
        mContext = context;
        mDisplay = ((WindowManager)context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();
        makeStatusBarView(context);
        mUninstallReceiver = new UninstallReceiver();
    }
    public void setNotificationCallbacks(NotificationCallbacks listener) {
        mNotificationCallbacks = listener;
    }
    private void makeStatusBarView(Context context) {
        Resources res = context.getResources();
        mRightIconSlots = res.getStringArray(com.android.internal.R.array.status_bar_icon_order);
        mRightIcons = new StatusBarIcon[mRightIconSlots.length];
        ExpandedView expanded = (ExpandedView)View.inflate(context,
                com.android.internal.R.layout.status_bar_expanded, null);
        expanded.mService = this;
        StatusBarView sb = (StatusBarView)View.inflate(context,
                com.android.internal.R.layout.status_bar, null);
        sb.mService = this;
        mPixelFormat = PixelFormat.TRANSLUCENT;
        Drawable bg = sb.getBackground();
        if (bg != null) {
            mPixelFormat = bg.getOpacity();
        }
        mStatusBarView = sb;
        mStatusIcons = (LinearLayout)sb.findViewById(R.id.statusIcons);
        mNotificationIcons = (IconMerger)sb.findViewById(R.id.notificationIcons);
        mNotificationIcons.service = this;
        mIcons = (LinearLayout)sb.findViewById(R.id.icons);
        mTickerView = sb.findViewById(R.id.ticker);
        mDateView = (DateView)sb.findViewById(R.id.date);
        mExpandedDialog = new ExpandedDialog(context);
        mExpandedView = expanded;
        mExpandedContents = expanded.findViewById(R.id.notificationLinearLayout);
        mOngoingTitle = (TextView)expanded.findViewById(R.id.ongoingTitle);
        mOngoingItems = (LinearLayout)expanded.findViewById(R.id.ongoingItems);
        mLatestTitle = (TextView)expanded.findViewById(R.id.latestTitle);
        mLatestItems = (LinearLayout)expanded.findViewById(R.id.latestItems);
        mNoNotificationsTitle = (TextView)expanded.findViewById(R.id.noNotificationsTitle);
        mClearButton = (TextView)expanded.findViewById(R.id.clear_all_button);
        mClearButton.setOnClickListener(mClearButtonListener);
        mSpnLabel = (TextView)expanded.findViewById(R.id.spnLabel);
        mPlmnLabel = (TextView)expanded.findViewById(R.id.plmnLabel);
        mScrollView = (ScrollView)expanded.findViewById(R.id.scroll);
        mNotificationLinearLayout = expanded.findViewById(R.id.notificationLinearLayout);
        mOngoingTitle.setVisibility(View.GONE);
        mLatestTitle.setVisibility(View.GONE);
        mTicker = new MyTicker(context, sb);
        TickerView tickerView = (TickerView)sb.findViewById(R.id.tickerText);
        tickerView.mTicker = mTicker;
        mTrackingView = (TrackingView)View.inflate(context,
                com.android.internal.R.layout.status_bar_tracking, null);
        mTrackingView.mService = this;
        mCloseView = (CloseDragHandle)mTrackingView.findViewById(R.id.close);
        mCloseView.mService = this;
        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
        IconData moreData = IconData.makeIcon(null, context.getPackageName(),
                R.drawable.stat_notify_more, 0, 42);
        mMoreIcon = new StatusBarIcon(context, moreData, mNotificationIcons);
        mMoreIcon.view.setId(R.drawable.stat_notify_more);
        mNotificationIcons.moreIcon = mMoreIcon;
        mNotificationIcons.addView(mMoreIcon.view);
        setAreThereNotifications();
        mDateView.setVisibility(View.INVISIBLE);
        mPlmnLabel.setText(R.string.lockscreen_carrier_default);
        mPlmnLabel.setVisibility(View.VISIBLE);
        mSpnLabel.setText("");
        mSpnLabel.setVisibility(View.GONE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION);
        context.registerReceiver(mBroadcastReceiver, filter);
    }
    public void systemReady() {
        final StatusBarView view = mStatusBarView;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                view.getContext().getResources().getDimensionPixelSize(
                        com.android.internal.R.dimen.status_bar_height),
                WindowManager.LayoutParams.TYPE_STATUS_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING,
                mPixelFormat);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("StatusBar");
        lp.windowAnimations = R.style.Animation_StatusBar;
        WindowManagerImpl.getDefault().addView(view, lp);
    }
    public void activate() {
        enforceExpandStatusBar();
        addPendingOp(OP_EXPAND, null, true);
    }
    public void deactivate() {
        enforceExpandStatusBar();
        addPendingOp(OP_EXPAND, null, false);
    }
    public void toggle() {
        enforceExpandStatusBar();
        addPendingOp(OP_TOGGLE, null, false);
    }
    public void disable(int what, IBinder token, String pkg) {
        enforceStatusBar();
        synchronized (mNotificationCallbacks) {
            int net;
            synchronized (mDisableRecords) {
                manageDisableListLocked(what, token, pkg);
                net = gatherDisableActionsLocked();
                mNotificationCallbacks.onSetDisabled(net);
            }
            addPendingOp(OP_DISABLE, net);
        }
    }
    public IBinder addIcon(String slot, String iconPackage, int iconId, int iconLevel) {
        enforceStatusBar();
        return addIcon(IconData.makeIcon(slot, iconPackage, iconId, iconLevel, 0), null);
    }
    public void updateIcon(IBinder key,
            String slot, String iconPackage, int iconId, int iconLevel) {
        enforceStatusBar();
        updateIcon(key, IconData.makeIcon(slot, iconPackage, iconId, iconLevel, 0), null);
    }
    public void removeIcon(IBinder key) {
        enforceStatusBar();
        addPendingOp(OP_REMOVE_ICON, key, null, null, -1);
    }
    private void enforceStatusBar() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.STATUS_BAR,
                "StatusBarService");
    }
    private void enforceExpandStatusBar() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.EXPAND_STATUS_BAR,
                "StatusBarService");
    }
    public IBinder addIcon(IconData data, NotificationData n) {
        int slot;
        if (data != null && n == null) {
            slot = getRightIconIndex(data.slot);
            if (slot < 0) {
                throw new SecurityException("invalid status bar icon slot: "
                        + (data.slot != null ? "'" + data.slot + "'" : "null"));
            }
        } else {
            slot = -1;
        }
        IBinder key = new Binder();
        addPendingOp(OP_ADD_ICON, key, data, n, -1);
        return key;
    }
    public void updateIcon(IBinder key, IconData data, NotificationData n) {
        addPendingOp(OP_UPDATE_ICON, key, data, n, -1);
    }
    public void setIconVisibility(IBinder key, boolean visible) {
        addPendingOp(OP_SET_VISIBLE, key, visible);
    }
    private void addPendingOp(int code, IBinder key, IconData data, NotificationData n, int i) {
        synchronized (mQueueLock) {
            PendingOp op = new PendingOp();
            op.key = key;
            op.code = code;
            op.iconData = data == null ? null : data.clone();
            op.notificationData = n;
            op.integer = i;
            mQueue.add(op);
            if (mQueue.size() == 1) {
                mHandler.sendEmptyMessage(2);
            }
        }
    }
    private void addPendingOp(int code, IBinder key, boolean visible) {
        synchronized (mQueueLock) {
            PendingOp op = new PendingOp();
            op.key = key;
            op.code = code;
            op.visible = visible;
            mQueue.add(op);
            if (mQueue.size() == 1) {
                mHandler.sendEmptyMessage(1);
            }
        }
    }
    private void addPendingOp(int code, int integer) {
        synchronized (mQueueLock) {
            PendingOp op = new PendingOp();
            op.code = code;
            op.integer = integer;
            mQueue.add(op);
            if (mQueue.size() == 1) {
                mHandler.sendEmptyMessage(1);
            }
        }
    }
    void manageDisableListLocked(int what, IBinder token, String pkg) {
        if (SPEW) {
            Slog.d(TAG, "manageDisableList what=0x" + Integer.toHexString(what)
                    + " pkg=" + pkg);
        }
        synchronized (mDisableRecords) {
            final int N = mDisableRecords.size();
            DisableRecord tok = null;
            int i;
            for (i=0; i<N; i++) {
                DisableRecord t = mDisableRecords.get(i);
                if (t.token == token) {
                    tok = t;
                    break;
                }
            }
            if (what == 0 || !token.isBinderAlive()) {
                if (tok != null) {
                    mDisableRecords.remove(i);
                    tok.token.unlinkToDeath(tok, 0);
                }
            } else {
                if (tok == null) {
                    tok = new DisableRecord();
                    try {
                        token.linkToDeath(tok, 0);
                    }
                    catch (RemoteException ex) {
                        return; 
                    }
                    mDisableRecords.add(tok);
                }
                tok.what = what;
                tok.token = token;
                tok.pkg = pkg;
            }
        }
    }
    int gatherDisableActionsLocked() {
        final int N = mDisableRecords.size();
        int net = 0;
        for (int i=0; i<N; i++) {
            net |= mDisableRecords.get(i).what;
        }
        return net;
    }
    private int getRightIconIndex(String slot) {
        final int N = mRightIconSlots.length;
        for (int i=0; i<N; i++) {
            if (mRightIconSlots[i].equals(slot)) {
                return i;
            }
        }
        return -1;
    }
    private class H extends Handler {
        public void handleMessage(Message m) {
            if (m.what == MSG_ANIMATE) {
                doAnimation();
                return;
            }
            if (m.what == MSG_ANIMATE_REVEAL) {
                doRevealAnimation();
                return;
            }
            ArrayList<PendingOp> queue;
            synchronized (mQueueLock) {
                queue = mQueue;
                mQueue = new ArrayList<PendingOp>();
            }
            boolean wasExpanded = mExpanded;
            boolean expand = wasExpanded;
            boolean doExpand = false;
            boolean doDisable = false;
            int disableWhat = 0;
            int N = queue.size();
            while (N > 0) {
                PendingOp op = queue.get(0);
                boolean doOp = false;
                boolean visible = false;
                boolean doVisibility = false;
                if (op.code == OP_SET_VISIBLE) {
                    doVisibility = true;
                    visible = op.visible;
                }
                else if (op.code == OP_EXPAND) {
                    doExpand = true;
                    expand = op.visible;
                }
                else if (op.code == OP_TOGGLE) {
                    doExpand = true;
                    expand = !expand;
                }
                else {
                    doOp = true;
                }
                if (alwaysHandle(op.code)) {
                    for (int i=1; i<N; i++) {
                        PendingOp o = queue.get(i);
                        if (!alwaysHandle(o.code) && o.key == op.key) {
                            if (o.code == OP_SET_VISIBLE) {
                                visible = o.visible;
                                doVisibility = true;
                            }
                            else if (o.code == OP_EXPAND) {
                                expand = o.visible;
                                doExpand = true;
                            }
                            else {
                                op.code = o.code;
                                op.iconData = o.iconData;
                                op.notificationData = o.notificationData;
                            }
                            queue.remove(i);
                            i--;
                            N--;
                        }
                    }
                }
                queue.remove(0);
                N--;
                if (doOp) {
                    switch (op.code) {
                        case OP_ADD_ICON:
                        case OP_UPDATE_ICON:
                            performAddUpdateIcon(op.key, op.iconData, op.notificationData);
                            break;
                        case OP_REMOVE_ICON:
                            performRemoveIcon(op.key);
                            break;
                        case OP_DISABLE:
                            doDisable = true;
                            disableWhat = op.integer;
                            break;
                    }
                }
                if (doVisibility && op.code != OP_REMOVE_ICON) {
                    performSetIconVisibility(op.key, visible);
                }
            }
            if (queue.size() != 0) {
                throw new RuntimeException("Assertion failed: queue.size=" + queue.size());
            }
            if (doExpand) {
                if (expand) {
                    animateExpand();
                } else {
                    animateCollapse();
                }
            }
            if (doDisable) {
                performDisableActions(disableWhat);
            }
        }
    }
    private boolean alwaysHandle(int code) {
        return code == OP_DISABLE;
    }
     void performAddUpdateIcon(IBinder key, IconData data, NotificationData n)
                        throws StatusBarException {
        if (SPEW) {
            Slog.d(TAG, "performAddUpdateIcon icon=" + data + " notification=" + n + " key=" + key);
        }
        if (n != null) {
            StatusBarNotification notification = getNotification(key);
            NotificationData oldData = null;
            if (notification == null) {
                notification = new StatusBarNotification();
                notification.key = key;
                notification.data = n;
                synchronized (mNotificationData) {
                    mNotificationData.add(notification);
                }
                addNotificationView(notification);
                setAreThereNotifications();
            } else {
                oldData = notification.data;
                notification.data = n;
                updateNotificationView(notification, oldData);
            }
            if (n.tickerText != null && mStatusBarView.getWindowToken() != null
                    && (oldData == null
                        || oldData.tickerText == null
                        || !CharSequences.equals(oldData.tickerText, n.tickerText))) {
                if (0 == (mDisabled & 
                    (StatusBarManager.DISABLE_NOTIFICATION_ICONS | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                    mTicker.addEntry(n, StatusBarIcon.getIcon(mContext, data), n.tickerText);
                }
            }
            updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
        }
        synchronized (mIconMap) {
            StatusBarIcon icon = mIconMap.get(key);
            if (icon == null) {
                LinearLayout v = n == null ? mStatusIcons : mNotificationIcons;
                icon = new StatusBarIcon(mContext, data, v);
                mIconMap.put(key, icon);
                mIconList.add(icon);
                if (n == null) {
                    int slotIndex = getRightIconIndex(data.slot);
                    StatusBarIcon[] rightIcons = mRightIcons;
                    if (rightIcons[slotIndex] == null) {
                        int pos = 0;
                        for (int i=mRightIcons.length-1; i>slotIndex; i--) {
                            StatusBarIcon ic = rightIcons[i];
                            if (ic != null) {
                                pos++;
                            }
                        }
                        rightIcons[slotIndex] = icon;
                        mStatusIcons.addView(icon.view, pos);
                    } else {
                        Slog.e(TAG, "duplicate icon in slot " + slotIndex + "/" + data.slot);
                        mIconMap.remove(key);
                        mIconList.remove(icon);
                        return ;
                    }
                } else {
                    int iconIndex = mNotificationData.getIconIndex(n);
                    mNotificationIcons.addView(icon.view, iconIndex);
                }
            } else {
                if (n == null) {
                    icon.update(mContext, data);
                } else {
                    ViewGroup parent = (ViewGroup)icon.view.getParent();
                    parent.removeView(icon.view);
                    icon.update(mContext, data);
                    int iconIndex = mNotificationData.getIconIndex(n);
                    mNotificationIcons.addView(icon.view, iconIndex);
                }
            }
        }
    }
     void performSetIconVisibility(IBinder key, boolean visible) {
        synchronized (mIconMap) {
            if (SPEW) {
                Slog.d(TAG, "performSetIconVisibility key=" + key + " visible=" + visible);
            }
            StatusBarIcon icon = mIconMap.get(key);
            icon.view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
     void performRemoveIcon(IBinder key) {
        synchronized (this) {
            if (SPEW) {
                Slog.d(TAG, "performRemoveIcon key=" + key);
            }
            StatusBarIcon icon = mIconMap.remove(key);
            mIconList.remove(icon);
            if (icon != null) {
                ViewGroup parent = (ViewGroup)icon.view.getParent();
                parent.removeView(icon.view);
                int slotIndex = getRightIconIndex(icon.mData.slot);
                if (slotIndex >= 0) {
                    mRightIcons[slotIndex] = null;
                }
            }
            StatusBarNotification notification = getNotification(key);
            if (notification != null) {
                removeNotificationView(notification);
                synchronized (mNotificationData) {
                    mNotificationData.remove(notification);
                }
                setAreThereNotifications();
            }
        }
    }
    int getIconNumberForView(View v) {
        synchronized (mIconMap) {
            StatusBarIcon icon = null;
            final int N = mIconList.size();
            for (int i=0; i<N; i++) {
                StatusBarIcon ic = mIconList.get(i);
                if (ic.view == v) {
                    icon = ic;
                    break;
                }
            }
            if (icon != null) {
                return icon.getNumber();
            } else {
                return -1;
            }
        }
    }
    StatusBarNotification getNotification(IBinder key) {
        synchronized (mNotificationData) {
            return mNotificationData.get(key);
        }
    }
    View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            v.setSelected(hasFocus);
        }
    };
    View makeNotificationView(StatusBarNotification notification, ViewGroup parent) {
        NotificationData n = notification.data;
        RemoteViews remoteViews = n.contentView;
        if (remoteViews == null) {
            return null;
        }
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(com.android.internal.R.layout.status_bar_latest_event, parent, false);
        ViewGroup content = (ViewGroup)row.findViewById(com.android.internal.R.id.content);
        content.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        content.setOnFocusChangeListener(mFocusChangeListener);
        PendingIntent contentIntent = n.contentIntent;
        if (contentIntent != null) {
            content.setOnClickListener(new Launcher(contentIntent, n.pkg, n.tag, n.id));
        }
        View child = null;
        Exception exception = null;
        try {
            child = remoteViews.apply(mContext, content);
        }
        catch (RuntimeException e) {
            exception = e;
        }
        if (child == null) {
            Slog.e(TAG, "couldn't inflate view for package " + n.pkg, exception);
            return null;
        }
        content.addView(child);
        row.setDrawingCacheEnabled(true);
        notification.view = row;
        notification.contentView = child;
        return row;
    }
    void addNotificationView(StatusBarNotification notification) {
        if (notification.view != null) {
            throw new RuntimeException("Assertion failed: notification.view="
                    + notification.view);
        }
        LinearLayout parent = notification.data.ongoingEvent ? mOngoingItems : mLatestItems;
        View child = makeNotificationView(notification, parent);
        if (child == null) {
            return ;
        }
        int index = mNotificationData.getExpandedIndex(notification);
        parent.addView(child, index);
    }
    void updateNotificationView(StatusBarNotification notification, NotificationData oldData) {
        NotificationData n = notification.data;
        if (oldData != null && n != null
                && n.when == oldData.when
                && n.ongoingEvent == oldData.ongoingEvent
                && n.contentView != null && oldData.contentView != null
                && n.contentView.getPackage() != null
                && oldData.contentView.getPackage() != null
                && oldData.contentView.getPackage().equals(n.contentView.getPackage())
                && oldData.contentView.getLayoutId() == n.contentView.getLayoutId()
                && notification.view != null) {
            mNotificationData.update(notification);
            try {
                n.contentView.reapply(mContext, notification.contentView);
                ViewGroup content = (ViewGroup)notification.view.findViewById(
                        com.android.internal.R.id.content);
                PendingIntent contentIntent = n.contentIntent;
                if (contentIntent != null) {
                    content.setOnClickListener(new Launcher(contentIntent, n.pkg, n.tag, n.id));
                }
            }
            catch (RuntimeException e) {
                Slog.w(TAG, "couldn't reapply views for package " + n.contentView.getPackage(), e);
                removeNotificationView(notification);
            }
        } else {
            mNotificationData.update(notification);
            removeNotificationView(notification);
            addNotificationView(notification);
        }
        setAreThereNotifications();
    }
    void removeNotificationView(StatusBarNotification notification) {
        View v = notification.view;
        if (v != null) {
            ViewGroup parent = (ViewGroup)v.getParent();
            parent.removeView(v);
            notification.view = null;
        }
    }
    private void setAreThereNotifications() {
        boolean ongoing = mOngoingItems.getChildCount() != 0;
        boolean latest = mLatestItems.getChildCount() != 0;
        if (mNotificationData.hasClearableItems()) {
            mClearButton.setVisibility(View.VISIBLE);
        } else {
            mClearButton.setVisibility(View.INVISIBLE);
        }
        mOngoingTitle.setVisibility(ongoing ? View.VISIBLE : View.GONE);
        mLatestTitle.setVisibility(latest ? View.VISIBLE : View.GONE);
        if (ongoing || latest) {
            mNoNotificationsTitle.setVisibility(View.GONE);
        } else {
            mNoNotificationsTitle.setVisibility(View.VISIBLE);
        }
    }
    private void makeExpandedVisible() {
        if (SPEW) Slog.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
        if (mExpandedVisible) {
            return;
        }
        mExpandedVisible = true;
        panelSlightlyVisible(true);
        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
        mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mExpandedDialog.getWindow().setAttributes(mExpandedParams);
        mExpandedView.requestFocus(View.FOCUS_FORWARD);
        mTrackingView.setVisibility(View.VISIBLE);
        if (!mTicking) {
            setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
        }
    }
    void animateExpand() {
        if (SPEW) Slog.d(TAG, "Animate expand: expanded=" + mExpanded);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }
        if (mExpanded) {
            return;
        }
        prepareTracking(0, true);
        performFling(0, 2000.0f, true);
    }
    void animateCollapse() {
        if (SPEW) {
            Slog.d(TAG, "animateCollapse(): mExpanded=" + mExpanded
                    + " mExpandedVisible=" + mExpandedVisible
                    + " mExpanded=" + mExpanded
                    + " mAnimating=" + mAnimating
                    + " mAnimY=" + mAnimY
                    + " mAnimVel=" + mAnimVel);
        }
        if (!mExpandedVisible) {
            return;
        }
        int y;
        if (mAnimating) {
            y = (int)mAnimY;
        } else {
            y = mDisplay.getHeight()-1;
        }
        mExpanded = true;
        prepareTracking(y, false);
        performFling(y, -2000.0f, true);
    }
    void performExpand() {
        if (SPEW) Slog.d(TAG, "performExpand: mExpanded=" + mExpanded);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }
        if (mExpanded) {
            return;
        }
        if (false) {
            synchronized (mNotificationData) {
                if (mNotificationData.size() == 0) {
                    return;
                }
            }
        }
        mExpanded = true;
        makeExpandedVisible();
        updateExpandedViewPos(EXPANDED_FULL_OPEN);
        if (false) postStartTracing();
    }
    void performCollapse() {
        if (SPEW) Slog.d(TAG, "performCollapse: mExpanded=" + mExpanded
                + " mExpandedVisible=" + mExpandedVisible);
        if (!mExpandedVisible) {
            return;
        }
        mExpandedVisible = false;
        panelSlightlyVisible(false);
        mExpandedParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mExpandedParams.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        mExpandedDialog.getWindow().setAttributes(mExpandedParams);
        mTrackingView.setVisibility(View.GONE);
        if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
            setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
        }
        setDateViewVisibility(false, com.android.internal.R.anim.fade_out);
        if (!mExpanded) {
            return;
        }
        mExpanded = false;
    }
    void doAnimation() {
        if (mAnimating) {
            if (SPEW) Slog.d(TAG, "doAnimation");
            if (SPEW) Slog.d(TAG, "doAnimation before mAnimY=" + mAnimY);
            incrementAnim();
            if (SPEW) Slog.d(TAG, "doAnimation after  mAnimY=" + mAnimY);
            if (mAnimY >= mDisplay.getHeight()-1) {
                if (SPEW) Slog.d(TAG, "Animation completed to expanded state.");
                mAnimating = false;
                updateExpandedViewPos(EXPANDED_FULL_OPEN);
                performExpand();
            }
            else if (mAnimY < mStatusBarView.getHeight()) {
                if (SPEW) Slog.d(TAG, "Animation completed to collapsed state.");
                mAnimating = false;
                updateExpandedViewPos(0);
                performCollapse();
            }
            else {
                updateExpandedViewPos((int)mAnimY);
                mCurAnimationTime += ANIM_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
            }
        }
    }
    void stopTracking() {
        mTracking = false;
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }
    void incrementAnim() {
        long now = SystemClock.uptimeMillis();
        float t = ((float)(now - mAnimLastTime)) / 1000;            
        final float y = mAnimY;
        final float v = mAnimVel;                                   
        final float a = mAnimAccel;                                 
        mAnimY = y + (v*t) + (0.5f*a*t*t);                          
        mAnimVel = v + (a*t);                                       
        mAnimLastTime = now;                                        
    }
    void doRevealAnimation() {
        final int h = mCloseView.getHeight() + mStatusBarView.getHeight();
        if (mAnimatingReveal && mAnimating && mAnimY < h) {
            incrementAnim();
            if (mAnimY >= h) {
                mAnimY = h;
                updateExpandedViewPos((int)mAnimY);
            } else {
                updateExpandedViewPos((int)mAnimY);
                mCurAnimationTime += ANIM_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
                        mCurAnimationTime);
            }
        }
    }
    void prepareTracking(int y, boolean opening) {
        mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        if (opening) {
            mAnimAccel = 2000.0f;
            mAnimVel = 200;
            mAnimY = mStatusBarView.getHeight();
            updateExpandedViewPos((int)mAnimY);
            mAnimating = true;
            mAnimatingReveal = true;
            mHandler.removeMessages(MSG_ANIMATE);
            mHandler.removeMessages(MSG_ANIMATE_REVEAL);
            long now = SystemClock.uptimeMillis();
            mAnimLastTime = now;
            mCurAnimationTime = now + ANIM_FRAME_DURATION;
            mAnimating = true;
            mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE_REVEAL),
                    mCurAnimationTime);
            makeExpandedVisible();
        } else {
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            updateExpandedViewPos(y + mViewDelta);
        }
    }
    void performFling(int y, float vel, boolean always) {
        mAnimatingReveal = false;
        mDisplayHeight = mDisplay.getHeight();
        mAnimY = y;
        mAnimVel = vel;
        if (mExpanded) {
            if (!always && (
                    vel > 200.0f
                    || (y > (mDisplayHeight-25) && vel > -200.0f))) {
                mAnimAccel = 2000.0f;
                if (vel < 0) {
                    mAnimVel = 0;
                }
            }
            else {
                mAnimAccel = -2000.0f;
                if (vel > 0) {
                    mAnimVel = 0;
                }
            }
        } else {
            if (always || (
                    vel > 200.0f
                    || (y > (mDisplayHeight/2) && vel > -200.0f))) {
                mAnimAccel = 2000.0f;
                if (vel < 0) {
                    mAnimVel = 0;
                }
            }
            else {
                mAnimAccel = -2000.0f;
                if (vel > 0) {
                    mAnimVel = 0;
                }
            }
        }
        long now = SystemClock.uptimeMillis();
        mAnimLastTime = now;
        mCurAnimationTime = now + ANIM_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.removeMessages(MSG_ANIMATE_REVEAL);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurAnimationTime);
        stopTracking();
    }
    boolean interceptTouchEvent(MotionEvent event) {
        if (SPEW) {
            Slog.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled="
                + mDisabled);
        }
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return false;
        }
        final int statusBarSize = mStatusBarView.getHeight();
        final int hitSize = statusBarSize*2;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final int y = (int)event.getRawY();
            if (!mExpanded) {
                mViewDelta = statusBarSize - y;
            } else {
                mTrackingView.getLocationOnScreen(mAbsPos);
                mViewDelta = mAbsPos[1] + mTrackingView.getHeight() - y;
            }
            if ((!mExpanded && y < hitSize) ||
                    (mExpanded && y > (mDisplay.getHeight()-hitSize))) {
                int x = (int)event.getRawX();
                final int edgeBorder = mEdgeBorder;
                if (x >= edgeBorder && x < mDisplay.getWidth() - edgeBorder) {
                    prepareTracking(y, !mExpanded);
                    mVelocityTracker.addMovement(event);
                }
            }
        } else if (mTracking) {
            mVelocityTracker.addMovement(event);
            final int minY = statusBarSize + mCloseView.getHeight();
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                int y = (int)event.getRawY();
                if (mAnimatingReveal && y < minY) {
                } else  {
                    mAnimatingReveal = false;
                    updateExpandedViewPos(y + mViewDelta);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mVelocityTracker.computeCurrentVelocity(1000);
                float yVel = mVelocityTracker.getYVelocity();
                boolean negative = yVel < 0;
                float xVel = mVelocityTracker.getXVelocity();
                if (xVel < 0) {
                    xVel = -xVel;
                }
                if (xVel > 150.0f) {
                    xVel = 150.0f; 
                }
                float vel = (float)Math.hypot(yVel, xVel);
                if (negative) {
                    vel = -vel;
                }
                performFling((int)event.getRawY(), vel, false);
            }
        }
        return false;
    }
    private class Launcher implements View.OnClickListener {
        private PendingIntent mIntent;
        private String mPkg;
        private String mTag;
        private int mId;
        Launcher(PendingIntent intent, String pkg, String tag, int id) {
            mIntent = intent;
            mPkg = pkg;
            mTag = tag;
            mId = id;
        }
        public void onClick(View v) {
            try {
                ActivityManagerNative.getDefault().resumeAppSwitches();
            } catch (RemoteException e) {
            }
            int[] pos = new int[2];
            v.getLocationOnScreen(pos);
            Intent overlay = new Intent();
            overlay.setSourceBounds(
                    new Rect(pos[0], pos[1], pos[0]+v.getWidth(), pos[1]+v.getHeight()));
            try {
                mIntent.send(mContext, 0, overlay);
                mNotificationCallbacks.onNotificationClick(mPkg, mTag, mId);
            } catch (PendingIntent.CanceledException e) {
                Slog.w(TAG, "Sending contentIntent failed: " + e);
            }
            deactivate();
        }
    }
    private class MyTicker extends Ticker {
        MyTicker(Context context, StatusBarView sb) {
            super(context, sb);
        }
        @Override
        void tickerStarting() {
            mTicking = true;
            mIcons.setVisibility(View.GONE);
            mTickerView.setVisibility(View.VISIBLE);
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_up_in, null));
            mIcons.startAnimation(loadAnim(com.android.internal.R.anim.push_up_out, null));
            if (mExpandedVisible) {
                setDateViewVisibility(false, com.android.internal.R.anim.push_up_out);
            }
        }
        @Override
        void tickerDone() {
            mIcons.setVisibility(View.VISIBLE);
            mTickerView.setVisibility(View.GONE);
            mIcons.startAnimation(loadAnim(com.android.internal.R.anim.push_down_in, null));
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_down_out,
                        mTickingDoneListener));
            if (mExpandedVisible) {
                setDateViewVisibility(true, com.android.internal.R.anim.push_down_in);
            }
        }
        void tickerHalting() {
            mIcons.setVisibility(View.VISIBLE);
            mTickerView.setVisibility(View.GONE);
            mIcons.startAnimation(loadAnim(com.android.internal.R.anim.fade_in, null));
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.fade_out,
                        mTickingDoneListener));
            if (mExpandedVisible) {
                setDateViewVisibility(true, com.android.internal.R.anim.fade_in);
            }
        }
    }
    Animation.AnimationListener mTickingDoneListener = new Animation.AnimationListener() {;
        public void onAnimationEnd(Animation animation) {
            mTicking = false;
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationStart(Animation animation) {
        }
    };
    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }
    public String viewInfo(View v) {
        return "(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                + " " + v.getWidth() + "x" + v.getHeight() + ")";
    }
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.DUMP)
                != PackageManager.PERMISSION_GRANTED) {
            pw.println("Permission Denial: can't dump StatusBar from from pid="
                    + Binder.getCallingPid()
                    + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (mQueueLock) {
            pw.println("Current Status Bar state:");
            pw.println("  mExpanded=" + mExpanded
                    + ", mExpandedVisible=" + mExpandedVisible);
            pw.println("  mTicking=" + mTicking);
            pw.println("  mTracking=" + mTracking);
            pw.println("  mAnimating=" + mAnimating
                    + ", mAnimY=" + mAnimY + ", mAnimVel=" + mAnimVel
                    + ", mAnimAccel=" + mAnimAccel);
            pw.println("  mCurAnimationTime=" + mCurAnimationTime
                    + " mAnimLastTime=" + mAnimLastTime);
            pw.println("  mDisplayHeight=" + mDisplayHeight
                    + " mAnimatingReveal=" + mAnimatingReveal
                    + " mViewDelta=" + mViewDelta);
            pw.println("  mDisplayHeight=" + mDisplayHeight);
            final int N = mQueue.size();
            pw.println("  mQueue.size=" + N);
            for (int i=0; i<N; i++) {
                PendingOp op = mQueue.get(i);
                pw.println("    [" + i + "] key=" + op.key + " code=" + op.code + " visible="
                        + op.visible);
                pw.println("           iconData=" + op.iconData);
                pw.println("           notificationData=" + op.notificationData);
            }
            pw.println("  mExpandedParams: " + mExpandedParams);
            pw.println("  mExpandedView: " + viewInfo(mExpandedView));
            pw.println("  mExpandedDialog: " + mExpandedDialog);
            pw.println("  mTrackingParams: " + mTrackingParams);
            pw.println("  mTrackingView: " + viewInfo(mTrackingView));
            pw.println("  mOngoingTitle: " + viewInfo(mOngoingTitle));
            pw.println("  mOngoingItems: " + viewInfo(mOngoingItems));
            pw.println("  mLatestTitle: " + viewInfo(mLatestTitle));
            pw.println("  mLatestItems: " + viewInfo(mLatestItems));
            pw.println("  mNoNotificationsTitle: " + viewInfo(mNoNotificationsTitle));
            pw.println("  mCloseView: " + viewInfo(mCloseView));
            pw.println("  mTickerView: " + viewInfo(mTickerView));
            pw.println("  mScrollView: " + viewInfo(mScrollView)
                    + " scroll " + mScrollView.getScrollX() + "," + mScrollView.getScrollY());
            pw.println("mNotificationLinearLayout: " + viewInfo(mNotificationLinearLayout));
        }
        synchronized (mIconMap) {
            final int N = mIconMap.size();
            pw.println("  mIconMap.size=" + N);
            Set<IBinder> keys = mIconMap.keySet();
            int i=0;
            for (IBinder key: keys) {
                StatusBarIcon icon = mIconMap.get(key);
                pw.println("    [" + i + "] key=" + key);
                pw.println("           data=" + icon.mData);
                i++;
            }
        }
        synchronized (mNotificationData) {
            int N = mNotificationData.ongoingCount();
            pw.println("  ongoingCount.size=" + N);
            for (int i=0; i<N; i++) {
                StatusBarNotification n = mNotificationData.getOngoing(i);
                pw.println("    [" + i + "] key=" + n.key + " view=" + n.view);
                pw.println("           data=" + n.data);
            }
            N = mNotificationData.latestCount();
            pw.println("  ongoingCount.size=" + N);
            for (int i=0; i<N; i++) {
                StatusBarNotification n = mNotificationData.getLatest(i);
                pw.println("    [" + i + "] key=" + n.key + " view=" + n.view);
                pw.println("           data=" + n.data);
            }
        }
        synchronized (mDisableRecords) {
            final int N = mDisableRecords.size();
            pw.println("  mDisableRecords.size=" + N
                    + " mDisabled=0x" + Integer.toHexString(mDisabled));
            for (int i=0; i<N; i++) {
                DisableRecord tok = mDisableRecords.get(i);
                pw.println("    [" + i + "] what=0x" + Integer.toHexString(tok.what)
                                + " pkg=" + tok.pkg + " token=" + tok.token);
            }
        }
        if (false) {
            pw.println("see the logcat for a dump of the views we have created.");
            mHandler.post(new Runnable() {
                    public void run() {
                        mStatusBarView.getLocationOnScreen(mAbsPos);
                        Slog.d(TAG, "mStatusBarView: ----- (" + mAbsPos[0] + "," + mAbsPos[1]
                                + ") " + mStatusBarView.getWidth() + "x"
                                + mStatusBarView.getHeight());
                        mStatusBarView.debug();
                        mExpandedView.getLocationOnScreen(mAbsPos);
                        Slog.d(TAG, "mExpandedView: ----- (" + mAbsPos[0] + "," + mAbsPos[1]
                                + ") " + mExpandedView.getWidth() + "x"
                                + mExpandedView.getHeight());
                        mExpandedView.debug();
                        mTrackingView.getLocationOnScreen(mAbsPos);
                        Slog.d(TAG, "mTrackingView: ----- (" + mAbsPos[0] + "," + mAbsPos[1]
                                + ") " + mTrackingView.getWidth() + "x"
                                + mTrackingView.getHeight());
                        mTrackingView.debug();
                    }
                });
        }
    }
    void onBarViewAttached() {
        WindowManager.LayoutParams lp;
        int pixelFormat;
        Drawable bg;
        pixelFormat = PixelFormat.RGBX_8888;
        bg = mTrackingView.getBackground();
        if (bg != null) {
            pixelFormat = bg.getOpacity();
        }
        lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                pixelFormat);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("TrackingView");
        lp.y = mTrackingPosition;
        mTrackingParams = lp;
        WindowManagerImpl.getDefault().addView(mTrackingView, lp);
    }
    void onTrackingViewAttached() {
        WindowManager.LayoutParams lp;
        int pixelFormat;
        Drawable bg;
        pixelFormat = PixelFormat.TRANSLUCENT;
        final int disph = mDisplay.getHeight();
        lp = mExpandedDialog.getWindow().getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = getExpandedHeight();
        lp.x = 0;
        mTrackingPosition = lp.y = -disph; 
        lp.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
        lp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_DITHER
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = pixelFormat;
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        lp.setTitle("StatusBarExpanded");
        mExpandedDialog.getWindow().setAttributes(lp);
        mExpandedDialog.getWindow().setFormat(pixelFormat);
        mExpandedParams = lp;
        mExpandedDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mExpandedDialog.setContentView(mExpandedView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.MATCH_PARENT));
        mExpandedDialog.getWindow().setBackgroundDrawable(null);
        mExpandedDialog.show();
        FrameLayout hack = (FrameLayout)mExpandedView.getParent();
    }
    void setDateViewVisibility(boolean visible, int anim) {
        mDateView.setUpdates(visible);
        mDateView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mDateView.startAnimation(loadAnim(anim, null));
    }
    void setNotificationIconVisibility(boolean visible, int anim) {
        int old = mNotificationIcons.getVisibility();
        int v = visible ? View.VISIBLE : View.INVISIBLE;
        if (old != v) {
            mNotificationIcons.setVisibility(v);
            mNotificationIcons.startAnimation(loadAnim(anim, null));
        }
    }
    void updateExpandedViewPos(int expandedPosition) {
        if (SPEW) {
            Slog.d(TAG, "updateExpandedViewPos before expandedPosition=" + expandedPosition
                    + " mTrackingParams.y=" + mTrackingParams.y
                    + " mTrackingPosition=" + mTrackingPosition);
        }
        int h = mStatusBarView.getHeight();
        int disph = mDisplay.getHeight();
        if (!mExpandedVisible) {
            if (mTrackingView != null) {
                mTrackingPosition = -disph;
                if (mTrackingParams != null) {
                    mTrackingParams.y = mTrackingPosition;
                    WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
                }
            }
            if (mExpandedParams != null) {
                mExpandedParams.y = -disph;
                mExpandedDialog.getWindow().setAttributes(mExpandedParams);
            }
            return;
        }
        int pos;
        if (expandedPosition == EXPANDED_FULL_OPEN) {
            pos = h;
        }
        else if (expandedPosition == EXPANDED_LEAVE_ALONE) {
            pos = mTrackingPosition;
        }
        else {
            if (expandedPosition <= disph) {
                pos = expandedPosition;
            } else {
                pos = disph;
            }
            pos -= disph-h;
        }
        mTrackingPosition = mTrackingParams.y = pos;
        mTrackingParams.height = disph-h;
        WindowManagerImpl.getDefault().updateViewLayout(mTrackingView, mTrackingParams);
        if (mExpandedParams != null) {
            mCloseView.getLocationInWindow(mPositionTmp);
            final int closePos = mPositionTmp[1];
            mExpandedContents.getLocationInWindow(mPositionTmp);
            final int contentsBottom = mPositionTmp[1] + mExpandedContents.getHeight();
            mExpandedParams.y = pos + mTrackingView.getHeight()
                    - (mTrackingParams.height-closePos) - contentsBottom;
            int max = h;
            if (mExpandedParams.y > max) {
                mExpandedParams.y = max;
            }
            int min = mTrackingPosition;
            if (mExpandedParams.y < min) {
                mExpandedParams.y = min;
            }
            boolean visible = (mTrackingPosition + mTrackingView.getHeight()) > h;
            if (!visible) {
                mExpandedParams.y = -disph;
            }
            panelSlightlyVisible(visible);
            mExpandedDialog.getWindow().setAttributes(mExpandedParams);
        }
        if (SPEW) {
            Slog.d(TAG, "updateExpandedViewPos after  expandedPosition=" + expandedPosition
                    + " mTrackingParams.y=" + mTrackingParams.y
                    + " mTrackingPosition=" + mTrackingPosition
                    + " mExpandedParams.y=" + mExpandedParams.y
                    + " mExpandedParams.height=" + mExpandedParams.height);
        }
    }
    int getExpandedHeight() {
        return mDisplay.getHeight() - mStatusBarView.getHeight() - mCloseView.getHeight();
    }
    void updateExpandedHeight() {
        if (mExpandedView != null) {
            mExpandedParams.height = getExpandedHeight();
            mExpandedDialog.getWindow().setAttributes(mExpandedParams);
        }
    }
    private boolean mPanelSlightlyVisible;
    void panelSlightlyVisible(boolean visible) {
        if (mPanelSlightlyVisible != visible) {
            mPanelSlightlyVisible = visible;
            if (visible) {
                mNotificationCallbacks.onPanelRevealed();
            }
        }
    }
    void performDisableActions(int net) {
        int old = mDisabled;
        int diff = net ^ old;
        mDisabled = net;
        if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((net & StatusBarManager.DISABLE_EXPAND) != 0) {
                Slog.d(TAG, "DISABLE_EXPAND: yes");
                animateCollapse();
            }
        }
        if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((net & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: yes");
                if (mTicking) {
                    mNotificationIcons.setVisibility(View.INVISIBLE);
                    mTicker.halt();
                } else {
                    setNotificationIconVisibility(false, com.android.internal.R.anim.fade_out);
                }
            } else {
                Slog.d(TAG, "DISABLE_NOTIFICATION_ICONS: no");
                if (!mExpandedVisible) {
                    setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
                }
            }
        } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
            if (mTicking && (net & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                mTicker.halt();
            }
        }
    }
    private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            mNotificationCallbacks.onClearAll();
            addPendingOp(OP_EXPAND, null, false);
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                    || Intent.ACTION_SCREEN_OFF.equals(action)) {
                deactivate();
            }
            else if (Telephony.Intents.SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                updateNetworkName(intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_SPN, false),
                        intent.getStringExtra(Telephony.Intents.EXTRA_SPN),
                        intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_PLMN, false),
                        intent.getStringExtra(Telephony.Intents.EXTRA_PLMN));
            }
            else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                updateResources();
            }
        }
    };
    void updateNetworkName(boolean showSpn, String spn, boolean showPlmn, String plmn) {
        if (false) {
            Slog.d(TAG, "updateNetworkName showSpn=" + showSpn + " spn=" + spn
                    + " showPlmn=" + showPlmn + " plmn=" + plmn);
        }
        boolean something = false;
        if (showPlmn) {
            mPlmnLabel.setVisibility(View.VISIBLE);
            if (plmn != null) {
                mPlmnLabel.setText(plmn);
            } else {
                mPlmnLabel.setText(R.string.lockscreen_carrier_default);
            }
        } else {
            mPlmnLabel.setText("");
            mPlmnLabel.setVisibility(View.GONE);
        }
        if (showSpn && spn != null) {
            mSpnLabel.setText(spn);
            mSpnLabel.setVisibility(View.VISIBLE);
            something = true;
        } else {
            mSpnLabel.setText("");
            mSpnLabel.setVisibility(View.GONE);
        }
    }
    void updateResources() {
        Resources res = mContext.getResources();
        mClearButton.setText(mContext.getText(R.string.status_bar_clear_all_button));
        mOngoingTitle.setText(mContext.getText(R.string.status_bar_ongoing_events_title));
        mLatestTitle.setText(mContext.getText(R.string.status_bar_latest_events_title));
        mNoNotificationsTitle.setText(mContext.getText(R.string.status_bar_no_notifications_title));
        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);
        if (false) Slog.v(TAG, "updateResources");
    }
    void postStartTracing() {
        mHandler.postDelayed(mStartTracing, 3000);
    }
    void vibrate() {
        android.os.Vibrator vib = (android.os.Vibrator)mContext.getSystemService(
                Context.VIBRATOR_SERVICE);
        vib.vibrate(250);
    }
    Runnable mStartTracing = new Runnable() {
        public void run() {
            vibrate();
            SystemClock.sleep(250);
            Slog.d(TAG, "startTracing");
            android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
            mHandler.postDelayed(mStopTracing, 10000);
        }
    };
    Runnable mStopTracing = new Runnable() {
        public void run() {
            android.os.Debug.stopMethodTracing();
            Slog.d(TAG, "stopTracing");
            vibrate();
        }
    };
    class UninstallReceiver extends BroadcastReceiver {
        public UninstallReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
            filter.addDataScheme("package");
            mContext.registerReceiver(this, filter);
            IntentFilter sdFilter = new IntentFilter(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mContext.registerReceiver(this, sdFilter);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String pkgList[] = null;
            if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(intent.getAction())) {
                pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            } else {
                Uri data = intent.getData();
                if (data != null) {
                    String pkg = data.getSchemeSpecificPart();
                    if (pkg != null) {
                        pkgList = new String[]{pkg};
                    }
                }
            }
            ArrayList<StatusBarNotification> list = null;
            if (pkgList != null) {
                synchronized (StatusBarService.this) {
                    for (String pkg : pkgList) {
                        list = mNotificationData.notificationsForPackage(pkg);
                    }
                }
            }
            if (list != null) {
                final int N = list.size();
                for (int i=0; i<N; i++) {
                    removeIcon(list.get(i).key);
                }
            }
        }
    }
}
