public final class CharStreams {
  private static final int BUF_SIZE = 0x800; 
  private CharStreams() {}
  public static InputSupplier<StringReader> newReaderSupplier(final String value) {
    Preconditions.checkNotNull(value);
    return new InputSupplier<StringReader>() {
      public StringReader getInput() {
        return new StringReader(value);
      }
    };
  }
  public static InputSupplier<InputStreamReader> newReaderSupplier(
      final InputSupplier<? extends InputStream> in, final Charset charset) {
    Preconditions.checkNotNull(in);
    Preconditions.checkNotNull(charset);
    return new InputSupplier<InputStreamReader>() {
      public InputStreamReader getInput() throws IOException {
        return new InputStreamReader(in.getInput(), charset);
      }
    };
  }
  public static OutputSupplier<OutputStreamWriter> newWriterSupplier(
      final OutputSupplier<? extends OutputStream> out, final Charset charset) {
    Preconditions.checkNotNull(out);
    Preconditions.checkNotNull(charset);
    return new OutputSupplier<OutputStreamWriter>() {
      public OutputStreamWriter getOutput() throws IOException {
        return new OutputStreamWriter(out.getOutput(), charset);
      }
    };
  }
  public static <W extends Appendable & Closeable> void write(CharSequence from,
      OutputSupplier<W> to) throws IOException {
    Preconditions.checkNotNull(from);
    boolean threw = true;
    W out = to.getOutput();
    try {
      out.append(from);
      threw = false;
    } finally {
      Closeables.close(out, threw);
    }
  }
  public static <R extends Readable & Closeable,
      W extends Appendable & Closeable> long copy(InputSupplier<R> from,
      OutputSupplier<W> to) throws IOException {
    boolean threw = true;
    R in = from.getInput();
    try {
      W out = to.getOutput();
      try {
        long count = copy(in, out);
        threw = false;
        return count;
      } finally {
        Closeables.close(out, threw);
      }
    } finally {
      Closeables.close(in, threw);
    }
  }
  public static <R extends Readable & Closeable> long copy(InputSupplier<R> from,
      Appendable to) throws IOException {
    boolean threw = true;
    R in = from.getInput();
    try {
      long count = copy(in, to);
      threw = false;
      return count;
    } finally {
      Closeables.close(in, threw);
    }
  }
  public static long copy(Readable from, Appendable to) throws IOException {
    CharBuffer buf = CharBuffer.allocate(BUF_SIZE);
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      buf.flip();
      to.append(buf, 0, r);
      total += r;
    }
    return total;
  }
  public static String toString(Readable r) throws IOException {
    return toStringBuilder(r).toString();
  }
  public static <R extends Readable & Closeable> String toString(
      InputSupplier<R> supplier) throws IOException {
    return toStringBuilder(supplier).toString();
  }
  private static StringBuilder toStringBuilder(Readable r) throws IOException {
    StringBuilder sb = new StringBuilder();
    copy(r, sb);
    return sb;
  }
  private static <R extends Readable & Closeable> StringBuilder toStringBuilder(
      InputSupplier<R> supplier) throws IOException {
    boolean threw = true;
    R r = supplier.getInput();
    try {
      StringBuilder result = toStringBuilder(r);
      threw = false;
      return result;
    } finally {
      Closeables.close(r, threw);
    }
  }
  public static <R extends Readable & Closeable> String readFirstLine(
      InputSupplier<R> supplier) throws IOException {
    boolean threw = true;
    R r = supplier.getInput();
    try {
      String line = new LineReader(r).readLine();
      threw = false;
      return line;
    } finally {
      Closeables.close(r, threw);
    }
  }
  public static <R extends Readable & Closeable> List<String> readLines(
      InputSupplier<R> supplier) throws IOException {
    boolean threw = true;
    R r = supplier.getInput();
    try {
      List<String> result = readLines(r);
      threw = false;
      return result;
    } finally {
      Closeables.close(r, threw);
    }
  }
  public static List<String> readLines(Readable r) throws IOException {
    List<String> result = new ArrayList<String>();
    LineReader lineReader = new LineReader(r);
    String line;
    while ((line = lineReader.readLine()) != null) {
      result.add(line);
    }
    return result;
  }
  public static <R extends Readable & Closeable, T> T readLines(
      InputSupplier<R> supplier, LineProcessor<T> callback) throws IOException {
    boolean threw = true;
    R r = supplier.getInput();
    try {
      LineReader lineReader = new LineReader(r);
      String line;
      while ((line = lineReader.readLine()) != null) {
        if (!callback.processLine(line)) {
          break;
        }
      }
      threw = false;
    } finally {
      Closeables.close(r, threw);
    }
    return callback.getResult();
  }
  public static InputSupplier<Reader> join(
      final Iterable<? extends InputSupplier<? extends Reader>> suppliers) {
    return new InputSupplier<Reader>() {
       public Reader getInput() throws IOException {
        return new MultiReader(suppliers.iterator());
      }
    };
  }
  public static InputSupplier<Reader> join(
      InputSupplier<? extends Reader>... suppliers) {
    return join(Arrays.asList(suppliers));
  }
  public static void skipFully(Reader reader, long n) throws IOException {
    while (n > 0) {
      long amt = reader.skip(n);
      if (amt == 0) {
        if (reader.read() == -1) {
          throw new EOFException();
        }
        n--;
      } else {
        n -= amt;
      }
    }
  }
  public static Writer asWriter(Appendable target) {
    if (target instanceof Writer) {
      return (Writer) target;
    }
    return new AppendableWriter(target);
  }
}
