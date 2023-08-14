public class DisplaySyncHistogram extends SyncCommon {
    Map<SimpleTimePeriod, Integer> mTimePeriodMap[];
    private TimePeriodValues mDatasetsSyncHist[];
    public DisplaySyncHistogram(String name) {
        super(name);
    }
    @Override
    public Control createComposite(final Composite parent, EventLogParser logParser,
            final ILogColumnListener listener) {
        Control composite = createCompositeChart(parent, logParser, "Sync Histogram");
        resetUI();
        return composite;
    }
    @Override
    void resetUI() {
        super.resetUI();
        XYPlot xyPlot = mChart.getXYPlot();
        AbstractXYItemRenderer br = new XYBarRenderer();
        mDatasetsSyncHist = new TimePeriodValues[NUM_AUTHS+1];
        mTimePeriodMap = new HashMap[NUM_AUTHS + 1];
        TimePeriodValuesCollection tpvc = new TimePeriodValuesCollection();
        xyPlot.setDataset(tpvc);
        xyPlot.setRenderer(br);
        for (int i = 0; i < NUM_AUTHS + 1; i++) {
            br.setSeriesPaint(i, AUTH_COLORS[i]);
            mDatasetsSyncHist[i] = new TimePeriodValues(AUTH_NAMES[i]);
            tpvc.addSeries(mDatasetsSyncHist[i]);
            mTimePeriodMap[i] = new HashMap<SimpleTimePeriod, Integer>();
        }
    }
    @Override
    void processSyncEvent(EventContainer event, int auth, long startTime, long stopTime,
            String details, boolean newEvent, int syncSource) {
        if (newEvent) {
            if (details.indexOf('x') >= 0 || details.indexOf('X') >= 0) {
                auth = ERRORS;
            }
            double delta = (stopTime - startTime) * 100. / 1000 / 3600; 
            addHistEvent(0, auth, delta);
        } else {
            if (details.indexOf('x') >= 0 || details.indexOf('X') >= 0) {
                double delta = (stopTime - startTime) * 100. / 1000 / 3600; 
                addHistEvent(0, auth, -delta);
                addHistEvent(0, ERRORS, delta);
            }
        }
    }
    private void addHistEvent(long stopTime, int auth, double value) {
        SimpleTimePeriod hour = getTimePeriod(stopTime, mHistWidth);
        for (int i = auth; i <= ERRORS; i++) {
            addToPeriod(mDatasetsSyncHist, i, hour, value);
        }
    }
    private void addToPeriod(TimePeriodValues tpv[], int auth, SimpleTimePeriod period,
            double value) {
        int index;
        if (mTimePeriodMap[auth].containsKey(period)) {
            index = mTimePeriodMap[auth].get(period);
            double oldValue = tpv[auth].getValue(index).doubleValue();
            tpv[auth].update(index, oldValue + value);
        } else {
            index = tpv[auth].getItemCount();
            mTimePeriodMap[auth].put(period, index);
            tpv[auth].add(period, value);
        }
    }
    private SimpleTimePeriod getTimePeriod(long time, long numHoursWide) {
        Date date = new Date(time);
        TimeZone zone = RegularTimePeriod.DEFAULT_TIME_ZONE;
        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(date);
        long hoursOfYear = calendar.get(Calendar.HOUR_OF_DAY) +
                calendar.get(Calendar.DAY_OF_YEAR) * 24;
        int year = calendar.get(Calendar.YEAR);
        hoursOfYear = (hoursOfYear / numHoursWide) * numHoursWide;
        calendar.clear();
        calendar.set(year, 0, 1, 0, 0); 
        long start = calendar.getTimeInMillis() + hoursOfYear * 3600 * 1000;
        return new SimpleTimePeriod(start, start + numHoursWide * 3600 * 1000);
    }
    @Override
    int getDisplayType() {
        return DISPLAY_TYPE_SYNC_HIST;
    }
}
