public final class AndroidVersion implements Comparable<AndroidVersion> {
    private static final String PROP_API_LEVEL = "AndroidVersion.ApiLevel";  
    private static final String PROP_CODENAME = "AndroidVersion.CodeName";   
    private final int mApiLevel;
    private final String mCodename;
    public final static class AndroidVersionException extends Exception {
        private static final long serialVersionUID = 1L;
        AndroidVersionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public AndroidVersion(int apiLevel, String codename) {
        mApiLevel = apiLevel;
        mCodename = codename;
    }
    public AndroidVersion(Properties properties, int defaultApiLevel, String defaultCodeName) {
        if (properties == null) {
            mApiLevel = defaultApiLevel;
            mCodename = defaultCodeName;
        } else {
            mApiLevel = Integer.parseInt(properties.getProperty(PROP_API_LEVEL,
                    Integer.toString(defaultApiLevel)));
            mCodename = properties.getProperty(PROP_CODENAME, defaultCodeName);
        }
    }
    public AndroidVersion(Properties properties) throws AndroidVersionException {
        Exception error = null;
        String apiLevel = properties.getProperty(PROP_API_LEVEL, null );
        if (apiLevel != null) {
            try {
                mApiLevel = Integer.parseInt(apiLevel);
                mCodename = properties.getProperty(PROP_CODENAME, null );
                return;
            } catch (NumberFormatException e) {
                error = e;
            }
        }
        throw new AndroidVersionException(PROP_API_LEVEL + " not found!", error);
    }
    public void saveProperties(Properties props) {
        props.setProperty(PROP_API_LEVEL, Integer.toString(mApiLevel));
        if (mCodename != null) {
            props.setProperty(PROP_CODENAME, mCodename);
        }
    }
    public int getApiLevel() {
        return mApiLevel;
    }
    public String getCodename() {
        return mCodename;
    }
    public String getApiString() {
        if (mCodename != null) {
            return mCodename;
        }
        return Integer.toString(mApiLevel);
    }
    public boolean isPreview() {
        return mCodename != null;
    }
    public boolean canRun(AndroidVersion appVersion) {
        if (appVersion.mCodename != null) {
            return appVersion.mCodename.equals(mCodename);
        }
        return mApiLevel >= appVersion.mApiLevel;
    }
    public boolean equals(int apiLevel) {
        return mCodename == null && apiLevel == mApiLevel;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AndroidVersion) {
            AndroidVersion version = (AndroidVersion)obj;
            if (mCodename == null) {
                return version.mCodename == null &&
                        mApiLevel == version.mApiLevel;
            } else {
                return mCodename.equals(version.mCodename) &&
                        mApiLevel == version.mApiLevel;
            }
        } else if (obj instanceof String) {
            if (mCodename != null) {
                return mCodename.equals(obj);
            }
            try {
                int value = Integer.parseInt((String)obj);
                return value == mApiLevel;
            } catch (NumberFormatException e) {
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        if (mCodename != null) {
            return mCodename.hashCode();
        }
        return mApiLevel;
    }
    public int compareTo(AndroidVersion o) {
        return compareTo(o.mApiLevel, o.mCodename);
    }
    private int compareTo(int apiLevel, String codename) {
        if (mCodename == null) {
            if (codename == null) {
                return mApiLevel - apiLevel;
            } else {
                if (mApiLevel == apiLevel) {
                    return -1; 
                }
                return mApiLevel - apiLevel;
            }
        } else {
            if (mApiLevel == apiLevel) {
                if (codename == null) {
                    return +1;
                } else {
                    return mCodename.compareTo(codename);    
                }
            } else {
                return mApiLevel - apiLevel;
            }
        }
    }
    public boolean isGreaterOrEqualThan(int api) {
        return compareTo(api, null ) >= 0;
    }
}
