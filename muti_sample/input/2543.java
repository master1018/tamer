public class SpuriousExitEnter_3 {
    static JFrame frame = new JFrame("SpuriousExitEnter_3_LW");
    static JButton jbutton = new JButton("jbutton");
    static Frame frame1 = new Frame("SpuriousExitEnter_3_HW");
    static Button button1 = new Button("button");
    static EnterExitAdapter frameAdapter;
    static EnterExitAdapter buttonAdapter;
    static Robot r = Util.createRobot();
    public static void testCase(Window w, Component comp) {
        frameAdapter = new EnterExitAdapter(w);
        buttonAdapter = new EnterExitAdapter(comp);
        w.addMouseListener(frameAdapter);
        comp.addMouseListener(buttonAdapter);
        w.setSize(200, 200);
        w.add(comp, BorderLayout.NORTH);
        w.setLocationRelativeTo(null);
        w.setVisible(true);
        Point centerA = new Point(comp.getLocationOnScreen().x + comp.getWidth() / 2,
                                  comp.getLocationOnScreen().y + comp.getHeight() / 2);
        Point centerB = new Point(w.getLocationOnScreen().x + w.getWidth() / 2,
                                  w.getLocationOnScreen().y + w.getHeight() / 2);
        Point centerC_1 = new Point(w.getLocationOnScreen().x + w.getWidth() + 20,  
                                    comp.getLocationOnScreen().y + comp.getHeight() / 2); 
        Point centerC_2 = new Point(w.getLocationOnScreen().x + w.getWidth() / 2,
                                    w.getLocationOnScreen().y + w.getHeight() + 20); 
        Util.pointOnComp(comp, r);
        Util.waitForIdle(r);
        frameAdapter.zeroCounters();
        buttonAdapter.zeroCounters();
        moveBetween(r, centerA, centerB);
        checkEvents(frameAdapter, 1, 1);
        checkEvents(buttonAdapter, 1, 1);
        Util.pointOnComp(comp, r);
        Util.waitForIdle(r);
        frameAdapter.zeroCounters();
        buttonAdapter.zeroCounters();
        moveBetween(r, centerA, centerC_1);
        checkEvents(frameAdapter, 0, 0);
        checkEvents(buttonAdapter, 1, 1);
        Util.pointOnComp(w, r);
        Util.waitForIdle(r);
        frameAdapter.zeroCounters();
        buttonAdapter.zeroCounters();
        moveBetween(r, centerB, centerC_2);
        checkEvents(frameAdapter, 1, 1);
        checkEvents(buttonAdapter, 0, 0);
        w.setVisible(false);
        Util.waitForIdle(r);
    }
    public static void main(String []s)
    {
        testCase(frame, jbutton);
        testCase(frame1, button1);
    }
    private static void moveBetween(Robot r, Point first, Point second) {
        Util.waitForIdle(r);
        Util.mouseMove(r, first, second);
        Util.waitForIdle(r);
        Util.mouseMove(r, second, first);
        Util.waitForIdle(r);
    }
    private static void checkEvents(EnterExitAdapter adapter, int entered, int exited) {
        if (adapter.getEnteredEventCount() != entered ||
            adapter.getExitedEventCount() != exited)
        {
            throw new RuntimeException(adapter.getTarget().getClass().getName()+": incorrect event number: Entered got: " +
                                       adapter.getEnteredEventCount() +" expected : " + entered
                                       + ". Exited got : " + adapter.getExitedEventCount() + " expected : "
                                       + exited);
        }
    }
}
class EnterExitAdapter extends MouseAdapter {
    private Component target;
    private int enteredEventCount = 0;
    private int exitedEventCount = 0;
    public EnterExitAdapter(Component target) {
        this.target = target;
    }
    public Component getTarget(){
        return target;
    }
    public int getEnteredEventCount(){
        return enteredEventCount;
    }
    public int getExitedEventCount(){
        return exitedEventCount;
    }
    public void zeroCounters(){
        System.out.println("Zeroeing on " +target.getClass().getName());
        enteredEventCount = 0;
        exitedEventCount = 0;
    }
    public void mouseEntered(MouseEvent e){
        System.out.println("Entered on " + e.getSource().getClass().getName());
        enteredEventCount ++;
    }
    public void mouseExited(MouseEvent e){
        System.out.println("Exited on " + e.getSource().getClass().getName());
        exitedEventCount ++;
    }
}
