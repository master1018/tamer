public class bug6612531 {
    public static void main(String[] args) {
        ScrollPaneLayout c = new ScrollPaneLayout();
        JViewport vp = new JViewport();
        c.addLayoutComponent("VIEWPORT", vp);
        c.preferredLayoutSize(new JScrollPane());
        System.out.println("Test passed");
    }
}
