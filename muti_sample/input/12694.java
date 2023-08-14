public class CheckZOrderChange {
    private static Button content[] = new Button[]{new Button("Button 1"), new Button("Button 2"), new Button("Button 3"), new Button("Button 4")};
    private static Frame frame;
    public static void main(String[] args) {
        frame = new Frame("Test Frame");
        frame.setLayout(new FlowLayout());
        for (Button b: content){
            frame.add(b);
        }
        frame.setSize(300, 300);
        frame.setVisible(true);
        for (Button b: content){
            System.out.println("frame.getComponentZOrder("+ b +") = " + frame.getComponentZOrder(b));
        }
        frame.setComponentZOrder(content[0], 2);
        System.out.println("ZOrder of button1 changed to 2");
        if (frame.getComponentZOrder(content[0]) != 2 ||
            frame.getComponentZOrder(content[1]) != 0 ||
            frame.getComponentZOrder(content[2]) != 1 ||
            frame.getComponentZOrder(content[3]) != 3)
        {
            for (Button b: content){
                System.out.println("frame.getComponentZOrder("+ b +") = " + frame.getComponentZOrder(b));
            }
            throw new RuntimeException("TEST FAILED: getComponentZOrder did not return the correct value");
        }
    }
}
