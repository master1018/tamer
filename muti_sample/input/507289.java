public class VoiceContact {
    private static final String TAG = "VoiceContact";
    public static final long ID_UNDEFINED = -1;
    public final String mName;
    public final long mContactId;
    public final long mPrimaryId;
    public final long mHomeId;
    public final long mMobileId;
    public final long mWorkId;
    public final long mOtherId;
    private VoiceContact(String name, long contactId, long primaryId,
            long homeId, long mobileId, long workId,long otherId) {
        mName = name;
        mContactId = contactId;
        mPrimaryId = primaryId;
        mHomeId = homeId;
        mMobileId = mobileId;
        mWorkId = workId;
        mOtherId = otherId;
    }
    @Override
    public int hashCode() {
        final int LARGE_PRIME = 1610612741;
        int hash = 0;
        hash = LARGE_PRIME * (hash + (int)mContactId);
        hash = LARGE_PRIME * (hash + (int)mPrimaryId);
        hash = LARGE_PRIME * (hash + (int)mHomeId);
        hash = LARGE_PRIME * (hash + (int)mMobileId);
        hash = LARGE_PRIME * (hash + (int)mWorkId);
        hash = LARGE_PRIME * (hash + (int)mOtherId);
        return mName.hashCode() + hash;
    }
    @Override
    public String toString() {
        return "mName=" + mName
                + " mPersonId=" + mContactId
                + " mPrimaryId=" + mPrimaryId
                + " mHomeId=" + mHomeId
                + " mMobileId=" + mMobileId
                + " mWorkId=" + mWorkId
                + " mOtherId=" + mOtherId;
    }
    public static List<VoiceContact> getVoiceContacts(Activity activity) {
        if (Config.LOGD) Log.d(TAG, "VoiceContact.getVoiceContacts");
        List<VoiceContact> contacts = new ArrayList<VoiceContact>();
        String[] phonesProjection = new String[] {
            Phone._ID,
            Phone.TYPE,
            Phone.IS_PRIMARY,
            Phone.LABEL,
            Phone.DISPLAY_NAME,
            Phone.CONTACT_ID,
        };
        Cursor cursor = activity.getContentResolver().query(
                Phone.CONTENT_URI, phonesProjection,
                Phone.NUMBER + " NOT NULL", null,
                Phone.LAST_TIME_CONTACTED + " DESC, " + Phone.DISPLAY_NAME + " ASC");
        final int phoneIdColumn = cursor.getColumnIndexOrThrow(Phone._ID);
        final int typeColumn = cursor.getColumnIndexOrThrow(Phone.TYPE);
        final int isPrimaryColumn = cursor.getColumnIndexOrThrow(Phone.IS_PRIMARY);
        final int labelColumn = cursor.getColumnIndexOrThrow(Phone.LABEL);
        final int nameColumn = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME);
        final int personIdColumn = cursor.getColumnIndexOrThrow(Phone.CONTACT_ID);
        String name = null;
        long personId = ID_UNDEFINED;
        long primaryId = ID_UNDEFINED;
        long homeId = ID_UNDEFINED;
        long mobileId = ID_UNDEFINED;
        long workId = ID_UNDEFINED;
        long otherId = ID_UNDEFINED;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            long phoneIdAtCursor = cursor.getLong(phoneIdColumn);
            int typeAtCursor = cursor.getInt(typeColumn);
            long isPrimaryAtCursor = cursor.getLong(isPrimaryColumn);
            String labelAtCursor = cursor.getString(labelColumn);
            String nameAtCursor = cursor.getString(nameColumn);
            long personIdAtCursor = cursor.getLong(personIdColumn);
            if (name != null && !name.equals(nameAtCursor)) {
                contacts.add(new VoiceContact(name, personId, primaryId,
                        homeId, mobileId, workId, otherId));
                name = null;
            }
            if (name == null) {
                name = nameAtCursor;
                personId = personIdAtCursor;
                primaryId = ID_UNDEFINED;
                homeId = ID_UNDEFINED;
                mobileId = ID_UNDEFINED;
                workId = ID_UNDEFINED;
                otherId = ID_UNDEFINED;
            }
            if (typeAtCursor == Phone.TYPE_CUSTOM &&
                    labelAtCursor != null) {
                String label = labelAtCursor.toLowerCase();
                if (label.contains("home") || label.contains("house")) {
                    typeAtCursor = Phone.TYPE_HOME;
                }
                else if (label.contains("mobile") || label.contains("cell")) {
                    typeAtCursor = Phone.TYPE_MOBILE;
                }
                else if (label.contains("work") || label.contains("office")) {
                    typeAtCursor = Phone.TYPE_WORK;
                }
                else if (label.contains("other")) {
                    typeAtCursor = Phone.TYPE_OTHER;
                }
            }
            switch (typeAtCursor) {
                case Phone.TYPE_HOME:
                    homeId = phoneIdAtCursor;
                    if (isPrimaryAtCursor != 0) {
                        primaryId = phoneIdAtCursor;
                    }
                    break;
                case Phone.TYPE_MOBILE:
                    mobileId = phoneIdAtCursor;
                    if (isPrimaryAtCursor != 0) {
                        primaryId = phoneIdAtCursor;
                    }
                    break;
                case Phone.TYPE_WORK:
                    workId = phoneIdAtCursor;
                    if (isPrimaryAtCursor != 0) {
                        primaryId = phoneIdAtCursor;
                    }
                    break;
                case Phone.TYPE_OTHER:
                    otherId = phoneIdAtCursor;
                    if (isPrimaryAtCursor != 0) {
                        primaryId = phoneIdAtCursor;
                    }
                    break;
            }
        }
        if (name != null) {
            contacts.add(new VoiceContact(name, personId, primaryId,
                            homeId, mobileId, workId, otherId));
        }
        cursor.close();
        if (Config.LOGD) Log.d(TAG, "VoiceContact.getVoiceContacts " + contacts.size());
        return contacts;
    }
    public static List<VoiceContact> getVoiceContactsFromFile(File contactsFile) {
        if (Config.LOGD) Log.d(TAG, "getVoiceContactsFromFile " + contactsFile);
        List<VoiceContact> contacts = new ArrayList<VoiceContact>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(contactsFile), 8192);
            String name;
            for (int id = 1; (name = br.readLine()) != null; id++) {
                contacts.add(new VoiceContact(name, id, ID_UNDEFINED,
                        ID_UNDEFINED, ID_UNDEFINED, ID_UNDEFINED, ID_UNDEFINED));
            }
        }
        catch (IOException e) {
            if (Config.LOGD) Log.d(TAG, "getVoiceContactsFromFile failed " + e);
        }
        finally {
            try {
                br.close();
            } catch (IOException e) {
                if (Config.LOGD) Log.d(TAG, "getVoiceContactsFromFile failed during close " + e);
            }
        }
        if (Config.LOGD) Log.d(TAG, "getVoiceContactsFromFile " + contacts.size());
        return contacts;
    }
    public static String redialNumber(Activity activity) {
        Cursor cursor = activity.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[] { CallLog.Calls.NUMBER },
                CallLog.Calls.TYPE + "=" + CallLog.Calls.OUTGOING_TYPE,
                null,
                CallLog.Calls.DEFAULT_SORT_ORDER + " LIMIT 1");
        String number = null;
        if (cursor.getCount() >= 1) {
            cursor.moveToNext();
            int column = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
            number = cursor.getString(column);
        }
        cursor.close();
        if (Config.LOGD) Log.d(TAG, "redialNumber " + number);
        return number;
    }
}
