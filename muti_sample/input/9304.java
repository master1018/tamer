public class ImageDecoratedDnD extends Applet {
    public void init() {
        this.setLayout(new BorderLayout());
        String[] instructions =
                {
                        "A Frame, which contains a yellow button labeled \"Drag ME!\" and ",
                        "a red panel, will appear below. ",
                        "1. Click on the button and drag to the red panel. ",
                        "2. When the mouse enters the red panel during the drag, the panel ",
                        "should turn yellow. On the systems that supports pictured drag, ",
                        "the image under the drag-cursor should appear (ancor is shifted ",
                        "from top-left corner of the picture inside the picture to 10pt in both dimensions ). ",
                        "In WIN32 systems the image under cursor would be visible ONLY over ",
                        "the drop targets with activated extended OLE D\'n\'D support (that are ",
                        "the desktop and IE ).",
                        "3. Release the mouse button.",
                        "The panel should turn red again and a yellow button labeled ",
                        "\"Drag ME!\" should appear inside the panel. You should be able ",
                        "to repeat this operation multiple times."
                };
        Sysout.createDialogWithInstructions(instructions);
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
