public class SplitAggregateView extends ListView {
    private static final String TAG = "SplitAggregateView";
    private interface SplitQuery {
        String[] COLUMNS = new String[] {
                Data.MIMETYPE, RawContacts.ACCOUNT_TYPE, Data.RAW_CONTACT_ID, Data.IS_PRIMARY,
                StructuredName.DISPLAY_NAME, Nickname.NAME, Email.DATA, Phone.NUMBER
        };
        int MIMETYPE = 0;
        int ACCOUNT_TYPE = 1;
        int RAW_CONTACT_ID = 2;
        int IS_PRIMARY = 3;
        int DISPLAY_NAME = 4;
        int NICKNAME = 5;
        int EMAIL = 6;
        int PHONE = 7;
    }
    private final Uri mAggregateUri;
    private OnContactSelectedListener mListener;
    private Sources mSources;
    public interface OnContactSelectedListener {
        void onContactSelected(long rawContactId);
    }
    public SplitAggregateView(Context context, Uri aggregateUri) {
        super(context);
        mAggregateUri = aggregateUri;
        mSources = Sources.getInstance(context);
        final List<RawContactInfo> list = loadData();
        setAdapter(new SplitAggregateAdapter(context, list));
        setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onContactSelected(list.get(position).rawContactId);
            }
        });
    }
    public void setOnContactSelectedListener(OnContactSelectedListener listener) {
        mListener = listener;
    }
    private static class RawContactInfo implements Comparable<RawContactInfo> {
        final long rawContactId;
        String accountType;
        String name;
        String phone;
        String email;
        String nickname;
        public RawContactInfo(long rawContactId) {
            this.rawContactId = rawContactId;
        }
        public String getAdditionalData() {
            if (nickname != null) {
                return nickname;
            }
            if (email != null) {
                return email;
            }
            if (phone != null) {
                return phone;
            }
            return "";
        }
        public int compareTo(RawContactInfo another) {
            String thisAccount = accountType != null ? accountType : "";
            String thatAccount = another.accountType != null ? another.accountType : "";
            return thisAccount.compareTo(thatAccount);
        }
    }
    private List<RawContactInfo> loadData() {
        HashMap<Long, RawContactInfo> rawContactInfos = new HashMap<Long, RawContactInfo>();
        Uri dataUri = Uri.withAppendedPath(mAggregateUri, Data.CONTENT_DIRECTORY);
        Cursor cursor = getContext().getContentResolver().query(dataUri,
                SplitQuery.COLUMNS, null, null, null);
        try {
            while (cursor.moveToNext()) {
                long rawContactId = cursor.getLong(SplitQuery.RAW_CONTACT_ID);
                RawContactInfo info = rawContactInfos.get(rawContactId);
                if (info == null) {
                    info = new RawContactInfo(rawContactId);
                    rawContactInfos.put(rawContactId, info);
                    info.accountType = cursor.getString(SplitQuery.ACCOUNT_TYPE);
                }
                String mimetype = cursor.getString(SplitQuery.MIMETYPE);
                if (StructuredName.CONTENT_ITEM_TYPE.equals(mimetype)) {
                    loadStructuredName(cursor, info);
                } else if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
                    loadPhoneNumber(cursor, info);
                } else if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
                    loadEmail(cursor, info);
                } else if (Nickname.CONTENT_ITEM_TYPE.equals(mimetype)) {
                    loadNickname(cursor, info);
                }
            }
        } finally {
            cursor.close();
        }
        List<RawContactInfo> list = new ArrayList<RawContactInfo>(rawContactInfos.values());
        Collections.sort(list);
        return list;
    }
    private void loadStructuredName(Cursor cursor, RawContactInfo info) {
        info.name = cursor.getString(SplitQuery.DISPLAY_NAME);
    }
    private void loadNickname(Cursor cursor, RawContactInfo info) {
        if (info.nickname == null || cursor.getInt(SplitQuery.IS_PRIMARY) != 0) {
            info.nickname = cursor.getString(SplitQuery.NICKNAME);
        }
    }
    private void loadEmail(Cursor cursor, RawContactInfo info) {
        if (info.email == null || cursor.getInt(SplitQuery.IS_PRIMARY) != 0) {
            info.email = cursor.getString(SplitQuery.EMAIL);
        }
    }
    private void loadPhoneNumber(Cursor cursor, RawContactInfo info) {
        if (info.phone == null || cursor.getInt(SplitQuery.IS_PRIMARY) != 0) {
            info.phone = cursor.getString(SplitQuery.PHONE);
        }
    }
    private static class SplitAggregateItemCache  {
        TextView name;
        TextView additionalData;
        ImageView sourceIcon;
    }
    private class SplitAggregateAdapter extends ArrayAdapter<RawContactInfo> {
        private LayoutInflater mInflater;
        public SplitAggregateAdapter(Context context, List<RawContactInfo> sources) {
            super(context, 0, sources);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.split_aggregate_list_item, parent, false);
            }
            SplitAggregateItemCache cache = (SplitAggregateItemCache)convertView.getTag();
            if (cache == null) {
                cache = new SplitAggregateItemCache();
                cache.name = (TextView)convertView.findViewById(R.id.name);
                cache.additionalData = (TextView)convertView.findViewById(R.id.additionalData);
                cache.sourceIcon = (ImageView)convertView.findViewById(R.id.sourceIcon);
                convertView.setTag(cache);
            }
            final RawContactInfo info = getItem(position);
            cache.name.setText(info.name);
            cache.additionalData.setText(info.getAdditionalData());
            Drawable icon = null;
            ContactsSource source = mSources.getInflatedSource(info.accountType,
                    ContactsSource.LEVEL_SUMMARY);
            if (source != null) {
                icon = source.getDisplayIcon(getContext());
            }
            if (icon != null) {
                cache.sourceIcon.setImageDrawable(icon);
            } else {
                cache.sourceIcon.setImageResource(R.drawable.unknown_source);
            }
            return convertView;
        }
    }
}
