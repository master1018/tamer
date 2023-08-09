abstract class EventDisplay {
    private final static String DISPLAY_DATA_STORAGE_SEPARATOR = ":"; 
    private final static String PID_STORAGE_SEPARATOR = ","; 
    private final static String DESCRIPTOR_STORAGE_SEPARATOR = "$"; 
    private final static String DESCRIPTOR_DATA_STORAGE_SEPARATOR = "!"; 
    private final static String FILTER_VALUE_NULL = "<null>"; 
    public final static int DISPLAY_TYPE_LOG_ALL = 0;
    public final static int DISPLAY_TYPE_FILTERED_LOG = 1;
    public final static int DISPLAY_TYPE_GRAPH = 2;
    public final static int DISPLAY_TYPE_SYNC = 3;
    public final static int DISPLAY_TYPE_SYNC_HIST = 4;
    public final static int DISPLAY_TYPE_SYNC_PERF = 5;
    private final static int EVENT_CHECK_FAILED = 0;
    protected final static int EVENT_CHECK_SAME_TAG = 1;
    protected final static int EVENT_CHECK_SAME_VALUE = 2;
    public static EventDisplay eventDisplayFactory(int type, String name) {
        switch (type) {
            case DISPLAY_TYPE_LOG_ALL:
                return new DisplayLog(name);
            case DISPLAY_TYPE_FILTERED_LOG:
                return new DisplayFilteredLog(name);
            case DISPLAY_TYPE_SYNC:
                return new DisplaySync(name);
            case DISPLAY_TYPE_SYNC_HIST:
                return new DisplaySyncHistogram(name);
            case DISPLAY_TYPE_GRAPH:
                return new DisplayGraph(name);
            case DISPLAY_TYPE_SYNC_PERF:
                return new DisplaySyncPerf(name);
            default:
                throw new InvalidParameterException("Unknown Display Type " + type); 
        }
    }
    abstract void newEvent(EventContainer event, EventLogParser logParser);
    abstract void resetUI();
    abstract int getDisplayType();
    abstract Control createComposite(final Composite parent, EventLogParser logParser,
            final ILogColumnListener listener);
    interface ILogColumnListener {
        void columnResized(int index, TableColumn sourceColumn);
    }
    static class OccurrenceDisplayDescriptor {
        int eventTag = -1;
        int seriesValueIndex = -1;
        boolean includePid = false;
        int filterValueIndex = -1;
        CompareMethod filterCompareMethod = CompareMethod.EQUAL_TO;
        Object filterValue = null;
        OccurrenceDisplayDescriptor() {
        }
        OccurrenceDisplayDescriptor(OccurrenceDisplayDescriptor descriptor) {
            replaceWith(descriptor);
        }
        OccurrenceDisplayDescriptor(int eventTag) {
            this.eventTag = eventTag;
        }
        OccurrenceDisplayDescriptor(int eventTag, int seriesValueIndex) {
            this.eventTag = eventTag;
            this.seriesValueIndex = seriesValueIndex;
        }
        void replaceWith(OccurrenceDisplayDescriptor descriptor) {
            eventTag = descriptor.eventTag;
            seriesValueIndex = descriptor.seriesValueIndex;
            includePid = descriptor.includePid;
            filterValueIndex = descriptor.filterValueIndex;
            filterCompareMethod = descriptor.filterCompareMethod;
            filterValue = descriptor.filterValue;
        }
        final void loadFrom(String storageString) {
            String[] values = storageString.split(Pattern.quote(DESCRIPTOR_DATA_STORAGE_SEPARATOR));
            loadFrom(values, 0);
        }
        protected int loadFrom(String[] storageStrings, int index) {
            eventTag = Integer.parseInt(storageStrings[index++]);
            seriesValueIndex = Integer.parseInt(storageStrings[index++]);
            includePid = Boolean.parseBoolean(storageStrings[index++]);
            filterValueIndex = Integer.parseInt(storageStrings[index++]);
            try {
                filterCompareMethod = CompareMethod.valueOf(storageStrings[index++]);
            } catch (IllegalArgumentException e) {
                filterCompareMethod = CompareMethod.EQUAL_TO;
            }
            String value = storageStrings[index++];
            if (filterValueIndex != -1 && FILTER_VALUE_NULL.equals(value) == false) {
                filterValue = EventValueType.getObjectFromStorageString(value);
            }
            return index;
        }
        String getStorageString() {
            StringBuilder sb = new StringBuilder();
            sb.append(eventTag);
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            sb.append(seriesValueIndex);
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            sb.append(Boolean.toString(includePid));
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            sb.append(filterValueIndex);
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            sb.append(filterCompareMethod.name());
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            if (filterValue != null) {
                String value = EventValueType.getStorageString(filterValue);
                if (value != null) {
                    sb.append(value);
                } else {
                    sb.append(FILTER_VALUE_NULL);
                }
            } else {
                sb.append(FILTER_VALUE_NULL);
            }
            return sb.toString();
        }
    }
    static final class ValueDisplayDescriptor extends OccurrenceDisplayDescriptor {
        String valueName;
        int valueIndex = -1;
        ValueDisplayDescriptor() {
            super();
        }
        ValueDisplayDescriptor(ValueDisplayDescriptor descriptor) {
            super();
            replaceWith(descriptor);
        }
        ValueDisplayDescriptor(int eventTag, String valueName, int valueIndex) {
            super(eventTag);
            this.valueName = valueName;
            this.valueIndex = valueIndex;
        }
        ValueDisplayDescriptor(int eventTag, String valueName, int valueIndex,
                int seriesValueIndex) {
            super(eventTag, seriesValueIndex);
            this.valueName = valueName;
            this.valueIndex = valueIndex;
        }
        @Override
        void replaceWith(OccurrenceDisplayDescriptor descriptor) {
            super.replaceWith(descriptor);
            if (descriptor instanceof ValueDisplayDescriptor) {
                ValueDisplayDescriptor valueDescriptor = (ValueDisplayDescriptor) descriptor;
                valueName = valueDescriptor.valueName;
                valueIndex = valueDescriptor.valueIndex;
            }
        }
        @Override
        protected int loadFrom(String[] storageStrings, int index) {
            index = super.loadFrom(storageStrings, index);
            valueName = storageStrings[index++];
            valueIndex = Integer.parseInt(storageStrings[index++]);
            return index;
        }
        @Override
        String getStorageString() {
            String superStorage = super.getStorageString();
            StringBuilder sb = new StringBuilder();
            sb.append(superStorage);
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            sb.append(valueName);
            sb.append(DESCRIPTOR_DATA_STORAGE_SEPARATOR);
            sb.append(valueIndex);
            return sb.toString();
        }
    }
    protected String mName;
    private boolean mPidFiltering = false;
    private ArrayList<Integer> mPidFilterList = null;
    protected final ArrayList<ValueDisplayDescriptor> mValueDescriptors =
            new ArrayList<ValueDisplayDescriptor>();
    private final ArrayList<OccurrenceDisplayDescriptor> mOccurrenceDescriptors =
            new ArrayList<OccurrenceDisplayDescriptor>();
    protected final HashMap<ValueDisplayDescriptor, HashMap<Integer, TimeSeries>> mValueDescriptorSeriesMap =
            new HashMap<ValueDisplayDescriptor, HashMap<Integer, TimeSeries>>();
    protected final HashMap<OccurrenceDisplayDescriptor, HashMap<Integer, TimeSeries>> mOcurrenceDescriptorSeriesMap =
            new HashMap<OccurrenceDisplayDescriptor, HashMap<Integer, TimeSeries>>();
    protected final HashMap<ValueType, TimeSeriesCollection> mValueTypeDataSetMap =
            new HashMap<ValueType, TimeSeriesCollection>();
    protected JFreeChart mChart;
    protected TimeSeriesCollection mOccurrenceDataSet;
    protected int mDataSetCount;
    private ChartComposite mChartComposite;
    protected long mMaximumChartItemAge = -1;
    protected long mHistWidth = 1;
    protected Table mLogTable;
    protected int mValueDescriptorCheck = EVENT_CHECK_FAILED;
    EventDisplay(String name) {
        mName = name;
    }
    static EventDisplay clone(EventDisplay from) {
        EventDisplay ed = eventDisplayFactory(from.getDisplayType(), from.getName());
        ed.mName = from.mName;
        ed.mPidFiltering = from.mPidFiltering;
        ed.mMaximumChartItemAge = from.mMaximumChartItemAge;
        ed.mHistWidth = from.mHistWidth;
        if (from.mPidFilterList != null) {
            ed.mPidFilterList = new ArrayList<Integer>();
            ed.mPidFilterList.addAll(from.mPidFilterList);
        }
        for (ValueDisplayDescriptor desc : from.mValueDescriptors) {
            ed.mValueDescriptors.add(new ValueDisplayDescriptor(desc));
        }
        ed.mValueDescriptorCheck = from.mValueDescriptorCheck;
        for (OccurrenceDisplayDescriptor desc : from.mOccurrenceDescriptors) {
            ed.mOccurrenceDescriptors.add(new OccurrenceDisplayDescriptor(desc));
        }
        return ed;
    }
    String getStorageString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mName);
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(getDisplayType());
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(Boolean.toString(mPidFiltering));
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(getPidStorageString());
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(getDescriptorStorageString(mValueDescriptors));
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(getDescriptorStorageString(mOccurrenceDescriptors));
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(mMaximumChartItemAge);
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        sb.append(mHistWidth);
        sb.append(DISPLAY_DATA_STORAGE_SEPARATOR);
        return sb.toString();
    }
    void setName(String name) {
        mName = name;
    }
    String getName() {
        return mName;
    }
    void setPidFiltering(boolean filterByPid) {
        mPidFiltering = filterByPid;
    }
    boolean getPidFiltering() {
        return mPidFiltering;
    }
    void setPidFilterList(ArrayList<Integer> pids) {
        if (mPidFiltering == false) {
            new InvalidParameterException();
        }
        mPidFilterList = pids;
    }
    ArrayList<Integer> getPidFilterList() {
        return mPidFilterList;
    }
    void addPidFiler(int pid) {
        if (mPidFiltering == false) {
            new InvalidParameterException();
        }
        if (mPidFilterList == null) {
            mPidFilterList = new ArrayList<Integer>();
        }
        mPidFilterList.add(pid);
    }
    Iterator<ValueDisplayDescriptor> getValueDescriptors() {
        return mValueDescriptors.iterator();
    }
    void updateValueDescriptorCheck() {
        mValueDescriptorCheck = checkDescriptors();
    }
    Iterator<OccurrenceDisplayDescriptor> getOccurrenceDescriptors() {
        return mOccurrenceDescriptors.iterator();
    }
    void addDescriptor(OccurrenceDisplayDescriptor descriptor) {
        if (descriptor instanceof ValueDisplayDescriptor) {
            mValueDescriptors.add((ValueDisplayDescriptor) descriptor);
            mValueDescriptorCheck = checkDescriptors();
        } else {
            mOccurrenceDescriptors.add(descriptor);
        }
    }
    OccurrenceDisplayDescriptor getDescriptor(
            Class<? extends OccurrenceDisplayDescriptor> descriptorClass, int index) {
        if (descriptorClass == OccurrenceDisplayDescriptor.class) {
            return mOccurrenceDescriptors.get(index);
        } else if (descriptorClass == ValueDisplayDescriptor.class) {
            return mValueDescriptors.get(index);
        }
        return null;
    }
    void removeDescriptor(Class<? extends OccurrenceDisplayDescriptor> descriptorClass, int index) {
        if (descriptorClass == OccurrenceDisplayDescriptor.class) {
            mOccurrenceDescriptors.remove(index);
        } else if (descriptorClass == ValueDisplayDescriptor.class) {
            mValueDescriptors.remove(index);
            mValueDescriptorCheck = checkDescriptors();
        }
    }
    Control createCompositeChart(final Composite parent, EventLogParser logParser,
            String title) {
        mChart = ChartFactory.createTimeSeriesChart(
                null,
                null ,
                null ,
                null, 
                true ,
                false ,
                false );
        Font f = parent.getFont();
        FontData[] fData = f.getFontData();
        FontData firstFontData = fData[0];
        java.awt.Font awtFont = SWTUtils.toAwtFont(parent.getDisplay(),
                firstFontData, true );
        mChart.setTitle(new TextTitle(title, awtFont));
        final XYPlot xyPlot = mChart.getXYPlot();
        xyPlot.setRangeCrosshairVisible(true);
        xyPlot.setRangeCrosshairLockedOnData(true);
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setDomainCrosshairLockedOnData(true);
        mChart.addChangeListener(new ChartChangeListener() {
            public void chartChanged(ChartChangeEvent event) {
                ChartChangeEventType type = event.getType();
                if (type == ChartChangeEventType.GENERAL) {
                    parent.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            processClick(xyPlot);
                        }
                    });
                }
            }
        });
        mChartComposite = new ChartComposite(parent, SWT.BORDER, mChart,
                ChartComposite.DEFAULT_WIDTH,
                ChartComposite.DEFAULT_HEIGHT,
                ChartComposite.DEFAULT_MINIMUM_DRAW_WIDTH,
                ChartComposite.DEFAULT_MINIMUM_DRAW_HEIGHT,
                3000, 
                3000, 
                true,  
                true,  
                true,  
                true,  
                true,  
                true);   
        mChartComposite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                mValueTypeDataSetMap.clear();
                mDataSetCount = 0;
                mOccurrenceDataSet = null;
                mChart = null;
                mChartComposite = null;
                mValueDescriptorSeriesMap.clear();
                mOcurrenceDescriptorSeriesMap.clear();
            }
        });
        return mChartComposite;
    }
    private void processClick(XYPlot xyPlot) {
        double rangeValue = xyPlot.getRangeCrosshairValue();
        if (rangeValue != 0) {
            double domainValue = xyPlot.getDomainCrosshairValue();
            Millisecond msec = new Millisecond(new Date((long) domainValue));
            Set<ValueDisplayDescriptor> descKeys = mValueDescriptorSeriesMap.keySet();
            for (ValueDisplayDescriptor descKey : descKeys) {
                HashMap<Integer, TimeSeries> map = mValueDescriptorSeriesMap.get(descKey);
                Set<Integer> pidKeys = map.keySet();
                for (Integer pidKey : pidKeys) {
                    TimeSeries series = map.get(pidKey);
                    Number value = series.getValue(msec);
                    if (value != null) {
                        if (value.doubleValue() == rangeValue) {
                            return;
                        }
                    }
                }
            }
        }
    }
    void resizeColumn(int index, TableColumn sourceColumn) {
    }
    protected void setNewLogParser(EventLogParser logParser) {
    }
    void startMultiEventDisplay() {
        if (mLogTable != null) {
            mLogTable.setRedraw(false);
        }
    }
    void endMultiEventDisplay() {
        if (mLogTable != null) {
            mLogTable.setRedraw(true);
        }
    }
    Table getTable() {
        return mLogTable;
    }
    static EventDisplay load(String storageString) {
        if (storageString.length() > 0) {
            String[] values = storageString.split(Pattern.quote(DISPLAY_DATA_STORAGE_SEPARATOR));
            try {
                int index = 0;
                String name = values[index++];
                int displayType = Integer.parseInt(values[index++]);
                boolean pidFiltering = Boolean.parseBoolean(values[index++]);
                EventDisplay ed = eventDisplayFactory(displayType, name);
                ed.setPidFiltering(pidFiltering);
                if (index < values.length) {
                    ed.loadPidFilters(values[index++]);
                }
                if (index < values.length) {
                    ed.loadValueDescriptors(values[index++]);
                }
                if (index < values.length) {
                    ed.loadOccurrenceDescriptors(values[index++]);
                }
                ed.updateValueDescriptorCheck();
                if (index < values.length) {
                    ed.mMaximumChartItemAge = Long.parseLong(values[index++]);
                }
                if (index < values.length) {
                    ed.mHistWidth = Long.parseLong(values[index++]);
                }
                return ed;
            } catch (RuntimeException re) {
                Log.e("ddms", re);
            }
        }
        return null;
    }
    private String getPidStorageString() {
        if (mPidFilterList != null) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer i : mPidFilterList) {
                if (first == false) {
                    sb.append(PID_STORAGE_SEPARATOR);
                } else {
                    first = false;
                }
                sb.append(i);
            }
            return sb.toString();
        }
        return ""; 
    }
    private void loadPidFilters(String storageString) {
        if (storageString.length() > 0) {
            String[] values = storageString.split(Pattern.quote(PID_STORAGE_SEPARATOR));
            for (String value : values) {
                if (mPidFilterList == null) {
                    mPidFilterList = new ArrayList<Integer>();
                }
                mPidFilterList.add(Integer.parseInt(value));
            }
        }
    }
    private String getDescriptorStorageString(
            ArrayList<? extends OccurrenceDisplayDescriptor> descriptorList) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (OccurrenceDisplayDescriptor descriptor : descriptorList) {
            if (first == false) {
                sb.append(DESCRIPTOR_STORAGE_SEPARATOR);
            } else {
                first = false;
            }
            sb.append(descriptor.getStorageString());
        }
        return sb.toString();
    }
    private void loadOccurrenceDescriptors(String storageString) {
        if (storageString.length() == 0) {
            return;
        }
        String[] values = storageString.split(Pattern.quote(DESCRIPTOR_STORAGE_SEPARATOR));
        for (String value : values) {
            OccurrenceDisplayDescriptor desc = new OccurrenceDisplayDescriptor();
            desc.loadFrom(value);
            mOccurrenceDescriptors.add(desc);
        }
    }
    private void loadValueDescriptors(String storageString) {
        if (storageString.length() == 0) {
            return;
        }
        String[] values = storageString.split(Pattern.quote(DESCRIPTOR_STORAGE_SEPARATOR));
        for (String value : values) {
            ValueDisplayDescriptor desc = new ValueDisplayDescriptor();
            desc.loadFrom(value);
            mValueDescriptors.add(desc);
        }
    }
    @SuppressWarnings("unchecked")
    private void getDescriptors(EventContainer event,
            ArrayList<? extends OccurrenceDisplayDescriptor> fullList,
            ArrayList outList) {
        for (OccurrenceDisplayDescriptor descriptor : fullList) {
            try {
                if (descriptor.eventTag == event.mTag) {
                    if (descriptor.filterValueIndex == -1 ||
                            event.testValue(descriptor.filterValueIndex, descriptor.filterValue,
                                    descriptor.filterCompareMethod)) {
                        outList.add(descriptor);
                    }
                }
            } catch (InvalidTypeException ite) {
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                Log.e("Event Log", String.format(
                        "ArrayIndexOutOfBoundsException occured when checking %1$d-th value of event %2$d", 
                        descriptor.filterValueIndex, descriptor.eventTag));
            }
        }
    }
    protected boolean filterEvent(EventContainer event,
            ArrayList<ValueDisplayDescriptor> valueDescriptors,
            ArrayList<OccurrenceDisplayDescriptor> occurrenceDescriptors) {
        if (mPidFiltering && mPidFilterList != null) {
            boolean found = false;
            for (int pid : mPidFilterList) {
                if (pid == event.pid) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        getDescriptors(event, mValueDescriptors, valueDescriptors);
        getDescriptors(event, mOccurrenceDescriptors, occurrenceDescriptors);
        return (valueDescriptors.size() > 0 || occurrenceDescriptors.size() > 0);
    }
    private int checkDescriptors() {
        if (mValueDescriptors.size() < 2) {
            return EVENT_CHECK_SAME_VALUE;
        }
        int tag = -1;
        int index = -1;
        for (ValueDisplayDescriptor display : mValueDescriptors) {
            if (tag == -1) {
                tag = display.eventTag;
                index = display.valueIndex;
            } else {
                if (tag != display.eventTag) {
                    return EVENT_CHECK_FAILED;
                } else {
                    if (index != -1) {
                        if (index != display.valueIndex) {
                            index = -1;
                        }
                    }
                }
            }
        }
        if (index == -1) {
            return EVENT_CHECK_SAME_TAG;
        }
        return EVENT_CHECK_SAME_VALUE;
    }
    void resetChartTimeLimit() {
        mMaximumChartItemAge = -1;
    }
    void setChartTimeLimit(long timeLimit) {
        mMaximumChartItemAge = timeLimit;
    }
    long getChartTimeLimit() {
        return mMaximumChartItemAge;
    }
    void resetHistWidth() {
        mHistWidth = 1;
    }
    void setHistWidth(long histWidth) {
        mHistWidth = histWidth;
    }
    long getHistWidth() {
        return mHistWidth;
    }
}
