public class CommandParser {
    private HashMap<String, String> mValues = new HashMap<String, String>();
    private String mAction;
    private ArrayList<String> mActionValues = new ArrayList<String>();
    private int mArgLength;
    private static final String COMMAND_PARSE_EXPRESSION = "(((\\\\\\s)|[\\S&&[^\"]])+|\".+\")";
    private static Set<String> sOptionsSet = new HashSet<String>(Arrays.asList(
            CTSCommand.OPTION_CFG, CTSCommand.OPTION_PACKAGE, CTSCommand.OPTION_PLAN,
            CTSCommand.OPTION_DEVICE, CTSCommand.OPTION_RESULT, CTSCommand.OPTION_E,
            CTSCommand.OPTION_SESSION, CTSCommand.OPTION_TEST, CTSCommand.OPTION_DERIVED_PLAN));
    private static HashMap<String, String> sOptionMap = new HashMap<String, String>();
    static {
        final String[] keys = new String[] {
                CTSCommand.OPTION_CFG,
                CTSCommand.OPTION_P,
                CTSCommand.OPTION_PACKAGE,
                CTSCommand.OPTION_PLAN,
                CTSCommand.OPTION_D,
                CTSCommand.OPTION_DEVICE,
                CTSCommand.OPTION_R,
                CTSCommand.OPTION_RESULT,
                CTSCommand.OPTION_E,
                CTSCommand.OPTION_S,
                CTSCommand.OPTION_SESSION,
                CTSCommand.OPTION_T,
                CTSCommand.OPTION_TEST,
                CTSCommand.OPTION_DERIVED_PLAN};
        final String[] values = new String[] {
                CTSCommand.OPTION_CFG,
                CTSCommand.OPTION_PACKAGE,
                CTSCommand.OPTION_PACKAGE,
                CTSCommand.OPTION_PLAN,
                CTSCommand.OPTION_DEVICE,
                CTSCommand.OPTION_DEVICE,
                CTSCommand.OPTION_RESULT,
                CTSCommand.OPTION_RESULT,
                CTSCommand.OPTION_E,
                CTSCommand.OPTION_SESSION,
                CTSCommand.OPTION_SESSION,
                CTSCommand.OPTION_TEST,
                CTSCommand.OPTION_TEST,
                CTSCommand.OPTION_DERIVED_PLAN};
        for (int i = 0; i < keys.length; i++) {
            sOptionMap.put(keys[i], values[i]);
        }
    }
    public static CommandParser parse(final String line)
            throws UnknownCommandException, CommandNotFoundException {
        ArrayList<String> arglist = new ArrayList<String>();
        Pattern p = Pattern.compile(COMMAND_PARSE_EXPRESSION);
        Matcher m = p.matcher(line);
        while (m.find()) {
            arglist.add(m.group(1));
        }
        CommandParser cp = new CommandParser();
        if (arglist.size() == 0) {
            throw new CommandNotFoundException("No command");
        }
        cp.parse(arglist);
        return cp;
    }
    private void parse(ArrayList<String> arglist)
            throws UnknownCommandException {
        mArgLength = arglist.size();
        int currentArgIndex = 0;
        mAction = arglist.get(currentArgIndex).toLowerCase();
        String originalOption = null;
        String option = null;
        while (++currentArgIndex < arglist.size()) {
            originalOption = arglist.get(currentArgIndex).trim();
            if (originalOption.startsWith("-")) {
                if (isNumber(originalOption)) {
                    mActionValues.add(originalOption);
                } else {
                    --currentArgIndex;
                    break;
                }
            } else {
                mActionValues.add(originalOption);
            }
        }
        while (++currentArgIndex < arglist.size()) {
            originalOption = arglist.get(currentArgIndex).trim().toLowerCase();
            option = originalOption;
            if (!option.startsWith("-")) {
                throw new UnknownCommandException(
                        "Option should start with '-'");
            }
            option = inputToOption(option);
            if (!sOptionsSet.contains(option)) {
                throw new UnknownCommandException("Unknown option :"
                        + originalOption);
            }
            if (mValues.containsKey(option)) {
                throw new UnknownCommandException("Duplicate option: "
                         + originalOption);
            }
            if (currentArgIndex + 1 == arglist.size()) {
                mValues.put(option, "");
                continue;
            }
            String value = arglist.get(++currentArgIndex).trim();
            if (value.startsWith("-")) {
                if (!isNumber(value)) {
                    value = "";
                    currentArgIndex--;
                }
            }
            mValues.put(option, value);
        }
    }
    private String inputToOption(String option) throws UnknownCommandException {
        String op = sOptionMap.get(option);
        if (op == null) {
            throw new UnknownCommandException("Unknow option " + option);
        }
        return op;
    }
    private boolean isNumber(String option) {
        try {
            Integer.parseInt(option);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public int getArgSize() {
        return mArgLength;
    }
    public String getAction() {
        return mAction;
    }
    public int getOptionSize() {
        return mValues.size();
    }
    public String getValue(String key) {
        if (mValues.containsKey(key)) {
            return mValues.get(key);
        } else {
            return null;
        }
    }
    public boolean containsKey(String key) {
        return mValues.containsKey(key);
    }
    public Set<String> getOptionKeys() {
        return mValues.keySet();
    }
    public ArrayList<String> getActionValues() {
        return mActionValues;
    }
    public void removeKey(String key) {
        mValues.remove(key);
    }
}
