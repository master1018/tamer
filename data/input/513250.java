public class OccurrenceRenderer extends XYLineAndShapeRenderer {
    private static final long serialVersionUID = 1L;
    @Override
    public void drawItem(Graphics2D g2, 
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot, 
                         ValueAxis domainAxis, 
                         ValueAxis rangeAxis,
                         XYDataset dataset, 
                         int series, 
                         int item,
                         CrosshairState crosshairState, 
                         int pass) {
        TimeSeriesCollection timeDataSet = (TimeSeriesCollection)dataset;
        double x = timeDataSet.getX(series, item).doubleValue();
        double yMin = rangeAxis.getLowerBound();
        double yMax = rangeAxis.getUpperBound();
        RectangleEdge domainEdge = plot.getDomainAxisEdge();
        RectangleEdge rangeEdge = plot.getRangeAxisEdge();
        double x2D = domainAxis.valueToJava2D(x, dataArea, domainEdge);
        double yMin2D = rangeAxis.valueToJava2D(yMin, dataArea, rangeEdge);
        double yMax2D = rangeAxis.valueToJava2D(yMax, dataArea, rangeEdge);
        Paint p = getItemPaint(series, item);
        Stroke s = getItemStroke(series, item);
        Line2D line = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(yMin2D, x2D, yMax2D, x2D);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(x2D, yMin2D, x2D, yMax2D);
        }
        g2.setPaint(p);
        g2.setStroke(s);
        g2.draw(line);
    }
}
