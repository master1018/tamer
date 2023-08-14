public class CountingOutputStream extends FilterOutputStream {
  private long count;
  public CountingOutputStream(OutputStream out) {
    super(out);
  }
  public long getCount() {
    return count;
  }
  @Override public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    count += len;
  }
  @Override public void write(int b) throws IOException {
    out.write(b);
    count++;
  }
}
