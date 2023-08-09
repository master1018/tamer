public class MetalProgressBarUI extends BasicProgressBarUI {
    private Rectangle innards;
    private Rectangle box;
    public static ComponentUI createUI(JComponent c) {
        return new MetalProgressBarUI();
    }
    public void paintDeterminate(Graphics g, JComponent c) {
        super.paintDeterminate(g,c);
        if (!(g instanceof Graphics2D)) {
            return;
        }
        if (progressBar.isBorderPainted()) {
            Insets b = progressBar.getInsets(); 
            int barRectWidth = progressBar.getWidth() - (b.left + b.right);
            int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
            int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
            boolean isLeftToRight = MetalUtils.isLeftToRight(c);
            int startX, startY, endX, endY;
            startX = b.left;
            startY = b.top;
            endX = b.left + barRectWidth - 1;
            endY = b.top + barRectHeight - 1;
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(new BasicStroke(1.f));
            if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
                g2.setColor(MetalLookAndFeel.getControlShadow());
                g2.drawLine(startX, startY, endX, startY);
                if (amountFull > 0) {
                    g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
                    if (isLeftToRight) {
                        g2.drawLine(startX, startY,
                                startX + amountFull - 1, startY);
                    } else {
                        g2.drawLine(endX, startY,
                                endX - amountFull + 1, startY);
                        if (progressBar.getPercentComplete() != 1.f) {
                            g2.setColor(MetalLookAndFeel.getControlShadow());
                        }
                    }
                }
                g2.drawLine(startX, startY, startX, endY);
            } else { 
                g2.setColor(MetalLookAndFeel.getControlShadow());
                g2.drawLine(startX, startY, startX, endY);
                if (amountFull > 0) {
                    g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
                    g2.drawLine(startX, endY,
                            startX, endY - amountFull + 1);
                }
                g2.setColor(MetalLookAndFeel.getControlShadow());
                if (progressBar.getPercentComplete() == 1.f) {
                    g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
                }
                g2.drawLine(startX, startY, endX, startY);
            }
        }
    }
    public void paintIndeterminate(Graphics g, JComponent c) {
        super.paintIndeterminate(g, c);
        if (!progressBar.isBorderPainted() || (!(g instanceof Graphics2D))) {
            return;
        }
        Insets b = progressBar.getInsets(); 
        int barRectWidth = progressBar.getWidth() - (b.left + b.right);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);
        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);
        boolean isLeftToRight = MetalUtils.isLeftToRight(c);
        int startX, startY, endX, endY;
        Rectangle box = null;
        box = getBox(box);
        startX = b.left;
        startY = b.top;
        endX = b.left + barRectWidth - 1;
        endY = b.top + barRectHeight - 1;
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke(new BasicStroke(1.f));
        if (progressBar.getOrientation() == JProgressBar.HORIZONTAL) {
            g2.setColor(MetalLookAndFeel.getControlShadow());
            g2.drawLine(startX, startY, endX, startY);
            g2.drawLine(startX, startY, startX, endY);
            g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
            g2.drawLine(box.x, startY, box.x + box.width - 1, startY);
        } else { 
            g2.setColor(MetalLookAndFeel.getControlShadow());
            g2.drawLine(startX, startY, startX, endY);
            g2.drawLine(startX, startY, endX, startY);
            g2.setColor(MetalLookAndFeel.getPrimaryControlDarkShadow());
            g2.drawLine(startX, box.y, startX, box.y + box.height - 1);
        }
    }
}
