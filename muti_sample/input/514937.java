class FetchUrlMimeType extends AsyncTask<ContentValues, String, String> {
    BrowserActivity mActivity;
    ContentValues mValues;
    public FetchUrlMimeType(BrowserActivity activity) {
        mActivity = activity;
    }
    @Override
    public String doInBackground(ContentValues... values) {
        mValues = values[0];
        String uri = mValues.getAsString(Downloads.Impl.COLUMN_URI);
        if (uri == null || uri.length() == 0) {
            return null;
        }
        AndroidHttpClient client = AndroidHttpClient.newInstance(
                mValues.getAsString(Downloads.Impl.COLUMN_USER_AGENT));
        HttpHead request = new HttpHead(uri);
        String cookie = mValues.getAsString(Downloads.Impl.COLUMN_COOKIE_DATA);
        if (cookie != null && cookie.length() > 0) {
            request.addHeader("Cookie", cookie);
        }
        String referer = mValues.getAsString(Downloads.Impl.COLUMN_REFERER);
        if (referer != null && referer.length() > 0) {
            request.addHeader("Referer", referer);
        }
        HttpResponse response;
        String mimeType = null;
        try {
            response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                Header header = response.getFirstHeader("Content-Type");
                if (header != null) {
                    mimeType = header.getValue();
                    final int semicolonIndex = mimeType.indexOf(';');
                    if (semicolonIndex != -1) {
                        mimeType = mimeType.substring(0, semicolonIndex);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            request.abort();
        } catch (IOException ex) {
            request.abort();
        } finally {
            client.close();
        }
        return mimeType;
    }
   @Override
    public void onPostExecute(String mimeType) {
       if (mimeType != null) {
           String url = mValues.getAsString(Downloads.Impl.COLUMN_URI);
           if (mimeType.equalsIgnoreCase("text/plain") ||
                   mimeType.equalsIgnoreCase("application/octet-stream")) {
               String newMimeType =
                       MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                           MimeTypeMap.getFileExtensionFromUrl(url));
               if (newMimeType != null) {
                   mValues.put(Downloads.Impl.COLUMN_MIME_TYPE, newMimeType);
               }
           }
           String filename = URLUtil.guessFileName(url,
                   null, mimeType);
           mValues.put(Downloads.Impl.COLUMN_FILE_NAME_HINT, filename);
       }
       final Uri contentUri =
           mActivity.getContentResolver().insert(Downloads.Impl.CONTENT_URI, mValues);
    }
}
