public class UriPatternMatcher {
    private final Map handlerMap;
    public UriPatternMatcher() {
        super();
        this.handlerMap = new HashMap();
    }
    public void register(final String pattern, final Object handler) {
        if (pattern == null) {
            throw new IllegalArgumentException("URI request pattern may not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("HTTP request handelr may not be null");
        }
        this.handlerMap.put(pattern, handler);
    }
    public void unregister(final String pattern) {
        if (pattern == null) {
            return;
        }
        this.handlerMap.remove(pattern);
    }
    public void setHandlers(final Map map) {
        if (map == null) {
            throw new IllegalArgumentException("Map of handlers may not be null");
        }
        this.handlerMap.clear();
        this.handlerMap.putAll(map);
    }
    public Object lookup(String requestURI) {
        if (requestURI == null) {
            throw new IllegalArgumentException("Request URI may not be null");
        }
        int index = requestURI.indexOf("?");
        if (index != -1) {
            requestURI = requestURI.substring(0, index);
        }
        Object handler = this.handlerMap.get(requestURI);
        if (handler == null) {
            String bestMatch = null;
            for (Iterator it = this.handlerMap.keySet().iterator(); it.hasNext();) {
                String pattern = (String) it.next();
                if (matchUriRequestPattern(pattern, requestURI)) {
                    if (bestMatch == null
                            || (bestMatch.length() < pattern.length())
                            || (bestMatch.length() == pattern.length() && pattern.endsWith("*"))) {
                        handler = this.handlerMap.get(pattern);
                        bestMatch = pattern;
                    }
                }
            }
        }
        return handler;
    }
    protected boolean matchUriRequestPattern(final String pattern, final String requestUri) {
        if (pattern.equals("*")) {
            return true;
        } else {
            return
            (pattern.endsWith("*") && requestUri.startsWith(pattern.substring(0, pattern.length() - 1))) ||
            (pattern.startsWith("*") && requestUri.endsWith(pattern.substring(1, pattern.length())));
        }
    }
}
