public class ProfileData {
    protected MethodData mElement;
    protected MethodData mContext;
    protected boolean mElementIsParent;
    protected long mElapsedInclusive;
    protected int mNumCalls;
    public ProfileData() {
    }
    public ProfileData(MethodData context, MethodData element,
            boolean elementIsParent) {
        mContext = context;
        mElement = element;
        mElementIsParent = elementIsParent;
    }
    public String getProfileName() {
        return mElement.getProfileName();
    }
    public MethodData getMethodData() {
        return mElement;
    }
    public void addElapsedInclusive(long elapsedInclusive) {
        mElapsedInclusive += elapsedInclusive;
        mNumCalls += 1;
    }
    public void setElapsedInclusive(long elapsedInclusive) {
        mElapsedInclusive = elapsedInclusive;
    }
    public long getElapsedInclusive() {
        return mElapsedInclusive;
    }
    public void setNumCalls(int numCalls) {
        mNumCalls = numCalls;
    }
    public String getNumCalls() {
        int totalCalls;
        if (mElementIsParent)
            totalCalls = mContext.getTotalCalls();
        else
            totalCalls = mElement.getTotalCalls();
        return String.format("%d/%d", mNumCalls, totalCalls);
    }
    public boolean isParent() {
        return mElementIsParent;
    }
    public MethodData getContext() {
        return mContext;
    }
}
