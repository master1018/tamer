public class ChoiceFocus {
    static Robot robot;
    volatile static boolean keyPressed = false;
    volatile static boolean keyReleased = false;
    volatile static boolean keyTyped = false;
    public static void main(String[] args) {
        Frame f = new Frame("Test Frame");
        f.setLayout(new GridLayout());
        Choice c1 = new Choice();
        c1.add("Choice 1, Item 1");
        c1.add("Choice 1, Item 2");
        Choice c2 = new Choice();
        c2.add("Choice 2, Item 1");
        c2.add("Choice 2, Item 2");
        c1.addKeyListener(new KeyListener(){
                public void keyPressed(KeyEvent e){
                    System.out.println("Key Pressed Event "+e);
                    keyPressed = true;
                }
                public void keyReleased(KeyEvent e){
                    System.out.println("Key Released Event "+e);
                    keyReleased = true;
                }
                public void keyTyped(KeyEvent e){
                    System.out.println("Key Typed Event "+e);
                    keyTyped = true;
                }
            });
        f.add(c1);
        f.add(c2);
        f.pack();
        f.setVisible(true);
        robot = Util.createRobot();
        robot.setAutoWaitForIdle(true);
        robot.setAutoDelay(50);
        Util.waitForIdle(robot);
        Util.clickOnComp(c1, robot);
        Util.waitForIdle(robot);
        Util.clickOnComp(c1, robot);
        Point pt = c2.getLocationOnScreen();
        robot.mouseMove(pt.x + c2.getWidth()/2, pt.y + c2.getHeight()/2);
        Util.waitForIdle(robot);
        robot.keyPress(KeyEvent.VK_A);
        robot.keyRelease(KeyEvent.VK_A);
        Util.waitForIdle(robot);
        if (!keyPressed || !keyReleased || !keyTyped){
            throw new RuntimeException("Failed. Some of event wasn't come "+keyPressed + " : "+keyReleased+" : "+keyTyped);
        } else {
            System.out.println("Test passed");
        }
    }
}
