public class EntityModifier {
    private static final String TAG = "EntityModifier";
    public static boolean canInsert(EntityDelta state, DataKind kind) {
        final int visibleCount = state.getMimeEntriesCount(kind.mimeType, true);
        final boolean validTypes = hasValidTypes(state, kind);
        final boolean validOverall = (kind.typeOverallMax == -1)
                || (visibleCount < kind.typeOverallMax);
        return (validTypes && validOverall);
    }
    public static boolean hasValidTypes(EntityDelta state, DataKind kind) {
        if (EntityModifier.hasEditTypes(kind)) {
            return (getValidTypes(state, kind).size() > 0);
        } else {
            return true;
        }
    }
    public static void ensureKindExists(EntityDelta state, ContactsSource source, String mimeType) {
        final DataKind kind = source.getKindForMimetype(mimeType);
        final boolean hasChild = state.getMimeEntriesCount(mimeType, true) > 0;
        if (!hasChild && kind != null) {
            final ValuesDelta child = insertChild(state, kind);
            if (kind.mimeType.equals(Photo.CONTENT_ITEM_TYPE)) {
                child.setFromTemplate(true);
            }
        }
    }
    public static ArrayList<EditType> getValidTypes(EntityDelta state, DataKind kind) {
        return getValidTypes(state, kind, null, true, null);
    }
    public static ArrayList<EditType> getValidTypes(EntityDelta state, DataKind kind,
            EditType forceInclude) {
        return getValidTypes(state, kind, forceInclude, true, null);
    }
    private static ArrayList<EditType> getValidTypes(EntityDelta state, DataKind kind,
            EditType forceInclude, boolean includeSecondary, SparseIntArray typeCount) {
        final ArrayList<EditType> validTypes = Lists.newArrayList();
        if (!hasEditTypes(kind)) return validTypes;
        if (typeCount == null) {
            typeCount = getTypeFrequencies(state, kind);
        }
        final int overallCount = typeCount.get(FREQUENCY_TOTAL);
        for (EditType type : kind.typeList) {
            final boolean validOverall = (kind.typeOverallMax == -1 ? true
                    : overallCount < kind.typeOverallMax);
            final boolean validSpecific = (type.specificMax == -1 ? true : typeCount
                    .get(type.rawValue) < type.specificMax);
            final boolean validSecondary = (includeSecondary ? true : !type.secondary);
            final boolean forcedInclude = type.equals(forceInclude);
            if (forcedInclude || (validOverall && validSpecific && validSecondary)) {
                validTypes.add(type);
            }
        }
        return validTypes;
    }
    private static final int FREQUENCY_TOTAL = Integer.MIN_VALUE;
    private static SparseIntArray getTypeFrequencies(EntityDelta state, DataKind kind) {
        final SparseIntArray typeCount = new SparseIntArray();
        final List<ValuesDelta> mimeEntries = state.getMimeEntries(kind.mimeType);
        if (mimeEntries == null) return typeCount;
        int totalCount = 0;
        for (ValuesDelta entry : mimeEntries) {
            if (!entry.isVisible()) continue;
            totalCount++;
            final EditType type = getCurrentType(entry, kind);
            if (type != null) {
                final int count = typeCount.get(type.rawValue);
                typeCount.put(type.rawValue, count + 1);
            }
        }
        typeCount.put(FREQUENCY_TOTAL, totalCount);
        return typeCount;
    }
    public static boolean hasEditTypes(DataKind kind) {
        return kind.typeList != null && kind.typeList.size() > 0;
    }
    public static EditType getCurrentType(ValuesDelta entry, DataKind kind) {
        final Long rawValue = entry.getAsLong(kind.typeColumn);
        if (rawValue == null) return null;
        return getType(kind, rawValue.intValue());
    }
    public static EditType getCurrentType(ContentValues entry, DataKind kind) {
        if (kind.typeColumn == null) return null;
        final Integer rawValue = entry.getAsInteger(kind.typeColumn);
        if (rawValue == null) return null;
        return getType(kind, rawValue);
    }
    public static EditType getCurrentType(Cursor cursor, DataKind kind) {
        if (kind.typeColumn == null) return null;
        final int index = cursor.getColumnIndex(kind.typeColumn);
        if (index == -1) return null;
        final int rawValue = cursor.getInt(index);
        return getType(kind, rawValue);
    }
    public static EditType getType(DataKind kind, int rawValue) {
        for (EditType type : kind.typeList) {
            if (type.rawValue == rawValue) {
                return type;
            }
        }
        return null;
    }
    public static int getTypePrecedence(DataKind kind, int rawValue) {
        for (int i = 0; i < kind.typeList.size(); i++) {
            final EditType type = kind.typeList.get(i);
            if (type.rawValue == rawValue) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }
    public static EditType getBestValidType(EntityDelta state, DataKind kind,
            boolean includeSecondary, int exactValue) {
        if (kind.typeColumn == null) return null;
        final SparseIntArray typeCount = getTypeFrequencies(state, kind);
        final ArrayList<EditType> validTypes = getValidTypes(state, kind, null, includeSecondary,
                typeCount);
        if (validTypes.size() == 0) return null;
        final EditType lastType = validTypes.get(validTypes.size() - 1);
        Iterator<EditType> iterator = validTypes.iterator();
        while (iterator.hasNext()) {
            final EditType type = iterator.next();
            final int count = typeCount.get(type.rawValue);
            if (exactValue == type.rawValue) {
                return type;
            }
            if (count > 0) {
                iterator.remove();
            }
        }
        if (validTypes.size() > 0) {
            return validTypes.get(0);
        } else {
            return lastType;
        }
    }
    public static ValuesDelta insertChild(EntityDelta state, DataKind kind) {
        EditType bestType = getBestValidType(state, kind, false, Integer.MIN_VALUE);
        if (bestType == null) {
            bestType = getBestValidType(state, kind, true, Integer.MIN_VALUE);
        }
        return insertChild(state, kind, bestType);
    }
    public static ValuesDelta insertChild(EntityDelta state, DataKind kind, EditType type) {
        if (kind == null) return null;
        final ContentValues after = new ContentValues();
        after.put(Data.MIMETYPE, kind.mimeType);
        if (kind.defaultValues != null) {
            after.putAll(kind.defaultValues);
        }
        if (kind.typeColumn != null && type != null) {
            after.put(kind.typeColumn, type.rawValue);
        }
        final ValuesDelta child = ValuesDelta.fromAfter(after);
	state.addEntry(child);
        return child;
    }
    public static void trimEmpty(EntitySet set, Sources sources) {
        for (EntityDelta state : set) {
            final String accountType = state.getValues().getAsString(RawContacts.ACCOUNT_TYPE);
            final ContactsSource source = sources.getInflatedSource(accountType,
                    ContactsSource.LEVEL_MIMETYPES);
            trimEmpty(state, source);
        }
    }
    public static void trimEmpty(EntityDelta state, ContactsSource source) {
        boolean hasValues = false;
        for (DataKind kind : source.getSortedDataKinds()) {
            final String mimeType = kind.mimeType;
            final ArrayList<ValuesDelta> entries = state.getMimeEntries(mimeType);
            if (entries == null) continue;
            for (ValuesDelta entry : entries) {
                final boolean touched = entry.isInsert() || entry.isUpdate();
                if (!touched) {
                    hasValues = true;
                    continue;
                }
                final boolean isGoogleSource = TextUtils.equals(GoogleSource.ACCOUNT_TYPE,
                        state.getValues().getAsString(RawContacts.ACCOUNT_TYPE));
                final boolean isPhoto = TextUtils.equals(Photo.CONTENT_ITEM_TYPE, kind.mimeType);
                final boolean isGooglePhoto = isPhoto && isGoogleSource;
                if (EntityModifier.isEmpty(entry, kind) && !isGooglePhoto) {
                    Log.w(TAG, "Trimming: " + entry.toString());
                    entry.markDeleted();
                } else if (!entry.isFromTemplate()) {
                    hasValues = true;
                }
            }
        }
        if (!hasValues) {
            state.markDeleted();
        }
    }
    public static boolean isEmpty(ValuesDelta values, DataKind kind) {
        if (kind.fieldList == null) return true;
        boolean hasValues = false;
        for (EditField field : kind.fieldList) {
            final String value = values.getAsString(field.column);
            if (ContactsUtils.isGraphic(value)) {
                hasValues = true;
            }
        }
        return !hasValues;
    }
    public static void parseExtras(Context context, ContactsSource source, EntityDelta state,
            Bundle extras) {
        if (extras == null || extras.size() == 0) {
            return;
        }
        {
            EntityModifier.ensureKindExists(state, source, StructuredName.CONTENT_ITEM_TYPE);
            final ValuesDelta child = state.getPrimaryEntry(StructuredName.CONTENT_ITEM_TYPE);
            final String name = extras.getString(Insert.NAME);
            if (ContactsUtils.isGraphic(name)) {
                child.put(StructuredName.GIVEN_NAME, name);
            }
            final String phoneticName = extras.getString(Insert.PHONETIC_NAME);
            if (ContactsUtils.isGraphic(phoneticName)) {
                child.put(StructuredName.PHONETIC_GIVEN_NAME, phoneticName);
            }
        }
        {
            final DataKind kind = source.getKindForMimetype(StructuredPostal.CONTENT_ITEM_TYPE);
            parseExtras(state, kind, extras, Insert.POSTAL_TYPE, Insert.POSTAL,
                    StructuredPostal.STREET);
        }
        {
            final DataKind kind = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
            parseExtras(state, kind, extras, Insert.PHONE_TYPE, Insert.PHONE, Phone.NUMBER);
            parseExtras(state, kind, extras, Insert.SECONDARY_PHONE_TYPE, Insert.SECONDARY_PHONE,
                    Phone.NUMBER);
            parseExtras(state, kind, extras, Insert.TERTIARY_PHONE_TYPE, Insert.TERTIARY_PHONE,
                    Phone.NUMBER);
        }
        {
            final DataKind kind = source.getKindForMimetype(Email.CONTENT_ITEM_TYPE);
            parseExtras(state, kind, extras, Insert.EMAIL_TYPE, Insert.EMAIL, Email.DATA);
            parseExtras(state, kind, extras, Insert.SECONDARY_EMAIL_TYPE, Insert.SECONDARY_EMAIL,
                    Email.DATA);
            parseExtras(state, kind, extras, Insert.TERTIARY_EMAIL_TYPE, Insert.TERTIARY_EMAIL,
                    Email.DATA);
        }
        {
            final DataKind kind = source.getKindForMimetype(Im.CONTENT_ITEM_TYPE);
            fixupLegacyImType(extras);
            parseExtras(state, kind, extras, Insert.IM_PROTOCOL, Insert.IM_HANDLE, Im.DATA);
        }
        final boolean hasOrg = extras.containsKey(Insert.COMPANY)
                || extras.containsKey(Insert.JOB_TITLE);
        final DataKind kindOrg = source.getKindForMimetype(Organization.CONTENT_ITEM_TYPE);
        if (hasOrg && EntityModifier.canInsert(state, kindOrg)) {
            final ValuesDelta child = EntityModifier.insertChild(state, kindOrg);
            final String company = extras.getString(Insert.COMPANY);
            if (ContactsUtils.isGraphic(company)) {
                child.put(Organization.COMPANY, company);
            }
            final String title = extras.getString(Insert.JOB_TITLE);
            if (ContactsUtils.isGraphic(title)) {
                child.put(Organization.TITLE, title);
            }
        }
        final boolean hasNotes = extras.containsKey(Insert.NOTES);
        final DataKind kindNotes = source.getKindForMimetype(Note.CONTENT_ITEM_TYPE);
        if (hasNotes && EntityModifier.canInsert(state, kindNotes)) {
            final ValuesDelta child = EntityModifier.insertChild(state, kindNotes);
            final String notes = extras.getString(Insert.NOTES);
            if (ContactsUtils.isGraphic(notes)) {
                child.put(Note.NOTE, notes);
            }
        }
    }
    private static void fixupLegacyImType(Bundle bundle) {
        final String encodedString = bundle.getString(Insert.IM_PROTOCOL);
        if (encodedString == null) return;
        try {
            final Object protocol = android.provider.Contacts.ContactMethods
                    .decodeImProtocol(encodedString);
            if (protocol instanceof Integer) {
                bundle.putInt(Insert.IM_PROTOCOL, (Integer)protocol);
            } else {
                bundle.putString(Insert.IM_PROTOCOL, (String)protocol);
            }
        } catch (IllegalArgumentException e) {
        }
    }
    public static void parseExtras(EntityDelta state, DataKind kind, Bundle extras,
            String typeExtra, String valueExtra, String valueColumn) {
        final CharSequence value = extras.getCharSequence(valueExtra);
        if (kind == null) return;
        final boolean canInsert = EntityModifier.canInsert(state, kind);
        final boolean validValue = (value != null && TextUtils.isGraphic(value));
        if (!validValue || !canInsert) return;
        final boolean hasType = extras.containsKey(typeExtra);
        final int typeValue = extras.getInt(typeExtra, hasType ? BaseTypes.TYPE_CUSTOM
                : Integer.MIN_VALUE);
        final EditType editType = EntityModifier.getBestValidType(state, kind, true, typeValue);
        final ValuesDelta child = EntityModifier.insertChild(state, kind, editType);
        child.put(valueColumn, value.toString());
        if (editType != null && editType.customColumn != null) {
            final String customType = extras.getString(typeExtra);
            child.put(editType.customColumn, customType);
        }
    }
}
