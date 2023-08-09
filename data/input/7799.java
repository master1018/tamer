public class RelativeEncoding {
    public static void main(String[] args) {
        try {
            URI one = new URI("Relative%20with%20spaces");
            URI two = (new File("/tmp/dir with spaces/File with spaces")).toURI();
            URI three = two.resolve(one);
            if (!three.getSchemeSpecificPart().equals(three.getPath()))
                throw new RuntimeException("Bad encoding on URI.resolve");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unexpected exception: " + e);
        }
    }
}
