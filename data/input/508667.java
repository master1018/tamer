public class AtParser {
    private static final int TYPE_ACTION = 0;   
    private static final int TYPE_READ = 1;     
    private static final int TYPE_SET = 2;      
    private static final int TYPE_TEST = 3;     
    private HashMap<String, AtCommandHandler> mExtHandlers;
    private HashMap<Character, AtCommandHandler> mBasicHandlers;
    private String mLastInput;  
    public AtParser() {
        mBasicHandlers = new HashMap<Character, AtCommandHandler>();
        mExtHandlers = new HashMap<String, AtCommandHandler>();
        mLastInput = "";
    }
    public void register(Character command, AtCommandHandler handler) {
        mBasicHandlers.put(command, handler);
    }
    public void register(String command, AtCommandHandler handler) {
        mExtHandlers.put(command, handler);
    }
    static private String clean(String input) {
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                int j = input.indexOf('"', i + 1 );  
                if (j == -1) {  
                    out.append(input.substring(i, input.length()));
                    out.append('"');
                    break;
                }
                out.append(input.substring(i, j + 1));
                i = j;
            } else if (c != ' ') {
                out.append(Character.toUpperCase(c));
            }
        }
        return out.toString();
    }
    static private boolean isAtoZ(char c) {
        return (c >= 'A' && c <= 'Z');
    }
    static private int findChar(char ch, String input, int fromIndex) {
        for (int i = fromIndex; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                i = input.indexOf('"', i + 1);
                if (i == -1) {
                    return input.length();
                }
            } else if (c == ch) {
                return i;
            }
        }
        return input.length();
    }
    static private Object[] generateArgs(String input) {
        int i = 0;
        int j;
        ArrayList<Object> out = new ArrayList<Object>();
        while (i <= input.length()) {
            j = findChar(',', input, i);
            String arg = input.substring(i, j);
            try {
                out.add(new Integer(arg));
            } catch (NumberFormatException e) {
                out.add(arg);
            }
            i = j + 1; 
        }
        return out.toArray();
    }
    static private int findEndExtendedName(String input, int index) {
        for (int i = index; i < input.length(); i++) {
            char c = input.charAt(i);
            if (isAtoZ(c)) continue;
            if (c >= '0' && c <= '9') continue;
            switch (c) {
            case '!':
            case '%':
            case '-':
            case '.':
            case '/':
            case ':':
            case '_':
                continue;
            default:
                return i;
            }
        }
        return input.length();
    }
    public AtCommandResult process(String raw_input) {
        String input = clean(raw_input);
        if (input.regionMatches(0, "A/", 0, 2)) {
            input = new String(mLastInput);
        } else {
            mLastInput = new String(input);
        }
        if (input.equals("")) {
            return new AtCommandResult(AtCommandResult.UNSOLICITED);
        }
        if (!input.regionMatches(0, "AT", 0, 2)) {
            return new AtCommandResult(AtCommandResult.ERROR);
        }
        int index = 2;
        AtCommandResult result =
                new AtCommandResult(AtCommandResult.UNSOLICITED);
        while (index < input.length()) {
            char c = input.charAt(index);
            if (isAtoZ(c)) {
                String args = input.substring(index + 1);
                if (mBasicHandlers.containsKey((Character)c)) {
                    result.addResult(mBasicHandlers.get(
                            (Character)c).handleBasicCommand(args));
                    return result;
                } else {
                    result.addResult(
                            new AtCommandResult(AtCommandResult.ERROR));
                    return result;
                }
            }
            if (c == '+') {
                int i = findEndExtendedName(input, index + 1);
                String commandName = input.substring(index, i);
                if (!mExtHandlers.containsKey(commandName)) {
                    result.addResult(
                            new AtCommandResult(AtCommandResult.ERROR));
                    return result;
                }
                AtCommandHandler handler = mExtHandlers.get(commandName);
                int endIndex = findChar(';', input, index);
                int type;
                if (i >= endIndex) {
                    type = TYPE_ACTION;
                } else if (input.charAt(i) == '?') {
                    type = TYPE_READ;
                } else if (input.charAt(i) == '=') {
                    if (i + 1 < endIndex) {
                        if (input.charAt(i + 1) == '?') {
                            type = TYPE_TEST;
                        } else {
                            type = TYPE_SET;
                        }
                    } else {
                        type = TYPE_SET;
                    }
                } else {
                    type = TYPE_ACTION;
                }
                switch (type) {
                case TYPE_ACTION:
                    result.addResult(handler.handleActionCommand());
                    break;
                case TYPE_READ:
                    result.addResult(handler.handleReadCommand());
                    break;
                case TYPE_TEST:
                    result.addResult(handler.handleTestCommand());
                    break;
                case TYPE_SET:
                    Object[] args =
                            generateArgs(input.substring(i + 1, endIndex));
                    result.addResult(handler.handleSetCommand(args));
                    break;
                }
                if (result.getResultCode() != AtCommandResult.OK) {
                    return result;   
                }
                index = endIndex;
            } else {
                index++;
            }
        }
        return result;
    }
}
