public final class CookieOrigin {
    private final String host;
    private final int port;
    private final String path;
    private final boolean secure;
    public CookieOrigin(final String host, int port, final String path, boolean secure) {
        super();
        if (host == null) {
            throw new IllegalArgumentException(
                    "Host of origin may not be null");
        }
        if (host.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "Host of origin may not be blank");
        }
        if (port < 0) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        if (path == null) {
            throw new IllegalArgumentException(
                    "Path of origin may not be null.");
        }
        this.host = host.toLowerCase(Locale.ENGLISH);
        this.port = port;
        if (path.trim().length() != 0) {
            this.path = path;
        } else {
            this.path = "/";
        }
        this.secure = secure;
    }
    public String getHost() {
        return this.host;
    }
    public String getPath() {
        return this.path;
    }
    public int getPort() {
        return this.port;
    }
    public boolean isSecure() {
        return this.secure;
    }
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('[');
        if (this.secure) {
            buffer.append("(secure)");
        }
        buffer.append(this.host);
        buffer.append(':');
        buffer.append(Integer.toString(this.port));
        buffer.append(this.path);
        buffer.append(']');
        return buffer.toString();
    }
}
