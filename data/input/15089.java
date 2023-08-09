public class ScrollOut
{
    public static final void main(String args[])
    {
        final Frame frame = new Frame();
        final List list = new List();
        Robot robot = null;
        for (int i = 0; i < 5; i++){
            list.add("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        }
        frame.add(list);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        try{
            robot = new Robot();
        }catch(AWTException e){
            throw new RuntimeException(e);
        }
        Point from = new Point(list.getLocationOnScreen().x + list.getWidth()/2,
                               list.getLocationOnScreen().y + list.getHeight()/2);
        Point to = new Point(list.getLocationOnScreen().x - 30,
                             from.y);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Util.drag(robot, from, to, InputEvent.BUTTON1_MASK);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        to = new Point(from.x,
                       list.getLocationOnScreen().y - 50);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Util.drag(robot, from, to, InputEvent.BUTTON1_MASK);
    }
}
