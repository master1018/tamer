public class DeadlockTest1 {
    Frame f = new Frame("Menu Frame");
    DeadlockTest1() {
        MenuBar menubar = new MenuBar();
        Menu file = new Menu("File");
        Menu edit = new Menu("Edit");
        Menu help = new Menu("Help");
        MenuItem open = new MenuItem("Open");
        MenuItem close = new MenuItem("Close");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        file.add(open);
        file.add(close);
        edit.add(copy);
        edit.add(paste);
        menubar.add(file);
        menubar.add(edit);
        menubar.add(help);
        menubar.setHelpMenu(help);
        f.setMenuBar(menubar);
        f.setSize(400,200);
        f.setVisible(true);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException z) {
            throw new RuntimeException(z);
        }
        f.dispose();
     }
    public static void main(String argv[]) {
        new DeadlockTest1();
    }
}
