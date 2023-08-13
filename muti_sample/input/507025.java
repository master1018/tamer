public class ViewContactActivity extends Activity
        implements View.OnCreateContextMenuListener, DialogInterface.OnClickListener,
        AdapterView.OnItemClickListener, NotifyingAsyncQueryHandler.AsyncQueryListener {
    private static final String TAG = "ViewContact";
    private static final boolean SHOW_SEPARATORS = false;
    private static final int DIALOG_CONFIRM_DELETE = 1;
    private static final int DIALOG_CONFIRM_READONLY_DELETE = 2;
    private static final int DIALOG_CONFIRM_MULTIPLE_DELETE = 3;
    private static final int DIALOG_CONFIRM_READONLY_HIDE = 4;
    private static final int REQUEST_JOIN_CONTACT = 1;
    private static final int REQUEST_EDIT_CONTACT = 2;
    public static final int MENU_ITEM_MAKE_DEFAULT = 3;
    protected Uri mLookupUri;
    private ContentResolver mResolver;
    private ViewAdapter mAdapter;
    private int mNumPhoneNumbers = 0;
    private ArrayList<Long> mRawContactIds = new ArrayList<Long>();
     ArrayList<ViewEntry> mPhoneEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mSmsEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mEmailEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mPostalEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mImEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mNicknameEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mOrganizationEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mGroupEntries = new ArrayList<ViewEntry>();
     ArrayList<ViewEntry> mOtherEntries = new ArrayList<ViewEntry>();
     ArrayList<ArrayList<ViewEntry>> mSections = new ArrayList<ArrayList<ViewEntry>>();
    private Cursor mCursor;
    protected ContactHeaderWidget mContactHeaderWidget;
    private NotifyingAsyncQueryHandler mHandler;
    protected LayoutInflater mInflater;
    protected int mReadOnlySourcesCnt;
    protected int mWritableSourcesCnt;
    protected boolean mAllRestricted;
    protected Uri mPrimaryPhoneUri = null;
    protected ArrayList<Long> mWritableRawContactIds = new ArrayList<Long>();
    private static final int TOKEN_ENTITIES = 0;
    private static final int TOKEN_STATUSES = 1;
    private boolean mHasEntities = false;
    private boolean mHasStatuses = false;
    private long mNameRawContactId = -1;
    private int mDisplayNameSource = DisplayNameSources.UNDEFINED;
    private ArrayList<Entity> mEntities = Lists.newArrayList();
    private HashMap<Long, DataStatus> mStatuses = Maps.newHashMap();
    private View mEmptyView;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            if (mCursor != null && !mCursor.isClosed()) {
                startEntityQuery();
            }
        }
    };
    public void onClick(DialogInterface dialog, int which) {
        closeCursor();
        getContentResolver().delete(mLookupUri, null, null);
        finish();
    }
    private ListView mListView;
    private boolean mShowSmsLinksForAllPhones;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Intent intent = getIntent();
        Uri data = intent.getData();
        String authority = data.getAuthority();
        if (ContactsContract.AUTHORITY.equals(authority)) {
            mLookupUri = data;
        } else if (android.provider.Contacts.AUTHORITY.equals(authority)) {
            final long rawContactId = ContentUris.parseId(data);
            mLookupUri = RawContacts.getContactLookupUri(getContentResolver(),
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId));
        }
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.contact_card_layout);
        mContactHeaderWidget = (ContactHeaderWidget) findViewById(R.id.contact_header_widget);
        mContactHeaderWidget.showStar(true);
        mContactHeaderWidget.setExcludeMimes(new String[] {
            Contacts.CONTENT_ITEM_TYPE
        });
        mHandler = new NotifyingAsyncQueryHandler(this, this);
        mListView = (ListView) findViewById(R.id.contact_data);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setOnItemClickListener(this);
        mEmptyView = findViewById(android.R.id.empty);
        mResolver = getContentResolver();
        mSections.add(mPhoneEntries);
        mSections.add(mSmsEntries);
        mSections.add(mEmailEntries);
        mSections.add(mImEntries);
        mSections.add(mPostalEntries);
        mSections.add(mNicknameEntries);
        mSections.add(mOrganizationEntries);
        mSections.add(mGroupEntries);
        mSections.add(mOtherEntries);
        mShowSmsLinksForAllPhones = true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        startEntityQuery();
    }
    @Override
    protected void onPause() {
        super.onPause();
        closeCursor();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeCursor();
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONFIRM_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.deleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .setCancelable(false)
                        .create();
            case DIALOG_CONFIRM_READONLY_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .setCancelable(false)
                        .create();
            case DIALOG_CONFIRM_MULTIPLE_DELETE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.multipleContactDeleteConfirmation)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, this)
                        .setCancelable(false)
                        .create();
            case DIALOG_CONFIRM_READONLY_HIDE: {
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.deleteConfirmation_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.readOnlyContactWarning)
                        .setPositiveButton(android.R.string.ok, this)
                        .create();
            }
        }
        return null;
    }
    public void onQueryComplete(int token, Object cookie, final Cursor cursor) {
        if (token == TOKEN_STATUSES) {
            try {
                readStatuses(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            considerBindData();
            return;
        }
        final ArrayList<Entity> oldEntities = mEntities;
        (new AsyncTask<Void, Void, ArrayList<Entity>>() {
            @Override
            protected ArrayList<Entity> doInBackground(Void... params) {
                ArrayList<Entity> newEntities = new ArrayList<Entity>(cursor.getCount());
                EntityIterator iterator = RawContacts.newEntityIterator(cursor);
                try {
                    while (iterator.hasNext()) {
                        Entity entity = iterator.next();
                        newEntities.add(entity);
                    }
                } finally {
                    iterator.close();
                }
                return newEntities;
            }
            @Override
            protected void onPostExecute(ArrayList<Entity> newEntities) {
                if (newEntities == null) {
                    return;
                }
                synchronized (ViewContactActivity.this) {
                    if (mEntities != oldEntities) {
                        return;
                    }
                    mEntities = newEntities;
                    mHasEntities = true;
                }
                considerBindData();
            }
        }).execute();
    }
    private long getRefreshedContactId() {
        Uri freshContactUri = Contacts.lookupContact(getContentResolver(), mLookupUri);
        if (freshContactUri != null) {
            return ContentUris.parseId(freshContactUri);
        }
        return -1;
    }
    private synchronized void readStatuses(Cursor cursor) {
        mStatuses.clear();
        while (cursor.moveToNext()) {
            final DataStatus status = new DataStatus(cursor);
            final long dataId = cursor.getLong(StatusQuery._ID);
            mStatuses.put(dataId, status);
        }
        mHasStatuses = true;
    }
    private static Cursor setupContactCursor(ContentResolver resolver, Uri lookupUri) {
        if (lookupUri == null) {
            return null;
        }
        final List<String> segments = lookupUri.getPathSegments();
        if (segments.size() != 4) {
            return null;
        }
        final long uriContactId = Long.parseLong(segments.get(3));
        final String uriLookupKey = Uri.encode(segments.get(2));
        final Uri dataUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, uriContactId),
                Contacts.Data.CONTENT_DIRECTORY);
        Cursor cursor = resolver.query(dataUri,
                new String[] {
                    Contacts.NAME_RAW_CONTACT_ID,
                    Contacts.DISPLAY_NAME_SOURCE,
                    Contacts.LOOKUP_KEY
                }, null, null, null);
        if (cursor.moveToFirst()) {
            String lookupKey =
                    cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
            if (!lookupKey.equals(uriLookupKey)) {
                cursor.close();
                return null;
            }
            return cursor;
        } else {
            cursor.close();
            return null;
        }
    }
    private synchronized void startEntityQuery() {
        closeCursor();
        mCursor = setupContactCursor(mResolver, mLookupUri);
        if (mCursor == null) {
            mLookupUri = Contacts.getLookupUri(getContentResolver(), mLookupUri);
            mCursor = setupContactCursor(mResolver, mLookupUri);
        }
        if (mCursor == null) {
            mNameRawContactId = -1;
            mDisplayNameSource = DisplayNameSources.UNDEFINED;
            Toast.makeText(this, R.string.invalidContactMessage, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "invalid contact uri: " + mLookupUri);
            finish();
            return;
        }
        final long contactId = ContentUris.parseId(mLookupUri);
        mNameRawContactId =
                mCursor.getLong(mCursor.getColumnIndex(Contacts.NAME_RAW_CONTACT_ID));
        mDisplayNameSource =
                mCursor.getInt(mCursor.getColumnIndex(Contacts.DISPLAY_NAME_SOURCE));
        mCursor.registerContentObserver(mObserver);
        mHasEntities = false;
        mHasStatuses = false;
        mHandler.startQuery(TOKEN_ENTITIES, null, RawContactsEntity.CONTENT_URI, null,
                RawContacts.CONTACT_ID + "=?", new String[] {
                    String.valueOf(contactId)
                }, null);
        final Uri dataUri = Uri.withAppendedPath(
                ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId),
                Contacts.Data.CONTENT_DIRECTORY);
        mHandler.startQuery(TOKEN_STATUSES, null, dataUri, StatusQuery.PROJECTION,
                        StatusUpdates.PRESENCE + " IS NOT NULL OR " + StatusUpdates.STATUS
                                + " IS NOT NULL", null, null);
        mContactHeaderWidget.bindFromContactLookupUri(mLookupUri);
    }
    private void closeCursor() {
        if (mCursor != null) {
            mCursor.unregisterContentObserver(mObserver);
            mCursor.close();
            mCursor = null;
        }
    }
    private void considerBindData() {
        if (mHasEntities && mHasStatuses) {
            bindData();
        }
    }
    private void bindData() {
        buildEntries();
        Collapser.collapseList(mPhoneEntries);
        Collapser.collapseList(mSmsEntries);
        Collapser.collapseList(mEmailEntries);
        Collapser.collapseList(mPostalEntries);
        Collapser.collapseList(mImEntries);
        if (mAdapter == null) {
            mAdapter = new ViewAdapter(this, mSections);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.setSections(mSections, SHOW_SEPARATORS);
        }
        mListView.setEmptyView(mEmptyView);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean hasRawContact = (mRawContactIds.size() > 0);
        menu.findItem(R.id.menu_edit).setEnabled(hasRawContact);
        menu.findItem(R.id.menu_share).setEnabled(!mAllRestricted);
        return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }
        if (info == null) {
            Log.e(TAG, "bad menuInfo");
            return;
        }
        ViewEntry entry = ContactEntryAdapter.getEntry(mSections, info.position, SHOW_SEPARATORS);
        menu.setHeaderTitle(R.string.contactOptionsTitle);
        if (entry.mimetype.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
            menu.add(0, 0, 0, R.string.menu_call).setIntent(entry.intent);
            menu.add(0, 0, 0, R.string.menu_sendSMS).setIntent(entry.secondaryIntent);
            if (!entry.isPrimary) {
                menu.add(0, MENU_ITEM_MAKE_DEFAULT, 0, R.string.menu_makeDefaultNumber);
            }
        } else if (entry.mimetype.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
            menu.add(0, 0, 0, R.string.menu_sendEmail).setIntent(entry.intent);
            if (!entry.isPrimary) {
                menu.add(0, MENU_ITEM_MAKE_DEFAULT, 0, R.string.menu_makeDefaultEmail);
            }
        } else if (entry.mimetype.equals(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)) {
            menu.add(0, 0, 0, R.string.menu_viewAddress).setIntent(entry.intent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_edit: {
                Long rawContactIdToEdit = null;
                if (mRawContactIds.size() > 0) {
                    rawContactIdToEdit = mRawContactIds.get(0);
                } else {
                    break;
                }
                Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                        rawContactIdToEdit);
                startActivityForResult(new Intent(Intent.ACTION_EDIT, rawContactUri),
                        REQUEST_EDIT_CONTACT);
                break;
            }
            case R.id.menu_delete: {
                if (mReadOnlySourcesCnt > 0 & mWritableSourcesCnt > 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_DELETE);
                } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_HIDE);
                } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
                    showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
                } else {
                    showDialog(DIALOG_CONFIRM_DELETE);
                }
                return true;
            }
            case R.id.menu_join: {
                showJoinAggregateActivity();
                return true;
            }
            case R.id.menu_options: {
                showOptionsActivity();
                return true;
            }
            case R.id.menu_share: {
                if (mAllRestricted) return false;
                final String lookupKey = Uri.encode(mLookupUri.getPathSegments().get(2));
                final Uri shareUri = Uri.withAppendedPath(Contacts.CONTENT_VCARD_URI, lookupKey);
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(Contacts.CONTENT_VCARD_TYPE);
                intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                final CharSequence chooseTitle = getText(R.string.share_via);
                final Intent chooseIntent = Intent.createChooser(intent, chooseTitle);
                try {
                    startActivity(chooseIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this, R.string.share_error, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_MAKE_DEFAULT: {
                if (makeItemDefault(item)) {
                    return true;
                }
                break;
            }
        }
        return super.onContextItemSelected(item);
    }
    private boolean makeItemDefault(MenuItem item) {
        ViewEntry entry = getViewEntryForMenuItem(item);
        if (entry == null) {
            return false;
        }
        ContentValues values = new ContentValues(1);
        values.put(Data.IS_SUPER_PRIMARY, 1);
        getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, entry.id),
                values, null, null);
        startEntityQuery();
        return true;
    }
    public void showJoinAggregateActivity() {
        long freshId = getRefreshedContactId();
        if (freshId > 0) {
            String displayName = null;
            if (mCursor.moveToFirst()) {
                displayName = mCursor.getString(0);
            }
            Intent intent = new Intent(ContactsListActivity.JOIN_AGGREGATE);
            intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_ID, freshId);
            if (displayName != null) {
                intent.putExtra(ContactsListActivity.EXTRA_AGGREGATE_NAME, displayName);
            }
            startActivityForResult(intent, REQUEST_JOIN_CONTACT);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_JOIN_CONTACT) {
            if (resultCode == RESULT_OK && intent != null) {
                final long contactId = ContentUris.parseId(intent.getData());
                joinAggregate(contactId);
            }
        } else if (requestCode == REQUEST_EDIT_CONTACT) {
            if (resultCode == EditContactActivity.RESULT_CLOSE_VIEW_ACTIVITY) {
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                mLookupUri = intent.getData();
                if (mLookupUri == null) {
                    finish();
                }
            }
        }
    }
    private void joinAggregate(final long contactId) {
        Cursor c = mResolver.query(RawContacts.CONTENT_URI, new String[] {RawContacts._ID},
                RawContacts.CONTACT_ID + "=" + contactId, null, null);
        try {
            while(c.moveToNext()) {
                long rawContactId = c.getLong(0);
                setAggregationException(rawContactId, AggregationExceptions.TYPE_KEEP_TOGETHER);
            }
        } finally {
            c.close();
        }
        Toast.makeText(this, R.string.contactsJoinedMessage, Toast.LENGTH_LONG).show();
        startEntityQuery();
    }
    protected void setAggregationException(long rawContactId, int exceptionType) {
        ContentValues values = new ContentValues(3);
        for (long aRawContactId : mRawContactIds) {
            if (aRawContactId != rawContactId) {
                values.put(AggregationExceptions.RAW_CONTACT_ID1, aRawContactId);
                values.put(AggregationExceptions.RAW_CONTACT_ID2, rawContactId);
                values.put(AggregationExceptions.TYPE, exceptionType);
                mResolver.update(AggregationExceptions.CONTENT_URI, values, null, null);
            }
        }
    }
    private void showOptionsActivity() {
        final Intent intent = new Intent(this, ContactOptionsActivity.class);
        intent.setData(mLookupUri);
        startActivity(intent);
    }
    private ViewEntry getViewEntryForMenuItem(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return null;
        }
        return ContactEntryAdapter.getEntry(mSections, info.position, SHOW_SEPARATORS);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                try {
                    ITelephony phone = ITelephony.Stub.asInterface(
                            ServiceManager.checkService("phone"));
                    if (phone != null && !phone.isIdle()) {
                        break;
                    }
                } catch (RemoteException re) {
                }
                int index = mListView.getSelectedItemPosition();
                if (index != -1) {
                    ViewEntry entry = ViewAdapter.getEntry(mSections, index, SHOW_SEPARATORS);
                    if (entry != null &&
                            entry.intent.getAction() == Intent.ACTION_CALL_PRIVILEGED) {
                        startActivity(entry.intent);
                        return true;
                    }
                } else if (mPrimaryPhoneUri != null) {
                    final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            mPrimaryPhoneUri);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
            case KeyEvent.KEYCODE_DEL: {
                if (mReadOnlySourcesCnt > 0 & mWritableSourcesCnt > 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_DELETE);
                } else if (mReadOnlySourcesCnt > 0 && mWritableSourcesCnt == 0) {
                    showDialog(DIALOG_CONFIRM_READONLY_HIDE);
                } else if (mReadOnlySourcesCnt == 0 && mWritableSourcesCnt > 1) {
                    showDialog(DIALOG_CONFIRM_MULTIPLE_DELETE);
                } else {
                    showDialog(DIALOG_CONFIRM_DELETE);
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        ViewEntry entry = ViewAdapter.getEntry(mSections, position, SHOW_SEPARATORS);
        if (entry != null) {
            Intent intent = entry.intent;
            if (intent != null) {
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "No activity found for intent: " + intent);
                    signalError();
                }
            } else {
                signalError();
            }
        } else {
            signalError();
        }
    }
    private void signalError() {
    }
    private final void buildEntries() {
        final int numSections = mSections.size();
        for (int i = 0; i < numSections; i++) {
            mSections.get(i).clear();
        }
        mRawContactIds.clear();
        mReadOnlySourcesCnt = 0;
        mWritableSourcesCnt = 0;
        mAllRestricted = true;
        mPrimaryPhoneUri = null;
        mWritableRawContactIds.clear();
        final Context context = this;
        final Sources sources = Sources.getInstance(context);
        if (mLookupUri != null) {
            for (Entity entity: mEntities) {
                final ContentValues entValues = entity.getEntityValues();
                final String accountType = entValues.getAsString(RawContacts.ACCOUNT_TYPE);
                final long rawContactId = entValues.getAsLong(RawContacts._ID);
                final boolean isRestricted = entValues.getAsInteger(RawContacts.IS_RESTRICTED) != 0;
                if (!isRestricted) mAllRestricted = false;
                if (!mRawContactIds.contains(rawContactId)) {
                    mRawContactIds.add(rawContactId);
                }
                ContactsSource contactsSource = sources.getInflatedSource(accountType,
                        ContactsSource.LEVEL_SUMMARY);
                if (contactsSource != null && contactsSource.readOnly) {
                    mReadOnlySourcesCnt += 1;
                } else {
                    mWritableSourcesCnt += 1;
                    mWritableRawContactIds.add(rawContactId);
                }
                for (NamedContentValues subValue : entity.getSubValues()) {
                    final ContentValues entryValues = subValue.values;
                    entryValues.put(Data.RAW_CONTACT_ID, rawContactId);
                    final long dataId = entryValues.getAsLong(Data._ID);
                    final String mimeType = entryValues.getAsString(Data.MIMETYPE);
                    if (mimeType == null) continue;
                    final DataKind kind = sources.getKindOrFallback(accountType, mimeType, this,
                            ContactsSource.LEVEL_MIMETYPES);
                    if (kind == null) continue;
                    final ViewEntry entry = ViewEntry.fromValues(context, mimeType, kind,
                            rawContactId, dataId, entryValues);
                    final boolean hasData = !TextUtils.isEmpty(entry.data);
                    final boolean isSuperPrimary = entryValues.getAsInteger(
                            Data.IS_SUPER_PRIMARY) != 0;
                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        mNumPhoneNumbers++;
                        entry.intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts(Constants.SCHEME_TEL, entry.data, null));
                        entry.secondaryIntent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts(Constants.SCHEME_SMSTO, entry.data, null));
                        if (isSuperPrimary) mPrimaryPhoneUri = entry.uri;
                        entry.isPrimary = isSuperPrimary;
                        mPhoneEntries.add(entry);
                        if (entry.type == CommonDataKinds.Phone.TYPE_MOBILE
                                || mShowSmsLinksForAllPhones) {
                            if (kind.iconAltRes > 0) {
                                entry.secondaryActionIcon = kind.iconAltRes;
                            }
                        }
                    } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        entry.intent = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts(Constants.SCHEME_MAILTO, entry.data, null));
                        entry.isPrimary = isSuperPrimary;
                        mEmailEntries.add(entry);
                        final DataStatus status = mStatuses.get(entry.id);
                        if (status != null) {
                            final String imMime = Im.CONTENT_ITEM_TYPE;
                            final DataKind imKind = sources.getKindOrFallback(accountType,
                                    imMime, this, ContactsSource.LEVEL_MIMETYPES);
                            final ViewEntry imEntry = ViewEntry.fromValues(context,
                                    imMime, imKind, rawContactId, dataId, entryValues);
                            imEntry.intent = ContactsUtils.buildImIntent(entryValues);
                            imEntry.applyStatus(status, false);
                            mImEntries.add(imEntry);
                        }
                    } else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        entry.maxLines = 4;
                        entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);
                        mPostalEntries.add(entry);
                    } else if (Im.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        entry.intent = ContactsUtils.buildImIntent(entryValues);
                        if (TextUtils.isEmpty(entry.label)) {
                            entry.label = getString(R.string.chat).toLowerCase();
                        }
                        final DataStatus status = mStatuses.get(entry.id);
                        if (status != null) {
                            entry.applyStatus(status, false);
                        }
                        mImEntries.add(entry);
                    } else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType) &&
                            (hasData || !TextUtils.isEmpty(entry.label))) {
                        final boolean isNameRawContact = (mNameRawContactId == rawContactId);
                        final boolean duplicatesTitle =
                            isNameRawContact
                            && mDisplayNameSource == DisplayNameSources.ORGANIZATION
                            && (!hasData || TextUtils.isEmpty(entry.label));
                        if (!duplicatesTitle) {
                            entry.uri = null;
                            if (TextUtils.isEmpty(entry.label)) {
                                entry.label = entry.data;
                                entry.data = "";
                            }
                            mOrganizationEntries.add(entry);
                        }
                    } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        final boolean isNameRawContact = (mNameRawContactId == rawContactId);
                        final boolean duplicatesTitle =
                            isNameRawContact
                            && mDisplayNameSource == DisplayNameSources.NICKNAME;
                        if (!duplicatesTitle) {
                            entry.uri = null;
                            mNicknameEntries.add(entry);
                        }
                    } else if (Note.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        entry.uri = null;
                        entry.maxLines = 100;
                        mOtherEntries.add(entry);
                    } else if (Website.CONTENT_ITEM_TYPE.equals(mimeType) && hasData) {
                        entry.uri = null;
                        entry.maxLines = 10;
                        try {
                            WebAddress webAddress = new WebAddress(entry.data);
                            entry.intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(webAddress.toString()));
                        } catch (ParseException e) {
                            Log.e(TAG, "Couldn't parse website: " + entry.data);
                        }
                        mOtherEntries.add(entry);
                    } else {
                        entry.intent = new Intent(Intent.ACTION_VIEW, entry.uri);
                        final DataStatus status = mStatuses.get(entry.id);
                        final boolean hasSocial = kind.actionBodySocial && status != null;
                        if (hasSocial) {
                            entry.applyStatus(status, true);
                        }
                        if (hasSocial || hasData) {
                            mOtherEntries.add(entry);
                        }
                    }
                }
            }
        }
    }
    static String buildActionString(DataKind kind, ContentValues values, boolean lowerCase,
            Context context) {
        if (kind.actionHeader == null) {
            return null;
        }
        CharSequence actionHeader = kind.actionHeader.inflateUsing(context, values);
        if (actionHeader == null) {
            return null;
        }
        return lowerCase ? actionHeader.toString().toLowerCase() : actionHeader.toString();
    }
    static String buildDataString(DataKind kind, ContentValues values, Context context) {
        if (kind.actionBody == null) {
            return null;
        }
        CharSequence actionBody = kind.actionBody.inflateUsing(context, values);
        return actionBody == null ? null : actionBody.toString();
    }
    static class ViewEntry extends ContactEntryAdapter.Entry implements Collapsible<ViewEntry> {
        public Context context = null;
        public String resPackageName = null;
        public int actionIcon = -1;
        public boolean isPrimary = false;
        public int secondaryActionIcon = -1;
        public Intent intent;
        public Intent secondaryIntent = null;
        public int maxLabelLines = 1;
        public ArrayList<Long> ids = new ArrayList<Long>();
        public int collapseCount = 0;
        public int presence = -1;
        public CharSequence footerLine = null;
        private ViewEntry() {
        }
        public static ViewEntry fromValues(Context context, String mimeType, DataKind kind,
                long rawContactId, long dataId, ContentValues values) {
            final ViewEntry entry = new ViewEntry();
            entry.context = context;
            entry.contactId = rawContactId;
            entry.id = dataId;
            entry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, entry.id);
            entry.mimetype = mimeType;
            entry.label = buildActionString(kind, values, false, context);
            entry.data = buildDataString(kind, values, context);
            if (kind.typeColumn != null && values.containsKey(kind.typeColumn)) {
                entry.type = values.getAsInteger(kind.typeColumn);
            }
            if (kind.iconRes > 0) {
                entry.resPackageName = kind.resPackageName;
                entry.actionIcon = kind.iconRes;
            }
            return entry;
        }
        public ViewEntry applyStatus(DataStatus status, boolean fillData) {
            presence = status.getPresence();
            if (fillData && status.isValid()) {
                this.data = status.getStatus().toString();
                this.footerLine = status.getTimestampLabel(context);
            }
            return this;
        }
        public boolean collapseWith(ViewEntry entry) {
            if (!shouldCollapseWith(entry)) {
                return false;
            }
            if (TypePrecedence.getTypePrecedence(mimetype, type)
                    > TypePrecedence.getTypePrecedence(entry.mimetype, entry.type)) {
                type = entry.type;
                label = entry.label;
            }
            maxLines = Math.max(maxLines, entry.maxLines);
            maxLabelLines = Math.max(maxLabelLines, entry.maxLabelLines);
            if (StatusUpdates.getPresencePrecedence(presence)
                    < StatusUpdates.getPresencePrecedence(entry.presence)) {
                presence = entry.presence;
            }
            isPrimary = entry.isPrimary ? true : isPrimary;
            ids.add(entry.id);
            collapseCount++;
            return true;
        }
        public boolean shouldCollapseWith(ViewEntry entry) {
            if (entry == null) {
                return false;
            }
            if (!ContactsUtils.shouldCollapse(context, mimetype, data, entry.mimetype,
                    entry.data)) {
                return false;
            }
            if (!TextUtils.equals(mimetype, entry.mimetype)
                    || !ContactsUtils.areIntentActionEqual(intent, entry.intent)
                    || !ContactsUtils.areIntentActionEqual(secondaryIntent, entry.secondaryIntent)
                    || actionIcon != entry.actionIcon) {
                return false;
            }
            return true;
        }
    }
    static class ViewCache {
        public TextView label;
        public TextView data;
        public TextView footer;
        public ImageView actionIcon;
        public ImageView presenceIcon;
        public ImageView primaryIcon;
        public ImageView secondaryActionButton;
        public View secondaryActionDivider;
        ViewEntry entry;
    }
    private final class ViewAdapter extends ContactEntryAdapter<ViewEntry>
            implements View.OnClickListener {
        ViewAdapter(Context context, ArrayList<ArrayList<ViewEntry>> sections) {
            super(context, sections, SHOW_SEPARATORS);
        }
        public void onClick(View v) {
            Intent intent = (Intent) v.getTag();
            startActivity(intent);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewEntry entry = getEntry(mSections, position, false);
            View v;
            ViewCache views;
            if (convertView != null) {
                v = convertView;
                views = (ViewCache) v.getTag();
            } else {
                v = mInflater.inflate(R.layout.list_item_text_icons, parent, false);
                views = new ViewCache();
                views.label = (TextView) v.findViewById(android.R.id.text1);
                views.data = (TextView) v.findViewById(android.R.id.text2);
                views.footer = (TextView) v.findViewById(R.id.footer);
                views.actionIcon = (ImageView) v.findViewById(R.id.action_icon);
                views.primaryIcon = (ImageView) v.findViewById(R.id.primary_icon);
                views.presenceIcon = (ImageView) v.findViewById(R.id.presence_icon);
                views.secondaryActionButton = (ImageView) v.findViewById(
                        R.id.secondary_action_button);
                views.secondaryActionButton.setOnClickListener(this);
                views.secondaryActionDivider = v.findViewById(R.id.divider);
                v.setTag(views);
            }
            views.entry = entry;
            bindView(v, entry);
            return v;
        }
        @Override
        protected View newView(int position, ViewGroup parent) {
            throw new UnsupportedOperationException();
        }
        @Override
        protected void bindView(View view, ViewEntry entry) {
            final Resources resources = mContext.getResources();
            ViewCache views = (ViewCache) view.getTag();
            TextView label = views.label;
            setMaxLines(label, entry.maxLabelLines);
            label.setText(entry.label);
            TextView data = views.data;
            if (data != null) {
                if (entry.mimetype.equals(Phone.CONTENT_ITEM_TYPE)
                        || entry.mimetype.equals(Constants.MIME_SMS_ADDRESS)) {
                    data.setText(PhoneNumberUtils.formatNumber(entry.data));
                } else {
                    data.setText(entry.data);
                }
                setMaxLines(data, entry.maxLines);
            }
            if (!TextUtils.isEmpty(entry.footerLine)) {
                views.footer.setText(entry.footerLine);
                views.footer.setVisibility(View.VISIBLE);
            } else {
                views.footer.setVisibility(View.GONE);
            }
            views.primaryIcon.setVisibility(entry.isPrimary ? View.VISIBLE : View.GONE);
            ImageView action = views.actionIcon;
            if (entry.actionIcon != -1) {
                Drawable actionIcon;
                if (entry.resPackageName != null) {
                    actionIcon = mContext.getPackageManager().getDrawable(entry.resPackageName,
                            entry.actionIcon, null);
                } else {
                    actionIcon = resources.getDrawable(entry.actionIcon);
                }
                action.setImageDrawable(actionIcon);
                action.setVisibility(View.VISIBLE);
            } else {
                action.setVisibility(View.INVISIBLE);
            }
            Drawable presenceIcon = ContactPresenceIconUtil.getPresenceIcon(
                    mContext, entry.presence);
            ImageView presenceIconView = views.presenceIcon;
            if (presenceIcon != null) {
                presenceIconView.setImageDrawable(presenceIcon);
                presenceIconView.setVisibility(View.VISIBLE);
            } else {
                presenceIconView.setVisibility(View.GONE);
            }
            ImageView secondaryActionView = views.secondaryActionButton;
            Drawable secondaryActionIcon = null;
            if (entry.secondaryActionIcon != -1) {
                secondaryActionIcon = resources.getDrawable(entry.secondaryActionIcon);
            }
            if (entry.secondaryIntent != null && secondaryActionIcon != null) {
                secondaryActionView.setImageDrawable(secondaryActionIcon);
                secondaryActionView.setTag(entry.secondaryIntent);
                secondaryActionView.setVisibility(View.VISIBLE);
                views.secondaryActionDivider.setVisibility(View.VISIBLE);
            } else {
                secondaryActionView.setVisibility(View.GONE);
                views.secondaryActionDivider.setVisibility(View.GONE);
            }
        }
        private void setMaxLines(TextView textView, int maxLines) {
            if (maxLines == 1) {
                textView.setSingleLine(true);
                textView.setEllipsize(TextUtils.TruncateAt.END);
            } else {
                textView.setSingleLine(false);
                textView.setMaxLines(maxLines);
                textView.setEllipsize(null);
            }
        }
    }
    private interface StatusQuery {
        final String[] PROJECTION = new String[] {
                Data._ID,
                Data.STATUS,
                Data.STATUS_RES_PACKAGE,
                Data.STATUS_ICON,
                Data.STATUS_LABEL,
                Data.STATUS_TIMESTAMP,
                Data.PRESENCE,
        };
        final int _ID = 0;
    }
    @Override
    public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
            boolean globalSearch) {
        if (globalSearch) {
            super.startSearch(initialQuery, selectInitialQuery, appSearchData, globalSearch);
        } else {
            ContactsSearchManager.startSearch(this, initialQuery);
        }
    }
}
