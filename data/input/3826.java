public class DisabledComponent
{
    private static volatile boolean passed = true;
    public static void main(String []s) throws Exception
    {
        Frame frame = new Frame();
        frame.setBounds(100,100,400,400);
        frame.setLayout(new FlowLayout());
        TextArea textArea = new TextArea("TextArea", 6, 15);
        frame.add(textArea);
        List list = new List(3);
        list.add("1");
        list.add("2");
        list.add("3");
        frame.add(list);
        MouseWheelListener listener = new MouseWheelListener(){
            @Override
                public void mouseWheelMoved(MouseWheelEvent mwe){
                    System.err.println(mwe);
                    passed = false;
                }
            };
        list.addMouseWheelListener(listener);
        textArea.addMouseWheelListener(listener);
        frame.setVisible(true);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        Robot robot = new Robot();
        Util.pointOnComp(list, robot);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        robot.mouseWheel(2);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        System.err.println(" disable text area ");
        textArea.setEnabled(false);
        passed = true;
        Util.pointOnComp(textArea, robot);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        robot.mouseWheel(2);
        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
        if (!passed) {
            throw new RuntimeException(" wrong wheel events ");
        }
    }
}
