public class PhoneSubInfoProxy extends IPhoneSubInfo.Stub {
    private PhoneSubInfo mPhoneSubInfo;
    public PhoneSubInfoProxy(PhoneSubInfo phoneSubInfo) {
        mPhoneSubInfo = phoneSubInfo;
        if(ServiceManager.getService("iphonesubinfo") == null) {
            ServiceManager.addService("iphonesubinfo", this);
        }
    }
    public void setmPhoneSubInfo(PhoneSubInfo phoneSubInfo) {
        this.mPhoneSubInfo = phoneSubInfo;
    }
    public String getDeviceId() {
        return mPhoneSubInfo.getDeviceId();
    }
    public String getDeviceSvn() {
        return mPhoneSubInfo.getDeviceSvn();
    }
    public String getSubscriberId() {
        return mPhoneSubInfo.getSubscriberId();
    }
    public String getIccSerialNumber() {
        return mPhoneSubInfo.getIccSerialNumber();
    }
    public String getLine1Number() {
        return mPhoneSubInfo.getLine1Number();
    }
    public String getLine1AlphaTag() {
        return mPhoneSubInfo.getLine1AlphaTag();
    }
    public String getVoiceMailNumber() {
        return mPhoneSubInfo.getVoiceMailNumber();
    }
    public String getCompleteVoiceMailNumber() {
        return mPhoneSubInfo.getCompleteVoiceMailNumber();
    }
    public String getVoiceMailAlphaTag() {
        return mPhoneSubInfo.getVoiceMailAlphaTag();
    }
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        mPhoneSubInfo.dump(fd, pw, args);
    }
}
