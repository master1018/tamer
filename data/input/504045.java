public class CallerInfo {
    private static final String TAG = "CallerInfo";
    public static final String UNKNOWN_NUMBER = "-1";
    public static final String PRIVATE_NUMBER = "-2";
    public static final String PAYPHONE_NUMBER = "-3";
    public String name;
    public String phoneNumber;
    public String cnapName;
    public int numberPresentation;
    public int namePresentation;
    public boolean contactExists;
    public String phoneLabel;
    public int    numberType;
    public String numberLabel;
    public int photoResource;
    public long person_id;
    public boolean needUpdate;
    public Uri contactRefUri;
    public Uri contactRingtoneUri;
    public boolean shouldSendToVoicemail;
    public Drawable cachedPhoto;
    public boolean isCachedPhotoCurrent;
    private boolean mIsEmergency;
    private boolean mIsVoiceMail;
    public CallerInfo() {
        mIsEmergency = false;
        mIsVoiceMail = false;
    }
    public static CallerInfo getCallerInfo(Context context, Uri contactRef, Cursor cursor) {
        CallerInfo info = new CallerInfo();
        info.photoResource = 0;
        info.phoneLabel = null;
        info.numberType = 0;
        info.numberLabel = null;
        info.cachedPhoto = null;
        info.isCachedPhotoCurrent = false;
        info.contactExists = false;
        if (Config.LOGV) Log.v(TAG, "construct callerInfo from cursor");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex;
                columnIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
                if (columnIndex != -1) {
                    info.name = cursor.getString(columnIndex);
                }
                columnIndex = cursor.getColumnIndex(PhoneLookup.NUMBER);
                if (columnIndex != -1) {
                    info.phoneNumber = cursor.getString(columnIndex);
                }
                columnIndex = cursor.getColumnIndex(PhoneLookup.LABEL);
                if (columnIndex != -1) {
                    int typeColumnIndex = cursor.getColumnIndex(PhoneLookup.TYPE);
                    if (typeColumnIndex != -1) {
                        info.numberType = cursor.getInt(typeColumnIndex);
                        info.numberLabel = cursor.getString(columnIndex);
                        info.phoneLabel = Phone.getDisplayLabel(context,
                                info.numberType, info.numberLabel)
                                .toString();
                    }
                }
                final String mimeType = context.getContentResolver().getType(contactRef);
                columnIndex = -1;
                if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                    columnIndex = cursor.getColumnIndex(RawContacts.CONTACT_ID);
                } else {
                    columnIndex = cursor.getColumnIndex(PhoneLookup._ID);
                }
                if (columnIndex != -1) {
                    info.person_id = cursor.getLong(columnIndex);
                } else {
                    Log.e(TAG, "Column missing for " + contactRef);
                }
                columnIndex = cursor.getColumnIndex(PhoneLookup.CUSTOM_RINGTONE);
                if ((columnIndex != -1) && (cursor.getString(columnIndex) != null)) {
                    info.contactRingtoneUri = Uri.parse(cursor.getString(columnIndex));
                } else {
                    info.contactRingtoneUri = null;
                }
                columnIndex = cursor.getColumnIndex(PhoneLookup.SEND_TO_VOICEMAIL);
                info.shouldSendToVoicemail = (columnIndex != -1) &&
                        ((cursor.getInt(columnIndex)) == 1);
                info.contactExists = true;
            }
            cursor.close();
        }
        info.needUpdate = false;
        info.name = normalize(info.name);
        info.contactRefUri = contactRef;
        return info;
    }
    public static CallerInfo getCallerInfo(Context context, Uri contactRef) {
        return getCallerInfo(context, contactRef,
                context.getContentResolver().query(contactRef, null, null, null, null));
    }
    public static CallerInfo getCallerInfo(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        if (PhoneNumberUtils.isEmergencyNumber(number)) {
            return new CallerInfo().markAsEmergency(context);
        } else if (PhoneNumberUtils.isVoiceMailNumber(number)) {
            return new CallerInfo().markAsVoiceMail();
        }
        Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        CallerInfo info = getCallerInfo(context, contactUri);
        if (TextUtils.isEmpty(info.phoneNumber)) {
            info.phoneNumber = number;
        }
        return info;
    }
    public static String getCallerId(Context context, String number) {
        CallerInfo info = getCallerInfo(context, number);
        String callerID = null;
        if (info != null) {
            String name = info.name;
            if (!TextUtils.isEmpty(name)) {
                callerID = name;
            } else {
                callerID = number;
            }
        }
        return callerID;
    }
    public boolean isEmergencyNumber() {
        return mIsEmergency;
    }
    public boolean isVoiceMailNumber() {
        return mIsVoiceMail;
    }
     CallerInfo markAsEmergency(Context context) {
        phoneNumber = context.getString(
            com.android.internal.R.string.emergency_call_dialog_number_for_display);
        photoResource = com.android.internal.R.drawable.picture_emergency;
        mIsEmergency = true;
        return this;
    }
     CallerInfo markAsVoiceMail() {
        mIsVoiceMail = true;
        try {
            String voiceMailLabel = TelephonyManager.getDefault().getVoiceMailAlphaTag();
            phoneNumber = voiceMailLabel;
        } catch (SecurityException se) {
            Log.e(TAG, "Cannot access VoiceMail.", se);
        }
        return this;
    }
    private static String normalize(String s) {
        if (s == null || s.length() > 0) {
            return s;
        } else {
            return null;
        }
    }
    public String toString() {
        return new StringBuilder(384)
                .append("\nname: " + name)
                .append("\nphoneNumber: " + phoneNumber)
                .append("\ncnapName: " + cnapName)
                .append("\nnumberPresentation: " + numberPresentation)
                .append("\nnamePresentation: " + namePresentation)
                .append("\ncontactExits: " + contactExists)
                .append("\nphoneLabel: " + phoneLabel)
                .append("\nnumberType: " + numberType)
                .append("\nnumberLabel: " + numberLabel)
                .append("\nphotoResource: " + photoResource)
                .append("\nperson_id: " + person_id)
                .append("\nneedUpdate: " + needUpdate)
                .append("\ncontactRefUri: " + contactRefUri)
                .append("\ncontactRingtoneUri: " + contactRefUri)
                .append("\nshouldSendToVoicemail: " + shouldSendToVoicemail)
                .append("\ncachedPhoto: " + cachedPhoto)
                .append("\nisCachedPhotoCurrent: " + isCachedPhotoCurrent)
                .append("\nemergency: " + mIsEmergency)
                .append("\nvoicemail " + mIsVoiceMail)
                .toString();
    }
}
