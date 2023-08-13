public class SourceTool extends JPanel {
    private static final long serialVersionUID = -5461299294186395257L;
    private Environment env;
    private ExecutionManager runtime;
    private ContextManager context;
    private SourceManager sourceManager;
    private JList list;
    private ListModel sourceModel;
    private String sourceName;          
    private Location sourceLocn;        
    private CommandInterpreter interpreter;
    public SourceTool(Environment env) {
        super(new BorderLayout());
        this.env = env;
        runtime = env.getExecutionManager();
        sourceManager = env.getSourceManager();
        this.context = env.getContextManager();
        this.interpreter = new CommandInterpreter(env, true);
        sourceModel = new DefaultListModel();  
        list = new JList(sourceModel);
        list.setCellRenderer(new SourceLineRenderer());
        list.setPrototypeCellValue(SourceModel.prototypeCellValue);
        SourceToolListener listener = new SourceToolListener();
        context.addContextListener(listener);
        runtime.addSpecListener(listener);
        sourceManager.addSourceListener(listener);
        MouseListener squeek = new STMouseListener();
        list.addMouseListener(squeek);
        add(new JScrollPane(list));
    }
    public void setTextFont(Font f) {
        list.setFont(f);
        list.setPrototypeCellValue(SourceModel.prototypeCellValue);
    }
    private class SourceToolListener
               implements ContextListener, SourceListener, SpecListener
    {
        @Override
        public void currentFrameChanged(CurrentFrameChangedEvent e) {
            showSourceContext(e.getThread(), e.getIndex());
        }
        @Override
        public void sourcepathChanged(SourcepathChangedEvent e) {
            if (sourceName != null) {
                showSourceFile(sourceName);
            } else if (sourceLocn != null) {
                showSourceForLocation(sourceLocn);
            }
        }
        @Override
        public void breakpointSet(SpecEvent e) {
            breakpointResolved(e);
        }
        @Override
        public void breakpointDeferred(SpecEvent e) { }
        @Override
        public void breakpointDeleted(SpecEvent e) {
            BreakpointRequest req = (BreakpointRequest)e.getEventRequest();
            Location loc = req.location();
            if (loc != null) {
                try {
                    SourceModel sm = sourceManager.sourceForLocation(loc);
                    sm.showBreakpoint(loc.lineNumber(), false);
                    showSourceForLocation(loc);
                } catch (Exception exc) {
                }
            }
        }
        @Override
        public void breakpointResolved(SpecEvent e) {
            BreakpointRequest req = (BreakpointRequest)e.getEventRequest();
            Location loc = req.location();
            try {
                SourceModel sm = sourceManager.sourceForLocation(loc);
                sm.showBreakpoint(loc.lineNumber(), true);
                showSourceForLocation(loc);
            } catch (Exception exc) {
            }
        }
        @Override
        public void breakpointError(SpecErrorEvent e) {
            breakpointDeleted(e);
        }
        @Override
        public void watchpointSet(SpecEvent e) {
        }
        @Override
        public void watchpointDeferred(SpecEvent e) {
        }
        @Override
        public void watchpointDeleted(SpecEvent e) {
        }
        @Override
        public void watchpointResolved(SpecEvent e) {
        }
        @Override
        public void watchpointError(SpecErrorEvent e) {
        }
        @Override
        public void exceptionInterceptSet(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptDeferred(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptDeleted(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptResolved(SpecEvent e) {
        }
        @Override
        public void exceptionInterceptError(SpecErrorEvent e) {
        }
    }
    private void showSourceContext(ThreadReference thread, int index) {
        StackFrame frame = null;
        if (thread != null) {
            try {
                frame = thread.frame(index);
            } catch (IncompatibleThreadStateException e) {}
        }
        if (frame == null) {
            return;
        }
        Location locn = frame.location();
        showSourceForLocation(locn);
    }
    public boolean showSourceForLocation(Location locn) {
        sourceName = null;
        sourceLocn = locn;
        int lineNo = locn.lineNumber();
        if (lineNo != -1) {
            SourceModel source = sourceManager.sourceForLocation(locn);
            if (source != null) {
                showSourceAtLine(source, lineNo-1);
                return true;
            }
        }
        showSourceUnavailable();
        return false;
    }
    public boolean showSourceFile(String fileName) {
        sourceLocn = null;
        File file;
        if (!fileName.startsWith(File.separator)) {
            sourceName = fileName;
            SearchPath sourcePath = sourceManager.getSourcePath();
            file = sourcePath.resolve(fileName);
            if (file == null) {
                showSourceUnavailable();
                return false;
            }
        } else {
            sourceName = null;  
            file = new File(fileName);
        }
        SourceModel source = sourceManager.sourceForFile(file);
        if (source != null) {
            showSource(source);
            return true;
        }
        showSourceUnavailable();
        return false;
    }
    private void showSource(SourceModel model) {
        setViewModel(model);
    }
    private void showSourceAtLine(SourceModel model, int lineNo) {
        setViewModel(model);
        if (model.isActuallySource && (lineNo < model.getSize())) {
            list.setSelectedIndex(lineNo);
            if (lineNo+4 < model.getSize()) {
                list.ensureIndexIsVisible(lineNo+4);  
            }
            list.ensureIndexIsVisible(lineNo);
        }
    }
    private void showSourceUnavailable() {
        SourceModel model = new SourceModel("[Source code is not available]");
        setViewModel(model);
    }
    private void setViewModel(SourceModel model) {
        if (model != sourceModel) {
            list.setModel(model);
            sourceModel = model;
        }
    }
    private class SourceLineRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index,
                                               isSelected, cellHasFocus);
            SourceModel.Line line = (SourceModel.Line)value;
            setText(line.text);
            if (line.hasBreakpoint) {
                setIcon(Icons.stopSignIcon);
            } else if (line.isExecutable()) {
                setIcon(Icons.execIcon);
            } else {
                setIcon(Icons.blankIcon);
            }
            return this;
        }
        @Override
        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();
            return new Dimension(dim.width, dim.height-5);
        }
    }
    private class STMouseListener extends MouseAdapter implements MouseListener {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopupMenu((Component)e.getSource(),
                              e.getX(), e.getY());
            }
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                showPopupMenu((Component)e.getSource(),
                              e.getX(), e.getY());
            }
        }
        private void showPopupMenu(Component invoker, int x, int y) {
            JList list = (JList)invoker;
            int ln = list.getSelectedIndex() + 1;
            SourceModel.Line line =
                (SourceModel.Line)list.getSelectedValue();
            JPopupMenu popup = new JPopupMenu();
            if (line == null) {
                popup.add(new JMenuItem("please select a line"));
            } else if (line.isExecutable()) {
                String className = line.refType.name();
                if (line.hasBreakpoint()) {
                    popup.add(commandItem("Clear Breakpoint",
                                          "clear " + className +
                                          ":" + ln));
                } else {
                    popup.add(commandItem("Set Breakpoint",
                                          "stop at " + className +
                                          ":" + ln));
                }
            } else {
                popup.add(new JMenuItem("not an executable line"));
            }
            popup.show(invoker,
                       x + popup.getWidth()/2, y + popup.getHeight()/2);
        }
        private JMenuItem commandItem(String label, final String cmd) {
            JMenuItem item = new JMenuItem(label);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    interpreter.executeCommand(cmd);
                }
            });
            return item;
        }
    }
}
