public class NativeDaemonConnectorException extends RuntimeException
{
    private int mCode = -1;
    private String mCmd;
    public NativeDaemonConnectorException() {}
    public NativeDaemonConnectorException(String error)
    {
        super(error);
    }
    public NativeDaemonConnectorException(int code, String cmd, String error)
    {
        super(String.format("Cmd {%s} failed with code %d : {%s}", cmd, code, error));
        mCode = code;
        mCmd = cmd;
    }
    public int getCode() {
        return mCode;
    }
    public String getCmd() {
        return mCmd;
    }
}
