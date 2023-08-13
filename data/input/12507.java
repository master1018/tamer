public class ValidateOnShow {
    private static Dialog dialog = new Dialog((Frame)null);
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
    private static void test() {
        System.out.println("Before showing: panel.isValid=" + panel.isValid() + "      dialog.isValid=" + dialog.isValid());
        dialog.setVisible(true);
        sleep();
        System.out.println("After showing:  panel.isValid=" + panel.isValid() + "      dialog.isValid=" + dialog.isValid());
        if (!panel.isValid()) {
            dialog.dispose();
            throw new RuntimeException("The panel hasn't been validated upon showing the dialog");
        }
        dialog.setVisible(false);
        sleep();
    }
    public static void main(String[] args) {
        dialog.add(panel);
        panel.add(button);
        dialog.setBounds(200, 200, 300, 200);
        test();
        button.setBounds(1, 1, 30, 30);
        sleep();
        test();
        dialog.dispose();
    }
}
