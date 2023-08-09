public class ReadResult implements Serializable {
  private byte[] data; 
  private long   failureAddress;
  public ReadResult(byte[] data) {
    this.data = data;
  }
  public ReadResult(long failureAddress) {
    this.failureAddress = failureAddress;
  }
  public byte[] getData() {
    return data;
  }
  public long getFailureAddress() {
    return failureAddress;
  }
}
