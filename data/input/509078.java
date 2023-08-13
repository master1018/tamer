public class SdkStatsService {
    private static final long PING_INTERVAL_MSEC = 86400 * 1000;  
    private static final String WINDOW_TITLE_TEXT =
        "Android SDK";
    private static final String HEADER_TEXT =
        "Thanks for using the Android SDK!";
    private static final String NOTICE_TEXT =
        "We know you just want to get started but please read this first.";
    public static final String BODY_TEXT =
        "By choosing to send certain usage statistics to Google, you can " +
        "help us improve the Android SDK.  These usage statistics let us " +
        "measure things like active usage of the SDK and let us know things " +
        "like which versions of the SDK are in use and which tools are the " +
        "most popular with developers.  This limited data is not associated " +
        "with personal information about you, is examined on an aggregate " +
        "basis, and is maintained in accordance with the " +
        "<a href=\"http:
        "Privacy Policy</a>.";
    public static final String CHECKBOX_TEXT =
        "Send usage statistics to Google.";
    private static final String FOOTER_TEXT =
        "If you later decide to change this setting, you can do so in the " +
        "\"ddms\" tool under \"File\" > \"Preferences\" > \"Usage Stats\".";
    private static final String BUTTON_TEXT =
        "   Proceed   ";
    private static final String[] LINUX_BROWSERS = new String[] {
        "firefox -remote openurl(%URL%,new-window)",  
        "mozilla -remote openurl(%URL%,new-window)",  
        "firefox %URL%",                              
        "mozilla %URL%",                              
        "kfmclient openURL %URL%",                    
        "opera -newwindow %URL%",                     
    };
    public final static String PING_OPT_IN = "pingOptIn"; 
    public final static String PING_TIME = "pingTime"; 
    public final static String PING_ID = "pingId"; 
    private static PreferenceStore sPrefStore;
    public static void ping(final String app, final String version, final Display display) {
        PreferenceStore prefs = getPreferenceStore();
        if (prefs != null) {
            if (prefs.contains(PING_ID) == false) {
                prefs.setValue(PING_ID, new Random().nextLong());
                getUserPermissionAndPing(app, version, prefs, display);
            } else {
                doPing(app, version, prefs);
            }
        }
    }
    public static synchronized PreferenceStore getPreferenceStore() {
        if (sPrefStore == null) {
            String homeDir = null;
            try {
                homeDir = AndroidLocation.getFolder();
            } catch (AndroidLocationException e1) {
            }
            if (homeDir != null) {
                String rcFileName = homeDir + "ddms.cfg"; 
                String oldPrefPath = System.getProperty("user.home") 
                    + File.separator + ".ddmsrc"; 
                File oldPrefFile = new File(oldPrefPath);
                if (oldPrefFile.isFile()) {
                    try {
                        PreferenceStore oldStore = new PreferenceStore(oldPrefPath);
                        oldStore.load();
                        oldStore.save(new FileOutputStream(rcFileName), "");
                        oldPrefFile.delete();
                        PreferenceStore newStore = new PreferenceStore(rcFileName);
                        newStore.load();
                        sPrefStore = newStore;
                    } catch (IOException e) {
                        sPrefStore = new PreferenceStore(rcFileName);
                    }
                } else {
                    sPrefStore = new PreferenceStore(rcFileName);
                    try {
                        sPrefStore.load();
                    } catch (IOException e) {
                        System.err.println("Error Loading Preferences");
                    }
                }
            } else {
                sPrefStore = new PreferenceStore();
            }
        }
        return sPrefStore;
    }
    private static void doPing(final String app, String version, PreferenceStore prefs) {
        final String normalVersion = normalizeVersion(app, version);
        if (!prefs.getBoolean(PING_OPT_IN)) {
            return;
        }
        String timePref = PING_TIME + "." + app;  
        long now = System.currentTimeMillis();
        long then = prefs.getLong(timePref);
        if (now - then < PING_INTERVAL_MSEC) {
            return;
        }
        prefs.setValue(timePref, now);
        try {
            prefs.save();
        }
        catch (IOException ioe) {
        }
        final long id = prefs.getLong(PING_ID);
        new Thread() {
            @Override
            public void run() {
                try {
                    actuallySendPing(app, normalVersion, id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    @SuppressWarnings("deprecation")
    private static void actuallySendPing(String app, String version, long id)
                throws IOException {
        String os = System.getProperty("os.name");          
        if (os.startsWith("Mac OS")) {                      
            os = "mac";                                     
            String osVers = getVersion();
            if (osVers != null) {
                os = os + "-" + osVers;                     
            }
        } else if (os.startsWith("Windows")) {              
            os = "win";                                     
            String osVers = getVersion();
            if (osVers != null) {
                os = os + "-" + osVers;                     
            }
        } else if (os.startsWith("Linux")) {                
            os = "linux";                                   
        } else {
            os = URLEncoder.encode(os);
        }
        URL url = new URL(
            "http",                                         
            "tools.google.com",                             
            "/service/update?as=androidsdk_" + app +        
                "&id=" + Long.toHexString(id) +             
                "&version=" + version +                     
                "&os=" + os);                               
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK &&
            conn.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IOException(
                conn.getResponseMessage() + ": " + url);    
        }
    }
    private static String getVersion() {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+).*"); 
        String osVers = System.getProperty("os.version"); 
        Matcher m = p.matcher(osVers);
        if (m.matches()) {
            return m.group(1) + "." + m.group(2);         
        }
        return null;
    }
    private static void getUserPermissionAndPing(final String app, final String version,
            final PreferenceStore prefs, Display display) {
        boolean dispose = false;
        if (display == null) {
            display = new Display();
            dispose = true;
        }
        final Display currentDisplay = display;
        final boolean disposeDisplay = dispose;
        display.asyncExec(new Runnable() {
            public void run() {
                final boolean[] permission = new boolean[] { false };
                final Shell shell = new Shell(currentDisplay, SWT.TITLE | SWT.BORDER);
                shell.setText(WINDOW_TITLE_TEXT);
                shell.setLayout(new GridLayout(1, false)); 
                final Label title = new Label(shell, SWT.CENTER | SWT.WRAP);
                final FontData[] fontdata = title.getFont().getFontData();
                for (int i = 0; i < fontdata.length; i++) {
                    fontdata[i].setHeight(fontdata[i].getHeight() * 4 / 3);
                }
                title.setFont(new Font(currentDisplay, fontdata));
                title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                title.setText(HEADER_TEXT);
                final Label notice = new Label(shell, SWT.WRAP);
                notice.setFont(title.getFont());
                notice.setForeground(new Color(currentDisplay, 255, 0, 0));
                notice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                notice.setText(NOTICE_TEXT);
                final Link text = new Link(shell, SWT.WRAP);
                text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                text.setText(BODY_TEXT);
                text.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        openUrl(event.text);
                    }
                });
                final Button checkbox = new Button(shell, SWT.CHECK);
                checkbox.setSelection(true); 
                checkbox.setText(CHECKBOX_TEXT);
                final Link footer = new Link(shell, SWT.WRAP);
                footer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                footer.setText(FOOTER_TEXT);
                final Button button = new Button(shell, SWT.PUSH);
                button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
                button.setText(BUTTON_TEXT);
                button.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        permission[0] = checkbox.getSelection();
                        shell.close();
                    }
                });
                final Point size = shell.computeSize(450, SWT.DEFAULT, true);
                final Rectangle screen = currentDisplay.getClientArea();
                shell.setBounds(screen.x + screen.width / 2 - size.x / 2, screen.y + screen.height
                        / 2 - size.y / 2, size.x, size.y);
                shell.open();
                while (!shell.isDisposed()) {
                    if (!currentDisplay.readAndDispatch())
                        currentDisplay.sleep();
                }
                prefs.setValue(PING_OPT_IN, permission[0]);
                try {
                    prefs.save();
                    doPing(app, version, prefs);
                }
                catch (IOException ioe) {
                }
                if (disposeDisplay) {
                    currentDisplay.dispose();
                }
            }
        });
    }
    public static void openUrl(final String url) {
        if (!Program.launch(url)) {
            new Thread() {
                @Override
                public void run() {
                    for (String cmd : LINUX_BROWSERS) {
                        cmd = cmd.replaceAll("%URL%", url);  
                        try {
                            Process proc = Runtime.getRuntime().exec(cmd);
                            if (proc.waitFor() == 0) break;  
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                        }
                    }
                }
            }.start();
        }
    }
    private static String normalizeVersion(String app, String version) {
        if (!app.matches("\\w+")) {
            throw new IllegalArgumentException("Bad app name: " + app);
        }
        String[] numbers = version.split("\\.");
        if (numbers.length > 4) {
            throw new IllegalArgumentException("Bad version: " + version);
        }
        for (String part: numbers) {
            if (!part.matches("\\d+")) {
                throw new IllegalArgumentException("Bad version: " + version);
            }
        }
        StringBuffer normal = new StringBuffer(numbers[0]);
        for (int i = 1; i < 4; i++) {
            normal.append(".").append(i < numbers.length ? numbers[i] : "0");
        }
        return normal.toString();
    }
}
