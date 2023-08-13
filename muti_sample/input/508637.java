class CommandLineProcessor {
    public final static String GLOBAL_FLAG_VERB = "@@internal@@";
    public final static String NO_VERB_OBJECT = "";
    public static final String KEY_HELP = "help";
    public static final String KEY_VERBOSE = "verbose";
    public static final String KEY_SILENT = "silent";
    private String mVerbRequested;
    private String mDirectObjectRequested;
    private final String[][] mActions;
    private static final int ACTION_VERB_INDEX = 0;
    private static final int ACTION_OBJECT_INDEX = 1;
    private static final int ACTION_DESC_INDEX = 2;
    private static final int ACTION_ALT_OBJECT_INDEX = 3;
    private final HashMap<String, Arg> mArguments = new HashMap<String, Arg>();
    private final ISdkLog mLog;
    public CommandLineProcessor(ISdkLog logger, String[][] actions) {
        mLog = logger;
        mActions = actions;
        define(Mode.BOOLEAN, false, GLOBAL_FLAG_VERB, NO_VERB_OBJECT, "v", KEY_VERBOSE,
                "Verbose mode: errors, warnings and informational messages are printed.",
                false);
        define(Mode.BOOLEAN, false, GLOBAL_FLAG_VERB, NO_VERB_OBJECT, "s", KEY_SILENT,
                "Silent mode: only errors are printed out.",
                false);
        define(Mode.BOOLEAN, false, GLOBAL_FLAG_VERB, NO_VERB_OBJECT, "h", KEY_HELP,
                "Help on a specific command.",
                false);
    }
    public boolean acceptLackOfVerb() {
        return false;
    }
    public boolean isVerbose() {
        return ((Boolean) getValue(GLOBAL_FLAG_VERB, NO_VERB_OBJECT, KEY_VERBOSE)).booleanValue();
    }
    public boolean isSilent() {
        return ((Boolean) getValue(GLOBAL_FLAG_VERB, NO_VERB_OBJECT, KEY_SILENT)).booleanValue();
    }
    public boolean isHelpRequested() {
        return ((Boolean) getValue(GLOBAL_FLAG_VERB, NO_VERB_OBJECT, KEY_HELP)).booleanValue();
    }
    public String getVerb() {
        return mVerbRequested;
    }
    public String getDirectObject() {
        return mDirectObjectRequested;
    }
    public Object getValue(String verb, String directObject, String longFlagName) {
        if (verb != null && directObject != null) {
            String key = verb + "/" + directObject + "/" + longFlagName;
            Arg arg = mArguments.get(key);
            return arg.getCurrentValue();
        }
        Object lastDefault = null;
        for (Arg arg : mArguments.values()) {
            if (arg.getLongArg().equals(longFlagName)) {
                if (verb == null || arg.getVerb().equals(verb)) {
                    if (directObject == null || arg.getDirectObject().equals(directObject)) {
                        if (arg.isInCommandLine()) {
                            return arg.getCurrentValue();
                        }
                        if (arg.getCurrentValue() != null) {
                            lastDefault = arg.getCurrentValue();
                        }
                    }
                }
            }
        }
        return lastDefault;
    }
    protected void setValue(String verb, String directObject, String longFlagName, Object value) {
        String key = verb + "/" + directObject + "/" + longFlagName;
        Arg arg = mArguments.get(key);
        arg.setCurrentValue(value);
    }
    public void parseArgs(String[] args) {
        String needsHelp = null;
        String verb = null;
        String directObject = null;
        try {
            int n = args.length;
            for (int i = 0; i < n; i++) {
                Arg arg = null;
                String a = args[i];
                if (a.startsWith("--")) {
                    arg = findLongArg(verb, directObject, a.substring(2));
                } else if (a.startsWith("-")) {
                    arg = findShortArg(verb, directObject, a.substring(1));
                }
                if (arg == null) {
                    if (a.startsWith("-")) {
                        if (verb == null || directObject == null) {
                            needsHelp = String.format(
                                "Flag '%1$s' is not a valid global flag. Did you mean to specify it after the verb/object name?",
                                a);
                            return;
                        } else {
                            needsHelp = String.format(
                                    "Flag '%1$s' is not valid for '%2$s %3$s'.",
                                    a, verb, directObject);
                            return;
                        }
                    }
                    if (verb == null) {
                        for (String[] actionDesc : mActions) {
                            if (actionDesc[ACTION_VERB_INDEX].equals(a)) {
                                verb = a;
                                break;
                            }
                        }
                        if (verb == null) {
                            needsHelp = String.format(
                                "Expected verb after global parameters but found '%1$s' instead.",
                                a);
                            return;
                        }
                    } else if (directObject == null) {
                        for (String[] actionDesc : mActions) {
                            if (actionDesc[ACTION_VERB_INDEX].equals(verb)) {
                                if (actionDesc[ACTION_OBJECT_INDEX].equals(a)) {
                                    directObject = a;
                                    break;
                                } else if (actionDesc.length > ACTION_ALT_OBJECT_INDEX &&
                                        actionDesc[ACTION_ALT_OBJECT_INDEX].equals(a)) {
                                    directObject = actionDesc[ACTION_OBJECT_INDEX];
                                    break;
                                }
                            }
                        }
                        if (directObject == null) {
                            needsHelp = String.format(
                                "Expected verb after global parameters but found '%1$s' instead.",
                                a);
                            return;
                        }
                    }
                } else if (arg != null) {
                    arg.setInCommandLine(true);
                    String error = null;
                    if (arg.getMode().needsExtra()) {
                        if (++i >= n) {
                            needsHelp = String.format("Missing argument for flag %1$s.", a);
                            return;
                        }
                        error = arg.getMode().process(arg, args[i]);
                    } else {
                        error = arg.getMode().process(arg, null);
                        if (isHelpRequested()) {
                            printHelpAndExit(null);
                            return;
                        }
                    }
                    if (error != null) {
                        needsHelp = String.format("Invalid usage for flag %1$s: %2$s.", a, error);
                        return;
                    }
                }
            }
            if (needsHelp == null) {
                if (verb == null && !acceptLackOfVerb()) {
                    needsHelp = "Missing verb name.";
                } else if (verb != null) {
                    if (directObject == null) {
                        for (String[] actionDesc : mActions) {
                            if (actionDesc[ACTION_VERB_INDEX].equals(verb) &&
                                    actionDesc[ACTION_OBJECT_INDEX].equals(NO_VERB_OBJECT)) {
                                directObject = NO_VERB_OBJECT;
                                break;
                            }
                        }
                        if (directObject == null) {
                            needsHelp = String.format("Missing object name for verb '%1$s'.", verb);
                            return;
                        }
                    }
                    String missing = null;
                    boolean plural = false;
                    for (Entry<String, Arg> entry : mArguments.entrySet()) {
                        Arg arg = entry.getValue();
                        if (arg.getVerb().equals(verb) &&
                                arg.getDirectObject().equals(directObject)) {
                            if (arg.isMandatory() && arg.getCurrentValue() == null) {
                                if (missing == null) {
                                    missing = "--" + arg.getLongArg();
                                } else {
                                    missing += ", --" + arg.getLongArg();
                                    plural = true;
                                }
                            }
                        }
                    }
                    if (missing != null) {
                        needsHelp  = String.format(
                                "The %1$s %2$s must be defined for action '%3$s %4$s'",
                                plural ? "parameters" : "parameter",
                                missing,
                                verb,
                                directObject);
                    }
                    mVerbRequested = verb;
                    mDirectObjectRequested = directObject;
                }
            }
        } finally {
            if (needsHelp != null) {
                printHelpAndExitForAction(verb, directObject, needsHelp);
            }
        }
    }
    protected Arg findLongArg(String verb, String directObject, String longName) {
        if (verb == null) {
            verb = GLOBAL_FLAG_VERB;
        }
        if (directObject == null) {
            directObject = NO_VERB_OBJECT;
        }
        String key = verb + "/" + directObject + "/" + longName;
        return mArguments.get(key);
    }
    protected Arg findShortArg(String verb, String directObject, String shortName) {
        if (verb == null) {
            verb = GLOBAL_FLAG_VERB;
        }
        if (directObject == null) {
            directObject = NO_VERB_OBJECT;
        }
        for (Entry<String, Arg> entry : mArguments.entrySet()) {
            Arg arg = entry.getValue();
            if (arg.getVerb().equals(verb) && arg.getDirectObject().equals(directObject)) {
                if (shortName.equals(arg.getShortArg())) {
                    return arg;
                }
            }
        }
        return null;
    }
    public void printHelpAndExit(String errorFormat, Object... args) {
        printHelpAndExitForAction(null , null , errorFormat, args);
    }
    public void printHelpAndExitForAction(String verb, String directObject,
            String errorFormat, Object... args) {
        if (errorFormat != null) {
            stderr(errorFormat, args);
        }
        stdout("\n" +
            "Usage:\n" +
            "  android [global options] action [action options]\n" +
            "\n" +
            "Global options:");
        listOptions(GLOBAL_FLAG_VERB, NO_VERB_OBJECT);
        if (verb == null || directObject == null) {
            stdout("\nValid actions are composed of a verb and an optional direct object:");
            for (String[] action : mActions) {
                stdout("- %1$6s %2$-12s: %3$s",
                        action[ACTION_VERB_INDEX],
                        action[ACTION_OBJECT_INDEX],
                        action[ACTION_DESC_INDEX]);
            }
        }
        for (String[] action : mActions) {
            if (verb == null || verb.equals(action[ACTION_VERB_INDEX])) {
                if (directObject == null || directObject.equals(action[ACTION_OBJECT_INDEX])) {
                    stdout("\nAction \"%1$s %2$s\":",
                            action[ACTION_VERB_INDEX],
                            action[ACTION_OBJECT_INDEX]);
                    stdout("  %1$s", action[ACTION_DESC_INDEX]);
                    stdout("Options:");
                    listOptions(action[ACTION_VERB_INDEX], action[ACTION_OBJECT_INDEX]);
                }
            }
        }
        exit();
    }
    protected void listOptions(String verb, String directObject) {
        int numOptions = 0;
        for (Entry<String, Arg> entry : mArguments.entrySet()) {
            Arg arg = entry.getValue();
            if (arg.getVerb().equals(verb) && arg.getDirectObject().equals(directObject)) {
                String value = "";
                String required = "";
                if (arg.isMandatory()) {
                    required = " [required]";
                } else {
                    if (arg.getDefaultValue() instanceof String[]) {
                        for (String v : (String[]) arg.getDefaultValue()) {
                            if (value.length() > 0) {
                                value += ", ";
                            }
                            value += v;
                        }
                    } else if (arg.getDefaultValue() != null) {
                        Object v = arg.getDefaultValue();
                        if (arg.getMode() != Mode.BOOLEAN || v.equals(Boolean.TRUE)) {
                            value = v.toString();
                        }
                    }
                    if (value.length() > 0) {
                        value = " [Default: " + value + "]";
                    }
                }
                stdout("  -%1$s %2$-10s %3$s%4$s%5$s",
                        arg.getShortArg(),
                        "--" + arg.getLongArg(),
                        arg.getDescription(),
                        value,
                        required);
                numOptions++;
            }
        }
        if (numOptions == 0) {
            stdout("  No options");
        }
    }
    static enum Mode {
        BOOLEAN {
            @Override
            public boolean needsExtra() {
                return false;
            }
            @Override
            public String process(Arg arg, String extra) {
                arg.setCurrentValue(! ((Boolean) arg.getCurrentValue()).booleanValue());
                return null;
            }
        },
        INTEGER {
            @Override
            public boolean needsExtra() {
                return true;
            }
            @Override
            public String process(Arg arg, String extra) {
                try {
                    arg.setCurrentValue(Integer.parseInt(extra));
                    return null;
                } catch (NumberFormatException e) {
                    return String.format("Failed to parse '%1$s' as an integer: %2%s",
                            extra, e.getMessage());
                }
            }
        },
        ENUM {
            @Override
            public boolean needsExtra() {
                return true;
            }
            @Override
            public String process(Arg arg, String extra) {
                StringBuilder desc = new StringBuilder();
                String[] values = (String[]) arg.getDefaultValue();
                for (String value : values) {
                    if (value.equals(extra)) {
                        arg.setCurrentValue(extra);
                        return null;
                    }
                    if (desc.length() != 0) {
                        desc.append(", ");
                    }
                    desc.append(value);
                }
                return String.format("'%1$s' is not one of %2$s", extra, desc.toString());
            }
        },
        STRING {
            @Override
            public boolean needsExtra() {
                return true;
            }
            @Override
            public String process(Arg arg, String extra) {
                arg.setCurrentValue(extra);
                return null;
            }
        };
        public abstract boolean needsExtra();
        public abstract String process(Arg arg, String extra);
    }
    static class Arg {
        private final String mVerb;
        private final String mDirectObject;
        private final String mShortName;
        private final String mLongName;
        private final String mDescription;
        private final Object mDefaultValue;
        private final Mode mMode;
        private final boolean mMandatory;
        private Object mCurrentValue;
        private boolean mInCommandLine;
        public Arg(Mode mode,
                   boolean mandatory,
                   String verb,
                   String directObject,
                   String shortName,
                   String longName,
                   String description,
                   Object defaultValue) {
            mMode = mode;
            mMandatory = mandatory;
            mVerb = verb;
            mDirectObject = directObject;
            mShortName = shortName;
            mLongName = longName;
            mDescription = description;
            mDefaultValue = defaultValue;
            mInCommandLine = false;
            if (defaultValue instanceof String[]) {
                mCurrentValue = ((String[])defaultValue)[0];
            } else {
                mCurrentValue = mDefaultValue;
            }
        }
        public boolean isMandatory() {
            return mMandatory;
        }
        public String getShortArg() {
            return mShortName;
        }
        public String getLongArg() {
            return mLongName;
        }
        public String getDescription() {
            return mDescription;
        }
        public String getVerb() {
            return mVerb;
        }
        public String getDirectObject() {
            return mDirectObject;
        }
        public Object getDefaultValue() {
            return mDefaultValue;
        }
        public Object getCurrentValue() {
            return mCurrentValue;
        }
        public void setCurrentValue(Object currentValue) {
            mCurrentValue = currentValue;
        }
        public Mode getMode() {
            return mMode;
        }
        public boolean isInCommandLine() {
            return mInCommandLine;
        }
        public void setInCommandLine(boolean inCommandLine) {
            mInCommandLine = inCommandLine;
        }
    }
    protected void define(Mode mode,
            boolean mandatory,
            String verb,
            String directObject,
            String shortName, String longName,
            String description, Object defaultValue) {
        assert(!(mandatory && mode == Mode.BOOLEAN)); 
        if (directObject == null) {
            directObject = NO_VERB_OBJECT;
        }
        String key = verb + "/" + directObject + "/" + longName;
        mArguments.put(key, new Arg(mode, mandatory,
                verb, directObject, shortName, longName, description, defaultValue));
    }
    protected void exit() {
        System.exit(1);
    }
    protected void stdout(String format, Object...args) {
        mLog.printf(format + "\n", args);
    }
    protected void stderr(String format, Object...args) {
        mLog.error(null, format, args);
    }
}
