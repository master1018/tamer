public final class LineReader {
  private final Readable readable;
  private final Reader reader;
  private final char[] buf = new char[0x1000]; 
  private final CharBuffer cbuf = CharBuffer.wrap(buf);
  private final Queue<String> lines = new LinkedList<String>();
  private final LineBuffer lineBuf = new LineBuffer() {
    @Override protected void handleLine(String line, String end) {
      lines.add(line);
    }
  };
  public LineReader(Readable readable) {
    Preconditions.checkNotNull(readable);
    this.readable = readable;
    this.reader = (readable instanceof Reader) ? (Reader) readable : null;
  }
  public String readLine() throws IOException {
    while (lines.peek() == null) {
      cbuf.clear();
      int read = (reader != null)
          ? reader.read(buf, 0, buf.length)
          : readable.read(cbuf);
      if (read == -1) {
        lineBuf.finish();
        break;
      }
      lineBuf.add(buf, 0, read);
    }
    return lines.poll();
  }
}
