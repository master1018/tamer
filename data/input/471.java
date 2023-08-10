public final class ToggleMagnification implements ActionListener {
    private MagnifierInterface magnifier = MagnifierFactory.getDefaultMagnifier();
    public void actionPerformed(ActionEvent e) {
        if (magnifier.isStarted()) {
            magnifier.stop();
            MagnificationOptions.setMagnificationEnabled(false);
        } else {
            magnifier.start();
            magnifier.setFullScreen(MagnificationOptions.isFullscreenEnabled());
            if (!MagnificationOptions.isFullscreenEnabled()) magnifier.setSize(MagnificationOptions.getMagnifierHeight(), MagnificationOptions.getMagnifierWidth());
            MagnificationOptions.setMagnificationEnabled(true);
        }
    }
}
