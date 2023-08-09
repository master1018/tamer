public abstract class PreferenceGroup extends Preference implements GenericInflater.Parent<Preference> {
    private List<Preference> mPreferenceList;
    private boolean mOrderingAsAdded = true;
    private int mCurrentPreferenceOrder = 0;
    private boolean mAttachedToActivity = false;
    public PreferenceGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPreferenceList = new ArrayList<Preference>();
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.PreferenceGroup, defStyle, 0);
        mOrderingAsAdded = a.getBoolean(com.android.internal.R.styleable.PreferenceGroup_orderingFromXml,
                mOrderingAsAdded);
        a.recycle();
    }
    public PreferenceGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public void setOrderingAsAdded(boolean orderingAsAdded) {
        mOrderingAsAdded = orderingAsAdded;
    }
    public boolean isOrderingAsAdded() {
        return mOrderingAsAdded;
    }
    public void addItemFromInflater(Preference preference) {
        addPreference(preference);
    }
    public int getPreferenceCount() {
        return mPreferenceList.size();
    }
    public Preference getPreference(int index) {
        return mPreferenceList.get(index);
    }
    public boolean addPreference(Preference preference) {
        if (mPreferenceList.contains(preference)) {
            return true;
        }
        if (preference.getOrder() == Preference.DEFAULT_ORDER) {
            if (mOrderingAsAdded) {
                preference.setOrder(mCurrentPreferenceOrder++);
            }
            if (preference instanceof PreferenceGroup) {
                ((PreferenceGroup)preference).setOrderingAsAdded(mOrderingAsAdded);
            }
        }
        int insertionIndex = Collections.binarySearch(mPreferenceList, preference);
        if (insertionIndex < 0) {
            insertionIndex = insertionIndex * -1 - 1;
        }
        if (!onPrepareAddPreference(preference)) {
            return false;
        }
        synchronized(this) {
            mPreferenceList.add(insertionIndex, preference);
        }
        preference.onAttachedToHierarchy(getPreferenceManager());
        if (mAttachedToActivity) {
            preference.onAttachedToActivity();
        }
        notifyHierarchyChanged();
        return true;
    }
    public boolean removePreference(Preference preference) {
        final boolean returnValue = removePreferenceInt(preference);
        notifyHierarchyChanged();
        return returnValue;
    }
    private boolean removePreferenceInt(Preference preference) {
        synchronized(this) {
            preference.onPrepareForRemoval();
            return mPreferenceList.remove(preference);
        }
    }
    public void removeAll() {
        synchronized(this) {
            List<Preference> preferenceList = mPreferenceList;
            for (int i = preferenceList.size() - 1; i >= 0; i--) {
                removePreferenceInt(preferenceList.get(0));
            }
        }
        notifyHierarchyChanged();
    }
    protected boolean onPrepareAddPreference(Preference preference) {
        if (!super.isEnabled()) {
            preference.setEnabled(false);
        }
        return true;
    }
    public Preference findPreference(CharSequence key) {
        if (TextUtils.equals(getKey(), key)) {
            return this;
        }
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = getPreference(i);
            final String curKey = preference.getKey();
            if (curKey != null && curKey.equals(key)) {
                return preference;
            }
            if (preference instanceof PreferenceGroup) {
                final Preference returnedPreference = ((PreferenceGroup)preference)
                        .findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }
        return null;
    }
    protected boolean isOnSameScreenAsChildren() {
        return true;
    }
    @Override
    protected void onAttachedToActivity() {
        super.onAttachedToActivity();
        mAttachedToActivity = true;
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onAttachedToActivity();
        }
    }
    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        mAttachedToActivity = false;
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).setEnabled(enabled);
        }
    }
    void sortPreferences() {
        synchronized (this) {
            Collections.sort(mPreferenceList);
        }
    }
    @Override
    protected void dispatchSaveInstanceState(Bundle container) {
        super.dispatchSaveInstanceState(container);
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(container);
        }
    }
    @Override
    protected void dispatchRestoreInstanceState(Bundle container) {
        super.dispatchRestoreInstanceState(container);
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(container);
        }
    }
}
