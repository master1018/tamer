public class CombinedTestApp1
{
    public static void main(String[] args)
    {
        Frame frame = new Frame("A test");
        frame.setSize (200,200);
        frame.setLayout(new FlowLayout());
        MenuBar mbar = new MenuBar();
        Menu file = new Menu("File");
        mbar.add(file);
        MenuItem quit = new MenuItem("Quit", new MenuShortcut(KeyEvent.VK_Q));
        quit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { System.exit(0);}
            });
        file.add(quit);
        frame.setMenuBar(mbar);
        frame.add(new Button("One"));
        frame.add(new Button("Two"));
        frame.add(new Button("Three"));
        frame.pack();
        frame.setVisible(true);
        try {
            Robot robot = new Robot();
            Util.waitForIdle(robot);
        } catch(AWTException e){
            throw new RuntimeException("Test interrupted.", e);
        }
        Insets frameInsets = frame.getInsets();
        System.out.println(frameInsets);
        if (frameInsets.bottom < 0 || frameInsets.top < 0 ||
            frameInsets.right < 0 || frameInsets.left <0){
            throw new RuntimeException("Failed. Frames' Insets are incorrect.");
        }
        System.out.println(" Test Passed. ");
    }
}
