public class LogImpl extends Canvas implements Log, BandyDisplayable, CommandListener, Runnable {
    public static final String VALUE_DELIMITER = " , ";
    public static final String VARIABLE_DELIMITER = " ; ";
    private static final int SCROLL_BAR_SIZE = 2;
    private static final int SCROLL_STEP = 50;
    public LogConfiguration config;
    private LogStorage logStorage;
    private PrintStream logPrintStream;
    private boolean openedLogPrintStream = false;
    private Vector logEntries = new Vector();
    private Vector logEntriesToDisplay = new Vector();
    private boolean funcAutoScroll = true;
    private boolean keyScroll = true;
    private boolean funcBeepOnError = false;
    private boolean funcShowStatusBox = false;
    private static Font logMessageFont;
    private static int logMessageFontHeight;
    private static int logMessageFontWidthPerChar;
    private static Font statusBoxFont;
    private static int statusBoxFontHeight = 0;
    private int x0 = 0;
    private int y0 = 0;
    private int yMax = 0;
    private int xMax = 0;
    private int yStatus = 0;
    private int width = 0;
    private int height = 0;
    private int scrollStepY = SCROLL_STEP;
    private int scrollStepX = SCROLL_STEP;
    private Thread animateStatusBox;
    private String statusMessage = "";
    private int statusMessageWidth = 0;
    private MIDlet midlet;
    protected Display display;
    protected Displayable previousDisplayable;
    private static Image bandyLogoBackgroundImage;
    private Command cmdBack;
    private Command cmdExit;
    private Command cmdSelectLogLevel;
    private Command cmdSelectFilterLevel;
    private Command cmdSelectLogMode;
    private Command cmdSelectBtDevice;
    private Command cmdClearCanvas;
    private Command cmdClearLogStorage;
    private Command cmdAutoScrollOn;
    private Command cmdAutoScrollOff;
    private Command cmdBeepOnErrorOn;
    private Command cmdBeepOnErrorOff;
    private Command cmdShowStatus;
    private Command cmdHideStatus;
    public LogImpl(LogConfiguration config) {
        setTitle("Log");
        this.setCommandListener(this);
        this.config = config;
        logStorage = new LogStorage(this);
        if (config.logModeFileEnabled()) {
            if (!openedLogPrintStream) {
                PrintStreamOpener printStreamOpener = new PrintStreamOpener();
                printStreamOpener.start();
            }
        }
        if (config.showCommandClearCanvas) {
            cmdClearCanvas = new Command("Clear Log", Command.SCREEN, 1);
            addCommand(cmdClearCanvas);
        }
        if (config.showCommandClearLogStorage) {
            cmdClearLogStorage = new Command("Clear Persistent Log", Command.SCREEN, 1);
            addCommand(cmdClearLogStorage);
        }
        if (config.showStatusBox) {
            cmdShowStatus = new Command("Show Levels", Command.SCREEN, 2);
            cmdHideStatus = new Command("Hide Levels", Command.SCREEN, 2);
            if (funcShowStatusBox) {
                addCommand(cmdHideStatus);
            } else {
                addCommand(cmdShowStatus);
            }
        }
        if (config.showCommandSelectLogLevel) {
            cmdSelectLogLevel = new Command("Log Level", Command.SCREEN, 3);
            addCommand(cmdSelectLogLevel);
        }
        if (config.showCommandSelectFilter) {
            cmdSelectFilterLevel = new Command("Filter Level", Command.SCREEN, 4);
            addCommand(cmdSelectFilterLevel);
        }
        if (config.showCommandSelectMode) {
            cmdSelectLogMode = new Command("Log Modes", Command.SCREEN, 5);
            addCommand(cmdSelectLogMode);
        }
        if (config.showCommandAutoScroll) {
            cmdAutoScrollOn = new Command("Auto Scroll", Command.SCREEN, 5);
            cmdAutoScrollOff = new Command("Lock Scroll", Command.SCREEN, 5);
            if (funcAutoScroll) {
                addCommand(cmdAutoScrollOff);
            } else {
                addCommand(cmdAutoScrollOn);
            }
        }
        if (config.showCommandBeep) {
            cmdBeepOnErrorOn = new Command("Beep On Error", Command.SCREEN, 6);
            cmdBeepOnErrorOff = new Command("Dont Beep", Command.SCREEN, 6);
            if (funcBeepOnError) {
                addCommand(cmdBeepOnErrorOff);
            } else {
                addCommand(cmdBeepOnErrorOn);
            }
        }
        if (config.showCommandSendLog) {
            cmdSelectBtDevice = new Command("Send Log", Command.SCREEN, 7);
            addCommand(cmdSelectBtDevice);
        }
        if (config.showCommandBack) {
            cmdBack = new Command("Back", Command.BACK, 8);
            addCommand(cmdBack);
        } else {
            cmdExit = new Command("Exit", Command.EXIT, 8);
            addCommand(cmdExit);
        }
        logMessageFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        logMessageFontHeight = logMessageFont.getHeight();
        logMessageFontWidthPerChar = logMessageFont.charWidth('A');
        if (config.showStatusBox) {
            statusBoxFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            statusBoxFontHeight = statusBoxFont.getHeight();
        }
        try {
            bandyLogoBackgroundImage = Image.createImage("/img/BandyLogoBackground.png");
        } catch (IOException e) {
        }
    }
    public void setMIDlet(MIDlet midlet) {
        this.midlet = midlet;
    }
    public void error(Object origin, String message, Throwable exception) {
        doLog(LogLevel.ERROR, message, exception, origin);
        if (config.showCommandBeep && funcBeepOnError && (display != null)) {
            AlertType.INFO.playSound(display);
        }
    }
    public void error(String originName, String message, Throwable exception) {
        doLog(LogLevel.ERROR, message, exception, originName);
        if (config.showCommandBeep && funcBeepOnError && (display != null)) {
            AlertType.INFO.playSound(display);
        }
    }
    public void warn(Object origin, String message) {
        doLog(LogLevel.WARNING, message, (Throwable) null, origin);
    }
    public void warn(String originName, String message) {
        doLog(LogLevel.WARNING, message, (Throwable) null, originName);
    }
    public void info(Object origin, String message) {
        doLog(LogLevel.INFO, message, (Throwable) null, origin);
    }
    public void info(String originName, String message) {
        doLog(LogLevel.INFO, message, (Throwable) null, originName);
    }
    public void debug(Object origin, String message) {
        doLog(LogLevel.DEBUG, message, (Throwable) null, origin);
    }
    public void debug(String originName, String message) {
        doLog(LogLevel.DEBUG, message, (Throwable) null, originName);
    }
    public void setLogLevel(int level) {
        try {
            config.setLogLevel(level);
        } catch (Exception e) {
            error(this, "Could not set log level", e);
        }
    }
    private void doLog(int level, Object message, Throwable exception, Object origin) {
        doLog(level, message, exception, StringUtil.getShortClassName(origin));
    }
    private void doLog(int level, Object message, Throwable exception, String originClassName) {
        if (level <= config.getLogLevel()) {
            String logMessage = LogHelper.createLogMessage(level, message, originClassName, exception);
            if (config.logModeCanvasEnabled()) {
                logOnCanvas(logMessage);
            }
            if (config.logModePersistentEnabled()) {
                logStorage.storeLogMessage(logMessage);
            }
            if (config.logModeFileEnabled()) {
                logToFile(logMessage);
            }
            if (config.logModeRemoteEnabled()) {
            }
            if (exception != null) {
                System.out.print("Bandy LogService caught an exception! Stack trace: ");
                exception.printStackTrace();
            }
        }
    }
    protected void logOnCanvas(String logMessage) {
        if (logEntries.size() == config.maxCanvasLogEntries) {
            logEntries.removeElementAt(0);
        }
        logEntries.addElement(logMessage);
        keyScroll = false;
        repaint();
    }
    protected void logToFile(String logMessage) {
        if (!openedLogPrintStream) {
            PrintStreamOpener printStreamOpener = new PrintStreamOpener();
            printStreamOpener.start();
        }
        if (logPrintStream != null) {
            logPrintStream.println(logMessage);
        } else {
            logOnCanvas(LogHelper.createLogMessage(LogLevel.WARNING, "Can not write to file the message: " + logMessage, "Log", null));
        }
    }
    public void displayStoredLogMessages() {
        logStorage.displayStoredLogMessagesOnCanvas();
    }
    public void clearCanvas() {
        logEntries.removeAllElements();
        resetCanvas();
        info(this, "Log cleaned");
    }
    public void show(Display display, Displayable previousDisplayable) {
        this.display = display;
        this.previousDisplayable = previousDisplayable;
        display.setCurrent(this);
    }
    protected void paint(Graphics g) {
        if (config.fullScreen) {
            setFullScreenMode(true);
        }
        width = this.getWidth();
        height = this.getHeight();
        logEntriesToDisplay.removeAllElements();
        if ((!config.showCommandSelectFilter) || noFilterIsSet()) {
            CollectionsUtil.copy(logEntries, logEntriesToDisplay);
        } else if (config.showCommandSelectFilter) {
            for (int i = 0; i < logEntries.size(); i++) {
                String logMessage = (String) logEntries.elementAt(i);
                int logLevel = LogHelper.getLogLevel(logMessage);
                if (config.filterLevels[logLevel]) {
                    logEntriesToDisplay.addElement(logMessage);
                }
            }
        }
        xMax = width;
        yMax = logEntriesToDisplay.size() * logMessageFontHeight + logMessageFontHeight;
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, width, height);
        if (bandyLogoBackgroundImage != null) {
            g.drawImage(bandyLogoBackgroundImage, (width / 2), (height / 2), Graphics.HCENTER | Graphics.VCENTER);
        }
        if (funcAutoScroll && (!keyScroll)) {
            if (yMax > height) {
                y0 = yMax - height;
            }
        }
        g.setFont(logMessageFont);
        g.translate(-x0, -y0);
        int y = logMessageFontHeight;
        for (int i = 0; i < logEntriesToDisplay.size(); i++) {
            String logMessage = (String) logEntriesToDisplay.elementAt(i);
            int logLevel = Integer.parseInt(logMessage.substring(0, 1));
            g.setColor(LogLevel.colors[logLevel]);
            logMessage = logMessage.substring(1);
            g.drawString(logMessage, 0, y, Graphics.BASELINE | Graphics.LEFT);
            int lineWidth = getApproximateLineWidth(logMessage, logMessageFont);
            if (xMax < lineWidth) {
                xMax = lineWidth;
            }
            y += logMessageFontHeight;
        }
        int scrollStepYFactor = yMax / SCROLL_STEP;
        int scrollStepXFactor = xMax / SCROLL_STEP;
        if (scrollStepYFactor != 0) {
            scrollStepY = yMax / scrollStepYFactor;
        }
        if (scrollStepXFactor != 0) {
            scrollStepX = xMax / scrollStepXFactor;
        }
        if (config.showScrollBars) {
            g.translate(+x0, +y0);
            g.setColor(200, 200, 200);
            g.fillRect(0, height - SCROLL_BAR_SIZE, width, SCROLL_BAR_SIZE);
            g.fillRect(width - SCROLL_BAR_SIZE, 0, SCROLL_BAR_SIZE, height);
            double scaleFactorX = (((double) width) / (xMax - SCROLL_BAR_SIZE));
            double scaleFactorY = (((double) height) / (yMax - SCROLL_BAR_SIZE));
            int scrollBarWidthX = (new Double(width * scaleFactorX)).intValue();
            int scrollBarX0 = (new Double(x0 * scaleFactorX)).intValue();
            int scrollBarHeightY = (new Double(height * scaleFactorY)).intValue();
            int scrollBarY0 = (new Double(y0 * scaleFactorY)).intValue();
            g.setColor(4478583);
            g.fillRect(scrollBarX0, height - SCROLL_BAR_SIZE, scrollBarWidthX, SCROLL_BAR_SIZE);
            g.fillRect(width - SCROLL_BAR_SIZE, scrollBarY0, SCROLL_BAR_SIZE, scrollBarHeightY);
            g.setColor(0);
            g.fillRect(width - SCROLL_BAR_SIZE, height - SCROLL_BAR_SIZE, SCROLL_BAR_SIZE, SCROLL_BAR_SIZE);
        }
        if (config.showStatusBox) {
            if (funcShowStatusBox) {
                g.setColor(230, 230, 230);
                g.fillRect(0, yStatus, width, statusBoxFontHeight + 1);
                g.setColor(4550589);
                g.setFont(statusBoxFont);
                g.drawString(statusMessage, (width / 2) - (statusMessageWidth / 2), yStatus + statusBoxFontHeight, Graphics.BOTTOM | Graphics.LEFT);
            }
        }
    }
    public void keyPressed(int key) {
        int gameAction = getGameAction(key);
        switch(gameAction) {
            case RIGHT:
                if (x0 < (xMax - width)) {
                    int remainingX = (xMax + 5) - width - x0;
                    if (remainingX > scrollStepX) {
                        x0 += scrollStepX;
                    } else {
                        x0 += remainingX;
                    }
                    keyScroll = true;
                    repaint();
                }
                break;
            case LEFT:
                if (x0 >= scrollStepX) {
                    x0 -= scrollStepX;
                    keyScroll = true;
                    repaint();
                } else if (x0 > 0) {
                    x0 = 0;
                    keyScroll = true;
                    repaint();
                }
                break;
            case DOWN:
                if (y0 < (yMax - height)) {
                    int remainingY = yMax - height - y0;
                    if (remainingY > scrollStepY) {
                        y0 += scrollStepY;
                    } else {
                        y0 += remainingY;
                    }
                    keyScroll = true;
                    repaint();
                }
                break;
            case UP:
                if (y0 >= scrollStepY) {
                    y0 -= scrollStepY;
                    keyScroll = true;
                    repaint();
                } else if (y0 > 0) {
                    y0 = 0;
                    keyScroll = true;
                    repaint();
                }
                break;
            default:
        }
    }
    protected void keyRepeated(int key) {
        keyPressed(key);
    }
    public void commandAction(Command c, Displayable d) {
        if (c == cmdBack) {
            if ((display != null) && (previousDisplayable != null)) {
                display.setCurrent(previousDisplayable);
            } else {
                if (midlet != null) {
                    midlet.notifyDestroyed();
                }
            }
        } else if (c == cmdExit) {
            if (midlet != null) {
                midlet.notifyDestroyed();
            }
        } else if (c == cmdSelectFilterLevel) {
            SelectFilterLevelForm selectForm = new SelectFilterLevelForm();
            selectForm.show(display, this);
            display.setCurrent(selectForm);
        } else if (c == cmdSelectLogLevel) {
            SelectLogLevelForm selectLogLevelForm = new SelectLogLevelForm();
            selectLogLevelForm.show(display, this);
            display.setCurrent(selectLogLevelForm);
        } else if (c == cmdSelectLogMode) {
            SelectLogModeForm selectLogModeForm = new SelectLogModeForm();
            selectLogModeForm.show(display, this);
            display.setCurrent(selectLogModeForm);
        } else if (c == cmdSelectBtDevice) {
            if (config.fullScreen) {
                setFullScreenMode(false);
            }
            if (display != null) {
                String logAsString = "";
                if (config.runningAsViewer) {
                    logAsString = getPersistentLogAsString(this);
                } else {
                    logAsString = getLogAsString();
                }
                SelectBtDeviceForm selectBtDeviceForm = new SelectBtDeviceForm(logAsString);
                selectBtDeviceForm.show(display, this);
                display.setCurrent(selectBtDeviceForm);
            } else {
                warn(this, "Display not set");
            }
        } else if (c == cmdClearCanvas) {
            clearCanvas();
        } else if (c == cmdClearLogStorage) {
            logStorage.clearStoredMessages();
            logEntries.removeAllElements();
            resetCanvas();
            Alert alert = new Alert("", "Persistent log cleared", null, AlertType.CONFIRMATION);
            alert.setTimeout(2500);
            display.setCurrent(alert, this);
        } else if (c == cmdAutoScrollOn) {
            funcAutoScroll = true;
            removeCommand(cmdAutoScrollOn);
            addCommand(cmdAutoScrollOff);
        } else if (c == cmdAutoScrollOff) {
            funcAutoScroll = false;
            removeCommand(cmdAutoScrollOff);
            addCommand(cmdAutoScrollOn);
        } else if (c == cmdBeepOnErrorOn) {
            funcBeepOnError = true;
            removeCommand(cmdBeepOnErrorOn);
            addCommand(cmdBeepOnErrorOff);
        } else if (c == cmdBeepOnErrorOff) {
            funcBeepOnError = false;
            removeCommand(cmdBeepOnErrorOff);
            addCommand(cmdBeepOnErrorOn);
        } else if (c == cmdShowStatus) {
            setStatusMessage();
            yStatus = -statusBoxFontHeight;
            removeCommand(cmdShowStatus);
            addCommand(cmdHideStatus);
            funcShowStatusBox = true;
            animateStatusBox = new Thread(this);
            animateStatusBox.start();
        } else if (c == cmdHideStatus) {
            removeCommand(cmdHideStatus);
            addCommand(cmdShowStatus);
            animateStatusBox = new Thread(this);
            animateStatusBox.start();
        }
    }
    public void run() {
        if (animateStatusBox != null) {
            Thread me = Thread.currentThread();
            if (yStatus < 0) {
                while ((me == animateStatusBox) && (yStatus < 0)) {
                    synchronized (this) {
                        try {
                            this.wait(10);
                            yStatus += 1;
                            keyScroll = true;
                            repaint();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                while ((me == animateStatusBox) && (yStatus > (-statusBoxFontHeight))) {
                    synchronized (this) {
                        try {
                            this.wait(10);
                            yStatus -= 1;
                            keyScroll = true;
                            repaint();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                funcShowStatusBox = false;
            }
        }
        animateStatusBox = null;
    }
    protected String getLogAsString() {
        String logAsString = "";
        for (int i = 0; i < logEntries.size(); i++) {
            String logMessage = (String) logEntries.elementAt(i);
            int logLevel = Integer.parseInt(logMessage.substring(0, 1));
            logAsString = logAsString + LogLevel.levels[logLevel] + " " + logMessage.substring(1) + "\n";
        }
        return logAsString;
    }
    public String getPersistentLogAsString(Displayable currentDisplayable) {
        return logStorage.getPersistenLogAsString(display, currentDisplayable);
    }
    protected void resetCanvas() {
        x0 = 0;
        y0 = 0;
        if (config.showStatusBox && funcShowStatusBox) {
            setStatusMessage();
        }
    }
    private void setStatusMessage() {
        statusMessage = "Log: " + LogLevel.levels[config.getLogLevel()];
        if (config.showCommandSelectFilter) {
            statusMessage = statusMessage + " Filter: ";
            if (noFilterIsSet()) {
                statusMessage = statusMessage + "No Filter";
            } else {
                boolean filterSet = false;
                for (int i = 0; i < config.filterLevels.length; i++) {
                    if (config.filterLevels[i]) {
                        if (filterSet) {
                            statusMessage = statusMessage + "|";
                        }
                        statusMessage = statusMessage + LogLevel.levels[i].trim();
                        filterSet = true;
                    }
                }
            }
        }
        statusMessageWidth = getExactLineWidth(statusMessage, statusBoxFont);
    }
    private boolean noFilterIsSet() {
        for (int i = 0; i < config.filterLevels.length; i++) {
            if (!config.filterLevels[i]) {
                return false;
            }
        }
        return true;
    }
    private static int getExactLineWidth(String string, Font font) {
        char[] data = new char[string.length()];
        string.getChars(0, string.length(), data, 0);
        int lineWidth = 0;
        char ch;
        for (int ccnt = 0; ccnt < data.length; ccnt++) {
            ch = data[ccnt];
            lineWidth = lineWidth + font.charWidth(ch);
        }
        return lineWidth;
    }
    private static int getApproximateLineWidth(String string, Font font) {
        return string.length() * logMessageFontWidthPerChar;
    }
    private class PrintStreamOpener extends Thread {
        public void run() {
            openedLogPrintStream = true;
            String logFileUrl = "";
            if (Service.runningOnSunWTK()) {
                logFileUrl = "file:
            } else {
                logFileUrl = "file:
            }
            try {
                FileConnection fc = (FileConnection) Connector.open(logFileUrl);
                if (!fc.exists()) {
                    fc.create();
                }
                logPrintStream = new PrintStream(fc.openOutputStream(fc.fileSize()));
            } catch (IOException ioe) {
            }
        }
    }
}
