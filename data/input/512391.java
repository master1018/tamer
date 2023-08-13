final class MultiInputStream extends InputStream {
  private Iterator<? extends InputSupplier<? extends InputStream>> it;
  private InputStream in;
  public MultiInputStream(
      Iterator<? extends InputSupplier<? extends InputStream>> it)
      throws IOException {
    this.it = it;
    advance();
  }
  @Override public void close() throws IOException {
    if (in != null) {
      try {
        in.close();
      } finally {
        in = null;
      }
    }
  }
  private void advance() throws IOException {
    close();
    if (it.hasNext()) {
      in = it.next().getInput();
    }
  }
  @Override public int available() throws IOException {
    if (in == null) {
      return 0;
    }
    return in.available();
  }
  @Override public boolean markSupported() {
    return false;
  }
  @Override public int read() throws IOException {
    if (in == null) {
      return -1;
    }
    int result = in.read();
    if (result == -1) {
      advance();
      return read();
    }
    return result;
  }
  @Override public int read(byte[] b, int off, int len) throws IOException {
    if (in == null) {
      return -1;
    }
    int result = in.read(b, off, len);
    if (result == -1) {
      advance();
      return read(b, off, len);
    }
    return result;
  }
  @Override public long skip(long n) throws IOException {
    if (in == null || n <= 0) {
      return 0;
    }
    long result = in.skip(n);
    if (result != 0) {
      return result;
    }
    if (read() == -1) {
      return 0;
    }
    return 1 + in.skip(n - 1);
  }
}
