public class ResultException extends StkException {
    private ResultCode mResult;
    private int mAdditionalInfo;
    public ResultException(ResultCode result) {
        super();
        switch (result) {
            case TERMINAL_CRNTLY_UNABLE_TO_PROCESS:    
            case NETWORK_CRNTLY_UNABLE_TO_PROCESS:     
            case LAUNCH_BROWSER_ERROR:                 
            case MULTI_CARDS_CMD_ERROR:                
            case USIM_CALL_CONTROL_PERMANENT:          
            case BIP_ERROR:                            
            case FRAMES_ERROR:                         
            case MMS_ERROR:                            
                throw new AssertionError(
                        "For result code, " + result +
                        ", additional information must be given!");
        }
        mResult = result;
        mAdditionalInfo = -1;
    }
    public ResultException(ResultCode result, int additionalInfo) {
        super();
        if (additionalInfo < 0) {
            throw new AssertionError(
                    "Additional info must be greater than zero!");
        }
        mResult = result;
        mAdditionalInfo = additionalInfo;
    }
    public ResultCode result() {
        return mResult;
    }
    public boolean hasAdditionalInfo() {
        return mAdditionalInfo >= 0;
    }
    public int additionalInfo() {
        return mAdditionalInfo;
    }
}
