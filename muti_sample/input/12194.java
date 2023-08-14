public class bug6657138 implements Runnable {
    private static Map<JComponent, Map<String, ComponentUI>> componentMap =
            Collections.synchronizedMap(
            new HashMap<JComponent, Map<String, ComponentUI>>());
    public void run() {
        SunToolkit.createNewAppContext();
        try {
            testUIMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static void testUIMap() throws Exception {
        UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        Set<JComponent> components = componentMap.keySet();
        for (JComponent c : components) {
            Map<String, ComponentUI> uiMap = componentMap.get(c);
            for (UIManager.LookAndFeelInfo laf : lafs) {
                if ("Nimbus".equals(laf.getName())) {
                    continue;
                }
                String className = laf.getClassName();
                UIManager.setLookAndFeel(className);
                ComponentUI ui = UIManager.getUI(c);
                if (ui == null) {
                    throw new RuntimeException("UI is null for " + c);
                }
                if (ui == uiMap.get(laf.getName())) {
                    throw new RuntimeException(
                            "Two AppContexts share the same UI delegate! \n" +
                                    c + "\n" + ui);
                }
                uiMap.put(laf.getName(), ui);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        componentMap.put(new JButton("JButton"),
                new HashMap<String, ComponentUI>());
        componentMap.put(new JToggleButton("JToggleButton"),
                new HashMap<String, ComponentUI>());
        componentMap.put(new JRadioButton("JRadioButton"),
                new HashMap<String, ComponentUI>());
        componentMap.put(new JCheckBox("JCheckBox"),
                new HashMap<String, ComponentUI>());
        componentMap.put(new JCheckBox("JLabel"),
                new HashMap<String, ComponentUI>());
        testUIMap();
        ThreadGroup group = new ThreadGroup("6657138");
        Thread thread = new Thread(group, new bug6657138());
        thread.start();
        thread.join();
    }
}
