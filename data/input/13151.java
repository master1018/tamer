public abstract class Negotiator {
    static Negotiator getNegotiator(HttpCallerInfo hci) {
        Class clazz;
        Constructor c;
        try {
            clazz = Class.forName("sun.net.www.protocol.http.spnego.NegotiatorImpl", true, null);
            c = clazz.getConstructor(HttpCallerInfo.class);
        } catch (ClassNotFoundException cnfe) {
            finest(cnfe);
            return null;
        } catch (ReflectiveOperationException roe) {
            throw new AssertionError(roe);
        }
        try {
            return (Negotiator) (c.newInstance(hci));
        } catch (ReflectiveOperationException roe) {
            finest(roe);
            Throwable t = roe.getCause();
            if (t != null && t instanceof Exception)
                finest((Exception)t);
            return null;
        }
    }
    public abstract byte[] firstToken() throws IOException;
    public abstract byte[] nextToken(byte[] in) throws IOException;
    private static void finest(Exception e) {
        PlatformLogger logger = HttpURLConnection.getHttpLogger();
        logger.finest("NegotiateAuthentication: " + e);
    }
}
