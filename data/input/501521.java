public class BasicCredentialsProvider implements CredentialsProvider {
    private final HashMap<AuthScope, Credentials> credMap;
    public BasicCredentialsProvider() {
        super();
        this.credMap = new HashMap<AuthScope, Credentials>();
    }
    public synchronized void setCredentials(
            final AuthScope authscope, 
            final Credentials credentials) {
        if (authscope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        credMap.put(authscope, credentials);
    }
    private static Credentials matchCredentials(
            final HashMap<AuthScope, Credentials> map, 
            final AuthScope authscope) {
        Credentials creds = map.get(authscope);
        if (creds == null) {
            int bestMatchFactor  = -1;
            AuthScope bestMatch  = null;
            for (AuthScope current: map.keySet()) {
                int factor = authscope.match(current);
                if (factor > bestMatchFactor) {
                    bestMatchFactor = factor;
                    bestMatch = current;
                }
            }
            if (bestMatch != null) {
                creds = map.get(bestMatch);
            }
        }
        return creds;
    }
    public synchronized Credentials getCredentials(final AuthScope authscope) {
        if (authscope == null) {
            throw new IllegalArgumentException("Authentication scope may not be null");
        }
        return matchCredentials(this.credMap, authscope);
    }
    @Override
    public String toString() {
        return credMap.toString();
    }
    public synchronized void clear() {
        this.credMap.clear();
    }
}
