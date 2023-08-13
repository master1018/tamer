public class Test7048204 {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                new JLabel();
                UIDefaults uid = UIManager.getDefaults();
                uid.putDefaults(new Object[0]);
                uid.put("what.ever", "else");
            }
        });
    }
}
