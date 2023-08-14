class OpenFilePanel extends JComponent {
    private BufferedImage dropHere;
    OpenFilePanel(MainFrame mainFrame) {
        setOpaque(false);
        loadSupportImage();
        setTransferHandler(new ImageTransferHandler(mainFrame));
    }
    private void loadSupportImage() {
        try {
            URL resource = getClass().getResource("/images/drop.png");
            dropHere = GraphicsUtilities.loadCompatibleImage(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        int x = (getWidth() - dropHere.getWidth()) / 2;
        int y = (getHeight() - dropHere.getHeight()) / 2;
        g.drawImage(dropHere, x, y, null);
    }
}
