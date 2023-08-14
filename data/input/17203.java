public abstract class Authenticator {
    public abstract static class Result {}
    public static class Failure extends Result {
        private int responseCode;
        public Failure (int responseCode) {
            this.responseCode = responseCode;
        }
        public int getResponseCode() {
            return responseCode;
        }
    }
    public static class Success extends Result {
        private HttpPrincipal principal;
        public Success (HttpPrincipal p) {
            principal = p;
        }
        public HttpPrincipal getPrincipal() {
            return principal;
        }
    }
    public static class Retry extends Result {
        private int responseCode;
        public Retry (int responseCode) {
            this.responseCode = responseCode;
        }
        public int getResponseCode() {
            return responseCode;
        }
    }
    public abstract Result authenticate (HttpExchange exch);
}
