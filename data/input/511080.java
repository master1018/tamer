class AppendableWriter extends Writer {
  private final Appendable target;
  private boolean closed;
  AppendableWriter(Appendable target) {
    this.target = target;
  }
  @Override public void write(char cbuf[], int off, int len)
      throws IOException {
    checkNotClosed();
    target.append(new String(cbuf, off, len));
  }
  @Override public void flush() throws IOException {
    checkNotClosed();
    if (target instanceof Flushable) {
      ((Flushable) target).flush();
    }
  }
  @Override public void close() throws IOException {
    this.closed = true;
    if (target instanceof Closeable) {
      ((Closeable) target).close();
    }
  }
  @Override public void write(int c) throws IOException {
    checkNotClosed();
    target.append((char) c);
  }
  @Override public void write(String str) throws IOException {
    checkNotClosed();
    target.append(str);
  }
  @Override public void write(String str, int off, int len) throws IOException {
    checkNotClosed();
    target.append(str, off, off + len);
  }
  @Override public Writer append(char c) throws IOException {
    checkNotClosed();
    target.append(c);
    return this;
  }
  @Override public Writer append(CharSequence charSeq) throws IOException {
    checkNotClosed();
    target.append(charSeq);
    return this;
  }
  @Override public Writer append(CharSequence charSeq, int start, int end)
      throws IOException {
    checkNotClosed();
    target.append(charSeq, start, end);
    return this;
  }
  private void checkNotClosed() throws IOException {
    if (closed) {
      throw new IOException("Cannot write to a closed writer.");
    }
  }
}
