public class WorkerThread {
  private volatile boolean done = false;
  private MessageQueueBackend mqb;
  private MessageQueue mq;
  public WorkerThread() {
    mqb = new MessageQueueBackend();
    mq = mqb.getFirstQueue();
    new Thread(new MainLoop()).start();
  }
  public void invokeLater(Runnable runnable) {
    mq.writeMessage(runnable);
  }
  public void shutdown() {
    done = true;
    mq.writeMessage(new Runnable() { public void run() {} });
  }
  class MainLoop implements Runnable {
    private MessageQueue myMq;
    public MainLoop() {
      myMq = mqb.getSecondQueue();
    }
    public void run() {
      while (!done) {
        Runnable runnable = (Runnable) myMq.readMessage();
        try {
          runnable.run();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}
