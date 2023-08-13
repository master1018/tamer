public abstract class AbstractCallback implements HttpCallback {
    Map requests;
    static class Request {
        URI uri;
        int count;
        Request (URI u) {
            uri = u;
            count = 0;
        }
    }
    AbstractCallback () {
        requests = Collections.synchronizedMap (new HashMap());
    }
    public void request (HttpTransaction msg) {
        URI uri = msg.getRequestURI();
        Request req = (Request) requests.get (uri);
        if (req == null) {
            req = new Request (uri);
            requests.put (uri, req);
        }
        request (msg, req.count++);
    }
    abstract public void request (HttpTransaction msg, int n);
}
