public class bug6794836 {
    public static void main(String[] args) throws Exception {
        new bug6794836().run();
    }
    public void run() throws Exception {
        JSlider slider = new JSlider(0, Integer.MAX_VALUE);
        slider.setPaintLabels(true);
        JLabel minLabel = new JLabel("Min");
        JLabel maxLabel = new JLabel("Max");
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        labelTable.put(Integer.MIN_VALUE, minLabel);
        labelTable.put(Integer.MAX_VALUE, maxLabel);
        slider.setLabelTable(labelTable);
        BasicSliderUI ui = (BasicSliderUI) slider.getUI();
        if (invokeMethod("getHighestValueLabel", ui) != maxLabel) {
            fail("invalid getHighestValueLabel result");
        }
        if (invokeMethod("getLowestValueLabel", ui) != minLabel) {
            fail("invalid getLowestValueLabel result");
        }
        System.out.println("The bug6794836 test passed");
    }
    private static Object invokeMethod(String name, BasicSliderUI ui) throws Exception {
        Method method = BasicSliderUI.class.getDeclaredMethod(name, null);
        method.setAccessible(true);
        return method.invoke(ui, null);
    }
    private static void fail(String s) {
        throw new RuntimeException("Test failed: " + s);
    }
}
