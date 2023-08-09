public class CompressedWriteStream extends CompressedStream {
  public CompressedWriteStream(Address buffer) {
    this(buffer, 0);
  }
  public CompressedWriteStream(Address buffer, int position) {
    super(buffer, position);
  }
}
