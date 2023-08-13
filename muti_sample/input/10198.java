public class F10TopToplevel extends Applet
{
    Frame frame;
    Dialog dialog;
    volatile boolean menuToggled = false;
    public void init()
    {
        setLayout (new BorderLayout ());
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        MenuBar mb;
        Menu menu;
        MenuItem item;
        frame = new Frame("am below");
        frame.setMenuBar( (mb=new MenuBar()) );
        menu = new Menu("nu");
        menu.add((item = new MenuItem("item")));
        item.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
                menuToggled = true;
            }
        });
        mb.add(menu);
        frame.setSize(200,200);
        frame.setLocation( 400,100 );
        frame.setVisible( true );
        dialog = new Dialog(frame);
        dialog.setSize( 100,100 );
        dialog.setVisible(true);
        Robot robot;
        try {
            robot = new Robot();
        } catch(AWTException e){
            throw new RuntimeException("cannot create robot.", e);
        }
        ((sun.awt.SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        robot.mouseMove(dialog.getLocationOnScreen().x + dialog.getWidth()/2,
                        dialog.getLocationOnScreen().y + dialog.getHeight()/2 );
        robot.delay(5);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(5);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(5);
        robot.keyPress(KeyEvent.VK_F10);
        robot.delay(5);
        robot.keyRelease(KeyEvent.VK_F10);
        robot.delay(5);
        robot.delay(10);
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.delay(5);
        robot.keyRelease(KeyEvent.VK_ENTER);
        robot.delay(5);
        ((sun.awt.SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        if(menuToggled) {
            throw new RuntimeException("Oops! Menu should not open.");
        }
    }
}
