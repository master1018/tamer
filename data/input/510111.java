public abstract class HttpRequestBase extends AbstractHttpMessage 
    implements HttpUriRequest, AbortableHttpRequest, Cloneable {
    private Lock abortLock;
    private boolean aborted;
    private URI uri;
    private ClientConnectionRequest connRequest;
    private ConnectionReleaseTrigger releaseTrigger;
    public HttpRequestBase() {
        super();
        this.abortLock = new ReentrantLock();
    }
    public abstract String getMethod();
    public ProtocolVersion getProtocolVersion() {
        return HttpProtocolParams.getVersion(getParams());
    }
    public URI getURI() {
        return this.uri;
    }
    public RequestLine getRequestLine() {
        String method = getMethod();
        ProtocolVersion ver = getProtocolVersion();
        URI uri = getURI();
        String uritext = null;
        if (uri != null) {
            uritext = uri.toASCIIString();
        }
        if (uritext == null || uritext.length() == 0) {
            uritext = "/";
        }
        return new BasicRequestLine(method, uritext, ver);
    }
    public void setURI(final URI uri) {
        this.uri = uri;
    }
    public void setConnectionRequest(final ClientConnectionRequest connRequest)
            throws IOException {
        this.abortLock.lock();
        try {
            if (this.aborted) {
                throw new IOException("Request already aborted");
            }
            this.releaseTrigger = null;
            this.connRequest = connRequest;
        } finally {
            this.abortLock.unlock();
        }
    }
    public void setReleaseTrigger(final ConnectionReleaseTrigger releaseTrigger)
            throws IOException {
        this.abortLock.lock();
        try {
            if (this.aborted) {
                throw new IOException("Request already aborted");
            }
            this.connRequest = null;
            this.releaseTrigger = releaseTrigger;
        } finally {
            this.abortLock.unlock();
        }
    }
    public void abort() {
        ClientConnectionRequest localRequest;
        ConnectionReleaseTrigger localTrigger;
        this.abortLock.lock();
        try {
            if (this.aborted) {
                return;
            }            
            this.aborted = true;
            localRequest = connRequest;
            localTrigger = releaseTrigger;
        } finally {
            this.abortLock.unlock();
        }        
        if (localRequest != null) {
            localRequest.abortRequest();
        }
        if (localTrigger != null) {
            try {
                localTrigger.abortConnection();
            } catch (IOException ex) {
            }
        }
    }
    public boolean isAborted() {
        return this.aborted;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        HttpRequestBase clone = (HttpRequestBase) super.clone();
        clone.abortLock = new ReentrantLock();
        clone.aborted = false;
        clone.releaseTrigger = null;
        clone.connRequest = null;
        clone.headergroup = (HeaderGroup) CloneUtils.clone(this.headergroup);
        clone.params = (HttpParams) CloneUtils.clone(this.params);
        return clone;
    }
}
