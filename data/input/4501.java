public class Test4193384 {
    public static void main(String[] args) {
        test(new Color[] {
                new Color(11, 12, 13),
                new Color(204, 0, 204),
                new Color(0, 51, 51)
        });
    }
    private static void test(Color[] colors) {
        JLabel label = new JLabel("Preview Panel"); 
        JColorChooser chooser = new JColorChooser();
        chooser.setPreviewPanel(label);
        float[] hsb = new float[3];
        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
            if (!color.equals(Color.getHSBColor(hsb[0], hsb[1], hsb[2]))) {
                throw new Error("color conversion is failed");
            }
            if (!color.equals(new JColorChooser(color).getColor())) {
                throw new Error("constructor sets incorrect initial color");
            }
            chooser.setColor(color);
            if (!color.equals(label.getForeground())) {
                throw new Error("a custom preview panel doesn't handle colors");
            }
        }
    }
}
