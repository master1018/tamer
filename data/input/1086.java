public class BaseChart {
    private static String ERROR_CREATE_GRAF = "No se puede generar el grafico: ";
    private static String ERROR_LOAD_INF = "No fue cargada la informacion";
    private static String WINDOW_NAME = "Reporte";
    private static String CHART_NAME = "Grafico del reporte";
    private ChartFrame chartFrame;
    public void showChart() {
        if (this.chartFrame == null) {
            JOptionPane.showMessageDialog(null, ERROR_CREATE_GRAF + ERROR_LOAD_INF, WINDOW_NAME, JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.chartFrame.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = this.chartFrame.getWidth();
        int h = this.chartFrame.getHeight();
        int x = (screen.width - (w + (w / 2)));
        int y = (screen.height - (h + (h / 2)));
        this.chartFrame.setBounds(new Rectangle(x, y, this.chartFrame.getWidth(), this.chartFrame.getHeight()));
        this.chartFrame.setVisible(true);
    }
    protected void setCreatedChart(JFreeChart chart) {
        ChartFrame chartFrame = new ChartFrame(CHART_NAME, chart);
        this.setChartFrame(chartFrame);
    }
    public ChartFrame getChartFrame() {
        return chartFrame;
    }
    public void setChartFrame(ChartFrame chartFrame) {
        this.chartFrame = chartFrame;
    }
}
