class DebugGraphicsInfo {
    Color                flashColor = Color.red;
    int                  flashTime = 100;
    int                  flashCount = 2;
    Hashtable<JComponent, Integer> componentToDebug;
    JFrame               debugFrame = null;
    java.io.PrintStream  stream = System.out;
    void setDebugOptions(JComponent component, int debug) {
        if (debug == 0) {
            return;
        }
        if (componentToDebug == null) {
            componentToDebug = new Hashtable<JComponent, Integer>();
        }
        if (debug > 0) {
            componentToDebug.put(component, Integer.valueOf(debug));
        } else {
            componentToDebug.remove(component);
        }
    }
    int getDebugOptions(JComponent component) {
        if (componentToDebug == null) {
            return 0;
        } else {
            Integer integer = componentToDebug.get(component);
            return integer == null ? 0 : integer.intValue();
        }
    }
    void log(String string) {
        stream.println(string);
    }
}
