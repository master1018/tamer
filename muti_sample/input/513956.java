final class EventValueSelector extends Dialog {
    private static final int DLG_WIDTH = 400;
    private static final int DLG_HEIGHT = 300;
    private Shell mParent;
    private Shell mShell;
    private boolean mEditStatus;
    private Combo mEventCombo;
    private Combo mValueCombo;
    private Combo mSeriesCombo;
    private Button mDisplayPidCheckBox;
    private Combo mFilterCombo;
    private Combo mFilterMethodCombo;
    private Text mFilterValue;
    private Button mOkButton;
    private EventLogParser mLogParser;
    private OccurrenceDisplayDescriptor mDescriptor;
    private Integer[] mEventTags;
    private final ArrayList<Integer> mSeriesIndices = new ArrayList<Integer>();
    public EventValueSelector(Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.BORDER | SWT.APPLICATION_MODAL);
    }
    boolean open(Class<? extends OccurrenceDisplayDescriptor> descriptorClass,
            EventLogParser logParser) {
        try {
            OccurrenceDisplayDescriptor descriptor = descriptorClass.newInstance();
            setModified();
            return open(descriptor, logParser);
        } catch (InstantiationException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
    boolean open(OccurrenceDisplayDescriptor descriptor, EventLogParser logParser) {
        if (descriptor instanceof ValueDisplayDescriptor) {
            mDescriptor = new ValueDisplayDescriptor((ValueDisplayDescriptor)descriptor);
        } else if (descriptor instanceof OccurrenceDisplayDescriptor) {
            mDescriptor = new OccurrenceDisplayDescriptor(descriptor);
        } else {
            return false;
        }
        mLogParser = logParser;
        createUI();
        if (mParent == null || mShell == null) {
            return false;
        }
        loadValueDescriptor();
        checkValidity();
        try { 
            mShell.setMinimumSize(DLG_WIDTH, DLG_HEIGHT);
            Rectangle r = mParent.getBounds();
            int cx = r.x + r.width/2;
            int x = cx - DLG_WIDTH / 2;
            int cy = r.y + r.height/2;
            int y = cy - DLG_HEIGHT / 2;
            mShell.setBounds(x, y, DLG_WIDTH, DLG_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mShell.layout();
        mShell.open();
        Display display = mParent.getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return mEditStatus;
    }
    OccurrenceDisplayDescriptor getDescriptor() {
        return mDescriptor;
    }
    private void createUI() {
        GridData gd;
        mParent = getParent();
        mShell = new Shell(mParent, getStyle());
        mShell.setText("Event Display Configuration");
        mShell.setLayout(new GridLayout(2, false));
        Label l = new Label(mShell, SWT.NONE);
        l.setText("Event:");
        mEventCombo = new Combo(mShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        mEventCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Map<Integer, String> eventTagMap = mLogParser.getTagMap();
        Map<Integer, EventValueDescription[]> eventInfoMap = mLogParser.getEventInfoMap();
        Set<Integer> keys = eventTagMap.keySet();
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (Integer i : keys) {
            if (eventInfoMap.get(i) != null) {
                String eventName = eventTagMap.get(i);
                mEventCombo.add(eventName);
                list.add(i);
            }
        }
        mEventTags = list.toArray(new Integer[list.size()]);
        mEventCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleEventComboSelection();
                setModified();
            }
        });
        l = new Label(mShell, SWT.NONE);
        l.setText("Value:");
        mValueCombo = new Combo(mShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        mValueCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mValueCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleValueComboSelection();
                setModified();
            }
        });
        l = new Label(mShell, SWT.NONE);
        l.setText("Series Name:");
        mSeriesCombo = new Combo(mShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        mSeriesCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSeriesCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSeriesComboSelection();
                setModified();
            }
        });
        new Composite(mShell, SWT.NONE).setLayoutData(gd = new GridData());
        gd.heightHint = gd.widthHint = 0;
        mDisplayPidCheckBox = new Button(mShell, SWT.CHECK);
        mDisplayPidCheckBox.setText("Also Show pid");
        mDisplayPidCheckBox.setEnabled(false);
        mDisplayPidCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mDescriptor.includePid = mDisplayPidCheckBox.getSelection();
                setModified();
            }
        });
        l = new Label(mShell, SWT.NONE);
        l.setText("Filter By:");
        mFilterCombo = new Combo(mShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        mFilterCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mFilterCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleFilterComboSelection();
                setModified();
            }
        });
        l = new Label(mShell, SWT.NONE);
        l.setText("Filter Method:");
        mFilterMethodCombo = new Combo(mShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        mFilterMethodCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (CompareMethod method : CompareMethod.values()) {
            mFilterMethodCombo.add(method.toString());
        }
        mFilterMethodCombo.select(0);
        mFilterMethodCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleFilterMethodComboSelection();
                setModified();
            }
        });
        l = new Label(mShell, SWT.NONE);
        l.setText("Filter Value:");
        mFilterValue = new Text(mShell, SWT.BORDER | SWT.SINGLE);
        mFilterValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mFilterValue.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (mDescriptor.filterValueIndex != -1) {
                    int index = mEventCombo.getSelectionIndex();
                    if (index != -1) {
                        int eventTag = mEventTags[index];
                        mDescriptor.eventTag = eventTag;
                        EventValueDescription valueDesc = mLogParser.getEventInfoMap()
                            .get(eventTag)[mDescriptor.filterValueIndex];
                        mDescriptor.filterValue = valueDesc.getObjectFromString(
                                mFilterValue.getText().trim());
                        setModified();
                    }
                }
            }
        });
        l = new Label(mShell, SWT.SEPARATOR | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        l.setLayoutData(gd);
        Composite buttonComp = new Composite(mShell, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        buttonComp.setLayoutData(gd);
        GridLayout gl;
        buttonComp.setLayout(gl = new GridLayout(6, true));
        gl.marginHeight = gl.marginWidth = 0;
        Composite padding = new Composite(mShell, SWT.NONE);
        padding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mOkButton = new Button(buttonComp, SWT.PUSH);
        mOkButton.setText("OK");
        mOkButton.setLayoutData(new GridData(GridData.CENTER));
        mOkButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mShell.close();
            }
        });
        padding = new Composite(mShell, SWT.NONE);
        padding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        padding = new Composite(mShell, SWT.NONE);
        padding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(GridData.CENTER));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mEditStatus = false;
                mShell.close();
            }
        });
        padding = new Composite(mShell, SWT.NONE);
        padding.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mShell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                event.doit = true;
            }
        });
    }
    private void setModified() {
        mEditStatus = true;
    }
    private void handleEventComboSelection() {
        int index = mEventCombo.getSelectionIndex();
        if (index != -1) {
            int eventTag = mEventTags[index];
            mDescriptor.eventTag = eventTag;
            EventValueDescription[] values = mLogParser.getEventInfoMap().get(eventTag);
            mValueCombo.removeAll();
            if (values != null) {
                if (mDescriptor instanceof ValueDisplayDescriptor) {
                    ValueDisplayDescriptor valueDescriptor = (ValueDisplayDescriptor)mDescriptor;
                    mValueCombo.setEnabled(true);
                    for (EventValueDescription value : values) {
                        mValueCombo.add(value.toString());
                    }
                    if (valueDescriptor.valueIndex != -1) {
                        mValueCombo.select(valueDescriptor.valueIndex);
                    } else {
                        mValueCombo.clearSelection();
                    }
                } else {
                    mValueCombo.setEnabled(false);
                }
                mSeriesCombo.removeAll();
                mSeriesCombo.setEnabled(false);
                mSeriesIndices.clear();
                int axisIndex = 0;
                int selectionIndex = -1;
                for (EventValueDescription value : values) {
                    if (value.getEventValueType() == EventValueType.STRING) {
                        mSeriesCombo.add(value.getName());
                        mSeriesCombo.setEnabled(true);
                        mSeriesIndices.add(axisIndex);
                        if (mDescriptor.seriesValueIndex != -1 &&
                                mDescriptor.seriesValueIndex == axisIndex) {
                            selectionIndex = axisIndex;
                        }
                    }
                    axisIndex++;
                }
                if (mSeriesCombo.isEnabled()) {
                    mSeriesCombo.add("default (pid)", 0 );
                    mSeriesIndices.add(0 , -1 );
                    mSeriesCombo.select(selectionIndex + 1);
                    if (selectionIndex >= 0) {
                        mDisplayPidCheckBox.setSelection(mDescriptor.includePid);
                        mDisplayPidCheckBox.setEnabled(true);
                    } else {
                        mDisplayPidCheckBox.setEnabled(false);
                        mDisplayPidCheckBox.setSelection(false);
                    }
                } else {
                    mDisplayPidCheckBox.setSelection(false);
                    mDisplayPidCheckBox.setEnabled(false);
                }
                mFilterCombo.setEnabled(true);
                mFilterCombo.removeAll();
                mFilterCombo.add("(no filter)");
                for (EventValueDescription value : values) {
                    mFilterCombo.add(value.toString());
                }
                mFilterCombo.select(mDescriptor.filterValueIndex + 1);
                mFilterMethodCombo.select(getFilterMethodIndex(mDescriptor.filterCompareMethod));
                if (mDescriptor.filterValueIndex != -1) {
                    EventValueDescription valueInfo = values[mDescriptor.filterValueIndex];
                    if (valueInfo.checkForType(mDescriptor.filterValue)) {
                        mFilterValue.setText(mDescriptor.filterValue.toString());
                    } else {
                        mFilterValue.setText("");
                    }
                } else {
                    mFilterValue.setText("");
                }
            } else {
                disableSubCombos();
            }
        } else {
            disableSubCombos();
        }
        checkValidity();
    }
    private void disableSubCombos() {
        mValueCombo.removeAll();
        mValueCombo.clearSelection();
        mValueCombo.setEnabled(false);
        mSeriesCombo.removeAll();
        mSeriesCombo.clearSelection();
        mSeriesCombo.setEnabled(false);
        mDisplayPidCheckBox.setEnabled(false);
        mDisplayPidCheckBox.setSelection(false);
        mFilterCombo.removeAll();
        mFilterCombo.clearSelection();
        mFilterCombo.setEnabled(false);
        mFilterValue.setEnabled(false);
        mFilterValue.setText("");
        mFilterMethodCombo.setEnabled(false);
    }
    private void handleValueComboSelection() {
        ValueDisplayDescriptor valueDescriptor = (ValueDisplayDescriptor)mDescriptor;
        int index = mValueCombo.getSelectionIndex();
        valueDescriptor.valueIndex = index;
        int eventIndex = mEventCombo.getSelectionIndex();
        int eventTag = mEventTags[eventIndex];
        EventValueDescription[] values = mLogParser.getEventInfoMap().get(eventTag);
        valueDescriptor.valueName = values[index].getName();
        checkValidity();
    }
    private void handleSeriesComboSelection() {
        int index = mSeriesCombo.getSelectionIndex();
        int valueIndex = mSeriesIndices.get(index);
        mDescriptor.seriesValueIndex = valueIndex;
        if (index > 0) {
            mDisplayPidCheckBox.setEnabled(true);
            mDisplayPidCheckBox.setSelection(mDescriptor.includePid);
        } else {
            mDisplayPidCheckBox.setSelection(false);
            mDisplayPidCheckBox.setEnabled(false);
        }
    }
    private void handleFilterComboSelection() {
        int index = mFilterCombo.getSelectionIndex();
        index--;
        mDescriptor.filterValueIndex = index;
        if (index != -1) {
            mFilterValue.setEnabled(true);
            mFilterMethodCombo.setEnabled(true);
            if (mDescriptor.filterValue instanceof String) {
                mFilterValue.setText((String)mDescriptor.filterValue);
            }
        } else {
            mFilterValue.setText("");
            mFilterValue.setEnabled(false);
            mFilterMethodCombo.setEnabled(false);
        }
    }
    private void handleFilterMethodComboSelection() {
        int index = mFilterMethodCombo.getSelectionIndex();
        CompareMethod method = CompareMethod.values()[index];
        mDescriptor.filterCompareMethod = method;
    }
    private int getFilterMethodIndex(CompareMethod filterCompareMethod) {
        CompareMethod[] values = CompareMethod.values();
        for (int i = 0 ; i < values.length ; i++) {
            if (values[i] == filterCompareMethod) {
                return i;
            }
        }
        return -1;
    }
    private void loadValueDescriptor() {
        int eventIndex = 0;
        int comboIndex = -1;
        for (int i : mEventTags) {
            if (i == mDescriptor.eventTag) {
                comboIndex = eventIndex;
                break;
            }
            eventIndex++;
        }
        if (comboIndex == -1) {
            mEventCombo.clearSelection();
        } else {
            mEventCombo.select(comboIndex);
        }
        handleEventComboSelection();
    }
    private void checkValidity() {
        mOkButton.setEnabled(mEventCombo.getSelectionIndex() != -1 &&
                (((mDescriptor instanceof ValueDisplayDescriptor) == false) ||
                        mValueCombo.getSelectionIndex() != -1));
    }
}
