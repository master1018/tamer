public class DisplaySyncPerf extends SyncCommon {
    CustomXYToolTipGenerator mTooltipGenerator;
    List mTooltips[];
    private static final int DB_QUERY = 4;
    private static final int DB_WRITE = 5;
    private static final int HTTP_NETWORK = 6;
    private static final int HTTP_PROCESSING = 7;
    private static final int NUM_SERIES = (HTTP_PROCESSING + 1);
    private static final String SERIES_NAMES[] = {"Calendar", "Gmail", "Feeds", "Contacts",
            "DB Query", "DB Write", "HTTP Response", "HTTP Processing",};
    private static final Color SERIES_COLORS[] = {Color.MAGENTA, Color.GREEN, Color.BLUE,
            Color.ORANGE, Color.RED, Color.CYAN, Color.PINK, Color.DARK_GRAY};
    private static final double SERIES_YCOORD[] = {0, 0, 0, 0, 1, 1, 2, 2};
    private static final int EVENT_DB_OPERATION = 52000;
    private static final int EVENT_HTTP_STATS = 52001;
    final int EVENT_DB_QUERY = 0;
    final int EVENT_DB_WRITE = 1;
    private TimePeriodValues mDatasets[];
    class YIntervalTimePeriodValuesCollection extends TimePeriodValuesCollection {
        private static final long serialVersionUID = 1L;
        private double yheight;
        YIntervalTimePeriodValuesCollection(double yheight) {
            this.yheight = yheight;
        }
        @Override
        public Number getEndY(int series, int item) {
            return getY(series, item).doubleValue() + yheight;
        }
    }
    public DisplaySyncPerf(String name) {
        super(name);
    }
    @Override
    public Control createComposite(final Composite parent, EventLogParser logParser,
            final ILogColumnListener listener) {
        Control composite = createCompositeChart(parent, logParser, "Sync Performance");
        resetUI();
        return composite;
    }
    @Override
    void resetUI() {
        super.resetUI();
        XYPlot xyPlot = mChart.getXYPlot();
        xyPlot.getRangeAxis().setVisible(false);
        mTooltipGenerator = new CustomXYToolTipGenerator();
        mTooltips = new List[NUM_SERIES];
        XYBarRenderer br = new XYBarRenderer();
        br.setUseYInterval(true);
        mDatasets = new TimePeriodValues[NUM_SERIES];
        TimePeriodValuesCollection tpvc = new YIntervalTimePeriodValuesCollection(1);
        xyPlot.setDataset(tpvc);
        xyPlot.setRenderer(br);
        for (int i = 0; i < NUM_SERIES; i++) {
            br.setSeriesPaint(i, SERIES_COLORS[i]);
            mDatasets[i] = new TimePeriodValues(SERIES_NAMES[i]);
            tpvc.addSeries(mDatasets[i]);
            mTooltips[i] = new ArrayList<String>();
            mTooltipGenerator.addToolTipSeries(mTooltips[i]);
            br.setSeriesToolTipGenerator(i, mTooltipGenerator);
        }
    }
    @Override
    void newEvent(EventContainer event, EventLogParser logParser) {
        super.newEvent(event, logParser); 
        try {
            if (event.mTag == EVENT_DB_OPERATION) {
                String tip = event.getValueAsString(0);
                long endTime = (long) event.sec * 1000L + (event.nsec / 1000000L);
                int opType = Integer.parseInt(event.getValueAsString(1));
                long duration = Long.parseLong(event.getValueAsString(2));
                if (opType == EVENT_DB_QUERY) {
                    mDatasets[DB_QUERY].add(new SimpleTimePeriod(endTime - duration, endTime),
                            SERIES_YCOORD[DB_QUERY]);
                    mTooltips[DB_QUERY].add(tip);
                } else if (opType == EVENT_DB_WRITE) {
                    mDatasets[DB_WRITE].add(new SimpleTimePeriod(endTime - duration, endTime),
                            SERIES_YCOORD[DB_WRITE]);
                    mTooltips[DB_WRITE].add(tip);
                }
            } else if (event.mTag == EVENT_HTTP_STATS) {
                String tip = event.getValueAsString(0) + ", tx:" + event.getValueAsString(3) +
                        ", rx: " + event.getValueAsString(4);
                long endTime = (long) event.sec * 1000L + (event.nsec / 1000000L);
                long netEndTime = endTime - Long.parseLong(event.getValueAsString(2));
                long netStartTime = netEndTime - Long.parseLong(event.getValueAsString(1));
                mDatasets[HTTP_NETWORK].add(new SimpleTimePeriod(netStartTime, netEndTime),
                        SERIES_YCOORD[HTTP_NETWORK]);
                mDatasets[HTTP_PROCESSING].add(new SimpleTimePeriod(netEndTime, endTime),
                        SERIES_YCOORD[HTTP_PROCESSING]);
                mTooltips[HTTP_NETWORK].add(tip);
                mTooltips[HTTP_PROCESSING].add(tip);
            }
        } catch (InvalidTypeException e) {
        }
    }
    @Override
    void processSyncEvent(EventContainer event, int auth, long startTime, long stopTime,
            String details, boolean newEvent, int syncSource) {
        if (newEvent) {
            mDatasets[auth].add(new SimpleTimePeriod(startTime, stopTime), SERIES_YCOORD[auth]);
        }
    }
    @Override
    int getDisplayType() {
        return DISPLAY_TYPE_SYNC_PERF;
    }
}
