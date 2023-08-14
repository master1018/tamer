public final class FolderConfiguration implements Comparable<FolderConfiguration> {
    public final static String QUALIFIER_SEP = "-"; 
    private final ResourceQualifier[] mQualifiers = new ResourceQualifier[INDEX_COUNT];
    private final static int INDEX_COUNTRY_CODE       = 0;
    private final static int INDEX_NETWORK_CODE       = 1;
    private final static int INDEX_LANGUAGE           = 2;
    private final static int INDEX_REGION             = 3;
    private final static int INDEX_SCREEN_SIZE        = 4;
    private final static int INDEX_SCREEN_RATIO       = 5;
    private final static int INDEX_SCREEN_ORIENTATION = 6;
    private final static int INDEX_PIXEL_DENSITY      = 7;
    private final static int INDEX_TOUCH_TYPE         = 8;
    private final static int INDEX_KEYBOARD_STATE     = 9;
    private final static int INDEX_TEXT_INPUT_METHOD  = 10;
    private final static int INDEX_NAVIGATION_METHOD  = 11;
    private final static int INDEX_SCREEN_DIMENSION   = 12;
    private final static int INDEX_VERSION            = 13;
    private final static int INDEX_COUNT              = 14;
    public static int getQualifierCount() {
        return INDEX_COUNT;
    }
    public void set(FolderConfiguration config) {
        if (config != null) {
            for (int i = 0 ; i < INDEX_COUNT ; i++) {
                mQualifiers[i] = config.mQualifiers[i];
            }
        }
    }
    public void substract(FolderConfiguration config) {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (config.mQualifiers[i] != null && config.mQualifiers[i].isValid()) {
                mQualifiers[i] = null;
            }
        }
    }
    public ResourceQualifier getInvalidQualifier() {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null && mQualifiers[i].isValid() == false) {
                return mQualifiers[i];
            }
        }
        return null;
    }
    public boolean checkRegion() {
        if (mQualifiers[INDEX_LANGUAGE] == null && mQualifiers[INDEX_REGION] != null) {
            return false;
        }
        return true;
    }
    public void addQualifier(ResourceQualifier qualifier) {
        if (qualifier instanceof CountryCodeQualifier) {
            mQualifiers[INDEX_COUNTRY_CODE] = qualifier;
        } else if (qualifier instanceof NetworkCodeQualifier) {
            mQualifiers[INDEX_NETWORK_CODE] = qualifier;
        } else if (qualifier instanceof LanguageQualifier) {
            mQualifiers[INDEX_LANGUAGE] = qualifier;
        } else if (qualifier instanceof RegionQualifier) {
            mQualifiers[INDEX_REGION] = qualifier;
        } else if (qualifier instanceof ScreenSizeQualifier) {
            mQualifiers[INDEX_SCREEN_SIZE] = qualifier;
        } else if (qualifier instanceof ScreenRatioQualifier) {
            mQualifiers[INDEX_SCREEN_RATIO] = qualifier;
        } else if (qualifier instanceof ScreenOrientationQualifier) {
            mQualifiers[INDEX_SCREEN_ORIENTATION] = qualifier;
        } else if (qualifier instanceof PixelDensityQualifier) {
            mQualifiers[INDEX_PIXEL_DENSITY] = qualifier;
        } else if (qualifier instanceof TouchScreenQualifier) {
            mQualifiers[INDEX_TOUCH_TYPE] = qualifier;
        } else if (qualifier instanceof KeyboardStateQualifier) {
            mQualifiers[INDEX_KEYBOARD_STATE] = qualifier;
        } else if (qualifier instanceof TextInputMethodQualifier) {
            mQualifiers[INDEX_TEXT_INPUT_METHOD] = qualifier;
        } else if (qualifier instanceof NavigationMethodQualifier) {
            mQualifiers[INDEX_NAVIGATION_METHOD] = qualifier;
        } else if (qualifier instanceof ScreenDimensionQualifier) {
            mQualifiers[INDEX_SCREEN_DIMENSION] = qualifier;
        } else if (qualifier instanceof VersionQualifier) {
            mQualifiers[INDEX_VERSION] = qualifier;
        }
    }
    public void removeQualifier(ResourceQualifier qualifier) {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] == qualifier) {
                mQualifiers[i] = null;
                return;
            }
        }
    }
    public ResourceQualifier getQualifier(int index) {
        return mQualifiers[index];
    }
    public void setCountryCodeQualifier(CountryCodeQualifier qualifier) {
        mQualifiers[INDEX_COUNTRY_CODE] = qualifier;
    }
    public CountryCodeQualifier getCountryCodeQualifier() {
        return (CountryCodeQualifier)mQualifiers[INDEX_COUNTRY_CODE];
    }
    public void setNetworkCodeQualifier(NetworkCodeQualifier qualifier) {
        mQualifiers[INDEX_NETWORK_CODE] = qualifier;
    }
    public NetworkCodeQualifier getNetworkCodeQualifier() {
        return (NetworkCodeQualifier)mQualifiers[INDEX_NETWORK_CODE];
    }
    public void setLanguageQualifier(LanguageQualifier qualifier) {
        mQualifiers[INDEX_LANGUAGE] = qualifier;
    }
    public LanguageQualifier getLanguageQualifier() {
        return (LanguageQualifier)mQualifiers[INDEX_LANGUAGE];
    }
    public void setRegionQualifier(RegionQualifier qualifier) {
        mQualifiers[INDEX_REGION] = qualifier;
    }
    public RegionQualifier getRegionQualifier() {
        return (RegionQualifier)mQualifiers[INDEX_REGION];
    }
    public void setScreenSizeQualifier(ScreenSizeQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_SIZE] = qualifier;
    }
    public ScreenSizeQualifier getScreenSizeQualifier() {
        return (ScreenSizeQualifier)mQualifiers[INDEX_SCREEN_SIZE];
    }
    public void setScreenRatioQualifier(ScreenRatioQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_RATIO] = qualifier;
    }
    public ScreenRatioQualifier getScreenRatioQualifier() {
        return (ScreenRatioQualifier)mQualifiers[INDEX_SCREEN_RATIO];
    }
    public void setScreenOrientationQualifier(ScreenOrientationQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_ORIENTATION] = qualifier;
    }
    public ScreenOrientationQualifier getScreenOrientationQualifier() {
        return (ScreenOrientationQualifier)mQualifiers[INDEX_SCREEN_ORIENTATION];
    }
    public void setPixelDensityQualifier(PixelDensityQualifier qualifier) {
        mQualifiers[INDEX_PIXEL_DENSITY] = qualifier;
    }
    public PixelDensityQualifier getPixelDensityQualifier() {
        return (PixelDensityQualifier)mQualifiers[INDEX_PIXEL_DENSITY];
    }
    public void setTouchTypeQualifier(TouchScreenQualifier qualifier) {
        mQualifiers[INDEX_TOUCH_TYPE] = qualifier;
    }
    public TouchScreenQualifier getTouchTypeQualifier() {
        return (TouchScreenQualifier)mQualifiers[INDEX_TOUCH_TYPE];
    }
    public void setKeyboardStateQualifier(KeyboardStateQualifier qualifier) {
        mQualifiers[INDEX_KEYBOARD_STATE] = qualifier;
    }
    public KeyboardStateQualifier getKeyboardStateQualifier() {
        return (KeyboardStateQualifier)mQualifiers[INDEX_KEYBOARD_STATE];
    }
    public void setTextInputMethodQualifier(TextInputMethodQualifier qualifier) {
        mQualifiers[INDEX_TEXT_INPUT_METHOD] = qualifier;
    }
    public TextInputMethodQualifier getTextInputMethodQualifier() {
        return (TextInputMethodQualifier)mQualifiers[INDEX_TEXT_INPUT_METHOD];
    }
    public void setNavigationMethodQualifier(NavigationMethodQualifier qualifier) {
        mQualifiers[INDEX_NAVIGATION_METHOD] = qualifier;
    }
    public NavigationMethodQualifier getNavigationMethodQualifier() {
        return (NavigationMethodQualifier)mQualifiers[INDEX_NAVIGATION_METHOD];
    }
    public void setScreenDimensionQualifier(ScreenDimensionQualifier qualifier) {
        mQualifiers[INDEX_SCREEN_DIMENSION] = qualifier;
    }
    public ScreenDimensionQualifier getScreenDimensionQualifier() {
        return (ScreenDimensionQualifier)mQualifiers[INDEX_SCREEN_DIMENSION];
    }
    public void setVersionQualifier(VersionQualifier qualifier) {
        mQualifiers[INDEX_VERSION] = qualifier;
    }
    public VersionQualifier getVersionQualifier() {
        return (VersionQualifier)mQualifiers[INDEX_VERSION];
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FolderConfiguration) {
            FolderConfiguration fc = (FolderConfiguration)obj;
            for (int i = 0 ; i < INDEX_COUNT ; i++) {
                ResourceQualifier qualifier = mQualifiers[i];
                ResourceQualifier fcQualifier = fc.mQualifiers[i];
                if (qualifier != null) {
                    if (qualifier.equals(fcQualifier) == false) {
                        return false;
                    }
                } else if (fcQualifier != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    public boolean isDefault() {
        for (ResourceQualifier irq : mQualifiers) {
            if (irq != null) {
                return false;
            }
        }
        return true;
    }
    public String getFolderName(ResourceFolderType folder, IAndroidTarget target) {
        StringBuilder result = new StringBuilder(folder.getName());
        for (ResourceQualifier qualifier : mQualifiers) {
            if (qualifier != null) {
                String segment = qualifier.getFolderSegment(target);
                if (segment != null && segment.length() > 0) {
                    result.append(QUALIFIER_SEP);
                    result.append(segment);
                }
            }
        }
        return result.toString();
    }
    public String getFolderName(ResourceFolderType folder, IProject project) {
        IAndroidTarget target = null;
        if (project != null) {
            Sdk currentSdk = Sdk.getCurrent();
            if (currentSdk != null) {
                target = currentSdk.getTarget(project);
            }
        }
        return getFolderName(folder, target);
    }
    @Override
    public String toString() {
        return toDisplayString();
    }
    public String toDisplayString() {
        if (isDefault()) {
            return "default";
        }
        StringBuilder result = null;
        int index = 0;
        ResourceQualifier qualifier = null;
        while (index < INDEX_LANGUAGE) {
            qualifier = mQualifiers[index++];
            if (qualifier != null) {
                if (result == null) {
                    result = new StringBuilder();
                } else {
                    result.append(", "); 
                }
                result.append(qualifier.getStringValue());
            }
        }
        if (mQualifiers[INDEX_LANGUAGE] != null && mQualifiers[INDEX_REGION] != null) {
            String language = mQualifiers[INDEX_LANGUAGE].getStringValue();
            String region = mQualifiers[INDEX_REGION].getStringValue();
            if (result == null) {
                result = new StringBuilder();
            } else {
                result.append(", "); 
            }
            result.append(String.format("%s_%s", language, region)); 
            index += 2;
        }
        while (index < INDEX_COUNT) {
            qualifier = mQualifiers[index++];
            if (qualifier != null) {
                if (result == null) {
                    result = new StringBuilder();
                } else {
                    result.append(", "); 
                }
                result.append(qualifier.getStringValue());
            }
        }
        return result == null ? null : result.toString();
    }
    public int compareTo(FolderConfiguration folderConfig) {
        if (isDefault()) {
            if (folderConfig.isDefault()) {
                return 0;
            }
            return -1;
        }
        for (int i = 0 ; i < INDEX_COUNT; i++) {
            ResourceQualifier qualifier1 = mQualifiers[i];
            ResourceQualifier qualifier2 = folderConfig.mQualifiers[i];
            if (qualifier1 == null) {
                if (qualifier2 == null) {
                    continue;
                } else {
                    return -1;
                }
            } else {
                if (qualifier2 == null) {
                    return 1;
                } else {
                    int result = qualifier1.compareTo(qualifier2);
                    if (result == 0) {
                        continue;
                    }
                    return result;
                }
            }
        }
        return 0;
    }
    public boolean isMatchFor(FolderConfiguration referenceConfig) {
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            ResourceQualifier testQualifier = mQualifiers[i];
            ResourceQualifier referenceQualifier = referenceConfig.mQualifiers[i];
            if (testQualifier != null && referenceQualifier != null &&
                        testQualifier.isMatchFor(referenceQualifier) == false) {
                return false;
            }
        }
        return true;
    }
    public int getHighestPriorityQualifier(int startIndex) {
        for (int i = startIndex ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null) {
                return i;
            }
        }
        return -1;
    }
    public void createDefault() {
        mQualifiers[INDEX_COUNTRY_CODE] = new CountryCodeQualifier();
        mQualifiers[INDEX_NETWORK_CODE] = new NetworkCodeQualifier();
        mQualifiers[INDEX_LANGUAGE] = new LanguageQualifier();
        mQualifiers[INDEX_REGION] = new RegionQualifier();
        mQualifiers[INDEX_SCREEN_SIZE] = new ScreenSizeQualifier();
        mQualifiers[INDEX_SCREEN_RATIO] = new ScreenRatioQualifier();
        mQualifiers[INDEX_SCREEN_ORIENTATION] = new ScreenOrientationQualifier();
        mQualifiers[INDEX_PIXEL_DENSITY] = new PixelDensityQualifier();
        mQualifiers[INDEX_TOUCH_TYPE] = new TouchScreenQualifier();
        mQualifiers[INDEX_KEYBOARD_STATE] = new KeyboardStateQualifier();
        mQualifiers[INDEX_TEXT_INPUT_METHOD] = new TextInputMethodQualifier();
        mQualifiers[INDEX_NAVIGATION_METHOD] = new NavigationMethodQualifier();
        mQualifiers[INDEX_SCREEN_DIMENSION] = new ScreenDimensionQualifier();
        mQualifiers[INDEX_VERSION] = new VersionQualifier();
    }
    public ResourceQualifier[] getQualifiers() {
        int count = 0;
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null) {
                count++;
            }
        }
        ResourceQualifier[] array = new ResourceQualifier[count];
        int index = 0;
        for (int i = 0 ; i < INDEX_COUNT ; i++) {
            if (mQualifiers[i] != null) {
                array[index++] = mQualifiers[i];
            }
        }
        return array;
    }
}
