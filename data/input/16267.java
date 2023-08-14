public class StreamMonitor implements Runnable {
  private BufferedReader input;
  private boolean printStreamContents;
  private String  waitString;
  private boolean waitStringSeen;
  private List    triggers = new LinkedList();
  private List    triggersSeen = new LinkedList();
  private String  prefixString;
  private boolean printContents;
  private StringBuffer captureBuffer;
  class Trigger {
    private String[] triggerStrings;
    private int      triggerVal;
    Trigger(String str, int val) {
      triggerStrings = new String[] { str };
      triggerVal     = val;
    }
    Trigger(String[] strs, int val) {
      triggerStrings = strs;
      triggerVal     = val;
    }
    boolean matches(String str) {
      for (int i = 0; i < triggerStrings.length; i++) {
        if (str.indexOf(triggerStrings[i]) == -1) {
          return false;
        }
      }
      return true;
    }
    boolean equals(String[] strs) {
      if (strs.length != triggerStrings.length) {
        return false;
      }
      for (int i = 0; i < strs.length; i++) {
        if (!strs[i].equals(triggerStrings[i])) {
          return false;
        }
      }
      return true;
    }
  }
  public StreamMonitor(InputStream istr) {
    this(istr, null, false);
  }
  public StreamMonitor(InputStream istr, String prefixString, boolean printContents) {
    input = new BufferedReader(new InputStreamReader(istr));
    this.prefixString = prefixString;
    this.printContents = printContents;
    Thread thr = new Thread(this);
    thr.setDaemon(true);
    thr.start();
  }
  public boolean addTrigger(String str, int value) {
    return addTrigger(new String[] { str }, value);
  }
  public boolean addTrigger(String[] strs, int value) {
    for (Iterator iter = triggers.iterator(); iter.hasNext(); ) {
      Trigger trigger = (Trigger) iter.next();
      if (trigger.equals(strs)) {
        return false;
      }
    }
    Trigger trigger = new Trigger(strs, value);
    return triggers.add(trigger);
  }
  public boolean removeTrigger(String str) {
    return removeTrigger(new String[] { str });
  }
  public boolean removeTrigger(String[] strs) {
    for (ListIterator iter = triggers.listIterator(); iter.hasNext(); ) {
      Trigger trigger = (Trigger) iter.next();
      if (trigger.equals(strs)) {
        iter.remove();
        return true;
      }
    }
    return false;
  }
  public synchronized List getTriggersSeen() {
    List tmpList = triggersSeen;
    triggersSeen = new LinkedList();
    return tmpList;
  }
  public synchronized boolean waitFor(String str, long millis) {
    waitString = str;
    waitStringSeen = false;
    try {
      wait(millis);
    }
    catch (InterruptedException e) {
    }
    waitString = null;
    return waitStringSeen;
  }
  public synchronized void startCapture() {
    captureBuffer = new StringBuffer();
  }
  public synchronized String stopCapture() {
    String ret = captureBuffer.toString();
    captureBuffer = null;
    return ret;
  }
  public void run() {
    byte[] buf = new byte[10240];
    boolean shouldContinue = true;
    try {
      do {
        String str = input.readLine();
        if (str == null) {
          shouldContinue = false;
        } else {
          if (printContents) {
            System.err.println(prefixString + ": " + str);
          }
          synchronized (this) {
            if (captureBuffer != null) {
              captureBuffer.append(str);
              captureBuffer.append("\n");
            }
            if ((waitString != null) &&
                (str.indexOf(waitString) != -1)) {
              waitStringSeen = true;
              notifyAll();
            }
            for (Iterator iter = triggers.iterator(); iter.hasNext(); ) {
              Trigger trigger = (Trigger) iter.next();
              if (trigger.matches(str)) {
                triggersSeen.add(new Integer(trigger.triggerVal));
              }
            }
          }
        }
      } while (shouldContinue);
    }
    catch (IOException e) {
    }
    System.err.print("StreamMonitor ");
    if (prefixString != null) {
      System.err.print("\"" + prefixString + "\" ");
    }
    System.err.println("exiting");
  }
}
