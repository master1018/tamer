public class RoutedRequest {
    protected final RequestWrapper request;
    protected final HttpRoute route;
    public RoutedRequest(final RequestWrapper req, final HttpRoute route) {
        super();
        this.request = req;
        this.route   = route;
    }
    public final RequestWrapper getRequest() {
        return request;
    }
    public final HttpRoute getRoute() {
        return route;
    }
} 
