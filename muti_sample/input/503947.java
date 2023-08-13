public class ToolItemAction implements ICommonAction {
    public ToolItem item;
    public ToolItemAction(ToolBar parent, int style) {
        item = new ToolItem(parent, style);
    }
    public void setChecked(boolean checked) {
        item.setSelection(checked);
    }
    public void setEnabled(boolean enabled) {
        item.setEnabled(enabled);
    }
    public void setRunnable(final Runnable runnable) {
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                runnable.run();
            }
        });
    }
}
