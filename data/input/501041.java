class LayoutFigure extends ElementFigure {
    private HighlightInfo mHighlightInfo;
    public LayoutFigure(IOutlineProvider provider) {
        super(provider);
    }
    public void setHighlighInfo(HighlightInfo highlightInfo) {
        mHighlightInfo = highlightInfo;
        repaint();
    }
    @Override
    protected void paintBorder(Graphics graphics) {
        super.paintBorder(graphics);
        if (mHighlightInfo == null) {
            return;
        }
        if (mHighlightInfo.drawDropBorder) {
            graphics.setLineWidth(3);
            graphics.setLineStyle(SWT.LINE_SOLID);
            graphics.setForegroundColor(ColorConstants.green);
            graphics.drawRectangle(getInnerBounds().getCopy().shrink(1, 1));
        }
        Rectangle bounds = getBounds();
        int bx = bounds.x;
        int by = bounds.y;
        int w = bounds.width;
        int h = bounds.height;
        if (mHighlightInfo.childParts != null) {
            graphics.setLineWidth(2);
            graphics.setLineStyle(SWT.LINE_DOT);
            graphics.setForegroundColor(ColorConstants.lightBlue);
            for (UiElementEditPart part : mHighlightInfo.childParts) {
                if (part != null) {
                    graphics.drawRectangle(part.getBounds().getCopy().translate(bx, by));
                }
            }
        }
        if (mHighlightInfo.linePoints != null) {
            int x1 = mHighlightInfo.linePoints[0].x;
            int y1 = mHighlightInfo.linePoints[0].y;
            int x2 = mHighlightInfo.linePoints[1].x;
            int y2 = mHighlightInfo.linePoints[1].y;
            if (x1 <= 0) x1++;
            if (x2 <= 0) x2++;
            if (y1 <= 0) y1++;
            if (y2 <= 0) y2++;
            if (x1 >= w - 1) x1--;
            if (x2 >= w - 1) x2--;
            if (y1 >= h - 1) y1--;
            if (y2 >= h - 1) y2--;
            x1 += bx;
            x2 += bx;
            y1 += by;
            y2 += by;
            graphics.setLineWidth(2);
            graphics.setLineStyle(SWT.LINE_DASH);
            graphics.setLineCap(SWT.CAP_ROUND);
            graphics.setForegroundColor(ColorConstants.orange);
            graphics.drawLine(x1, y1, x2, y2);
        }
        if (mHighlightInfo.anchorPoint != null) {
            int x = mHighlightInfo.anchorPoint.x;
            int y = mHighlightInfo.anchorPoint.y;
            if (x <= 0) x++;
            if (y <= 0) y++;
            if (x >= w - 1) x--;
            if (y >= h - 1) y--;
            x += bx;
            y += by;
            graphics.setLineWidth(2);
            graphics.setLineStyle(SWT.LINE_SOLID);
            graphics.setLineCap(SWT.CAP_ROUND);
            graphics.setForegroundColor(ColorConstants.orange);
            graphics.drawLine(x-5, y-5, x+5, y+5);
            graphics.drawLine(x-5, y+5, x+5, y-5);
            graphics.setLineWidth(1);
            graphics.drawOval(x-8, y-8, 16, 16);
        }
    }
}
