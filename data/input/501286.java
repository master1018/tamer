public class ContextMenuBuilder extends MenuBuilder implements ContextMenu {
    public ContextMenuBuilder(Context context) {
        super(context);
    }
    public ContextMenu setHeaderIcon(Drawable icon) {
        return (ContextMenu) super.setHeaderIconInt(icon);
    }
    public ContextMenu setHeaderIcon(int iconRes) {
        return (ContextMenu) super.setHeaderIconInt(iconRes);
    }
    public ContextMenu setHeaderTitle(CharSequence title) {
        return (ContextMenu) super.setHeaderTitleInt(title);
    }
    public ContextMenu setHeaderTitle(int titleRes) {
        return (ContextMenu) super.setHeaderTitleInt(titleRes);
    }
    public ContextMenu setHeaderView(View view) {
        return (ContextMenu) super.setHeaderViewInt(view);
    }
    public MenuDialogHelper show(View originalView, IBinder token) {
        if (originalView != null) {
            originalView.createContextMenu(this);
        }
        if (getVisibleItems().size() > 0) {
            EventLog.writeEvent(50001, 1);
            MenuDialogHelper helper = new MenuDialogHelper(this); 
            helper.show(token);
            return helper;
        }
        return null;
    }
}
