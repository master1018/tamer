public final class StackTracePanel {
    private static ISourceRevealer sSourceRevealer;
    private Table mStackTraceTable;
    private TableViewer mStackTraceViewer;
    private Client mCurrentClient;
    private static class StackTraceContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IStackTraceInfo) {
                StackTraceElement trace[] = ((IStackTraceInfo)inputElement).getStackTrace();
                if (trace != null) {
                    return trace;
                }
            }
            return new Object[0];
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    private static class StackTraceLabelProvider implements ITableLabelProvider {
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof StackTraceElement) {
                StackTraceElement traceElement = (StackTraceElement)element;
                switch (columnIndex) {
                    case 0:
                        return traceElement.getClassName();
                    case 1:
                        return traceElement.getMethodName();
                    case 2:
                        return traceElement.getFileName();
                    case 3:
                        return Integer.toString(traceElement.getLineNumber());
                    case 4:
                        return Boolean.toString(traceElement.isNativeMethod());
                }
            }
            return null;
        }
        public void addListener(ILabelProviderListener listener) {
        }
        public void dispose() {
        }
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }
        public void removeListener(ILabelProviderListener listener) {
        }
    }
    public interface ISourceRevealer {
        public void reveal(String applicationName, String className, int line);
    }
    public static void setSourceRevealer(ISourceRevealer revealer) {
        sSourceRevealer = revealer;
    }
    public Table createPanel(Composite parent, String prefs_stack_col_class,
            String prefs_stack_col_method, String prefs_stack_col_file, String prefs_stack_col_line,
            String prefs_stack_col_native, IPreferenceStore store) {
        mStackTraceTable = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION);
        mStackTraceTable.setHeaderVisible(true);
        mStackTraceTable.setLinesVisible(true);
        TableHelper.createTableColumn(
                mStackTraceTable,
                "Class",
                SWT.LEFT,
                "SomeLongClassName", 
                prefs_stack_col_class, store);
        TableHelper.createTableColumn(
                mStackTraceTable,
                "Method",
                SWT.LEFT,
                "someLongMethod", 
                prefs_stack_col_method, store);
        TableHelper.createTableColumn(
                mStackTraceTable,
                "File",
                SWT.LEFT,
                "android/somepackage/someotherpackage/somefile.class", 
                prefs_stack_col_file, store);
        TableHelper.createTableColumn(
                mStackTraceTable,
                "Line",
                SWT.RIGHT,
                "99999", 
                prefs_stack_col_line, store);
        TableHelper.createTableColumn(
                mStackTraceTable,
                "Native",
                SWT.LEFT,
                "Native", 
                prefs_stack_col_native, store);
        mStackTraceViewer = new TableViewer(mStackTraceTable);
        mStackTraceViewer.setContentProvider(new StackTraceContentProvider());
        mStackTraceViewer.setLabelProvider(new StackTraceLabelProvider());
        mStackTraceViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                if (sSourceRevealer != null && mCurrentClient != null) {
                    ISelection selection = mStackTraceViewer.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
                        Object object = structuredSelection.getFirstElement();
                        if (object instanceof StackTraceElement) {
                            StackTraceElement traceElement = (StackTraceElement)object;
                            if (traceElement.isNativeMethod() == false) {
                                sSourceRevealer.reveal(
                                        mCurrentClient.getClientData().getClientDescription(), 
                                        traceElement.getClassName(),
                                        traceElement.getLineNumber());
                            }
                        }
                    }
                }
            }
        });
        return mStackTraceTable;
    }
    public void setViewerInput(IStackTraceInfo input) {
        mStackTraceViewer.setInput(input);
        mStackTraceViewer.refresh();
    }
    public void setCurrentClient(Client currentClient) {
        mCurrentClient = currentClient;
    }
}
