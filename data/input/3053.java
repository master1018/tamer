public class LinkedQueue {
  protected LinkedNode head_;
  protected int count_;
  protected final Object lastMonitor_ = new Object();
  protected LinkedNode last_;
  protected int waitingForTake_ = 0;
  public LinkedQueue() {
    head_ = new LinkedNode(null);
    last_ = head_;
    count_ = 0;
  }
  protected void insert(Object x) {
    synchronized(lastMonitor_) {
      LinkedNode p = new LinkedNode(x);
      last_.next = p;
      last_ = p;
      count_++;
      if (count_ > 1000 && (count_ % 1000 == 0))
        System.out.println("In Queue : " + count_);
      if (waitingForTake_ > 0)
        lastMonitor_.notify();
    }
  }
  protected synchronized Object extract() {
    Object x = null;
    LinkedNode first = head_.next;
    if (first != null) {
      x = first.value;
      first.value = null;
      head_ = first;
      count_ --;
    }
    return x;
  }
  public void put(Object x) throws InterruptedException {
    if (x == null) throw new IllegalArgumentException();
    if (Thread.interrupted()) throw new InterruptedException();
    insert(x);
  }
  public boolean offer(Object x, long msecs) throws InterruptedException {
    if (x == null) throw new IllegalArgumentException();
    if (Thread.interrupted()) throw new InterruptedException();
    insert(x);
    return true;
  }
  public Object take() throws InterruptedException {
    if (Thread.interrupted()) throw new InterruptedException();
    Object x = extract();
    if (x != null)
      return x;
    else {
      synchronized(lastMonitor_) {
        try {
          ++waitingForTake_;
          for (;;) {
            x = extract();
            if (x != null) {
              --waitingForTake_;
              return x;
            }
            else {
              lastMonitor_.wait();
            }
          }
        }
        catch(InterruptedException ex) {
          --waitingForTake_;
          lastMonitor_.notify();
          throw ex;
        }
      }
    }
  }
  public synchronized Object peek() {
    LinkedNode first = head_.next;
    if (first != null)
      return first.value;
    else
      return null;
  }
  public synchronized boolean isEmpty() {
    return head_.next == null;
  }
  public Object poll(long msecs) throws InterruptedException {
    if (Thread.interrupted()) throw new InterruptedException();
    Object x = extract();
    if (x != null)
      return x;
    else {
      synchronized(lastMonitor_) {
        try {
          long waitTime = msecs;
          long start = (msecs <= 0)? 0 : System.currentTimeMillis();
          ++waitingForTake_;
          for (;;) {
            x = extract();
            if (x != null || waitTime <= 0) {
              --waitingForTake_;
              return x;
            }
            else {
              lastMonitor_.wait(waitTime);
              waitTime = msecs - (System.currentTimeMillis() - start);
            }
          }
        }
        catch(InterruptedException ex) {
          --waitingForTake_;
          lastMonitor_.notify();
          throw ex;
        }
      }
    }
  }
  class LinkedNode {
    Object value;
    LinkedNode next = null;
    LinkedNode(Object x) { value = x; }
    LinkedNode(Object x, LinkedNode n) { value = x; next = n; }
  }
}
