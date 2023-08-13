public final class java_net_URI extends AbstractTest<URI> {
    public static void main(String[] args) {
        new java_net_URI().test(true);
    }
    protected URI getObject() {
        try {
            return new URI("http:
        } catch (URISyntaxException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    protected URI getAnotherObject() {
        try {
            return new URI("ftp:
        } catch (URISyntaxException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}
