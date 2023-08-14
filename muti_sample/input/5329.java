public class StackTracePanel extends JPanel {
  public interface Listener {
    public void frameChanged(CFrame fr, JavaVFrame jfr);
  }
  class Model extends AbstractListModel implements ComboBoxModel {
    private Object selectedItem;
    public Object getElementAt(int index) {
      if (trace == null) return null;
      return trace.get(index);
    }
    public int getSize() {
      if (trace == null) return 0;
      return trace.size();
    }
    public Object getSelectedItem() {
      return selectedItem;
    }
    public void setSelectedItem(Object item) {
      selectedItem = item;
    }
    public void dataChanged() {
      fireContentsChanged(this, 0, trace.size());
    }
  }
  private java.util.List trace;
  private Model model;
  private JComboBox list;
  private java.util.List listeners;
  public StackTracePanel() {
    super();
    model = new Model();
    setLayout(new BorderLayout());
    setBorder(GraphicsUtilities.newBorder(5));
    list = new JComboBox(model);
    list.setPrototypeDisplayValue("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
    add(list, BorderLayout.CENTER);
    list.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            fireFrameChanged();
          }
        }
      });
  }
  public void setTrace(java.util.List trace) {
    this.trace = trace;
    model.dataChanged();
    list.setSelectedIndex(0);
    fireFrameChanged();
  }
  public void addListener(Listener listener) {
    if (listeners == null) {
      listeners = new ArrayList();
    }
    listeners.add(listener);
  }
  protected void fireFrameChanged() {
    if (listeners != null) {
      StackTraceEntry entry = (StackTraceEntry) trace.get(list.getSelectedIndex());
      for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
        ((Listener) iter.next()).frameChanged(entry.getCFrame(), entry.getJavaFrame());
      }
    }
  }
}
