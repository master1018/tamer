public class MouseClickTest extends Applet
{
    private static final int TIMEOUT = 3000;
    public void init()
    {
        setLayout (new BorderLayout ());
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
        runTests();
    }
    public static final void main(String args[])
    {
        MouseClickTest test = new MouseClickTest();
        test.init();
        test.start();
    }
    void runTests()
    {
        Frame frame = new Frame("frame");
        frame.setBounds(100, 100, 100, 100);
        frame.setVisible(true);
        frame.validate();
        Util.waitTillShown(frame);
        Robot robot = Util.createRobot();
        robot.setAutoDelay(10);
        oneButtonPressRelease(frame, robot, false, 1);
        oneButtonPressRelease(frame, robot, true, 0);
        twoButtonPressRelease(frame, robot, false, 2);
        twoButtonPressRelease(frame, robot, true, 1);
    }
    public static void oneButtonPressRelease(final Component comp, final Robot robot,
                                             final boolean dragging, final int EXPECTED_EVENT_COUNT)
    {
        final AtomicInteger eventCount = new AtomicInteger(0);
        final MouseAdapter adapter = new MouseEventAdapter(eventCount, EXPECTED_EVENT_COUNT);
        comp.addMouseListener(adapter);
        Rectangle bounds = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
        robot.mouseMove(bounds.x + bounds.width / 4, bounds.y + bounds.height / 2);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        if (dragging) {
            robot.mouseMove(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(robot);
        waitForCondition(eventCount, EXPECTED_EVENT_COUNT, TIMEOUT);
        if (eventCount.get() != EXPECTED_EVENT_COUNT) {
            System.out.println("Wrong number of MouseClick events were generated: " +
                               eventCount.get() + ", expected: " + EXPECTED_EVENT_COUNT);
            throw new RuntimeException("Test failed!");
        }
        comp.removeMouseListener(adapter);
        System.out.println("Test passed.");
    }
    public static void twoButtonPressRelease(final Component comp, final Robot robot,
                                             final boolean dragging, final int EXPECTED_EVENT_COUNT)
    {
        final AtomicInteger eventCount = new AtomicInteger(0);
        final MouseAdapter adapter = new MouseEventAdapter(eventCount, EXPECTED_EVENT_COUNT);
        comp.addMouseListener(adapter);
        Rectangle bounds = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
        robot.mouseMove(bounds.x + bounds.width / 4, bounds.y + bounds.height / 2);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        if (dragging) {
            robot.mouseMove(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        }
        robot.mousePress(InputEvent.BUTTON2_MASK);
        robot.mouseRelease(InputEvent.BUTTON2_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(robot);
        waitForCondition(eventCount, EXPECTED_EVENT_COUNT, TIMEOUT);
        if (eventCount.get() != EXPECTED_EVENT_COUNT) {
            System.out.println("Wrong number of MouseClick events were generated: " +
                               eventCount.get() + ", expected: " + EXPECTED_EVENT_COUNT);
            throw new RuntimeException("Test failed!");
        }
        comp.removeMouseListener(adapter);
        System.out.println("Test passed.");
    }
    private static void waitForCondition(final AtomicInteger eventCount, int EXPECTED_EVENT_COUNT,
                                         long timeout)
    {
        synchronized (eventCount) {
            if (eventCount.get() != EXPECTED_EVENT_COUNT) {
                try {
                    eventCount.wait(timeout);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted unexpectedly!");
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
class MouseEventAdapter extends MouseAdapter {
    private final int EXPECTED_EVENT_COUNT;
    private final AtomicInteger eventCount;
    public MouseEventAdapter(final AtomicInteger eventCount, final int EXPECTED_EVENT_COUNT) {
        this.EXPECTED_EVENT_COUNT = EXPECTED_EVENT_COUNT;
        this.eventCount = eventCount;
    }
    public void mouseClicked(MouseEvent e) {
        System.out.println(e);
        synchronized (eventCount) {
            if (eventCount.incrementAndGet() == EXPECTED_EVENT_COUNT) {
                eventCount.notifyAll();
            }
        }
    }
}
