public class NullMenuLabelTest {
    public static void main(String[] args) {
        Frame frame = new Frame("Test Frame");
        frame.pack();
        frame.setVisible(true);
        MenuBar menuBar = new MenuBar();
        frame.setMenuBar(menuBar);
        Menu menu = new Menu(null);
        menuBar.add(menu);
        menu.add(new MenuItem(null));
        frame.setVisible(false);
        frame.dispose();
    }
} 
