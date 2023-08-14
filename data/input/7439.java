public class bug6918861 {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new SynthLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JSlider slider = new JSlider();
                HackedSynthSliderUI ui = new HackedSynthSliderUI(slider);
                slider.setUI(ui);
                if (ui.counter != 111) {
                    throw new RuntimeException("Some installers of SynthSliderUI weren't invoked");
                }
                slider.setUI(null);
                if (ui.counter != 0) {
                    throw new RuntimeException("Some uninstallers of SynthSliderUI weren't invoked");
                }
            }
        });
    }
    private static class HackedSynthSliderUI extends SynthSliderUI {
        private int counter;
        protected HackedSynthSliderUI(JSlider c) {
            super(c);
        }
        protected void installDefaults(JSlider slider) {
            super.installDefaults(slider);
            counter += 1;
        }
        protected void uninstallDefaults(JSlider slider) {
            super.uninstallDefaults(slider);
            counter -= 1;
        }
        protected void installListeners(JSlider slider) {
            super.installListeners(slider);
            counter += 10;
        }
        protected void uninstallListeners(JSlider slider) {
            super.uninstallListeners(slider);
            counter -= 10;
        }
        protected void installKeyboardActions(JSlider slider) {
            super.installKeyboardActions(slider);
            counter += 100;
        }
        protected void uninstallKeyboardActions(JSlider slider) {
            super.uninstallKeyboardActions(slider);
            counter -= 100;
        }
    }
}
