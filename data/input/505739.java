public class CommandHistory {
    private static final int CMD_RECORD_DEPTH = 50;
    private List<String> mCmdRecords;
    public CommandHistory() {
        mCmdRecords = new ArrayList<String>();
    }
    public boolean isHistoryCommand(final String cmd) {
        return CTSCommand.HISTORY.equals(cmd) || CTSCommand.H.equals(cmd);
    }
    public int size() {
        return mCmdRecords.size();
    }
    public String get(final int index) {
        return mCmdRecords.get(index);
    }
    public void show(final int cmdCount) {
        int cmdSize = mCmdRecords.size();
        int start = 0;
        if (cmdSize == 0) {
            CUIOutputStream.println("no history command list");
            return;
        }
        if (cmdCount < cmdSize) {
            start = cmdSize - cmdCount;
        }
        for (; start < cmdSize; start ++) {
            String cmdLine = mCmdRecords.get(start);
            CUIOutputStream.println("  " + Long.toString(start) + "\t" + cmdLine);
        }
    }
    public void addCommand(final CommandParser cp,
            final String cmdLine) {
        if ((cmdLine == null) || (cmdLine.length() == 0)) {
            return;
        }
        if (isValidCommand(cp.getAction()) && (!hasCommand(cmdLine))) {
            mCmdRecords.add(cmdLine);
            if (mCmdRecords.size() > CMD_RECORD_DEPTH) {
                mCmdRecords.remove(0);
            }
        }
    }
    private boolean isValidCommand(final String action) {
        if (!(CTSCommand.HISTORY.equals(action) || CTSCommand.H.equals(action))) {
            if (CTSCommand.ADD.equals(action)
                    || CTSCommand.EXIT.equals(action)
                    || CTSCommand.HELP.equals(action)
                    || CTSCommand.LIST.equals(action)
                    || CTSCommand.REMOVE.equals(action)
                    || CTSCommand.START.equals(action)) {
                return true;
            }
        }
        return false;
    }
    private boolean hasCommand(final String cmdLine) {
        for(String cmd : mCmdRecords) {
            if (cmd.equals(cmdLine)) {
                return true;
            }
        }
        return false;
    }
}
