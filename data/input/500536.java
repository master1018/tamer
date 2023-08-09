public class SmsAssembler implements SmsListener {
    private static final Pattern sPreamplePattern =
        Pattern.compile("\\AWV(\\d{2})(\\p{Alpha}{2})(\\d{1,3})(\\p{Alpha}{2})?");
    private SmsListener mListener;
    private HashMap<String, RawPtsData> mPtsCache;
    public SmsAssembler() {
        mPtsCache = new HashMap<String, RawPtsData>();
    }
    public void setSmsListener(SmsListener listener) {
        mListener = listener;
    }
    public void onIncomingSms(byte[] data) {
        String preamble = extractPreamble(data);
        if (preamble == null) {
            ImpsLog.logError("Received non PTS SMS");
            return;
        }
        Matcher m = sPreamplePattern.matcher(preamble);
        if (!m.matches()) {
            ImpsLog.logError("Received non PTS SMS");
            return;
        }
        String dd = m.group(4);
        if (dd == null || dd.length() == 0) {
            notifyAssembledSms(data);
        } else {
            int totalSegmentsCount = dd.charAt(1) - 'a' + 1;
            int index = dd.charAt(0) - 'a';
            if (index < 0 || index >= totalSegmentsCount) {
                ImpsLog.logError("Invalid multiple SMSes identifier");
                return;
            }
            String transId = m.group(3);
            RawPtsData pts = mPtsCache.get(transId);
            if (pts == null) {
                pts = new RawPtsData(preamble.length(), totalSegmentsCount);
                mPtsCache.put(transId, pts);
            }
            pts.setSegment(index, data);
            if (pts.isAllSegmentsReceived()) {
                mPtsCache.remove(transId);
                notifyAssembledSms(pts.assemble());
            }
        }
    }
    private String extractPreamble(byte[] data) {
        int N = data.length;
        int preambleIndex = 0;
        while (data[preambleIndex] != ' ' && preambleIndex < N) {
            preambleIndex++;
        }
        if (preambleIndex >= N) {
            return null;
        }
        try {
            return new String(data, 0, preambleIndex, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    private void notifyAssembledSms(byte[] data) {
        if (mListener != null) {
            mListener.onIncomingSms(data);
        }
    }
    private static class RawPtsData {
        private int mOrigPreambeLen;
        private byte[][] mSegments;
        public RawPtsData(int origPreambleLen, int totalSegments) {
            mOrigPreambeLen = origPreambleLen;
            mSegments = new byte[totalSegments][];
        }
        public void setSegment(int index, byte[] segment) {
            mSegments[index] = segment;
        }
        public boolean isAllSegmentsReceived() {
            for (byte[] segment : mSegments) {
                if (segment == null) {
                    return false;
                }
            }
            return true;
        }
        public byte[] assemble() {
            int len = calculateLength();
            byte[] res = new byte[len];
            int index = 0;
            System.arraycopy(mSegments[0], 0, res, index, mOrigPreambeLen - 2);
            index += mOrigPreambeLen - 2;
            res[index++] = ' ';
            for (byte[] segment : mSegments) {
                int payloadStart = mOrigPreambeLen + 1;
                int payloadLen = segment.length - payloadStart;
                System.arraycopy(segment, payloadStart, res, index, payloadLen);
                index += payloadLen;
            }
            return res;
        }
        private int calculateLength() {
            int preambleLen = mOrigPreambeLen - 2;
            int total = preambleLen + 1;
            for (byte[] segment : mSegments) {
                int segmentPayload = segment.length - (mOrigPreambeLen + 1);
                total += segmentPayload;
            }
            return total;
        }
    }
}
