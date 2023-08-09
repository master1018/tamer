class GradientPanel extends JPanel {
    private static final int DARK_BLUE = 0x202737;
    GradientPanel() {
        super(new BorderLayout());
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Rectangle clip = g2.getClipBounds();
        Paint paint = g2.getPaint();
        g2.setPaint(new GradientPaint(0.0f, getHeight() * 0.22f, new Color(DARK_BLUE),
                                      0.0f, getHeight() * 0.9f, Color.BLACK));
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setPaint(paint);
    }
}
