public class DefaultMenuLayout extends BoxLayout implements UIResource {
    public DefaultMenuLayout(Container target, int axis) {
        super(target, axis);
    }
    public Dimension preferredLayoutSize(Container target) {
        if (target instanceof JPopupMenu) {
            JPopupMenu popupMenu = (JPopupMenu) target;
            sun.swing.MenuItemLayoutHelper.clearUsedClientProperties(popupMenu);
            if (popupMenu.getComponentCount() == 0) {
                return new Dimension(0, 0);
            }
        }
        super.invalidateLayout(target);
        return super.preferredLayoutSize(target);
    }
}
