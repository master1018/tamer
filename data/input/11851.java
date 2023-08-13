public class EventTimeInFuture {
    public static void main(String []s) {
        Frame frame = new SensibleFrame();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Robot robot = Util.createRobot();
        Util.waitForIdle(robot);
        Point start = new Point(frame.getLocationOnScreen().x - frame.getWidth()/5,
                                frame.getLocationOnScreen().y - frame.getHeight()/5);
        Point end = new Point(frame.getLocationOnScreen().x + frame.getWidth() * 6 / 5,
                              frame.getLocationOnScreen().y + frame.getHeight() * 6 / 5);
        Sysout.println("start = " + start);
        Sysout.println("end = " + end);
        Util.mouseMove(robot, start, end);
        start = new Point(frame.getLocationOnScreen().x + frame.getWidth()/2,
                          frame.getLocationOnScreen().y + frame.getHeight()/2);
        end = new Point(frame.getLocationOnScreen().x + frame.getWidth() * 6 / 5,
                        frame.getLocationOnScreen().y + frame.getHeight() * 6 / 5);
        Util.drag(robot, start, end, MouseEvent.BUTTON1_MASK);
    }
}
class SensibleFrame extends Frame implements MouseListener,
    MouseMotionListener{
    public SensibleFrame(){
        super("Is event time in future");
        setPreferredSize(new Dimension(100,100));
        setBackground(Color.white);
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    private void traceMouse(String k, MouseEvent e){
        long eventTime = e.getWhen();
        long currTime = System.currentTimeMillis();
        long diff = currTime - eventTime;
        Sysout.println(k + " diff is " + diff + ", event is "+ e);
        if (diff < 0){
            AbstractTest.fail(k + " diff is " + diff + ", event = "+e);
        }
    }
    public void mouseMoved(MouseEvent e){
        traceMouse("moved",e);
    }
    public void mouseEntered(MouseEvent e){
        traceMouse("entered",e);
    }
    public void mouseExited(MouseEvent e){
        traceMouse("exited",e);
    }
    public void mouseClicked(MouseEvent e){
        traceMouse("clicked",e);
    }
    public void mousePressed(MouseEvent e){
        traceMouse("pressed",e);
    }
    public void mouseReleased(MouseEvent e){
        traceMouse("released",e);
    }
    public void mouseDragged(MouseEvent e){
        traceMouse("dragged",e);
    }
}
