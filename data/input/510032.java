public class EntitySet extends ArrayList<EntityDelta> implements Parcelable {
    private boolean mSplitRawContacts;
    private EntitySet() {
    }
    public static EntitySet fromSingle(EntityDelta delta) {
        final EntitySet state = new EntitySet();
        state.add(delta);
        return state;
    }
    public static EntitySet fromQuery(ContentResolver resolver, String selection,
            String[] selectionArgs, String sortOrder) {
        EntityIterator iterator = RawContacts.newEntityIterator(resolver.query(
                RawContactsEntity.CONTENT_URI, null, selection, selectionArgs,
                sortOrder));
        try {
            final EntitySet state = new EntitySet();
            while (iterator.hasNext()) {
                final Entity before = iterator.next();
                final EntityDelta entity = EntityDelta.fromBefore(before);
                state.add(entity);
            }
            return state;
        } finally {
            iterator.close();
        }
    }
    public static EntitySet mergeAfter(EntitySet local, EntitySet remote) {
        if (local == null) local = new EntitySet();
        for (EntityDelta remoteEntity : remote) {
            final Long rawContactId = remoteEntity.getValues().getId();
            final EntityDelta localEntity = local.getByRawContactId(rawContactId);
            final EntityDelta merged = EntityDelta.mergeAfter(localEntity, remoteEntity);
            if (localEntity == null && merged != null) {
                local.add(merged);
            }
        }
        return local;
    }
    public ArrayList<ContentProviderOperation> buildDiff() {
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        final long rawContactId = this.findRawContactId();
        int firstInsertRow = -1;
        for (EntityDelta delta : this) {
            delta.buildAssert(diff);
        }
        final int assertMark = diff.size();
        int backRefs[] = new int[size()];
        int rawContactIndex = 0;
        for (EntityDelta delta : this) {
            final int firstBatch = diff.size();
            backRefs[rawContactIndex++] = firstBatch;
            delta.buildDiff(diff);
            if (!delta.isContactInsert()) continue;
            if (mSplitRawContacts) continue;
            if (rawContactId != -1) {
                final Builder builder = beginKeepTogether();
                builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId);
                builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID2, firstBatch);
                diff.add(builder.build());
            } else if (firstInsertRow == -1) {
                firstInsertRow = firstBatch;
            } else {
                final Builder builder = beginKeepTogether();
                builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID1, firstInsertRow);
                builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID2, firstBatch);
                diff.add(builder.build());
            }
        }
        if (mSplitRawContacts) {
            buildSplitContactDiff(diff, backRefs);
        }
        if (diff.size() == assertMark) {
            diff.clear();
        }
        return diff;
    }
    protected Builder beginKeepTogether() {
        final Builder builder = ContentProviderOperation
                .newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        return builder;
    }
    private void buildSplitContactDiff(final ArrayList<ContentProviderOperation> diff,
            int[] backRefs) {
        int count = size();
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (i != j) {
                    buildSplitContactDiff(diff, i, j, backRefs);
                }
            }
        }
    }
    private void buildSplitContactDiff(ArrayList<ContentProviderOperation> diff, int index1,
            int index2, int[] backRefs) {
        Builder builder =
                ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_SEPARATE);
        Long rawContactId1 = get(index1).getValues().getAsLong(RawContacts._ID);
        if (rawContactId1 != null && rawContactId1 >= 0) {
            builder.withValue(AggregationExceptions.RAW_CONTACT_ID1, rawContactId1);
        } else {
            builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID1, backRefs[index1]);
        }
        Long rawContactId2 = get(index2).getValues().getAsLong(RawContacts._ID);
        if (rawContactId2 != null && rawContactId2 >= 0) {
            builder.withValue(AggregationExceptions.RAW_CONTACT_ID2, rawContactId2);
        } else {
            builder.withValueBackReference(AggregationExceptions.RAW_CONTACT_ID2, backRefs[index2]);
        }
        diff.add(builder.build());
    }
    public long findRawContactId() {
        for (EntityDelta delta : this) {
            final Long rawContactId = delta.getValues().getAsLong(RawContacts._ID);
            if (rawContactId != null && rawContactId >= 0) {
                return rawContactId;
            }
        }
        return -1;
    }
    public Long getRawContactId(int index) {
        if (index >= 0 && index < this.size()) {
            final EntityDelta delta = this.get(index);
            final ValuesDelta values = delta.getValues();
            if (values.isVisible()) {
                return values.getAsLong(RawContacts._ID);
            }
        }
        return null;
    }
    public EntityDelta getByRawContactId(Long rawContactId) {
        final int index = this.indexOfRawContactId(rawContactId);
        return (index == -1) ? null : this.get(index);
    }
    public int indexOfRawContactId(Long rawContactId) {
        if (rawContactId == null) return -1;
        final int size = this.size();
        for (int i = 0; i < size; i++) {
            final Long currentId = getRawContactId(i);
            if (rawContactId.equals(currentId)) {
                return i;
            }
        }
        return -1;
    }
    public ValuesDelta getSuperPrimaryEntry(final String mimeType) {
        ValuesDelta primary = null;
        ValuesDelta randomEntry = null;
        for (EntityDelta delta : this) {
            final ArrayList<ValuesDelta> mimeEntries = delta.getMimeEntries(mimeType);
            if (mimeEntries == null) return null;
            for (ValuesDelta entry : mimeEntries) {
                if (entry.isSuperPrimary()) {
                    return entry;
                } else if (primary == null && entry.isPrimary()) {
                    primary = entry;
                } else if (randomEntry == null) {
                    randomEntry = entry;
                }
            }
        }
        if (primary != null) {
            return primary;
        }
        return randomEntry;
    }
    public void splitRawContacts() {
        mSplitRawContacts = true;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        final int size = this.size();
        dest.writeInt(size);
        for (EntityDelta delta : this) {
            dest.writeParcelable(delta, flags);
        }
    }
    public void readFromParcel(Parcel source) {
        final ClassLoader loader = getClass().getClassLoader();
        final int size = source.readInt();
        for (int i = 0; i < size; i++) {
            this.add(source.<EntityDelta> readParcelable(loader));
        }
    }
    public static final Parcelable.Creator<EntitySet> CREATOR = new Parcelable.Creator<EntitySet>() {
        public EntitySet createFromParcel(Parcel in) {
            final EntitySet state = new EntitySet();
            state.readFromParcel(in);
            return state;
        }
        public EntitySet[] newArray(int size) {
            return new EntitySet[size];
        }
    };
}
