public class BasicHttpResponse extends AbstractHttpMessage
    implements HttpResponse {
    private StatusLine          statusline;
    private HttpEntity          entity;
    private ReasonPhraseCatalog reasonCatalog;
    private Locale              locale;
    public BasicHttpResponse(final StatusLine statusline,
                             final ReasonPhraseCatalog catalog,
                             final Locale locale) {
        super();
        if (statusline == null) {
            throw new IllegalArgumentException("Status line may not be null.");
        }
        this.statusline    = statusline;
        this.reasonCatalog = catalog;
        this.locale        = (locale != null) ? locale : Locale.getDefault();
    }
    public BasicHttpResponse(final StatusLine statusline) {
        this(statusline, null, null);
    }
    public BasicHttpResponse(final ProtocolVersion ver,
                             final int code,
                             final String reason) {
        this(new BasicStatusLine(ver, code, reason), null, null);
    }
    public ProtocolVersion getProtocolVersion() {
        return this.statusline.getProtocolVersion();
    }
    public StatusLine getStatusLine() {
        return this.statusline; 
    }
    public HttpEntity getEntity() {
        return this.entity;
    }
    public Locale getLocale() {
        return this.locale;
    }
    public void setStatusLine(final StatusLine statusline) {
        if (statusline == null) {
            throw new IllegalArgumentException("Status line may not be null");
        }
        this.statusline = statusline;
    }
    public void setStatusLine(final ProtocolVersion ver, final int code) {
        this.statusline = new BasicStatusLine(ver, code, getReason(code));
    }
    public void setStatusLine(final ProtocolVersion ver, final int code,
                              final String reason) {
        this.statusline = new BasicStatusLine(ver, code, reason);
    }
    public void setStatusCode(int code) {
        ProtocolVersion ver = this.statusline.getProtocolVersion();
        this.statusline = new BasicStatusLine(ver, code, getReason(code));
    }
    public void setReasonPhrase(String reason) {
        if ((reason != null) && ((reason.indexOf('\n') >= 0) ||
                                 (reason.indexOf('\r') >= 0))
            ) {
            throw new IllegalArgumentException("Line break in reason phrase.");
        }
        this.statusline = new BasicStatusLine(this.statusline.getProtocolVersion(),
                                              this.statusline.getStatusCode(),
                                              reason);
    }
    public void setEntity(final HttpEntity entity) {
        this.entity = entity;
    }
    public void setLocale(Locale loc) {
        if (loc == null) {
            throw new IllegalArgumentException("Locale may not be null.");
        }
        this.locale = loc;
        final int code = this.statusline.getStatusCode();
        this.statusline = new BasicStatusLine
            (this.statusline.getProtocolVersion(), code, getReason(code));
    }
    protected String getReason(int code) {
        return (this.reasonCatalog == null) ?
            null : this.reasonCatalog.getReason(code, this.locale);
    }
}
