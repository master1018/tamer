public class PhoneDisambigDialog implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener, CompoundButton.OnCheckedChangeListener{
    private boolean mMakePrimary = false;
    private Context mContext;
    private AlertDialog mDialog;
    private boolean mSendSms;
    private Cursor mPhonesCursor;
    private ListAdapter mPhonesAdapter;
    private ArrayList<PhoneItem> mPhoneItemList;
    public PhoneDisambigDialog(Context context, Cursor phonesCursor) {
        this(context, phonesCursor, false );
    }
    public PhoneDisambigDialog(Context context, Cursor phonesCursor, boolean sendSms) {
        mContext = context;
        mSendSms = sendSms;
        mPhonesCursor = phonesCursor;
        mPhoneItemList = makePhoneItemsList(phonesCursor);
        Collapser.collapseList(mPhoneItemList);
        mPhonesAdapter = new PhonesAdapter(mContext, mPhoneItemList, mSendSms);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View setPrimaryView = inflater.
                inflate(R.layout.set_primary_checkbox, null);
        ((CheckBox) setPrimaryView.findViewById(R.id.setPrimary)).
                setOnCheckedChangeListener(this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext).
                setAdapter(mPhonesAdapter, this).
                        setTitle(sendSms ?
                                R.string.sms_disambig_title : R.string.call_disambig_title).
                        setView(setPrimaryView);
        mDialog = dialogBuilder.create();
    }
    public void show() {
        if (mPhoneItemList.size() == 1) {
            onClick(mDialog, 0);
            return;
        }
        mDialog.show();
    }
    public void onClick(DialogInterface dialog, int which) {
        if (mPhoneItemList.size() > which && which >= 0) {
            PhoneItem phoneItem = mPhoneItemList.get(which);
            long id = phoneItem.id;
            String phone = phoneItem.phoneNumber;
            if (mMakePrimary) {
                ContentValues values = new ContentValues(1);
                values.put(Data.IS_SUPER_PRIMARY, 1);
                mContext.getContentResolver().update(ContentUris.
                        withAppendedId(Data.CONTENT_URI, id), values, null, null);
            }
            if (mSendSms) {
                ContactsUtils.initiateSms(mContext, phone);
            } else {
                ContactsUtils.initiateCall(mContext, phone);
            }
        } else {
            dialog.dismiss();
        }
    }
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mMakePrimary = isChecked;
    }
    public void onDismiss(DialogInterface dialog) {
        mPhonesCursor.close();
    }
    private static class PhonesAdapter extends ArrayAdapter<PhoneItem> {
        private final boolean sendSms;
        private final Sources mSources;
        public PhonesAdapter(Context context, List<PhoneItem> objects, boolean sendSms) {
            super(context, R.layout.phone_disambig_item,
                    android.R.id.text2, objects);
            this.sendSms = sendSms;
            mSources = Sources.getInstance(context);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            PhoneItem item = getItem(position);
            ContactsSource source = mSources.getInflatedSource(item.accountType,
                    ContactsSource.LEVEL_SUMMARY);
            TextView typeView = (TextView)view.findViewById(android.R.id.text1);
            DataKind kind = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
            if (kind != null) {
                ContentValues values = new ContentValues();
                values.put(Phone.TYPE, item.type);
                values.put(Phone.LABEL, item.label);
                StringInflater header = sendSms ? kind.actionAltHeader : kind.actionHeader;
                typeView.setText(header.inflateUsing(getContext(), values));
            } else {
                typeView.setText(R.string.call_other);
            }
            return view;
        }
    }
    private class PhoneItem implements Collapsible<PhoneItem> {
        final long id;
        final String phoneNumber;
        final String accountType;
        final long type;
        final String label;
        public PhoneItem(long id, String phoneNumber, String accountType, int type, String label) {
            this.id = id;
            this.phoneNumber = (phoneNumber != null ? phoneNumber : "");
            this.accountType = accountType;
            this.type = type;
            this.label = label;
        }
        public boolean collapseWith(PhoneItem phoneItem) {
            if (!shouldCollapseWith(phoneItem)) {
                return false;
            }
            return true;
        }
        public boolean shouldCollapseWith(PhoneItem phoneItem) {
            if (PhoneNumberUtils.compare(PhoneDisambigDialog.this.mContext,
                    phoneNumber, phoneItem.phoneNumber)) {
                return true;
            }
            return false;
        }
        @Override
        public String toString() {
            return phoneNumber;
        }
    }
    private ArrayList<PhoneItem> makePhoneItemsList(Cursor phonesCursor) {
        ArrayList<PhoneItem> phoneList = new ArrayList<PhoneItem>();
        phonesCursor.moveToPosition(-1);
        while (phonesCursor.moveToNext()) {
            long id = phonesCursor.getLong(phonesCursor.getColumnIndex(Data._ID));
            String phone = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.NUMBER));
            String accountType =
                    phonesCursor.getString(phonesCursor.getColumnIndex(RawContacts.ACCOUNT_TYPE));
            int type = phonesCursor.getInt(phonesCursor.getColumnIndex(Phone.TYPE));
            String label = phonesCursor.getString(phonesCursor.getColumnIndex(Phone.LABEL));
            phoneList.add(new PhoneItem(id, phone, accountType, type, label));
        }
        return phoneList;
    }
}
