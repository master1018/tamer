public class MonkeySourceRandomScript implements MonkeyEventSource {
    private int mVerbose = 0;
    private MonkeySourceScript mSetupSource = null;
    private ArrayList<MonkeySourceScript> mScriptSources = new ArrayList<MonkeySourceScript>();
    private MonkeySourceScript mCurrentSource = null;
    private Random mRandom;
    public MonkeySourceRandomScript(String setupFileName, ArrayList<String> scriptFileNames,
            long throttle, boolean randomizeThrottle, Random random) {
        if (setupFileName != null) {
            mSetupSource = new MonkeySourceScript(random, setupFileName, throttle,
                    randomizeThrottle);
            mCurrentSource = mSetupSource;
        }
        for (String fileName: scriptFileNames) {
            mScriptSources.add(new MonkeySourceScript(random, fileName, throttle,
                      randomizeThrottle));
        }
        mRandom = random;
    }
    public MonkeySourceRandomScript(ArrayList<String> scriptFileNames, long throttle,
            boolean randomizeThrottle, Random random) {
        this(null, scriptFileNames, throttle, randomizeThrottle, random);
    }
    public MonkeyEvent getNextEvent() {
        if (mCurrentSource == null) {
            int numSources = mScriptSources.size();
            if (numSources == 1) {
                mCurrentSource = mScriptSources.get(0);
            } else if (numSources > 1) {
                mCurrentSource = mScriptSources.get(mRandom.nextInt(numSources));
            }
        }
        if (mCurrentSource != null) {
            MonkeyEvent nextEvent = mCurrentSource.getNextEvent();
            if (nextEvent == null) {
                mCurrentSource = null;
            }
            return nextEvent;
        }
        return null;
    }
    public void setVerbose(int verbose) {
        mVerbose = verbose;
        if (mSetupSource != null) {
            mSetupSource.setVerbose(verbose);
        }
        for (MonkeySourceScript source: mScriptSources) {
            source.setVerbose(verbose);
        }
    }
    public boolean validate() {
        if (mSetupSource != null && !mSetupSource.validate()) {
            return false;
        }
        for (MonkeySourceScript source: mScriptSources) {
            if (!source.validate()) {
                return false;
            }
        }
        return true;
    }
}
