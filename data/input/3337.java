public class ClosedStreams {
    public static void main(String args[]) throws IOException {
        (new FileInputStream(FileDescriptor.in)).close();
        if (System.inheritedChannel() != null) {
            throw new RuntimeException("inherited channel not null - unexpected!");
        }
    }
}
