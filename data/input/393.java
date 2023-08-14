public class Test7034614 {
    public static void main(String[] args) {
        Graphics g = new BufferedImage(9, 9, 9).getGraphics();
        BrokenBorder broken = new BrokenBorder();
        TitledBorder titled = new TitledBorder(broken, broken.getClass().getName());
        Insets insets = (Insets) broken.getBorderInsets(broken).clone();
        titled.getBorderInsets(broken);
        broken.validate(insets);
        for (int i = 0; i < 10; i++) {
            titled.paintBorder(broken, g, 0, 0, i, i);
            broken.validate(insets);
            titled.getBaseline(broken, i, i);
            broken.validate(insets);
        }
    }
    private static class BrokenBorder extends Component implements Border {
        private Insets insets = new Insets(1, 2, 3, 4);
        private void validate(Insets insets) {
            if (!this.insets.equals(insets)) {
                throw new Error("unexpected change");
            }
        }
        public Insets getBorderInsets(Component c) {
            return this.insets;
        }
        public boolean isBorderOpaque() {
            return false;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        }
    }
}
