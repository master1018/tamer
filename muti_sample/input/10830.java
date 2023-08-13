public class SwingApplet extends JApplet {
    JButton button;
    private void initUI() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            Logger.getLogger(SwingApplet.class.getName()).
                    log(Level.SEVERE, "Failed to apply Nimbus look and feel", ex);
        }
        getContentPane().setLayout(new FlowLayout());
        button = new JButton("Hello, I'm a Swing Button!");
        getContentPane().add(button);
        getContentPane().doLayout();
    }
    @Override
    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initUI();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(SwingApplet.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(SwingApplet.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void stop() {
        if (button != null) {
            getContentPane().remove(button);
            button = null;
        }
    }
}
