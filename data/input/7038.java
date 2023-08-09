public class ThreadListPanel extends JPanel {
  public static interface Listener {
    public void setFocus(ThreadProxy thread, JavaThread jthread);
  }
  static class ThreadInfo {
    private ThreadProxy thread;
    private boolean     gotPC;
    private Address     pc;
    private String      location;
    private JavaThread  javaThread;
    private String      javaThreadName;
    public ThreadInfo(ThreadProxy thread, CDebugger dbg, JavaThread jthread) {
      this.thread = thread;
      this.location = "<unknown>";
      CFrame fr = dbg.topFrameForThread(thread);
      if (fr != null) {
        gotPC = true;
        pc = fr.pc();
        PCFinder.Info info = PCFinder.findPC(pc, fr.loadObjectForPC(), dbg);
        if (info.getName() != null) {
          location = info.getName();
          if (info.getConfidence() == PCFinder.LOW_CONFIDENCE) {
            location = location + " (?)";
          }
          if (info.getOffset() < 0) {
            location = location + " + 0x" + Long.toHexString(info.getOffset());
          }
        }
      }
      if (jthread != null) {
        javaThread = jthread;
        javaThreadName = jthread.getThreadName();
      }
    }
    public ThreadProxy getThread()    { return thread;       }
    public boolean     hasPC()        { return gotPC;        }
    public Address     getPC()        { return pc;           }
    public String      getLocation()  { return location;     }
    public boolean     isJavaThread() { return (javaThread != null); }
    public JavaThread  getJavaThread() { return javaThread; }
    public String      getJavaThreadName() { return javaThreadName; }
  }
  private java.util.List threadList;
  private JTable table;
  private AbstractTableModel dataModel;
  private java.util.List listeners;
  public ThreadListPanel(CDebugger dbg, final boolean displayJavaThreads) {
    super();
    Map threadToJavaThreadMap = null;
    if (displayJavaThreads) {
      threadToJavaThreadMap = new HashMap();
      Threads threads = VM.getVM().getThreads();
      for (JavaThread thr = threads.first(); thr != null; thr = thr.next()) {
        threadToJavaThreadMap.put(thr.getThreadProxy(), thr);
      }
    }
    java.util.List threads = dbg.getThreadList();
    threadList = new ArrayList(threads.size());
    for (Iterator iter = threads.iterator(); iter.hasNext(); ) {
      ThreadProxy thr = (ThreadProxy) iter.next();
      JavaThread jthr = null;
      if (displayJavaThreads) {
        jthr = (JavaThread) threadToJavaThreadMap.get(thr);
      }
      threadList.add(new ThreadInfo(thr, dbg, jthr));
    }
    dataModel = new AbstractTableModel() {
        public int getColumnCount() { return (displayJavaThreads ? 5 : 3); }
        public int getRowCount()    { return threadList.size(); }
        public String getColumnName(int col) {
          switch (col) {
          case 0:
            return "Thread ID";
          case 1:
            return "PC";
          case 2:
            return "Location";
          case 3:
            return "Java?";
          case 4:
            return "Java Thread Name";
          default:
            throw new RuntimeException("Index " + col + " out of bounds");
          }
        }
        public Object getValueAt(int row, int col) {
          ThreadInfo info = (ThreadInfo) threadList.get(row);
          switch (col) {
          case 0:
            return info.getThread();
          case 1:
            {
              if (info.hasPC()) {
                return info.getPC();
              }
              return "<no frames on stack>";
            }
          case 2:
            return info.getLocation();
          case 3:
            if (info.isJavaThread()) {
              return "Yes";
            } else {
              return "";
            }
          case 4:
            if (info.isJavaThread()) {
              return info.getJavaThreadName();
            } else {
              return "";
            }
          default:
            throw new RuntimeException("Index (" + col + ", " + row + ") out of bounds");
          }
        }
      };
    setLayout(new BorderLayout());
    table = new JTable(dataModel);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JTableHeader header = table.getTableHeader();
    header.setReorderingAllowed(false);
    table.setRowSelectionAllowed(true);
    table.setColumnSelectionAllowed(false);
    JScrollPane scrollPane = new JScrollPane(table);
    add(scrollPane, BorderLayout.CENTER);
    if (threadList.size() > 0) {
      table.setRowSelectionInterval(0, 0);
    }
    JButton button = new JButton("Set Focus");
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int i = table.getSelectedRow();
          if (i < 0) {
            return;
          }
          ThreadInfo info = (ThreadInfo) threadList.get(i);
          for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            ((Listener) iter.next()).setFocus(info.getThread(), info.getJavaThread());
          }
        }
      });
    JPanel focusPanel = new JPanel();
    focusPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    focusPanel.setLayout(new BoxLayout(focusPanel, BoxLayout.Y_AXIS));
    focusPanel.add(Box.createGlue());
    focusPanel.add(button);
    focusPanel.add(Box.createGlue());
    add(focusPanel, BorderLayout.EAST);
  }
  public void addListener(Listener l) {
    if (listeners == null) {
      listeners = new ArrayList();
    }
    listeners.add(l);
  }
}
