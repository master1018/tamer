public class HorizBagLayout implements LayoutManager {
    int hgap;
    public HorizBagLayout() {
        this(0);
    }
    public HorizBagLayout(int hgap) {
        this.hgap = hgap;
    }
    public void addLayoutComponent(String name, Component comp) {
    }
    public void removeLayoutComponent(Component comp) {
    }
    public Dimension minimumLayoutSize(Container target) {
        Dimension dim = new Dimension();
        for (int i = 0; i < target.countComponents(); i++) {
            Component comp = target.getComponent(i);
            if (comp.isVisible()) {
                Dimension d = comp.minimumSize();
                dim.width += d.width + hgap;
                dim.height = Math.max(d.height, dim.height);
            }
        }
        Insets insets = target.insets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;
        return dim;
    }
    public Dimension preferredLayoutSize(Container target) {
        Dimension dim = new Dimension();
        for (int i = 0; i < target.countComponents(); i++) {
            Component comp = target.getComponent(i);
            if (comp.isVisible()) {
                Dimension d = comp.preferredSize();
                dim.width += d.width + hgap;
                dim.height = Math.max(dim.height, d.height);
            }
        }
        Insets insets = target.insets();
        dim.width += insets.left + insets.right;
        dim.height += insets.top + insets.bottom;
        return dim;
    }
    public void layoutContainer(Container target) {
        Insets insets = target.insets();
        int top = insets.top;
        int bottom = target.size().height - insets.bottom;
        int left = insets.left;
        int right = target.size().width - insets.right;
        for (int i = 0; i < target.countComponents(); i++) {
            Component comp = target.getComponent(i);
            if (comp.isVisible()) {
                int compWidth = comp.size().width;
                comp.resize(compWidth, bottom - top);
                Dimension d = comp.preferredSize();
                comp.reshape(left, top, d.width, bottom - top);
                left += d.width + hgap;
            }
        }
    }
    public String toString() {
        return getClass().getName() + "[hgap=" + hgap + "]";
    }
}
