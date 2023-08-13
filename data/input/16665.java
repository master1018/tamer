public class MessageQueueBackend {
  private MessageQueueImpl leftRightQueue;
  private MessageQueueImpl rightLeftQueue;
  public MessageQueueBackend() {
    LinkedList leftRightPipe = new LinkedList();
    LinkedList rightLeftPipe = new LinkedList();
    leftRightQueue = new MessageQueueImpl(rightLeftPipe, leftRightPipe);
    rightLeftQueue = new MessageQueueImpl(leftRightPipe, rightLeftPipe);
  }
  public MessageQueue getFirstQueue() {
    return leftRightQueue;
  }
  public MessageQueue getSecondQueue() {
    return rightLeftQueue;
  }
  private class MessageQueueImpl implements MessageQueue {
    private LinkedList readList;
    private LinkedList writeList;
    public MessageQueueImpl(LinkedList listToReadFrom, LinkedList listToWriteTo) {
      readList = listToReadFrom;
      writeList = listToWriteTo;
    }
    public Object readMessage() {
      synchronized(readList) {
        while (readList.isEmpty()) {
          try {
            readList.wait();
          }
          catch (InterruptedException e) {
          }
        }
        return readList.removeFirst();
      }
    }
    public Object readMessageWithTimeout(long millis) {
      synchronized(readList) {
        if (readList.isEmpty()) {
          if (millis == 0) {
            return null;
          }
          try {
            readList.wait(millis);
          }
          catch (InterruptedException e) {
          }
        }
        if (readList.isEmpty()) {
          return null;
        }
        return readList.removeFirst();
      }
    }
    public void writeMessage(Object obj) {
      synchronized(writeList) {
        writeList.addLast(obj);
        writeList.notify();
      }
    }
  }
}
