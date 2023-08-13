public class NullTest {
    public static void main(String args[]) {
        Channel c1, c2;
        try {
            c1 = SelectorProvider.provider().inheritedChannel();
            c2 = System.inheritedChannel();
        } catch (IOException ioe) {
            throw new RuntimeException("Unexpected IOException: " + ioe);
        }
        if (c1 != null || c2 != null) {
            throw new RuntimeException("Channel returned - unexpected");
        }
    }
}
