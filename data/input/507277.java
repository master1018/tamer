public final class GDataClient {
    private static final String TAG = "GDataClient";
    private static final String USER_AGENT = "Cooliris-GData/1.0; gzip";
    private static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
    private static final String IF_MATCH = "If-Match";
    private static final int CONNECTION_TIMEOUT = 20000; 
    private static final int MIN_GZIP_SIZE = 512;
    public static final HttpParams HTTP_PARAMS;
    public static final ThreadSafeClientConnManager HTTP_CONNECTION_MANAGER;
    private final DefaultHttpClient mHttpClient = new DefaultHttpClient(HTTP_CONNECTION_MANAGER, HTTP_PARAMS);
    private String mAuthToken;
    static {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setStaleCheckingEnabled(params, false);
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
        HttpClientParams.setRedirecting(params, true);
        HttpProtocolParams.setUserAgent(params, USER_AGENT);
        HTTP_PARAMS = params;
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        HTTP_CONNECTION_MANAGER = new ThreadSafeClientConnManager(params, schemeRegistry);
    }
    public static final class Operation {
        public String inOutEtag;
        public int outStatus;
        public InputStream outBody;
    }
    public void setAuthToken(String authToken) {
        mAuthToken = authToken;
    }
    public void get(String feedUrl, Operation operation) throws IOException {
        callMethod(new HttpGet(feedUrl), operation);
    }
    public void post(String feedUrl, byte[] data, String contentType, Operation operation) throws IOException {
        ByteArrayEntity entity = getCompressedEntity(data);
        entity.setContentType(contentType);
        HttpPost post = new HttpPost(feedUrl);
        post.setEntity(entity);
        callMethod(post, operation);
    }
    public void put(String feedUrl, byte[] data, String contentType, Operation operation) throws IOException {
        ByteArrayEntity entity = getCompressedEntity(data);
        entity.setContentType(contentType);
        HttpPost post = new HttpPost(feedUrl);
        post.setHeader(X_HTTP_METHOD_OVERRIDE, "PUT");
        post.setEntity(entity);
        callMethod(post, operation);
    }
    public void putStream(String feedUrl, InputStream stream, String contentType, Operation operation) throws IOException {
        InputStreamEntity entity = new InputStreamEntity(stream, -1);
        entity.setContentType(contentType);
        HttpPost post = new HttpPost(feedUrl);
        post.setHeader(X_HTTP_METHOD_OVERRIDE, "PUT");
        post.setEntity(entity);
        callMethod(post, operation);
    }
    public void delete(String feedUrl, Operation operation) throws IOException {
        HttpPost post = new HttpPost(feedUrl);
        String etag = operation.inOutEtag;
        post.setHeader(X_HTTP_METHOD_OVERRIDE, "DELETE");
        post.setHeader(IF_MATCH, etag != null ? etag : "*");
        callMethod(post, operation);
    }
    private void callMethod(HttpUriRequest request, Operation operation) throws IOException {
        request.addHeader("GData-Version", "2");
        request.addHeader("Accept-Encoding", "gzip");
        String authToken = mAuthToken;
        if (!TextUtils.isEmpty(authToken)) {
            request.addHeader("Authorization", "GoogleLogin auth=" + authToken);
        }
        String etag = operation.inOutEtag;
        if (etag != null) {
            request.addHeader("If-None-Match", etag);
        }
        HttpResponse httpResponse = null;
        try {
            httpResponse = mHttpClient.execute(request);
        } catch (IOException e) {
            Log.w(TAG, "Request failed: " + request.getURI());
            throw e;
        }
        int status = httpResponse.getStatusLine().getStatusCode();
        InputStream stream = null;
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            stream = entity.getContent();
            if (stream != null) {
                Header header = entity.getContentEncoding();
                if (header != null) {
                    if (header.getValue().contains("gzip")) {
                        stream = new GZIPInputStream(stream);
                    }
                }
            }
        }
        Header etagHeader = httpResponse.getFirstHeader("ETag");
        operation.outStatus = status;
        operation.inOutEtag = etagHeader != null ? etagHeader.getValue() : null;
        operation.outBody = stream;
    }
    private ByteArrayEntity getCompressedEntity(byte[] data) throws IOException {
        ByteArrayEntity entity;
        if (data.length >= MIN_GZIP_SIZE) {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream(data.length / 2);
            GZIPOutputStream gzipOutput = new GZIPOutputStream(byteOutput);
            gzipOutput.write(data);
            gzipOutput.close();
            entity = new ByteArrayEntity(byteOutput.toByteArray());
        } else {
            entity = new ByteArrayEntity(data);
        }
        return entity;
    }
    public static String inputStreamToString(InputStream stream) {
        if (stream != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    bytes.write(buffer, 0, bytesRead);
                }
                return new String(bytes.toString());
            } catch (IOException e) {
            }
        }
        return null;
    }
}
