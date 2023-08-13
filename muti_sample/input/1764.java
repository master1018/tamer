public class bug6632953 {
    public static void main(String... args) throws Exception {
        MetalComboBoxUI ui = new MetalComboBoxUI();
        ui.installUI(new JComboBox());
        ui.getBaseline(new JComboBox(), 0, 0);
        ui.getBaseline(new JComboBox(), 1, 1);
        ui.getBaseline(new JComboBox(), 2, 2);
        ui.getBaseline(new JComboBox(), 3, 3);
        ui.getBaseline(new JComboBox(), 4, 4);
    }
}
