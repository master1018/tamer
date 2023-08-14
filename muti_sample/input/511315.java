public abstract class Connection {
    public static int PRESENTATION_ALLOWED = 1;    
    public static int PRESENTATION_RESTRICTED = 2; 
    public static int PRESENTATION_UNKNOWN = 3;    
    public static int PRESENTATION_PAYPHONE = 4;   
    private static String LOG_TAG = "TelephonyConnection";
    public enum DisconnectCause {
        NOT_DISCONNECTED,               
        INCOMING_MISSED,                
        NORMAL,                         
        LOCAL,                          
        BUSY,                           
        CONGESTION,                     
        MMI,                            
        INVALID_NUMBER,                 
        LOST_SIGNAL,
        LIMIT_EXCEEDED,                 
        INCOMING_REJECTED,              
        POWER_OFF,                      
        OUT_OF_SERVICE,                 
        ICC_ERROR,                      
        CALL_BARRED,                    
        FDN_BLOCKED,                    
        CS_RESTRICTED,                  
        CS_RESTRICTED_NORMAL,           
        CS_RESTRICTED_EMERGENCY,        
        CDMA_LOCKED_UNTIL_POWER_CYCLE,  
        CDMA_DROP,
        CDMA_INTERCEPT,                 
        CDMA_REORDER,                   
        CDMA_SO_REJECT,                 
        CDMA_RETRY_ORDER,               
        CDMA_ACCESS_FAILURE,
        CDMA_PREEMPTED,
        CDMA_NOT_EMERGENCY,              
        CDMA_ACCESS_BLOCKED,            
        ERROR_UNSPECIFIED
    }
    Object userData;
    public abstract String getAddress();
    public String getCnapName() {
        return null;
    }
    public String getOrigDialString(){
        return null;
    }
    public int getCnapNamePresentation() {
       return 0;
    };
    public abstract Call getCall();
    public abstract long getCreateTime();
    public abstract long getConnectTime();
    public abstract long getDisconnectTime();
    public abstract long getDurationMillis();
    public abstract long getHoldDurationMillis();
    public abstract DisconnectCause getDisconnectCause();
    public abstract boolean isIncoming();
    public Call.State getState() {
        Call c;
        c = getCall();
        if (c == null) {
            return Call.State.IDLE;
        } else {
            return c.getState();
        }
    }
    public boolean
    isAlive() {
        return getState().isAlive();
    }
    public boolean
    isRinging() {
        return getState().isRinging();
    }
    public Object getUserData() {
        return userData;
    }
    public void setUserData(Object userdata) {
        this.userData = userdata;
    }
    public abstract void hangup() throws CallStateException;
    public abstract void separate() throws CallStateException;
    public enum PostDialState {
        NOT_STARTED,    
        STARTED,        
        WAIT,           
        WILD,           
        COMPLETE,       
        CANCELLED,       
        PAUSE           
    }
    public void clearUserData(){
        userData = null;
    }
    public abstract PostDialState getPostDialState();
    public abstract String getRemainingPostDialString();
    public abstract void proceedAfterWaitChar();
    public abstract void proceedAfterWildChar(String str);
    public abstract void cancelPostDial();
    public abstract int getNumberPresentation();
    public String toString() {
        StringBuilder str = new StringBuilder(128);
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            str.append("addr: " + getAddress())
                    .append(" pres.: " + getNumberPresentation())
                    .append(" dial: " + getOrigDialString())
                    .append(" postdial: " + getRemainingPostDialString())
                    .append(" cnap name: " + getCnapName())
                    .append("(" + getCnapNamePresentation() + ")");
        }
        str.append(" incoming: " + isIncoming())
                .append(" state: " + getState())
                .append(" post dial state: " + getPostDialState());
        return str.toString();
    }
}
