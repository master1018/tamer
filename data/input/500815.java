public class SimpleWikiHelper {
    private static final String TAG = "SimpleWikiHelper";
    public static final String WORD_OF_DAY_REGEX =
            "(?s)\\{\\{wotd\\|(.+?)\\|(.+?)\\|([^#\\|]+).*?\\}\\}";
    private static final String WIKTIONARY_PAGE =
            "http:
            "rvprop=content&format=json%s";
    private static final String WIKTIONARY_EXPAND_TEMPLATES =
            "&rvexpandtemplates=true";
    private static final int HTTP_STATUS_OK = 200;
    private static byte[] sBuffer = new byte[512];
    private static String sUserAgent = null;
    public static class ApiException extends Exception {
        public ApiException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
        public ApiException(String detailMessage) {
            super(detailMessage);
        }
    }
    public static class ParseException extends Exception {
        public ParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }
    public static void prepareUserAgent(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sUserAgent = String.format(context.getString(R.string.template_user_agent),
                    info.packageName, info.versionName);
        } catch(NameNotFoundException e) {
            Log.e(TAG, "Couldn't find package information in PackageManager", e);
        }
    }
    public static String getPageContent(String title, boolean expandTemplates)
            throws ApiException, ParseException {
        String encodedTitle = Uri.encode(title);
        String expandClause = expandTemplates ? WIKTIONARY_EXPAND_TEMPLATES : "";
        String content = getUrlContent(String.format(WIKTIONARY_PAGE,
                encodedTitle, expandClause));
        try {
            JSONObject response = new JSONObject(content);
            JSONObject query = response.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");
            JSONObject page = pages.getJSONObject((String) pages.keys().next());
            JSONArray revisions = page.getJSONArray("revisions");
            JSONObject revision = revisions.getJSONObject(0);
            return revision.getString("*");
        } catch (JSONException e) {
            throw new ParseException("Problem parsing API response", e);
        }
    }
    protected static synchronized String getUrlContent(String url) throws ApiException {
        if (sUserAgent == null) {
            throw new ApiException("User-Agent string must be prepared");
        }
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", sUserAgent);
        try {
            HttpResponse response = client.execute(request);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
                throw new ApiException("Invalid response from server: " +
                        status.toString());
            }
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            return new String(content.toByteArray());
        } catch (IOException e) {
            throw new ApiException("Problem communicating with API", e);
        }
    }
}
