public class CtorTest
{
    public static void main(String []s) throws Exception
    {
        GraphicsDevice graphicsDevice = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Robot robot = new Robot(graphicsDevice);
        clickOnFrame(robot);
    }
    private static void clickOnFrame(Robot robot) {
        Frame frame = new Frame();
        frame.setBounds(100, 100, 100, 100);
        frame.setVisible(true);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        robot.mouseMove(150, 150);
        robot.delay(50);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
    }
}
