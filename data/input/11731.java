public class Button2DragTest extends Applet {
    private volatile boolean dropSuccess;
    private Frame frame;
    public void init() {
        setLayout(new BorderLayout());
        frame = new Frame();
        final DragSourceListener dragSourceListener = new DragSourceAdapter() {
            public void dragDropEnd(DragSourceDropEvent e) {
                dropSuccess = e.getDropSuccess();
                System.err.println("Drop was successful: " + dropSuccess);
            }
        };
        DragGestureListener dragGestureListener = new DragGestureListener() {
            public void dragGestureRecognized(DragGestureEvent dge) {
                dge.startDrag(null, new StringSelection("OK"), dragSourceListener);
            }
        };
        new DragSource().createDefaultDragGestureRecognizer(frame, DnDConstants.ACTION_MOVE,
                                                            dragGestureListener);
        DropTargetAdapter dropTargetListener = new DropTargetAdapter() {
            public void drop(DropTargetDropEvent dtde) {
                dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                dtde.dropComplete(true);
                System.err.println("Drop");
            }
        };
        new DropTarget(frame, dropTargetListener);
    }
    public void start() {
        setSize(200,200);
        setVisible(true);
        validate();
        frame.setBounds(100, 100, 200, 200);
        frame.setVisible(true);
        Robot robot = Util.createRobot();
        Util.waitForIdle(robot);
        Point startPoint = frame.getLocationOnScreen();
        Point endPoint = new Point(startPoint);
        startPoint.translate(50, 50);
        endPoint.translate(150, 150);
        Util.drag(robot, startPoint, endPoint, InputEvent.BUTTON2_MASK);
        Util.waitForIdle(robot);
        if (dropSuccess) {
            System.err.println("test passed");
        } else {
            throw new RuntimeException("test failed: drop was not successful");
        }
    }
}
