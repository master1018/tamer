public final class ByteStreams {
  private static final int BUF_SIZE = 0x1000; 
  private ByteStreams() {}
  public static InputSupplier<ByteArrayInputStream> newInputStreamSupplier(
      byte[] b) {
    return newInputStreamSupplier(b, 0, b.length);
  }
  public static InputSupplier<ByteArrayInputStream> newInputStreamSupplier(
      final byte[] b, final int off, final int len) {
    return new InputSupplier<ByteArrayInputStream>() {
      public ByteArrayInputStream getInput() {
        return new ByteArrayInputStream(b, off, len);
      }
    };
  }
  public static void write(byte[] from,
      OutputSupplier<? extends OutputStream> to) throws IOException {
    Preconditions.checkNotNull(from);
    boolean threw = true;
    OutputStream out = to.getOutput();
    try {
      out.write(from);
      threw = false;
    } finally {
      Closeables.close(out, threw);
    }
  }
  public static long copy(InputSupplier<? extends InputStream> from,
      OutputSupplier<? extends OutputStream> to) throws IOException {
    boolean threw = true;
    InputStream in = from.getInput();
    try {
      OutputStream out = to.getOutput();
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
  public static long copy(InputSupplier<? extends InputStream> from,
      OutputStream to) throws IOException {
    boolean threw = true;
    InputStream in = from.getInput();
    try {
      long count = copy(in, to);
      threw = false;
      return count;
    } finally {
      Closeables.close(in, threw);
    }
  }
  public static long copy(InputStream from, OutputStream to)
      throws IOException {
    byte[] buf = new byte[BUF_SIZE];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      total += r;
    }
    return total;
  }
  public static long copy(ReadableByteChannel from,
      WritableByteChannel to) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(BUF_SIZE);
    long total = 0;
    while (from.read(buf) != -1) {
      buf.flip();
      while (buf.hasRemaining()) {
        total += to.write(buf);
      }
      buf.clear();
    }
    return total;
  }
  public static byte[] toByteArray(InputStream in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(in, out);
    return out.toByteArray();
  }
  public static byte[] toByteArray(
      InputSupplier<? extends InputStream> supplier) throws IOException {
    boolean threw = true;
    InputStream in = supplier.getInput();
    try {
      byte[] result = toByteArray(in);
      threw = false;
      return result;
    } finally {
      Closeables.close(in, threw);
    }
  }
  public static ByteArrayDataInput newDataInput(byte[] bytes) {
    return new ByteArrayDataInputStream(bytes);
  }
  public static ByteArrayDataInput newDataInput(byte[] bytes, int start) {
    Preconditions.checkPositionIndex(start, bytes.length);
    return new ByteArrayDataInputStream(bytes, start);
  }
  private static class ByteArrayDataInputStream implements ByteArrayDataInput {
    final DataInput input;
    ByteArrayDataInputStream(byte[] bytes) {
      this.input = new DataInputStream(new ByteArrayInputStream(bytes));
    }
    ByteArrayDataInputStream(byte[] bytes, int start) {
      this.input = new DataInputStream(
          new ByteArrayInputStream(bytes, start, bytes.length - start));
    }
     public void readFully(byte b[]) {
      try {
        input.readFully(b);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public void readFully(byte b[], int off, int len) {
      try {
        input.readFully(b, off, len);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public int skipBytes(int n) {
      try {
        return input.skipBytes(n);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public boolean readBoolean() {
      try {
        return input.readBoolean();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public byte readByte() {
      try {
        return input.readByte();
      } catch (EOFException e) {
        throw new IllegalStateException(e);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public int readUnsignedByte() {
      try {
        return input.readUnsignedByte();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public short readShort() {
      try {
        return input.readShort();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public int readUnsignedShort() {
      try {
        return input.readUnsignedShort();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public char readChar() {
      try {
        return input.readChar();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public int readInt() {
      try {
        return input.readInt();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public long readLong() {
      try {
        return input.readLong();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public float readFloat() {
      try {
        return input.readFloat();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public double readDouble() {
      try {
        return input.readDouble();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public String readLine() {
      try {
        return input.readLine();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
     public String readUTF() {
      try {
        return input.readUTF();
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }
  }
  public static ByteArrayDataOutput newDataOutput() {
    return new ByteArrayDataOutputStream();
  }
  public static ByteArrayDataOutput newDataOutput(int size) {
    Preconditions.checkArgument(size >= 0, "Invalid size: %s", size);
    return new ByteArrayDataOutputStream(size);
  }
  @SuppressWarnings("deprecation") 
  private static class ByteArrayDataOutputStream
      implements ByteArrayDataOutput {
    final DataOutput output;
    final ByteArrayOutputStream byteArrayOutputSteam;
    ByteArrayDataOutputStream() {
      this(new ByteArrayOutputStream());
    }
    ByteArrayDataOutputStream(int size) {
      this(new ByteArrayOutputStream(size));
    }
    ByteArrayDataOutputStream(ByteArrayOutputStream byteArrayOutputSteam) {
      this.byteArrayOutputSteam = byteArrayOutputSteam;
      output = new DataOutputStream(byteArrayOutputSteam);
    }
     public void write(int b) {
      try {
        output.write(b);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void write(byte[] b) {
      try {
        output.write(b);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void write(byte[] b, int off, int len) {
      try {
        output.write(b, off, len);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeBoolean(boolean v) {
      try {
        output.writeBoolean(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeByte(int v) {
      try {
        output.writeByte(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeBytes(String s) {
      try {
        output.writeBytes(s);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeChar(int v) {
      try {
        output.writeChar(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeChars(String s) {
      try {
        output.writeChars(s);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeDouble(double v) {
      try {
        output.writeDouble(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeFloat(float v) {
      try {
        output.writeFloat(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeInt(int v) {
      try {
        output.writeInt(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeLong(long v) {
      try {
        output.writeLong(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeShort(int v) {
      try {
        output.writeShort(v);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public void writeUTF(String s) {
      try {
        output.writeUTF(s);
      } catch (IOException impossible) {
        throw new AssertionError(impossible);
      }
    }
     public byte[] toByteArray() {
      return byteArrayOutputSteam.toByteArray();
    }
  }
  public static long length(InputSupplier<? extends InputStream> supplier)
      throws IOException {
    long count = 0;
    boolean threw = true;
    InputStream in = supplier.getInput();
    try {
      while (true) {
        long amt = in.skip(Integer.MAX_VALUE);
        if (amt == 0) {
          if (in.read() == -1) {
            threw = false;
            return count;
          }
          count++;
        } else {
          count += amt;
        }
      }
    } finally {
      Closeables.close(in, threw);
    }
  }
  public static boolean equal(InputSupplier<? extends InputStream> supplier1,
      InputSupplier<? extends InputStream> supplier2) throws IOException {
    byte[] buf1 = new byte[BUF_SIZE];
    byte[] buf2 = new byte[BUF_SIZE];
    boolean threw = true;
    InputStream in1 = supplier1.getInput();
    try {
      InputStream in2 = supplier2.getInput();
      try {
        while (true) {
          int read1 = read(in1, buf1, 0, BUF_SIZE);
          int read2 = read(in2, buf2, 0, BUF_SIZE);
          if (read1 != read2 || !Arrays.equals(buf1, buf2)) {
            threw = false;
            return false;
          } else if (read1 != BUF_SIZE) {
            threw = false;
            return true;
          }
        }
      } finally {
        Closeables.close(in2, threw);
      }
    } finally {
      Closeables.close(in1, threw);
    }
  }
  public static void readFully(InputStream in, byte[] b) throws IOException {
    readFully(in, b, 0, b.length);
  }
  public static void readFully(InputStream in, byte[] b, int off, int len)
      throws IOException {
    if (read(in, b, off, len) != len) {
      throw new EOFException();
    }
  }
  public static void skipFully(InputStream in, long n) throws IOException {
    while (n > 0) {
      long amt = in.skip(n);
      if (amt == 0) {
        if (in.read() == -1) {
          throw new EOFException();
        }
        n--;
      } else {
        n -= amt;
      }
    }
  }
  public static <T> T readBytes(InputSupplier<? extends InputStream> supplier,
      ByteProcessor<T> processor) throws IOException {
    byte[] buf = new byte[BUF_SIZE];
    boolean threw = true;
    InputStream in = supplier.getInput();
    try {
      int amt;
      do {
        amt = in.read(buf);
        if (amt == -1) {
          threw = false;
          break;
        }
      } while (processor.processBytes(buf, 0, amt));
      return processor.getResult();
    } finally {
      Closeables.close(in, threw);
    }
  }
  public static long getChecksum(InputSupplier<? extends InputStream> supplier,
      final Checksum checksum) throws IOException {
    return readBytes(supplier, new ByteProcessor<Long>() {
      public boolean processBytes(byte[] buf, int off, int len) {
        checksum.update(buf, off, len);
        return true;
      }
      public Long getResult() {
        long result = checksum.getValue();
        checksum.reset();
        return result;
      }
    });
  }
  public static byte[] getDigest(InputSupplier<? extends InputStream> supplier,
      final MessageDigest md) throws IOException {
    return readBytes(supplier, new ByteProcessor<byte[]>() {
      public boolean processBytes(byte[] buf, int off, int len) {
        md.update(buf, off, len);
        return true;
      }
      public byte[] getResult() {
        return md.digest();
      }
    });
  }
  public static int read(InputStream in, byte[] b, int off, int len)
      throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException("len is negative");
    }
    int total = 0;
    while (total < len) {
      int result = in.read(b, off + total, len - total);
      if (result == -1) {
        break;
      }
      total += result;
    }
    return total;
  }
  public static InputSupplier<InputStream> slice(
      final InputSupplier<? extends InputStream> supplier,
      final long offset,
      final long length) {
    Preconditions.checkNotNull(supplier);
    Preconditions.checkArgument(offset >= 0, "offset is negative");
    Preconditions.checkArgument(length >= 0, "length is negative");
    return new InputSupplier<InputStream>() {
       public InputStream getInput() throws IOException {
        InputStream in = supplier.getInput();
        if (offset > 0) {
          try {
            skipFully(in, offset);
          } catch (IOException e) {
            Closeables.closeQuietly(in);
            throw e;
          }
        }
        return new LimitInputStream(in, length);
      }
    };
  }
  public static InputSupplier<InputStream> join(
     final Iterable<? extends InputSupplier<? extends InputStream>> suppliers) {
    return new InputSupplier<InputStream>() {
       public InputStream getInput() throws IOException {
        return new MultiInputStream(suppliers.iterator());
      }
    };
  }
  public static InputSupplier<InputStream> join(
      InputSupplier<? extends InputStream>... suppliers) {
    return join(Arrays.asList(suppliers));
  }
}
