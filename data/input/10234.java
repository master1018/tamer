public class BytesAndLines {
    static final Random rand = new Random();
    static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static void main(String[] args) throws IOException {
        testReadAndWriteBytes();
        testReadLines();
        testWriteLines();
    }
    static void testReadAndWriteBytes() throws IOException {
        testReadAndWriteBytes(0);
        for (int i=0; i<100; i++) {
            testReadAndWriteBytes(rand.nextInt(32000));
        }
        Path file = Paths.get("foo");
        List<String> lines = Collections.emptyList();
        try {
            readAllBytes(null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            write(null, lines, Charset.defaultCharset());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            write(file, null, Charset.defaultCharset());
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            write(file, lines, null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            write(file, lines, Charset.defaultCharset(), (OpenOption[])null);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
        try {
            OpenOption[] opts = { null };
            write(file, lines, Charset.defaultCharset(), opts);
            throw new RuntimeException("NullPointerException expected");
        } catch (NullPointerException ignore) { }
    }
    static void testReadAndWriteBytes(int size) throws IOException {
        Path path = createTempFile("blah", null);
        try {
            boolean append = rand.nextBoolean();
            byte[] b1 = new byte[size];
            rand.nextBytes(b1);
            byte[] b2 = (append) ? new byte[size] : new byte[0];
            rand.nextBytes(b2);
            if (rand.nextBoolean())
                delete(path);
            Path target = write(path, b1);
            assertTrue(target==path, "Unexpected path");
            assertTrue(size(path) == b1.length, "Unexpected file size");
            write(path, b2, StandardOpenOption.APPEND);
            assertTrue(size(path) == b1.length + b2.length, "Unexpected file size");
            byte[] read = readAllBytes(path);
            byte[] expected;
            if (append) {
                expected = new byte[b1.length + b2.length];
                System.arraycopy(b1, 0, expected, 0, b1.length);
                System.arraycopy(b2, 0, expected, b1.length, b2.length);
            } else {
                expected = b1;
            }
            assertTrue(Arrays.equals(read, expected),
                       "Bytes read not the same as bytes written");
        } finally {
            deleteIfExists(path);
        }
    }
    static void testReadLines() throws IOException {
        Path tmpfile = createTempFile("blah", "txt");
        try {
            List<String> lines;
            assertTrue(size(tmpfile) == 0, "File should be empty");
            lines = readAllLines(tmpfile, US_ASCII);
            assertTrue(lines.isEmpty(), "No line expected");
            byte[] hi = { (byte)'h', (byte)'i' };
            write(tmpfile, hi);
            lines = readAllLines(tmpfile, US_ASCII);
            assertTrue(lines.size() == 1, "One line expected");
            assertTrue(lines.get(0).equals("hi"), "'Hi' expected");
            List<String> expected = Arrays.asList("hi", "there");
            write(tmpfile, expected, US_ASCII);
            assertTrue(size(tmpfile) > 0, "File is empty");
            lines = readAllLines(tmpfile, US_ASCII);
            assertTrue(lines.equals(expected), "Unexpected lines");
            byte[] bad = { (byte)0xff, (byte)0xff };
            write(tmpfile, bad);
            try {
                readAllLines(tmpfile, US_ASCII);
                throw new RuntimeException("MalformedInputException expected");
            } catch (MalformedInputException ignore) { }
            try {
                readAllLines(null, US_ASCII);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
            try {
                readAllLines(tmpfile, null);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
        } finally {
            delete(tmpfile);
        }
    }
    static void testWriteLines() throws IOException {
        Path tmpfile = createTempFile("blah", "txt");
        try {
            if (rand.nextBoolean())
                delete(tmpfile);
            Path result = write(tmpfile, Collections.<String>emptyList(), US_ASCII);
            assert(size(tmpfile) == 0);
            assert(result == tmpfile);
            List<String> lines = Arrays.asList("hi", "there");
            write(tmpfile, lines, US_ASCII);
            List<String> actual = readAllLines(tmpfile, US_ASCII);
            assertTrue(actual.equals(lines), "Unexpected lines");
            write(tmpfile, lines, US_ASCII, StandardOpenOption.APPEND);
            List<String> expected = new ArrayList<String>();
            expected.addAll(lines);
            expected.addAll(lines);
            assertTrue(expected.size() == 4, "List should have 4 elements");
            actual = readAllLines(tmpfile, US_ASCII);
            assertTrue(actual.equals(expected), "Unexpected lines");
            try {
                String s = "\u00A0\u00A1";
                write(tmpfile, Arrays.asList(s), US_ASCII);
                throw new RuntimeException("UnmappableCharacterException expected");
            } catch (UnmappableCharacterException ignore) { }
            try {
                write(null, lines, US_ASCII);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
            try {
                write(tmpfile, null, US_ASCII);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
            try {
                write(tmpfile, lines, null);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
            try {
                write(tmpfile, lines, US_ASCII, (OpenOption[])null);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
            try {
                OpenOption[] opts = { (OpenOption)null };
                write(tmpfile, lines, US_ASCII, opts);
                throw new RuntimeException("NullPointerException expected");
            } catch (NullPointerException ignore) { }
        } finally {
            delete(tmpfile);
        }
    }
    static void assertTrue(boolean expr, String errmsg) {
        if (!expr)
            throw new RuntimeException(errmsg);
    }
}
