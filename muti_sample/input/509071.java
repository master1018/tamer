public final class SearchableInfo implements Parcelable {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "SearchableInfo";
    private static final String MD_LABEL_SEARCHABLE = "android.app.searchable";
    private static final String MD_XML_ELEMENT_SEARCHABLE = "searchable";
    private static final String MD_XML_ELEMENT_SEARCHABLE_ACTION_KEY = "actionkey";
    private static final int SEARCH_MODE_BADGE_LABEL = 0x04;
    private static final int SEARCH_MODE_BADGE_ICON = 0x08;
    private static final int SEARCH_MODE_QUERY_REWRITE_FROM_DATA = 0x10;
    private static final int SEARCH_MODE_QUERY_REWRITE_FROM_TEXT = 0x20;
    private final int mLabelId;
    private final ComponentName mSearchActivity;
    private final int mHintId;
    private final int mSearchMode;
    private final int mIconId;
    private final int mSearchButtonText;
    private final int mSearchInputType;
    private final int mSearchImeOptions;
    private final boolean mIncludeInGlobalSearch;
    private final boolean mQueryAfterZeroResults;
    private final boolean mAutoUrlDetect;
    private final int mSettingsDescriptionId;
    private final String mSuggestAuthority;
    private final String mSuggestPath;
    private final String mSuggestSelection;
    private final String mSuggestIntentAction;
    private final String mSuggestIntentData;
    private final int mSuggestThreshold;
    private HashMap<Integer,ActionKeyInfo> mActionKeys = null;
    private final String mSuggestProviderPackage;
    private static final int VOICE_SEARCH_SHOW_BUTTON = 1;
    private static final int VOICE_SEARCH_LAUNCH_WEB_SEARCH = 2;
    private static final int VOICE_SEARCH_LAUNCH_RECOGNIZER = 4;
    private final int mVoiceSearchMode;
    private final int mVoiceLanguageModeId;       
    private final int mVoicePromptTextId;         
    private final int mVoiceLanguageId;           
    private final int mVoiceMaxResults;           
    public String getSuggestAuthority() {
        return mSuggestAuthority;
    }
    public String getSuggestPackage() {
        return mSuggestProviderPackage;
    }
    public ComponentName getSearchActivity() {
        return mSearchActivity;
    }
    public boolean useBadgeLabel() {
        return 0 != (mSearchMode & SEARCH_MODE_BADGE_LABEL);
    }
    public boolean useBadgeIcon() {
        return (0 != (mSearchMode & SEARCH_MODE_BADGE_ICON)) && (mIconId != 0);
    }
    public boolean shouldRewriteQueryFromData() {
        return 0 != (mSearchMode & SEARCH_MODE_QUERY_REWRITE_FROM_DATA);
    }
    public boolean shouldRewriteQueryFromText() {
        return 0 != (mSearchMode & SEARCH_MODE_QUERY_REWRITE_FROM_TEXT);
    }
    public int getSettingsDescriptionId() {
        return mSettingsDescriptionId;
    }
    public String getSuggestPath() {
        return mSuggestPath;
    }
    public String getSuggestSelection() {
        return mSuggestSelection;
    }
    public String getSuggestIntentAction() {
        return mSuggestIntentAction;
    }
    public String getSuggestIntentData() {
        return mSuggestIntentData;
    }
    public int getSuggestThreshold() {
        return mSuggestThreshold;
    }
    public Context getActivityContext(Context context) {
        return createActivityContext(context, mSearchActivity);
    }
    private static Context createActivityContext(Context context, ComponentName activity) {
        Context theirContext = null;
        try {
            theirContext = context.createPackageContext(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Package not found " + activity.getPackageName());
        } catch (java.lang.SecurityException e) {
            Log.e(LOG_TAG, "Can't make context for " + activity.getPackageName(), e);
        }
        return theirContext;
    }
    public Context getProviderContext(Context context, Context activityContext) {
        Context theirContext = null;
        if (mSearchActivity.getPackageName().equals(mSuggestProviderPackage)) {
            return activityContext;
        }
        if (mSuggestProviderPackage != null) {
            try {
                theirContext = context.createPackageContext(mSuggestProviderPackage, 0);
            } catch (PackageManager.NameNotFoundException e) {
            } catch (java.lang.SecurityException e) {
            }
        }
        return theirContext;
    }
    private SearchableInfo(Context activityContext, AttributeSet attr, final ComponentName cName) {
        mSearchActivity = cName;
        TypedArray a = activityContext.obtainStyledAttributes(attr,
                com.android.internal.R.styleable.Searchable);
        mSearchMode = a.getInt(com.android.internal.R.styleable.Searchable_searchMode, 0);
        mLabelId = a.getResourceId(com.android.internal.R.styleable.Searchable_label, 0);
        mHintId = a.getResourceId(com.android.internal.R.styleable.Searchable_hint, 0);
        mIconId = a.getResourceId(com.android.internal.R.styleable.Searchable_icon, 0);
        mSearchButtonText = a.getResourceId(
                com.android.internal.R.styleable.Searchable_searchButtonText, 0);
        mSearchInputType = a.getInt(com.android.internal.R.styleable.Searchable_inputType, 
                InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_NORMAL);
        mSearchImeOptions = a.getInt(com.android.internal.R.styleable.Searchable_imeOptions, 
                EditorInfo.IME_ACTION_GO);
        mIncludeInGlobalSearch = a.getBoolean(
                com.android.internal.R.styleable.Searchable_includeInGlobalSearch, false);
        mQueryAfterZeroResults = a.getBoolean(
                com.android.internal.R.styleable.Searchable_queryAfterZeroResults, false);
        mAutoUrlDetect = a.getBoolean(
                com.android.internal.R.styleable.Searchable_autoUrlDetect, false);
        mSettingsDescriptionId = a.getResourceId(
                com.android.internal.R.styleable.Searchable_searchSettingsDescription, 0);
        mSuggestAuthority = a.getString(
                com.android.internal.R.styleable.Searchable_searchSuggestAuthority);
        mSuggestPath = a.getString(
                com.android.internal.R.styleable.Searchable_searchSuggestPath);
        mSuggestSelection = a.getString(
                com.android.internal.R.styleable.Searchable_searchSuggestSelection);
        mSuggestIntentAction = a.getString(
                com.android.internal.R.styleable.Searchable_searchSuggestIntentAction);
        mSuggestIntentData = a.getString(
                com.android.internal.R.styleable.Searchable_searchSuggestIntentData);
        mSuggestThreshold = a.getInt(
                com.android.internal.R.styleable.Searchable_searchSuggestThreshold, 0);
        mVoiceSearchMode = 
            a.getInt(com.android.internal.R.styleable.Searchable_voiceSearchMode, 0);
        mVoiceLanguageModeId = 
            a.getResourceId(com.android.internal.R.styleable.Searchable_voiceLanguageModel, 0);
        mVoicePromptTextId = 
            a.getResourceId(com.android.internal.R.styleable.Searchable_voicePromptText, 0);
        mVoiceLanguageId = 
            a.getResourceId(com.android.internal.R.styleable.Searchable_voiceLanguage, 0);
        mVoiceMaxResults = 
            a.getInt(com.android.internal.R.styleable.Searchable_voiceMaxResults, 0);
        a.recycle();
        String suggestProviderPackage = null;
        if (mSuggestAuthority != null) {
            PackageManager pm = activityContext.getPackageManager();
            ProviderInfo pi = pm.resolveContentProvider(mSuggestAuthority, 0);
            if (pi != null) {
                suggestProviderPackage = pi.packageName;
            }
        }
        mSuggestProviderPackage = suggestProviderPackage;
        if (mLabelId == 0) {
            throw new IllegalArgumentException("Search label must be a resource reference.");
        }
    }
    public static class ActionKeyInfo implements Parcelable {
        private final int mKeyCode;
        private final String mQueryActionMsg;
        private final String mSuggestActionMsg;
        private final String mSuggestActionMsgColumn;
        ActionKeyInfo(Context activityContext, AttributeSet attr) {
            TypedArray a = activityContext.obtainStyledAttributes(attr,
                    com.android.internal.R.styleable.SearchableActionKey);
            mKeyCode = a.getInt(
                    com.android.internal.R.styleable.SearchableActionKey_keycode, 0);
            mQueryActionMsg = a.getString(
                    com.android.internal.R.styleable.SearchableActionKey_queryActionMsg);
            mSuggestActionMsg = a.getString(
                    com.android.internal.R.styleable.SearchableActionKey_suggestActionMsg);
            mSuggestActionMsgColumn = a.getString(
                    com.android.internal.R.styleable.SearchableActionKey_suggestActionMsgColumn);
            a.recycle();
            if (mKeyCode == 0) {
                throw new IllegalArgumentException("No keycode.");
            } else if ((mQueryActionMsg == null) && 
                    (mSuggestActionMsg == null) && 
                    (mSuggestActionMsgColumn == null)) {
                throw new IllegalArgumentException("No message information.");
            }
        }
        private ActionKeyInfo(Parcel in) {
            mKeyCode = in.readInt();
            mQueryActionMsg = in.readString();
            mSuggestActionMsg = in.readString();
            mSuggestActionMsgColumn = in.readString();
        }
        public int getKeyCode() {
            return mKeyCode;
        }
        public String getQueryActionMsg() {
            return mQueryActionMsg;
        }
        public String getSuggestActionMsg() {
            return mSuggestActionMsg;
        }
        public String getSuggestActionMsgColumn() {
            return mSuggestActionMsgColumn;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mKeyCode);
            dest.writeString(mQueryActionMsg);
            dest.writeString(mSuggestActionMsg);
            dest.writeString(mSuggestActionMsgColumn);
        }
    }
    public ActionKeyInfo findActionKey(int keyCode) {
        if (mActionKeys == null) {
            return null;
        }
        return mActionKeys.get(keyCode);
    }
    private void addActionKey(ActionKeyInfo keyInfo) {
        if (mActionKeys == null) {
            mActionKeys = new HashMap<Integer,ActionKeyInfo>();
        }
        mActionKeys.put(keyInfo.getKeyCode(), keyInfo);
    }
    public static SearchableInfo getActivityMetaData(Context context, ActivityInfo activityInfo) {
        XmlResourceParser xml = 
                activityInfo.loadXmlMetaData(context.getPackageManager(), MD_LABEL_SEARCHABLE);
        if (xml == null) {
            return null;
        }
        ComponentName cName = new ComponentName(activityInfo.packageName, activityInfo.name);
        SearchableInfo searchable = getActivityMetaData(context, xml, cName);
        xml.close();
        if (DBG) {
            if (searchable != null) {
                Log.d(LOG_TAG, "Checked " + activityInfo.name
                        + ",label=" + searchable.getLabelId()
                        + ",icon=" + searchable.getIconId()
                        + ",suggestAuthority=" + searchable.getSuggestAuthority()
                        + ",target=" + searchable.getSearchActivity().getClassName()
                        + ",global=" + searchable.shouldIncludeInGlobalSearch()
                        + ",settingsDescription=" + searchable.getSettingsDescriptionId()
                        + ",threshold=" + searchable.getSuggestThreshold());
            } else {
                Log.d(LOG_TAG, "Checked " + activityInfo.name + ", no searchable meta-data");
            }
        }
        return searchable;
    }
    private static SearchableInfo getActivityMetaData(Context context, XmlPullParser xml,
            final ComponentName cName)  {
        SearchableInfo result = null;
        Context activityContext = createActivityContext(context, cName);
        if (activityContext == null) return null;
        try {
            int tagType = xml.next();
            while (tagType != XmlPullParser.END_DOCUMENT) {
                if (tagType == XmlPullParser.START_TAG) {
                    if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE)) {
                        AttributeSet attr = Xml.asAttributeSet(xml);
                        if (attr != null) {
                            try {
                                result = new SearchableInfo(activityContext, attr, cName);
                            } catch (IllegalArgumentException ex) {
                                Log.w(LOG_TAG, "Invalid searchable metadata for " +
                                        cName.flattenToShortString() + ": " + ex.getMessage());
                                return null;
                            }
                        }
                    } else if (xml.getName().equals(MD_XML_ELEMENT_SEARCHABLE_ACTION_KEY)) {
                        if (result == null) {
                            return null;
                        }
                        AttributeSet attr = Xml.asAttributeSet(xml);
                        if (attr != null) {
                            try {
                                result.addActionKey(new ActionKeyInfo(activityContext, attr));
                            } catch (IllegalArgumentException ex) {
                                Log.w(LOG_TAG, "Invalid action key for " +
                                        cName.flattenToShortString() + ": " + ex.getMessage());
                                return null;
                            }
                        }
                    }
                }
                tagType = xml.next();
            }
        } catch (XmlPullParserException e) {
            Log.w(LOG_TAG, "Reading searchable metadata for " + cName.flattenToShortString(), e);
            return null;
        } catch (IOException e) {
            Log.w(LOG_TAG, "Reading searchable metadata for " + cName.flattenToShortString(), e);
            return null;
        }
        return result;
    }
    public int getLabelId() {
        return mLabelId;
    }
    public int getHintId() {
        return mHintId;
    }
    public int getIconId() {
        return mIconId;
    }
    public boolean getVoiceSearchEnabled() {
        return 0 != (mVoiceSearchMode & VOICE_SEARCH_SHOW_BUTTON);
    }
    public boolean getVoiceSearchLaunchWebSearch() {
        return 0 != (mVoiceSearchMode & VOICE_SEARCH_LAUNCH_WEB_SEARCH);
    }
    public boolean getVoiceSearchLaunchRecognizer() {
        return 0 != (mVoiceSearchMode & VOICE_SEARCH_LAUNCH_RECOGNIZER);
    }
    public int getVoiceLanguageModeId() {
        return mVoiceLanguageModeId;
    }
    public int getVoicePromptTextId() {
        return mVoicePromptTextId;
    }
    public int getVoiceLanguageId() {
        return mVoiceLanguageId;
    }
    public int getVoiceMaxResults() {
        return mVoiceMaxResults;
    }
    public int getSearchButtonText() {
        return mSearchButtonText;
    }
    public int getInputType() {
        return mSearchInputType;
    }
    public int getImeOptions() {
        return mSearchImeOptions;
    }
    public boolean shouldIncludeInGlobalSearch() {
        return mIncludeInGlobalSearch;
    }
    public boolean queryAfterZeroResults() {
        return mQueryAfterZeroResults;
    }
    public boolean autoUrlDetect() {
        return mAutoUrlDetect;
    }
    public static final Parcelable.Creator<SearchableInfo> CREATOR
    = new Parcelable.Creator<SearchableInfo>() {
        public SearchableInfo createFromParcel(Parcel in) {
            return new SearchableInfo(in);
        }
        public SearchableInfo[] newArray(int size) {
            return new SearchableInfo[size];
        }
    };
    SearchableInfo(Parcel in) {
        mLabelId = in.readInt();
        mSearchActivity = ComponentName.readFromParcel(in);
        mHintId = in.readInt();
        mSearchMode = in.readInt();
        mIconId = in.readInt();
        mSearchButtonText = in.readInt();
        mSearchInputType = in.readInt();
        mSearchImeOptions = in.readInt();
        mIncludeInGlobalSearch = in.readInt() != 0;
        mQueryAfterZeroResults = in.readInt() != 0;
        mAutoUrlDetect = in.readInt() != 0;
        mSettingsDescriptionId = in.readInt();
        mSuggestAuthority = in.readString();
        mSuggestPath = in.readString();
        mSuggestSelection = in.readString();
        mSuggestIntentAction = in.readString();
        mSuggestIntentData = in.readString();
        mSuggestThreshold = in.readInt();
        for (int count = in.readInt(); count > 0; count--) {
            addActionKey(new ActionKeyInfo(in));
        }
        mSuggestProviderPackage = in.readString();
        mVoiceSearchMode = in.readInt();
        mVoiceLanguageModeId = in.readInt();
        mVoicePromptTextId = in.readInt();
        mVoiceLanguageId = in.readInt();
        mVoiceMaxResults = in.readInt();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLabelId);
        mSearchActivity.writeToParcel(dest, flags);
        dest.writeInt(mHintId);
        dest.writeInt(mSearchMode);
        dest.writeInt(mIconId);
        dest.writeInt(mSearchButtonText);
        dest.writeInt(mSearchInputType);
        dest.writeInt(mSearchImeOptions);
        dest.writeInt(mIncludeInGlobalSearch ? 1 : 0);
        dest.writeInt(mQueryAfterZeroResults ? 1 : 0);
        dest.writeInt(mAutoUrlDetect ? 1 : 0);
        dest.writeInt(mSettingsDescriptionId);
        dest.writeString(mSuggestAuthority);
        dest.writeString(mSuggestPath);
        dest.writeString(mSuggestSelection);
        dest.writeString(mSuggestIntentAction);
        dest.writeString(mSuggestIntentData);
        dest.writeInt(mSuggestThreshold);
        if (mActionKeys == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(mActionKeys.size());
            for (ActionKeyInfo actionKey : mActionKeys.values()) {
                actionKey.writeToParcel(dest, flags);
            }
        }
        dest.writeString(mSuggestProviderPackage);
        dest.writeInt(mVoiceSearchMode);
        dest.writeInt(mVoiceLanguageModeId);
        dest.writeInt(mVoicePromptTextId);
        dest.writeInt(mVoiceLanguageId);
        dest.writeInt(mVoiceMaxResults);
    }
}
