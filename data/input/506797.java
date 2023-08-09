public final class ContactsPreferencesActivity extends ExpandableListActivity implements
        AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "DisplayGroupsActivity";
    public interface Prefs {
        public static final String DISPLAY_ONLY_PHONES = "only_phones";
        public static final boolean DISPLAY_ONLY_PHONES_DEFAULT = false;
    }
    private static final int DIALOG_SORT_ORDER = 1;
    private static final int DIALOG_DISPLAY_ORDER = 2;
    private ExpandableListView mList;
    private DisplayAdapter mAdapter;
    private SharedPreferences mPrefs;
    private ContactsPreferences mContactsPrefs;
    private CheckBox mDisplayPhones;
    private View mHeaderPhones;
    private View mHeaderSeparator;
    private View mSortOrderView;
    private TextView mSortOrderTextView;
    private int mSortOrder;
    private View mDisplayOrderView;
    private TextView mDisplayOrderTextView;
    private int mDisplayOrder;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.contacts_preferences);
        mList = getExpandableListView();
        mList.setHeaderDividersEnabled(true);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mContactsPrefs = new ContactsPreferences(this);
        mAdapter = new DisplayAdapter(this);
        final LayoutInflater inflater = getLayoutInflater();
        createWithPhonesOnlyPreferenceView(inflater);
        createSortOrderPreferenceView(inflater);
        createDisplayOrderPreferenceView(inflater);
        createDisplayGroupHeader(inflater);
        findViewById(R.id.btn_done).setOnClickListener(this);
        findViewById(R.id.btn_discard).setOnClickListener(this);
        mList.setOnItemClickListener(this);
        mList.setOnCreateContextMenuListener(this);
        mSortOrder = mContactsPrefs.getSortOrder();
        mDisplayOrder = mContactsPrefs.getDisplayOrder();
    }
    private void createWithPhonesOnlyPreferenceView(LayoutInflater inflater) {
        mHeaderPhones = inflater.inflate(R.layout.display_options_phones_only, mList, false);
        mHeaderPhones.setId(R.id.header_phones);
        mDisplayPhones = (CheckBox) mHeaderPhones.findViewById(android.R.id.checkbox);
        mDisplayPhones.setChecked(mPrefs.getBoolean(Prefs.DISPLAY_ONLY_PHONES,
                Prefs.DISPLAY_ONLY_PHONES_DEFAULT));
        {
            final TextView text1 = (TextView)mHeaderPhones.findViewById(android.R.id.text1);
            final TextView text2 = (TextView)mHeaderPhones.findViewById(android.R.id.text2);
            text1.setText(R.string.showFilterPhones);
            text2.setText(R.string.showFilterPhonesDescrip);
        }
    }
    private void createSortOrderPreferenceView(LayoutInflater inflater) {
        mSortOrderView = inflater.inflate(R.layout.preference_with_more_button, mList, false);
        View preferenceLayout = mSortOrderView.findViewById(R.id.preference);
        TextView label = (TextView)preferenceLayout.findViewById(R.id.label);
        label.setText(getString(R.string.display_options_sort_list_by));
        mSortOrderTextView = (TextView)preferenceLayout.findViewById(R.id.data);
    }
    private void createDisplayOrderPreferenceView(LayoutInflater inflater) {
        mDisplayOrderView = inflater.inflate(R.layout.preference_with_more_button, mList, false);
        View preferenceLayout = mDisplayOrderView.findViewById(R.id.preference);
        TextView label = (TextView)preferenceLayout.findViewById(R.id.label);
        label.setText(getString(R.string.display_options_view_names_as));
        mDisplayOrderTextView = (TextView)preferenceLayout.findViewById(R.id.data);
    }
    private void createDisplayGroupHeader(LayoutInflater inflater) {
        mHeaderSeparator = inflater.inflate(R.layout.list_separator, mList, false);
        {
            final TextView text1 = (TextView)mHeaderSeparator;
            text1.setText(R.string.headerContactGroups);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mList.removeHeaderView(mHeaderPhones);
        mList.removeHeaderView(mSortOrderView);
        mList.removeHeaderView(mDisplayOrderView);
        mList.removeHeaderView(mHeaderSeparator);
        setListAdapter(null);
        mList.addHeaderView(mHeaderPhones, null, true);
        if (mContactsPrefs.isSortOrderUserChangeable()) {
            mList.addHeaderView(mSortOrderView, null, true);
        }
        if (mContactsPrefs.isSortOrderUserChangeable()) {
            mList.addHeaderView(mDisplayOrderView, null, true);
        }
        mList.addHeaderView(mHeaderSeparator, null, false);
        setListAdapter(mAdapter);
        bindView();
        new QueryGroupsTask(this).execute();
    }
    private void bindView() {
        mSortOrderTextView.setText(
                mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY
                        ? getString(R.string.display_options_sort_by_given_name)
                        : getString(R.string.display_options_sort_by_family_name));
        mDisplayOrderTextView.setText(
                mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY
                        ? getString(R.string.display_options_view_given_name_first)
                        : getString(R.string.display_options_view_family_name_first));
    }
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case DIALOG_SORT_ORDER:
                return createSortOrderDialog();
            case DIALOG_DISPLAY_ORDER:
                return createDisplayOrderDialog();
        }
        return null;
    }
    private Dialog createSortOrderDialog() {
        String[] items = new String[] {
                getString(R.string.display_options_sort_by_given_name),
                getString(R.string.display_options_sort_by_family_name),
        };
        return new AlertDialog.Builder(this)
            .setIcon(com.android.internal.R.drawable.ic_dialog_menu_generic)
            .setTitle(R.string.display_options_sort_list_by)
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setSortOrder(dialog);
                        dialog.dismiss();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }
    private Dialog createDisplayOrderDialog() {
        String[] items = new String[] {
                getString(R.string.display_options_view_given_name_first),
                getString(R.string.display_options_view_family_name_first),
        };
        return new AlertDialog.Builder(this)
            .setIcon(com.android.internal.R.drawable.ic_dialog_menu_generic)
            .setTitle(R.string.display_options_view_names_as)
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setDisplayOrder(dialog);
                        dialog.dismiss();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null)
            .create();
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
            case DIALOG_SORT_ORDER:
                setCheckedItem(dialog,
                        mSortOrder == ContactsContract.Preferences.SORT_ORDER_PRIMARY ? 0 : 1);
                break;
            case DIALOG_DISPLAY_ORDER:
                setCheckedItem(dialog,
                        mDisplayOrder == ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY
                                ? 0 : 1);
                break;
        }
    }
    private void setCheckedItem(Dialog dialog, int position) {
        ListView listView = ((AlertDialog)dialog).getListView();
        listView.setItemChecked(position, true);
        listView.setSelection(position);
    }
    protected void setSortOrder(DialogInterface dialog) {
        ListView listView = ((AlertDialog)dialog).getListView();
        int checked = listView.getCheckedItemPosition();
        mSortOrder = checked == 0
                ? ContactsContract.Preferences.SORT_ORDER_PRIMARY
                : ContactsContract.Preferences.SORT_ORDER_ALTERNATIVE;
        bindView();
    }
    protected void setDisplayOrder(DialogInterface dialog) {
        ListView listView = ((AlertDialog)dialog).getListView();
        int checked = listView.getCheckedItemPosition();
        mDisplayOrder = checked == 0
                ? ContactsContract.Preferences.DISPLAY_ORDER_PRIMARY
                : ContactsContract.Preferences.DISPLAY_ORDER_ALTERNATIVE;
        bindView();
    }
    private static class QueryGroupsTask extends
            WeakAsyncTask<Void, Void, AccountSet, ContactsPreferencesActivity> {
        public QueryGroupsTask(ContactsPreferencesActivity target) {
            super(target);
        }
        @Override
        protected AccountSet doInBackground(ContactsPreferencesActivity target,
                Void... params) {
            final Context context = target;
            final Sources sources = Sources.getInstance(context);
            final ContentResolver resolver = context.getContentResolver();
            final AccountSet accounts = new AccountSet();
            for (Account account : sources.getAccounts(false)) {
                accounts.add(new AccountDisplay(resolver, account.name, account.type));
            }
            return accounts;
        }
        @Override
        protected void onPostExecute(ContactsPreferencesActivity target, AccountSet result) {
            target.mAdapter.setAccounts(result);
        }
    }
    private static final int DEFAULT_SHOULD_SYNC = 1;
    private static final int DEFAULT_VISIBLE = 0;
    protected static class GroupDelta extends ValuesDelta {
        private boolean mUngrouped = false;
        private boolean mAccountHasGroups;
        private GroupDelta() {
            super();
        }
        public static GroupDelta fromSettings(ContentResolver resolver, String accountName,
                String accountType, boolean accountHasGroups) {
            final Uri settingsUri = Settings.CONTENT_URI.buildUpon()
                    .appendQueryParameter(Settings.ACCOUNT_NAME, accountName)
                    .appendQueryParameter(Settings.ACCOUNT_TYPE, accountType).build();
            final Cursor cursor = resolver.query(settingsUri, new String[] {
                    Settings.SHOULD_SYNC, Settings.UNGROUPED_VISIBLE
            }, null, null, null);
            try {
                final ContentValues values = new ContentValues();
                values.put(Settings.ACCOUNT_NAME, accountName);
                values.put(Settings.ACCOUNT_TYPE, accountType);
                if (cursor != null && cursor.moveToFirst()) {
                    values.put(Settings.SHOULD_SYNC, cursor.getInt(0));
                    values.put(Settings.UNGROUPED_VISIBLE, cursor.getInt(1));
                    return fromBefore(values).setUngrouped(accountHasGroups);
                } else {
                    values.put(Settings.SHOULD_SYNC, DEFAULT_SHOULD_SYNC);
                    values.put(Settings.UNGROUPED_VISIBLE, DEFAULT_VISIBLE);
                    return fromAfter(values).setUngrouped(accountHasGroups);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        public static GroupDelta fromBefore(ContentValues before) {
            final GroupDelta entry = new GroupDelta();
            entry.mBefore = before;
            entry.mAfter = new ContentValues();
            return entry;
        }
        public static GroupDelta fromAfter(ContentValues after) {
            final GroupDelta entry = new GroupDelta();
            entry.mBefore = null;
            entry.mAfter = after;
            return entry;
        }
        protected GroupDelta setUngrouped(boolean accountHasGroups) {
            mUngrouped = true;
            mAccountHasGroups = accountHasGroups;
            return this;
        }
        @Override
        public boolean beforeExists() {
            return mBefore != null;
        }
        public boolean getShouldSync() {
            return getAsInteger(mUngrouped ? Settings.SHOULD_SYNC : Groups.SHOULD_SYNC,
                    DEFAULT_SHOULD_SYNC) != 0;
        }
        public boolean getVisible() {
            return getAsInteger(mUngrouped ? Settings.UNGROUPED_VISIBLE : Groups.GROUP_VISIBLE,
                    DEFAULT_VISIBLE) != 0;
        }
        public void putShouldSync(boolean shouldSync) {
            put(mUngrouped ? Settings.SHOULD_SYNC : Groups.SHOULD_SYNC, shouldSync ? 1 : 0);
        }
        public void putVisible(boolean visible) {
            put(mUngrouped ? Settings.UNGROUPED_VISIBLE : Groups.GROUP_VISIBLE, visible ? 1 : 0);
        }
        public CharSequence getTitle(Context context) {
            if (mUngrouped) {
                if (mAccountHasGroups) {
                    return context.getText(R.string.display_ungrouped);
                } else {
                    return context.getText(R.string.display_all_contacts);
                }
            } else {
                final Integer titleRes = getAsInteger(Groups.TITLE_RES);
                if (titleRes != null) {
                    final String packageName = getAsString(Groups.RES_PACKAGE);
                    return context.getPackageManager().getText(packageName, titleRes, null);
                } else {
                    return getAsString(Groups.TITLE);
                }
            }
        }
        public ContentProviderOperation buildDiff() {
            if (isNoop()) {
                return null;
            } else if (isUpdate()) {
                final Builder builder = ContentProviderOperation
                        .newUpdate(mUngrouped ? Settings.CONTENT_URI : addCallerIsSyncAdapterParameter(Groups.CONTENT_URI));
                if (mUngrouped) {
                    builder.withSelection(Settings.ACCOUNT_NAME + "=? AND " + Settings.ACCOUNT_TYPE
                            + "=?", new String[] {
                            this.getAsString(Settings.ACCOUNT_NAME),
                            this.getAsString(Settings.ACCOUNT_TYPE)
                    });
                } else {
                    builder.withSelection(Groups._ID + "=" + this.getId(), null);
                }
                builder.withValues(mAfter);
                return builder.build();
            } else if (isInsert() && mUngrouped) {
                mAfter.remove(mIdColumn);
                final Builder builder = ContentProviderOperation.newInsert(Settings.CONTENT_URI);
                builder.withValues(mAfter);
                return builder.build();
            } else {
                throw new IllegalStateException("Unexpected delete or insert");
            }
        }
    }
    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon()
	        .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
	        .build();
    }
    private static Comparator<GroupDelta> sIdComparator = new Comparator<GroupDelta>() {
        public int compare(GroupDelta object1, GroupDelta object2) {
            final Long id1 = object1.getId();
            final Long id2 = object2.getId();
            if (id1 == null && id2 == null) {
                return 0;
            } else if (id1 == null) {
                return -1;
            } else if (id2 == null) {
                return 1;
            } else if (id1 < id2) {
                return -1;
            } else if (id1 > id2) {
                return 1;
            } else {
                return 0;
            }
        }
    };
    protected static class AccountSet extends ArrayList<AccountDisplay> {
        public ArrayList<ContentProviderOperation> buildDiff() {
            final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
            for (AccountDisplay account : this) {
                account.buildDiff(diff);
            }
            return diff;
        }
    }
    protected static class AccountDisplay {
        public String mName;
        public String mType;
        public GroupDelta mUngrouped;
        public ArrayList<GroupDelta> mSyncedGroups = Lists.newArrayList();
        public ArrayList<GroupDelta> mUnsyncedGroups = Lists.newArrayList();
        public AccountDisplay(ContentResolver resolver, String accountName, String accountType) {
            mName = accountName;
            mType = accountType;
            final Uri groupsUri = Groups.CONTENT_URI.buildUpon()
                    .appendQueryParameter(Groups.ACCOUNT_NAME, accountName)
                    .appendQueryParameter(Groups.ACCOUNT_TYPE, accountType).build();
            EntityIterator iterator = ContactsContract.Groups.newEntityIterator(resolver.query(
                    groupsUri, null, null, null, null));
            try {
                boolean hasGroups = false;
                while (iterator.hasNext()) {
                    final ContentValues values = iterator.next().getEntityValues();
                    final GroupDelta group = GroupDelta.fromBefore(values);
                    addGroup(group);
                    hasGroups = true;
                }
                mUngrouped = GroupDelta.fromSettings(resolver, accountName, accountType, hasGroups);
                addGroup(mUngrouped);
            } finally {
                iterator.close();
            }
        }
        private void addGroup(GroupDelta group) {
            if (group.getShouldSync()) {
                mSyncedGroups.add(group);
            } else {
                mUnsyncedGroups.add(group);
            }
        }
        public void setShouldSync(boolean shouldSync) {
            final Iterator<GroupDelta> oppositeChildren = shouldSync ?
                    mUnsyncedGroups.iterator() : mSyncedGroups.iterator();
            while (oppositeChildren.hasNext()) {
                final GroupDelta child = oppositeChildren.next();
                setShouldSync(child, shouldSync, false);
                oppositeChildren.remove();
            }
        }
        public void setShouldSync(GroupDelta child, boolean shouldSync) {
            setShouldSync(child, shouldSync, true);
        }
        public void setShouldSync(GroupDelta child, boolean shouldSync, boolean attemptRemove) {
            child.putShouldSync(shouldSync);
            if (shouldSync) {
                if (attemptRemove) {
                    mUnsyncedGroups.remove(child);
                }
                mSyncedGroups.add(child);
                Collections.sort(mSyncedGroups, sIdComparator);
            } else {
                if (attemptRemove) {
                    mSyncedGroups.remove(child);
                }
                mUnsyncedGroups.add(child);
            }
        }
        public void buildDiff(ArrayList<ContentProviderOperation> diff) {
            for (GroupDelta group : mSyncedGroups) {
                final ContentProviderOperation oper = group.buildDiff();
                if (oper != null) diff.add(oper);
            }
            for (GroupDelta group : mUnsyncedGroups) {
                final ContentProviderOperation oper = group.buildDiff();
                if (oper != null) diff.add(oper);
            }
        }
    }
    protected static class DisplayAdapter extends BaseExpandableListAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private Sources mSources;
        private AccountSet mAccounts;
        private boolean mChildWithPhones = false;
        public DisplayAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mSources = Sources.getInstance(context);
        }
        public void setAccounts(AccountSet accounts) {
            mAccounts = accounts;
            notifyDataSetChanged();
        }
        public void setChildDescripWithPhones(boolean withPhones) {
            mChildWithPhones = withPhones;
        }
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.display_child, parent, false);
            }
            final TextView text1 = (TextView)convertView.findViewById(android.R.id.text1);
            final TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
            final CheckBox checkbox = (CheckBox)convertView.findViewById(android.R.id.checkbox);
            final AccountDisplay account = mAccounts.get(groupPosition);
            final GroupDelta child = (GroupDelta)this.getChild(groupPosition, childPosition);
            if (child != null) {
                final boolean groupVisible = child.getVisible();
                checkbox.setVisibility(View.VISIBLE);
                checkbox.setChecked(groupVisible);
                final CharSequence groupTitle = child.getTitle(mContext);
                text1.setText(groupTitle);
                text2.setVisibility(View.GONE);
            } else {
                checkbox.setVisibility(View.GONE);
                text1.setText(R.string.display_more_groups);
                text2.setVisibility(View.GONE);
            }
            return convertView;
        }
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.display_group, parent, false);
            }
            final TextView text1 = (TextView)convertView.findViewById(android.R.id.text1);
            final TextView text2 = (TextView)convertView.findViewById(android.R.id.text2);
            final AccountDisplay account = (AccountDisplay)this.getGroup(groupPosition);
            final ContactsSource source = mSources.getInflatedSource(account.mType,
                    ContactsSource.LEVEL_SUMMARY);
            text1.setText(account.mName);
            text2.setText(source.getDisplayLabel(mContext));
            text2.setVisibility(account.mName == null ? View.GONE : View.VISIBLE);
            return convertView;
        }
        public Object getChild(int groupPosition, int childPosition) {
            final AccountDisplay account = mAccounts.get(groupPosition);
            final boolean validChild = childPosition >= 0
                    && childPosition < account.mSyncedGroups.size();
            if (validChild) {
                return account.mSyncedGroups.get(childPosition);
            } else {
                return null;
            }
        }
        public long getChildId(int groupPosition, int childPosition) {
            final GroupDelta child = (GroupDelta)getChild(groupPosition, childPosition);
            if (child != null) {
                final Long childId = child.getId();
                return childId != null ? childId : Long.MIN_VALUE;
            } else {
                return Long.MIN_VALUE;
            }
        }
        public int getChildrenCount(int groupPosition) {
            final AccountDisplay account = mAccounts.get(groupPosition);
            final boolean anyHidden = account.mUnsyncedGroups.size() > 0;
            return account.mSyncedGroups.size() + (anyHidden ? 1 : 0);
        }
        public Object getGroup(int groupPosition) {
            return mAccounts.get(groupPosition);
        }
        public int getGroupCount() {
            if (mAccounts == null) {
                return 0;
            }
            return mAccounts.size();
        }
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        public boolean hasStableIds() {
            return true;
        }
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "OnItemClick, position=" + position + ", id=" + id);
        if (view == mHeaderPhones) {
            mDisplayPhones.toggle();
            return;
        }
        if (view == mDisplayOrderView) {
            Log.d(TAG, "Showing Display Order dialog");
            showDialog(DIALOG_DISPLAY_ORDER);
            return;
        }
        if (view == mSortOrderView) {
            Log.d(TAG, "Showing Sort Order dialog");
            showDialog(DIALOG_SORT_ORDER);
            return;
        }
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done: {
                this.doSaveAction();
                break;
            }
            case R.id.btn_discard: {
                this.finish();
                break;
            }
        }
    }
    protected void setDisplayOnlyPhones(boolean displayOnlyPhones) {
        mDisplayPhones.setChecked(displayOnlyPhones);
        Editor editor = mPrefs.edit();
        editor.putBoolean(Prefs.DISPLAY_ONLY_PHONES, displayOnlyPhones);
        editor.commit();
        mAdapter.setChildDescripWithPhones(displayOnlyPhones);
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
            int childPosition, long id) {
        final CheckBox checkbox = (CheckBox)view.findViewById(android.R.id.checkbox);
        final AccountDisplay account = (AccountDisplay)mAdapter.getGroup(groupPosition);
        final GroupDelta child = (GroupDelta)mAdapter.getChild(groupPosition, childPosition);
        if (child != null) {
            checkbox.toggle();
            child.putVisible(checkbox.isChecked());
        } else {
            this.openContextMenu(view);
        }
        return true;
    }
    private static final int SYNC_MODE_UNSUPPORTED = 0;
    private static final int SYNC_MODE_UNGROUPED = 1;
    private static final int SYNC_MODE_EVERYTHING = 2;
    protected int getSyncMode(AccountDisplay account) {
        if (GoogleSource.ACCOUNT_TYPE.equals(account.mType)) {
            return SYNC_MODE_EVERYTHING;
        } else {
            return SYNC_MODE_UNSUPPORTED;
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (!(menuInfo instanceof ExpandableListContextMenuInfo)) return;
        final ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        final int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        final int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
        if (childPosition == -1) return;
        final AccountDisplay account = (AccountDisplay)mAdapter.getGroup(groupPosition);
        final GroupDelta child = (GroupDelta)mAdapter.getChild(groupPosition, childPosition);
        final int syncMode = getSyncMode(account);
        if (syncMode == SYNC_MODE_UNSUPPORTED) return;
        if (child != null) {
            showRemoveSync(menu, account, child, syncMode);
        } else {
            showAddSync(menu, account, syncMode);
        }
    }
    protected void showRemoveSync(ContextMenu menu, final AccountDisplay account,
            final GroupDelta child, final int syncMode) {
        final CharSequence title = child.getTitle(this);
        menu.setHeaderTitle(title);
        menu.add(R.string.menu_sync_remove).setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        handleRemoveSync(account, child, syncMode, title);
                        return true;
                    }
                });
    }
    protected void handleRemoveSync(final AccountDisplay account, final GroupDelta child,
            final int syncMode, CharSequence title) {
        final boolean shouldSyncUngrouped = account.mUngrouped.getShouldSync();
        if (syncMode == SYNC_MODE_EVERYTHING && shouldSyncUngrouped
                && !child.equals(account.mUngrouped)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final CharSequence removeMessage = this.getString(
                    R.string.display_warn_remove_ungrouped, title);
            builder.setTitle(R.string.menu_sync_remove);
            builder.setMessage(removeMessage);
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    account.setShouldSync(account.mUngrouped, false);
                    account.setShouldSync(child, false);
                    mAdapter.notifyDataSetChanged();
                }
            });
            builder.show();
        } else {
            account.setShouldSync(child, false);
            mAdapter.notifyDataSetChanged();
        }
    }
    protected void showAddSync(ContextMenu menu, final AccountDisplay account, final int syncMode) {
        menu.setHeaderTitle(R.string.dialog_sync_add);
        for (final GroupDelta child : account.mUnsyncedGroups) {
            if (!child.getShouldSync()) {
                final CharSequence title = child.getTitle(this);
                menu.add(title).setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (child.mUngrouped && syncMode == SYNC_MODE_EVERYTHING) {
                            account.setShouldSync(true);
                        } else {
                            account.setShouldSync(child, true);
                        }
                        mAdapter.notifyDataSetChanged();
                        return true;
                    }
                });
            }
        }
    }
    @Override
    public void onBackPressed() {
        doSaveAction();
    }
    private void doSaveAction() {
        mContactsPrefs.setSortOrder(mSortOrder);
        mContactsPrefs.setDisplayOrder(mDisplayOrder);
        if (mAdapter == null || mAdapter.mAccounts == null) {
            return;
        }
        setDisplayOnlyPhones(mDisplayPhones.isChecked());
        new UpdateTask(this).execute(mAdapter.mAccounts);
    }
    public static class UpdateTask extends
            WeakAsyncTask<AccountSet, Void, Void, Activity> {
        private WeakReference<ProgressDialog> mProgress;
        public UpdateTask(Activity target) {
            super(target);
        }
        @Override
        protected void onPreExecute(Activity target) {
            final Context context = target;
            mProgress = new WeakReference<ProgressDialog>(ProgressDialog.show(context, null,
                    context.getText(R.string.savingDisplayGroups)));
            context.startService(new Intent(context, EmptyService.class));
        }
        @Override
        protected Void doInBackground(Activity target, AccountSet... params) {
            final Context context = target;
            final ContentValues values = new ContentValues();
            final ContentResolver resolver = context.getContentResolver();
            try {
                final AccountSet set = params[0];
                final ArrayList<ContentProviderOperation> diff = set.buildDiff();
                resolver.applyBatch(ContactsContract.AUTHORITY, diff);
            } catch (RemoteException e) {
                Log.e(TAG, "Problem saving display groups", e);
            } catch (OperationApplicationException e) {
                Log.e(TAG, "Problem saving display groups", e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Activity target, Void result) {
            final Context context = target;
            final ProgressDialog dialog = mProgress.get();
            if (dialog != null) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Error dismissing progress dialog", e);
                }
            }
            target.finish();
            context.stopService(new Intent(context, EmptyService.class));
        }
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
