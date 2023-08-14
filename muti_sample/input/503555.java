public class EventLog {
    private static final String TAG = "EventLog";
    private static final String TAGS_FILE = "/system/etc/event-log-tags";
    private static final String COMMENT_PATTERN = "^\\s*(#.*)?$";
    private static final String TAG_PATTERN = "^\\s*(\\d+)\\s+(\\w+)\\s*(\\(.*\\))?\\s*$";
    private static HashMap<String, Integer> sTagCodes = null;
    private static HashMap<Integer, String> sTagNames = null;
    public static final class Event {
        private final ByteBuffer mBuffer;
        private static final int LENGTH_OFFSET = 0;
        private static final int PROCESS_OFFSET = 4;
        private static final int THREAD_OFFSET = 8;
        private static final int SECONDS_OFFSET = 12;
        private static final int NANOSECONDS_OFFSET = 16;
        private static final int PAYLOAD_START = 20;
        private static final int TAG_OFFSET = 20;
        private static final int DATA_START = 24;
        private static final byte INT_TYPE    = 0;
        private static final byte LONG_TYPE   = 1;
        private static final byte STRING_TYPE = 2;
        private static final byte LIST_TYPE   = 3;
         Event(byte[] data) {
            mBuffer = ByteBuffer.wrap(data);
            mBuffer.order(ByteOrder.nativeOrder());
        }
        public int getProcessId() {
            return mBuffer.getInt(PROCESS_OFFSET);
        }
        public int getThreadId() {
            return mBuffer.getInt(THREAD_OFFSET);
        }
        public long getTimeNanos() {
            return mBuffer.getInt(SECONDS_OFFSET) * 1000000000l
                    + mBuffer.getInt(NANOSECONDS_OFFSET);
        }
        public int getTag() {
            return mBuffer.getInt(TAG_OFFSET);
        }
        public synchronized Object getData() {
            try {
                mBuffer.limit(PAYLOAD_START + mBuffer.getShort(LENGTH_OFFSET));
                mBuffer.position(DATA_START);  
                return decodeObject();
            } catch (IllegalArgumentException e) {
                Log.wtf(TAG, "Illegal entry payload: tag=" + getTag(), e);
                return null;
            } catch (BufferUnderflowException e) {
                Log.wtf(TAG, "Truncated entry payload: tag=" + getTag(), e);
                return null;
            }
        }
        private Object decodeObject() {
            byte type = mBuffer.get();
            switch (type) {
            case INT_TYPE:
                return (Integer) mBuffer.getInt();
            case LONG_TYPE:
                return (Long) mBuffer.getLong();
            case STRING_TYPE:
                try {
                    int length = mBuffer.getInt();
                    int start = mBuffer.position();
                    mBuffer.position(start + length);
                    return new String(mBuffer.array(), start, length, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.wtf(TAG, "UTF-8 is not supported", e);
                    return null;
                }
            case LIST_TYPE:
                int length = mBuffer.get();
                if (length < 0) length += 256;  
                Object[] array = new Object[length];
                for (int i = 0; i < length; ++i) array[i] = decodeObject();
                return array;
            default:
                throw new IllegalArgumentException("Unknown entry type: " + type);
            }
        }
    }
    public static native int writeEvent(int tag, int value);
    public static native int writeEvent(int tag, long value);
    public static native int writeEvent(int tag, String str);
    public static native int writeEvent(int tag, Object... list);
    public static native void readEvents(int[] tags, Collection<Event> output)
            throws IOException;
    public static String getTagName(int tag) {
        readTagsFile();
        return sTagNames.get(tag);
    }
    public static int getTagCode(String name) {
        readTagsFile();
        Integer code = sTagCodes.get(name);
        return code != null ? code : -1;
    }
    private static synchronized void readTagsFile() {
        if (sTagCodes != null && sTagNames != null) return;
        sTagCodes = new HashMap<String, Integer>();
        sTagNames = new HashMap<Integer, String>();
        Pattern comment = Pattern.compile(COMMENT_PATTERN);
        Pattern tag = Pattern.compile(TAG_PATTERN);
        BufferedReader reader = null;
        String line;
        try {
            reader = new BufferedReader(new FileReader(TAGS_FILE), 256);
            while ((line = reader.readLine()) != null) {
                if (comment.matcher(line).matches()) continue;
                Matcher m = tag.matcher(line);
                if (!m.matches()) {
                    Log.wtf(TAG, "Bad entry in " + TAGS_FILE + ": " + line);
                    continue;
                }
                try {
                    int num = Integer.parseInt(m.group(1));
                    String name = m.group(2);
                    sTagCodes.put(name, num);
                    sTagNames.put(num, name);
                } catch (NumberFormatException e) {
                    Log.wtf(TAG, "Error in " + TAGS_FILE + ": " + line, e);
                }
            }
        } catch (IOException e) {
            Log.wtf(TAG, "Error reading " + TAGS_FILE, e);
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException e) {}
        }
    }
}
