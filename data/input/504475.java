public class ContentProviderOperation implements Parcelable {
    public final static int TYPE_INSERT = 1;
    public final static int TYPE_UPDATE = 2;
    public final static int TYPE_DELETE = 3;
    public final static int TYPE_ASSERT = 4;
    private final int mType;
    private final Uri mUri;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final ContentValues mValues;
    private final Integer mExpectedCount;
    private final ContentValues mValuesBackReferences;
    private final Map<Integer, Integer> mSelectionArgsBackReferences;
    private final boolean mYieldAllowed;
    private final static String TAG = "ContentProviderOperation";
    private ContentProviderOperation(Builder builder) {
        mType = builder.mType;
        mUri = builder.mUri;
        mValues = builder.mValues;
        mSelection = builder.mSelection;
        mSelectionArgs = builder.mSelectionArgs;
        mExpectedCount = builder.mExpectedCount;
        mSelectionArgsBackReferences = builder.mSelectionArgsBackReferences;
        mValuesBackReferences = builder.mValuesBackReferences;
        mYieldAllowed = builder.mYieldAllowed;
    }
    private ContentProviderOperation(Parcel source) {
        mType = source.readInt();
        mUri = Uri.CREATOR.createFromParcel(source);
        mValues = source.readInt() != 0 ? ContentValues.CREATOR.createFromParcel(source) : null;
        mSelection = source.readInt() != 0 ? source.readString() : null;
        mSelectionArgs = source.readInt() != 0 ? source.readStringArray() : null;
        mExpectedCount = source.readInt() != 0 ? source.readInt() : null;
        mValuesBackReferences = source.readInt() != 0
                ? ContentValues.CREATOR.createFromParcel(source)
                : null;
        mSelectionArgsBackReferences = source.readInt() != 0
                ? new HashMap<Integer, Integer>()
                : null;
        if (mSelectionArgsBackReferences != null) {
            final int count = source.readInt();
            for (int i = 0; i < count; i++) {
                mSelectionArgsBackReferences.put(source.readInt(), source.readInt());
            }
        }
        mYieldAllowed = source.readInt() != 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mType);
        Uri.writeToParcel(dest, mUri);
        if (mValues != null) {
            dest.writeInt(1);
            mValues.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (mSelection != null) {
            dest.writeInt(1);
            dest.writeString(mSelection);
        } else {
            dest.writeInt(0);
        }
        if (mSelectionArgs != null) {
            dest.writeInt(1);
            dest.writeStringArray(mSelectionArgs);
        } else {
            dest.writeInt(0);
        }
        if (mExpectedCount != null) {
            dest.writeInt(1);
            dest.writeInt(mExpectedCount);
        } else {
            dest.writeInt(0);
        }
        if (mValuesBackReferences != null) {
            dest.writeInt(1);
            mValuesBackReferences.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        if (mSelectionArgsBackReferences != null) {
            dest.writeInt(1);
            dest.writeInt(mSelectionArgsBackReferences.size());
            for (Map.Entry<Integer, Integer> entry : mSelectionArgsBackReferences.entrySet()) {
                dest.writeInt(entry.getKey());
                dest.writeInt(entry.getValue());
            }
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(mYieldAllowed ? 1 : 0);
    }
    public static Builder newInsert(Uri uri) {
        return new Builder(TYPE_INSERT, uri);
    }
    public static Builder newUpdate(Uri uri) {
        return new Builder(TYPE_UPDATE, uri);
    }
    public static Builder newDelete(Uri uri) {
        return new Builder(TYPE_DELETE, uri);
    }
    public static Builder newAssertQuery(Uri uri) {
        return new Builder(TYPE_ASSERT, uri);
    }
    public Uri getUri() {
        return mUri;
    }
    public boolean isYieldAllowed() {
        return mYieldAllowed;
    }
    public int getType() {
        return mType;
    }
    public boolean isWriteOperation() {
        return mType == TYPE_DELETE || mType == TYPE_INSERT || mType == TYPE_UPDATE;
    }
    public boolean isReadOperation() {
        return mType == TYPE_ASSERT;
    }
    public ContentProviderResult apply(ContentProvider provider, ContentProviderResult[] backRefs,
            int numBackRefs) throws OperationApplicationException {
        ContentValues values = resolveValueBackReferences(backRefs, numBackRefs);
        String[] selectionArgs =
                resolveSelectionArgsBackReferences(backRefs, numBackRefs);
        if (mType == TYPE_INSERT) {
            Uri newUri = provider.insert(mUri, values);
            if (newUri == null) {
                throw new OperationApplicationException("insert failed");
            }
            return new ContentProviderResult(newUri);
        }
        int numRows;
        if (mType == TYPE_DELETE) {
            numRows = provider.delete(mUri, mSelection, selectionArgs);
        } else if (mType == TYPE_UPDATE) {
            numRows = provider.update(mUri, values, mSelection, selectionArgs);
        } else if (mType == TYPE_ASSERT) {
            String[] projection =  null;
            if (values != null) {
                final ArrayList<String> projectionList = new ArrayList<String>();
                for (Map.Entry<String, Object> entry : values.valueSet()) {
                    projectionList.add(entry.getKey());
                }
                projection = projectionList.toArray(new String[projectionList.size()]);
            }
            final Cursor cursor = provider.query(mUri, projection, mSelection, selectionArgs, null);
            try {
                numRows = cursor.getCount();
                if (projection != null) {
                    while (cursor.moveToNext()) {
                        for (int i = 0; i < projection.length; i++) {
                            final String cursorValue = cursor.getString(i);
                            final String expectedValue = values.getAsString(projection[i]);
                            if (!TextUtils.equals(cursorValue, expectedValue)) {
                                Log.e(TAG, this.toString());
                                throw new OperationApplicationException("Found value " + cursorValue
                                        + " when expected " + expectedValue + " for column "
                                        + projection[i]);
                            }
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        } else {
            Log.e(TAG, this.toString());
            throw new IllegalStateException("bad type, " + mType);
        }
        if (mExpectedCount != null && mExpectedCount != numRows) {
            Log.e(TAG, this.toString());
            throw new OperationApplicationException("wrong number of rows: " + numRows);
        }
        return new ContentProviderResult(numRows);
    }
    public ContentValues resolveValueBackReferences(
            ContentProviderResult[] backRefs, int numBackRefs) {
        if (mValuesBackReferences == null) {
            return mValues;
        }
        final ContentValues values;
        if (mValues == null) {
            values = new ContentValues();
        } else {
            values = new ContentValues(mValues);
        }
        for (Map.Entry<String, Object> entry : mValuesBackReferences.valueSet()) {
            String key = entry.getKey();
            Integer backRefIndex = mValuesBackReferences.getAsInteger(key);
            if (backRefIndex == null) {
                Log.e(TAG, this.toString());
                throw new IllegalArgumentException("values backref " + key + " is not an integer");
            }
            values.put(key, backRefToValue(backRefs, numBackRefs, backRefIndex));
        }
        return values;
    }
    public String[] resolveSelectionArgsBackReferences(
            ContentProviderResult[] backRefs, int numBackRefs) {
        if (mSelectionArgsBackReferences == null) {
            return mSelectionArgs;
        }
        String[] newArgs = new String[mSelectionArgs.length];
        System.arraycopy(mSelectionArgs, 0, newArgs, 0, mSelectionArgs.length);
        for (Map.Entry<Integer, Integer> selectionArgBackRef
                : mSelectionArgsBackReferences.entrySet()) {
            final Integer selectionArgIndex = selectionArgBackRef.getKey();
            final int backRefIndex = selectionArgBackRef.getValue();
            newArgs[selectionArgIndex] =
                    String.valueOf(backRefToValue(backRefs, numBackRefs, backRefIndex));
        }
        return newArgs;
    }
    @Override
    public String toString() {
        return "mType: " + mType + ", mUri: " + mUri +
                ", mSelection: " + mSelection +
                ", mExpectedCount: " + mExpectedCount +
                ", mYieldAllowed: " + mYieldAllowed +
                ", mValues: " + mValues +
                ", mValuesBackReferences: " + mValuesBackReferences +
                ", mSelectionArgsBackReferences: " + mSelectionArgsBackReferences;
    }
    private long backRefToValue(ContentProviderResult[] backRefs, int numBackRefs,
            Integer backRefIndex) {
        if (backRefIndex >= numBackRefs) {
            Log.e(TAG, this.toString());
            throw new ArrayIndexOutOfBoundsException("asked for back ref " + backRefIndex
                    + " but there are only " + numBackRefs + " back refs");
        }
        ContentProviderResult backRef = backRefs[backRefIndex];
        long backRefValue;
        if (backRef.uri != null) {
            backRefValue = ContentUris.parseId(backRef.uri);
        } else {
            backRefValue = backRef.count;
        }
        return backRefValue;
    }
    public int describeContents() {
        return 0;
    }
    public static final Creator<ContentProviderOperation> CREATOR =
            new Creator<ContentProviderOperation>() {
        public ContentProviderOperation createFromParcel(Parcel source) {
            return new ContentProviderOperation(source);
        }
        public ContentProviderOperation[] newArray(int size) {
            return new ContentProviderOperation[size];
        }
    };
    public static class Builder {
        private final int mType;
        private final Uri mUri;
        private String mSelection;
        private String[] mSelectionArgs;
        private ContentValues mValues;
        private Integer mExpectedCount;
        private ContentValues mValuesBackReferences;
        private Map<Integer, Integer> mSelectionArgsBackReferences;
        private boolean mYieldAllowed;
        private Builder(int type, Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("uri must not be null");
            }
            mType = type;
            mUri = uri;
        }
        public ContentProviderOperation build() {
            if (mType == TYPE_UPDATE) {
                if ((mValues == null || mValues.size() == 0)
                        && (mValuesBackReferences == null || mValuesBackReferences.size() == 0)) {
                    throw new IllegalArgumentException("Empty values");
                }
            }
            if (mType == TYPE_ASSERT) {
                if ((mValues == null || mValues.size() == 0)
                        && (mValuesBackReferences == null || mValuesBackReferences.size() == 0)
                        && (mExpectedCount == null)) {
                    throw new IllegalArgumentException("Empty values");
                }
            }
            return new ContentProviderOperation(this);
        }
        public Builder withValueBackReferences(ContentValues backReferences) {
            if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException(
                        "only inserts, updates, and asserts can have value back-references");
            }
            mValuesBackReferences = backReferences;
            return this;
        }
        public Builder withValueBackReference(String key, int previousResult) {
            if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException(
                        "only inserts, updates, and asserts can have value back-references");
            }
            if (mValuesBackReferences == null) {
                mValuesBackReferences = new ContentValues();
            }
            mValuesBackReferences.put(key, previousResult);
            return this;
        }
        public Builder withSelectionBackReference(int selectionArgIndex, int previousResult) {
            if (mType != TYPE_UPDATE && mType != TYPE_DELETE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException("only updates, deletes, and asserts "
                        + "can have selection back-references");
            }
            if (mSelectionArgsBackReferences == null) {
                mSelectionArgsBackReferences = new HashMap<Integer, Integer>();
            }
            mSelectionArgsBackReferences.put(selectionArgIndex, previousResult);
            return this;
        }
        public Builder withValues(ContentValues values) {
            if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException(
                        "only inserts, updates, and asserts can have values");
            }
            if (mValues == null) {
                mValues = new ContentValues();
            }
            mValues.putAll(values);
            return this;
        }
        public Builder withValue(String key, Object value) {
            if (mType != TYPE_INSERT && mType != TYPE_UPDATE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException("only inserts and updates can have values");
            }
            if (mValues == null) {
                mValues = new ContentValues();
            }
            if (value == null) {
                mValues.putNull(key);
            } else if (value instanceof String) {
                mValues.put(key, (String) value);
            } else if (value instanceof Byte) {
                mValues.put(key, (Byte) value);
            } else if (value instanceof Short) {
                mValues.put(key, (Short) value);
            } else if (value instanceof Integer) {
                mValues.put(key, (Integer) value);
            } else if (value instanceof Long) {
                mValues.put(key, (Long) value);
            } else if (value instanceof Float) {
                mValues.put(key, (Float) value);
            } else if (value instanceof Double) {
                mValues.put(key, (Double) value);
            } else if (value instanceof Boolean) {
                mValues.put(key, (Boolean) value);
            } else if (value instanceof byte[]) {
                mValues.put(key, (byte[]) value);
            } else {
                throw new IllegalArgumentException("bad value type: " + value.getClass().getName());
            }
            return this;
        }
        public Builder withSelection(String selection, String[] selectionArgs) {
            if (mType != TYPE_UPDATE && mType != TYPE_DELETE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException(
                        "only updates, deletes, and asserts can have selections");
            }
            mSelection = selection;
            if (selectionArgs == null) {
                mSelectionArgs = null;
            } else {
                mSelectionArgs = new String[selectionArgs.length];
                System.arraycopy(selectionArgs, 0, mSelectionArgs, 0, selectionArgs.length);
            }
            return this;
        }
        public Builder withExpectedCount(int count) {
            if (mType != TYPE_UPDATE && mType != TYPE_DELETE && mType != TYPE_ASSERT) {
                throw new IllegalArgumentException(
                        "only updates, deletes, and asserts can have expected counts");
            }
            mExpectedCount = count;
            return this;
        }
        public Builder withYieldAllowed(boolean yieldAllowed) {
            mYieldAllowed = yieldAllowed;
            return this;
        }
    }
}
