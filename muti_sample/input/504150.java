public class GalEmailAddressAdapter extends EmailAddressAdapter {
    private static final boolean DEBUG_GAL_LOG = false;
    private static final int MINIMUM_GAL_CONSTRAINT_LENGTH = 3;
    private Activity mActivity;
    private Account mAccount;
    private boolean mAccountHasGal;
    private String mAccountEmailDomain;
    private LayoutInflater mInflater;
    private int mSeparatorDisplayCount;
    private int mSeparatorTotalCount;
    public GalEmailAddressAdapter(Activity activity) {
        super(activity);
        mActivity = activity;
        mAccount = null;
        mAccountHasGal = false;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public void setAccount(Account account) {
        mAccount = account;
        mAccountHasGal = false;
        int finalSplit = mAccount.mEmailAddress.lastIndexOf('@');
        mAccountEmailDomain = mAccount.mEmailAddress.substring(finalSplit + 1);
    }
    private void checkGalAccount(Account account) {
        HostAuth ha = HostAuth.restoreHostAuthWithId(mActivity, account.mHostAuthKeyRecv);
        if (ha != null) {
            if ("eas".equalsIgnoreCase(ha.mProtocol)) {
                mAccountHasGal = true;
                return;
            }
        }
        mAccount = null;
        mAccountHasGal = false;
    }
    @Override
    public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
        if (mAccount != null && mAccountHasGal == false) {
            checkGalAccount(mAccount);
        }
        Cursor contactsCursor = super.runQueryOnBackgroundThread(constraint);
        if (!mAccountHasGal || constraint == null) {
            return contactsCursor;
        }
        final String constraintString = constraint.toString().trim();
        if (constraintString.length() < MINIMUM_GAL_CONSTRAINT_LENGTH) {
            return contactsCursor;
        }
        final MatrixCursor matrixCursor = new MatrixCursor(ExchangeProvider.GAL_PROJECTION);
        final MyMergeCursor mergedResultCursor =
            new MyMergeCursor(new Cursor[] {contactsCursor, matrixCursor});
        mergedResultCursor.setSeparatorPosition(contactsCursor.getCount());
        mSeparatorDisplayCount = -1;
        mSeparatorTotalCount = -1;
        new Thread(new Runnable() {
            public void run() {
                Uri galUri =
                    ExchangeProvider.GAL_URI.buildUpon()
                        .appendPath(Long.toString(mAccount.mId))
                        .appendPath(constraintString).build();
                if (DEBUG_GAL_LOG) {
                    Log.d(Email.LOG_TAG, "Query: " + galUri);
                }
                final Cursor galCursor =
                    mContentResolver.query(galUri, ExchangeProvider.GAL_PROJECTION,
                            null, null, null);
                if (mergedResultCursor.isClosed()) {
                    if (DEBUG_GAL_LOG) {
                        Log.d(Email.LOG_TAG, "Drop result (cursor closed, bg thread)");
                    }
                    return;
                }
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (mergedResultCursor.isClosed()) {
                            if (DEBUG_GAL_LOG) {
                                Log.d(Email.LOG_TAG, "Drop result (cursor closed, ui thread)");
                            }
                            return;
                        }
                        if (galCursor == null || galCursor.getCount() == 0) {
                            if (DEBUG_GAL_LOG) {
                                Log.d(Email.LOG_TAG, "Drop empty result");
                            }
                            mergedResultCursor.setSeparatorPosition(ListView.INVALID_POSITION);
                            GalEmailAddressAdapter.this.notifyDataSetChanged();
                            return;
                        }
                        galCursor.moveToPosition(-1);
                        while (galCursor.moveToNext()) {
                            MatrixCursor.RowBuilder rb = matrixCursor.newRow();
                            rb.add(galCursor.getLong(ExchangeProvider.GAL_COLUMN_ID));
                            rb.add(galCursor.getString(ExchangeProvider.GAL_COLUMN_DISPLAYNAME));
                            rb.add(galCursor.getString(ExchangeProvider.GAL_COLUMN_DATA));
                        }
                        mSeparatorDisplayCount = galCursor.getCount();
                        mSeparatorTotalCount =
                            galCursor.getExtras().getInt(ExchangeProvider.EXTRAS_TOTAL_RESULTS);
                        if (DEBUG_GAL_LOG) {
                            Log.d(Email.LOG_TAG, "Notify result, added=" + mSeparatorDisplayCount);
                        }
                        GalEmailAddressAdapter.this.notifyDataSetChanged();
                    }});
            }}).start();
        return mergedResultCursor;
    }
    private int getSeparatorPosition() {
        Cursor c = this.getCursor();
        if (c instanceof MyMergeCursor) {
            return ((MyMergeCursor)c).getSeparatorPosition();
        } else {
            return ListView.INVALID_POSITION;
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position == getSeparatorPosition()) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == getSeparatorPosition()) {
            View separator;
            separator = mInflater.inflate(R.layout.recipient_dropdown_separator, parent, false);
            TextView text1 = (TextView) separator.findViewById(R.id.text1);
            View progress = separator.findViewById(R.id.progress);
            String bannerText;
            if (mSeparatorDisplayCount == -1) {
                bannerText = mContext.getString(R.string.gal_searching_fmt, mAccountEmailDomain);
                progress.setVisibility(View.VISIBLE);
            } else {
                if (mSeparatorDisplayCount == mSeparatorTotalCount) {
                    bannerText = mContext.getResources().getQuantityString(
                            R.plurals.gal_completed_fmt, mSeparatorDisplayCount,
                            mSeparatorDisplayCount, mAccountEmailDomain);
                } else {
                    bannerText = mContext.getString(R.string.gal_completed_limited_fmt,
                            mSeparatorDisplayCount, mAccountEmailDomain);
                }
                progress.setVisibility(View.GONE);
            }
            text1.setText(bannerText);
            return separator;
        }
        return super.getView(getRealPosition(position), convertView, parent);
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public boolean isEnabled(int position) {
        return position != getSeparatorPosition();
    }
    @Override
    public int getCount() {
        int count = super.getCount();
        if (getSeparatorPosition() != ListView.INVALID_POSITION) {
            count += 1;
        }
        return count;
    }
    private int getRealPosition(int pos) {
        int separatorPosition = getSeparatorPosition();
        if (separatorPosition == ListView.INVALID_POSITION) {
            return pos;
        } else if (pos <= separatorPosition) {
            return pos;
        } else {
            return pos - 1;
        }
    }
    @Override
    public Object getItem(int pos) {
        return super.getItem(getRealPosition(pos));
    }
    @Override
    public long getItemId(int pos) {
        if (pos == getSeparatorPosition()) {
            return View.NO_ID;
        }
        return super.getItemId(getRealPosition(pos));
    }
    private static class MyMergeCursor extends MergeCursor {
        private int mSeparatorPosition;
        public MyMergeCursor(Cursor[] cursors) {
            super(cursors);
            mClosed = false;
            mSeparatorPosition = ListView.INVALID_POSITION;
        }
        @Override
        public synchronized void close() {
            super.close();
            if (DEBUG_GAL_LOG) {
                Log.d(Email.LOG_TAG, "Closing MyMergeCursor");
            }
        }
        @Override
        public synchronized boolean isClosed() {
            return super.isClosed();
        }
        void setSeparatorPosition(int newPos) {
            mSeparatorPosition = newPos;
        }
        int getSeparatorPosition() {
            return mSeparatorPosition;
        }
    }
}
