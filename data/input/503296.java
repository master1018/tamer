public class DisplaySync extends SyncCommon {
    private TimePeriodValues mDatasetsSync[];
    private List<String> mTooltipsSync[];
    private CustomXYToolTipGenerator mTooltipGenerators[];
    private TimeSeries mDatasetsSyncTickle[];
    private TimeSeries mDatasetError;
    public DisplaySync(String name) {
        super(name);
    }
    @Override
    public Control createComposite(final Composite parent, EventLogParser logParser,
            final ILogColumnListener listener) {
        Control composite = createCompositeChart(parent, logParser, "Sync Status");
        resetUI();
        return composite;
    }
    @Override
    void resetUI() {
        super.resetUI();
        XYPlot xyPlot = mChart.getXYPlot();
        XYBarRenderer br = new XYBarRenderer();
        mDatasetsSync = new TimePeriodValues[NUM_AUTHS];
        mTooltipsSync = new List[NUM_AUTHS];
        mTooltipGenerators = new CustomXYToolTipGenerator[NUM_AUTHS];
        TimePeriodValuesCollection tpvc = new TimePeriodValuesCollection();
        xyPlot.setDataset(tpvc);
        xyPlot.setRenderer(0, br);
        XYLineAndShapeRenderer ls = new XYLineAndShapeRenderer();
        ls.setBaseLinesVisible(false);
        mDatasetsSyncTickle = new TimeSeries[NUM_AUTHS];
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        xyPlot.setDataset(1, tsc);
        xyPlot.setRenderer(1, ls);
        mDatasetError = new TimeSeries("Errors", FixedMillisecond.class);
        xyPlot.setDataset(2, new TimeSeriesCollection(mDatasetError));
        XYLineAndShapeRenderer errls = new XYLineAndShapeRenderer();
        errls.setBaseLinesVisible(false);
        errls.setSeriesPaint(0, Color.RED);
        xyPlot.setRenderer(2, errls);
        for (int i = 0; i < NUM_AUTHS; i++) {
            br.setSeriesPaint(i, AUTH_COLORS[i]);
            ls.setSeriesPaint(i, AUTH_COLORS[i]);
            mDatasetsSync[i] = new TimePeriodValues(AUTH_NAMES[i]);
            tpvc.addSeries(mDatasetsSync[i]);
            mTooltipsSync[i] = new ArrayList<String>();
            mTooltipGenerators[i] = new CustomXYToolTipGenerator();
            br.setSeriesToolTipGenerator(i, mTooltipGenerators[i]);
            mTooltipGenerators[i].addToolTipSeries(mTooltipsSync[i]);
            mDatasetsSyncTickle[i] = new TimeSeries(AUTH_NAMES[i] + " tickle",
                    FixedMillisecond.class);
            tsc.addSeries(mDatasetsSyncTickle[i]);
            ls.setSeriesShape(i, ShapeUtilities.createUpTriangle(2.5f));
        }
    }
    @Override
    void newEvent(EventContainer event, EventLogParser logParser) {
        super.newEvent(event, logParser); 
        try {
            if (event.mTag == EVENT_TICKLE) {
                int auth = getAuth(event.getValueAsString(0));
                if (auth >= 0) {
                    long msec = (long)event.sec * 1000L + (event.nsec / 1000000L);
                    mDatasetsSyncTickle[auth].addOrUpdate(new FixedMillisecond(msec), -1);
                }
            }
        } catch (InvalidTypeException e) {
        }
    }
    private int getHeightFromDetails(String details) {
        if (details == null) {
            return 1; 
        }
        int total = 0;
        String parts[] = details.split("[a-zA-Z]");
        for (String part : parts) {
            if ("".equals(part)) continue;
            total += Integer.parseInt(part);
        }
        if (total == 0) {
            total = 1;
        }
        return total;
    }
    private String getTextFromDetails(int auth, String details, int eventSource) {
        StringBuffer sb = new StringBuffer();
        sb.append(AUTH_NAMES[auth]).append(": \n");
        Scanner scanner = new Scanner(details);
        Pattern charPat = Pattern.compile("[a-zA-Z]");
        Pattern numPat = Pattern.compile("[0-9]+");
        while (scanner.hasNext()) {
            String key = scanner.findInLine(charPat);
            int val = Integer.parseInt(scanner.findInLine(numPat));
            if (auth == GMAIL && "M".equals(key)) {
                sb.append("messages from server: ").append(val).append("\n");
            } else if (auth == GMAIL && "L".equals(key)) {
                sb.append("labels from server: ").append(val).append("\n");
            } else if (auth == GMAIL && "C".equals(key)) {
                sb.append("check conversation requests from server: ").append(val).append("\n");
            } else if (auth == GMAIL && "A".equals(key)) {
                sb.append("attachments from server: ").append(val).append("\n");
            } else if (auth == GMAIL && "U".equals(key)) {
                sb.append("op updates from server: ").append(val).append("\n");
            } else if (auth == GMAIL && "u".equals(key)) {
                sb.append("op updates to server: ").append(val).append("\n");
            } else if (auth == GMAIL && "S".equals(key)) {
                sb.append("send/receive cycles: ").append(val).append("\n");
            } else if ("Q".equals(key)) {
                sb.append("queries to server: ").append(val).append("\n");
            } else if ("E".equals(key)) {
                sb.append("entries from server: ").append(val).append("\n");
            } else if ("u".equals(key)) {
                sb.append("updates from client: ").append(val).append("\n");
            } else if ("i".equals(key)) {
                sb.append("inserts from client: ").append(val).append("\n");
            } else if ("d".equals(key)) {
                sb.append("deletes from client: ").append(val).append("\n");
            } else if ("f".equals(key)) {
                sb.append("full sync requested\n");
            } else if ("r".equals(key)) {
                sb.append("partial sync unavailable\n");
            } else if ("X".equals(key)) {
                sb.append("hard error\n");
            } else if ("e".equals(key)) {
                sb.append("number of parse exceptions: ").append(val).append("\n");
            } else if ("c".equals(key)) {
                sb.append("number of conflicts: ").append(val).append("\n");
            } else if ("a".equals(key)) {
                sb.append("number of auth exceptions: ").append(val).append("\n");
            } else if ("D".equals(key)) {
                sb.append("too many deletions\n");
            } else if ("R".equals(key)) {
                sb.append("too many retries: ").append(val).append("\n");
            } else if ("b".equals(key)) {
                sb.append("database error\n");
            } else if ("x".equals(key)) {
                sb.append("soft error\n");
            } else if ("l".equals(key)) {
                sb.append("sync already in progress\n");
            } else if ("I".equals(key)) {
                sb.append("io exception\n");
            } else if (auth == CONTACTS && "g".equals(key)) {
                sb.append("aggregation query: ").append(val).append("\n");
            } else if (auth == CONTACTS && "G".equals(key)) {
                sb.append("aggregation merge: ").append(val).append("\n");
            } else if (auth == CONTACTS && "n".equals(key)) {
                sb.append("num entries: ").append(val).append("\n");
            } else if (auth == CONTACTS && "p".equals(key)) {
                sb.append("photos uploaded from server: ").append(val).append("\n");
            } else if (auth == CONTACTS && "P".equals(key)) {
                sb.append("photos downloaded from server: ").append(val).append("\n");
            } else if (auth == CALENDAR && "F".equals(key)) {
                sb.append("server refresh\n");
            } else if (auth == CALENDAR && "s".equals(key)) {
                sb.append("server diffs fetched\n");
            } else {
                sb.append(key).append("=").append(val);
            }
        }
        if (eventSource == 0) {
            sb.append("(server)");
        } else if (eventSource == 1) {
            sb.append("(local)");
        } else if (eventSource == 2) {
            sb.append("(poll)");
        } else if (eventSource == 3) {
            sb.append("(user)");
        }
        return sb.toString();
    }
    @Override
    void processSyncEvent(EventContainer event, int auth, long startTime, long stopTime,
            String details, boolean newEvent, int syncSource) {
        if (!newEvent) {
            int lastItem = mDatasetsSync[auth].getItemCount();
            mDatasetsSync[auth].delete(lastItem-1, lastItem-1);
            mTooltipsSync[auth].remove(lastItem-1);
        }
        double height = getHeightFromDetails(details);
        height = height / (stopTime - startTime + 1) * 10000;
        if (height > 30) {
            height = 30;
        }
        mDatasetsSync[auth].add(new SimpleTimePeriod(startTime, stopTime), height);
        mTooltipsSync[auth].add(getTextFromDetails(auth, details, syncSource));
        mTooltipGenerators[auth].addToolTipSeries(mTooltipsSync[auth]);
        if (details.indexOf('x') >= 0 || details.indexOf('X') >= 0) {
            long msec = (long)event.sec * 1000L + (event.nsec / 1000000L);
            mDatasetError.addOrUpdate(new FixedMillisecond(msec), -1);
        }
    }
    @Override
    int getDisplayType() {
        return DISPLAY_TYPE_SYNC;
    }
}
