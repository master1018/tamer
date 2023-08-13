public final class BugReportParser {
    private static final int BUFFER_SIZE = 8*1024;
    private static final String SECTION_HEADER = "------";
    private static final int MAX_LINES = 1000; 
    private BugReportParser() {}
    public static String extractSystemLogs(InputStream in, String section) throws IOException {
        final String sectionWithHeader = SECTION_HEADER + " " + section;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), BUFFER_SIZE);
        boolean inSection = false;
        int numLines = 0;
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (inSection) {
                if (line.startsWith(SECTION_HEADER) || (numLines > MAX_LINES)) {
                    break;
                }
                sb.append(line);
                sb.append("\n");
                ++numLines;
            } else if (line.startsWith(sectionWithHeader)) {
                sb.append(line);
                sb.append("\n");
                inSection = true;
            }
        }
        return sb.toString();
    }
}
