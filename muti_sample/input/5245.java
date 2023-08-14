public class Revalidate {
    private static Frame frame = new Frame();
    private static Panel panel = new Panel() {
        @Override
        public boolean isValidateRoot() {
            return true;
        }
    };
    private static Button button = new Button("Test");
    private static void sleep() {
        try { Thread.sleep(500); } catch (Exception e) {}
    }
    private static void printState(String str) {
        System.out.println(str + " isValid state: ");
        System.out.println("         frame: " + frame.isValid());
        System.out.println("         panel: " + panel.isValid());
        System.out.println("        button: " + button.isValid());
    }
    private static void fail(String msg) {
        frame.dispose();
        throw new RuntimeException(msg);
    }
    private static void check(String n, Component c, boolean v) {
        if (c.isValid() != v) {
            fail(n + ".isValid() = " + c.isValid() + ";   expected: " + v);
        }
    }
    private static void check(String str, boolean f, boolean p, boolean b) {
        printState(str);
        check("frame", frame, f);
        check("panel", panel, p);
        check("button", button, b);
    }
    public static void main(String[] args) {
        frame.add(panel);
        panel.add(button);
        frame.setBounds(200, 200, 300, 200);
        frame.setVisible(true);
        sleep();
        check("Upon showing", true, true, true);
        button.setBounds(1, 1, 30, 30);
        sleep();
        check("button.setBounds():", true, false, false);
        button.revalidate();
        sleep();
        check("button.revalidate():", true, true, true);
        button.setBounds(1, 1, 30, 30);
        sleep();
        check("button.setBounds():", true, false, false);
        panel.revalidate();
        sleep();
        check("panel.revalidate():", true, false, false);
        button.revalidate();
        sleep();
        check("button.revalidate():", true, true, true);
        panel.setBounds(2, 2, 125, 130);
        sleep();
        check("panel.setBounds():", false, false, true);
        button.revalidate();
        sleep();
        check("button.revalidate():", false, true, true);
        panel.setBounds(3, 3, 152, 121);
        sleep();
        check("panel.setBounds():", false, false, true);
        panel.revalidate();
        sleep();
        check("panel.revalidate():", true, true, true);
        frame.dispose();
    }
}
