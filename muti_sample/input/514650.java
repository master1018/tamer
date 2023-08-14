public class NTUserPrincipal implements Principal {
    private final String username;
    private final String domain;
    private final String ntname;
    public NTUserPrincipal(
            final String domain,
            final String username) {
        super();
        if (username == null) {
            throw new IllegalArgumentException("User name may not be null");
        }
        this.username = username;
        if (domain != null) {
            this.domain = domain.toUpperCase(Locale.ENGLISH);
        } else {
            this.domain = null;
        }
        if (this.domain != null && this.domain.length() > 0) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(this.domain);
            buffer.append('/');
            buffer.append(this.username);
            this.ntname = buffer.toString();
        } else {
            this.ntname = this.username;
        }
    }
    public String getName() {
        return this.ntname;
    }
    public String getDomain() {
        return this.domain;
    }
    public String getUsername() {
        return this.username;
    }
    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.username);
        hash = LangUtils.hashCode(hash, this.domain);
        return hash;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof NTUserPrincipal) {
            NTUserPrincipal that = (NTUserPrincipal) o;
            if (LangUtils.equals(this.username, that.username)
                    && LangUtils.equals(this.domain, that.domain)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return this.ntname;
    }
}
