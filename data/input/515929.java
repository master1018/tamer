public class RetryManager {
    static public final String LOG_TAG = "RetryManager";
    static public final boolean DBG = false;
    static public final int RETRYIES_NOT_STARTED = 0;
    static public final int RETRYIES_ON_GOING = 1;
    static public final int RETRYIES_COMPLETED = 2;
    private static class RetryRec {
        RetryRec(int delayTime, int randomizationTime) {
            mDelayTime = delayTime;
            mRandomizationTime = randomizationTime;
        }
        int mDelayTime;
        int mRandomizationTime;
    }
    private ArrayList<RetryRec> mRetryArray = new ArrayList<RetryRec>();
    private boolean mRetryForever;
    private int mMaxRetryCount;
    private int mRetryCount;
    private Random rng = new Random();
    public RetryManager() {
        if (DBG) log("constructor");
    }
    public boolean configure(int maxRetryCount, int retryTime, int randomizationTime) {
        Pair<Boolean, Integer> value;
        if (DBG) log("configure: " + maxRetryCount + ", " + retryTime + "," + randomizationTime);
        if (!validateNonNegativeInt("maxRetryCount", maxRetryCount)) {
            return false;
        }
        if (!validateNonNegativeInt("retryTime", retryTime)) {
            return false;
        }
        if (!validateNonNegativeInt("randomizationTime", randomizationTime)) {
            return false;
        }
        mMaxRetryCount = maxRetryCount;
        resetRetryCount();
        mRetryArray.clear();
        mRetryArray.add(new RetryRec(retryTime, randomizationTime));
        return true;
    }
    public boolean configure(String configStr) {
        if ((configStr.startsWith("\"") && configStr.endsWith("\""))) {
            configStr = configStr.substring(1, configStr.length()-1);
        }
        if (DBG) log("configure: '" + configStr + "'");
        if (!TextUtils.isEmpty(configStr)) {
            int defaultRandomization = 0;
            if (DBG) log("configure: not empty");
            mMaxRetryCount = 0;
            resetRetryCount();
            mRetryArray.clear();
            String strArray[] = configStr.split(",");
            for (int i = 0; i < strArray.length; i++) {
                if (DBG) log("configure: strArray[" + i + "]='" + strArray[i] + "'");
                Pair<Boolean, Integer> value;
                String splitStr[] = strArray[i].split("=", 2);
                splitStr[0] = splitStr[0].trim();
                if (DBG) log("configure: splitStr[0]='" + splitStr[0] + "'");
                if (splitStr.length > 1) {
                    splitStr[1] = splitStr[1].trim();
                    if (DBG) log("configure: splitStr[1]='" + splitStr[1] + "'");
                    if (TextUtils.equals(splitStr[0], "default_randomization")) {
                        value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                        if (!value.first) return false;
                        defaultRandomization = value.second;
                    } else if (TextUtils.equals(splitStr[0], "max_retries")) {
                        if (TextUtils.equals("infinite",splitStr[1])) {
                            mRetryForever = true;
                        } else {
                            value = parseNonNegativeInt(splitStr[0], splitStr[1]);
                            if (!value.first) return false;
                            mMaxRetryCount = value.second;
                        }
                    } else {
                        Log.e(LOG_TAG, "Unrecognized configuration name value pair: "
                                        + strArray[i]);
                        return false;
                    }
                } else {
                    splitStr = strArray[i].split(":", 2);
                    splitStr[0] = splitStr[0].trim();
                    RetryRec rr = new RetryRec(0, 0);
                    value = parseNonNegativeInt("delayTime", splitStr[0]);
                    if (!value.first) return false;
                    rr.mDelayTime = value.second;
                    if (splitStr.length > 1) {
                        splitStr[1] = splitStr[1].trim();
                        if (DBG) log("configure: splitStr[1]='" + splitStr[1] + "'");
                        value = parseNonNegativeInt("randomizationTime", splitStr[1]);
                        if (!value.first) return false;
                        rr.mRandomizationTime = value.second;
                    } else {
                        rr.mRandomizationTime = defaultRandomization;
                    }
                    mRetryArray.add(rr);
                }
            }
            if (mRetryArray.size() > mMaxRetryCount) {
                mMaxRetryCount = mRetryArray.size();
                if (DBG) log("configure: setting mMaxRetryCount=" + mMaxRetryCount);
            }
            if (DBG) log("configure: true");
            return true;
        } else {
            if (DBG) log("configure: false it's empty");
            return false;
        }
    }
    public boolean isRetryNeeded() {
        boolean retVal = mRetryForever || (mRetryCount < mMaxRetryCount);
        if (DBG) log("isRetryNeeded: " + retVal);
        return retVal;
    }
    public int getRetryTimer() {
        int index;
        if (mRetryCount < mRetryArray.size()) {
            index = mRetryCount;
        } else {
            index = mRetryArray.size() - 1;
        }
        int retVal;
        if ((index >= 0) && (index < mRetryArray.size())) {
            retVal = mRetryArray.get(index).mDelayTime + nextRandomizationTime(index);
        } else {
            retVal = 0;
        }
        if (DBG) log("getRetryTimer: " + retVal);
        return retVal;
    }
    public int getRetryCount() {
        if (DBG) log("getRetryCount: " + mRetryCount);
        return mRetryCount;
    }
    public void increaseRetryCount() {
        mRetryCount++;
        if (mRetryCount > mMaxRetryCount) {
            mRetryCount = mMaxRetryCount;
        }
        if (DBG) log("increseRetryCount: " + mRetryCount);
    }
    public void setRetryCount(int count) {
        mRetryCount = count;
        if (mRetryCount > mMaxRetryCount) {
            mRetryCount = mMaxRetryCount;
        }
        if (mRetryCount < 0) {
            mRetryCount = 0;
        }
        mRetryForever = false;
        if (DBG) log("setRetryCount: " + mRetryCount);
    }
    public void resetRetryCount() {
        mRetryCount = 0;
        mRetryForever = false;
        if (DBG) log("resetRetryCount: " + mRetryCount);
    }
    public void retryForeverUsingLastTimeout() {
        mRetryCount = mMaxRetryCount;
        mRetryForever = true;
        if (DBG) log("retryForeverUsingLastTimeout: " + mRetryForever + ", " + mRetryCount);
    }
    public boolean isRetryForever() {
        if (DBG) log("isRetryForever: " + mRetryForever);
        return mRetryForever;
    }
    private Pair<Boolean, Integer> parseNonNegativeInt(String name, String stringValue) {
        int value;
        Pair<Boolean, Integer> retVal;
        try {
            value = Integer.parseInt(stringValue);
            retVal = new Pair<Boolean, Integer>(validateNonNegativeInt(name, value), value);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, name + " bad value: " + stringValue, e);
            retVal = new Pair<Boolean, Integer>(false, 0);
        }
        if (DBG) log("parseNonNetativeInt: " + name + ", " + stringValue + ", "
                    + retVal.first + ", " + retVal.second);
        return retVal;
    }
    private boolean validateNonNegativeInt(String name, int value) {
        boolean retVal;
        if (value < 0) {
            Log.e(LOG_TAG, name + " bad value: is < 0");
            retVal = false;
        } else {
            retVal = true;
        }
        if (DBG) log("validateNonNegative: " + name + ", " + value + ", " + retVal);
        return retVal;
    }
    private int nextRandomizationTime(int index) {
        int randomTime = mRetryArray.get(index).mRandomizationTime;
        if (randomTime == 0) {
            return 0;
        } else {
            return rng.nextInt(randomTime);
        }
    }
    private void log(String s) {
        Log.d(LOG_TAG, s);
    }
}
