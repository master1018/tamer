public class Test6788531 {
    public static void main(String[] args) throws Exception {
        JButton button = new JButton("hi");
        button.addActionListener(EventHandler.create(ActionListener.class, new Private(), "run"));
        button.addActionListener(EventHandler.create(ActionListener.class, new PrivateGeneric(), "run", "actionCommand"));
        button.doClick();
    }
    public static class Public {
        public void run() {
            throw new Error("method is overridden");
        }
    }
    static class Private extends Public {
        public void run() {
            System.out.println("default");
        }
    }
    public static class PublicGeneric<T> {
        public void run(T object) {
            throw new Error("method is overridden");
        }
    }
    static class PrivateGeneric extends PublicGeneric<String> {
        public void run(String string) {
            System.out.println(string);
        }
    }
}
