public class DisplayLog extends EventDisplay {
    public DisplayLog(String name) {
        super(name);
    }
    private final static String PREFS_COL_DATE = "EventLogPanel.log.Col1"; 
    private final static String PREFS_COL_PID = "EventLogPanel.log.Col2"; 
    private final static String PREFS_COL_EVENTTAG = "EventLogPanel.log.Col3"; 
    private final static String PREFS_COL_VALUENAME = "EventLogPanel.log.Col4"; 
    private final static String PREFS_COL_VALUE = "EventLogPanel.log.Col5"; 
    private final static String PREFS_COL_TYPE = "EventLogPanel.log.Col6"; 
    @Override
    void resetUI() {
        mLogTable.removeAll();
    }
    @Override
    void newEvent(EventContainer event, EventLogParser logParser) {
        addToLog(event, logParser);
    }
    @Override
    Control createComposite(Composite parent, EventLogParser logParser, ILogColumnListener listener) {
        return createLogUI(parent, listener);
    }
    private void addToLog(EventContainer event, EventLogParser logParser) {
        ScrollBar bar = mLogTable.getVerticalBar();
        boolean scroll = bar.getMaximum() == bar.getSelection() + bar.getThumb();
        Calendar c = Calendar.getInstance();
        long msec = (long) event.sec * 1000L;
        c.setTimeInMillis(msec);
        String date = String.format("%1$tF %1$tT", c);
        String eventName = logParser.getTagMap().get(event.mTag);
        String pidName = Integer.toString(event.pid);
        EventValueDescription[] valueDescription = logParser.getEventInfoMap().get(event.mTag);
        if (valueDescription != null) {
            for (int i = 0; i < valueDescription.length; i++) {
                EventValueDescription description = valueDescription[i];
                try {
                    String value = event.getValueAsString(i);
                    logValue(date, pidName, eventName, description.getName(), value,
                            description.getEventValueType(), description.getValueType());
                } catch (InvalidTypeException e) {
                    logValue(date, pidName, eventName, description.getName(), e.getMessage(),
                            description.getEventValueType(), description.getValueType());
                }
            }
            if (scroll) {
                int itemCount = mLogTable.getItemCount();
                if (itemCount > 0) {
                    mLogTable.showItem(mLogTable.getItem(itemCount - 1));
                }
            }
        }
    }
    protected void addToLog(EventContainer event, EventLogParser logParser,
            ArrayList<ValueDisplayDescriptor> valueDescriptors,
            ArrayList<OccurrenceDisplayDescriptor> occurrenceDescriptors) {
        ScrollBar bar = mLogTable.getVerticalBar();
        boolean scroll = bar.getMaximum() == bar.getSelection() + bar.getThumb();
        Calendar c = Calendar.getInstance();
        long msec = (long) event.sec * 1000L;
        c.setTimeInMillis(msec);
        String date = String.format("%1$tF %1$tT", c);
        String eventName = logParser.getTagMap().get(event.mTag);
        String pidName = Integer.toString(event.pid);
        if (valueDescriptors.size() > 0) {
            for (ValueDisplayDescriptor descriptor : valueDescriptors) {
                logDescriptor(event, descriptor, date, pidName, eventName, logParser);
            }
        } else {
        }
        if (scroll) {
            int itemCount = mLogTable.getItemCount();
            if (itemCount > 0) {
                mLogTable.showItem(mLogTable.getItem(itemCount - 1));
            }
        }
    }
    private void logValue(String date, String pid, String event, String valueName,
            String value, EventContainer.EventValueType eventValueType, EventValueDescription.ValueType valueType) {
        TableItem item = new TableItem(mLogTable, SWT.NONE);
        item.setText(0, date);
        item.setText(1, pid);
        item.setText(2, event);
        item.setText(3, valueName);
        item.setText(4, value);
        String type;
        if (valueType != EventValueDescription.ValueType.NOT_APPLICABLE) {
            type = String.format("%1$s, %2$s", eventValueType.toString(), valueType.toString());
        } else {
            type = eventValueType.toString();
        }
        item.setText(5, type);
    }
    private void logDescriptor(EventContainer event, ValueDisplayDescriptor descriptor,
            String date, String pidName, String eventName, EventLogParser logParser) {
        String value;
        try {
            value = event.getValueAsString(descriptor.valueIndex);
        } catch (InvalidTypeException e) {
            value = e.getMessage();
        }
        EventValueDescription[] values = logParser.getEventInfoMap().get(event.mTag);
        EventValueDescription valueDescription = values[descriptor.valueIndex];
        logValue(date, pidName, eventName, descriptor.valueName, value,
                valueDescription.getEventValueType(), valueDescription.getValueType());
    }
    private Control createLogUI(Composite parent, final ILogColumnListener listener) {
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout gl;
        mainComp.setLayout(gl = new GridLayout(1, false));
        gl.marginHeight = gl.marginWidth = 0;
        mainComp.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                mLogTable = null;
            }
        });
        Label l = new Label(mainComp, SWT.CENTER);
        l.setText(mName);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mLogTable = new Table(mainComp, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL |
                SWT.BORDER);
        mLogTable.setLayoutData(new GridData(GridData.FILL_BOTH));
        IPreferenceStore store = DdmUiPreferences.getStore();
        TableColumn col = TableHelper.createTableColumn(
                mLogTable, "Time",
                SWT.LEFT, "0000-00-00 00:00:00", PREFS_COL_DATE, store); 
        col.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Object source = e.getSource();
                if (source instanceof TableColumn) {
                    listener.columnResized(0, (TableColumn) source);
                }
            }
        });
        col = TableHelper.createTableColumn(
                mLogTable, "pid",
                SWT.LEFT, "0000", PREFS_COL_PID, store); 
        col.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Object source = e.getSource();
                if (source instanceof TableColumn) {
                    listener.columnResized(1, (TableColumn) source);
                }
            }
        });
        col = TableHelper.createTableColumn(
                mLogTable, "Event",
                SWT.LEFT, "abcdejghijklmno", PREFS_COL_EVENTTAG, store); 
        col.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Object source = e.getSource();
                if (source instanceof TableColumn) {
                    listener.columnResized(2, (TableColumn) source);
                }
            }
        });
        col = TableHelper.createTableColumn(
                mLogTable, "Name",
                SWT.LEFT, "Process Name", PREFS_COL_VALUENAME, store); 
        col.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Object source = e.getSource();
                if (source instanceof TableColumn) {
                    listener.columnResized(3, (TableColumn) source);
                }
            }
        });
        col = TableHelper.createTableColumn(
                mLogTable, "Value",
                SWT.LEFT, "0000000", PREFS_COL_VALUE, store); 
        col.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Object source = e.getSource();
                if (source instanceof TableColumn) {
                    listener.columnResized(4, (TableColumn) source);
                }
            }
        });
        col = TableHelper.createTableColumn(
                mLogTable, "Type",
                SWT.LEFT, "long, seconds", PREFS_COL_TYPE, store); 
        col.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Object source = e.getSource();
                if (source instanceof TableColumn) {
                    listener.columnResized(5, (TableColumn) source);
                }
            }
        });
        mLogTable.setHeaderVisible(true);
        mLogTable.setLinesVisible(true);
        return mainComp;
    }
    @Override
    void resizeColumn(int index, TableColumn sourceColumn) {
        if (mLogTable != null) {
            TableColumn col = mLogTable.getColumn(index);
            if (col != sourceColumn) {
                col.setWidth(sourceColumn.getWidth());
            }
        }
    }
    @Override
    int getDisplayType() {
        return DISPLAY_TYPE_LOG_ALL;
    }
}
