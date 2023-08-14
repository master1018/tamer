public class DropdownSelectionListener extends SelectionAdapter {
    private Menu mMenu;
    private ToolItem mDropdown;
    public DropdownSelectionListener(ToolItem item) {
        mDropdown = item;
        mMenu = new Menu(item.getParent().getShell(), SWT.POP_UP);
    }
    public void add(String label) {
        MenuItem item = new MenuItem(mMenu, SWT.NONE);
        item.setText(label);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MenuItem sel = (MenuItem) e.widget;
                mDropdown.setText(sel.getText());
            }
        });
    }
    @Override
    public void widgetSelected(SelectionEvent e) {
        if (e.detail == SWT.ARROW) {
            ToolItem item = (ToolItem) e.widget;
            Rectangle rect = item.getBounds();
            Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
            mMenu.setLocation(pt.x, pt.y + rect.height);
            mMenu.setVisible(true);
        } else {
            Log.d("ddms", mDropdown.getText() + " Pressed");
        }
    }
}
