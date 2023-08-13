public class Preference implements Comparable<Preference>, OnDependencyChangeListener { 
    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;
    private Context mContext;
    private PreferenceManager mPreferenceManager;
    private long mId;
    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private int mOrder = DEFAULT_ORDER;
    private CharSequence mTitle;
    private CharSequence mSummary;
    private String mKey;
    private Intent mIntent;
    private boolean mEnabled = true;
    private boolean mSelectable = true;
    private boolean mRequiresKey;
    private boolean mPersistent = true;
    private String mDependencyKey;
    private Object mDefaultValue;
    private boolean mDependencyMet = true;
    private boolean mShouldDisableView = true;
    private int mLayoutResId = com.android.internal.R.layout.preference;
    private int mWidgetLayoutResId;
    private boolean mHasSpecifiedLayout = false;
    private OnPreferenceChangeInternalListener mListener;
    private List<Preference> mDependents;
    private boolean mBaseMethodCalled;
    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object newValue);
    }
    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }
    interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(Preference preference);
        void onPreferenceHierarchyChange(Preference preference);
    }
    public Preference(Context context, AttributeSet attrs, int defStyle) {
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.Preference, defStyle, 0);
        for (int i = a.getIndexCount(); i >= 0; i--) {
            int attr = a.getIndex(i); 
            switch (attr) {
                case com.android.internal.R.styleable.Preference_key:
                    mKey = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.Preference_title:
                    mTitle = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.Preference_summary:
                    mSummary = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.Preference_order:
                    mOrder = a.getInt(attr, mOrder);
                    break;
                case com.android.internal.R.styleable.Preference_layout:
                    mLayoutResId = a.getResourceId(attr, mLayoutResId);
                    break;
                case com.android.internal.R.styleable.Preference_widgetLayout:
                    mWidgetLayoutResId = a.getResourceId(attr, mWidgetLayoutResId);
                    break;
                case com.android.internal.R.styleable.Preference_enabled:
                    mEnabled = a.getBoolean(attr, true);
                    break;
                case com.android.internal.R.styleable.Preference_selectable:
                    mSelectable = a.getBoolean(attr, true);
                    break;
                case com.android.internal.R.styleable.Preference_persistent:
                    mPersistent = a.getBoolean(attr, mPersistent);
                    break;
                case com.android.internal.R.styleable.Preference_dependency:
                    mDependencyKey = a.getString(attr);
                    break;
                case com.android.internal.R.styleable.Preference_defaultValue:
                    mDefaultValue = onGetDefaultValue(a, attr);
                    break;
                case com.android.internal.R.styleable.Preference_shouldDisableView:
                    mShouldDisableView = a.getBoolean(attr, mShouldDisableView);
                    break;
            }
        }
        a.recycle();
        if (!getClass().getName().startsWith("android.preference")) {
            mHasSpecifiedLayout = true;
        }
    }
    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public Preference(Context context) {
        this(context, null);
    }
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }
    public void setIntent(Intent intent) {
        mIntent = intent;
    }
    public Intent getIntent() {
        return mIntent;
    }
    public void setLayoutResource(int layoutResId) {
        if (layoutResId != mLayoutResId) {
            mHasSpecifiedLayout = true;
        }
        mLayoutResId = layoutResId;
    }
    public int getLayoutResource() {
        return mLayoutResId;
    }
    public void setWidgetLayoutResource(int widgetLayoutResId) {
        if (widgetLayoutResId != mWidgetLayoutResId) {
            mHasSpecifiedLayout = true;
        }
        mWidgetLayoutResId = widgetLayoutResId;
    }
    public int getWidgetLayoutResource() {
        return mWidgetLayoutResId;
    }
    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
        }
        onBindView(convertView);
        return convertView;
    }
    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(mLayoutResId, parent, false); 
        if (mWidgetLayoutResId != 0) {
            final ViewGroup widgetFrame = (ViewGroup)layout.findViewById(com.android.internal.R.id.widget_frame);
            layoutInflater.inflate(mWidgetLayoutResId, widgetFrame);
        }
        return layout;
    }
    protected void onBindView(View view) {
        TextView textView = (TextView) view.findViewById(com.android.internal.R.id.title); 
        if (textView != null) {
            textView.setText(getTitle());
        }
        textView = (TextView) view.findViewById(com.android.internal.R.id.summary);
        if (textView != null) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                if (textView.getVisibility() != View.VISIBLE) {
                    textView.setVisibility(View.VISIBLE);
                }
                textView.setText(getSummary());
            } else {
                if (textView.getVisibility() != View.GONE) {
                    textView.setVisibility(View.GONE);
                }
            }
        }
        if (mShouldDisableView) {
            setEnabledStateOnViews(view, isEnabled());
        }
    }
    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }
    public void setOrder(int order) {
        if (order != mOrder) {
            mOrder = order;
            notifyHierarchyChanged();
        }
    }
    public int getOrder() {
        return mOrder;
    }
    public void setTitle(CharSequence title) {
        if (title == null && mTitle != null || title != null && !title.equals(mTitle)) {
            mTitle = title;
            notifyChanged();
        }
    }
    public void setTitle(int titleResId) {
        setTitle(mContext.getString(titleResId));
    }
    public CharSequence getTitle() {
        return mTitle;
    }
    public CharSequence getSummary() {
        return mSummary;
    }
    public void setSummary(CharSequence summary) {
        if (summary == null && mSummary != null || summary != null && !summary.equals(mSummary)) {
            mSummary = summary;
            notifyChanged();
        }
    }
    public void setSummary(int summaryResId) {
        setSummary(mContext.getString(summaryResId));
    }
    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }
    public boolean isEnabled() {
        return mEnabled && mDependencyMet;
    }
    public void setSelectable(boolean selectable) {
        if (mSelectable != selectable) {
            mSelectable = selectable;
            notifyChanged();
        }
    }
    public boolean isSelectable() {
        return mSelectable;
    }
    public void setShouldDisableView(boolean shouldDisableView) {
        mShouldDisableView = shouldDisableView;
        notifyChanged();
    }
    public boolean getShouldDisableView() {
        return mShouldDisableView;
    }
    long getId() {
        return mId;
    }
    protected void onClick() {
    }
    public void setKey(String key) {
        mKey = key;
        if (mRequiresKey && !hasKey()) {
            requireKey();
        }
    }
    public String getKey() {
        return mKey;
    }
    void requireKey() {
        if (mKey == null) {
            throw new IllegalStateException("Preference does not have a key assigned.");
        }
        mRequiresKey = true;
    }
    public boolean hasKey() {
        return !TextUtils.isEmpty(mKey);
    }
    public boolean isPersistent() {
        return mPersistent;
    }
    protected boolean shouldPersist() {
        return mPreferenceManager != null && isPersistent() && hasKey();
    }
    public void setPersistent(boolean persistent) {
        mPersistent = persistent;
    }
    protected boolean callChangeListener(Object newValue) {
        return mOnChangeListener == null ? true : mOnChangeListener.onPreferenceChange(this, newValue);
    }
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnChangeListener = onPreferenceChangeListener;
    }
    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return mOnChangeListener;
    }
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        mOnClickListener = onPreferenceClickListener;
    }
    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return mOnClickListener;
    }
    void performClick(PreferenceScreen preferenceScreen) {
        if (!isEnabled()) {
            return;
        }
        onClick();
        if (mOnClickListener != null && mOnClickListener.onPreferenceClick(this)) {
            return;
        }
        PreferenceManager preferenceManager = getPreferenceManager();
        if (preferenceManager != null) {
            PreferenceManager.OnPreferenceTreeClickListener listener = preferenceManager
                    .getOnPreferenceTreeClickListener();
            if (preferenceScreen != null && listener != null
                    && listener.onPreferenceTreeClick(preferenceScreen, this)) {
                return;
            }
        }
        if (mIntent != null) {
            Context context = getContext();
            context.startActivity(mIntent);
        }
    }
    public Context getContext() {
        return mContext;
    }
    public SharedPreferences getSharedPreferences() {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.getSharedPreferences();
    }
    public SharedPreferences.Editor getEditor() {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.getEditor();
    }
    public boolean shouldCommit() {
        if (mPreferenceManager == null) {
            return false;
        }
        return mPreferenceManager.shouldCommit();
    }
    public int compareTo(Preference another) {
        if (mOrder != DEFAULT_ORDER
                || (mOrder == DEFAULT_ORDER && another.mOrder != DEFAULT_ORDER)) {
            return mOrder - another.mOrder; 
        } else if (mTitle == null) {
            return 1;
        } else if (another.mTitle == null) {
            return -1;
        } else {
            return CharSequences.compareToIgnoreCase(mTitle, another.mTitle);
        }
    }
    final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        mListener = listener;
    }
    protected void notifyChanged() {
        if (mListener != null) {
            mListener.onPreferenceChange(this);
        }
    }
    protected void notifyHierarchyChanged() {
        if (mListener != null) {
            mListener.onPreferenceHierarchyChange(this);
        }
    }
    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        mPreferenceManager = preferenceManager;
        mId = preferenceManager.getNextId();
        dispatchSetInitialValue();
    }
    protected void onAttachedToActivity() {
        registerDependency();
    }
    private void registerDependency() {
        if (TextUtils.isEmpty(mDependencyKey)) return;
        Preference preference = findPreferenceInHierarchy(mDependencyKey);
        if (preference != null) {
            preference.registerDependent(this);
        } else {
            throw new IllegalStateException("Dependency \"" + mDependencyKey
                    + "\" not found for preference \"" + mKey + "\" (title: \"" + mTitle + "\"");
        }
    }
    private void unregisterDependency() {
        if (mDependencyKey != null) {
            final Preference oldDependency = findPreferenceInHierarchy(mDependencyKey);
            if (oldDependency != null) {
                oldDependency.unregisterDependent(this);
            }
        }
    }
    protected Preference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }
    private void registerDependent(Preference dependent) {
        if (mDependents == null) {
            mDependents = new ArrayList<Preference>();
        }
        mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }
    private void unregisterDependent(Preference dependent) {
        if (mDependents != null) {
            mDependents.remove(dependent);
        }
    }
    public void notifyDependencyChange(boolean disableDependents) {
        final List<Preference> dependents = mDependents;
        if (dependents == null) {
            return;
        }
        final int dependentsCount = dependents.size();
        for (int i = 0; i < dependentsCount; i++) {
            dependents.get(i).onDependencyChanged(this, disableDependents);
        }
    }
    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (mDependencyMet == disableDependent) {
            mDependencyMet = !disableDependent;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }
    public boolean shouldDisableDependents() {
        return !isEnabled();
    }
    public void setDependency(String dependencyKey) {
        unregisterDependency();
        mDependencyKey = dependencyKey;
        registerDependency();
    }
    public String getDependency() {
        return mDependencyKey;
    }
    protected void onPrepareForRemoval() {
        unregisterDependency();
    }
    public void setDefaultValue(Object defaultValue) {
        mDefaultValue = defaultValue;
    }
    private void dispatchSetInitialValue() {
        final boolean shouldPersist = shouldPersist();
        if (!shouldPersist || !getSharedPreferences().contains(mKey)) {
            if (mDefaultValue != null) {
                onSetInitialValue(false, mDefaultValue);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }
    private void tryCommit(SharedPreferences.Editor editor) {
        if (mPreferenceManager.shouldCommit()) {
            editor.commit();
        }
    }
    protected boolean persistString(String value) {
        if (shouldPersist()) {
            if (value == getPersistedString(null)) {
                return true;
            }
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putString(mKey, value);
            tryCommit(editor);
            return true;
        }
        return false;
    }
    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return mPreferenceManager.getSharedPreferences().getString(mKey, defaultReturnValue);
    }
    protected boolean persistInt(int value) {
        if (shouldPersist()) {
            if (value == getPersistedInt(~value)) {
                return true;
            }
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putInt(mKey, value);
            tryCommit(editor);
            return true;
        }
        return false;
    }
    protected int getPersistedInt(int defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return mPreferenceManager.getSharedPreferences().getInt(mKey, defaultReturnValue);
    }
    protected boolean persistFloat(float value) {
        if (shouldPersist()) {
            if (value == getPersistedFloat(Float.NaN)) {
                return true;
            }
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putFloat(mKey, value);
            tryCommit(editor);
            return true;
        }
        return false;
    }
    protected float getPersistedFloat(float defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return mPreferenceManager.getSharedPreferences().getFloat(mKey, defaultReturnValue);
    }
    protected boolean persistLong(long value) {
        if (shouldPersist()) {
            if (value == getPersistedLong(~value)) {
                return true;
            }
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putLong(mKey, value);
            tryCommit(editor);
            return true;
        }
        return false;
    }
    protected long getPersistedLong(long defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return mPreferenceManager.getSharedPreferences().getLong(mKey, defaultReturnValue);
    }
    protected boolean persistBoolean(boolean value) {
        if (shouldPersist()) {
            if (value == getPersistedBoolean(!value)) {
                return true;
            }
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putBoolean(mKey, value);
            tryCommit(editor);
            return true;
        }
        return false;
    }
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return mPreferenceManager.getSharedPreferences().getBoolean(mKey, defaultReturnValue);
    }
    boolean hasSpecifiedLayout() {
        return mHasSpecifiedLayout;
    }
    @Override
    public String toString() {
        return getFilterableStringBuilder().toString();
    }
    StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }
    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }
    void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!mBaseMethodCalled) {
                throw new IllegalStateException(
                        "Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                container.putParcelable(mKey, state);
            }
        }
    }
    protected Parcelable onSaveInstanceState() {
        mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }
    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }
    void dispatchRestoreInstanceState(Bundle container) {
        if (hasKey()) {
            Parcelable state = container.getParcelable(mKey);
            if (state != null) {
                mBaseMethodCalled = false;
                onRestoreInstanceState(state);
                if (!mBaseMethodCalled) {
                    throw new IllegalStateException(
                            "Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }
    protected void onRestoreInstanceState(Parcelable state) {
        mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }
    public static class BaseSavedState extends AbsSavedState {
        public BaseSavedState(Parcel source) {
            super(source);
        }
        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
        public static final Parcelable.Creator<BaseSavedState> CREATOR =
                new Parcelable.Creator<BaseSavedState>() {
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }
            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
    }
}
