public class RegisterPanel extends JPanel {
  private java.util.List registers;
  private AbstractTableModel dataModel;
  private boolean valid;
  private boolean editable;
  private String nullAddressString;
  private ThreadProxy curThread;
  private JTable table;
  static class RegisterInfo {
    private String name;
    private Address value;
    RegisterInfo(String name, Address value) {
      this.name = name;
      this.value = value;
    }
    String  getName()  { return name;  }
    Address getValue() { return value; }
  }
  public RegisterPanel() {
    super();
    registers = new ArrayList();
    dataModel = new AbstractTableModel() {
        public int getColumnCount() { return 2; }
        public int getRowCount()    { return registers.size(); }
        public String getColumnName(int col) {
          switch (col) {
          case 0:
            return "Register Name";
          case 1:
            return "Register Value";
          default:
            throw new RuntimeException("Index " + col + " out of bounds");
          }
        }
        public Object getValueAt(int row, int col) {
          RegisterInfo info = (RegisterInfo) registers.get(row);
          switch (col) {
          case 0:
            return info.getName();
          case 1:
            if (valid) {
              Address val = info.getValue();
              if (val != null) {
                return val;
              } else {
                return nullAddressString;
              }
            } else {
              return "-";
            }
          default:
            throw new RuntimeException("Index (" + col + ", " + row + ") out of bounds");
          }
        }
        public boolean isCellEditable(int row, int col) {
          if (col == 0) return false;
          if (!valid) return false;
          if (curThread == null) return false;
          if (!curThread.canSetContext()) return false;
          return false;
        }
      };
    setLayout(new BorderLayout());
    table = new JTable(dataModel);
    table.setCellSelectionEnabled(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    table.setDragEnabled(true);
    JTableHeader header = table.getTableHeader();
    header.setReorderingAllowed(false);
    JScrollPane scrollPane = new JScrollPane(table);
    add(scrollPane, BorderLayout.CENTER);
  }
  public void update(ThreadProxy curThread) {
    this.curThread = curThread;
    ThreadContext context = curThread.getContext();
    editable = curThread.canSetContext();
    registers.clear();
    for (int i = 0; i < context.getNumRegisters(); i++) {
      String name = context.getRegisterName(i);
      Address addr = context.getRegisterAsAddress(i);
      if ((nullAddressString == null) && (addr != null)) {
        String addrStr = addr.toString();
        StringBuffer buf = new StringBuffer();
        buf.append("0x");
        int len = addrStr.length() - 2;
        for (int j = 0; j < len; j++) {
          buf.append("0");
        }
        nullAddressString = buf.toString();
      }
      registers.add(new RegisterInfo(name, addr));
    }
    valid = true;
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          dataModel.fireTableDataChanged();
        }
      });
  }
  public void clear() {
    valid = false;
    nullAddressString = null;
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          dataModel.fireTableDataChanged();
        }
      });
  }
  public void setFont(Font font) {
    super.setFont(font);
    if (table != null) {
      table.setFont(font);
    }
  }
}
