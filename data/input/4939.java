public class WatcherThread extends Thread {
  public WatcherThread(Address addr) {
    super(addr);
  }
  public boolean isWatcherThread() { return true; }
}
