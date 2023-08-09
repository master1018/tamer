public final class Scheme {
    private final String name;
    private final SocketFactory socketFactory;
    private final int defaultPort;
    private final boolean layered;
    private String stringRep;
    public Scheme(final String name,
                  final SocketFactory factory,
                  final int port) {
        if (name == null) {
            throw new IllegalArgumentException
                ("Scheme name may not be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException
                ("Socket factory may not be null");
        }
        if ((port <= 0) || (port > 0xffff)) {
            throw new IllegalArgumentException
                ("Port is invalid: " + port);
        }
        this.name = name.toLowerCase(Locale.ENGLISH);
        this.socketFactory = factory;
        this.defaultPort = port;
        this.layered = (factory instanceof LayeredSocketFactory);
    }
    public final int getDefaultPort() {
        return defaultPort;
    }
    public final SocketFactory getSocketFactory() {
        return socketFactory;
    }
    public final String getName() {
        return name;
    }
    public final boolean isLayered() {
        return layered;
    }
    public final int resolvePort(int port) {
        return ((port <= 0) || (port > 0xffff)) ? defaultPort : port;
    }
    @Override
    public final String toString() {
        if (stringRep == null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.name);
            buffer.append(':');
            buffer.append(Integer.toString(this.defaultPort));
            stringRep = buffer.toString();
        }
        return stringRep;
    }
    @Override
    public final boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (!(obj instanceof Scheme)) return false;
        Scheme s = (Scheme) obj;
        return (name.equals(s.name) &&
                defaultPort == s.defaultPort &&
                layered == s.layered &&
                socketFactory.equals(s.socketFactory)
                );
    } 
    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.defaultPort);
        hash = LangUtils.hashCode(hash, this.name);
        hash = LangUtils.hashCode(hash, this.layered);
        hash = LangUtils.hashCode(hash, this.socketFactory);
        return hash;
    }
} 
