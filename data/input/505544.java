public class SmsCirChannel extends CirChannel
        implements SmsListener, SmsSendFailureCallback {
    private String mAddr;
    private int mPort;
    private SmsService mSmsService;
    protected SmsCirChannel(ImpsConnection connection) {
        super(connection);
        ImpsConnectionConfig cfg = connection.getConfig();
        mAddr = cfg.getSmsCirAddr();
        mPort = cfg.getSmsCirPort();
    }
    @Override
    public void connect() throws ImException {
        mSmsService = SystemService.getDefault().getSmsService();
        if (mAddr != null) {
            mSmsService.addSmsListener(mAddr, mPort, this);
            sendHelo();
        } else {
            mSmsService.addSmsListener(SmsService.ANY_ADDRESS, mPort, this);
        }
    }
    public boolean isShutdown() {
        return false;
    }
    @Override
    public void shutdown() {
        mSmsService.removeSmsListener(this);
    }
    public void onIncomingSms(byte[] data) {
        int lengthSeptets = data.length * 8 / 7;
        String s = GsmAlphabet.gsm7BitPackedToString(data, 0,
                lengthSeptets, 0);
        if (!s.startsWith("WVCI")) {
            Log.w("SmsCir", "Received a non-CIR SMS, ignore!");
            return;
        }
        String sessionCookie = mConnection.getSession().getCookie();
        String[] fields = s.split(" ");
        if (fields.length != 3 || !sessionCookie.equalsIgnoreCase(fields[2])) {
            Log.w("SmsCir", "The CIR format is not correct or session cookie" +
                    " does not match");
        }
        mConnection.sendPollingRequest();
    }
    public void onFailure(int errorCode) {
        mConnection.shutdownOnError(new ImErrorInfo(ImpsErrorInfo.NETWORK_ERROR,
                "Could not establish SMS CIR channel"));
    }
    private void sendHelo() {
        String data = "HELO " + mConnection.getSession().getID();
        try {
            byte[] bytes = GsmAlphabet.stringToGsm7BitPacked(data);
            mSmsService.sendSms(mAddr, mPort, bytes, this);
        } catch (EncodeException ignore) {
        }
    }
}
