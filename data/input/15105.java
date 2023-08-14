public class DriveLetter {
    public static void main(String[] args) throws IOException {
        String os = System.getProperty("os.name");
        if (!os.startsWith("Windows")) {
            System.out.println("This is Windows specific test");
            return;
        }
        String here = System.getProperty("user.dir");
        if (here.length() < 2 || here.charAt(1) != ':')
            throw new RuntimeException("Unable to determine drive letter");
        File tempFile = File.createTempFile("foo", "tmp", new File(here));
        try {
            String drive = here.substring(0, 2);
            Path expected = Paths.get(drive).resolve(tempFile.getName());
            boolean found = false;
            Path dir = Paths.get(drive);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    if (file.equals(expected)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                throw new RuntimeException("Temporary file not found???");
        } finally {
            tempFile.delete();
        }
    }
}
