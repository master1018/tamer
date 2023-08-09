public class DeleteOnClose {
    public static void main(String[] args) throws IOException {
        Files.newByteChannel(Paths.get(args[0]), READ, WRITE, DELETE_ON_CLOSE);
        Path file = Files.createTempFile("blah", "tmp");
        Files.newByteChannel(file, READ, WRITE, DELETE_ON_CLOSE).close();
        if (Files.exists(file))
            throw new RuntimeException("Temporary file was not deleted");
        Path dir = Files.createTempDirectory("blah");
        try {
            if (TestUtil.supportsLinks(dir)) {
                file = dir.resolve("foo");
                Files.createFile(file);
                Path link = dir.resolve("link");
                Files.createSymbolicLink(link, file);
                try {
                    Files.newByteChannel(link, READ, WRITE, DELETE_ON_CLOSE);
                    throw new RuntimeException("IOException expected");
                } catch (IOException ignore) { }
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                if (stream instanceof SecureDirectoryStream) {
                    SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>)stream;
                    file = Paths.get("foo");
                    Set<OpenOption> opts = new HashSet<>();
                    opts.add(WRITE);
                    opts.add(DELETE_ON_CLOSE);
                    secure.newByteChannel(file, opts).close();
                    if (Files.exists(dir.resolve(file)))
                        throw new RuntimeException("File not deleted");
                }
            }
        } finally {
            TestUtil.removeAll(dir);
        }
    }
}
