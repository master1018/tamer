class DoubleDigitManager {
    private final long timeoutInMillis;
    private final CallBack mCallBack;
    private Integer intermediateDigit;
    public DoubleDigitManager(long timeoutInMillis, CallBack callBack) {
        this.timeoutInMillis = timeoutInMillis;
        mCallBack = callBack;
    }
    public void reportDigit(int digit) {
        if (intermediateDigit == null) {
            intermediateDigit = digit;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (intermediateDigit != null) {
                        mCallBack.singleDigitFinal(intermediateDigit);
                        intermediateDigit = null;
                    }
                }
            }, timeoutInMillis);
            if (!mCallBack.singleDigitIntermediate(digit)) {
                intermediateDigit = null;
                mCallBack.singleDigitFinal(digit);
            }
        } else if (mCallBack.twoDigitsFinal(intermediateDigit, digit)) {
             intermediateDigit = null;
        }
    }
    static interface CallBack {
        boolean singleDigitIntermediate(int digit);
        void singleDigitFinal(int digit);
        boolean twoDigitsFinal(int digit1, int digit2);
    }
}
