public class CallDetailActivity extends ListActivity implements
        AdapterView.OnItemClickListener {
    private static final String TAG = "CallDetail";
    private TextView mCallType;
    private ImageView mCallTypeIcon;
    private TextView mCallTime;
    private TextView mCallDuration;
    private String mNumber = null;
     LayoutInflater mInflater;
     Resources mResources;
    static final String[] CALL_LOG_PROJECTION = new String[] {
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
    };
    static final int DATE_COLUMN_INDEX = 0;
    static final int DURATION_COLUMN_INDEX = 1;
    static final int NUMBER_COLUMN_INDEX = 2;
    static final int CALL_TYPE_COLUMN_INDEX = 3;
    static final String[] PHONES_PROJECTION = new String[] {
        PhoneLookup._ID,
        PhoneLookup.DISPLAY_NAME,
        PhoneLookup.TYPE,
        PhoneLookup.LABEL,
        PhoneLookup.NUMBER,
    };
    static final int COLUMN_INDEX_ID = 0;
    static final int COLUMN_INDEX_NAME = 1;
    static final int COLUMN_INDEX_TYPE = 2;
    static final int COLUMN_INDEX_LABEL = 3;
    static final int COLUMN_INDEX_NUMBER = 4;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.call_detail);
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();
        mCallType = (TextView) findViewById(R.id.type);
        mCallTypeIcon = (ImageView) findViewById(R.id.icon);
        mCallTime = (TextView) findViewById(R.id.time);
        mCallDuration = (TextView) findViewById(R.id.duration);
        getListView().setOnItemClickListener(this);
    }
    @Override
    public void onResume() {
        super.onResume();
        updateData(getIntent().getData());
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                TelephonyManager tm = (TelephonyManager)
                        getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", mNumber, null));
                    startActivity(callIntent);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    private void updateData(Uri callUri) {
        ContentResolver resolver = getContentResolver();
        Cursor callCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
        try {
            if (callCursor != null && callCursor.moveToFirst()) {
                mNumber = callCursor.getString(NUMBER_COLUMN_INDEX);
                long date = callCursor.getLong(DATE_COLUMN_INDEX);
                long duration = callCursor.getLong(DURATION_COLUMN_INDEX);
                int callType = callCursor.getInt(CALL_TYPE_COLUMN_INDEX);
                CharSequence dateClause = DateUtils.formatDateRange(this, date, date,
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
                mCallTime.setText(dateClause);
                if (callType == Calls.MISSED_TYPE) {
                    mCallDuration.setVisibility(View.GONE);
                } else {
                    mCallDuration.setVisibility(View.VISIBLE);
                    mCallDuration.setText(formatDuration(duration));
                }
                String callText = null;
                switch (callType) {
                    case Calls.INCOMING_TYPE:
                        mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_incoming_call);
                        mCallType.setText(R.string.type_incoming);
                        callText = getString(R.string.callBack);
                        break;
                    case Calls.OUTGOING_TYPE:
                        mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_outgoing_call);
                        mCallType.setText(R.string.type_outgoing);
                        callText = getString(R.string.callAgain);
                        break;
                    case Calls.MISSED_TYPE:
                        mCallTypeIcon.setImageResource(R.drawable.ic_call_log_header_missed_call);
                        mCallType.setText(R.string.type_missed);
                        callText = getString(R.string.returnCall);
                        break;
                }
                if (mNumber.equals(CallerInfo.UNKNOWN_NUMBER) ||
                        mNumber.equals(CallerInfo.PRIVATE_NUMBER)) {
                    TextView emptyText = (TextView) findViewById(R.id.emptyText);
                    if (emptyText != null) {
                        emptyText.setText(mNumber.equals(CallerInfo.PRIVATE_NUMBER)
                                ? R.string.private_num : R.string.unknown);
                    }
                } else {
                    String callLabel = null;
                    Uri personUri = null;
                    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(mNumber));
                    Cursor phonesCursor = resolver.query(phoneUri, PHONES_PROJECTION, null, null, null);
                    try {
                        if (phonesCursor != null && phonesCursor.moveToFirst()) {
                            long personId = phonesCursor.getLong(COLUMN_INDEX_ID);
                            personUri = ContentUris.withAppendedId(
                                    Contacts.CONTENT_URI, personId);
                            callText = getString(R.string.recentCalls_callNumber,
                                    phonesCursor.getString(COLUMN_INDEX_NAME));
                            mNumber = PhoneNumberUtils.formatNumber(
                                    phonesCursor.getString(COLUMN_INDEX_NUMBER));
                            callLabel = Phone.getDisplayLabel(this,
                                    phonesCursor.getInt(COLUMN_INDEX_TYPE),
                                    phonesCursor.getString(COLUMN_INDEX_LABEL)).toString();
                        } else {
                            mNumber = PhoneNumberUtils.formatNumber(mNumber);
                        }
                    } finally {
                        if (phonesCursor != null) phonesCursor.close();
                    }
                    List<ViewEntry> actions = new ArrayList<ViewEntry>();
                    Intent callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                            Uri.fromParts("tel", mNumber, null));
                    ViewEntry entry = new ViewEntry(android.R.drawable.sym_action_call, callText,
                            callIntent);
                    entry.number = mNumber;
                    entry.label = callLabel;
                    actions.add(entry);
                    Intent smsIntent = new Intent(Intent.ACTION_SENDTO,
                            Uri.fromParts("sms", mNumber, null));
                    actions.add(new ViewEntry(R.drawable.sym_action_sms,
                            getString(R.string.menu_sendTextMessage), smsIntent));
                    if (personUri != null) {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, personUri);
                        actions.add(new ViewEntry(R.drawable.sym_action_view_contact,
                                getString(R.string.menu_viewContact), viewIntent));
                    } else {
                        Intent createIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                        createIntent.setType(Contacts.CONTENT_ITEM_TYPE);
                        createIntent.putExtra(Insert.PHONE, mNumber);
                        actions.add(new ViewEntry(R.drawable.sym_action_add,
                                getString(R.string.recentCalls_addToContact), createIntent));
                    }
                    ViewAdapter adapter = new ViewAdapter(this, actions);
                    setListAdapter(adapter);
                }
            } else {
                Toast.makeText(this, R.string.toast_call_detail_error,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }
    private String formatDuration(long elapsedSeconds) {
        long minutes = 0;
        long seconds = 0;
        if (elapsedSeconds >= 60) {
            minutes = elapsedSeconds / 60;
            elapsedSeconds -= minutes * 60;
        }
        seconds = elapsedSeconds;
        return getString(R.string.callDetailsDurationFormat, minutes, seconds);
    }
    static final class ViewEntry {
        public int icon = -1;
        public String text = null;
        public Intent intent = null;
        public String label = null;
        public String number = null;
        public ViewEntry(int icon, String text, Intent intent) {
            this.icon = icon;
            this.text = text;
            this.intent = intent;
        }
    }
    static final class ViewAdapter extends BaseAdapter {
        private final List<ViewEntry> mActions;
        private final LayoutInflater mInflater;
        public ViewAdapter(Context context, List<ViewEntry> actions) {
            mActions = actions;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        public int getCount() {
            return mActions.size();
        }
        public Object getItem(int position) {
            return mActions.get(position);
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.call_detail_list_item, parent, false);
            }
            ViewEntry entry = mActions.get(position);
            convertView.setTag(entry);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView text = (TextView) convertView.findViewById(android.R.id.text1);
            icon.setImageResource(entry.icon);
            text.setText(entry.text);
            View line2 = convertView.findViewById(R.id.line2);
            boolean numberEmpty = TextUtils.isEmpty(entry.number);
            boolean labelEmpty = TextUtils.isEmpty(entry.label) || numberEmpty;
            if (labelEmpty && numberEmpty) {
                line2.setVisibility(View.GONE);
            } else {
                line2.setVisibility(View.VISIBLE);
                TextView label = (TextView) convertView.findViewById(R.id.label);
                if (labelEmpty) {
                    label.setVisibility(View.GONE);
                } else {
                    label.setText(entry.label);
                    label.setVisibility(View.VISIBLE);
                }
                TextView number = (TextView) convertView.findViewById(R.id.number);
                number.setText(entry.number);
            }
            return convertView;
        }
    }
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        if (view.getTag() instanceof ViewEntry) {
            ViewEntry entry = (ViewEntry) view.getTag();
            if (entry.intent != null) {
                startActivity(entry.intent);
            }
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
