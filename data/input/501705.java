public class BugReportImporter {
    private final static String TAG_HEADER = "------ EVENT LOG TAGS ------";
    private final static String LOG_HEADER = "------ EVENT LOG ------";
    private final static String HEADER_TAG = "------";
    private String[] mTags;
    private String[] mLog;
    public BugReportImporter(String filePath) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath)));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (TAG_HEADER.equals(line)) {
                    readTags(reader);
                    return;
                }
            }
        } catch (IOException e) {
        }
    }
    public String[] getTags() {
        return mTags;
    }
    public String[] getLog() {
        return mLog;
    }
    private void readTags(BufferedReader reader) throws IOException {
        String line;
        ArrayList<String> content = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (LOG_HEADER.equals(line)) {
                mTags = content.toArray(new String[content.size()]);
                readLog(reader);
                return;
            } else {
                content.add(line);
            }
        }
    }
    private void readLog(BufferedReader reader) throws IOException {
        String line;
        ArrayList<String> content = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(HEADER_TAG) == false) {
                content.add(line);
            } else {
                break;
            }
        }
        mLog = content.toArray(new String[content.size()]);
    }
}
