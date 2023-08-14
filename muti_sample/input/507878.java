public class ConnConnectionParamBean extends HttpAbstractParamBean {
    public ConnConnectionParamBean (final HttpParams params) {
        super(params);
    }
    public void setMaxStatusLineGarbage (final int maxStatusLineGarbage) {
        params.setIntParameter(ConnConnectionPNames.MAX_STATUS_LINE_GARBAGE, maxStatusLineGarbage);
    }
}
