public class EmailAddressAdapter extends ResourceCursorAdapter {
    public static final int ID_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int DATA_INDEX = 2;
    protected static final String SORT_ORDER =
            Contacts.TIMES_CONTACTED + " DESC, " + Contacts.DISPLAY_NAME;
    protected ContentResolver mContentResolver;
    protected static final String[] PROJECTION = {
        Data._ID,               
        Contacts.DISPLAY_NAME,  
        Email.DATA              
    };
    public EmailAddressAdapter(Context context) {
        super(context, R.layout.recipient_dropdown_item, null);
        mContentResolver = context.getContentResolver();
    }
    @Override
    public final String convertToString(Cursor cursor) {
        String name = cursor.getString(NAME_INDEX);
        String address = cursor.getString(DATA_INDEX);
        return new Address(address, name).toString();
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text1 = (TextView)view.findViewById(R.id.text1);
        TextView text2 = (TextView)view.findViewById(R.id.text2);
        text1.setText(cursor.getString(NAME_INDEX));
        text2.setText(cursor.getString(DATA_INDEX));
    }
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String filter = constraint == null ? "" : constraint.toString();
        Uri uri = Uri.withAppendedPath(Email.CONTENT_FILTER_URI, Uri.encode(filter));
        Cursor c = mContentResolver.query(uri, PROJECTION, null, null, SORT_ORDER);
        if (c != null) {
            c.getCount();
        }
        return c;
    }
    public void setAccount(Account account) { }
}
