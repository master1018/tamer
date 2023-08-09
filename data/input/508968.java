public class SysinfoPanel extends TablePanel implements IShellOutputReceiver {
    private Label mLabel;
    private Button mFetchButton;
    private Combo mDisplayMode;
    private DefaultPieDataset mDataset;
    private File mDataFile;
    private FileOutputStream mTempStream;
    private int mMode = 0;
    private static final int MODE_CPU = 0;
    private static final int MODE_ALARM = 1;
    private static final int MODE_WAKELOCK = 2;
    private static final int MODE_MEMINFO = 3;
    private static final int MODE_SYNC = 4;
    private static final String BUGREPORT_SECTION[] = {"cpuinfo", "alarm",
            "batteryinfo", "MEMORY INFO", "content"};
    private static final String DUMP_COMMAND[] = {"dumpsys cpuinfo",
            "dumpsys alarm", "dumpsys batteryinfo", "cat /proc/meminfo ; procrank",
            "dumpsys content"};
    private static final String CAPTIONS[] = {"CPU load", "Alarms",
            "Wakelocks", "Memory usage", "Sync"};
    public void generateDataset(File file) {
        mDataset.clear();
        mLabel.setText("");
        if (file == null) {
            return;
        }
        try {
            BufferedReader br = getBugreportReader(file);
            if (mMode == MODE_CPU) {
                readCpuDataset(br);
            } else if (mMode == MODE_ALARM) {
                readAlarmDataset(br);
            } else if (mMode == MODE_WAKELOCK) {
                readWakelockDataset(br);
            } else if (mMode == MODE_MEMINFO) {
                readMeminfoDataset(br);
            } else if (mMode == MODE_SYNC) {
                readSyncDataset(br);
            }
        } catch (IOException e) {
            Log.e("DDMS", e);
        }
    }
    @Override
    public void deviceSelected() {
        if (getCurrentDevice() != null) {
            mFetchButton.setEnabled(true);
            loadFromDevice();
        } else {
            mFetchButton.setEnabled(false);
        }
    }
    @Override
    public void clientSelected() {
    }
    @Override
    public void setFocus() {
        mDisplayMode.setFocus();
    }
    private void loadFromDevice() {
        try {
            initShellOutputBuffer();
            if (mMode == MODE_MEMINFO) {
                mTempStream.write("------ MEMORY INFO ------\n".getBytes());
            }
            getCurrentDevice().executeShellCommand(
                    DUMP_COMMAND[mMode], this);
        } catch (IOException e) {
            Log.e("DDMS", e);
        }
    }
    void initShellOutputBuffer() throws IOException {
        mDataFile = File.createTempFile("ddmsfile", ".txt");
        mDataFile.deleteOnExit();
        mTempStream = new FileOutputStream(mDataFile);
    }
    public void addOutput(byte[] data, int offset, int length) {
        try {
            mTempStream.write(data, offset, length);
        }
        catch (IOException e) {
            Log.e("DDMS", e);
        }
    }
    public void flush() {
        if (mTempStream != null) {
            try {
                mTempStream.close();
                generateDataset(mDataFile);
                mTempStream = null;
                mDataFile = null;
            } catch (IOException e) {
                Log.e("DDMS", e);
            }
        }
    }
    public boolean isCancelled() {
        return false;
    }
    @Override
    protected Control createControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout(1, false));
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite buttons = new Composite(top, SWT.NONE);
        buttons.setLayout(new RowLayout());
        mDisplayMode = new Combo(buttons, SWT.PUSH);
        for (String mode : CAPTIONS) {
            mDisplayMode.add(mode);
        }
        mDisplayMode.select(mMode);
        mDisplayMode.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mMode = mDisplayMode.getSelectionIndex();
                if (mDataFile != null) {
                    generateDataset(mDataFile);
                } else if (getCurrentDevice() != null) {
                    loadFromDevice();
                }
            }
        });
        final Button loadButton = new Button(buttons, SWT.PUSH);
        loadButton.setText("Load from File");
        loadButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(loadButton.getShell(),
                        SWT.OPEN);
                fileDialog.setText("Load bugreport");
                String filename = fileDialog.open();
                if (filename != null) {
                    mDataFile = new File(filename);
                    generateDataset(mDataFile);
                }
            }
        });
        mFetchButton = new Button(buttons, SWT.PUSH);
        mFetchButton.setText("Update from Device");
        mFetchButton.setEnabled(false);
        mFetchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadFromDevice();
            }
        });
        mLabel = new Label(top, SWT.NONE);
        mLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDataset = new DefaultPieDataset();
        JFreeChart chart = ChartFactory.createPieChart("", mDataset, false
                , true, false );
        ChartComposite chartComposite = new ChartComposite(top,
                SWT.BORDER, chart,
                ChartComposite.DEFAULT_HEIGHT,
                ChartComposite.DEFAULT_HEIGHT,
                ChartComposite.DEFAULT_MINIMUM_DRAW_WIDTH,
                ChartComposite.DEFAULT_MINIMUM_DRAW_HEIGHT,
                3000,
                3000,
                true,  
                true,  
                true,  
                true,  
                false,  
                true);
        chartComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return top;
    }
    public void clientChanged(final Client client, int changeMask) {
    }
    private BufferedReader getBugreportReader(File file) throws
            IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        while (true) {
            String line = br.readLine();
            if (line == null) {
                Log.d("DDMS", "Service not found " + line);
                break;
            }
            if ((line.startsWith("DUMP OF SERVICE ") || line.startsWith("-----")) &&
                    line.indexOf(BUGREPORT_SECTION[mMode]) > 0) {
                break;
            }
        }
        return br;
    }
    private static long parseTimeMs(String s) {
        long total = 0;
        Pattern p = Pattern.compile("([\\d\\.]+)\\s*([a-z]+)");
        Matcher m = p.matcher(s);
        while (m.find()) {
            String label = m.group(2);
            if ("sec".equals(label)) {
                total += (long) (Double.parseDouble(m.group(1)) * 1000);
                continue;
            }
            long value = Integer.parseInt(m.group(1));
            if ("d".equals(label)) {
                total += value * 24 * 60 * 60 * 1000;
            } else if ("h".equals(label)) {
                total += value * 60 * 60 * 1000;
            } else if ("m".equals(label)) {
                total += value * 60 * 1000;
            } else if ("s".equals(label)) {
                total += value * 1000;
            } else if ("ms".equals(label)) {
                total += value;
            }
        }
        return total;
    }
    void readWakelockDataset(BufferedReader br) throws IOException {
        Pattern lockPattern = Pattern.compile("Wake lock (\\S+): (.+) partial");
        Pattern totalPattern = Pattern.compile("Total: (.+) uptime");
        double total = 0;
        boolean inCurrent = false;
        while (true) {
            String line = br.readLine();
            if (line == null || line.startsWith("DUMP OF SERVICE")) {
                break;
            }
            if (line.startsWith("Current Battery Usage Statistics")) {
                inCurrent = true;
            } else if (inCurrent) {
                Matcher m = lockPattern.matcher(line);
                if (m.find()) {
                    double value = parseTimeMs(m.group(2)) / 1000.;
                    mDataset.setValue(m.group(1), value);
                    total -= value;
                } else {
                    m = totalPattern.matcher(line);
                    if (m.find()) {
                        total += parseTimeMs(m.group(1)) / 1000.;
                    }
                }
            }
        }
        if (total > 0) {
            mDataset.setValue("Unlocked", total);
        }
    }
    void readAlarmDataset(BufferedReader br) throws IOException {
        Pattern pattern = Pattern
                .compile("(\\d+) alarms: Intent .*\\.([^. ]+) flags");
        while (true) {
            String line = br.readLine();
            if (line == null || line.startsWith("DUMP OF SERVICE")) {
                break;
            }
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                long count = Long.parseLong(m.group(1));
                String name = m.group(2);
                mDataset.setValue(name, count);
            }
        }
    }
    void readCpuDataset(BufferedReader br) throws IOException {
        Pattern pattern = Pattern
                .compile("(\\S+): (\\S+)% = (.+)% user . (.+)% kernel");
        while (true) {
            String line = br.readLine();
            if (line == null || line.startsWith("DUMP OF SERVICE")) {
                break;
            }
            if (line.startsWith("Load:")) {
                mLabel.setText(line);
                continue;
            }
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                String name = m.group(1);
                long both = Long.parseLong(m.group(2));
                long user = Long.parseLong(m.group(3));
                long kernel = Long.parseLong(m.group(4));
                if ("TOTAL".equals(name)) {
                    if (both < 100) {
                        mDataset.setValue("Idle", (100 - both));
                    }
                } else {
                    if (user > 0) {
                        mDataset.setValue(name + " (user)", user);
                    }
                    if (kernel > 0) {
                        mDataset.setValue(name + " (kernel)" , both - user);
                    }
                    if (user == 0 && kernel == 0 && both > 0) {
                        mDataset.setValue(name, both);
                    }
                }
            }
        }
    }
    void readMeminfoDataset(BufferedReader br) throws IOException {
        Pattern valuePattern = Pattern.compile("(\\d+) kB");
        long total = 0;
        long other = 0;
        mLabel.setText("PSS in kB");        
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            Matcher m = valuePattern.matcher(line);
            if (m.find()) {
                long kb = Long.parseLong(m.group(1));
                if (line.startsWith("MemTotal")) {
                    total = kb;
                } else if (line.startsWith("MemFree")) {
                    mDataset.setValue("Free", kb);
                    total -= kb;
                } else if (line.startsWith("Slab")) {
                    mDataset.setValue("Slab", kb);
                    total -= kb;
                } else if (line.startsWith("PageTables")) {
                    mDataset.setValue("PageTables", kb);
                    total -= kb;
                } else if (line.startsWith("Buffers") && kb > 0) {
                    mDataset.setValue("Buffers", kb);
                    total -= kb;
                } else if (line.startsWith("Inactive")) {
                    mDataset.setValue("Inactive", kb);
                    total -= kb;
                } else if (line.startsWith("MemFree")) {
                    mDataset.setValue("Free", kb);
                    total -= kb;
                }
            } else {
                break;
            }
        }
        while (true) {
            String line = br.readLine();
            if (line == null) {
                break;
            }
            if (line.indexOf("PROCRANK") >= 0 || line.indexOf("PID") >= 0) {
                continue;
            }
            if  (line.indexOf("----") >= 0) {
                break;
            }
            long pss = Long.parseLong(line.substring(23, 31).trim());
            String cmdline = line.substring(43).trim().replace("/system/bin/", "");
            if (pss > 2000) {
                mDataset.setValue(cmdline, pss);
            } else {
                other += pss;
            }
            total -= pss;
        }
        mDataset.setValue("Other", other);
        mDataset.setValue("Unknown", total);
    }
    void readSyncDataset(BufferedReader br) throws IOException {
        while (true) {
            String line = br.readLine();
            if (line == null || line.startsWith("DUMP OF SERVICE")) {
                break;
            }
            if (line.startsWith(" |") && line.length() > 70) {
                String authority = line.substring(3, 18).trim();
                String duration = line.substring(61, 70).trim();
                String durParts[] = duration.split(":");
                if (durParts.length == 2) {
                    long dur = Long.parseLong(durParts[0]) * 60 + Long
                            .parseLong(durParts[1]);
                    mDataset.setValue(authority, dur);
                } else if (duration.length() == 3) {
                    long dur = Long.parseLong(durParts[0]) * 3600
                            + Long.parseLong(durParts[1]) * 60 + Long
                            .parseLong(durParts[2]);
                    mDataset.setValue(authority, dur);
                }
            }
        }
    }
}
