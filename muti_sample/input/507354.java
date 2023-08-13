public class DiscourseLogger {
    private final int mBufferSize;
    private String[] mBuffer;
    private int mPos;
    private final StringBuilder mReceivingLine = new StringBuilder(100);
    public DiscourseLogger(int bufferSize) {
        mBufferSize = bufferSize;
        initBuffer();
    }
    private void initBuffer() {
        mBuffer = new String[mBufferSize];
    }
    private void addLine(String s) {
        mBuffer[mPos] = s;
        mPos++;
        if (mPos >= mBufferSize) {
            mPos = 0;
        }
    }
    private void addReceivingLineToBuffer() {
        if (mReceivingLine.length() > 0) {
            addLine(mReceivingLine.toString());
            mReceivingLine.delete(0, Integer.MAX_VALUE);
        }
    }
    public void addReceivedByte(int b) {
        if (0x20 <= b && b <= 0x7e) { 
            mReceivingLine.append((char) b);
        } else if (b == '\n') { 
            addReceivingLineToBuffer();
        } else if (b == '\r') { 
        } else {
            final String hex = "00" + Integer.toHexString(b);
            mReceivingLine.append("\\x" + hex.substring(hex.length() - 2, hex.length()));
        }
    }
    public void addSentCommand(String command) {
        addLine(command);
    }
     String[] getLines() {
        addReceivingLineToBuffer();
        ArrayList<String> list = new ArrayList<String>();
        final int start = mPos;
        int pos = mPos;
        do {
            String s = mBuffer[pos];
            if (s != null) {
                list.add(s);
            }
            pos = (pos + 1) % mBufferSize;
        } while (pos != start);
        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }
    public void logLastDiscourse() {
        String[] lines = getLines();
        if (lines.length == 0) {
            return;
        }
        Log.w(Email.LOG_TAG, "Last network activities:");
        for (String r : getLines()) {
            Log.w(Email.LOG_TAG, r);
        }
        initBuffer();
    }
}
