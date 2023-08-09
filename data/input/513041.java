public class RecentApplicationsDialog extends Dialog implements OnClickListener {
    private static final boolean DBG_FORCE_EMPTY_LIST = false;
    static private StatusBarManager sStatusBar;
    private static final int NUM_BUTTONS = 8;
    private static final int MAX_RECENT_TASKS = NUM_BUTTONS * 2;    
    final TextView[] mIcons = new TextView[NUM_BUTTONS];
    View mNoAppsText;
    IntentFilter mBroadcastIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    private int mIconSize;
    public RecentApplicationsDialog(Context context) {
        super(context, com.android.internal.R.style.Theme_Dialog_RecentApplications);
        final Resources resources = context.getResources();
        mIconSize = (int) resources.getDimension(android.R.dimen.app_icon_size);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (sStatusBar == null) {
            sStatusBar = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);
        }
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setTitle("Recents");
        setContentView(com.android.internal.R.layout.recent_apps_dialog);
        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mIcons[0] = (TextView)findViewById(com.android.internal.R.id.button0);
        mIcons[1] = (TextView)findViewById(com.android.internal.R.id.button1);
        mIcons[2] = (TextView)findViewById(com.android.internal.R.id.button2);
        mIcons[3] = (TextView)findViewById(com.android.internal.R.id.button3);
        mIcons[4] = (TextView)findViewById(com.android.internal.R.id.button4);
        mIcons[5] = (TextView)findViewById(com.android.internal.R.id.button5);
        mIcons[6] = (TextView)findViewById(com.android.internal.R.id.button6);
        mIcons[7] = (TextView)findViewById(com.android.internal.R.id.button7);
        mNoAppsText = findViewById(com.android.internal.R.id.no_applications_message);
        for (TextView b: mIcons) {
            b.setOnClickListener(this);
        }
    }
    public void onClick(View v) {
        for (TextView b: mIcons) {
            if (b == v) {
                Intent intent = (Intent)b.getTag();
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                    try {
                        getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.w("Recent", "Unable to launch recent task", e);
                    }
                }
                break;
            }
        }
        dismiss();
    }
    @Override
    public void onStart() {
        super.onStart();
        reloadButtons();
        if (sStatusBar != null) {
            sStatusBar.disable(StatusBarManager.DISABLE_EXPAND);
        }
        getContext().registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
    }
    @Override
    public void onStop() {
        super.onStop();
        for (TextView icon: mIcons) {
            icon.setCompoundDrawables(null, null, null, null);
            icon.setTag(null);
        }
        if (sStatusBar != null) {
            sStatusBar.disable(StatusBarManager.DISABLE_NONE);
        }
        getContext().unregisterReceiver(mBroadcastReceiver);
     }
    private void reloadButtons() {
        final Context context = getContext();
        final PackageManager pm = context.getPackageManager();
        final ActivityManager am = (ActivityManager)
                                        context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> recentTasks =
            am.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
        ActivityInfo homeInfo = 
            new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    .resolveActivityInfo(pm, 0);
        IconUtilities iconUtilities = new IconUtilities(getContext());
        int index = 0;
        int numTasks = recentTasks.size();
        for (int i = 0; i < numTasks && (index < NUM_BUTTONS); ++i) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
            if (DBG_FORCE_EMPTY_LIST && (i == 0)) continue;
            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }
            if (homeInfo != null) {
                if (homeInfo.packageName.equals(
                        intent.getComponent().getPackageName())
                        && homeInfo.name.equals(
                                intent.getComponent().getClassName())) {
                    continue;
                }
            }
            intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                final ActivityInfo activityInfo = resolveInfo.activityInfo;
                final String title = activityInfo.loadLabel(pm).toString();
                Drawable icon = activityInfo.loadIcon(pm);
                if (title != null && title.length() > 0 && icon != null) {
                    final TextView tv = mIcons[index];
                    tv.setText(title);
                    icon = iconUtilities.createIconDrawable(icon);
                    tv.setCompoundDrawables(null, icon, null, null);
                    tv.setTag(intent);
                    tv.setVisibility(View.VISIBLE);
                    tv.setPressed(false);
                    tv.clearFocus();
                    ++index;
                }
            }
        }
        mNoAppsText.setVisibility((index == 0) ? View.VISIBLE : View.GONE);
        for (; index < NUM_BUTTONS; ++index) {
            mIcons[index].setVisibility(View.GONE);
        }
    }
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (! PhoneWindowManager.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    dismiss();
                }
            }
        }
    };
}
