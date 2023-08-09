public class EntityDelta implements Parcelable {
    private static final String TAG = "EntityDelta";
    private static final boolean LOGV = true;
    private ValuesDelta mValues;
    private HashMap<String, ArrayList<ValuesDelta>> mEntries = Maps.newHashMap();
    public EntityDelta() {
    }
    public EntityDelta(ValuesDelta values) {
        mValues = values;
    }
    public static EntityDelta fromBefore(Entity before) {
        final EntityDelta entity = new EntityDelta();
        entity.mValues = ValuesDelta.fromBefore(before.getEntityValues());
        entity.mValues.setIdColumn(RawContacts._ID);
        for (NamedContentValues namedValues : before.getSubValues()) {
            entity.addEntry(ValuesDelta.fromBefore(namedValues.values));
        }
        return entity;
    }
    public static EntityDelta mergeAfter(EntityDelta local, EntityDelta remote) {
        final ValuesDelta remoteValues = remote.mValues;
        if (local == null && (remoteValues.isDelete() || remoteValues.isTransient())) return null;
        if (local == null) local = new EntityDelta();
        if (LOGV) {
            final Long localVersion = (local.mValues == null) ? null : local.mValues
                    .getAsLong(RawContacts.VERSION);
            final Long remoteVersion = remote.mValues.getAsLong(RawContacts.VERSION);
            Log.d(TAG, "Re-parenting from original version " + remoteVersion + " to "
                    + localVersion);
        }
        local.mValues = ValuesDelta.mergeAfter(local.mValues, remote.mValues);
        for (ArrayList<ValuesDelta> mimeEntries : remote.mEntries.values()) {
            for (ValuesDelta remoteEntry : mimeEntries) {
                final Long childId = remoteEntry.getId();
                final ValuesDelta localEntry = local.getEntry(childId);
                final ValuesDelta merged = ValuesDelta.mergeAfter(localEntry, remoteEntry);
                if (localEntry == null && merged != null) {
                    local.addEntry(merged);
                }
            }
        }
        return local;
    }
    public ValuesDelta getValues() {
        return mValues;
    }
    public boolean isContactInsert() {
        return mValues.isInsert();
    }
    public ValuesDelta getPrimaryEntry(String mimeType) {
        final ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) return null;
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isPrimary()) {
                return entry;
            }
        }
        return mimeEntries.size() > 0 ? mimeEntries.get(0) : null;
    }
    public ValuesDelta getSuperPrimaryEntry(String mimeType) {
        return getSuperPrimaryEntry(mimeType, true);
    }
    public ValuesDelta getSuperPrimaryEntry(String mimeType, boolean forceSelection) {
        final ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType, false);
        if (mimeEntries == null) return null;
        ValuesDelta primary = null;
        for (ValuesDelta entry : mimeEntries) {
            if (entry.isSuperPrimary()) {
                return entry;
            } else if (entry.isPrimary()) {
                primary = entry;
            }
        }
        if (!forceSelection) {
            return null;
        }
        if (primary != null) {
            return primary;
        }
        return mimeEntries.size() > 0 ? mimeEntries.get(0) : null;
    }
    private ArrayList<ValuesDelta> getMimeEntries(String mimeType, boolean lazyCreate) {
        ArrayList<ValuesDelta> mimeEntries = mEntries.get(mimeType);
        if (mimeEntries == null && lazyCreate) {
            mimeEntries = Lists.newArrayList();
            mEntries.put(mimeType, mimeEntries);
        }
        return mimeEntries;
    }
    public ArrayList<ValuesDelta> getMimeEntries(String mimeType) {
        return getMimeEntries(mimeType, false);
    }
    public int getMimeEntriesCount(String mimeType, boolean onlyVisible) {
        final ArrayList<ValuesDelta> mimeEntries = getMimeEntries(mimeType);
        if (mimeEntries == null) return 0;
        int count = 0;
        for (ValuesDelta child : mimeEntries) {
            if (onlyVisible && !child.isVisible()) continue;
            count++;
        }
        return count;
    }
    public boolean hasMimeEntries(String mimeType) {
        return mEntries.containsKey(mimeType);
    }
    public ValuesDelta addEntry(ValuesDelta entry) {
        final String mimeType = entry.getMimetype();
        getMimeEntries(mimeType, true).add(entry);
        return entry;
    }
    public ValuesDelta getEntry(Long childId) {
        if (childId == null) {
            return null;
        }
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta entry : mimeEntries) {
                if (childId.equals(entry.getId())) {
                    return entry;
                }
            }
        }
        return null;
    }
    public int getEntryCount(boolean onlyVisible) {
        int count = 0;
        for (String mimeType : mEntries.keySet()) {
            count += getMimeEntriesCount(mimeType, onlyVisible);
        }
        return count;
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof EntityDelta) {
            final EntityDelta other = (EntityDelta)object;
            if (!other.mValues.equals(mValues)) return false;
            for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
                for (ValuesDelta child : mimeEntries) {
                    if (!other.containsEntry(child)) return false;
                }
            }
            return true;
        }
        return false;
    }
    private boolean containsEntry(ValuesDelta entry) {
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                if (child.equals(entry)) return true;
            }
        }
        return false;
    }
    public void markDeleted() {
        this.mValues.markDeleted();
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                child.markDeleted();
            }
        }
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("\n(");
        builder.append(mValues.toString());
        builder.append(") = {");
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                builder.append("\n\t");
                child.toString(builder);
            }
        }
        builder.append("\n}\n");
        return builder.toString();
    }
    private void possibleAdd(ArrayList<ContentProviderOperation> diff,
            ContentProviderOperation.Builder builder) {
        if (builder != null) {
            diff.add(builder.build());
        }
    }
    public void buildAssert(ArrayList<ContentProviderOperation> buildInto) {
        final boolean isContactInsert = mValues.isInsert();
        if (!isContactInsert) {
            final Long beforeId = mValues.getId();
            final Long beforeVersion = mValues.getAsLong(RawContacts.VERSION);
            if (beforeId == null || beforeVersion == null) return;
            final ContentProviderOperation.Builder builder = ContentProviderOperation
                    .newAssertQuery(RawContacts.CONTENT_URI);
            builder.withSelection(RawContacts._ID + "=" + beforeId, null);
            builder.withValue(RawContacts.VERSION, beforeVersion);
            buildInto.add(builder.build());
        }
    }
    public void buildDiff(ArrayList<ContentProviderOperation> buildInto) {
        final int firstIndex = buildInto.size();
        final boolean isContactInsert = mValues.isInsert();
        final boolean isContactDelete = mValues.isDelete();
        final boolean isContactUpdate = !isContactInsert && !isContactDelete;
        final Long beforeId = mValues.getId();
        Builder builder;
        if (isContactInsert) {
            mValues.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED);
        }
        builder = mValues.buildDiff(RawContacts.CONTENT_URI);
        possibleAdd(buildInto, builder);
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                if (isContactDelete) continue;
                builder = child.buildDiff(Data.CONTENT_URI);
                if (child.isInsert()) {
                    if (isContactInsert) {
                        builder.withValueBackReference(Data.RAW_CONTACT_ID, firstIndex);
                    } else {
                        builder.withValue(Data.RAW_CONTACT_ID, beforeId);
                    }
                } else if (isContactInsert && builder != null) {
                    throw new IllegalArgumentException("When parent insert, child must be also");
                }
                possibleAdd(buildInto, builder);
            }
        }
        final boolean addedOperations = buildInto.size() > firstIndex;
        if (addedOperations && isContactUpdate) {
            builder = buildSetAggregationMode(beforeId, RawContacts.AGGREGATION_MODE_SUSPENDED);
            buildInto.add(firstIndex, builder.build());
            builder = buildSetAggregationMode(beforeId, RawContacts.AGGREGATION_MODE_DEFAULT);
            buildInto.add(builder.build());
        } else if (isContactInsert) {
            builder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
            builder.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT);
            builder.withSelection(RawContacts._ID + "=?", new String[1]);
            builder.withSelectionBackReference(0, firstIndex);
            buildInto.add(builder.build());
        }
    }
    protected Builder buildSetAggregationMode(Long beforeId, int mode) {
        Builder builder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
        builder.withValue(RawContacts.AGGREGATION_MODE, mode);
        builder.withSelection(RawContacts._ID + "=" + beforeId, null);
        return builder;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        final int size = this.getEntryCount(false);
        dest.writeInt(size);
        dest.writeParcelable(mValues, flags);
        for (ArrayList<ValuesDelta> mimeEntries : mEntries.values()) {
            for (ValuesDelta child : mimeEntries) {
                dest.writeParcelable(child, flags);
            }
        }
    }
    public void readFromParcel(Parcel source) {
        final ClassLoader loader = getClass().getClassLoader();
        final int size = source.readInt();
        mValues = source.<ValuesDelta> readParcelable(loader);
        for (int i = 0; i < size; i++) {
            final ValuesDelta child = source.<ValuesDelta> readParcelable(loader);
            this.addEntry(child);
        }
    }
    public static final Parcelable.Creator<EntityDelta> CREATOR = new Parcelable.Creator<EntityDelta>() {
        public EntityDelta createFromParcel(Parcel in) {
            final EntityDelta state = new EntityDelta();
            state.readFromParcel(in);
            return state;
        }
        public EntityDelta[] newArray(int size) {
            return new EntityDelta[size];
        }
    };
    public static class ValuesDelta implements Parcelable {
        protected ContentValues mBefore;
        protected ContentValues mAfter;
        protected String mIdColumn = BaseColumns._ID;
        private boolean mFromTemplate;
        protected static int sNextInsertId = -1;
        protected ValuesDelta() {
        }
        public static ValuesDelta fromBefore(ContentValues before) {
            final ValuesDelta entry = new ValuesDelta();
            entry.mBefore = before;
            entry.mAfter = new ContentValues();
            return entry;
        }
        public static ValuesDelta fromAfter(ContentValues after) {
            final ValuesDelta entry = new ValuesDelta();
            entry.mBefore = null;
            entry.mAfter = after;
            entry.mAfter.put(entry.mIdColumn, sNextInsertId--);
            return entry;
        }
        public ContentValues getAfter() {
            return mAfter;
        }
        public String getAsString(String key) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsString(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsString(key);
            } else {
                return null;
            }
        }
        public byte[] getAsByteArray(String key) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsByteArray(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsByteArray(key);
            } else {
                return null;
            }
        }
        public Long getAsLong(String key) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsLong(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsLong(key);
            } else {
                return null;
            }
        }
        public Integer getAsInteger(String key) {
            return getAsInteger(key, null);
        }
        public Integer getAsInteger(String key, Integer defaultValue) {
            if (mAfter != null && mAfter.containsKey(key)) {
                return mAfter.getAsInteger(key);
            } else if (mBefore != null && mBefore.containsKey(key)) {
                return mBefore.getAsInteger(key);
            } else {
                return defaultValue;
            }
        }
        public String getMimetype() {
            return getAsString(Data.MIMETYPE);
        }
        public Long getId() {
            return getAsLong(mIdColumn);
        }
        public void setIdColumn(String idColumn) {
            mIdColumn = idColumn;
        }
        public boolean isPrimary() {
            final Long isPrimary = getAsLong(Data.IS_PRIMARY);
            return isPrimary == null ? false : isPrimary != 0;
        }
        public void setFromTemplate(boolean isFromTemplate) {
            mFromTemplate = isFromTemplate;
        }
        public boolean isFromTemplate() {
            return mFromTemplate;
        }
        public boolean isSuperPrimary() {
            final Long isSuperPrimary = getAsLong(Data.IS_SUPER_PRIMARY);
            return isSuperPrimary == null ? false : isSuperPrimary != 0;
        }
        public boolean beforeExists() {
            return (mBefore != null && mBefore.containsKey(mIdColumn));
        }
        public boolean isVisible() {
            return (mAfter != null);
        }
        public boolean isDelete() {
            return beforeExists() && (mAfter == null);
        }
        public boolean isTransient() {
            return (mBefore == null) && (mAfter == null);
        }
        public boolean isUpdate() {
            return beforeExists() && (mAfter != null && mAfter.size() > 0);
        }
        public boolean isNoop() {
            return beforeExists() && (mAfter != null && mAfter.size() == 0);
        }
        public boolean isInsert() {
            return !beforeExists() && (mAfter != null);
        }
        public void markDeleted() {
            mAfter = null;
        }
        private void ensureUpdate() {
            if (mAfter == null) {
                mAfter = new ContentValues();
            }
        }
        public void put(String key, String value) {
            ensureUpdate();
            mAfter.put(key, value);
        }
        public void put(String key, byte[] value) {
            ensureUpdate();
            mAfter.put(key, value);
        }
        public void put(String key, int value) {
            ensureUpdate();
            mAfter.put(key, value);
        }
        public Set<String> keySet() {
            final HashSet<String> keys = Sets.newHashSet();
            if (mBefore != null) {
                for (Map.Entry<String, Object> entry : mBefore.valueSet()) {
                    keys.add(entry.getKey());
                }
            }
            if (mAfter != null) {
                for (Map.Entry<String, Object> entry : mAfter.valueSet()) {
                    keys.add(entry.getKey());
                }
            }
            return keys;
        }
        public ContentValues getCompleteValues() {
            final ContentValues values = new ContentValues();
            if (mBefore != null) {
                values.putAll(mBefore);
            }
            if (mAfter != null) {
                values.putAll(mAfter);
            }
            if (values.containsKey(GroupMembership.GROUP_ROW_ID)) {
                values.remove(GroupMembership.GROUP_SOURCE_ID);
            }
            return values;
        }
        public static ValuesDelta mergeAfter(ValuesDelta local, ValuesDelta remote) {
            if (local == null && (remote.isDelete() || remote.isTransient())) return null;
            if (local == null) local = new ValuesDelta();
            if (!local.beforeExists()) {
                local.mAfter = remote.getCompleteValues();
            } else {
                local.mAfter = remote.mAfter;
            }
            return local;
        }
        @Override
        public boolean equals(Object object) {
            if (object instanceof ValuesDelta) {
                final ValuesDelta other = (ValuesDelta)object;
                return this.subsetEquals(other) && other.subsetEquals(this);
            }
            return false;
        }
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            toString(builder);
            return builder.toString();
        }
        public void toString(StringBuilder builder) {
            builder.append("{ ");
            for (String key : this.keySet()) {
                builder.append(key);
                builder.append("=");
                builder.append(this.getAsString(key));
                builder.append(", ");
            }
            builder.append("}");
        }
        public boolean subsetEquals(ValuesDelta other) {
            for (String key : this.keySet()) {
                final String ourValue = this.getAsString(key);
                final String theirValue = other.getAsString(key);
                if (ourValue == null) {
                    if (theirValue != null) return false;
                } else {
                    if (!ourValue.equals(theirValue)) return false;
                }
            }
            return true;
        }
        public ContentProviderOperation.Builder buildDiff(Uri targetUri) {
            Builder builder = null;
            if (isInsert()) {
                mAfter.remove(mIdColumn);
                builder = ContentProviderOperation.newInsert(targetUri);
                builder.withValues(mAfter);
            } else if (isDelete()) {
                builder = ContentProviderOperation.newDelete(targetUri);
                builder.withSelection(mIdColumn + "=" + getId(), null);
            } else if (isUpdate()) {
                builder = ContentProviderOperation.newUpdate(targetUri);
                builder.withSelection(mIdColumn + "=" + getId(), null);
                builder.withValues(mAfter);
            }
            return builder;
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mBefore, flags);
            dest.writeParcelable(mAfter, flags);
            dest.writeString(mIdColumn);
        }
        public void readFromParcel(Parcel source) {
            final ClassLoader loader = getClass().getClassLoader();
            mBefore = source.<ContentValues> readParcelable(loader);
            mAfter = source.<ContentValues> readParcelable(loader);
            mIdColumn = source.readString();
        }
        public static final Parcelable.Creator<ValuesDelta> CREATOR = new Parcelable.Creator<ValuesDelta>() {
            public ValuesDelta createFromParcel(Parcel in) {
                final ValuesDelta values = new ValuesDelta();
                values.readFromParcel(in);
                return values;
            }
            public ValuesDelta[] newArray(int size) {
                return new ValuesDelta[size];
            }
        };
    }
}
