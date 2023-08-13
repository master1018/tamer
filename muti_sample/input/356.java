public class bug4331767
{
    private static boolean passed = false;
    public static void main(String[] argv) {
        try {
            UIManager.setLookAndFeel(new BrokenLookAndFeel());
        } catch (Exception e) {
            throw new Error("Failed to set BrokenLookAndFeel, cannot test", e);
        }
        new JButton();
        if (!passed) {
            throw new RuntimeException("Failed: Custom getUIError() not called");
        }
    }
    static class BrokenUIDefaults extends UIDefaults {
        UIDefaults defaults;
        public BrokenUIDefaults(UIDefaults def) {
            defaults = def;
        }
        public Object get(Object key) {
            if ("ButtonUI".equals(key)) {
                System.err.println("[II]  Called BrokenUIDefaults.get(Object)");
                return "a nonexistent class";
            }
            return defaults.get(key);
        }
        public Object get(Object key, Locale l) {
            if ("ButtonUI".equals(key)) {
                System.err.println("[II]  Called BrokenUIDefaults.get(Object, Locale)");
                return "a nonexistent class";
            }
            return defaults.get(key, l);
        }
        protected void getUIError(String msg) {
            System.err.println("[II]  BrokenUIDefaults.getUIError() called, test passes");
            passed = true;
        }
    }
    static class BrokenLookAndFeel extends MetalLookAndFeel {
        UIDefaults defaults;
        public BrokenLookAndFeel() {
            defaults = new BrokenUIDefaults(super.getDefaults());
        }
        public UIDefaults getDefaults() {
            return defaults;
        }
    }
}
