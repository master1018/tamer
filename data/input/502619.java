public class DataStatus {
    private int mPresence = -1;
    private String mStatus = null;
    private long mTimestamp = -1;
    private String mResPackage = null;
    private int mIconRes = -1;
    private int mLabelRes = -1;
    public DataStatus() {
    }
    public DataStatus(Cursor cursor) {
        fromCursor(cursor);
    }
    public void possibleUpdate(Cursor cursor) {
        final boolean hasStatus = !isNull(cursor, Data.STATUS);
        final boolean hasTimestamp = !isNull(cursor, Data.STATUS_TIMESTAMP);
        if (!hasStatus) return;
        if (isValid() && !hasTimestamp) return;
        if (hasTimestamp) {
            final long newTimestamp = getLong(cursor, Data.STATUS_TIMESTAMP, -1);
            if (newTimestamp < mTimestamp) return;
            mTimestamp = newTimestamp;
        }
        fromCursor(cursor);
    }
    private void fromCursor(Cursor cursor) {
        mPresence = getInt(cursor, Data.PRESENCE, -1);
        mStatus = getString(cursor, Data.STATUS);
        mTimestamp = getLong(cursor, Data.STATUS_TIMESTAMP, -1);
        mResPackage = getString(cursor, Data.STATUS_RES_PACKAGE);
        mIconRes = getInt(cursor, Data.STATUS_ICON, -1);
        mLabelRes = getInt(cursor, Data.STATUS_LABEL, -1);
    }
    public boolean isValid() {
        return !TextUtils.isEmpty(mStatus);
    }
    public int getPresence() {
        return mPresence;
    }
    public CharSequence getStatus() {
        return mStatus;
    }
    public CharSequence getTimestampLabel(Context context) {
        final PackageManager pm = context.getPackageManager();
        if (mResPackage == null) mResPackage = context.getPackageName();
        final boolean validTimestamp = mTimestamp > 0;
        final boolean validLabel = mResPackage != null && mLabelRes != -1;
        final CharSequence timeClause = validTimestamp ? DateUtils.getRelativeTimeSpanString(
                mTimestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE) : null;
        final CharSequence labelClause = validLabel ? pm.getText(mResPackage, mLabelRes,
                null) : null;
        if (validTimestamp && validLabel) {
            return context.getString(
                    com.android.internal.R.string.contact_status_update_attribution_with_date,
                    timeClause, labelClause);
        } else if (validLabel) {
            return context.getString(
                    com.android.internal.R.string.contact_status_update_attribution,
                    labelClause);
        } else if (validTimestamp) {
            return timeClause;
        } else {
            return null;
        }
    }
    public Drawable getIcon(Context context) {
        final PackageManager pm = context.getPackageManager();
        if (mResPackage == null) mResPackage = context.getPackageName();
        final boolean validIcon = mResPackage != null && mIconRes != -1;
        return validIcon ? pm.getDrawable(mResPackage, mIconRes, null) : null;
    }
    private static String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }
    private static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }
    private static int getInt(Cursor cursor, String columnName, int missingValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? missingValue : cursor.getInt(columnIndex);
    }
    private static long getLong(Cursor cursor, String columnName, long missingValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? missingValue : cursor.getLong(columnIndex);
    }
    private static boolean isNull(Cursor cursor, String columnName) {
        return cursor.isNull(cursor.getColumnIndex(columnName));
    }
}
