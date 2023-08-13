public class EventLogImporter {
    private String[] mTags;
    private String[] mLog;
    public EventLogImporter(String filePath) throws FileNotFoundException {
        String top = System.getenv("ANDROID_BUILD_TOP");
        if (top == null) {
            throw new FileNotFoundException();
        }
        final String tagFile = top + "/system/core/logcat/event-log-tags";
        BufferedReader tagReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(tagFile)));
        BufferedReader eventReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath)));
        try {
            readTags(tagReader);
            readLog(eventReader);
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
            content.add(line);
        }
        mTags = content.toArray(new String[content.size()]);
    }
    private void readLog(BufferedReader reader) throws IOException {
        String line;
        ArrayList<String> content = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            content.add(line);
        }
        mLog = content.toArray(new String[content.size()]);
    }
}
