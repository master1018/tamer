public class StackTraceTool extends JPanel {
    private static final long serialVersionUID = 9140041989427965718L;
    private Environment env;
    private ExecutionManager runtime;
    private ContextManager context;
    private ThreadInfo tinfo;
    private JList list;
    private ListModel stackModel;
    public StackTraceTool(Environment env) {
        super(new BorderLayout());
        this.env = env;
        this.runtime = env.getExecutionManager();
        this.context = env.getContextManager();
        stackModel = new DefaultListModel();  
        list = new JList(stackModel);
        list.setCellRenderer(new StackFrameRenderer());
        JScrollPane listView = new JScrollPane(list);
        add(listView);
        StackTraceToolListener listener = new StackTraceToolListener();
        context.addContextListener(listener);
        list.addListSelectionListener(listener);
    }
    private class StackTraceToolListener
        implements ContextListener, ListSelectionListener
    {
        @Override
        public void currentFrameChanged(CurrentFrameChangedEvent e) {
            int frameIndex = e.getIndex();
            ThreadInfo ti = e.getThreadInfo();
            if (e.getInvalidate() || tinfo != ti) {
                tinfo = ti;
                showStack(ti, frameIndex);
            } else {
                if (frameIndex < stackModel.getSize()) {
                    list.setSelectedIndex(frameIndex);
                    list.ensureIndexIsVisible(frameIndex);
                }
            }
        }
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int index = list.getSelectedIndex();
            if (index != -1) {
                try {
                    context.setCurrentFrameIndex(index);
                } catch (VMNotInterruptedException exc) {
                }
            }
        }
    }
    private class StackFrameRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index,
                                               isSelected, cellHasFocus);
            if (value == null) {
                this.setText("<unavailable>");
            } else {
                StackFrame frame = (StackFrame)value;
                Location loc = frame.location();
                Method meth = loc.method();
                String methName =
                    meth.declaringType().name() + '.' + meth.name();
                String position = "";
                if (meth.isNative()) {
                    position = " (native method)";
                } else if (loc.lineNumber() != -1) {
                    position = ":" + loc.lineNumber();
                } else {
                    long pc = loc.codeIndex();
                    if (pc != -1) {
                        position = ", pc = " + pc;
                    }
                }
                this.setText("[" + (index+1) +"] " + methName + position);
            }
            return this;
        }
    }
    private void showStack(ThreadInfo tinfo, int selectFrame) {
        StackTraceListModel model = new StackTraceListModel(tinfo);
        stackModel = model;
        list.setModel(stackModel);
        list.setSelectedIndex(selectFrame);
        list.ensureIndexIsVisible(selectFrame);
    }
    private static class StackTraceListModel extends AbstractListModel {
        private final ThreadInfo tinfo;
        public StackTraceListModel(ThreadInfo tinfo) {
            this.tinfo = tinfo;
        }
        @Override
        public Object getElementAt(int index) {
            try {
                return tinfo == null? null : tinfo.getFrame(index);
            } catch (VMNotInterruptedException e) {
                return null;
            }
        }
        @Override
        public int getSize() {
            try {
                return tinfo == null? 1 : tinfo.getFrameCount();
            } catch (VMNotInterruptedException e) {
                return 0;
            }
        }
    }
}
