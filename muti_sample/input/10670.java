public class bug6456844 {
    public static void main(String[] args) {
        JEditorPane ep = new JEditorPane();
        ep.setContentType("text/html");
        ep.setText("<html><body>abc</body></html>");
        ep.setBorder(new EmptyBorder(20, 20, 20, 20));
        ep.setBounds(0, 0, 100, 100);
        JTextComponent.DropLocation location =
                (JTextComponent.DropLocation) SwingAccessor.getJTextComponentAccessor().dropLocationForPoint(ep,
                        new Point(0, 0));
        if (location.getBias() == null) {
            throw new RuntimeException("null bias");
        }
    }
}
