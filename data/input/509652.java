class EventDisplayOptions  extends Dialog {
    private static final int DLG_WIDTH = 700;
    private static final int DLG_HEIGHT = 700;
    private IImageLoader mImageLoader;
    private Shell mParent;
    private Shell mShell;
    private boolean mEditStatus = false;
    private final ArrayList<EventDisplay> mDisplayList = new ArrayList<EventDisplay>();
    private List mEventDisplayList;
    private Button mEventDisplayNewButton;
    private Button mEventDisplayDeleteButton;
    private Button mEventDisplayUpButton;
    private Button mEventDisplayDownButton;
    private Text mDisplayWidthText;
    private Text mDisplayHeightText;
    private Text mDisplayNameText;
    private Combo mDisplayTypeCombo;
    private Group mChartOptions;
    private Group mHistOptions;
    private Button mPidFilterCheckBox;
    private Text mPidText;
    private Map<Integer, String> mEventTagMap;
    private Map<Integer, EventValueDescription[]> mEventDescriptionMap;
    private ArrayList<Integer> mPidList;
    private EventLogParser mLogParser;
    private Group mInfoGroup;
    private static class SelectionWidgets {
        private List mList;
        private Button mNewButton;
        private Button mEditButton;
        private Button mDeleteButton;
        private void setEnabled(boolean enable) {
            mList.setEnabled(enable);
            mNewButton.setEnabled(enable);
            mEditButton.setEnabled(enable);
            mDeleteButton.setEnabled(enable);
        }
    }
    private SelectionWidgets mValueSelection;
    private SelectionWidgets mOccurrenceSelection;
    private boolean mProcessTextChanges = true;
    private Text mTimeLimitText;
    private Text mHistWidthText;
    EventDisplayOptions(IImageLoader imageLoader, Shell parent) {
        super(parent, SWT.DIALOG_TRIM | SWT.BORDER | SWT.APPLICATION_MODAL);
        mImageLoader = imageLoader;
    }
    boolean open(EventLogParser logParser, ArrayList<EventDisplay> displayList,
            ArrayList<EventContainer> eventList) {
        mLogParser = logParser;
        if (logParser != null) {
            mEventTagMap = logParser.getTagMap();
            mEventDescriptionMap = logParser.getEventInfoMap();
        }
        duplicateEventDisplay(displayList);
        buildPidList(eventList);
        createUI();
        if (mParent == null || mShell == null) {
            return false;
        }
        mShell.setMinimumSize(DLG_WIDTH, DLG_HEIGHT);
        Rectangle r = mParent.getBounds();
        int cx = r.x + r.width/2;
        int x = cx - DLG_WIDTH / 2;
        int cy = r.y + r.height/2;
        int y = cy - DLG_HEIGHT / 2;
        mShell.setBounds(x, y, DLG_WIDTH, DLG_HEIGHT);
        mShell.layout();
        mShell.open();
        Display display = mParent.getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return mEditStatus;
    }
    ArrayList<EventDisplay> getEventDisplays() {
        return mDisplayList;
    }
    private void createUI() {
        mParent = getParent();
        mShell = new Shell(mParent, getStyle());
        mShell.setText("Event Display Configuration");
        mShell.setLayout(new GridLayout(1, true));
        final Composite topPanel = new Composite(mShell, SWT.NONE);
        topPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        topPanel.setLayout(new GridLayout(2, false));
        Composite leftPanel = new Composite(topPanel, SWT.NONE);
        Composite rightPanel = new Composite(topPanel, SWT.NONE);
        createLeftPanel(leftPanel);
        createRightPanel(rightPanel);
        mShell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                event.doit = true;
            }
        });
        Label separator = new Label(mShell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Composite bottomButtons = new Composite(mShell, SWT.NONE);
        bottomButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gl;
        bottomButtons.setLayout(gl = new GridLayout(2, true));
        gl.marginHeight = gl.marginWidth = 0;
        Button okButton = new Button(bottomButtons, SWT.PUSH);
        okButton.setText("OK");
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mShell.close();
            }
        });
        Button cancelButton = new Button(bottomButtons, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mEditStatus = false;
                mShell.close();
            }
        });
        enable(false);
        fillEventDisplayList();
    }
    private void createLeftPanel(Composite leftPanel) {
        final IPreferenceStore store = DdmUiPreferences.getStore();
        GridLayout gl;
        leftPanel.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        leftPanel.setLayout(gl = new GridLayout(1, false));
        gl.verticalSpacing = 1;
        mEventDisplayList = new List(leftPanel,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
        mEventDisplayList.setLayoutData(new GridData(GridData.FILL_BOTH));
        mEventDisplayList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleEventDisplaySelection();
            }
        });
        Composite bottomControls = new Composite(leftPanel, SWT.NONE);
        bottomControls.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bottomControls.setLayout(gl = new GridLayout(5, false));
        gl.marginHeight = gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        mEventDisplayNewButton = new Button(bottomControls, SWT.PUSH | SWT.FLAT);
        mEventDisplayNewButton.setImage(mImageLoader.loadImage("add.png", 
                leftPanel.getDisplay()));
        mEventDisplayNewButton.setToolTipText("Adds a new event display");
        mEventDisplayNewButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        mEventDisplayNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createNewEventDisplay();
            }
        });
        mEventDisplayDeleteButton = new Button(bottomControls, SWT.PUSH | SWT.FLAT);
        mEventDisplayDeleteButton.setImage(mImageLoader.loadImage("delete.png", 
                leftPanel.getDisplay()));
        mEventDisplayDeleteButton.setToolTipText("Deletes the selected event display");
        mEventDisplayDeleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        mEventDisplayDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteEventDisplay();
            }
        });
        mEventDisplayUpButton = new Button(bottomControls, SWT.PUSH | SWT.FLAT);
        mEventDisplayUpButton.setImage(mImageLoader.loadImage("up.png", 
                leftPanel.getDisplay()));
        mEventDisplayUpButton.setToolTipText("Moves the selected event display up");
        mEventDisplayUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selection = mEventDisplayList.getSelectionIndex();
                if (selection > 0) {
                    EventDisplay display = mDisplayList.remove(selection);
                    mDisplayList.add(selection - 1, display);
                    mEventDisplayList.remove(selection);
                    mEventDisplayList.add(display.getName(), selection - 1);
                    mEventDisplayList.select(selection - 1);
                    handleEventDisplaySelection();
                    mEventDisplayList.showSelection();
                    setModified();
                }
            }
        });
        mEventDisplayDownButton = new Button(bottomControls, SWT.PUSH | SWT.FLAT);
        mEventDisplayDownButton.setImage(mImageLoader.loadImage("down.png", 
                leftPanel.getDisplay()));
        mEventDisplayDownButton.setToolTipText("Moves the selected event display down");
        mEventDisplayDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selection = mEventDisplayList.getSelectionIndex();
                if (selection != -1 && selection < mEventDisplayList.getItemCount() - 1) {
                    EventDisplay display = mDisplayList.remove(selection);
                    mDisplayList.add(selection + 1, display);
                    mEventDisplayList.remove(selection);
                    mEventDisplayList.add(display.getName(), selection + 1);
                    mEventDisplayList.select(selection + 1);
                    handleEventDisplaySelection();
                    mEventDisplayList.showSelection();
                    setModified();
                }
            }
        });
        Group sizeGroup = new Group(leftPanel, SWT.NONE);
        sizeGroup.setText("Display Size:");
        sizeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sizeGroup.setLayout(new GridLayout(2, false));
        Label l = new Label(sizeGroup, SWT.NONE);
        l.setText("Width:");
        mDisplayWidthText = new Text(sizeGroup, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        mDisplayWidthText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDisplayWidthText.setText(Integer.toString(
                store.getInt(EventLogPanel.PREFS_DISPLAY_WIDTH)));
        mDisplayWidthText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String text = mDisplayWidthText.getText().trim();
                try {
                    store.setValue(EventLogPanel.PREFS_DISPLAY_WIDTH, Integer.parseInt(text));
                    setModified();
                } catch (NumberFormatException nfe) {
                }
            }
        });
        l = new Label(sizeGroup, SWT.NONE);
        l.setText("Height:");
        mDisplayHeightText = new Text(sizeGroup, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
        mDisplayHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDisplayHeightText.setText(Integer.toString(
                store.getInt(EventLogPanel.PREFS_DISPLAY_HEIGHT)));
        mDisplayHeightText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String text = mDisplayHeightText.getText().trim();
                try {
                    store.setValue(EventLogPanel.PREFS_DISPLAY_HEIGHT, Integer.parseInt(text));
                    setModified();
                } catch (NumberFormatException nfe) {
                }
            }
        });
    }
    private void createRightPanel(Composite rightPanel) {
        rightPanel.setLayout(new GridLayout(1, true));
        rightPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        mInfoGroup = new Group(rightPanel, SWT.NONE);
        mInfoGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mInfoGroup.setLayout(new GridLayout(2, false));
        Label nameLabel = new Label(mInfoGroup, SWT.LEFT);
        nameLabel.setText("Name:");
        mDisplayNameText = new Text(mInfoGroup, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
        mDisplayNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDisplayNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (mProcessTextChanges) {
                    EventDisplay eventDisplay = getCurrentEventDisplay();
                    if (eventDisplay != null) {
                        eventDisplay.setName(mDisplayNameText.getText());
                        int index = mEventDisplayList.getSelectionIndex();
                        mEventDisplayList.remove(index);
                        mEventDisplayList.add(eventDisplay.getName(), index);
                        mEventDisplayList.select(index);
                        handleEventDisplaySelection();
                        setModified();
                    }
                }
            }
        });
        Label displayLabel = new Label(mInfoGroup, SWT.LEFT);
        displayLabel.setText("Type:");
        mDisplayTypeCombo = new Combo(mInfoGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
        mDisplayTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDisplayTypeCombo.add("Log All");
        mDisplayTypeCombo.add("Filtered Log");
        mDisplayTypeCombo.add("Graph");
        mDisplayTypeCombo.add("Sync");
        mDisplayTypeCombo.add("Sync Histogram");
        mDisplayTypeCombo.add("Sync Performance");
        mDisplayTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                EventDisplay eventDisplay = getCurrentEventDisplay();
                if (eventDisplay != null && eventDisplay.getDisplayType() != mDisplayTypeCombo.getSelectionIndex()) {
                    setModified();
                    String name = eventDisplay.getName();
                    EventDisplay newEventDisplay = EventDisplay.eventDisplayFactory(mDisplayTypeCombo.getSelectionIndex(), name);
                    setCurrentEventDisplay(newEventDisplay);
                    fillUiWith(newEventDisplay);
                }
            }
        });
        mChartOptions = new Group(mInfoGroup, SWT.NONE);
        mChartOptions.setText("Chart Options");
        GridData gd;
        mChartOptions.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        mChartOptions.setLayout(new GridLayout(2, false));
        Label l = new Label(mChartOptions, SWT.NONE);
        l.setText("Time Limit (seconds):");
        mTimeLimitText = new Text(mChartOptions, SWT.BORDER);
        mTimeLimitText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mTimeLimitText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                String text = mTimeLimitText.getText().trim();
                EventDisplay eventDisplay = getCurrentEventDisplay();
                if (eventDisplay != null) {
                    try {
                        if (text.length() == 0) {
                            eventDisplay.resetChartTimeLimit();
                        } else {
                            eventDisplay.setChartTimeLimit(Long.parseLong(text));
                        }
                    } catch (NumberFormatException nfe) {
                        eventDisplay.resetChartTimeLimit();
                    } finally {
                        setModified();
                    }
                }
            }
        });
        mHistOptions = new Group(mInfoGroup, SWT.NONE);
        mHistOptions.setText("Histogram Options");
        GridData gdh;
        mHistOptions.setLayoutData(gdh = new GridData(GridData.FILL_HORIZONTAL));
        gdh.horizontalSpan = 2;
        mHistOptions.setLayout(new GridLayout(2, false));
        Label lh = new Label(mHistOptions, SWT.NONE);
        lh.setText("Histogram width (hours):");
        mHistWidthText = new Text(mHistOptions, SWT.BORDER);
        mHistWidthText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mHistWidthText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                String text = mHistWidthText.getText().trim();
                EventDisplay eventDisplay = getCurrentEventDisplay();
                if (eventDisplay != null) {
                    try {
                        if (text.length() == 0) {
                            eventDisplay.resetHistWidth();
                        } else {
                            eventDisplay.setHistWidth(Long.parseLong(text));
                        }
                    } catch (NumberFormatException nfe) {
                        eventDisplay.resetHistWidth();
                    } finally {
                        setModified();
                    }
                }
            }
        });
        mPidFilterCheckBox = new Button(mInfoGroup, SWT.CHECK);
        mPidFilterCheckBox.setText("Enable filtering by pid");
        mPidFilterCheckBox.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        mPidFilterCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                EventDisplay eventDisplay = getCurrentEventDisplay();
                if (eventDisplay != null) {
                    eventDisplay.setPidFiltering(mPidFilterCheckBox.getSelection());
                    mPidText.setEnabled(mPidFilterCheckBox.getSelection());
                    setModified();
                }
            }
        });
        Label pidLabel = new Label(mInfoGroup, SWT.NONE);
        pidLabel.setText("Pid Filter:");
        pidLabel.setToolTipText("Enter all pids, separated by commas");
        mPidText = new Text(mInfoGroup, SWT.BORDER);
        mPidText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mPidText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (mProcessTextChanges) {
                    EventDisplay eventDisplay = getCurrentEventDisplay();
                    if (eventDisplay != null && eventDisplay.getPidFiltering()) {
                        String pidText = mPidText.getText().trim();
                        String[] pids = pidText.split("\\s*,\\s*"); 
                        ArrayList<Integer> list = new ArrayList<Integer>();
                        for (String pid : pids) {
                            try {
                                list.add(Integer.valueOf(pid));
                            } catch (NumberFormatException nfe) {
                            }
                        }
                        eventDisplay.setPidFilterList(list);
                        setModified();
                    }
                }
            }
        });
        mValueSelection = createEventSelection(rightPanel, ValueDisplayDescriptor.class,
                "Event Value Display");
        mOccurrenceSelection = createEventSelection(rightPanel, OccurrenceDisplayDescriptor.class,
                "Event Occurrence Display");
    }
    private SelectionWidgets createEventSelection(Composite rightPanel,
            final Class<? extends OccurrenceDisplayDescriptor> descriptorClass,
            String groupMessage) {
        Group eventSelectionPanel = new Group(rightPanel, SWT.NONE);
        eventSelectionPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl;
        eventSelectionPanel.setLayout(gl = new GridLayout(2, false));
        gl.marginHeight = gl.marginWidth = 0;
        eventSelectionPanel.setText(groupMessage);
        final SelectionWidgets widgets = new SelectionWidgets();
        widgets.mList = new List(eventSelectionPanel, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        widgets.mList.setLayoutData(new GridData(GridData.FILL_BOTH));
        widgets.mList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = widgets.mList.getSelectionIndex();
                if (index != -1) {
                    widgets.mDeleteButton.setEnabled(true);
                    widgets.mEditButton.setEnabled(true);
                } else {
                    widgets.mDeleteButton.setEnabled(false);
                    widgets.mEditButton.setEnabled(false);
                }
            }
        });
        Composite rightControls = new Composite(eventSelectionPanel, SWT.NONE);
        rightControls.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        rightControls.setLayout(gl = new GridLayout(1, false));
        gl.marginHeight = gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        widgets.mNewButton = new Button(rightControls, SWT.PUSH | SWT.FLAT);
        widgets.mNewButton.setText("New...");
        widgets.mNewButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        widgets.mNewButton.setEnabled(false);
        widgets.mNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    EventDisplay eventDisplay = getCurrentEventDisplay();
                    if (eventDisplay != null) {
                        EventValueSelector dialog = new EventValueSelector(mShell);
                        if (dialog.open(descriptorClass, mLogParser)) {
                            eventDisplay.addDescriptor(dialog.getDescriptor());
                            fillUiWith(eventDisplay);
                            setModified();
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        widgets.mEditButton = new Button(rightControls, SWT.PUSH | SWT.FLAT);
        widgets.mEditButton.setText("Edit...");
        widgets.mEditButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        widgets.mEditButton.setEnabled(false);
        widgets.mEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                EventDisplay eventDisplay = getCurrentEventDisplay();
                if (eventDisplay != null) {
                    int index = widgets.mList.getSelectionIndex();
                    if (index != -1) {
                        OccurrenceDisplayDescriptor descriptor = eventDisplay.getDescriptor(
                                descriptorClass, index);
                        EventValueSelector dialog = new EventValueSelector(mShell);
                        if (dialog.open(descriptor, mLogParser)) {
                            descriptor.replaceWith(dialog.getDescriptor());
                            eventDisplay.updateValueDescriptorCheck();
                            fillUiWith(eventDisplay);
                            widgets.mList.select(index);
                            widgets.mList.notifyListeners(SWT.Selection, null);
                            setModified();
                        }
                    }
                }
            }
        });
        widgets.mDeleteButton = new Button(rightControls, SWT.PUSH | SWT.FLAT);
        widgets.mDeleteButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        widgets.mDeleteButton.setText("Delete");
        widgets.mDeleteButton.setEnabled(false);
        widgets.mDeleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                EventDisplay eventDisplay = getCurrentEventDisplay();
                if (eventDisplay != null) {
                    int index = widgets.mList.getSelectionIndex();
                    if (index != -1) {
                        eventDisplay.removeDescriptor(descriptorClass, index);
                        fillUiWith(eventDisplay);
                        setModified();
                    }
                }
            }
        });
        return widgets;
    }
    private void duplicateEventDisplay(ArrayList<EventDisplay> displayList) {
        for (EventDisplay eventDisplay : displayList) {
            mDisplayList.add(EventDisplay.clone(eventDisplay));
        }
    }
    private void buildPidList(ArrayList<EventContainer> eventList) {
        mPidList = new ArrayList<Integer>();
        for (EventContainer event : eventList) {
            if (mPidList.indexOf(event.pid) == -1) {
                mPidList.add(event.pid);
            }
        }
    }
    private void setModified() {
        mEditStatus = true;
    }
    private void enable(boolean status) {
        mEventDisplayDeleteButton.setEnabled(status);
        int selection = mEventDisplayList.getSelectionIndex();
        int count = mEventDisplayList.getItemCount();
        mEventDisplayUpButton.setEnabled(status && selection > 0);
        mEventDisplayDownButton.setEnabled(status && selection != -1 && selection < count - 1);
        mDisplayNameText.setEnabled(status);
        mDisplayTypeCombo.setEnabled(status);
        mPidFilterCheckBox.setEnabled(status);
        mValueSelection.setEnabled(status);
        mOccurrenceSelection.setEnabled(status);
        mValueSelection.mNewButton.setEnabled(status);
        mOccurrenceSelection.mNewButton.setEnabled(status);
        if (status == false) {
            mPidText.setEnabled(false);
        }
    }
    private void fillEventDisplayList() {
        for (EventDisplay eventDisplay : mDisplayList) {
            mEventDisplayList.add(eventDisplay.getName());
        }
    }
    private void createNewEventDisplay() {
        int count = mDisplayList.size();
        String name = String.format("display %1$d", count + 1);
        EventDisplay eventDisplay = EventDisplay.eventDisplayFactory(0 , name);
        mDisplayList.add(eventDisplay);
        mEventDisplayList.add(name);
        mEventDisplayList.select(count);
        handleEventDisplaySelection();
        mEventDisplayList.showSelection();
        setModified();
    }
    private void deleteEventDisplay() {
        int selection = mEventDisplayList.getSelectionIndex();
        if (selection != -1) {
            mDisplayList.remove(selection);
            mEventDisplayList.remove(selection);
            if (mDisplayList.size() < selection) {
                selection--;
            }
            mEventDisplayList.select(selection);
            handleEventDisplaySelection();
            setModified();
        }
    }
    private EventDisplay getCurrentEventDisplay() {
        int selection = mEventDisplayList.getSelectionIndex();
        if (selection != -1) {
            return mDisplayList.get(selection);
        }
        return null;
    }
    private void setCurrentEventDisplay(EventDisplay eventDisplay) {
        int selection = mEventDisplayList.getSelectionIndex();
        if (selection != -1) {
            mDisplayList.set(selection, eventDisplay);
        }
    }
    private void handleEventDisplaySelection() {
        EventDisplay eventDisplay = getCurrentEventDisplay();
        if (eventDisplay != null) {
            enable(true);
            fillUiWith(eventDisplay);
        } else {
            enable(false);
            emptyUi();
        }
    }
    private void emptyUi() {
        mDisplayNameText.setText("");
        mDisplayTypeCombo.clearSelection();
        mValueSelection.mList.removeAll();
        mOccurrenceSelection.mList.removeAll();
    }
    private void fillUiWith(EventDisplay eventDisplay) {
        mProcessTextChanges = false;
        mDisplayNameText.setText(eventDisplay.getName());
        int displayMode = eventDisplay.getDisplayType();
        mDisplayTypeCombo.select(displayMode);
        if (displayMode == EventDisplay.DISPLAY_TYPE_GRAPH) {
            GridData gd = (GridData) mChartOptions.getLayoutData();
            gd.exclude = false;
            mChartOptions.setVisible(!gd.exclude);
            long limit = eventDisplay.getChartTimeLimit();
            if (limit != -1) {
                mTimeLimitText.setText(Long.toString(limit));
            } else {
                mTimeLimitText.setText(""); 
            }
        } else {
            GridData gd = (GridData) mChartOptions.getLayoutData();
            gd.exclude = true;
            mChartOptions.setVisible(!gd.exclude);
            mTimeLimitText.setText(""); 
        }
        if (displayMode == EventDisplay.DISPLAY_TYPE_SYNC_HIST) {
            GridData gd = (GridData) mHistOptions.getLayoutData();
            gd.exclude = false;
            mHistOptions.setVisible(!gd.exclude);
            long limit = eventDisplay.getHistWidth();
            if (limit != -1) {
                mHistWidthText.setText(Long.toString(limit));
            } else {
                mHistWidthText.setText(""); 
            }
        } else {
            GridData gd = (GridData) mHistOptions.getLayoutData();
            gd.exclude = true;
            mHistOptions.setVisible(!gd.exclude);
            mHistWidthText.setText(""); 
        }
        mInfoGroup.layout(true);
        mShell.layout(true);
        mShell.pack();
        if (eventDisplay.getPidFiltering()) {
            mPidFilterCheckBox.setSelection(true);
            mPidText.setEnabled(true);
            ArrayList<Integer> list = eventDisplay.getPidFilterList();
            if (list != null) {
                StringBuilder sb = new StringBuilder();
                int count = list.size();
                for (int i = 0 ; i < count ; i++) {
                    sb.append(list.get(i));
                    if (i < count - 1) {
                        sb.append(", ");
                    }
                }
                mPidText.setText(sb.toString());
            } else {
                mPidText.setText(""); 
            }
        } else {
            mPidFilterCheckBox.setSelection(false);
            mPidText.setEnabled(false);
            mPidText.setText(""); 
        }
        mProcessTextChanges = true;
        mValueSelection.mList.removeAll();
        mOccurrenceSelection.mList.removeAll();
        if (eventDisplay.getDisplayType() == EventDisplay.DISPLAY_TYPE_FILTERED_LOG ||
                eventDisplay.getDisplayType() == EventDisplay.DISPLAY_TYPE_GRAPH) {
            mOccurrenceSelection.setEnabled(true);
            mValueSelection.setEnabled(true);
            Iterator<ValueDisplayDescriptor> valueIterator = eventDisplay.getValueDescriptors();
            while (valueIterator.hasNext()) {
                ValueDisplayDescriptor descriptor = valueIterator.next();
                mValueSelection.mList.add(String.format("%1$s: %2$s [%3$s]%4$s",
                        mEventTagMap.get(descriptor.eventTag), descriptor.valueName,
                        getSeriesLabelDescription(descriptor), getFilterDescription(descriptor)));
            }
            Iterator<OccurrenceDisplayDescriptor> occurrenceIterator =
                eventDisplay.getOccurrenceDescriptors();
            while (occurrenceIterator.hasNext()) {
                OccurrenceDisplayDescriptor descriptor = occurrenceIterator.next();
                mOccurrenceSelection.mList.add(String.format("%1$s [%2$s]%3$s",
                        mEventTagMap.get(descriptor.eventTag),
                        getSeriesLabelDescription(descriptor),
                        getFilterDescription(descriptor)));
            }
            mValueSelection.mList.notifyListeners(SWT.Selection, null);
            mOccurrenceSelection.mList.notifyListeners(SWT.Selection, null);
        } else {
            mOccurrenceSelection.setEnabled(false);
            mValueSelection.setEnabled(false);
        }
    }
    private String getSeriesLabelDescription(OccurrenceDisplayDescriptor descriptor) {
        if (descriptor.seriesValueIndex != -1) {
            if (descriptor.includePid) {
                return String.format("%1$s + pid",
                        mEventDescriptionMap.get(
                                descriptor.eventTag)[descriptor.seriesValueIndex].getName());
            } else {
                return mEventDescriptionMap.get(descriptor.eventTag)[descriptor.seriesValueIndex]
                                                                     .getName();
            }
        }
        return "pid";
    }
    private String getFilterDescription(OccurrenceDisplayDescriptor descriptor) {
        if (descriptor.filterValueIndex != -1) {
            return String.format(" [%1$s %2$s %3$s]",
                    mEventDescriptionMap.get(
                            descriptor.eventTag)[descriptor.filterValueIndex].getName(),
                            descriptor.filterCompareMethod.testString(),
                            descriptor.filterValue != null ?
                                    descriptor.filterValue.toString() : "?"); 
        }
        return ""; 
    }
}
