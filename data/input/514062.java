public class ShowActivity extends Activity {
    private ActivityInfo mActivityInfo;
    private TextView mPackage;
    private ImageView mIconImage;
    private TextView mClass;
    private TextView mLabel;
    private TextView mLaunch;
    private TextView mProcess;
    private TextView mTaskAffinity;
    private TextView mPermission;
    private TextView mMultiprocess;
    private TextView mClearOnBackground;
    private TextView mStateNotNeeded;
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.show_activity);
        mPackage = (TextView)findViewById(R.id.packageView);
        mIconImage = (ImageView)findViewById(R.id.icon);
        mClass = (TextView)findViewById(R.id.classView);
        mLabel = (TextView)findViewById(R.id.label);
        mLaunch = (TextView)findViewById(R.id.launch);
        mProcess = (TextView)findViewById(R.id.process);
        mTaskAffinity = (TextView)findViewById(R.id.taskAffinity);
        mPermission = (TextView)findViewById(R.id.permission);
        mMultiprocess = (TextView)findViewById(R.id.multiprocess);
        mClearOnBackground = (TextView)findViewById(R.id.clearOnBackground);
        mStateNotNeeded = (TextView)findViewById(R.id.stateNotNeeded);
        final PackageManager pm = getPackageManager();
        try {
            mActivityInfo = pm.getActivityInfo(ComponentName.unflattenFromString(
                getIntent().getData().getSchemeSpecificPart()), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (mActivityInfo != null) {
            mPackage.setText(mActivityInfo.applicationInfo.packageName);
            mIconImage.setImageDrawable(mActivityInfo.loadIcon(pm));
            if (mActivityInfo.name.startsWith(
                    mActivityInfo.applicationInfo.packageName + ".")) {
                mClass.setText(mActivityInfo.name.substring(
                        mActivityInfo.applicationInfo.packageName.length()));
            } else {
                mClass.setText(mActivityInfo.name);
            }
            CharSequence label = mActivityInfo.loadLabel(pm);
            mLabel.setText("\"" + (label != null ? label : "") + "\"");
            switch (mActivityInfo.launchMode) {
            case ActivityInfo.LAUNCH_MULTIPLE:
                mLaunch.setText(getText(R.string.launch_multiple));
                break;
            case ActivityInfo.LAUNCH_SINGLE_TOP:
                mLaunch.setText(getText(R.string.launch_singleTop));
                break;
            case ActivityInfo.LAUNCH_SINGLE_TASK:
                mLaunch.setText(getText(R.string.launch_singleTask));
                break;
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
                mLaunch.setText(getText(R.string.launch_singleInstance));
                break;
            default:
                mLaunch.setText(getText(R.string.launch_unknown));
            }
            mProcess.setText(mActivityInfo.processName);
            mTaskAffinity.setText(mActivityInfo.taskAffinity != null
                    ? mActivityInfo.taskAffinity : getText(R.string.none));
            mPermission.setText(mActivityInfo.permission != null
                    ? mActivityInfo.permission : getText(R.string.none));
            mMultiprocess.setText(
                    (mActivityInfo.flags&ActivityInfo.FLAG_MULTIPROCESS) != 0
                    ? getText(R.string.yes) : getText(R.string.no));
            mClearOnBackground.setText(
                    (mActivityInfo.flags&ActivityInfo.FLAG_CLEAR_TASK_ON_LAUNCH) != 0
                    ? getText(R.string.yes) : getText(R.string.no));
            mStateNotNeeded.setText(
                    (mActivityInfo.flags&ActivityInfo.FLAG_STATE_NOT_NEEDED) != 0
                    ? getText(R.string.yes) : getText(R.string.no));
        }
    }
    public void onResume() {
        super.onResume();
    }
}
