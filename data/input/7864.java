public abstract class NTLMAuthenticationCallback {
    private static volatile NTLMAuthenticationCallback callback =
            new DefaultNTLMAuthenticationCallback();
    public static void setNTLMAuthenticationCallback(
            NTLMAuthenticationCallback callback) {
        NTLMAuthenticationCallback.callback = callback;
    }
    public static NTLMAuthenticationCallback getNTLMAuthenticationCallback() {
        return callback;
    }
    public abstract boolean isTrustedSite(URL url);
    static class DefaultNTLMAuthenticationCallback extends NTLMAuthenticationCallback {
        @Override
        public boolean isTrustedSite(URL url) { return true; }
    }
}
