public class TGTextImpl extends TGText {
    public void paint(TGLayout layout, TGPainter painter, int fromX, int fromY) {
        TGBeatImpl beat = (TGBeatImpl) getBeat();
        TGMeasureImpl measure = beat.getMeasureImpl();
        int x = fromX + beat.getSpacing() + beat.getPosX();
        int y = fromY + measure.getTs().getPosition(TGTrackSpacing.POSITION_TEXT);
        layout.setTextStyle(painter);
        painter.drawString(getValue(), x, y);
    }
}
