public class BasicResponseHandler implements ResponseHandler<String> {
    public String handleResponse(final HttpResponse response)
            throws HttpResponseException, IOException {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
            throw new HttpResponseException(statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        HttpEntity entity = response.getEntity();
        return entity == null ? null : EntityUtils.toString(entity);
    }
}
