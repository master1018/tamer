public class RestrictedState {
    private boolean mPsRestricted;
    private boolean mCsNormalRestricted;
    private boolean mCsEmergencyRestricted;
    public RestrictedState() {
        setPsRestricted(false);
        setCsNormalRestricted(false);
        setCsEmergencyRestricted(false);
    }
    public void setCsEmergencyRestricted(boolean csEmergencyRestricted) {
        mCsEmergencyRestricted = csEmergencyRestricted;
    }
    public boolean isCsEmergencyRestricted() {
        return mCsEmergencyRestricted;
    }
    public void setCsNormalRestricted(boolean csNormalRestricted) {
        mCsNormalRestricted = csNormalRestricted;
    }
    public boolean isCsNormalRestricted() {
        return mCsNormalRestricted;
    }
    public void setPsRestricted(boolean psRestricted) {
        mPsRestricted = psRestricted;
    }
    public boolean isPsRestricted() {
        return mPsRestricted;
    }
    public boolean isCsRestricted() {
        return mCsNormalRestricted && mCsEmergencyRestricted;
    }
    @Override
    public boolean equals (Object o) {
        RestrictedState s;
        try {
            s = (RestrictedState) o;
        } catch (ClassCastException ex) {
            return false;
        }
        if (o == null) {
            return false;
        }
        return mPsRestricted == s.mPsRestricted
        && mCsNormalRestricted == s.mCsNormalRestricted
        && mCsEmergencyRestricted == s.mCsEmergencyRestricted;
    }
    @Override
    public String toString() {
        String csString = "none";
        if (mCsEmergencyRestricted && mCsNormalRestricted) {
            csString = "all";
        } else if (mCsEmergencyRestricted && !mCsNormalRestricted) {
            csString = "emergency";
        } else if (!mCsEmergencyRestricted && mCsNormalRestricted) {
            csString = "normal call";
        }
        return  "Restricted State CS: " + csString + " PS:" + mPsRestricted;
    }
}
