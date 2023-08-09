public class ContactsListActivity extends ListActivity implements View.OnCreateContextMenuListener,
        View.OnClickListener, View.OnKeyListener, TextWatcher, TextView.OnEditorActionListener,
        OnFocusChangeListener, OnTouchListener {
    public static class JoinContactActivity extends ContactsListActivity {
    }
    public static class ContactsSearchActivity extends ContactsListActivity {
    }
    private static final String TAG = "ContactsListActivity";
    private static final boolean ENABLE_ACTION_ICON_OVERLAYS = true;
    private static final String LIST_STATE_KEY = "liststate";
    private static final String SHORTCUT_ACTION_KEY = "shortcutAction";
    static final int MENU_ITEM_VIEW_CONTACT = 1;
    static final int MENU_ITEM_CALL = 2;
    static final int MENU_ITEM_EDIT_BEFORE_CALL = 3;
    static final int MENU_ITEM_SEND_SMS = 4;
    static final int MENU_ITEM_SEND_IM = 5;
    static final int MENU_ITEM_EDIT = 6;
    static final int MENU_ITEM_DELETE = 7;
    static final int MENU_ITEM_TOGGLE_STAR = 8;
    private static final int SUBACTIVITY_NEW_CONTACT = 1;
    private static final int SUBACTIVITY_VIEW_CONTACT = 2;
    private static final int SUBACTIVITY_DISPLAY_GROUP = 3;
    private static final int SUBACTIVITY_SEARCH = 4;
    private static final int SUBACTIVITY_FILTER = 5;
    private static final int TEXT_HIGHLIGHTING_ANIMATION_DURATION = 350;
    public static final String JOIN_AGGREGATE =
            "com.android.contacts.action.JOIN_AGGREGATE";
    public static final String EXTRA_AGGREGATE_ID =
            "com.android.contacts.action.AGGREGATE_ID";
    @Deprecated
    public static final String EXTRA_AGGREGATE_NAME =
            "com.android.contacts.action.AGGREGATE_NAME";
    public static final String AUTHORITIES_FILTER_KEY = "authorities";
    private static final Uri CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS =
            buildSectionIndexerUri(Contacts.CONTENT_URI);
    static final int MODE_MASK_PICKER = 0x80000000;
    static final int MODE_MASK_NO_PRESENCE = 0x40000000;
    static final int MODE_MASK_NO_FILTER = 0x20000000;
    static final int MODE_MASK_CREATE_NEW = 0x10000000;
    static final int MODE_MASK_SHOW_PHOTOS = 0x08000000;
    static final int MODE_MASK_NO_DATA = 0x04000000;
    static final int MODE_MASK_SHOW_CALL_BUTTON = 0x02000000;
    static final int MODE_MASK_DISABLE_QUIKCCONTACT = 0x01000000;
    static final int MODE_MASK_SHOW_NUMBER_OF_CONTACTS = 0x00800000;
    static final int MODE_UNKNOWN = 0;
    static final int MODE_DEFAULT = 4 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    static final int MODE_CUSTOM = 8;
    static final int MODE_STARRED = 20 | MODE_MASK_SHOW_PHOTOS;
    static final int MODE_FREQUENT = 30 | MODE_MASK_SHOW_PHOTOS;
    static final int MODE_STREQUENT = 35 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_SHOW_CALL_BUTTON;
    static final int MODE_PICK_CONTACT = 40 | MODE_MASK_PICKER | MODE_MASK_SHOW_PHOTOS
            | MODE_MASK_DISABLE_QUIKCCONTACT;
    static final int MODE_PICK_OR_CREATE_CONTACT = 42 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW
            | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    static final int MODE_LEGACY_PICK_PERSON = 43 | MODE_MASK_PICKER
            | MODE_MASK_DISABLE_QUIKCCONTACT;
    static final int MODE_LEGACY_PICK_OR_CREATE_PERSON = 44 | MODE_MASK_PICKER
            | MODE_MASK_CREATE_NEW | MODE_MASK_DISABLE_QUIKCCONTACT;
    static final int MODE_INSERT_OR_EDIT_CONTACT = 45 | MODE_MASK_PICKER | MODE_MASK_CREATE_NEW
            | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    static final int MODE_PICK_PHONE = 50 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE;
    static final int MODE_LEGACY_PICK_PHONE =
            51 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    static final int MODE_PICK_POSTAL =
            55 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    static final int MODE_LEGACY_PICK_POSTAL =
            56 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE | MODE_MASK_NO_FILTER;
    static final int MODE_GROUP = 57 | MODE_MASK_SHOW_PHOTOS;
    static final int MODE_QUERY = 60 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_NO_FILTER
            | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    static final int MODE_QUERY_PICK_TO_VIEW = 65 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_PICKER
            | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    static final int MODE_JOIN_CONTACT = 70 | MODE_MASK_PICKER | MODE_MASK_NO_PRESENCE
            | MODE_MASK_NO_DATA | MODE_MASK_SHOW_PHOTOS | MODE_MASK_DISABLE_QUIKCCONTACT;
    static final int MODE_QUERY_PICK = 75 | MODE_MASK_SHOW_PHOTOS | MODE_MASK_NO_FILTER
            | MODE_MASK_PICKER | MODE_MASK_DISABLE_QUIKCCONTACT | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    static final int MODE_QUERY_PICK_PHONE = 80 | MODE_MASK_NO_FILTER | MODE_MASK_PICKER
            | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    static final int MODE_QUERY_PICK_TO_EDIT = 85 | MODE_MASK_NO_FILTER | MODE_MASK_SHOW_PHOTOS
            | MODE_MASK_PICKER | MODE_MASK_SHOW_NUMBER_OF_CONTACTS;
    private static final String ACTION_SEARCH_INTERNAL = "com.android.contacts.INTERNAL_SEARCH";
    static final int MAX_SUGGESTIONS = 4;
    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
        Contacts._ID,                       
        Contacts.DISPLAY_NAME_PRIMARY,      
        Contacts.DISPLAY_NAME_ALTERNATIVE,  
        Contacts.SORT_KEY_PRIMARY,          
        Contacts.STARRED,                   
        Contacts.TIMES_CONTACTED,           
        Contacts.CONTACT_PRESENCE,          
        Contacts.PHOTO_ID,                  
        Contacts.LOOKUP_KEY,                
        Contacts.PHONETIC_NAME,             
        Contacts.HAS_PHONE_NUMBER,          
    };
    static final String[] CONTACTS_SUMMARY_PROJECTION_FROM_EMAIL = new String[] {
        Contacts._ID,                       
        Contacts.DISPLAY_NAME_PRIMARY,      
        Contacts.DISPLAY_NAME_ALTERNATIVE,  
        Contacts.SORT_KEY_PRIMARY,          
        Contacts.STARRED,                   
        Contacts.TIMES_CONTACTED,           
        Contacts.CONTACT_PRESENCE,          
        Contacts.PHOTO_ID,                  
        Contacts.LOOKUP_KEY,                
        Contacts.PHONETIC_NAME,             
    };
    static final String[] CONTACTS_SUMMARY_FILTER_PROJECTION = new String[] {
        Contacts._ID,                       
        Contacts.DISPLAY_NAME_PRIMARY,      
        Contacts.DISPLAY_NAME_ALTERNATIVE,  
        Contacts.SORT_KEY_PRIMARY,          
        Contacts.STARRED,                   
        Contacts.TIMES_CONTACTED,           
        Contacts.CONTACT_PRESENCE,          
        Contacts.PHOTO_ID,                  
        Contacts.LOOKUP_KEY,                
        Contacts.PHONETIC_NAME,             
        Contacts.HAS_PHONE_NUMBER,          
        SearchSnippetColumns.SNIPPET_MIMETYPE, 
        SearchSnippetColumns.SNIPPET_DATA1,     
        SearchSnippetColumns.SNIPPET_DATA4,     
    };
    static final String[] LEGACY_PEOPLE_PROJECTION = new String[] {
        People._ID,                         
        People.DISPLAY_NAME,                
        People.DISPLAY_NAME,                
        People.DISPLAY_NAME,                
        People.STARRED,                     
        PeopleColumns.TIMES_CONTACTED,      
        People.PRESENCE_STATUS,             
    };
    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX = 1;
    static final int SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX = 2;
    static final int SUMMARY_SORT_KEY_PRIMARY_COLUMN_INDEX = 3;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 4;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 5;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 6;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 7;
    static final int SUMMARY_LOOKUP_KEY_COLUMN_INDEX = 8;
    static final int SUMMARY_PHONETIC_NAME_COLUMN_INDEX = 9;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 10;
    static final int SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX = 11;
    static final int SUMMARY_SNIPPET_DATA1_COLUMN_INDEX = 12;
    static final int SUMMARY_SNIPPET_DATA4_COLUMN_INDEX = 13;
    static final String[] PHONES_PROJECTION = new String[] {
        Phone._ID, 
        Phone.TYPE, 
        Phone.LABEL, 
        Phone.NUMBER, 
        Phone.DISPLAY_NAME, 
        Phone.CONTACT_ID, 
    };
    static final String[] LEGACY_PHONES_PROJECTION = new String[] {
        Phones._ID, 
        Phones.TYPE, 
        Phones.LABEL, 
        Phones.NUMBER, 
        People.DISPLAY_NAME, 
    };
    static final int PHONE_ID_COLUMN_INDEX = 0;
    static final int PHONE_TYPE_COLUMN_INDEX = 1;
    static final int PHONE_LABEL_COLUMN_INDEX = 2;
    static final int PHONE_NUMBER_COLUMN_INDEX = 3;
    static final int PHONE_DISPLAY_NAME_COLUMN_INDEX = 4;
    static final int PHONE_CONTACT_ID_COLUMN_INDEX = 5;
    static final String[] POSTALS_PROJECTION = new String[] {
        StructuredPostal._ID, 
        StructuredPostal.TYPE, 
        StructuredPostal.LABEL, 
        StructuredPostal.DATA, 
        StructuredPostal.DISPLAY_NAME, 
    };
    static final String[] LEGACY_POSTALS_PROJECTION = new String[] {
        ContactMethods._ID, 
        ContactMethods.TYPE, 
        ContactMethods.LABEL, 
        ContactMethods.DATA, 
        People.DISPLAY_NAME, 
    };
    static final String[] RAW_CONTACTS_PROJECTION = new String[] {
        RawContacts._ID, 
        RawContacts.CONTACT_ID, 
        RawContacts.ACCOUNT_TYPE, 
    };
    static final int POSTAL_ID_COLUMN_INDEX = 0;
    static final int POSTAL_TYPE_COLUMN_INDEX = 1;
    static final int POSTAL_LABEL_COLUMN_INDEX = 2;
    static final int POSTAL_ADDRESS_COLUMN_INDEX = 3;
    static final int POSTAL_DISPLAY_NAME_COLUMN_INDEX = 4;
    private static final int QUERY_TOKEN = 42;
    static final String KEY_PICKER_MODE = "picker_mode";
    private ContactItemListAdapter mAdapter;
    int mMode = MODE_DEFAULT;
    private QueryHandler mQueryHandler;
    private boolean mJustCreated;
    private boolean mSyncEnabled;
    Uri mSelectedContactUri;
    private boolean mDisplayOnlyPhones;
    private Uri mGroupUri;
    private long mQueryAggregateId;
    private ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();
    private int  mWritableSourcesCnt;
    private int  mReadOnlySourcesCnt;
    private Parcelable mListState = null;
    private String mShortcutAction;
    private int mQueryMode = QUERY_MODE_NONE;
    private static final int QUERY_MODE_NONE = -1;
    private static final int QUERY_MODE_MAILTO = 1;
    private static final int QUERY_MODE_TEL = 2;
    private int mProviderStatus = ProviderStatus.STATUS_NORMAL;
    private boolean mSearchMode;
    private boolean mSearchResultsMode;
    private boolean mShowNumberOfContacts;
    private boolean mShowSearchSnippets;
    private boolean mSearchInitiated;
    private String mInitialFilter;
    private static final String CLAUSE_ONLY_VISIBLE = Contacts.IN_VISIBLE_GROUP + "=1";
    private static final String CLAUSE_ONLY_PHONES = Contacts.HAS_PHONE_NUMBER + "=1";
    private boolean mJoinModeShowAllContacts;
    private static final long JOIN_MODE_SHOW_ALL_CONTACTS_ID = -2;
    private static final int CONTACTS_ID = 1001;
    private static final UriMatcher sContactsIdMatcher;
    private ContactPhotoLoader mPhotoLoader;
    final String[] sLookupProjection = new String[] {
            Contacts.LOOKUP_KEY
    };
    static {
        sContactsIdMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sContactsIdMatcher.addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACTS_ID);
    }
    private class DeleteClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (mSelectedContactUri != null) {
                getContentResolver().delete(mSelectedContactUri, null, null);
            }
        }
    }
    private static class NameHighlightingAnimation extends TextHighlightingAnimation {
        private final ListView mListView;
        private NameHighlightingAnimation(ListView listView, int duration) {
            super(duration);
            this.mListView = listView;
        }
        @Override
        protected void invalidate() {
            int childCount = mListView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View itemView = mListView.getChildAt(i);
                if (itemView instanceof ContactListItemView) {
                    final ContactListItemView view = (ContactListItemView)itemView;
                    view.getNameTextView().invalidate();
                }
            }
        }
        @Override
        protected void onAnimationStarted() {
            mListView.setScrollingCacheEnabled(false);
        }
        @Override
        protected void onAnimationEnded() {
            mListView.setScrollingCacheEnabled(true);
        }
    }
    private int mIconSize;
    private ContactsPreferences mContactsPrefs;
    private int mDisplayOrder;
    private int mSortOrder;
    private boolean mHighlightWhenScrolling;
    private TextHighlightingAnimation mHighlightingAnimation;
    private SearchEditText mSearchEditText;
    private int mPinnedHeaderBackgroundColor;
    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            checkProviderState(true);
        }
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mIconSize = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
        mContactsPrefs = new ContactsPreferences(this);
        mPhotoLoader = new ContactPhotoLoader(this, R.drawable.ic_contact_list_picture);
        final Intent intent = getIntent();
        String title = intent.getStringExtra(UI.TITLE_EXTRA_KEY);
        if (title != null) {
            setTitle(title);
        }
        String action = intent.getAction();
        String component = intent.getComponent().getClassName();
        if (UI.FILTER_CONTACTS_ACTION.equals(action)) {
            mSearchMode = true;
            mShowSearchSnippets = true;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mInitialFilter = extras.getString(UI.FILTER_TEXT_EXTRA_KEY);
                String originalAction =
                        extras.getString(ContactsSearchManager.ORIGINAL_ACTION_EXTRA_KEY);
                if (originalAction != null) {
                    action = originalAction;
                }
                String originalComponent =
                        extras.getString(ContactsSearchManager.ORIGINAL_COMPONENT_EXTRA_KEY);
                if (originalComponent != null) {
                    component = originalComponent;
                }
            } else {
                mInitialFilter = null;
            }
        }
        Log.i(TAG, "Called with action: " + action);
        mMode = MODE_UNKNOWN;
        if (UI.LIST_DEFAULT.equals(action) || UI.FILTER_CONTACTS_ACTION.equals(action)) {
            mMode = MODE_DEFAULT;
        } else if (UI.LIST_GROUP_ACTION.equals(action)) {
            mMode = MODE_GROUP;
            String groupName = intent.getStringExtra(UI.GROUP_NAME_EXTRA_KEY);
            if (TextUtils.isEmpty(groupName)) {
                finish();
                return;
            }
            buildUserGroupUri(groupName);
        } else if (UI.LIST_ALL_CONTACTS_ACTION.equals(action)) {
            mMode = MODE_CUSTOM;
            mDisplayOnlyPhones = false;
        } else if (UI.LIST_STARRED_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_STARRED;
        } else if (UI.LIST_FREQUENT_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_FREQUENT;
        } else if (UI.LIST_STREQUENT_ACTION.equals(action)) {
            mMode = mSearchMode ? MODE_DEFAULT : MODE_STREQUENT;
        } else if (UI.LIST_CONTACTS_WITH_PHONES_ACTION.equals(action)) {
            mMode = MODE_CUSTOM;
            mDisplayOnlyPhones = true;
        } else if (Intent.ACTION_PICK.equals(action)) {
            final String type = intent.resolveType(this);
            if (Contacts.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_CONTACT;
            } else if (People.CONTENT_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PERSON;
            } else if (Phone.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_PHONE;
            } else if (Phones.CONTENT_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PHONE;
            } else if (StructuredPostal.CONTENT_TYPE.equals(type)) {
                mMode = MODE_PICK_POSTAL;
            } else if (ContactMethods.CONTENT_POSTAL_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_POSTAL;
            }
        } else if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            if (component.equals("alias.DialShortcut")) {
                mMode = MODE_PICK_PHONE;
                mShortcutAction = Intent.ACTION_CALL;
                mShowSearchSnippets = false;
                setTitle(R.string.callShortcutActivityTitle);
            } else if (component.equals("alias.MessageShortcut")) {
                mMode = MODE_PICK_PHONE;
                mShortcutAction = Intent.ACTION_SENDTO;
                mShowSearchSnippets = false;
                setTitle(R.string.messageShortcutActivityTitle);
            } else if (mSearchMode) {
                mMode = MODE_PICK_CONTACT;
                mShortcutAction = Intent.ACTION_VIEW;
                setTitle(R.string.shortcutActivityTitle);
            } else {
                mMode = MODE_PICK_OR_CREATE_CONTACT;
                mShortcutAction = Intent.ACTION_VIEW;
                setTitle(R.string.shortcutActivityTitle);
            }
        } else if (Intent.ACTION_GET_CONTENT.equals(action)) {
            final String type = intent.resolveType(this);
            if (Contacts.CONTENT_ITEM_TYPE.equals(type)) {
                if (mSearchMode) {
                    mMode = MODE_PICK_CONTACT;
                } else {
                    mMode = MODE_PICK_OR_CREATE_CONTACT;
                }
            } else if (Phone.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_PICK_PHONE;
            } else if (Phones.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_PHONE;
            } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(type)) {
                mMode = MODE_PICK_POSTAL;
            } else if (ContactMethods.CONTENT_POSTAL_ITEM_TYPE.equals(type)) {
                mMode = MODE_LEGACY_PICK_POSTAL;
            }  else if (People.CONTENT_ITEM_TYPE.equals(type)) {
                if (mSearchMode) {
                    mMode = MODE_LEGACY_PICK_PERSON;
                } else {
                    mMode = MODE_LEGACY_PICK_OR_CREATE_PERSON;
                }
            }
        } else if (Intent.ACTION_INSERT_OR_EDIT.equals(action)) {
            mMode = MODE_INSERT_OR_EDIT_CONTACT;
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG))) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                if (!TextUtils.isEmpty(query)) {
                    Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", query, null));
                    startActivity(newIntent);
                }
                finish();
                return;
            }
            if (intent.hasExtra(Insert.EMAIL)) {
                mMode = MODE_QUERY_PICK_TO_VIEW;
                mQueryMode = QUERY_MODE_MAILTO;
                mInitialFilter = intent.getStringExtra(Insert.EMAIL);
            } else if (intent.hasExtra(Insert.PHONE)) {
                mMode = MODE_QUERY_PICK_TO_VIEW;
                mQueryMode = QUERY_MODE_TEL;
                mInitialFilter = intent.getStringExtra(Insert.PHONE);
            } else {
                mMode = MODE_QUERY;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            }
            mSearchResultsMode = true;
        } else if (ACTION_SEARCH_INTERNAL.equals(action)) {
            String originalAction = null;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                originalAction = extras.getString(ContactsSearchManager.ORIGINAL_ACTION_EXTRA_KEY);
            }
            mShortcutAction = intent.getStringExtra(SHORTCUT_ACTION_KEY);
            if (Intent.ACTION_INSERT_OR_EDIT.equals(originalAction)) {
                mMode = MODE_QUERY_PICK_TO_EDIT;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            } else if (mShortcutAction != null && intent.hasExtra(Insert.PHONE)) {
                mMode = MODE_QUERY_PICK_PHONE;
                mQueryMode = QUERY_MODE_TEL;
                mInitialFilter = intent.getStringExtra(Insert.PHONE);
            } else {
                mMode = MODE_QUERY_PICK;
                mQueryMode = QUERY_MODE_NONE;
                mShowSearchSnippets = true;
                mInitialFilter = getIntent().getStringExtra(SearchManager.QUERY);
            }
            mSearchResultsMode = true;
        } else if (Intents.SEARCH_SUGGESTION_CLICKED.equals(action)) {
            Uri data = intent.getData();
            Uri telUri = null;
            if (sContactsIdMatcher.match(data) == CONTACTS_ID) {
                long contactId = Long.valueOf(data.getLastPathSegment());
                final Cursor cursor = queryPhoneNumbers(contactId);
                if (cursor != null) {
                    if (cursor.getCount() == 1 && cursor.moveToFirst()) {
                        int phoneNumberIndex = cursor.getColumnIndex(Phone.NUMBER);
                        String phoneNumber = cursor.getString(phoneNumberIndex);
                        telUri = Uri.parse("tel:" + phoneNumber);
                    }
                    cursor.close();
                }
            }
            Intent newIntent;
            if ("call".equals(intent.getStringExtra(SearchManager.ACTION_MSG)) && telUri != null) {
                newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, telUri);
            } else {
                newIntent = new Intent(Intent.ACTION_VIEW, data);
            }
            startActivity(newIntent);
            finish();
            return;
        } else if (Intents.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED.equals(action)) {
            Intent newIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED, intent.getData());
            startActivity(newIntent);
            finish();
            return;
        } else if (Intents.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED.equals(action)) {
            String number = intent.getData().getSchemeSpecificPart();
            Intent newIntent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            newIntent.putExtra(Intents.Insert.PHONE, number);
            startActivity(newIntent);
            finish();
            return;
        }
        if (JOIN_AGGREGATE.equals(action)) {
            if (mSearchMode) {
                mMode = MODE_PICK_CONTACT;
            } else {
                mMode = MODE_JOIN_CONTACT;
                mQueryAggregateId = intent.getLongExtra(EXTRA_AGGREGATE_ID, -1);
                if (mQueryAggregateId == -1) {
                    Log.e(TAG, "Intent " + action + " is missing required extra: "
                            + EXTRA_AGGREGATE_ID);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
        if (mMode == MODE_UNKNOWN) {
            mMode = MODE_DEFAULT;
        }
        if (((mMode & MODE_MASK_SHOW_NUMBER_OF_CONTACTS) != 0 || mSearchMode)
                && !mSearchResultsMode) {
            mShowNumberOfContacts = true;
        }
        if (mMode == MODE_JOIN_CONTACT) {
            setContentView(R.layout.contacts_list_content_join);
            TextView blurbView = (TextView)findViewById(R.id.join_contact_blurb);
            String blurb = getString(R.string.blurbJoinContactDataWith,
                    getContactDisplayName(mQueryAggregateId));
            blurbView.setText(blurb);
            mJoinModeShowAllContacts = true;
        } else if (mSearchMode) {
            setContentView(R.layout.contacts_search_content);
        } else if (mSearchResultsMode) {
            setContentView(R.layout.contacts_list_search_results);
            TextView titleText = (TextView)findViewById(R.id.search_results_for);
            titleText.setText(Html.fromHtml(getString(R.string.search_results_for,
                    "<b>" + mInitialFilter + "</b>")));
        } else {
            setContentView(R.layout.contacts_list_content);
        }
        setupListView();
        if (mSearchMode) {
            setupSearchView();
        }
        mQueryHandler = new QueryHandler(this);
        mJustCreated = true;
        mSyncEnabled = true;
    }
    private void registerProviderStatusObserver() {
        getContentResolver().registerContentObserver(ProviderStatus.CONTENT_URI,
                false, mProviderStatusObserver);
    }
    private void unregisterProviderStatusObserver() {
        getContentResolver().unregisterContentObserver(mProviderStatusObserver);
    }
    private void setupListView() {
        final ListView list = getListView();
        final LayoutInflater inflater = getLayoutInflater();
        mHighlightingAnimation =
                new NameHighlightingAnimation(list, TEXT_HIGHLIGHTING_ANIMATION_DURATION);
        list.setDividerHeight(0);
        list.setOnCreateContextMenuListener(this);
        mAdapter = new ContactItemListAdapter(this);
        setListAdapter(mAdapter);
        if (list instanceof PinnedHeaderListView && mAdapter.getDisplaySectionHeadersEnabled()) {
            mPinnedHeaderBackgroundColor =
                    getResources().getColor(R.color.pinned_header_background);
            PinnedHeaderListView pinnedHeaderList = (PinnedHeaderListView)list;
            View pinnedHeader = inflater.inflate(R.layout.list_section, list, false);
            pinnedHeaderList.setPinnedHeaderView(pinnedHeader);
        }
        list.setOnScrollListener(mAdapter);
        list.setOnKeyListener(this);
        list.setOnFocusChangeListener(this);
        list.setOnTouchListener(this);
        list.setSaveEnabled(false);
    }
    private void setupSearchView() {
        mSearchEditText = (SearchEditText)findViewById(R.id.search_src_text);
        mSearchEditText.addTextChangedListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.setText(mInitialFilter);
    }
    private String getContactDisplayName(long contactId) {
        String contactName = null;
        Cursor c = getContentResolver().query(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                new String[] {Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (c != null && c.moveToFirst()) {
                contactName = c.getString(0);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (contactName == null) {
            contactName = "";
        }
        return contactName;
    }
    private int getSummaryDisplayNameColumnIndex() {
        if (mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            return SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX;
        } else {
            return SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX;
        }
    }
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case android.R.id.button1: {
                final int position = (Integer)v.getTag();
                Cursor c = mAdapter.getCursor();
                if (c != null) {
                    c.moveToPosition(position);
                    callContact(c);
                }
                break;
            }
        }
    }
    private void setEmptyText() {
        if (mMode == MODE_JOIN_CONTACT || mSearchMode) {
            return;
        }
        TextView empty = (TextView) findViewById(R.id.emptyText);
        if (mDisplayOnlyPhones) {
            empty.setText(getText(R.string.noContactsWithPhoneNumbers));
        } else if (mMode == MODE_STREQUENT || mMode == MODE_STARRED) {
            empty.setText(getText(R.string.noFavoritesHelpText));
        } else if (mMode == MODE_QUERY || mMode == MODE_QUERY_PICK
                || mMode == MODE_QUERY_PICK_PHONE || mMode == MODE_QUERY_PICK_TO_VIEW
                || mMode == MODE_QUERY_PICK_TO_EDIT) {
            empty.setText(getText(R.string.noMatchingContacts));
        } else {
            boolean hasSim = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE))
                    .hasIccCard();
            boolean createShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction());
            if (isSyncActive()) {
                if (createShortcut) {
                    empty.setText(getText(R.string.noContactsHelpTextWithSyncForCreateShortcut));
                } else if (hasSim) {
                    empty.setText(getText(R.string.noContactsHelpTextWithSync));
                } else {
                    empty.setText(getText(R.string.noContactsNoSimHelpTextWithSync));
                }
            } else {
                if (createShortcut) {
                    empty.setText(getText(R.string.noContactsHelpTextForCreateShortcut));
                } else if (hasSim) {
                    empty.setText(getText(R.string.noContactsHelpText));
                } else {
                    empty.setText(getText(R.string.noContactsNoSimHelpText));
                }
            }
        }
    }
    private boolean isSyncActive() {
        Account[] accounts = AccountManager.get(this).getAccounts();
        if (accounts != null && accounts.length > 0) {
            IContentService contentService = ContentResolver.getContentService();
            for (Account account : accounts) {
                try {
                    if (contentService.isSyncActive(account, ContactsContract.AUTHORITY)) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Could not get the sync status");
                }
            }
        }
        return false;
    }
    private void buildUserGroupUri(String group) {
        mGroupUri = Uri.withAppendedPath(Contacts.CONTENT_GROUP_URI, group);
    }
    private void setDefaultMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDisplayOnlyPhones = prefs.getBoolean(Prefs.DISPLAY_ONLY_PHONES,
                Prefs.DISPLAY_ONLY_PHONES_DEFAULT);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoLoader.stop();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterProviderStatusObserver();
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerProviderStatusObserver();
        mPhotoLoader.resume();
        Activity parent = getParent();
        if (mMode == MODE_DEFAULT) {
            setDefaultMode();
        }
        if (mSearchMode) {
            mSearchEditText.requestFocus();
        }
        if (!mSearchMode && !checkProviderState(mJustCreated)) {
            return;
        }
        if (mJustCreated) {
            startQuery();
        }
        mJustCreated = false;
        mSearchInitiated = false;
    }
    private boolean checkProviderState(boolean loadData) {
        View importFailureView = findViewById(R.id.import_failure);
        if (importFailureView == null) {
            return true;
        }
        TextView messageView = (TextView) findViewById(R.id.emptyText);
        Cursor cursor = getContentResolver().query(ProviderStatus.CONTENT_URI, new String[] {
                ProviderStatus.STATUS, ProviderStatus.DATA1
        }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(0);
                if (status != mProviderStatus) {
                    mProviderStatus = status;
                    switch (status) {
                        case ProviderStatus.STATUS_NORMAL:
                            mAdapter.notifyDataSetInvalidated();
                            if (loadData) {
                                startQuery();
                            }
                            break;
                        case ProviderStatus.STATUS_CHANGING_LOCALE:
                            messageView.setText(R.string.locale_change_in_progress);
                            mAdapter.changeCursor(null);
                            mAdapter.notifyDataSetInvalidated();
                            break;
                        case ProviderStatus.STATUS_UPGRADING:
                            messageView.setText(R.string.upgrade_in_progress);
                            mAdapter.changeCursor(null);
                            mAdapter.notifyDataSetInvalidated();
                            break;
                        case ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY:
                            long size = cursor.getLong(1);
                            String message = getResources().getString(
                                    R.string.upgrade_out_of_memory, new Object[] {size});
                            messageView.setText(message);
                            configureImportFailureView(importFailureView);
                            mAdapter.changeCursor(null);
                            mAdapter.notifyDataSetInvalidated();
                            break;
                    }
                }
            }
        } finally {
            cursor.close();
        }
        importFailureView.setVisibility(
                mProviderStatus == ProviderStatus.STATUS_UPGRADE_OUT_OF_MEMORY
                        ? View.VISIBLE
                        : View.GONE);
        return mProviderStatus == ProviderStatus.STATUS_NORMAL;
    }
    private void configureImportFailureView(View importFailureView) {
        OnClickListener listener = new OnClickListener(){
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.import_failure_uninstall_apps: {
                        startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS));
                        break;
                    }
                    case R.id.import_failure_retry_upgrade: {
                        ContentValues values = new ContentValues();
                        values.put(ProviderStatus.STATUS, ProviderStatus.STATUS_UPGRADING);
                        getContentResolver().update(ProviderStatus.CONTENT_URI, values, null, null);
                        break;
                    }
                }
            }};
        Button uninstallApps = (Button) findViewById(R.id.import_failure_uninstall_apps);
        uninstallApps.setOnClickListener(listener);
        Button retryUpgrade = (Button) findViewById(R.id.import_failure_retry_upgrade);
        retryUpgrade.setOnClickListener(listener);
    }
    private String getTextFilter() {
        if (mSearchEditText != null) {
            return mSearchEditText.getText().toString();
        }
        return null;
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (!checkProviderState(false)) {
            return;
        }
        if (TextUtils.isEmpty(getTextFilter())) {
            startQuery();
        } else {
            mAdapter.onContentChanged();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if (mList != null) {
            icicle.putParcelable(LIST_STATE_KEY, mList.onSaveInstanceState());
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle icicle) {
        super.onRestoreInstanceState(icicle);
        mListState = icicle.getParcelable(LIST_STATE_KEY);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.setSuggestionsCursor(null);
        mAdapter.changeCursor(null);
        if (mMode == MODE_QUERY) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchManager.stopSearch();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
            return false;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean defaultMode = (mMode == MODE_DEFAULT);
        menu.findItem(R.id.menu_display_groups).setVisible(defaultMode);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_display_groups: {
                final Intent intent = new Intent(this, ContactsPreferencesActivity.class);
                startActivityForResult(intent, SUBACTIVITY_DISPLAY_GROUP);
                return true;
            }
            case R.id.menu_search: {
                onSearchRequested();
                return true;
            }
            case R.id.menu_add: {
                final Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                startActivity(intent);
                return true;
            }
            case R.id.menu_import_export: {
                displayImportExportDialog();
                return true;
            }
            case R.id.menu_accounts: {
                final Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                intent.putExtra(AUTHORITIES_FILTER_KEY, new String[] {
                    ContactsContract.AUTHORITY
                });
                startActivity(intent);
                return true;
            }
        }
        return false;
    }
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
            return;
        }
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            if (!mSearchMode && (mMode & MODE_MASK_NO_FILTER) == 0) {
                if ((mMode & MODE_MASK_PICKER) != 0) {
                    ContactsSearchManager.startSearchForResult(this, initialQuery,
                            SUBACTIVITY_FILTER);
                } else {
                    ContactsSearchManager.startSearch(this, initialQuery);
                }
            }
        }
    }
    protected void onSearchTextChanged() {
        setEmptyText();
        Filter filter = mAdapter.getFilter();
        filter.filter(getTextFilter());
    }
    private void doSearch() {
        String query = getTextFilter();
        if (TextUtils.isEmpty(query)) {
            return;
        }
        Intent intent = new Intent(this, SearchResultsActivity.class);
        Intent originalIntent = getIntent();
        Bundle originalExtras = originalIntent.getExtras();
        if (originalExtras != null) {
            intent.putExtras(originalExtras);
        }
        intent.putExtra(SearchManager.QUERY, query);
        if ((mMode & MODE_MASK_PICKER) != 0) {
            intent.setAction(ACTION_SEARCH_INTERNAL);
            intent.putExtra(SHORTCUT_ACTION_KEY, mShortcutAction);
            if (mShortcutAction != null) {
                if (Intent.ACTION_CALL.equals(mShortcutAction)
                        || Intent.ACTION_SENDTO.equals(mShortcutAction)) {
                    intent.putExtra(Insert.PHONE, query);
                }
            } else {
                switch (mQueryMode) {
                    case QUERY_MODE_MAILTO:
                        intent.putExtra(Insert.EMAIL, query);
                        break;
                    case QUERY_MODE_TEL:
                        intent.putExtra(Insert.PHONE, query);
                        break;
                }
            }
            startActivityForResult(intent, SUBACTIVITY_SEARCH);
        } else {
            intent.setAction(Intent.ACTION_SEARCH);
            startActivity(intent);
        }
    }
    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
            case R.string.import_from_sim:
            case R.string.import_from_sdcard: {
                return AccountSelectionUtil.getSelectAccountDialog(this, id);
            }
            case R.id.dialog_sdcard_not_found: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.no_sdcard_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.no_sdcard_message)
                        .setPositiveButton(android.R.string.ok, null).create();
            }
            case R.id.dialog_delete_contact_confirmation: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
            }
            case R.id.dialog_readonly_contact_hide_confirmation: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactWarning)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
            }
            case R.id.dialog_readonly_contact_delete_confirmation: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
            }
            case R.id.dialog_multiple_contact_delete_confirmation: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.multipleContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok,
                                new DeleteClickListener()).create();
            }
        }
        return super.onCreateDialog(id, bundle);
    }
    private void displayImportExportDialog() {
        final Context dialogContext = new ContextThemeWrapper(this, android.R.style.Theme_Light);
        final Resources res = dialogContext.getResources();
        final LayoutInflater dialogInflater = (LayoutInflater)dialogContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = dialogInflater.inflate(android.R.layout.simple_list_item_1,
                            parent, false);
                }
                final int resId = this.getItem(position);
                ((TextView)convertView).setText(resId);
                return convertView;
            }
        };
        if (TelephonyManager.getDefault().hasIccCard()) {
            adapter.add(R.string.import_from_sim);
        }
        if (res.getBoolean(R.bool.config_allow_import_from_sdcard)) {
            adapter.add(R.string.import_from_sdcard);
        }
        if (res.getBoolean(R.bool.config_allow_export_to_sdcard)) {
            adapter.add(R.string.export_to_sdcard);
        }
        if (res.getBoolean(R.bool.config_allow_share_visible_contacts)) {
            adapter.add(R.string.share_visible_contacts);
        }
        final DialogInterface.OnClickListener clickListener =
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final int resId = adapter.getItem(which);
                switch (resId) {
                    case R.string.import_from_sim:
                    case R.string.import_from_sdcard: {
                        handleImportRequest(resId);
                        break;
                    }
                    case R.string.export_to_sdcard: {
                        Context context = ContactsListActivity.this;
                        Intent exportIntent = new Intent(context, ExportVCardActivity.class);
                        context.startActivity(exportIntent);
                        break;
                    }
                    case R.string.share_visible_contacts: {
                        doShareVisibleContacts();
                        break;
                    }
                    default: {
                        Log.e(TAG, "Unexpected resource: " +
                                getResources().getResourceEntryName(resId));
                    }
                }
            }
        };
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_import_export)
            .setNegativeButton(android.R.string.cancel, null)
            .setSingleChoiceItems(adapter, -1, clickListener)
            .show();
    }
    private void doShareVisibleContacts() {
        final Cursor cursor = getContentResolver().query(Contacts.CONTENT_URI,
                sLookupProjection, getContactSelection(), null, null);
        try {
            if (!cursor.moveToFirst()) {
                Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder uriListBuilder = new StringBuilder();
            int index = 0;
            for (;!cursor.isAfterLast(); cursor.moveToNext()) {
                if (index != 0)
                    uriListBuilder.append(':');
                uriListBuilder.append(cursor.getString(0));
                index++;
            }
            Uri uri = Uri.withAppendedPath(
                    Contacts.CONTENT_MULTI_VCARD_URI,
                    Uri.encode(uriListBuilder.toString()));
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(Contacts.CONTENT_VCARD_TYPE);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(intent);
        } finally {
            cursor.close();
        }
    }
    private void handleImportRequest(int resId) {
        final Sources sources = Sources.getInstance(this);
        final List<Account> accountList = sources.getAccounts(true);
        final int size = accountList.size();
        if (size > 1) {
            showDialog(resId);
            return;
        }
        AccountSelectionUtil.doImport(this, resId, (size == 1 ? accountList.get(0) : null));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SUBACTIVITY_NEW_CONTACT:
                if (resultCode == RESULT_OK) {
                    returnPickerResult(null, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME),
                            data.getData());
                }
                break;
            case SUBACTIVITY_VIEW_CONTACT:
                if (resultCode == RESULT_OK) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case SUBACTIVITY_DISPLAY_GROUP:
                mJustCreated = true;
                break;
            case SUBACTIVITY_FILTER:
            case SUBACTIVITY_SEARCH:
                if (resultCode == RESULT_OK) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        if ((mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER) {
            return;
        }
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            return;
        }
        long id = info.id;
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
        long rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), id);
        Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
        menu.setHeaderTitle(cursor.getString(getSummaryDisplayNameColumnIndex()));
        menu.add(0, MENU_ITEM_VIEW_CONTACT, 0, R.string.menu_viewContact)
                .setIntent(new Intent(Intent.ACTION_VIEW, contactUri));
        if (cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0) {
            menu.add(0, MENU_ITEM_CALL, 0,
                    getString(R.string.menu_call));
            menu.add(0, MENU_ITEM_SEND_SMS, 0, getString(R.string.menu_sendSMS));
        }
        int starState = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
        if (starState == 0) {
            menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_addStar);
        } else {
            menu.add(0, MENU_ITEM_TOGGLE_STAR, 0, R.string.menu_removeStar);
        }
        menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_editContact)
                .setIntent(new Intent(Intent.ACTION_EDIT, rawContactUri));
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_deleteContact);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        switch (item.getItemId()) {
            case MENU_ITEM_TOGGLE_STAR: {
                ContentValues values = new ContentValues(1);
                values.put(Contacts.STARRED, cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX) == 0 ? 1 : 0);
                final Uri selectedUri = this.getContactUri(info.position);
                getContentResolver().update(selectedUri, values, null, null);
                return true;
            }
            case MENU_ITEM_CALL: {
                callContact(cursor);
                return true;
            }
            case MENU_ITEM_SEND_SMS: {
                smsContact(cursor);
                return true;
            }
            case MENU_ITEM_DELETE: {
                doContactDelete(getContactUri(info.position));
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (!mSearchMode && (mMode & MODE_MASK_NO_FILTER) == 0 && !mSearchInitiated) {
            int unicodeChar = event.getUnicodeChar();
            if (unicodeChar != 0) {
                mSearchInitiated = true;
                startSearch(new String(new int[]{unicodeChar}, 0, 1), false, null, false);
                return true;
            }
        }
        return false;
    }
    public void afterTextChanged(Editable s) {
        onSearchTextChanged();
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideSoftKeyboard();
            if (TextUtils.isEmpty(getTextFilter())) {
                finish();
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                if (callSelection()) {
                    return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_DEL: {
                if (deleteSelection()) {
                    return true;
                }
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private boolean deleteSelection() {
        if ((mMode & MODE_MASK_PICKER) != 0) {
            return false;
        }
        final int position = getListView().getSelectedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            Uri contactUri = getContactUri(position);
            if (contactUri != null) {
                doContactDelete(contactUri);
                return true;
            }
        }
        return false;
    }
    protected void doContactDelete(Uri contactUri) {
        mReadOnlySourcesCnt = 0;
        mWritableSourcesCnt = 0;
        mWritableRawContactIds.clear();
        Sources sources = Sources.getInstance(ContactsListActivity.this);
        Cursor c = getContentResolver().query(RawContacts.CONTENT_URI, RAW_CONTACTS_PROJECTION,
                RawContacts.CONTACT_ID + "=" + ContentUris.parseId(contactUri), null,
                null);
        if (c != null) {
            try {
                while (c.moveToNext()) {
                    final String accountType = c.getString(2);
                    final long rawContactId = c.getLong(0);
                    ContactsSource contactsSource = sources.getInflatedSource(accountType,
                            ContactsSource.LEVEL_SUMMARY);
                    if (contactsSource != null && contactsSource.readOnly) {
                        mReadOnlySourcesCnt += 1;
                    } else {
                        mWritableSourcesCnt += 1;
                        mWritableRawContactIds.add(rawContactId);
                    }
                }
            } finally {
                c.close();
            }
        }
        mSelectedContactUri = contactUri;
        if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt > 0) {
            showDialog(R.id.dialog_readonly_contact_delete_confirmation);
        } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
            showDialog(R.id.dialog_readonly_contact_hide_confirmation);
        } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
            showDialog(R.id.dialog_multiple_contact_delete_confirmation);
        } else {
            showDialog(R.id.dialog_delete_contact_confirmation);
        }
    }
    public void onFocusChange(View view, boolean hasFocus) {
        if (view == getListView() && hasFocus) {
            hideSoftKeyboard();
        }
    }
    public boolean onTouch(View view, MotionEvent event) {
        if (view == getListView()) {
            hideSoftKeyboard();
        }
        return false;
    }
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (mSearchMode && keyCode == KeyEvent.KEYCODE_BACK && TextUtils.isEmpty(getTextFilter())) {
            hideSoftKeyboard();
            onBackPressed();
            return true;
        }
        return false;
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        hideSoftKeyboard();
        if (mSearchMode && mAdapter.isSearchAllContactsItemPosition(position)) {
            doSearch();
        } else if (mMode == MODE_INSERT_OR_EDIT_CONTACT || mMode == MODE_QUERY_PICK_TO_EDIT) {
            Intent intent;
            if (position == 0 && !mSearchMode && mMode != MODE_QUERY_PICK_TO_EDIT) {
                intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
            } else {
                intent = new Intent(Intent.ACTION_EDIT, getSelectedUri(position));
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            intent.putExtra(KEY_PICKER_MODE, (mMode & MODE_MASK_PICKER) == MODE_MASK_PICKER);
            startActivity(intent);
            finish();
        } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW
                && position == 0) {
            Intent newContact = new Intent(Intents.Insert.ACTION, Contacts.CONTENT_URI);
            startActivityForResult(newContact, SUBACTIVITY_NEW_CONTACT);
        } else if (mMode == MODE_JOIN_CONTACT && id == JOIN_MODE_SHOW_ALL_CONTACTS_ID) {
            mJoinModeShowAllContacts = false;
            startQuery();
        } else if (id > 0) {
            final Uri uri = getSelectedUri(position);
            if ((mMode & MODE_MASK_PICKER) == 0) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivityForResult(intent, SUBACTIVITY_VIEW_CONTACT);
            } else if (mMode == MODE_JOIN_CONTACT) {
                returnPickerResult(null, null, uri);
            } else if (mMode == MODE_QUERY_PICK_TO_VIEW) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                finish();
            } else if (mMode == MODE_PICK_PHONE || mMode == MODE_QUERY_PICK_PHONE) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                returnPickerResult(c, c.getString(PHONE_DISPLAY_NAME_COLUMN_INDEX), uri);
            } else if ((mMode & MODE_MASK_PICKER) != 0) {
                Cursor c = (Cursor) mAdapter.getItem(position);
                returnPickerResult(c, c.getString(getSummaryDisplayNameColumnIndex()), uri);
            } else if (mMode == MODE_PICK_POSTAL
                    || mMode == MODE_LEGACY_PICK_POSTAL
                    || mMode == MODE_LEGACY_PICK_PHONE) {
                returnPickerResult(null, null, uri);
            }
        } else {
            signalError();
        }
    }
    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mList.getWindowToken(), 0);
    }
    private void returnPickerResult(Cursor c, String name, Uri selectedUri) {
        final Intent intent = new Intent();
        if (mShortcutAction != null) {
            Intent shortcutIntent;
            if (Intent.ACTION_VIEW.equals(mShortcutAction)) {
                shortcutIntent = new Intent(ContactsContract.QuickContact.ACTION_QUICK_CONTACT);
                shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                shortcutIntent.setData(selectedUri);
                shortcutIntent.putExtra(ContactsContract.QuickContact.EXTRA_MODE,
                        ContactsContract.QuickContact.MODE_LARGE);
                shortcutIntent.putExtra(ContactsContract.QuickContact.EXTRA_EXCLUDE_MIMES,
                        (String[]) null);
                final Bitmap icon = framePhoto(loadContactPhoto(selectedUri, null));
                if (icon != null) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, scaleToAppIconSize(icon));
                } else {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                            Intent.ShortcutIconResource.fromContext(this,
                                    R.drawable.ic_launcher_shortcut_contact));
                }
            } else {
                String number = c.getString(PHONE_NUMBER_COLUMN_INDEX);
                int type = c.getInt(PHONE_TYPE_COLUMN_INDEX);
                String scheme;
                int resid;
                if (Intent.ACTION_CALL.equals(mShortcutAction)) {
                    scheme = Constants.SCHEME_TEL;
                    resid = R.drawable.badge_action_call;
                } else {
                    scheme = Constants.SCHEME_SMSTO;
                    resid = R.drawable.badge_action_sms;
                }
                Uri phoneUri = Uri.fromParts(scheme, number, null);
                shortcutIntent = new Intent(mShortcutAction, phoneUri);
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                        generatePhoneNumberIcon(selectedUri, type, resid));
            }
            shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            setResult(RESULT_OK, intent);
        } else {
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            setResult(RESULT_OK, intent.setData(selectedUri));
        }
        finish();
    }
    private Bitmap framePhoto(Bitmap photo) {
        final Resources r = getResources();
        final Drawable frame = r.getDrawable(com.android.internal.R.drawable.quickcontact_badge);
        final int width = r.getDimensionPixelSize(R.dimen.contact_shortcut_frame_width);
        final int height = r.getDimensionPixelSize(R.dimen.contact_shortcut_frame_height);
        frame.setBounds(0, 0, width, height);
        final Rect padding = new Rect();
        frame.getPadding(padding);
        final Rect source = new Rect(0, 0, photo.getWidth(), photo.getHeight());
        final Rect destination = new Rect(padding.left, padding.top,
                width - padding.right, height - padding.bottom);
        final int d = Math.max(width, height);
        final Bitmap b = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(b);
        c.translate((d - width) / 2.0f, (d - height) / 2.0f);
        frame.draw(c);
        c.drawBitmap(photo, source, destination, new Paint(Paint.FILTER_BITMAP_FLAG));
        return b;
    }
    private Bitmap generatePhoneNumberIcon(Uri lookupUri, int type, int actionResId) {
        final Resources r = getResources();
        boolean drawPhoneOverlay = true;
        final float scaleDensity = getResources().getDisplayMetrics().scaledDensity;
        Bitmap photo = loadContactPhoto(lookupUri, null);
        if (photo == null) {
            Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
            if (phoneIcon != null) {
                photo = phoneIcon;
                drawPhoneOverlay = false;
            } else {
                return null;
            }
        }
        Bitmap icon = createShortcutBitmap();
        Canvas canvas = new Canvas(icon);
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);
        Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
        Rect dst = new Rect(0,0, mIconSize, mIconSize);
        canvas.drawBitmap(photo, src, dst, photoPaint);
        String overlay = null;
        switch (type) {
            case Phone.TYPE_HOME:
                overlay = getString(R.string.type_short_home);
                break;
            case Phone.TYPE_MOBILE:
                overlay = getString(R.string.type_short_mobile);
                break;
            case Phone.TYPE_WORK:
                overlay = getString(R.string.type_short_work);
                break;
            case Phone.TYPE_PAGER:
                overlay = getString(R.string.type_short_pager);
                break;
            case Phone.TYPE_OTHER:
                overlay = getString(R.string.type_short_other);
                break;
        }
        if (overlay != null) {
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            textPaint.setTextSize(20.0f * scaleDensity);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint.setColor(r.getColor(R.color.textColorIconOverlay));
            textPaint.setShadowLayer(3f, 1, 1, r.getColor(R.color.textColorIconOverlayShadow));
            canvas.drawText(overlay, 2 * scaleDensity, 16 * scaleDensity, textPaint);
        }
        if (ENABLE_ACTION_ICON_OVERLAYS && drawPhoneOverlay) {
            Bitmap phoneIcon = getPhoneActionIcon(r, actionResId);
            if (phoneIcon != null) {
                src.set(0, 0, phoneIcon.getWidth(), phoneIcon.getHeight());
                int iconWidth = icon.getWidth();
                dst.set(iconWidth - ((int) (20 * scaleDensity)), -1,
                        iconWidth, ((int) (19 * scaleDensity)));
                canvas.drawBitmap(phoneIcon, src, dst, photoPaint);
            }
        }
        return icon;
    }
    private Bitmap scaleToAppIconSize(Bitmap photo) {
        Bitmap icon = createShortcutBitmap();
        Canvas canvas = new Canvas(icon);
        Paint photoPaint = new Paint();
        photoPaint.setDither(true);
        photoPaint.setFilterBitmap(true);
        Rect src = new Rect(0,0, photo.getWidth(),photo.getHeight());
        Rect dst = new Rect(0,0, mIconSize, mIconSize);
        canvas.drawBitmap(photo, src, dst, photoPaint);
        return icon;
    }
    private Bitmap createShortcutBitmap() {
        return Bitmap.createBitmap(mIconSize, mIconSize, Bitmap.Config.ARGB_8888);
    }
    private Bitmap getPhoneActionIcon(Resources r, int resId) {
        Drawable phoneIcon = r.getDrawable(resId);
        if (phoneIcon instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) phoneIcon;
            return bd.getBitmap();
        } else {
            return null;
        }
    }
    private Uri getUriToQuery() {
        switch(mMode) {
            case MODE_JOIN_CONTACT:
                return getJoinSuggestionsUri(null);
            case MODE_FREQUENT:
            case MODE_STARRED:
                return Contacts.CONTENT_URI;
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_INSERT_OR_EDIT_CONTACT:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT:{
                return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
            }
            case MODE_STREQUENT: {
                return Contacts.CONTENT_STREQUENT_URI;
            }
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return People.CONTENT_URI;
            }
            case MODE_PICK_PHONE: {
                return buildSectionIndexerUri(Phone.CONTENT_URI);
            }
            case MODE_LEGACY_PICK_PHONE: {
                return Phones.CONTENT_URI;
            }
            case MODE_PICK_POSTAL: {
                return buildSectionIndexerUri(StructuredPostal.CONTENT_URI);
            }
            case MODE_LEGACY_PICK_POSTAL: {
                return ContactMethods.CONTENT_URI;
            }
            case MODE_QUERY_PICK_TO_VIEW: {
                if (mQueryMode == QUERY_MODE_MAILTO) {
                    return Uri.withAppendedPath(Email.CONTENT_FILTER_URI,
                            Uri.encode(mInitialFilter));
                } else if (mQueryMode == QUERY_MODE_TEL) {
                    return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI,
                            Uri.encode(mInitialFilter));
                }
                return CONTACTS_CONTENT_URI_WITH_LETTER_COUNTS;
            }
            case MODE_QUERY:
            case MODE_QUERY_PICK:
            case MODE_QUERY_PICK_TO_EDIT: {
                return getContactFilterUri(mInitialFilter);
            }
            case MODE_QUERY_PICK_PHONE: {
                return Uri.withAppendedPath(Phone.CONTENT_FILTER_URI,
                        Uri.encode(mInitialFilter));
            }
            case MODE_GROUP: {
                return mGroupUri;
            }
            default: {
                throw new IllegalStateException("Can't generate URI: Unsupported Mode.");
            }
        }
    }
    private Uri getContactUri(int position) {
        if (position == ListView.INVALID_POSITION) {
            throw new IllegalArgumentException("Position not in list bounds");
        }
        final Cursor cursor = (Cursor)mAdapter.getItem(position);
        if (cursor == null) {
            return null;
        }
        switch(mMode) {
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                final long personId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                return ContentUris.withAppendedId(People.CONTENT_URI, personId);
            }
            default: {
                final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
                return Contacts.getLookupUri(contactId, lookupKey);
            }
        }
    }
    private Uri getSelectedUri(int position) {
        if (position == ListView.INVALID_POSITION) {
            throw new IllegalArgumentException("Position not in list bounds");
        }
        final long id = mAdapter.getItemId(position);
        switch(mMode) {
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return ContentUris.withAppendedId(People.CONTENT_URI, id);
            }
            case MODE_PICK_PHONE:
            case MODE_QUERY_PICK_PHONE: {
                return ContentUris.withAppendedId(Data.CONTENT_URI, id);
            }
            case MODE_LEGACY_PICK_PHONE: {
                return ContentUris.withAppendedId(Phones.CONTENT_URI, id);
            }
            case MODE_PICK_POSTAL: {
                return ContentUris.withAppendedId(Data.CONTENT_URI, id);
            }
            case MODE_LEGACY_PICK_POSTAL: {
                return ContentUris.withAppendedId(ContactMethods.CONTENT_URI, id);
            }
            default: {
                return getContactUri(position);
            }
        }
    }
    String[] getProjectionForQuery() {
        switch(mMode) {
            case MODE_JOIN_CONTACT:
            case MODE_STREQUENT:
            case MODE_FREQUENT:
            case MODE_STARRED:
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_INSERT_OR_EDIT_CONTACT:
            case MODE_GROUP:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT: {
                return mSearchMode
                        ? CONTACTS_SUMMARY_FILTER_PROJECTION
                        : CONTACTS_SUMMARY_PROJECTION;
            }
            case MODE_QUERY:
            case MODE_QUERY_PICK:
            case MODE_QUERY_PICK_TO_EDIT: {
                return CONTACTS_SUMMARY_FILTER_PROJECTION;
            }
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return LEGACY_PEOPLE_PROJECTION ;
            }
            case MODE_QUERY_PICK_PHONE:
            case MODE_PICK_PHONE: {
                return PHONES_PROJECTION;
            }
            case MODE_LEGACY_PICK_PHONE: {
                return LEGACY_PHONES_PROJECTION;
            }
            case MODE_PICK_POSTAL: {
                return POSTALS_PROJECTION;
            }
            case MODE_LEGACY_PICK_POSTAL: {
                return LEGACY_POSTALS_PROJECTION;
            }
            case MODE_QUERY_PICK_TO_VIEW: {
                if (mQueryMode == QUERY_MODE_MAILTO) {
                    return CONTACTS_SUMMARY_PROJECTION_FROM_EMAIL;
                } else if (mQueryMode == QUERY_MODE_TEL) {
                    return PHONES_PROJECTION;
                }
                break;
            }
        }
        return CONTACTS_SUMMARY_PROJECTION;
    }
    private Bitmap loadContactPhoto(Uri selectedUri, BitmapFactory.Options options) {
        Uri contactUri = null;
        if (Contacts.CONTENT_ITEM_TYPE.equals(getContentResolver().getType(selectedUri))) {
            contactUri = Contacts.lookupContact(getContentResolver(), selectedUri);
        } else {
            Cursor cursor = getContentResolver().query(selectedUri,
                    new String[] { Data.CONTACT_ID }, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    final long contactId = cursor.getLong(0);
                    contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        Cursor cursor = null;
        Bitmap bm = null;
        try {
            Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
            cursor = getContentResolver().query(photoUri, new String[] {Photo.PHOTO},
                    null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                bm = ContactsUtils.loadContactPhoto(cursor, 0, options);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (bm == null) {
            final int[] fallbacks = {
                R.drawable.ic_contact_picture,
                R.drawable.ic_contact_picture_2,
                R.drawable.ic_contact_picture_3
            };
            bm = BitmapFactory.decodeResource(getResources(),
                    fallbacks[new Random().nextInt(fallbacks.length)]);
        }
        return bm;
    }
    private String getContactSelection() {
        if (mDisplayOnlyPhones) {
            return CLAUSE_ONLY_VISIBLE + " AND " + CLAUSE_ONLY_PHONES;
        } else {
            return CLAUSE_ONLY_VISIBLE;
        }
    }
    private Uri getContactFilterUri(String filter) {
        Uri baseUri;
        if (!TextUtils.isEmpty(filter)) {
            baseUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(filter));
        } else {
            baseUri = Contacts.CONTENT_URI;
        }
        if (mAdapter.getDisplaySectionHeadersEnabled()) {
            return buildSectionIndexerUri(baseUri);
        } else {
            return baseUri;
        }
    }
    private Uri getPeopleFilterUri(String filter) {
        if (!TextUtils.isEmpty(filter)) {
            return Uri.withAppendedPath(People.CONTENT_FILTER_URI, Uri.encode(filter));
        } else {
            return People.CONTENT_URI;
        }
    }
    private static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon()
                .appendQueryParameter(ContactCounts.ADDRESS_BOOK_INDEX_EXTRAS, "true").build();
    }
    private Uri getJoinSuggestionsUri(String filter) {
        Builder builder = Contacts.CONTENT_URI.buildUpon();
        builder.appendEncodedPath(String.valueOf(mQueryAggregateId));
        builder.appendEncodedPath(AggregationSuggestions.CONTENT_DIRECTORY);
        if (!TextUtils.isEmpty(filter)) {
            builder.appendEncodedPath(Uri.encode(filter));
        }
        builder.appendQueryParameter("limit", String.valueOf(MAX_SUGGESTIONS));
        return builder.build();
    }
    private String getSortOrder(String[] projectionType) {
        if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY) {
            return Contacts.SORT_KEY_PRIMARY;
        } else {
            return Contacts.SORT_KEY_ALTERNATIVE;
        }
    }
    void startQuery() {
        setEmptyText();
        if (mSearchResultsMode) {
            TextView foundContactsText = (TextView)findViewById(R.id.search_results_found);
            foundContactsText.setText(R.string.search_results_searching);
        }
        mAdapter.setLoading(true);
        mQueryHandler.cancelOperation(QUERY_TOKEN);
        mQueryHandler.setLoadingJoinSuggestions(false);
        mSortOrder = mContactsPrefs.getSortOrder();
        mDisplayOrder = mContactsPrefs.getDisplayOrder();
        mHighlightWhenScrolling = false;
        if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY &&
                mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_ALTERNATIVE) {
            mHighlightWhenScrolling = true;
        } else if (mSortOrder == ContactsContract.Preferences.SORT_ORDER_ALTERNATIVE &&
                mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
            mHighlightWhenScrolling = true;
        }
        String[] projection = getProjectionForQuery();
        if (mSearchMode && TextUtils.isEmpty(getTextFilter())) {
            mAdapter.changeCursor(new MatrixCursor(projection));
            return;
        }
        String callingPackage = getCallingPackage();
        Uri uri = getUriToQuery();
        if (!TextUtils.isEmpty(callingPackage)) {
            uri = uri.buildUpon()
                    .appendQueryParameter(ContactsContract.REQUESTING_PACKAGE_PARAM_KEY,
                            callingPackage)
                    .build();
        }
        switch (mMode) {
            case MODE_GROUP:
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT:
            case MODE_INSERT_OR_EDIT_CONTACT:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, getContactSelection(),
                        null, getSortOrder(projection));
                break;
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null,
                        People.DISPLAY_NAME);
                break;
            }
            case MODE_PICK_POSTAL:
            case MODE_QUERY:
            case MODE_QUERY_PICK:
            case MODE_QUERY_PICK_PHONE:
            case MODE_QUERY_PICK_TO_VIEW:
            case MODE_QUERY_PICK_TO_EDIT: {
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null,
                        getSortOrder(projection));
                break;
            }
            case MODE_STARRED:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, Contacts.STARRED + "=1", null,
                        getSortOrder(projection));
                break;
            case MODE_FREQUENT:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection,
                        Contacts.TIMES_CONTACTED + " > 0", null,
                        Contacts.TIMES_CONTACTED + " DESC, "
                        + getSortOrder(projection));
                break;
            case MODE_STREQUENT:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection, null, null, null);
                break;
            case MODE_PICK_PHONE:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, CLAUSE_ONLY_VISIBLE, null, getSortOrder(projection));
                break;
            case MODE_LEGACY_PICK_PHONE:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection, null, null, Phones.DISPLAY_NAME);
                break;
            case MODE_LEGACY_PICK_POSTAL:
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri,
                        projection,
                        ContactMethods.KIND + "=" + android.provider.Contacts.KIND_POSTAL, null,
                        ContactMethods.DISPLAY_NAME);
                break;
            case MODE_JOIN_CONTACT:
                mQueryHandler.setLoadingJoinSuggestions(true);
                mQueryHandler.startQuery(QUERY_TOKEN, null, uri, projection,
                        null, null, null);
                break;
        }
    }
    Cursor doFilter(String filter) {
        String[] projection = getProjectionForQuery();
        if (mSearchMode && TextUtils.isEmpty(getTextFilter())) {
            return new MatrixCursor(projection);
        }
        final ContentResolver resolver = getContentResolver();
        switch (mMode) {
            case MODE_DEFAULT:
            case MODE_CUSTOM:
            case MODE_PICK_CONTACT:
            case MODE_PICK_OR_CREATE_CONTACT:
            case MODE_INSERT_OR_EDIT_CONTACT: {
                return resolver.query(getContactFilterUri(filter), projection,
                        getContactSelection(), null, getSortOrder(projection));
            }
            case MODE_LEGACY_PICK_PERSON:
            case MODE_LEGACY_PICK_OR_CREATE_PERSON: {
                return resolver.query(getPeopleFilterUri(filter), projection, null, null,
                        People.DISPLAY_NAME);
            }
            case MODE_STARRED: {
                return resolver.query(getContactFilterUri(filter), projection,
                        Contacts.STARRED + "=1", null,
                        getSortOrder(projection));
            }
            case MODE_FREQUENT: {
                return resolver.query(getContactFilterUri(filter), projection,
                        Contacts.TIMES_CONTACTED + " > 0", null,
                        Contacts.TIMES_CONTACTED + " DESC, "
                        + getSortOrder(projection));
            }
            case MODE_STREQUENT: {
                Uri uri;
                if (!TextUtils.isEmpty(filter)) {
                    uri = Uri.withAppendedPath(Contacts.CONTENT_STREQUENT_FILTER_URI,
                            Uri.encode(filter));
                } else {
                    uri = Contacts.CONTENT_STREQUENT_URI;
                }
                return resolver.query(uri, projection, null, null, null);
            }
            case MODE_PICK_PHONE: {
                Uri uri = getUriToQuery();
                if (!TextUtils.isEmpty(filter)) {
                    uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(filter));
                }
                return resolver.query(uri, projection, CLAUSE_ONLY_VISIBLE, null,
                        getSortOrder(projection));
            }
            case MODE_LEGACY_PICK_PHONE: {
                break;
            }
            case MODE_JOIN_CONTACT: {
                Cursor cursor = resolver.query(getJoinSuggestionsUri(filter), projection, null,
                        null, null);
                mAdapter.setSuggestionsCursor(cursor);
                mJoinModeShowAllContacts = false;
                return resolver.query(getContactFilterUri(filter), projection,
                        Contacts._ID + " != " + mQueryAggregateId + " AND " + CLAUSE_ONLY_VISIBLE,
                        null, getSortOrder(projection));
            }
        }
        throw new UnsupportedOperationException("filtering not allowed in mode " + mMode);
    }
    private Cursor getShowAllContactsLabelCursor(String[] projection) {
        MatrixCursor matrixCursor = new MatrixCursor(projection);
        Object[] row = new Object[projection.length];
        row[SUMMARY_ID_COLUMN_INDEX] = JOIN_MODE_SHOW_ALL_CONTACTS_ID;
        matrixCursor.addRow(row);
        return matrixCursor;
    }
    boolean callSelection() {
        ListView list = getListView();
        if (list.hasFocus()) {
            Cursor cursor = (Cursor) list.getSelectedItem();
            return callContact(cursor);
        }
        return false;
    }
    boolean callContact(Cursor cursor) {
        return callOrSmsContact(cursor, false );
    }
    boolean smsContact(Cursor cursor) {
        return callOrSmsContact(cursor, true );
    }
    boolean callOrSmsContact(Cursor cursor, boolean sendSms) {
        if (cursor == null) {
            return false;
        }
        switch (mMode) {
            case MODE_PICK_PHONE:
            case MODE_LEGACY_PICK_PHONE:
            case MODE_QUERY_PICK_PHONE: {
                String phone = cursor.getString(PHONE_NUMBER_COLUMN_INDEX);
                if (sendSms) {
                    ContactsUtils.initiateSms(this, phone);
                } else {
                    ContactsUtils.initiateCall(this, phone);
                }
                return true;
            }
            case MODE_PICK_POSTAL:
            case MODE_LEGACY_PICK_POSTAL: {
                return false;
            }
            default: {
                boolean hasPhone = cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;
                if (!hasPhone) {
                    signalError();
                    return false;
                }
                String phone = null;
                Cursor phonesCursor = null;
                phonesCursor = queryPhoneNumbers(cursor.getLong(SUMMARY_ID_COLUMN_INDEX));
                if (phonesCursor == null || phonesCursor.getCount() == 0) {
                    signalError();
                    return false;
                } else if (phonesCursor.getCount() == 1) {
                    phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                } else {
                    phonesCursor.moveToPosition(-1);
                    while (phonesCursor.moveToNext()) {
                        if (phonesCursor.getInt(phonesCursor.
                                getColumnIndex(Phone.IS_SUPER_PRIMARY)) != 0) {
                            phone = phonesCursor.
                            getString(phonesCursor.getColumnIndex(Phone.NUMBER));
                            break;
                        }
                    }
                }
                if (phone == null) {
                    PhoneDisambigDialog phoneDialog = new PhoneDisambigDialog(
                            this, phonesCursor, sendSms);
                    phoneDialog.show();
                } else {
                    if (sendSms) {
                        ContactsUtils.initiateSms(this, phone);
                    } else {
                        ContactsUtils.initiateCall(this, phone);
                    }
                }
            }
        }
        return true;
    }
    private Cursor queryPhoneNumbers(long contactId) {
        Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri dataUri = Uri.withAppendedPath(baseUri, Contacts.Data.CONTENT_DIRECTORY);
        Cursor c = getContentResolver().query(dataUri,
                new String[] {Phone._ID, Phone.NUMBER, Phone.IS_SUPER_PRIMARY,
                        RawContacts.ACCOUNT_TYPE, Phone.TYPE, Phone.LABEL},
                Data.MIMETYPE + "=?", new String[] {Phone.CONTENT_ITEM_TYPE}, null);
        if (c != null && c.moveToFirst()) {
            return c;
        }
        return null;
    }
    protected String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
        if (count == 0) {
            return getString(zeroResourceId);
        } else {
            String format = getResources().getQuantityText(pluralResourceId, count).toString();
            return String.format(format, count);
        }
    }
    void signalError() {
    }
    Cursor getItemForView(View view) {
        ListView listView = getListView();
        int index = listView.getPositionForView(view);
        if (index < 0) {
            return null;
        }
        return (Cursor) listView.getAdapter().getItem(index);
    }
    private static class QueryHandler extends AsyncQueryHandler {
        protected final WeakReference<ContactsListActivity> mActivity;
        protected boolean mLoadingJoinSuggestions = false;
        public QueryHandler(Context context) {
            super(context.getContentResolver());
            mActivity = new WeakReference<ContactsListActivity>((ContactsListActivity) context);
        }
        public void setLoadingJoinSuggestions(boolean flag) {
            mLoadingJoinSuggestions = flag;
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            final ContactsListActivity activity = mActivity.get();
            if (activity != null && !activity.isFinishing()) {
                if (cursor != null && mLoadingJoinSuggestions) {
                    mLoadingJoinSuggestions = false;
                    if (cursor.getCount() > 0) {
                        activity.mAdapter.setSuggestionsCursor(cursor);
                    } else {
                        cursor.close();
                        activity.mAdapter.setSuggestionsCursor(null);
                    }
                    if (activity.mAdapter.mSuggestionsCursorCount == 0
                            || !activity.mJoinModeShowAllContacts) {
                        startQuery(QUERY_TOKEN, null, activity.getContactFilterUri(
                                        activity.getTextFilter()),
                                CONTACTS_SUMMARY_PROJECTION,
                                Contacts._ID + " != " + activity.mQueryAggregateId
                                        + " AND " + CLAUSE_ONLY_VISIBLE, null,
                                activity.getSortOrder(CONTACTS_SUMMARY_PROJECTION));
                        return;
                    }
                    cursor = activity.getShowAllContactsLabelCursor(CONTACTS_SUMMARY_PROJECTION);
                }
                activity.mAdapter.changeCursor(cursor);
                if (activity.mListState != null) {
                    activity.mList.onRestoreInstanceState(activity.mListState);
                    activity.mListState = null;
                }
            } else {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
    final static class ContactListItemCache {
        public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
        public CharArrayBuffer dataBuffer = new CharArrayBuffer(128);
        public CharArrayBuffer highlightedTextBuffer = new CharArrayBuffer(128);
        public TextWithHighlighting textWithHighlighting;
        public CharArrayBuffer phoneticNameBuffer = new CharArrayBuffer(128);
    }
    final static class PinnedHeaderCache {
        public TextView titleView;
        public ColorStateList textColor;
        public Drawable background;
    }
    private final class ContactItemListAdapter extends CursorAdapter
            implements SectionIndexer, OnScrollListener, PinnedHeaderListView.PinnedHeaderAdapter {
        private SectionIndexer mIndexer;
        private boolean mLoading = true;
        private CharSequence mUnknownNameText;
        private boolean mDisplayPhotos = false;
        private boolean mDisplayCallButton = false;
        private boolean mDisplayAdditionalData = true;
        private int mFrequentSeparatorPos = ListView.INVALID_POSITION;
        private boolean mDisplaySectionHeaders = true;
        private Cursor mSuggestionsCursor;
        private int mSuggestionsCursorCount;
        public ContactItemListAdapter(Context context) {
            super(context, null, false);
            mUnknownNameText = context.getText(android.R.string.unknownName);
            switch (mMode) {
                case MODE_LEGACY_PICK_POSTAL:
                case MODE_PICK_POSTAL:
                case MODE_LEGACY_PICK_PHONE:
                case MODE_PICK_PHONE:
                case MODE_STREQUENT:
                case MODE_FREQUENT:
                    mDisplaySectionHeaders = false;
                    break;
            }
            if (mSearchMode) {
                mDisplaySectionHeaders = false;
            }
            if (mMode != MODE_QUERY_PICK_PHONE && mQueryMode != QUERY_MODE_NONE) {
                mDisplayAdditionalData = false;
            }
            if ((mMode & MODE_MASK_NO_DATA) == MODE_MASK_NO_DATA) {
                mDisplayAdditionalData = false;
            }
            if ((mMode & MODE_MASK_SHOW_CALL_BUTTON) == MODE_MASK_SHOW_CALL_BUTTON) {
                mDisplayCallButton = true;
            }
            if ((mMode & MODE_MASK_SHOW_PHOTOS) == MODE_MASK_SHOW_PHOTOS) {
                mDisplayPhotos = true;
            }
        }
        public boolean getDisplaySectionHeadersEnabled() {
            return mDisplaySectionHeaders;
        }
        public void setSuggestionsCursor(Cursor cursor) {
            if (mSuggestionsCursor != null) {
                mSuggestionsCursor.close();
            }
            mSuggestionsCursor = cursor;
            mSuggestionsCursorCount = cursor == null ? 0 : cursor.getCount();
        }
        @Override
        protected void onContentChanged() {
            CharSequence constraint = getTextFilter();
            if (!TextUtils.isEmpty(constraint)) {
                Filter filter = getFilter();
                filter.filter(constraint);
            } else {
                startQuery();
            }
        }
        public void setLoading(boolean loading) {
            mLoading = loading;
        }
        @Override
        public boolean isEmpty() {
            if (mProviderStatus != ProviderStatus.STATUS_NORMAL) {
                return true;
            }
            if (mSearchMode) {
                return TextUtils.isEmpty(getTextFilter());
            } else if ((mMode & MODE_MASK_CREATE_NEW) == MODE_MASK_CREATE_NEW) {
                return false;
            } else {
                if (mCursor == null || mLoading) {
                    return false;
                } else {
                    return super.isEmpty();
                }
            }
        }
        @Override
        public int getItemViewType(int position) {
            if (position == 0 && (mShowNumberOfContacts || (mMode & MODE_MASK_CREATE_NEW) != 0)) {
                return IGNORE_ITEM_VIEW_TYPE;
            }
            if (isShowAllContactsItemPosition(position)) {
                return IGNORE_ITEM_VIEW_TYPE;
            }
            if (isSearchAllContactsItemPosition(position)) {
                return IGNORE_ITEM_VIEW_TYPE;
            }
            if (getSeparatorId(position) != 0) {
                return IGNORE_ITEM_VIEW_TYPE;
            }
            return super.getItemViewType(position);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (!mDataValid) {
                throw new IllegalStateException(
                        "this should only be called when the cursor is valid");
            }
            if (position == 0 && mShowNumberOfContacts) {
                return getTotalContactCountView(parent);
            }
            if (position == 0 && (mMode & MODE_MASK_CREATE_NEW) != 0) {
                return getLayoutInflater().inflate(R.layout.create_new_contact, parent, false);
            }
            if (isShowAllContactsItemPosition(position)) {
                return getLayoutInflater().
                        inflate(R.layout.contacts_list_show_all_item, parent, false);
            }
            if (isSearchAllContactsItemPosition(position)) {
                return getLayoutInflater().
                        inflate(R.layout.contacts_list_search_all_item, parent, false);
            }
            int separatorId = getSeparatorId(position);
            if (separatorId != 0) {
                TextView view = (TextView) getLayoutInflater().
                        inflate(R.layout.list_separator, parent, false);
                view.setText(separatorId);
                return view;
            }
            boolean showingSuggestion;
            Cursor cursor;
            if (mSuggestionsCursorCount != 0 && position < mSuggestionsCursorCount + 2) {
                showingSuggestion = true;
                cursor = mSuggestionsCursor;
            } else {
                showingSuggestion = false;
                cursor = mCursor;
            }
            int realPosition = getRealPosition(position);
            if (!cursor.moveToPosition(realPosition)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            boolean newView;
            View v;
            if (convertView == null || convertView.getTag() == null) {
                newView = true;
                v = newView(mContext, cursor, parent);
            } else {
                newView = false;
                v = convertView;
            }
            bindView(v, mContext, cursor);
            bindSectionHeader(v, realPosition, mDisplaySectionHeaders && !showingSuggestion);
            return v;
        }
        private View getTotalContactCountView(ViewGroup parent) {
            final LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.total_contacts, parent, false);
            TextView totalContacts = (TextView) view.findViewById(R.id.totalContactsText);
            String text;
            int count = getRealCount();
            if (mSearchMode && !TextUtils.isEmpty(getTextFilter())) {
                text = getQuantityText(count, R.string.listFoundAllContactsZero,
                        R.plurals.searchFoundContacts);
            } else {
                if (mDisplayOnlyPhones) {
                    text = getQuantityText(count, R.string.listTotalPhoneContactsZero,
                            R.plurals.listTotalPhoneContacts);
                } else {
                    text = getQuantityText(count, R.string.listTotalAllContactsZero,
                            R.plurals.listTotalAllContacts);
                }
            }
            totalContacts.setText(text);
            return view;
        }
        private boolean isShowAllContactsItemPosition(int position) {
            return mMode == MODE_JOIN_CONTACT && mJoinModeShowAllContacts
                    && mSuggestionsCursorCount != 0 && position == mSuggestionsCursorCount + 2;
        }
        private boolean isSearchAllContactsItemPosition(int position) {
            return mSearchMode && position == getCount() - 1;
        }
        private int getSeparatorId(int position) {
            int separatorId = 0;
            if (position == mFrequentSeparatorPos) {
                separatorId = R.string.favoritesFrquentSeparator;
            }
            if (mSuggestionsCursorCount != 0) {
                if (position == 0) {
                    separatorId = R.string.separatorJoinAggregateSuggestions;
                } else if (position == mSuggestionsCursorCount + 1) {
                    separatorId = R.string.separatorJoinAggregateAll;
                }
            }
            return separatorId;
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final ContactListItemView view = new ContactListItemView(context, null);
            view.setOnCallButtonClickListener(ContactsListActivity.this);
            view.setTag(new ContactListItemCache());
            return view;
        }
        @Override
        public void bindView(View itemView, Context context, Cursor cursor) {
            final ContactListItemView view = (ContactListItemView)itemView;
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();
            int typeColumnIndex;
            int dataColumnIndex;
            int labelColumnIndex;
            int defaultType;
            int nameColumnIndex;
            int phoneticNameColumnIndex;
            boolean displayAdditionalData = mDisplayAdditionalData;
            boolean highlightingEnabled = false;
            switch(mMode) {
                case MODE_PICK_PHONE:
                case MODE_LEGACY_PICK_PHONE:
                case MODE_QUERY_PICK_PHONE: {
                    nameColumnIndex = PHONE_DISPLAY_NAME_COLUMN_INDEX;
                    phoneticNameColumnIndex = -1;
                    dataColumnIndex = PHONE_NUMBER_COLUMN_INDEX;
                    typeColumnIndex = PHONE_TYPE_COLUMN_INDEX;
                    labelColumnIndex = PHONE_LABEL_COLUMN_INDEX;
                    defaultType = Phone.TYPE_HOME;
                    break;
                }
                case MODE_PICK_POSTAL:
                case MODE_LEGACY_PICK_POSTAL: {
                    nameColumnIndex = POSTAL_DISPLAY_NAME_COLUMN_INDEX;
                    phoneticNameColumnIndex = -1;
                    dataColumnIndex = POSTAL_ADDRESS_COLUMN_INDEX;
                    typeColumnIndex = POSTAL_TYPE_COLUMN_INDEX;
                    labelColumnIndex = POSTAL_LABEL_COLUMN_INDEX;
                    defaultType = StructuredPostal.TYPE_HOME;
                    break;
                }
                default: {
                    nameColumnIndex = getSummaryDisplayNameColumnIndex();
                    if (mMode == MODE_LEGACY_PICK_PERSON
                            || mMode == MODE_LEGACY_PICK_OR_CREATE_PERSON) {
                        phoneticNameColumnIndex = -1;
                    } else {
                        phoneticNameColumnIndex = SUMMARY_PHONETIC_NAME_COLUMN_INDEX;
                    }
                    dataColumnIndex = -1;
                    typeColumnIndex = -1;
                    labelColumnIndex = -1;
                    defaultType = Phone.TYPE_HOME;
                    displayAdditionalData = false;
                    highlightingEnabled = mHighlightWhenScrolling && mMode != MODE_STREQUENT;
                }
            }
            cursor.copyStringToBuffer(nameColumnIndex, cache.nameBuffer);
            TextView nameView = view.getNameTextView();
            int size = cache.nameBuffer.sizeCopied;
            if (size != 0) {
                if (highlightingEnabled) {
                    if (cache.textWithHighlighting == null) {
                        cache.textWithHighlighting =
                                mHighlightingAnimation.createTextWithHighlighting();
                    }
                    buildDisplayNameWithHighlighting(nameView, cursor, cache.nameBuffer,
                            cache.highlightedTextBuffer, cache.textWithHighlighting);
                } else {
                    nameView.setText(cache.nameBuffer.data, 0, size);
                }
            } else {
                nameView.setText(mUnknownNameText);
            }
            boolean hasPhone = cursor.getColumnCount() >= SUMMARY_HAS_PHONE_COLUMN_INDEX
                    && cursor.getInt(SUMMARY_HAS_PHONE_COLUMN_INDEX) != 0;
            if (mDisplayCallButton && hasPhone) {
                int pos = cursor.getPosition();
                view.showCallButton(android.R.id.button1, pos);
            } else {
                view.hideCallButton();
            }
            if (mDisplayPhotos) {
                boolean useQuickContact = (mMode & MODE_MASK_DISABLE_QUIKCCONTACT) == 0;
                long photoId = 0;
                if (!cursor.isNull(SUMMARY_PHOTO_ID_COLUMN_INDEX)) {
                    photoId = cursor.getLong(SUMMARY_PHOTO_ID_COLUMN_INDEX);
                }
                ImageView viewToUse;
                if (useQuickContact) {
                    final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
                    final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY_COLUMN_INDEX);
                    QuickContactBadge quickContact = view.getQuickContact();
                    quickContact.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
                    viewToUse = quickContact;
                } else {
                    viewToUse = view.getPhotoView();
                }
                final int position = cursor.getPosition();
                mPhotoLoader.loadPhoto(viewToUse, photoId);
            }
            if ((mMode & MODE_MASK_NO_PRESENCE) == 0) {
                int serverStatus;
                if (!cursor.isNull(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX)) {
                    serverStatus = cursor.getInt(SUMMARY_PRESENCE_STATUS_COLUMN_INDEX);
                    Drawable icon = ContactPresenceIconUtil.getPresenceIcon(mContext, serverStatus);
                    if (icon != null) {
                        view.setPresence(icon);
                    } else {
                        view.setPresence(null);
                    }
                } else {
                    view.setPresence(null);
                }
            } else {
                view.setPresence(null);
            }
            if (mShowSearchSnippets) {
                boolean showSnippet = false;
                String snippetMimeType = cursor.getString(SUMMARY_SNIPPET_MIMETYPE_COLUMN_INDEX);
                if (Email.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                    String email = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                    if (!TextUtils.isEmpty(email)) {
                        view.setSnippet(email);
                        showSnippet = true;
                    }
                } else if (Organization.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                    String company = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                    String title = cursor.getString(SUMMARY_SNIPPET_DATA4_COLUMN_INDEX);
                    if (!TextUtils.isEmpty(company)) {
                        if (!TextUtils.isEmpty(title)) {
                            view.setSnippet(company + " / " + title);
                        } else {
                            view.setSnippet(company);
                        }
                        showSnippet = true;
                    } else if (!TextUtils.isEmpty(title)) {
                        view.setSnippet(title);
                        showSnippet = true;
                    }
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(snippetMimeType)) {
                    String nickname = cursor.getString(SUMMARY_SNIPPET_DATA1_COLUMN_INDEX);
                    if (!TextUtils.isEmpty(nickname)) {
                        view.setSnippet(nickname);
                        showSnippet = true;
                    }
                }
                if (!showSnippet) {
                    view.setSnippet(null);
                }
            }
            if (!displayAdditionalData) {
                if (phoneticNameColumnIndex != -1) {
                    cursor.copyStringToBuffer(phoneticNameColumnIndex, cache.phoneticNameBuffer);
                    int phoneticNameSize = cache.phoneticNameBuffer.sizeCopied;
                    if (phoneticNameSize != 0) {
                        view.setLabel(cache.phoneticNameBuffer.data, phoneticNameSize);
                    } else {
                        view.setLabel(null);
                    }
                } else {
                    view.setLabel(null);
                }
                return;
            }
            cursor.copyStringToBuffer(dataColumnIndex, cache.dataBuffer);
            size = cache.dataBuffer.sizeCopied;
            view.setData(cache.dataBuffer.data, size);
            if (!cursor.isNull(typeColumnIndex)) {
                final int type = cursor.getInt(typeColumnIndex);
                final String label = cursor.getString(labelColumnIndex);
                if (mMode == MODE_LEGACY_PICK_POSTAL || mMode == MODE_PICK_POSTAL) {
                    view.setLabel(StructuredPostal.getTypeLabel(context.getResources(), type,
                            label));
                } else {
                    view.setLabel(Phone.getTypeLabel(context.getResources(), type, label));
                }
            } else {
                view.setLabel(null);
            }
        }
        private void buildDisplayNameWithHighlighting(TextView textView, Cursor cursor,
                CharArrayBuffer buffer1, CharArrayBuffer buffer2,
                TextWithHighlighting textWithHighlighting) {
            int oppositeDisplayOrderColumnIndex;
            if (mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY) {
                oppositeDisplayOrderColumnIndex = SUMMARY_DISPLAY_NAME_ALTERNATIVE_COLUMN_INDEX;
            } else {
                oppositeDisplayOrderColumnIndex = SUMMARY_DISPLAY_NAME_PRIMARY_COLUMN_INDEX;
            }
            cursor.copyStringToBuffer(oppositeDisplayOrderColumnIndex, buffer2);
            textWithHighlighting.setText(buffer1, buffer2);
            textView.setText(textWithHighlighting);
        }
        private void bindSectionHeader(View itemView, int position, boolean displaySectionHeaders) {
            final ContactListItemView view = (ContactListItemView)itemView;
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();
            if (!displaySectionHeaders) {
                view.setSectionHeader(null);
                view.setDividerVisible(true);
            } else {
                final int section = getSectionForPosition(position);
                if (getPositionForSection(section) == position) {
                    String title = (String)mIndexer.getSections()[section];
                    view.setSectionHeader(title);
                } else {
                    view.setDividerVisible(false);
                    view.setSectionHeader(null);
                }
                if (getPositionForSection(section + 1) - 1 == position) {
                    view.setDividerVisible(false);
                } else {
                    view.setDividerVisible(true);
                }
            }
        }
        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor != null) {
                setLoading(false);
            }
            mFrequentSeparatorPos = ListView.INVALID_POSITION;
            int cursorCount = 0;
            if (cursor != null && (cursorCount = cursor.getCount()) > 0
                    && mMode == MODE_STREQUENT) {
                cursor.move(-1);
                for (int i = 0; cursor.moveToNext(); i++) {
                    int starred = cursor.getInt(SUMMARY_STARRED_COLUMN_INDEX);
                    if (starred == 0) {
                        if (i > 0) {
                            mFrequentSeparatorPos = i;
                        }
                        break;
                    }
                }
            }
            if (cursor != null && mSearchResultsMode) {
                TextView foundContactsText = (TextView)findViewById(R.id.search_results_found);
                String text = getQuantityText(cursor.getCount(), R.string.listFoundAllContactsZero,
                        R.plurals.listFoundAllContacts);
                foundContactsText.setText(text);
            }
            super.changeCursor(cursor);
            updateIndexer(cursor);
        }
        private void updateIndexer(Cursor cursor) {
            if (cursor == null) {
                mIndexer = null;
                return;
            }
            Bundle bundle = cursor.getExtras();
            if (bundle.containsKey(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES)) {
                String sections[] =
                    bundle.getStringArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_TITLES);
                int counts[] = bundle.getIntArray(ContactCounts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS);
                mIndexer = new ContactsSectionIndexer(sections, counts);
            } else {
                mIndexer = null;
            }
        }
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            return doFilter(constraint.toString());
        }
        public Object [] getSections() {
            if (mIndexer == null) {
                return new String[] { " " };
            } else {
                return mIndexer.getSections();
            }
        }
        public int getPositionForSection(int sectionIndex) {
            if (mIndexer == null) {
                return -1;
            }
            return mIndexer.getPositionForSection(sectionIndex);
        }
        public int getSectionForPosition(int position) {
            if (mIndexer == null) {
                return -1;
            }
            return mIndexer.getSectionForPosition(position);
        }
        @Override
        public boolean areAllItemsEnabled() {
            return mMode != MODE_STARRED
                && !mShowNumberOfContacts
                && mSuggestionsCursorCount == 0;
        }
        @Override
        public boolean isEnabled(int position) {
            if (mShowNumberOfContacts) {
                if (position == 0) {
                    return false;
                }
                position--;
            }
            if (mSuggestionsCursorCount > 0) {
                return position != 0 && position != mSuggestionsCursorCount + 1;
            }
            return position != mFrequentSeparatorPos;
        }
        @Override
        public int getCount() {
            if (!mDataValid) {
                return 0;
            }
            int superCount = super.getCount();
            if (mShowNumberOfContacts && (mSearchMode || superCount > 0)) {
                superCount++;
            }
            if (mSearchMode) {
                superCount++;
            }
            if ((mMode & MODE_MASK_CREATE_NEW) != 0 && !mSearchMode) {
                superCount++;
            }
            if (mSuggestionsCursorCount != 0) {
                return mSuggestionsCursorCount + superCount + 2;
            }
            else if (mFrequentSeparatorPos != ListView.INVALID_POSITION) {
                return superCount + 1;
            } else {
                return superCount;
            }
        }
        public int getRealCount() {
            return super.getCount();
        }
        private int getRealPosition(int pos) {
            if (mShowNumberOfContacts) {
                pos--;
            }
            if ((mMode & MODE_MASK_CREATE_NEW) != 0 && !mSearchMode) {
                return pos - 1;
            } else if (mSuggestionsCursorCount != 0) {
                if (pos < mSuggestionsCursorCount + 2) {
                    return pos - 1;
                } else {
                    return pos - mSuggestionsCursorCount - 2;
                }
            } else if (mFrequentSeparatorPos == ListView.INVALID_POSITION) {
                return pos;
            } else if (pos <= mFrequentSeparatorPos) {
                return pos;
            } else {
                return pos - 1;
            }
        }
        @Override
        public Object getItem(int pos) {
            if (mSuggestionsCursorCount != 0 && pos <= mSuggestionsCursorCount) {
                mSuggestionsCursor.moveToPosition(getRealPosition(pos));
                return mSuggestionsCursor;
            } else if (isSearchAllContactsItemPosition(pos)){
                return null;
            } else {
                int realPosition = getRealPosition(pos);
                if (realPosition < 0) {
                    return null;
                }
                return super.getItem(realPosition);
            }
        }
        @Override
        public long getItemId(int pos) {
            if (mSuggestionsCursorCount != 0 && pos < mSuggestionsCursorCount + 2) {
                if (mSuggestionsCursor.moveToPosition(pos - 1)) {
                    return mSuggestionsCursor.getLong(mRowIDColumn);
                } else {
                    return 0;
                }
            } else if (isSearchAllContactsItemPosition(pos)) {
                return 0;
            }
            int realPosition = getRealPosition(pos);
            if (realPosition < 0) {
                return 0;
            }
            return super.getItemId(realPosition);
        }
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            if (view instanceof PinnedHeaderListView) {
                ((PinnedHeaderListView)view).configureHeaderView(firstVisibleItem);
            }
        }
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mHighlightWhenScrolling) {
                if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                    mHighlightingAnimation.startHighlighting();
                } else {
                    mHighlightingAnimation.stopHighlighting();
                }
            }
            if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                mPhotoLoader.pause();
            } else if (mDisplayPhotos) {
                mPhotoLoader.resume();
            }
        }
        public int getPinnedHeaderState(int position) {
            if (mIndexer == null || mCursor == null || mCursor.getCount() == 0) {
                return PINNED_HEADER_GONE;
            }
            int realPosition = getRealPosition(position);
            if (realPosition < 0) {
                return PINNED_HEADER_GONE;
            }
            int section = getSectionForPosition(realPosition);
            int nextSectionPosition = getPositionForSection(section + 1);
            if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
                return PINNED_HEADER_PUSHED_UP;
            }
            return PINNED_HEADER_VISIBLE;
        }
        public void configurePinnedHeader(View header, int position, int alpha) {
            PinnedHeaderCache cache = (PinnedHeaderCache)header.getTag();
            if (cache == null) {
                cache = new PinnedHeaderCache();
                cache.titleView = (TextView)header.findViewById(R.id.header_text);
                cache.textColor = cache.titleView.getTextColors();
                cache.background = header.getBackground();
                header.setTag(cache);
            }
            int realPosition = getRealPosition(position);
            int section = getSectionForPosition(realPosition);
            String title = (String)mIndexer.getSections()[section];
            cache.titleView.setText(title);
            if (alpha == 255) {
                header.setBackgroundDrawable(cache.background);
                cache.titleView.setTextColor(cache.textColor);
            } else {
                header.setBackgroundColor(Color.rgb(
                        Color.red(mPinnedHeaderBackgroundColor) * alpha / 255,
                        Color.green(mPinnedHeaderBackgroundColor) * alpha / 255,
                        Color.blue(mPinnedHeaderBackgroundColor) * alpha / 255));
                int textColor = cache.textColor.getDefaultColor();
                cache.titleView.setTextColor(Color.argb(alpha,
                        Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
            }
        }
    }
}
