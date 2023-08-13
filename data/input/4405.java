public class HttpTimestamper implements Timestamper {
    private static final int CONNECT_TIMEOUT = 15000; 
    private static final String TS_QUERY_MIME_TYPE =
        "application/timestamp-query";
    private static final String TS_REPLY_MIME_TYPE =
        "application/timestamp-reply";
    private static final boolean DEBUG = false;
    private String tsaUrl = null;
    public HttpTimestamper(String tsaUrl) {
        this.tsaUrl = tsaUrl;
    }
    public TSResponse generateTimestamp(TSRequest tsQuery) throws IOException {
        HttpURLConnection connection =
            (HttpURLConnection) new URL(tsaUrl).openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(false); 
        connection.setRequestProperty("Content-Type", TS_QUERY_MIME_TYPE);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        if (DEBUG) {
            Set headers = connection.getRequestProperties().entrySet();
            System.out.println(connection.getRequestMethod() + " " + tsaUrl +
                " HTTP/1.1");
            for (Iterator i = headers.iterator(); i.hasNext(); ) {
                System.out.println("  " + i.next());
            }
            System.out.println();
        }
        connection.connect(); 
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(connection.getOutputStream());
            byte[] request = tsQuery.encode();
            output.write(request, 0, request.length);
            output.flush();
            if (DEBUG) {
                System.out.println("sent timestamp query (length=" +
                        request.length + ")");
            }
        } finally {
            if (output != null) {
                output.close();
            }
        }
        BufferedInputStream input = null;
        byte[] replyBuffer = null;
        try {
            input = new BufferedInputStream(connection.getInputStream());
            if (DEBUG) {
                String header = connection.getHeaderField(0);
                System.out.println(header);
                int i = 1;
                while ((header = connection.getHeaderField(i)) != null) {
                    String key = connection.getHeaderFieldKey(i);
                    System.out.println("  " + ((key==null) ? "" : key + ": ") +
                        header);
                    i++;
                }
                System.out.println();
            }
            verifyMimeType(connection.getContentType());
            int total = 0;
            int contentLength = connection.getContentLength();
            replyBuffer = IOUtils.readFully(input, contentLength, false);
            if (DEBUG) {
                System.out.println("received timestamp response (length=" +
                        total + ")");
            }
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return new TSResponse(replyBuffer);
    }
    private static void verifyMimeType(String contentType) throws IOException {
        if (! TS_REPLY_MIME_TYPE.equalsIgnoreCase(contentType)) {
            throw new IOException("MIME Content-Type is not " +
                TS_REPLY_MIME_TYPE);
        }
    }
}
