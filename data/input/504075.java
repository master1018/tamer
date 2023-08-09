public final class FileBackedOutputStream extends OutputStream {
  private final int fileThreshold;
  private final boolean resetOnFinalize;
  private final InputSupplier<InputStream> supplier;
  private OutputStream out;
  private MemoryOutput memory;
  private File file;
  private static class MemoryOutput extends ByteArrayOutputStream {
    byte[] getBuffer() {
      return buf;
    }
    int getCount() {
      return count;
    }
  }
  @VisibleForTesting synchronized File getFile() {
    return file;
  }
  public FileBackedOutputStream(int fileThreshold) {
    this(fileThreshold, false);
  }
  public FileBackedOutputStream(int fileThreshold, boolean resetOnFinalize) {
    this.fileThreshold = fileThreshold;
    this.resetOnFinalize = resetOnFinalize;
    memory = new MemoryOutput();
    out = memory;
    if (resetOnFinalize) {
      supplier = new InputSupplier<InputStream>() {
        public InputStream getInput() throws IOException {
          return openStream();
        }
        @Override protected void finalize() {
          try {
            reset();
          } catch (Throwable t) {
            t.printStackTrace(System.err);
          }
        }
      };
    } else {
      supplier = new InputSupplier<InputStream>() {
        public InputStream getInput() throws IOException {
          return openStream();
        }
      };
    }
  }
  public InputSupplier<InputStream> getSupplier() {
    return supplier;
  }
  private synchronized InputStream openStream() throws IOException {
    if (file != null) {
      return new FileInputStream(file);
    } else {
      return new ByteArrayInputStream(
          memory.getBuffer(), 0, memory.getCount());
    }
  }
  public synchronized void reset() throws IOException {
    try {
      close();
    } finally {
      if (memory == null) {
        memory = new MemoryOutput();
      } else {
        memory.reset();
      }
      out = memory;
      if (file != null) {
        File deleteMe = file;
        file = null;
        if (!deleteMe.delete()) {
          throw new IOException("Could not delete: " + deleteMe);
        }
      }
    }
  }
  @Override public synchronized void write(int b) throws IOException {
    update(1);
    out.write(b);
  }
  @Override public synchronized void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }
  @Override public synchronized void write(byte[] b, int off, int len) throws IOException {
    update(len);
    out.write(b, off, len);
  }
  @Override public synchronized void close() throws IOException {
    out.close();
  }
  @Override public synchronized void flush() throws IOException {
    out.flush();
  }
  private void update(int len) throws IOException {
    if (file == null && (memory.getCount() + len > fileThreshold)) {
      File temp = File.createTempFile("FileBackedOutputStream", null);
      if (resetOnFinalize) {
        temp.deleteOnExit();
      }
      FileOutputStream transfer = new FileOutputStream(temp);
      transfer.write(memory.getBuffer(), 0, memory.getCount());
      transfer.flush();
      out = transfer;
      file = temp;
      memory = null;
    }
  }
}
