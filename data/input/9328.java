public class bug6278700 {
    private int changeCount;
    private final ChangeListener listener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            changeCount++;
        }
    };
    public static void main(String[] args) {
        new bug6278700();
    }
    public bug6278700() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JSlider slider = new JSlider(new DefaultBoundedRangeModel(5, 0, 0, 10));
                slider.addChangeListener(listener);
                slider.setValue(0);
                if (changeCount != 1) {
                    throw new RuntimeException("Incorrect stateChanged count: " + Integer.toString(changeCount));
                }
                changeCount = 0;
                slider = new JSlider();
                slider.addChangeListener(listener);
                slider.setValue(0);
                if (changeCount != 1) {
                    throw new RuntimeException("Incorrect stateChanged count: " + Integer.toString(changeCount));
                }
            }
        });
    }
}
