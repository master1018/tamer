public class BluetoothAtPhonebook {
    private static final String TAG = "BtAtPhonebook";
    private static final boolean DBG = false;
    private static final String[] CALLS_PROJECTION = new String[] {
        Calls._ID, Calls.NUMBER
    };
    private static final String[] PHONES_PROJECTION = new String[] {
        Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER, Phone.TYPE
    };
    private static final int MAX_PHONEBOOK_SIZE = 16384;
    private static final String OUTGOING_CALL_WHERE = Calls.TYPE + "=" + Calls.OUTGOING_TYPE;
    private static final String INCOMING_CALL_WHERE = Calls.TYPE + "=" + Calls.INCOMING_TYPE;
    private static final String MISSED_CALL_WHERE = Calls.TYPE + "=" + Calls.MISSED_TYPE;
    private static final String VISIBLE_PHONEBOOK_WHERE = Phone.IN_VISIBLE_GROUP + "=1";
    private class PhonebookResult {
        public Cursor  cursor; 
        public int     numberColumn;
        public int     typeColumn;
        public int     nameColumn;
    };
    private final Context mContext;
    private final BluetoothHandsfree mHandsfree;
    private String mCurrentPhonebook;
    private final HashMap<String, PhonebookResult> mPhonebooks =
            new HashMap<String, PhonebookResult>(4);
    public BluetoothAtPhonebook(Context context, BluetoothHandsfree handsfree) {
        mContext = context;
        mHandsfree = handsfree;
        mPhonebooks.put("DC", new PhonebookResult());  
        mPhonebooks.put("RC", new PhonebookResult());  
        mPhonebooks.put("MC", new PhonebookResult());  
        mPhonebooks.put("ME", new PhonebookResult());  
        mCurrentPhonebook = "ME";  
    }
    public String getLastDialledNumber() {
        String[] projection = {Calls.NUMBER};
        Cursor cursor = mContext.getContentResolver().query(Calls.CONTENT_URI, projection,
                Calls.TYPE + "=" + Calls.OUTGOING_TYPE, null, Calls.DEFAULT_SORT_ORDER +
                " LIMIT 1");
        if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        }
        cursor.moveToNext();
        int column = cursor.getColumnIndexOrThrow(Calls.NUMBER);
        String number = cursor.getString(column);
        cursor.close();
        return number;
    }
    public void register(AtParser parser) {
        parser.register("+CSCS", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                return new AtCommandResult("+CSCS: \"UTF-8\"");
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length < 1) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
                if (((String)args[0]).equals("\"GSM\"") || ((String)args[0]).equals("\"IRA\"") ||
                        ((String)args[0]).equals("\"UTF-8\"") ||
                        ((String)args[0]).equals("\"UTF8\"") ) {
                    return new AtCommandResult(AtCommandResult.OK);
                } else {
                    return mHandsfree.reportCmeError(BluetoothCmeError.OPERATION_NOT_SUPPORTED);
                }
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult( "+CSCS: (\"UTF-8\",\"IRA\",\"GSM\")");
            }
        });
        parser.register("+CPBS", new AtCommandHandler() {
            @Override
            public AtCommandResult handleReadCommand() {
                if ("SM".equals(mCurrentPhonebook)) {
                    return new AtCommandResult("+CPBS: \"SM\",0," + getMaxPhoneBookSize(0));
                }
                PhonebookResult pbr = getPhonebookResult(mCurrentPhonebook, true);
                if (pbr == null) {
                    return mHandsfree.reportCmeError(BluetoothCmeError.OPERATION_NOT_ALLOWED);
                }
                int size = pbr.cursor.getCount();
                return new AtCommandResult("+CPBS: \"" + mCurrentPhonebook + "\"," +
                        size + "," + getMaxPhoneBookSize(size));
            }
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                if (args.length < 1 || !(args[0] instanceof String)) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                }
                String pb = ((String)args[0]).trim();
                while (pb.endsWith("\"")) pb = pb.substring(0, pb.length() - 1);
                while (pb.startsWith("\"")) pb = pb.substring(1, pb.length());
                if (getPhonebookResult(pb, false) == null && !"SM".equals(pb)) {
                    if (DBG) log("Dont know phonebook: '" + pb + "'");
                    return mHandsfree.reportCmeError(BluetoothCmeError.OPERATION_NOT_SUPPORTED);
                }
                mCurrentPhonebook = pb;
                return new AtCommandResult(AtCommandResult.OK);
            }
            @Override
            public AtCommandResult handleTestCommand() {
                return new AtCommandResult("+CPBS: (\"ME\",\"SM\",\"DC\",\"RC\",\"MC\")");
            }
        });
        parser.register("+CPBR", new AtCommandHandler() {
            @Override
            public AtCommandResult handleSetCommand(Object[] args) {
                int index1;
                int index2;
                if (args.length < 1 || !(args[0] instanceof Integer)) {
                    return new AtCommandResult(AtCommandResult.ERROR);
                } else {
                    index1 = (Integer)args[0];
                }
                if (args.length == 1) {
                    index2 = index1;
                } else if (!(args[1] instanceof Integer)) {
                    return mHandsfree.reportCmeError(BluetoothCmeError.TEXT_HAS_INVALID_CHARS);
                } else {
                    index2 = (Integer)args[1];
                }
                if ("SM".equals(mCurrentPhonebook)) {
                    return new AtCommandResult(AtCommandResult.OK);
                }
                PhonebookResult pbr = getPhonebookResult(mCurrentPhonebook, false);
                if (pbr == null) {
                    return mHandsfree.reportCmeError(BluetoothCmeError.OPERATION_NOT_ALLOWED);
                }
                if (pbr.cursor.getCount() == 0 || index1 <= 0 || index2 < index1  ||
                    index2 > pbr.cursor.getCount() || index1 > pbr.cursor.getCount()) {
                    return new AtCommandResult(AtCommandResult.OK);
                }
                AtCommandResult result = new AtCommandResult(AtCommandResult.OK);
                int errorDetected = -1; 
                pbr.cursor.moveToPosition(index1 - 1);
                for (int index = index1; index <= index2; index++) {
                    String number = pbr.cursor.getString(pbr.numberColumn);
                    String name = null;
                    int type = -1;
                    if (pbr.nameColumn == -1) {
                        Cursor c = mContext.getContentResolver().query(
                                Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number),
                                new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE},
                                null, null, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                name = c.getString(0);
                                type = c.getInt(1);
                            }
                            c.close();
                        }
                        if (DBG && name == null) log("Caller ID lookup failed for " + number);
                    } else {
                        name = pbr.cursor.getString(pbr.nameColumn);
                    }
                    if (name == null) name = "";
                    name = name.trim();
                    if (name.length() > 28) name = name.substring(0, 28);
                    if (pbr.typeColumn != -1) {
                        type = pbr.cursor.getInt(pbr.typeColumn);
                        name = name + "/" + getPhoneType(type);
                    }
                    if (number == null) number = "";
                    int regionType = PhoneNumberUtils.toaFromString(number);
                    number = number.trim();
                    number = PhoneNumberUtils.stripSeparators(number);
                    if (number.length() > 30) number = number.substring(0, 30);
                    if (number.equals("-1")) {
                        number = "";
                        name = "unknown";
                    }
                    result.addResponse("+CPBR: " + index + ",\"" + number + "\"," +
                                       regionType + ",\"" + name + "\"");
                    if (!pbr.cursor.moveToNext()) {
                        break;
                    }
                }
                return result;
            }
            @Override
            public AtCommandResult handleTestCommand() {
                int size;
                if ("SM".equals(mCurrentPhonebook)) {
                    size = 0;
                } else {
                    PhonebookResult pbr = getPhonebookResult(mCurrentPhonebook, false);
                    if (pbr == null) {
                        return mHandsfree.reportCmeError(BluetoothCmeError.OPERATION_NOT_ALLOWED);
                    }
                    size = pbr.cursor.getCount();
                }
                if (size == 0) {
                    size = 1;
                }
                return new AtCommandResult("+CPBR: (1-" + size + "),30,30");
            }
        });
    }
    private synchronized PhonebookResult getPhonebookResult(String pb, boolean force) {
        if (pb == null) {
            return null;
        }
        PhonebookResult pbr = mPhonebooks.get(pb);
        if (pbr == null) {
            pbr = new PhonebookResult();
        }
        if (force || pbr.cursor == null) {
            if (!queryPhonebook(pb, pbr)) {
                return null;
            }
        }
        if (pbr.cursor == null) {
            return null;
        }
        return pbr;
    }
    private synchronized boolean queryPhonebook(String pb, PhonebookResult pbr) {
        String where;
        boolean ancillaryPhonebook = true;
        if (pb.equals("ME")) {
            ancillaryPhonebook = false;
            where = VISIBLE_PHONEBOOK_WHERE;
        } else if (pb.equals("DC")) {
            where = OUTGOING_CALL_WHERE;
        } else if (pb.equals("RC")) {
            where = INCOMING_CALL_WHERE;
        } else if (pb.equals("MC")) {
            where = MISSED_CALL_WHERE;
        } else {
            return false;
        }
        if (pbr.cursor != null) {
            pbr.cursor.close();
            pbr.cursor = null;
        }
        if (ancillaryPhonebook) {
            pbr.cursor = mContext.getContentResolver().query(
                    Calls.CONTENT_URI, CALLS_PROJECTION, where, null,
                    Calls.DEFAULT_SORT_ORDER + " LIMIT " + MAX_PHONEBOOK_SIZE);
            pbr.numberColumn = pbr.cursor.getColumnIndexOrThrow(Calls.NUMBER);
            pbr.typeColumn = -1;
            pbr.nameColumn = -1;
        } else {
            Uri uri = Phone.CONTENT_URI.buildUpon()
                    .appendQueryParameter(ContactsContract.REQUESTING_PACKAGE_PARAM_KEY,
                            "com.android.bluetooth")
                    .build();
            pbr.cursor = mContext.getContentResolver().query(uri, PHONES_PROJECTION, where, null,
                    Phone.NUMBER + " LIMIT " + MAX_PHONEBOOK_SIZE);
            pbr.numberColumn = pbr.cursor.getColumnIndex(Phone.NUMBER);
            pbr.typeColumn = pbr.cursor.getColumnIndex(Phone.TYPE);
            pbr.nameColumn = pbr.cursor.getColumnIndex(Phone.DISPLAY_NAME);
        }
        Log.i(TAG, "Refreshed phonebook " + pb + " with " + pbr.cursor.getCount() + " results");
        return true;
    }
    private synchronized int getMaxPhoneBookSize(int currSize) {
        int maxSize = (currSize < 100) ? 100 : currSize;
        maxSize += maxSize / 2;
        return roundUpToPowerOfTwo(maxSize);
    }
    private int roundUpToPowerOfTwo(int x) {
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }
    private static String getPhoneType(int type) {
        switch (type) {
            case Phone.TYPE_HOME:
                return "H";
            case Phone.TYPE_MOBILE:
                return "M";
            case Phone.TYPE_WORK:
                return "W";
            case Phone.TYPE_FAX_HOME:
            case Phone.TYPE_FAX_WORK:
                return "F";
            case Phone.TYPE_OTHER:
            case Phone.TYPE_CUSTOM:
            default:
                return "O";
        }
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
