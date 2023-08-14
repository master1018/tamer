public class SyncStateCheckBoxPreference extends CheckBoxPreference {
    private boolean mIsActive = false;
    private boolean mIsPending = false;
    private boolean mFailed = false;
    private Account mAccount;
    private String mAuthority;
    private boolean mOneTimeSyncMode = false;
    public SyncStateCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_sync_toggle);
        mAccount = null;
        mAuthority = null;
    }
    public SyncStateCheckBoxPreference(Context context, Account account, String authority) {
        super(context, null);
        mAccount = account;
        mAuthority = authority;
        setWidgetLayoutResource(R.layout.preference_widget_sync_toggle);
    }
    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        ImageView syncActiveView = (ImageView) view.findViewById(R.id.sync_active);
        View syncPendingView = view.findViewById(R.id.sync_pending);
        View syncFailedView = view.findViewById(R.id.sync_failed);
        syncActiveView.setVisibility(mIsActive ? View.VISIBLE : View.GONE);
        final AnimationDrawable anim = (AnimationDrawable) syncActiveView.getDrawable();
        boolean showError;
        boolean showPending;
        if (mIsActive) {
            syncActiveView.post(new Runnable() {
                public void run() {
                    anim.start();
                }
            });
            showPending = false;
            showError = false;
        } else {
            anim.stop();
            if (mIsPending) {
                showPending = true;
                showError = false;
            } else {
                showPending = false;
                showError = mFailed;
            }
        }
        syncFailedView.setVisibility(showError ? View.VISIBLE : View.GONE);
        syncPendingView.setVisibility((showPending && !mIsActive) ? View.VISIBLE : View.GONE);
        View checkBox = view.findViewById(android.R.id.checkbox);
        if (mOneTimeSyncMode) {
            checkBox.setVisibility(View.GONE);
            TextView summary = (TextView) view.findViewById(android.R.id.summary);
            summary.setText(getContext().getString(R.string.sync_one_time_sync, getSummary()));
        } else {
            checkBox.setVisibility(View.VISIBLE);
        }
    }
    public void setActive(boolean isActive) {
        mIsActive = isActive;
        notifyChanged();
    }
    public void setPending(boolean isPending) {
        mIsPending = isPending;
        notifyChanged();
    }
    public void setFailed(boolean failed) {
        mFailed = failed;
        notifyChanged();
    }
    public void setOneTimeSyncMode(boolean oneTimeSyncMode) {
        mOneTimeSyncMode = oneTimeSyncMode;
        notifyChanged();
    }
    public boolean isOneTimeSyncMode() {
        return mOneTimeSyncMode;
    }
    @Override
    protected void onClick() {
        if (!mOneTimeSyncMode) {
            super.onClick();
        }            
    }
    public Account getAccount() {
        return mAccount;
    }
    public String getAuthority() {
        return mAuthority;
    }
}
