public abstract class MultiLineReceiver implements IShellOutputReceiver {
    private boolean mTrimLines = true;
    private String mUnfinishedLine = null;
    private final ArrayList<String> mArray = new ArrayList<String>();
    public void setTrimLine(boolean trim) {
        mTrimLines = trim;
    }
    public final void addOutput(byte[] data, int offset, int length) {
        if (isCancelled() == false) {
            String s = null;
            try {
                s = new String(data, offset, length, "ISO-8859-1"); 
            } catch (UnsupportedEncodingException e) {
                s = new String(data, offset,length);
            }
            if (s != null) {
                if (mUnfinishedLine != null) {
                    s = mUnfinishedLine + s;
                    mUnfinishedLine = null;
                }
                mArray.clear();
                int start = 0;
                do {
                    int index = s.indexOf("\r\n", start); 
                    if (index == -1) {
                        mUnfinishedLine = s.substring(start);
                        break;
                    }
                    String line = s.substring(start, index);
                    if (mTrimLines) {
                        line = line.trim();
                    }
                    mArray.add(line);
                    start = index + 2;
                } while (true);
                if (mArray.size() > 0) {
                    String[] lines = mArray.toArray(new String[mArray.size()]);
                    processNewLines(lines);
                }
            }
        }
    }
    public final void flush() {
        if (mUnfinishedLine != null) {
            processNewLines(new String[] { mUnfinishedLine });
        }
        done();
    }
    public void done() {
    }
    public abstract void processNewLines(String[] lines);
}
