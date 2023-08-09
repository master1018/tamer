public class DisplayGraph extends EventDisplay {
    public DisplayGraph(String name) {
        super(name);
    }
    @Override
    void resetUI() {
        Collection<TimeSeriesCollection> datasets = mValueTypeDataSetMap.values();
        for (TimeSeriesCollection dataset : datasets) {
            dataset.removeAllSeries();
        }
        if (mOccurrenceDataSet != null) {
            mOccurrenceDataSet.removeAllSeries();
        }
        mValueDescriptorSeriesMap.clear();
        mOcurrenceDescriptorSeriesMap.clear();
    }
    @Override
    public Control createComposite(final Composite parent, EventLogParser logParser,
            final ILogColumnListener listener) {
        String title = getChartTitle(logParser);
        return createCompositeChart(parent, logParser, title);
    }
    @Override
    void newEvent(EventContainer event, EventLogParser logParser) {
        ArrayList<ValueDisplayDescriptor> valueDescriptors =
                new ArrayList<ValueDisplayDescriptor>();
        ArrayList<OccurrenceDisplayDescriptor> occurrenceDescriptors =
                new ArrayList<OccurrenceDisplayDescriptor>();
        if (filterEvent(event, valueDescriptors, occurrenceDescriptors)) {
            updateChart(event, logParser, valueDescriptors, occurrenceDescriptors);
        }
    }
    private void updateChart(EventContainer event, EventLogParser logParser,
            ArrayList<ValueDisplayDescriptor> valueDescriptors,
            ArrayList<OccurrenceDisplayDescriptor> occurrenceDescriptors) {
        Map<Integer, String> tagMap = logParser.getTagMap();
        Millisecond millisecondTime = null;
        long msec = -1;
        boolean accumulateValues = false;
        double accumulatedValue = 0;
        if (event.mTag == 2721) {
            accumulateValues = true;
            for (ValueDisplayDescriptor descriptor : valueDescriptors) {
                accumulateValues &= (descriptor.valueIndex != 0);
            }
        }
        for (ValueDisplayDescriptor descriptor : valueDescriptors) {
            try {
                HashMap<Integer, TimeSeries> map = mValueDescriptorSeriesMap.get(descriptor);
                if (map == null) {
                    map = new HashMap<Integer, TimeSeries>();
                    mValueDescriptorSeriesMap.put(descriptor, map);
                }
                TimeSeries timeSeries = map.get(event.pid);
                if (timeSeries == null) {
                    String seriesFullName = null;
                    String seriesLabel = getSeriesLabel(event, descriptor);
                    switch (mValueDescriptorCheck) {
                        case EVENT_CHECK_SAME_TAG:
                            seriesFullName = String.format("%1$s / %2$s", seriesLabel,
                                    descriptor.valueName);
                            break;
                        case EVENT_CHECK_SAME_VALUE:
                            seriesFullName = String.format("%1$s", seriesLabel);
                            break;
                        default:
                            seriesFullName = String.format("%1$s / %2$s: %3$s", seriesLabel,
                                    tagMap.get(descriptor.eventTag),
                                    descriptor.valueName);
                            break;
                    }
                    TimeSeriesCollection dataset = getValueDataset(
                            logParser.getEventInfoMap().get(event.mTag)[descriptor.valueIndex]
                                                                        .getValueType(),
                            accumulateValues);
                    timeSeries = new TimeSeries(seriesFullName, Millisecond.class);
                    if (mMaximumChartItemAge != -1) {
                        timeSeries.setMaximumItemAge(mMaximumChartItemAge * 1000);
                    }
                    dataset.addSeries(timeSeries);
                    map.put(event.pid, timeSeries);
                }
                double value = event.getValueAsDouble(descriptor.valueIndex);
                if (accumulateValues) {
                    accumulatedValue += value;
                    value = accumulatedValue;
                }
                if (millisecondTime == null) {
                    msec = (long)event.sec * 1000L + (event.nsec / 1000000L);
                    millisecondTime = new Millisecond(new Date(msec));
                }
                timeSeries.addOrUpdate(millisecondTime, value);
            } catch (InvalidTypeException e) {
            }
        }
        for (OccurrenceDisplayDescriptor descriptor : occurrenceDescriptors) {
            try {
                HashMap<Integer, TimeSeries> map = mOcurrenceDescriptorSeriesMap.get(descriptor);
                if (map == null) {
                    map = new HashMap<Integer, TimeSeries>();
                    mOcurrenceDescriptorSeriesMap.put(descriptor, map);
                }
                TimeSeries timeSeries = map.get(event.pid);
                if (timeSeries == null) {
                    String seriesLabel = getSeriesLabel(event, descriptor);
                    String seriesFullName = String.format("[%1$s:%2$s]",
                            tagMap.get(descriptor.eventTag), seriesLabel);
                    timeSeries = new TimeSeries(seriesFullName, Millisecond.class);
                    if (mMaximumChartItemAge != -1) {
                        timeSeries.setMaximumItemAge(mMaximumChartItemAge);
                    }
                    getOccurrenceDataSet().addSeries(timeSeries);
                    map.put(event.pid, timeSeries);
                }
                if (millisecondTime == null) {
                    msec = (long)event.sec * 1000L + (event.nsec / 1000000L);
                    millisecondTime = new Millisecond(new Date(msec));
                }
                timeSeries.addOrUpdate(millisecondTime, 0); 
            } catch (InvalidTypeException e) {
            }
        }
        if (msec != -1 && mMaximumChartItemAge != -1) {
            Collection<HashMap<Integer, TimeSeries>> pidMapValues =
                mValueDescriptorSeriesMap.values();
            for (HashMap<Integer, TimeSeries> pidMapValue : pidMapValues) {
                Collection<TimeSeries> seriesCollection = pidMapValue.values();
                for (TimeSeries timeSeries : seriesCollection) {
                    timeSeries.removeAgedItems(msec, true);
                }
            }
            pidMapValues = mOcurrenceDescriptorSeriesMap.values();
            for (HashMap<Integer, TimeSeries> pidMapValue : pidMapValues) {
                Collection<TimeSeries> seriesCollection = pidMapValue.values();
                for (TimeSeries timeSeries : seriesCollection) {
                    timeSeries.removeAgedItems(msec, true);
                }
            }
        }
    }
    private TimeSeriesCollection getValueDataset(EventValueDescription.ValueType type, boolean accumulateValues) {
        TimeSeriesCollection dataset = mValueTypeDataSetMap.get(type);
        if (dataset == null) {
            dataset = new TimeSeriesCollection();
            mValueTypeDataSetMap.put(type, dataset);
            AbstractXYItemRenderer renderer;
            if (type == EventValueDescription.ValueType.PERCENT && accumulateValues) {
                renderer = new XYAreaRenderer();
            } else {
                XYLineAndShapeRenderer r = new XYLineAndShapeRenderer();
                r.setBaseShapesVisible(type != EventValueDescription.ValueType.PERCENT);
                renderer = r;
            }
            XYPlot xyPlot = mChart.getXYPlot();
            xyPlot.setDataset(mDataSetCount, dataset);
            xyPlot.setRenderer(mDataSetCount, renderer);
            NumberAxis axis = new NumberAxis(type.toString());
            if (type == EventValueDescription.ValueType.PERCENT) {
                axis.setAutoRange(false);
                axis.setRange(0., 100.);
            }
            int count = mDataSetCount;
            if (mOccurrenceDataSet != null) {
                count--;
            }
            xyPlot.setRangeAxis(count, axis);
            if ((count % 2) == 0) {
                xyPlot.setRangeAxisLocation(count, AxisLocation.BOTTOM_OR_LEFT);
            } else {
                xyPlot.setRangeAxisLocation(count, AxisLocation.TOP_OR_RIGHT);
            }
            xyPlot.mapDatasetToRangeAxis(mDataSetCount, count);
            mDataSetCount++;
        }
        return dataset;
    }
    private String getSeriesLabel(EventContainer event, OccurrenceDisplayDescriptor descriptor)
            throws InvalidTypeException {
        if (descriptor.seriesValueIndex != -1) {
            if (descriptor.includePid == false) {
                return event.getValueAsString(descriptor.seriesValueIndex);
            } else {
                return String.format("%1$s (%2$d)",
                        event.getValueAsString(descriptor.seriesValueIndex), event.pid);
            }
        }
        return Integer.toString(event.pid);
    }
    private TimeSeriesCollection getOccurrenceDataSet() {
        if (mOccurrenceDataSet == null) {
            mOccurrenceDataSet = new TimeSeriesCollection();
            XYPlot xyPlot = mChart.getXYPlot();
            xyPlot.setDataset(mDataSetCount, mOccurrenceDataSet);
            OccurrenceRenderer renderer = new OccurrenceRenderer();
            renderer.setBaseShapesVisible(false);
            xyPlot.setRenderer(mDataSetCount, renderer);
            mDataSetCount++;
        }
        return mOccurrenceDataSet;
    }
    @Override
    int getDisplayType() {
        return DISPLAY_TYPE_GRAPH;
    }
    @Override
    protected void setNewLogParser(EventLogParser logParser) {
        if (mChart != null) {
            mChart.setTitle(getChartTitle(logParser));
        }
    }
    private String getChartTitle(EventLogParser logParser) {
        if (mValueDescriptors.size() > 0) {
            String chartDesc = null;
            switch (mValueDescriptorCheck) {
                case EVENT_CHECK_SAME_TAG:
                    if (logParser != null) {
                        chartDesc = logParser.getTagMap().get(mValueDescriptors.get(0).eventTag);
                    }
                    break;
                case EVENT_CHECK_SAME_VALUE:
                    if (logParser != null) {
                        chartDesc = String.format("%1$s / %2$s",
                                logParser.getTagMap().get(mValueDescriptors.get(0).eventTag),
                                mValueDescriptors.get(0).valueName);
                    }
                    break;
            }
            if (chartDesc != null) {
                return String.format("%1$s - %2$s", mName, chartDesc);
            }
        }
        return mName;
    }
}