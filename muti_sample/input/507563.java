public class Term extends Activity {
    public static final boolean DEBUG = false;
    public static final boolean LOG_CHARACTERS_FLAG = DEBUG && false;
    public static final boolean LOG_UNKNOWN_ESCAPE_SEQUENCES = DEBUG && false;
    public static final String LOG_TAG = "Term";
    private EmulatorView mEmulatorView;
    private FileDescriptor mTermFd;
    private FileOutputStream mTermOut;
    private TermKeyListener mKeyListener;
    private static final int EMULATOR_VIEW = R.id.emulatorView;
    private int mFontSize = 9;
    private int mColorId = 2;
    private int mControlKeyId = 0;
    private static final String FONTSIZE_KEY = "fontsize";
    private static final String COLOR_KEY = "color";
    private static final String CONTROLKEY_KEY = "controlkey";
    private static final String SHELL_KEY = "shell";
    private static final String INITIALCOMMAND_KEY = "initialcommand";
    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;
    public static final int BLUE = 0xff344ebd;
    private static final int[][] COLOR_SCHEMES = {
        {BLACK, WHITE}, {WHITE, BLACK}, {WHITE, BLUE}};
    private static final int[] CONTROL_KEY_SCHEMES = {
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_AT,
        KeyEvent.KEYCODE_ALT_LEFT,
        KeyEvent.KEYCODE_ALT_RIGHT
    };
    private static final String[] CONTROL_KEY_NAME = {
        "Ball", "@", "Left-Alt", "Right-Alt"
    };
    private int mControlKeyCode;
    private final static String DEFAULT_SHELL = "/system/bin/sh -";
    private String mShell;
    private final static String DEFAULT_INITIAL_COMMAND =
        "export PATH=/data/local/bin:$PATH";
    private String mInitialCommand;
    private SharedPreferences mPrefs;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.e(Term.LOG_TAG, "onCreate");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        readPrefs();
        setContentView(R.layout.term_activity);
        mEmulatorView = (EmulatorView) findViewById(EMULATOR_VIEW);
        startListening();
        mKeyListener = new TermKeyListener();
        mEmulatorView.setFocusable(true);
        mEmulatorView.setFocusableInTouchMode(true);
        mEmulatorView.requestFocus();
        mEmulatorView.register(mKeyListener);
        updatePrefs();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTermFd != null) {
            Exec.close(mTermFd);
            mTermFd = null;
        }
    }
    private void startListening() {
        int[] processId = new int[1];
        createSubprocess(processId);
        final int procId = processId[0];
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            }
        };
        Runnable watchForDeath = new Runnable() {
            public void run() {
                Log.i(Term.LOG_TAG, "waiting for: " + procId);
               int result = Exec.waitFor(procId);
                Log.i(Term.LOG_TAG, "Subprocess exited: " + result);
                handler.sendEmptyMessage(result);
             }
        };
        Thread watcher = new Thread(watchForDeath);
        watcher.start();
        mTermOut = new FileOutputStream(mTermFd);
        mEmulatorView.initialize(mTermFd, mTermOut);
        sendInitialCommand();
    }
    private void sendInitialCommand() {
        String initialCommand = mInitialCommand;
        if (initialCommand == null || initialCommand.equals("")) {
            initialCommand = DEFAULT_INITIAL_COMMAND;
        }
        if (initialCommand.length() > 0) {
            write(initialCommand + '\r');
        }
    }
    private void restart() {
        startActivity(getIntent());
        finish();
    }
    private void write(String data) {
        try {
            mTermOut.write(data.getBytes());
            mTermOut.flush();
        } catch (IOException e) {
        }
    }
    private void createSubprocess(int[] processId) {
        String shell = mShell;
        if (shell == null || shell.equals("")) {
            shell = DEFAULT_SHELL;
        }
        ArrayList<String> args = parse(shell);
        String arg0 = args.get(0);
        String arg1 = null;
        String arg2 = null;
        if (args.size() >= 2) {
            arg1 = args.get(1);
        }
        if (args.size() >= 3) {
            arg2 = args.get(2);
        }
        mTermFd = Exec.createSubprocess(arg0, arg1, arg2, processId);
    }
    private ArrayList<String> parse(String cmd) {
        final int PLAIN = 0;
        final int WHITESPACE = 1;
        final int INQUOTE = 2;
        int state = WHITESPACE;
        ArrayList<String> result =  new ArrayList<String>();
        int cmdLen = cmd.length();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cmdLen; i++) {
            char c = cmd.charAt(i);
            if (state == PLAIN) {
                if (Character.isWhitespace(c)) {
                    result.add(builder.toString());
                    builder.delete(0,builder.length());
                    state = WHITESPACE;
                } else if (c == '"') {
                    state = INQUOTE;
                } else {
                    builder.append(c);
                }
            } else if (state == WHITESPACE) {
                if (Character.isWhitespace(c)) {
                } else if (c == '"') {
                    state = INQUOTE;
                } else {
                    state = PLAIN;
                    builder.append(c);
                }
            } else if (state == INQUOTE) {
                if (c == '\\') {
                    if (i + 1 < cmdLen) {
                        i += 1;
                        builder.append(cmd.charAt(i));
                    }
                } else if (c == '"') {
                    state = PLAIN;
                } else {
                    builder.append(c);
                }
            }
        }
        if (builder.length() > 0) {
            result.add(builder.toString());
        }
        return result;
    }
    private void readPrefs() {
        mFontSize = readIntPref(FONTSIZE_KEY, mFontSize, 20);
        mColorId = readIntPref(COLOR_KEY, mColorId, COLOR_SCHEMES.length - 1);
        mControlKeyId = readIntPref(CONTROLKEY_KEY, mControlKeyId,
                CONTROL_KEY_SCHEMES.length - 1);
        {
            String newShell = readStringPref(SHELL_KEY, mShell);
            if ((newShell == null) || ! newShell.equals(mShell)) {
                if (mShell != null) {
                    Log.i(Term.LOG_TAG, "New shell set. Restarting.");
                    restart();
                }
                mShell = newShell;
            }
        }
        {
            String newInitialCommand = readStringPref(INITIALCOMMAND_KEY,
                    mInitialCommand);
            if ((newInitialCommand == null)
                    || ! newInitialCommand.equals(mInitialCommand)) {
                if (mInitialCommand != null) {
                    Log.i(Term.LOG_TAG, "New initial command set. Restarting.");
                    restart();
                }
                mInitialCommand = newInitialCommand;
            }
        }
    }
    private void updatePrefs() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mEmulatorView.setTextSize((int) (mFontSize * metrics.density));
        setColors();
        mControlKeyCode = CONTROL_KEY_SCHEMES[mControlKeyId];
    }
    private int readIntPref(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }
    private String readStringPref(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }
    @Override
    public void onResume() {
        super.onResume();
        readPrefs();
        updatePrefs();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mEmulatorView.updateSize();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (handleControlKey(keyCode, true)) {
            return true;
        } else if (isSystemKey(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        } else if (handleDPad(keyCode, true)) {
            return true;
        }
        int letter = mKeyListener.keyDown(keyCode, event);
        if (letter >= 0) {
            try {
                mTermOut.write(letter);
            } catch (IOException e) {
            }
        }
        return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (handleControlKey(keyCode, false)) {
            return true;
        } else if (isSystemKey(keyCode, event)) {
            return super.onKeyUp(keyCode, event);
        } else if (handleDPad(keyCode, false)) {
            return true;
        }
        mKeyListener.keyUp(keyCode);
        return true;
    }
    private boolean handleControlKey(int keyCode, boolean down) {
        if (keyCode == mControlKeyCode) {
            mKeyListener.handleControlKey(down);
            return true;
        }
        return false;
    }
    private boolean handleDPad(int keyCode, boolean down) {
        if (keyCode < KeyEvent.KEYCODE_DPAD_UP ||
                keyCode > KeyEvent.KEYCODE_DPAD_CENTER) {
            return false;
        }
        if (down) {
            try {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    mTermOut.write('\r');
                } else {
                    char code;
                    switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        code = 'A';
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        code = 'B';
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        code = 'D';
                        break;
                    default:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        code = 'C';
                        break;
                    }
                    mTermOut.write(27); 
                    if (mEmulatorView.getKeypadApplicationMode()) {
                        mTermOut.write('O');
                    } else {
                        mTermOut.write('[');
                    }
                    mTermOut.write(code);
                }
            } catch (IOException e) {
            }
        }
        return true;
    }
    private boolean isSystemKey(int keyCode, KeyEvent event) {
        return event.isSystem();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_preferences) {
            doPreferences();
        } else if (id == R.id.menu_reset) {
            doResetTerminal();
        } else if (id == R.id.menu_send_email) {
            doEmailTranscript();
        } else if (id == R.id.menu_special_keys) {
            doDocumentKeys();
        }
        return super.onOptionsItemSelected(item);
    }
    private void doPreferences() {
        startActivity(new Intent(this, TermPreferences.class));
    }
    private void setColors() {
        int[] scheme = COLOR_SCHEMES[mColorId];
        mEmulatorView.setColors(scheme[0], scheme[1]);
    }
    private void doResetTerminal() {
        restart();
    }
    private void doEmailTranscript() {
        String addr = "user@example.com";
        Intent intent =
                new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                        + addr));
        intent.putExtra("body", mEmulatorView.getTranscriptText());
        startActivity(intent);
    }
    private void doDocumentKeys() {
        String controlKey = CONTROL_KEY_NAME[mControlKeyId];
        new AlertDialog.Builder(this).
            setTitle("Press " + controlKey + " and Key").
            setMessage(controlKey + " Space ==> Control-@ (NUL)\n"
                    + controlKey + " A..Z ==> Control-A..Z\n"
                    + controlKey + " 1 ==> Control-[ (ESC)\n"
                    + controlKey + " 5 ==> Control-_\n"
                    + controlKey + " . ==> Control-\\\n"
                    + controlKey + " 0 ==> Control-]\n"
                    + controlKey + " 6 ==> Control-^").
            show();
     }
}
interface Screen {
    void setLineWrap(int row);
    void set(int x, int y, byte b, int foreColor, int backColor);
    void scroll(int topMargin, int bottomMargin, int foreColor, int backColor);
    void blockCopy(int sx, int sy, int w, int h, int dx, int dy);
    void blockSet(int sx, int sy, int w, int h, int val, int foreColor, int
            backColor);
    String getTranscriptText();
    void resize(int columns, int rows, int foreColor, int backColor);
}
class TranscriptScreen implements Screen {
    private int mColumns;
    private int mTotalRows;
    private int mActiveTranscriptRows;
    private int mHead;
    private int mActiveRows;
    private int mScreenRows;
    private char[] mData;
    private char[] mRowBuffer;
    private boolean[] mLineWrap;
    public TranscriptScreen(int columns, int totalRows, int screenRows,
            int foreColor, int backColor) {
        init(columns, totalRows, screenRows, foreColor, backColor);
    }
    private void init(int columns, int totalRows, int screenRows, int foreColor, int backColor) {
        mColumns = columns;
        mTotalRows = totalRows;
        mActiveTranscriptRows = 0;
        mHead = 0;
        mActiveRows = screenRows;
        mScreenRows = screenRows;
        int totalSize = columns * totalRows;
        mData = new char[totalSize];
        blockSet(0, 0, mColumns, mScreenRows, ' ', foreColor, backColor);
        mRowBuffer = new char[columns];
        mLineWrap = new boolean[totalRows];
        consistencyCheck();
   }
    private int externalToInternalRow(int row) {
        if (row < -mActiveTranscriptRows || row >= mScreenRows) {
            throw new IllegalArgumentException();
        }
        if (row >= 0) {
            return row; 
        }
        return mScreenRows
                + ((mHead + mActiveTranscriptRows + row) % mActiveTranscriptRows);
    }
    private int getOffset(int externalLine) {
        return externalToInternalRow(externalLine) * mColumns;
    }
    private int getOffset(int x, int y) {
        return getOffset(y) + x;
    }
    public void setLineWrap(int row) {
        mLineWrap[externalToInternalRow(row)] = true;
    }
    public void set(int x, int y, byte b, int foreColor, int backColor) {
        mData[getOffset(x, y)] = encode(b, foreColor, backColor);
    }
    private char encode(int b, int foreColor, int backColor) {
        return (char) ((foreColor << 12) | (backColor << 8) | b);
    }
    public void scroll(int topMargin, int bottomMargin, int foreColor,
            int backColor) {
        if (topMargin > bottomMargin - 2 || topMargin > mScreenRows - 2
                || bottomMargin > mScreenRows) {
            throw new IllegalArgumentException();
        }
        consistencyCheck();
        int expansionRows = Math.min(1, mTotalRows - mActiveRows);
        int rollRows = 1 - expansionRows;
        mActiveRows += expansionRows;
        mActiveTranscriptRows += expansionRows;
        if (mActiveTranscriptRows > 0) {
            mHead = (mHead + rollRows) % mActiveTranscriptRows;
        }
        consistencyCheck();
        int topOffset = getOffset(topMargin);
        int destOffset = getOffset(-1);
        System.arraycopy(mData, topOffset, mData, destOffset, mColumns);
        int topLine = externalToInternalRow(topMargin);
        int destLine = externalToInternalRow(-1);
        System.arraycopy(mLineWrap, topLine, mLineWrap, destLine, 1);
        int numScrollChars = (bottomMargin - topMargin - 1) * mColumns;
        System.arraycopy(mData, topOffset + mColumns, mData, topOffset,
                numScrollChars);
        int numScrollLines = (bottomMargin - topMargin - 1);
        System.arraycopy(mLineWrap, topLine + 1, mLineWrap, topLine,
                numScrollLines);
        blockSet(0, bottomMargin - 1, mColumns, 1, ' ', foreColor, backColor);
        mLineWrap[externalToInternalRow(bottomMargin-1)] = false;
    }
    private void consistencyCheck() {
        checkPositive(mColumns);
        checkPositive(mTotalRows);
        checkRange(0, mActiveTranscriptRows, mTotalRows);
        if (mActiveTranscriptRows == 0) {
            checkEqual(mHead, 0);
        } else {
            checkRange(0, mHead, mActiveTranscriptRows-1);
        }
        checkEqual(mScreenRows + mActiveTranscriptRows, mActiveRows);
        checkRange(0, mScreenRows, mTotalRows);
        checkEqual(mTotalRows, mLineWrap.length);
        checkEqual(mTotalRows*mColumns, mData.length);
        checkEqual(mColumns, mRowBuffer.length);
    }
    private void checkPositive(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("checkPositive " + n);
        }
    }
    private void checkRange(int a, int b, int c) {
        if (a > b || b > c) {
            throw new IllegalArgumentException("checkRange " + a + " <= " + b + " <= " + c);
        }
    }
    private void checkEqual(int a, int b) {
        if (a != b) {
            throw new IllegalArgumentException("checkEqual " + a + " == " + b);
        }
    }
    public void blockCopy(int sx, int sy, int w, int h, int dx, int dy) {
        if (sx < 0 || sx + w > mColumns || sy < 0 || sy + h > mScreenRows
                || dx < 0 || dx + w > mColumns || dy < 0
                || dy + h > mScreenRows) {
            throw new IllegalArgumentException();
        }
        if (sy <= dy) {
            for (int y = 0; y < h; y++) {
                int srcOffset = getOffset(sx, sy + y);
                int dstOffset = getOffset(dx, dy + y);
                System.arraycopy(mData, srcOffset, mData, dstOffset, w);
            }
        } else {
            for (int y = 0; y < h; y++) {
                int y2 = h - (y + 1);
                int srcOffset = getOffset(sx, sy + y2);
                int dstOffset = getOffset(dx, dy + y2);
                System.arraycopy(mData, srcOffset, mData, dstOffset, w);
            }
        }
    }
    public void blockSet(int sx, int sy, int w, int h, int val,
            int foreColor, int backColor) {
        if (sx < 0 || sx + w > mColumns || sy < 0 || sy + h > mScreenRows) {
            throw new IllegalArgumentException();
        }
        char[] data = mData;
        char encodedVal = encode(val, foreColor, backColor);
        for (int y = 0; y < h; y++) {
            int offset = getOffset(sx, sy + y);
            for (int x = 0; x < w; x++) {
                data[offset + x] = encodedVal;
            }
        }
    }
    public final void drawText(int row, Canvas canvas, float x, float y,
            TextRenderer renderer, int cx) {
        if (row < -mActiveTranscriptRows || row >= mScreenRows) {
            return;
        }
        int offset = getOffset(row);
        char[] rowBuffer = mRowBuffer;
        char[] data = mData;
        int columns = mColumns;
        int lastColors = 0;
        int lastRunStart = -1;
        final int CURSOR_MASK = 0x10000;
        for (int i = 0; i < columns; i++) {
            char c = data[offset + i];
            int colors = (char) (c & 0xff00);
            if (cx == i) {
                colors |= CURSOR_MASK;
            }
            rowBuffer[i] = (char) (c & 0x00ff);
            if (colors != lastColors) {
                if (lastRunStart >= 0) {
                    renderer.drawTextRun(canvas, x, y, lastRunStart, rowBuffer,
                            lastRunStart, i - lastRunStart,
                            (lastColors & CURSOR_MASK) != 0,
                            0xf & (lastColors >> 12), 0xf & (lastColors >> 8));
                }
                lastColors = colors;
                lastRunStart = i;
            }
        }
        if (lastRunStart >= 0) {
            renderer.drawTextRun(canvas, x, y, lastRunStart, rowBuffer,
                    lastRunStart, columns - lastRunStart,
                    (lastColors & CURSOR_MASK) != 0,
                    0xf & (lastColors >> 12), 0xf & (lastColors >> 8));
        }
     }
    public int getActiveRows() {
        return mActiveRows;
    }
    public int getActiveTranscriptRows() {
        return mActiveTranscriptRows;
    }
    public String getTranscriptText() {
        return internalGetTranscriptText(true);
    }
    private String internalGetTranscriptText(boolean stripColors) {
        StringBuilder builder = new StringBuilder();
        char[] rowBuffer = mRowBuffer;
        char[] data = mData;
        int columns = mColumns;
        for (int row = -mActiveTranscriptRows; row < mScreenRows; row++) {
            int offset = getOffset(row);
            int lastPrintingChar = -1;
            for (int column = 0; column < columns; column++) {
                char c = data[offset + column];
                if (stripColors) {
                    c = (char) (c & 0xff);
                }
                if ((c & 0xff) != ' ') {
                    lastPrintingChar = column;
                }
                rowBuffer[column] = c;
            }
            if (mLineWrap[externalToInternalRow(row)]) {
                builder.append(rowBuffer, 0, columns);
            } else {
                builder.append(rowBuffer, 0, lastPrintingChar + 1);
                builder.append('\n');
            }
        }
        return builder.toString();
    }
    public void resize(int columns, int rows, int foreColor, int backColor) {
        init(columns, mTotalRows, rows, foreColor, backColor);
    }
}
class TerminalEmulator {
    private int mCursorRow;
    private int mCursorCol;
    private int mRows;
    private int mColumns;
    private FileOutputStream mTermOut;
    private Screen mScreen;
    private int mArgIndex;
    private static final int MAX_ESCAPE_PARAMETERS = 16;
    private int[] mArgs = new int[MAX_ESCAPE_PARAMETERS];
    private static final int ESC_NONE = 0;
    private static final int ESC = 1;
    private static final int ESC_POUND = 2;
    private static final int ESC_SELECT_LEFT_PAREN = 3;
    private static final int ESC_SELECT_RIGHT_PAREN = 4;
    private static final int ESC_LEFT_SQUARE_BRACKET = 5;
    private static final int ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK = 6;
    private boolean mContinueSequence;
    private int mEscapeState;
    private int mSavedCursorRow;
    private int mSavedCursorCol;
    private static final int K_132_COLUMN_MODE_MASK = 1 << 3;
    private static final int K_ORIGIN_MODE_MASK = 1 << 6;
    private static final int K_WRAPAROUND_MODE_MASK = 1 << 7;
    private int mDecFlags;
    private int mSavedDecFlags;
    private boolean mInsertMode;
    private boolean mAutomaticNewlineMode;
    private boolean[] mTabStop;
    private int mTopMargin;
    private int mBottomMargin;
    private boolean mAboutToAutoWrap;
    private int mProcessedCharCount;
    private int mForeColor;
    private int mBackColor;
    private boolean mInverseColors;
    private boolean mbKeypadApplicationMode;
    private boolean mAlternateCharSet;
    public TerminalEmulator(Screen screen, int columns, int rows,
            FileOutputStream termOut) {
        mScreen = screen;
        mRows = rows;
        mColumns = columns;
        mTabStop = new boolean[mColumns];
        mTermOut = termOut;
        reset();
    }
    public void updateSize(int columns, int rows) {
        if (mRows == rows && mColumns == columns) {
            return;
        }
        String transcriptText = mScreen.getTranscriptText();
        mScreen.resize(columns, rows, mForeColor, mBackColor);
        if (mRows != rows) {
            mRows = rows;
            mTopMargin = 0;
            mBottomMargin = mRows;
        }
        if (mColumns != columns) {
            int oldColumns = mColumns;
            mColumns = columns;
            boolean[] oldTabStop = mTabStop;
            mTabStop = new boolean[mColumns];
            int toTransfer = Math.min(oldColumns, columns);
            System.arraycopy(oldTabStop, 0, mTabStop, 0, toTransfer);
            while (mCursorCol >= columns) {
                mCursorCol -= columns;
                mCursorRow = Math.min(mBottomMargin-1, mCursorRow + 1);
            }
        }
        mCursorRow = 0;
        mCursorCol = 0;
        mAboutToAutoWrap = false;
        int end = transcriptText.length()-1;
        while ((end >= 0) && transcriptText.charAt(end) == '\n') {
            end--;
        }
        for(int i = 0; i <= end; i++) {
            byte c = (byte) transcriptText.charAt(i);
            if (c == '\n') {
                setCursorCol(0);
                doLinefeed();
            } else {
                emit(c);
            }
        }
    }
    public final int getCursorRow() {
        return mCursorRow;
    }
    public final int getCursorCol() {
        return mCursorCol;
    }
    public final boolean getKeypadApplicationMode() {
        return mbKeypadApplicationMode;
    }
    private void setDefaultTabStops() {
        for (int i = 0; i < mColumns; i++) {
            mTabStop[i] = (i & 7) == 0 && i != 0;
        }
    }
    public void append(byte[] buffer, int base, int length) {
        for (int i = 0; i < length; i++) {
            byte b = buffer[base + i];
            try {
                if (Term.LOG_CHARACTERS_FLAG) {
                    char printableB = (char) b;
                    if (b < 32 || b > 126) {
                        printableB = ' ';
                    }
                    Log.w(Term.LOG_TAG, "'" + Character.toString(printableB)
                            + "' (" + Integer.toString(b) + ")");
                }
                process(b);
                mProcessedCharCount++;
            } catch (Exception e) {
                Log.e(Term.LOG_TAG, "Exception while processing character "
                        + Integer.toString(mProcessedCharCount) + " code "
                        + Integer.toString(b), e);
            }
        }
    }
    private void process(byte b) {
        switch (b) {
        case 0: 
            break;
        case 7: 
            break;
        case 8: 
            setCursorCol(Math.max(0, mCursorCol - 1));
            break;
        case 9: 
            setCursorCol(nextTabStop(mCursorCol));
            break;
        case 13:
            setCursorCol(0);
            break;
        case 10: 
        case 11: 
        case 12: 
            doLinefeed();
            break;
        case 14: 
            setAltCharSet(true);
            break;
        case 15: 
            setAltCharSet(false);
            break;
        case 24: 
        case 26: 
            if (mEscapeState != ESC_NONE) {
                mEscapeState = ESC_NONE;
                emit((byte) 127);
            }
            break;
        case 27: 
            startEscapeSequence(ESC);
            break;
        case (byte) 0x9b: 
            startEscapeSequence(ESC_LEFT_SQUARE_BRACKET);
            break;
        default:
            mContinueSequence = false;
            switch (mEscapeState) {
            case ESC_NONE:
                if (b >= 32) {
                    emit(b);
                }
                break;
            case ESC:
                doEsc(b);
                break;
            case ESC_POUND:
                doEscPound(b);
                break;
            case ESC_SELECT_LEFT_PAREN:
                doEscSelectLeftParen(b);
                break;
            case ESC_SELECT_RIGHT_PAREN:
                doEscSelectRightParen(b);
                break;
            case ESC_LEFT_SQUARE_BRACKET:
                doEscLeftSquareBracket(b);
                break;
            case ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK:
                doEscLSBQuest(b);
                break;
            default:
                unknownSequence(b);
                break;
            }
            if (!mContinueSequence) {
                mEscapeState = ESC_NONE;
            }
            break;
        }
    }
    private void setAltCharSet(boolean alternateCharSet) {
        mAlternateCharSet = alternateCharSet;
    }
    private int nextTabStop(int cursorCol) {
        for (int i = cursorCol; i < mColumns; i++) {
            if (mTabStop[i]) {
                return i;
            }
        }
        return mColumns - 1;
    }
    private void doEscLSBQuest(byte b) {
        int mask = getDecFlagsMask(getArg0(0));
        switch (b) {
        case 'h': 
            mDecFlags |= mask;
            break;
        case 'l': 
            mDecFlags &= ~mask;
            break;
        case 'r': 
            mDecFlags = (mDecFlags & ~mask) | (mSavedDecFlags & mask);
            break;
        case 's': 
            mSavedDecFlags = (mSavedDecFlags & ~mask) | (mDecFlags & mask);
            break;
        default:
            parseArg(b);
            break;
        }
        if ((mask & K_132_COLUMN_MODE_MASK) != 0) {
            blockClear(0, 0, mColumns, mRows);
            setCursorRowCol(0, 0);
        }
        if ((mask & K_ORIGIN_MODE_MASK) != 0) {
            setCursorPosition(0, 0);
        }
    }
    private int getDecFlagsMask(int argument) {
        if (argument >= 1 && argument <= 9) {
            return (1 << argument);
        }
        return 0;
    }
    private void startEscapeSequence(int escapeState) {
        mEscapeState = escapeState;
        mArgIndex = 0;
        for (int j = 0; j < MAX_ESCAPE_PARAMETERS; j++) {
            mArgs[j] = -1;
        }
    }
    private void doLinefeed() {
        int newCursorRow = mCursorRow + 1;
        if (newCursorRow >= mBottomMargin) {
            scroll();
            newCursorRow = mBottomMargin - 1;
        }
        setCursorRow(newCursorRow);
    }
    private void continueSequence() {
        mContinueSequence = true;
    }
    private void continueSequence(int state) {
        mEscapeState = state;
        mContinueSequence = true;
    }
    private void doEscSelectLeftParen(byte b) {
        doSelectCharSet(true, b);
    }
    private void doEscSelectRightParen(byte b) {
        doSelectCharSet(false, b);
    }
    private void doSelectCharSet(boolean isG0CharSet, byte b) {
        switch (b) {
        case 'A': 
            break;
        case 'B': 
            break;
        case '0': 
            break;
        case '1': 
            break;
        case '2':
            break;
        default:
            unknownSequence(b);
        }
    }
    private void doEscPound(byte b) {
        switch (b) {
        case '8': 
            mScreen.blockSet(0, 0, mColumns, mRows, 'E',
                    getForeColor(), getBackColor());
            break;
        default:
            unknownSequence(b);
            break;
        }
    }
    private void doEsc(byte b) {
        switch (b) {
        case '#':
            continueSequence(ESC_POUND);
            break;
        case '(':
            continueSequence(ESC_SELECT_LEFT_PAREN);
            break;
        case ')':
            continueSequence(ESC_SELECT_RIGHT_PAREN);
            break;
        case '7': 
            mSavedCursorRow = mCursorRow;
            mSavedCursorCol = mCursorCol;
            break;
        case '8': 
            setCursorRowCol(mSavedCursorRow, mSavedCursorCol);
            break;
        case 'D': 
            doLinefeed();
            break;
        case 'E': 
            setCursorCol(0);
            doLinefeed();
            break;
        case 'F': 
            setCursorRowCol(0, mBottomMargin - 1);
            break;
        case 'H': 
            mTabStop[mCursorCol] = true;
            break;
        case 'M': 
            if (mCursorRow == 0) {
                mScreen.blockCopy(0, mTopMargin + 1, mColumns, mBottomMargin
                        - (mTopMargin + 1), 0, mTopMargin);
                blockClear(0, mBottomMargin - 1, mColumns);
            } else {
                mCursorRow--;
            }
            break;
        case 'N': 
            unimplementedSequence(b);
            break;
        case '0': 
            unimplementedSequence(b);
            break;
        case 'P': 
            unimplementedSequence(b);
            break;
        case 'Z': 
            sendDeviceAttributes();
            break;
        case '[':
            continueSequence(ESC_LEFT_SQUARE_BRACKET);
            break;
        case '=': 
            mbKeypadApplicationMode = true;
            break;
        case '>' : 
            mbKeypadApplicationMode = false;
            break;
        default:
            unknownSequence(b);
            break;
        }
    }
    private void doEscLeftSquareBracket(byte b) {
        switch (b) {
        case '@': 
        {
            int charsAfterCursor = mColumns - mCursorCol;
            int charsToInsert = Math.min(getArg0(1), charsAfterCursor);
            int charsToMove = charsAfterCursor - charsToInsert;
            mScreen.blockCopy(mCursorCol, mCursorRow, charsToMove, 1,
                    mCursorCol + charsToInsert, mCursorRow);
            blockClear(mCursorCol, mCursorRow, charsToInsert);
        }
            break;
        case 'A': 
            setCursorRow(Math.max(mTopMargin, mCursorRow - getArg0(1)));
            break;
        case 'B': 
            setCursorRow(Math.min(mBottomMargin - 1, mCursorRow + getArg0(1)));
            break;
        case 'C': 
            setCursorCol(Math.min(mColumns - 1, mCursorCol + getArg0(1)));
            break;
        case 'D': 
            setCursorCol(Math.max(0, mCursorCol - getArg0(1)));
            break;
        case 'G': 
            setCursorCol(Math.min(Math.max(1, getArg0(1)), mColumns) - 1);
            break;
        case 'H': 
            setHorizontalVerticalPosition();
            break;
        case 'J': 
            switch (getArg0(0)) {
            case 0: 
                blockClear(mCursorCol, mCursorRow, mColumns - mCursorCol);
                blockClear(0, mCursorRow + 1, mColumns,
                        mBottomMargin - (mCursorRow + 1));
                break;
            case 1: 
                blockClear(0, mTopMargin, mColumns, mCursorRow - mTopMargin);
                blockClear(0, mCursorRow, mCursorCol + 1);
                break;
            case 2: 
                blockClear(0, mTopMargin, mColumns, mBottomMargin - mTopMargin);
                break;
            default:
                unknownSequence(b);
                break;
            }
            break;
        case 'K': 
            switch (getArg0(0)) {
            case 0: 
                blockClear(mCursorCol, mCursorRow, mColumns - mCursorCol);
                break;
            case 1: 
                blockClear(0, mCursorRow, mCursorCol + 1);
                break;
            case 2: 
                blockClear(0, mCursorRow, mColumns);
                break;
            default:
                unknownSequence(b);
                break;
            }
            break;
        case 'L': 
        {
            int linesAfterCursor = mBottomMargin - mCursorRow;
            int linesToInsert = Math.min(getArg0(1), linesAfterCursor);
            int linesToMove = linesAfterCursor - linesToInsert;
            mScreen.blockCopy(0, mCursorRow, mColumns, linesToMove, 0,
                    mCursorRow + linesToInsert);
            blockClear(0, mCursorRow, mColumns, linesToInsert);
        }
            break;
        case 'M': 
        {
            int linesAfterCursor = mBottomMargin - mCursorRow;
            int linesToDelete = Math.min(getArg0(1), linesAfterCursor);
            int linesToMove = linesAfterCursor - linesToDelete;
            mScreen.blockCopy(0, mCursorRow + linesToDelete, mColumns,
                    linesToMove, 0, mCursorRow);
            blockClear(0, mCursorRow + linesToMove, mColumns, linesToDelete);
        }
            break;
        case 'P': 
        {
            int charsAfterCursor = mColumns - mCursorCol;
            int charsToDelete = Math.min(getArg0(1), charsAfterCursor);
            int charsToMove = charsAfterCursor - charsToDelete;
            mScreen.blockCopy(mCursorCol + charsToDelete, mCursorRow,
                    charsToMove, 1, mCursorCol, mCursorRow);
            blockClear(mCursorCol + charsToMove, mCursorRow, charsToDelete);
        }
            break;
        case 'T': 
            unimplementedSequence(b);
            break;
        case '?': 
            continueSequence(ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK);
            break;
        case 'c': 
            sendDeviceAttributes();
            break;
        case 'd': 
            setCursorRow(Math.min(Math.max(1, getArg0(1)), mRows) - 1);
            break;
        case 'f': 
            setHorizontalVerticalPosition();
            break;
        case 'g': 
            switch (getArg0(0)) {
            case 0:
                mTabStop[mCursorCol] = false;
                break;
            case 3:
                for (int i = 0; i < mColumns; i++) {
                    mTabStop[i] = false;
                }
                break;
            default:
                break;
            }
            break;
        case 'h': 
            doSetMode(true);
            break;
        case 'l': 
            doSetMode(false);
            break;
        case 'm': 
            selectGraphicRendition();
            break;
        case 'r': 
        {
            int top = Math.max(0, Math.min(getArg0(1) - 1, mRows - 2));
            int bottom = Math.max(top + 2, Math.min(getArg1(mRows), mRows));
            mTopMargin = top;
            mBottomMargin = bottom;
            setCursorRowCol(mTopMargin, 0);
        }
            break;
        default:
            parseArg(b);
            break;
        }
    }
    private void selectGraphicRendition() {
        for (int i = 0; i <= mArgIndex; i++) {
            int code = mArgs[i];
            if ( code < 0) {
                if (mArgIndex > 0) {
                    continue;
                } else {
                    code = 0;
                }
            }
            if (code == 0) { 
                mInverseColors = false;
                mForeColor = 7;
                mBackColor = 0;
            } else if (code == 1) { 
                mForeColor |= 0x8;
            } else if (code == 4) { 
                mBackColor |= 0x8;
            } else if (code == 7) { 
                mInverseColors = true;
            } else if (code >= 30 && code <= 37) { 
                mForeColor = (mForeColor & 0x8) | (code - 30);
            } else if (code >= 40 && code <= 47) { 
                mBackColor = (mBackColor & 0x8) | (code - 40);
            } else {
                if (Term.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
                    Log.w(Term.LOG_TAG, String.format("SGR unknown code %d", code));
                }
            }
        }
    }
    private void blockClear(int sx, int sy, int w) {
        blockClear(sx, sy, w, 1);
    }
    private void blockClear(int sx, int sy, int w, int h) {
        mScreen.blockSet(sx, sy, w, h, ' ', getForeColor(), getBackColor());
    }
    private int getForeColor() {
        return mInverseColors ?
                ((mBackColor & 0x7) | (mForeColor & 0x8)) : mForeColor;
    }
    private int getBackColor() {
        return mInverseColors ?
                ((mForeColor & 0x7) | (mBackColor & 0x8)) : mBackColor;
    }
    private void doSetMode(boolean newValue) {
        int modeBit = getArg0(0);
        switch (modeBit) {
        case 4:
            mInsertMode = newValue;
            break;
        case 20:
            mAutomaticNewlineMode = newValue;
            break;
        default:
            unknownParameter(modeBit);
            break;
        }
    }
    private void setHorizontalVerticalPosition() {
        setCursorPosition(getArg1(1) - 1, getArg0(1) - 1);
    }
    private void setCursorPosition(int x, int y) {
        int effectiveTopMargin = 0;
        int effectiveBottomMargin = mRows;
        if ((mDecFlags & K_ORIGIN_MODE_MASK) != 0) {
            effectiveTopMargin = mTopMargin;
            effectiveBottomMargin = mBottomMargin;
        }
        int newRow =
                Math.max(effectiveTopMargin, Math.min(effectiveTopMargin + y,
                        effectiveBottomMargin - 1));
        int newCol = Math.max(0, Math.min(x, mColumns - 1));
        setCursorRowCol(newRow, newCol);
    }
    private void sendDeviceAttributes() {
        byte[] attributes =
                {
                 (byte) 27, (byte) '[', (byte) '?', (byte) '1',
                 (byte) ';', (byte) '2', (byte) 'c'
                };
        write(attributes);
    }
    private void write(byte[] data) {
        try {
            mTermOut.write(data);
            mTermOut.flush();
        } catch (IOException e) {
        }
    }
    private void scroll() {
        mScreen.scroll(mTopMargin, mBottomMargin,
                getForeColor(), getBackColor());
    }
    private void parseArg(byte b) {
        if (b >= '0' && b <= '9') {
            if (mArgIndex < mArgs.length) {
                int oldValue = mArgs[mArgIndex];
                int thisDigit = b - '0';
                int value;
                if (oldValue >= 0) {
                    value = oldValue * 10 + thisDigit;
                } else {
                    value = thisDigit;
                }
                mArgs[mArgIndex] = value;
            }
            continueSequence();
        } else if (b == ';') {
            if (mArgIndex < mArgs.length) {
                mArgIndex++;
            }
            continueSequence();
        } else {
            unknownSequence(b);
        }
    }
    private int getArg0(int defaultValue) {
        return getArg(0, defaultValue);
    }
    private int getArg1(int defaultValue) {
        return getArg(1, defaultValue);
    }
    private int getArg(int index, int defaultValue) {
        int result = mArgs[index];
        if (result < 0) {
            result = defaultValue;
        }
        return result;
    }
    private void unimplementedSequence(byte b) {
        if (Term.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            logError("unimplemented", b);
        }
        finishSequence();
    }
    private void unknownSequence(byte b) {
        if (Term.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            logError("unknown", b);
        }
        finishSequence();
    }
    private void unknownParameter(int parameter) {
        if (Term.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            StringBuilder buf = new StringBuilder();
            buf.append("Unknown parameter");
            buf.append(parameter);
            logError(buf.toString());
        }
    }
    private void logError(String errorType, byte b) {
        if (Term.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            StringBuilder buf = new StringBuilder();
            buf.append(errorType);
            buf.append(" sequence ");
            buf.append(" EscapeState: ");
            buf.append(mEscapeState);
            buf.append(" char: '");
            buf.append((char) b);
            buf.append("' (");
            buf.append(b);
            buf.append(")");
            boolean firstArg = true;
            for (int i = 0; i <= mArgIndex; i++) {
                int value = mArgs[i];
                if (value >= 0) {
                    if (firstArg) {
                        firstArg = false;
                        buf.append("args = ");
                    }
                    buf.append(String.format("%d; ", value));
                }
            }
            logError(buf.toString());
        }
    }
    private void logError(String error) {
        if (Term.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
            Log.e(Term.LOG_TAG, error);
        }
        finishSequence();
    }
    private void finishSequence() {
        mEscapeState = ESC_NONE;
    }
    private boolean autoWrapEnabled() {
        return true;
    }
    private void emit(byte b) {
        boolean autoWrap = autoWrapEnabled();
        if (autoWrap) {
            if (mCursorCol == mColumns - 1 && mAboutToAutoWrap) {
                mScreen.setLineWrap(mCursorRow);
                mCursorCol = 0;
                if (mCursorRow + 1 < mBottomMargin) {
                    mCursorRow++;
                } else {
                    scroll();
                }
            }
        }
        if (mInsertMode) { 
            int destCol = mCursorCol + 1;
            if (destCol < mColumns) {
                mScreen.blockCopy(mCursorCol, mCursorRow, mColumns - destCol,
                        1, destCol, mCursorRow);
            }
        }
        mScreen.set(mCursorCol, mCursorRow, b, getForeColor(), getBackColor());
        if (autoWrap) {
            mAboutToAutoWrap = (mCursorCol == mColumns - 1);
        }
        mCursorCol = Math.min(mCursorCol + 1, mColumns - 1);
    }
    private void setCursorRow(int row) {
        mCursorRow = row;
        mAboutToAutoWrap = false;
    }
    private void setCursorCol(int col) {
        mCursorCol = col;
        mAboutToAutoWrap = false;
    }
    private void setCursorRowCol(int row, int col) {
        mCursorRow = Math.min(row, mRows-1);
        mCursorCol = Math.min(col, mColumns-1);
        mAboutToAutoWrap = false;
    }
    public void reset() {
        mCursorRow = 0;
        mCursorCol = 0;
        mArgIndex = 0;
        mContinueSequence = false;
        mEscapeState = ESC_NONE;
        mSavedCursorRow = 0;
        mSavedCursorCol = 0;
        mDecFlags = 0;
        mSavedDecFlags = 0;
        mInsertMode = false;
        mAutomaticNewlineMode = false;
        mTopMargin = 0;
        mBottomMargin = mRows;
        mAboutToAutoWrap = false;
        mForeColor = 7;
        mBackColor = 0;
        mInverseColors = false;
        mbKeypadApplicationMode = false;
        mAlternateCharSet = false;
        setDefaultTabStops();
        blockClear(0, 0, mColumns, mRows);
    }
    public String getTranscriptText() {
        return mScreen.getTranscriptText();
    }
}
interface TextRenderer {
    int getCharacterWidth();
    int getCharacterHeight();
    void drawTextRun(Canvas canvas, float x, float y,
            int lineOffset, char[] text,
            int index, int count, boolean cursor, int foreColor, int backColor);
}
abstract class BaseTextRenderer implements TextRenderer {
    protected int[] mForePaint = {
            0xff000000, 
            0xffff0000, 
            0xff00ff00, 
            0xffffff00, 
            0xff0000ff, 
            0xffff00ff, 
            0xff00ffff, 
            0xffffffff  
    };
    protected int[] mBackPaint = {
            0xff000000, 
            0xffcc0000, 
            0xff00cc00, 
            0xffcccc00, 
            0xff0000cc, 
            0xffff00cc, 
            0xff00cccc, 
            0xffffffff  
    };
    protected final static int mCursorPaint = 0xff808080;
    public BaseTextRenderer(int forePaintColor, int backPaintColor) {
        mForePaint[7] = forePaintColor;
        mBackPaint[0] = backPaintColor;
    }
}
class Bitmap4x8FontRenderer extends BaseTextRenderer {
    private final static int kCharacterWidth = 4;
    private final static int kCharacterHeight = 8;
    private Bitmap mFont;
    private int mCurrentForeColor;
    private int mCurrentBackColor;
    private float[] mColorMatrix;
    private Paint mPaint;
    private static final float BYTE_SCALE = 1.0f / 255.0f;
    public Bitmap4x8FontRenderer(Resources resources,
            int forePaintColor, int backPaintColor) {
        super(forePaintColor, backPaintColor);
        mFont = BitmapFactory.decodeResource(resources,
                R.drawable.atari_small);
        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }
    public int getCharacterWidth() {
        return kCharacterWidth;
    }
    public int getCharacterHeight() {
        return kCharacterHeight;
    }
    public void drawTextRun(Canvas canvas, float x, float y,
            int lineOffset, char[] text, int index, int count,
            boolean cursor, int foreColor, int backColor) {
        setColorMatrix(mForePaint[foreColor & 7],
                cursor ? mCursorPaint : mBackPaint[backColor & 7]);
        int destX = (int) x + kCharacterWidth * lineOffset;
        int destY = (int) y;
        Rect srcRect = new Rect();
        Rect destRect = new Rect();
        destRect.top = (destY - kCharacterHeight);
        destRect.bottom = destY;
        for(int i = 0; i < count; i++) {
            char c = text[i + index];
            if ((cursor || (c != 32)) && (c < 128)) {
                int cellX = c & 31;
                int cellY = (c >> 5) & 3;
                int srcX = cellX * kCharacterWidth;
                int srcY = cellY * kCharacterHeight;
                srcRect.set(srcX, srcY,
                        srcX + kCharacterWidth, srcY + kCharacterHeight);
                destRect.left = destX;
                destRect.right = destX + kCharacterWidth;
                canvas.drawBitmap(mFont, srcRect, destRect, mPaint);
            }
            destX += kCharacterWidth;
        }
    }
    private void setColorMatrix(int foreColor, int backColor) {
        if ((foreColor != mCurrentForeColor)
                || (backColor != mCurrentBackColor)
                || (mColorMatrix == null)) {
            mCurrentForeColor = foreColor;
            mCurrentBackColor = backColor;
            if (mColorMatrix == null) {
                mColorMatrix = new float[20];
                mColorMatrix[18] = 1.0f; 
            }
            for (int component = 0; component < 3; component++) {
                int rightShift = (2 - component) << 3;
                int fore = 0xff & (foreColor >> rightShift);
                int back = 0xff & (backColor >> rightShift);
                int delta = back - fore;
                mColorMatrix[component * 6] = delta * BYTE_SCALE;
                mColorMatrix[component * 5 + 4] = fore;
            }
            mPaint.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
        }
    }
}
class PaintRenderer extends BaseTextRenderer {
    public PaintRenderer(int fontSize, int forePaintColor, int backPaintColor) {
        super(forePaintColor, backPaintColor);
        mTextPaint = new Paint();
        mTextPaint.setTypeface(Typeface.MONOSPACE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(fontSize);
        mCharHeight = (int) Math.ceil(mTextPaint.getFontSpacing());
        mCharAscent = (int) Math.ceil(mTextPaint.ascent());
        mCharDescent = mCharHeight + mCharAscent;
        mCharWidth = (int) mTextPaint.measureText(EXAMPLE_CHAR, 0, 1);
    }
    public void drawTextRun(Canvas canvas, float x, float y, int lineOffset,
            char[] text, int index, int count,
            boolean cursor, int foreColor, int backColor) {
        if (cursor) {
            mTextPaint.setColor(mCursorPaint);
        } else {
            mTextPaint.setColor(mBackPaint[backColor & 0x7]);
        }
        float left = x + lineOffset * mCharWidth;
        canvas.drawRect(left, y + mCharAscent,
                left + count * mCharWidth, y + mCharDescent,
                mTextPaint);
        boolean bold = ( foreColor & 0x8 ) != 0;
        boolean underline = (backColor & 0x8) != 0;
        if (bold) {
            mTextPaint.setFakeBoldText(true);
        }
        if (underline) {
            mTextPaint.setUnderlineText(true);
        }
        mTextPaint.setColor(mForePaint[foreColor & 0x7]);
        canvas.drawText(text, index, count, left, y, mTextPaint);
        if (bold) {
            mTextPaint.setFakeBoldText(false);
        }
        if (underline) {
            mTextPaint.setUnderlineText(false);
        }
    }
    public int getCharacterHeight() {
        return mCharHeight;
    }
    public int getCharacterWidth() {
        return mCharWidth;
    }
    private Paint mTextPaint;
    private int mCharWidth;
    private int mCharHeight;
    private int mCharAscent;
    private int mCharDescent;
    private static final char[] EXAMPLE_CHAR = {'X'};
    }
class ByteQueue {
    public ByteQueue(int size) {
        mBuffer = new byte[size];
    }
    public int getBytesAvailable() {
        synchronized(this) {
            return mStoredBytes;
        }
    }
    public int read(byte[] buffer, int offset, int length)
        throws InterruptedException {
        if (length + offset > buffer.length) {
            throw
                new IllegalArgumentException("length + offset > buffer.length");
        }
        if (length < 0) {
            throw
            new IllegalArgumentException("length < 0");
        }
        if (length == 0) {
            return 0;
        }
        synchronized(this) {
            while (mStoredBytes == 0) {
                wait();
            }
            int totalRead = 0;
            int bufferLength = mBuffer.length;
            boolean wasFull = bufferLength == mStoredBytes;
            while (length > 0 && mStoredBytes > 0) {
                int oneRun = Math.min(bufferLength - mHead, mStoredBytes);
                int bytesToCopy = Math.min(length, oneRun);
                System.arraycopy(mBuffer, mHead, buffer, offset, bytesToCopy);
                mHead += bytesToCopy;
                if (mHead >= bufferLength) {
                    mHead = 0;
                }
                mStoredBytes -= bytesToCopy;
                length -= bytesToCopy;
                offset += bytesToCopy;
                totalRead += bytesToCopy;
            }
            if (wasFull) {
                notify();
            }
            return totalRead;
        }
    }
    public void write(byte[] buffer, int offset, int length)
    throws InterruptedException {
        if (length + offset > buffer.length) {
            throw
                new IllegalArgumentException("length + offset > buffer.length");
        }
        if (length < 0) {
            throw
            new IllegalArgumentException("length < 0");
        }
        if (length == 0) {
            return;
        }
        synchronized(this) {
            int bufferLength = mBuffer.length;
            boolean wasEmpty = mStoredBytes == 0;
            while (length > 0) {
                while(bufferLength == mStoredBytes) {
                    wait();
                }
                int tail = mHead + mStoredBytes;
                int oneRun;
                if (tail >= bufferLength) {
                    tail = tail - bufferLength;
                    oneRun = mHead - tail;
                } else {
                    oneRun = bufferLength - tail;
                }
                int bytesToCopy = Math.min(oneRun, length);
                System.arraycopy(buffer, offset, mBuffer, tail, bytesToCopy);
                offset += bytesToCopy;
                mStoredBytes += bytesToCopy;
                length -= bytesToCopy;
            }
            if (wasEmpty) {
                notify();
            }
        }
    }
    private byte[] mBuffer;
    private int mHead;
    private int mStoredBytes;
}
class EmulatorView extends View implements GestureDetector.OnGestureListener {
    private boolean mKnownSize;
    private TranscriptScreen mTranscriptScreen;
    private static final int TRANSCRIPT_ROWS = 10000;
    private int mCharacterWidth;
    private int mCharacterHeight;
    private TextRenderer mTextRenderer;
    private int mTextSize;
    private int mForeground;
    private int mBackground;
    private Paint mCursorPaint;
    private Paint mBackgroundPaint;
    private TerminalEmulator mEmulator;
    private int mRows;
    private int mColumns;
    private int mVisibleColumns;
    private int mTopRow;
    private int mLeftColumn;
    private FileDescriptor mTermFd;
    private FileInputStream mTermIn;
    private FileOutputStream mTermOut;
    private ByteQueue mByteQueue;
    private byte[] mReceiveBuffer;
    private static final int UPDATE = 1;
    private Thread mPollingThread;
    private GestureDetector mGestureDetector;
    private float mScrollRemainder;
    private TermKeyListener mKeyListener;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE) {
                update();
            }
        }
    };
    public EmulatorView(Context context) {
        super(context);
        commonConstructor();
    }
    public void register(TermKeyListener listener) {
        mKeyListener = listener;
    }
    public void setColors(int foreground, int background) {
        mForeground = foreground;
        mBackground = background;
        updateText();
    }
    public String getTranscriptText() {
        return mEmulator.getTranscriptText();
    }
    public void resetTerminal() {
        mEmulator.reset();
        invalidate();
    }
    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new BaseInputConnection(this, false) {
            @Override
            public boolean beginBatchEdit() {
                return true;
            }
            @Override
            public boolean clearMetaKeyStates(int states) {
                return true;
            }
            @Override
            public boolean commitCompletion(CompletionInfo text) {
                return true;
            }
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                sendText(text);
                return true;
            }
            @Override
            public boolean deleteSurroundingText(int leftLength, int rightLength) {
                return true;
            }
            @Override
            public boolean endBatchEdit() {
                return true;
            }
            @Override
            public boolean finishComposingText() {
                return true;
            }
            @Override
            public int getCursorCapsMode(int reqModes) {
                return 0;
            }
            @Override
            public ExtractedText getExtractedText(ExtractedTextRequest request,
                    int flags) {
                return null;
            }
            @Override
            public CharSequence getTextAfterCursor(int n, int flags) {
                return null;
            }
            @Override
            public CharSequence getTextBeforeCursor(int n, int flags) {
                return null;
            }
            @Override
            public boolean performEditorAction(int actionCode) {
                if(actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    sendText("\n");
                    return true;
                }
                return false;
            }
            @Override
            public boolean performContextMenuAction(int id) {
                return true;
            }
            @Override
            public boolean performPrivateCommand(String action, Bundle data) {
                return true;
            }
            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch(event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DEL:
                        sendChar(127);
                        break;
                    }
                }
                return true;
            }
            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                return true;
            }
            @Override
            public boolean setSelection(int start, int end) {
                return true;
            }
            private void sendChar(int c) {
                try {
                    mapAndSend(c);
                } catch (IOException ex) {
                }
            }
            private void sendText(CharSequence text) {
                int n = text.length();
                try {
                    for(int i = 0; i < n; i++) {
                        char c = text.charAt(i);
                        mapAndSend(c);
                    }
                } catch (IOException e) {
                }
            }
            private void mapAndSend(int c) throws IOException {
                mTermOut.write(
                        mKeyListener.mapControlChar(c));
            }
        };
    }
    public boolean getKeypadApplicationMode() {
        return mEmulator.getKeypadApplicationMode();
    }
    public EmulatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public EmulatorView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a =
                context.obtainStyledAttributes(android.R.styleable.View);
        initializeScrollbars(a);
        a.recycle();
        commonConstructor();
    }
    private void commonConstructor() {
        mTextRenderer = null;
        mCursorPaint = new Paint();
        mCursorPaint.setARGB(255,128,128,128);
        mBackgroundPaint = new Paint();
        mTopRow = 0;
        mLeftColumn = 0;
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setIsLongpressEnabled(false);
        setVerticalScrollBarEnabled(true);
    }
    @Override
    protected int computeVerticalScrollRange() {
        return mTranscriptScreen.getActiveRows();
    }
    @Override
    protected int computeVerticalScrollExtent() {
        return mRows;
    }
    @Override
    protected int computeVerticalScrollOffset() {
        return mTranscriptScreen.getActiveRows() + mTopRow - mRows;
    }
    public void initialize(FileDescriptor termFd, FileOutputStream termOut) {
        mTermOut = termOut;
        mTermFd = termFd;
        mTextSize = 10;
        mForeground = Term.WHITE;
        mBackground = Term.BLACK;
        updateText();
        mTermIn = new FileInputStream(mTermFd);
        mReceiveBuffer = new byte[4 * 1024];
        mByteQueue = new ByteQueue(4 * 1024);
    }
    public void append(byte[] buffer, int base, int length) {
        mEmulator.append(buffer, base, length);
        ensureCursorVisible();
        invalidate();
    }
    public void page(int delta) {
        mTopRow =
                Math.min(0, Math.max(-(mTranscriptScreen
                        .getActiveTranscriptRows()), mTopRow + mRows * delta));
        invalidate();
    }
    public void pageHorizontal(int deltaColumns) {
        mLeftColumn =
                Math.max(0, Math.min(mLeftColumn + deltaColumns, mColumns
                        - mVisibleColumns));
        invalidate();
    }
    public void setTextSize(int fontSize) {
        mTextSize = fontSize;
        updateText();
    }
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
    public void onLongPress(MotionEvent e) {
    }
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
            float distanceX, float distanceY) {
        distanceY += mScrollRemainder;
        int deltaRows = (int) (distanceY / mCharacterHeight);
        mScrollRemainder = distanceY - deltaRows * mCharacterHeight;
        mTopRow =
            Math.min(0, Math.max(-(mTranscriptScreen
                    .getActiveTranscriptRows()), mTopRow + deltaRows));
        invalidate();
        return true;
   }
    public void onSingleTapConfirmed(MotionEvent e) {
    }
    public boolean onJumpTapDown(MotionEvent e1, MotionEvent e2) {
       mTopRow = 0;
       invalidate();
       return true;
    }
    public boolean onJumpTapUp(MotionEvent e1, MotionEvent e2) {
        mTopRow = -mTranscriptScreen.getActiveTranscriptRows();
        invalidate();
        return true;
    }
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        mScrollRemainder = 0.0f;
        onScroll(e1, e2, 2 * velocityX, -2 * velocityY);
        return true;
    }
    public void onShowPress(MotionEvent e) {
    }
    public boolean onDown(MotionEvent e) {
        mScrollRemainder = 0.0f;
        return true;
    }
    @Override public boolean onTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }
    private void updateText() {
        if (mTextSize > 0) {
            mTextRenderer = new PaintRenderer(mTextSize, mForeground,
                    mBackground);
        }
        else {
            mTextRenderer = new Bitmap4x8FontRenderer(getResources(),
                    mForeground, mBackground);
        }
        mBackgroundPaint.setColor(mBackground);
        mCharacterWidth = mTextRenderer.getCharacterWidth();
        mCharacterHeight = mTextRenderer.getCharacterHeight();
        if (mKnownSize) {
            updateSize(getWidth(), getHeight());
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateSize(w, h);
        if (!mKnownSize) {
            mKnownSize = true;
            mPollingThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while(true) {
                            int read = mTermIn.read(mBuffer);
                            mByteQueue.write(mBuffer, 0, read);
                            mHandler.sendMessage(
                                    mHandler.obtainMessage(UPDATE));
                        }
                    } catch (IOException e) {
                    } catch (InterruptedException e) {
                    }
                }
                private byte[] mBuffer = new byte[4096];
            });
            mPollingThread.setName("Input reader");
            mPollingThread.start();
        }
    }
    private void updateSize(int w, int h) {
        mColumns = w / mCharacterWidth;
        mRows = h / mCharacterHeight;
        Exec.setPtyWindowSize(mTermFd, mRows, mColumns, w, h);
        if (mTranscriptScreen != null) {
            mEmulator.updateSize(mColumns, mRows);
        } else {
            mTranscriptScreen =
                    new TranscriptScreen(mColumns, TRANSCRIPT_ROWS, mRows, 0, 7);
            mEmulator =
                    new TerminalEmulator(mTranscriptScreen, mColumns, mRows,
                            mTermOut);
        }
        mTopRow = 0;
        mLeftColumn = 0;
        invalidate();
    }
    void updateSize() {
        if (mKnownSize) {
            updateSize(getWidth(), getHeight());
        }
    }
    private void update() {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
        try {
            int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
            append(mReceiveBuffer, 0, bytesRead);
        } catch (InterruptedException e) {
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        canvas.drawRect(0, 0, w, h, mBackgroundPaint);
        mVisibleColumns = w / mCharacterWidth;
        float x = -mLeftColumn * mCharacterWidth;
        float y = mCharacterHeight;
        int endLine = mTopRow + mRows;
        int cx = mEmulator.getCursorCol();
        int cy = mEmulator.getCursorRow();
        for (int i = mTopRow; i < endLine; i++) {
            int cursorX = -1;
            if (i == cy) {
                cursorX = cx;
            }
            mTranscriptScreen.drawText(i, canvas, x, y, mTextRenderer, cursorX);
            y += mCharacterHeight;
        }
    }
    private void ensureCursorVisible() {
        mTopRow = 0;
        if (mVisibleColumns > 0) {
            int cx = mEmulator.getCursorCol();
            int visibleCursorX = mEmulator.getCursorCol() - mLeftColumn;
            if (visibleCursorX < 0) {
                mLeftColumn = cx;
            } else if (visibleCursorX >= mVisibleColumns) {
                mLeftColumn = (cx - mVisibleColumns) + 1;
            }
        }
    }
}
class TermKeyListener {
    private class ModifierKey {
        private int mState;
        private static final int UNPRESSED = 0;
        private static final int PRESSED = 1;
        private static final int RELEASED = 2;
        private static final int USED = 3;
        private static final int LOCKED = 4;
        public ModifierKey() {
            mState = UNPRESSED;
        }
        public void onPress() {
            switch (mState) {
            case PRESSED:
                break;
            case RELEASED:
                mState = LOCKED;
                break;
            case USED:
                break;
            case LOCKED:
                mState = UNPRESSED;
                break;
            default:
                mState = PRESSED;
                break;
            }
        }
        public void onRelease() {
            switch (mState) {
            case USED:
                mState = UNPRESSED;
                break;
            case PRESSED:
                mState = RELEASED;
                break;
            default:
                break;
            }
        }
        public void adjustAfterKeypress() {
            switch (mState) {
            case PRESSED:
                mState = USED;
                break;
            case RELEASED:
                mState = UNPRESSED;
                break;
            default:
                break;
            }
        }
        public boolean isActive() {
            return mState != UNPRESSED;
        }
    }
    private ModifierKey mAltKey = new ModifierKey();
    private ModifierKey mCapKey = new ModifierKey();
    private ModifierKey mControlKey = new ModifierKey();
    public TermKeyListener() {
    }
    public void handleControlKey(boolean down) {
        if (down) {
            mControlKey.onPress();
        } else {
            mControlKey.onRelease();
        }
    }
    public int mapControlChar(int ch) {
        int result = ch;
        if (mControlKey.isActive()) {
            if (result >= 'a' && result <= 'z') {
                result = (char) (result - 'a' + '\001');
            } else if (result == ' ') {
                result = 0;
            } else if ((result == '[') || (result == '1')) {
                result = 27;
            } else if ((result == '\\') || (result == '.')) {
                result = 28;
            } else if ((result == ']') || (result == '0')) {
                result = 29;
            } else if ((result == '^') || (result == '6')) {
                result = 30; 
            } else if ((result == '_') || (result == '5')) {
                result = 31;
            }
        }
        if (result > -1) {
            mAltKey.adjustAfterKeypress();
            mCapKey.adjustAfterKeypress();
            mControlKey.adjustAfterKeypress();
        }
        return result;
    }
    public int keyDown(int keyCode, KeyEvent event) {
        int result = -1;
        switch (keyCode) {
        case KeyEvent.KEYCODE_ALT_RIGHT:
        case KeyEvent.KEYCODE_ALT_LEFT:
            mAltKey.onPress();
            break;
        case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_SHIFT_RIGHT:
            mCapKey.onPress();
            break;
        case KeyEvent.KEYCODE_ENTER:
            result = '\r';
            break;
        case KeyEvent.KEYCODE_DEL:
            result = 127;
            break;
        default: {
            result = event.getUnicodeChar(
                   (mCapKey.isActive() ? KeyEvent.META_SHIFT_ON : 0) |
                   (mAltKey.isActive() ? KeyEvent.META_ALT_ON : 0));
            break;
            }
        }
        result = mapControlChar(result);
        return result;
    }
    public void keyUp(int keyCode) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_ALT_LEFT:
        case KeyEvent.KEYCODE_ALT_RIGHT:
            mAltKey.onRelease();
            break;
        case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_SHIFT_RIGHT:
            mCapKey.onRelease();
            break;
        default:
            break;
        }
    }
}
