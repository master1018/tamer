public class ImageDecoratedDnDNegative extends Applet {
    public void init() {
        this.setLayout(new BorderLayout());
        String[] instructions =
                {
                        "Automatic test.",
                        "A Frame, which contains a yellow button labeled \"Drag ME!\" and ",
                        "a red panel, will appear below. ",
                        "1. The button would be clicked and dragged to the red panel. ",
                        "2. When the mouse enters the red panel during the drag, the panel ",
                        "should turn yellow. On the systems that supports pictured drag, ",
                        "the image under the drag-cursor should appear (ancor is shifted ",
                        "from top-left corner of the picture inside the picture to 10pt in both dimensions ). ",
                        "In WIN32 systems the image under cursor would be visible ONLY over ",
                        "the drop targets with activated extended OLE D\'n\'D support (that are ",
                        "the desktop and IE ).",
                        "3. The mouse would be released.",
                        "The panel should turn red again and a yellow button labeled ",
                        "\"Drag ME!\" should appear inside the panel. You should be able ",
                        "to repeat this operation multiple times."
                };
        Sysout.createDialogWithInstructions(instructions);
    }
    public void moveTo(
        Robot r,
        Point b,
        Point e)
    {
        Point2D.Double ee = new Point2D.Double(e.getX(), e.getY());
        Point2D.Double bb = new Point2D.Double(b.getX(), b.getY());
        final int count = (int)(ee.distance(bb));
        Point2D.Double c = new Point2D.Double(bb.getX(), bb.getY());
        for(int i=0; i<count; ++i){
            c.setLocation(
                    bb.getX() + (ee.getX()-bb.getX())*i/count,
                    bb.getY() + (ee.getY()-bb.getY())*i/count);
            r.mouseMove(
                    (int)c.getX(),
                    (int)c.getY());
            r.delay(5);
        }
        r.mouseMove(
                (int)ee.getX(),
                (int)ee.getY());
        r.delay(5);
    }
    public void start() {
        Frame f = new Frame("Use keyboard for DnD change");
        Panel mainPanel;
        Component dragSource, dropTarget;
        f.setBounds(0, 400, 200, 200);
        f.setLayout(new BorderLayout());
        mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.blue);
        dropTarget = new DnDTarget(Color.red, Color.yellow);
        dragSource = new DnDSource("Drag ME! (" + (DragSource.isDragImageSupported()?"with ":"without") + " image)" );
        mainPanel.add(dragSource, "North");
        mainPanel.add(dropTarget, "Center");
        f.add(mainPanel, BorderLayout.CENTER);
        f.setVisible(true);
        Point sourcePoint = dragSource.getLocationOnScreen();
        Dimension d = dragSource.getSize();
        sourcePoint.translate(d.width / 2, d.height / 2);
        try {
            Robot robot = new Robot();
            robot.mouseMove(sourcePoint.x, sourcePoint.y);
            Point start = new Point(
                    sourcePoint.x,
                    sourcePoint.y);
            Point out = new Point(
                    sourcePoint.x + d.width / 2 + 10,
                    sourcePoint.y + d.height);
            Point cur = start;
            for(int i = 2; i < 5; ++i){
                moveTo(robot, cur, start);
                robot.delay(500);
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.delay(500);
                moveTo(robot, start, out);
                robot.keyPress(KeyEvent.VK_CONTROL);
                Point drop = new Point(
                        (int)start.getX(),
                        (int)start.getY() + (d.height + 5) * i );
                moveTo(robot, out, drop);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                robot.delay(10);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.delay(1000);
                cur = drop;
            }
        } catch( Exception e){
            e.printStackTrace();
            throw new RuntimeException("test failed: drop was not successful with exception " + e);
        }
    }
}
class Sysout {
    private static TestDialog dialog;
    public static void createDialogWithInstructions(String[] instructions) {
        dialog = new TestDialog(new Frame(), "Instructions");
        dialog.printInstructions(instructions);
        dialog.show();
        println("Any messages for the tester will display here.");
    }
    public static void createDialog() {
        dialog = new TestDialog(new Frame(), "Instructions");
        String[] defInstr = {"Instructions will appear here. ", ""};
        dialog.printInstructions(defInstr);
        dialog.show();
        println("Any messages for the tester will display here.");
    }
    public static void printInstructions(String[] instructions) {
        dialog.printInstructions(instructions);
    }
    public static void println(String messageIn) {
        dialog.displayMessage(messageIn);
    }
}
class TestDialog extends Dialog {
    TextArea instructionsText;
    TextArea messageText;
    int maxStringLength = 80;
    public TestDialog(Frame frame, String name) {
        super(frame, name);
        int scrollBoth = TextArea.SCROLLBARS_BOTH;
        instructionsText = new TextArea("", 15, maxStringLength, scrollBoth);
        add("North", instructionsText);
        messageText = new TextArea("", 5, maxStringLength, scrollBoth);
        add("South", messageText);
        pack();
        show();
    }
    public void printInstructions(String[] instructions) {
        instructionsText.setText("");
        String printStr, remainingStr;
        for (int i = 0; i < instructions.length; i++) {
            remainingStr = instructions[i];
            while (remainingStr.length() > 0) {
                if (remainingStr.length() >= maxStringLength) {
                    int posOfSpace = remainingStr.
                            lastIndexOf(' ', maxStringLength - 1);
                    if (posOfSpace <= 0) posOfSpace = maxStringLength - 1;
                    printStr = remainingStr.substring(0, posOfSpace + 1);
                    remainingStr = remainingStr.substring(posOfSpace + 1);
                }
                else {
                    printStr = remainingStr;
                    remainingStr = "";
                }
                instructionsText.append(printStr + "\n");
            }
        }
    }
    public void displayMessage(String messageIn) {
        messageText.append(messageIn + "\n");
    }
}
