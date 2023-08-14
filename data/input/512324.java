public class MeetingResponseParser extends Parser {
    private EasSyncService mService;
    public MeetingResponseParser(InputStream in, EasSyncService service) throws IOException {
        super(in);
        mService = service;
    }
    public void parseResult() throws IOException {
        while (nextTag(Tags.MREQ_RESULT) != END) {
            if (tag == Tags.MREQ_STATUS) {
                int status = getValueInt();
                if (status != 1) {
                    mService.userLog("Error in meeting response: " + status);
                }
            } else if (tag == Tags.MREQ_CAL_ID) {
                mService.userLog("Meeting response calendar id: " + getValue());
            } else {
                skipTag();
            }
        }
    }
    @Override
    public boolean parse() throws IOException {
        boolean res = false;
        if (nextTag(START_DOCUMENT) != Tags.MREQ_MEETING_RESPONSE) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == Tags.MREQ_RESULT) {
                parseResult();
            } else {
                skipTag();
            }
        }
        return res;
    }
}
