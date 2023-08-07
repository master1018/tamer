public class XYBarRendererPositiveNegative extends XYBarRenderer {
    private static final long serialVersionUID = -2439685144988997039L;
    public XYBarRendererPositiveNegative(final double d) {
        super(d);
    }
    @Override
    public final Paint getItemPaint(final int row, final int column) {
        XYDataset dataset = getPlot().getDataset();
        double value = dataset.getYValue(row, column);
        if (value < 0) {
            return DataChartUtilities.DEFAULT_RED;
        } else {
            return DataChartUtilities.DEFAULT_BLUE;
        }
    }
}
