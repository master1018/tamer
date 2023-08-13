public final class EriInfo {
    public static final int ROAMING_INDICATOR_ON    = 0;
    public static final int ROAMING_INDICATOR_OFF   = 1;
    public static final int ROAMING_INDICATOR_FLASH = 2;
    public static final int ROAMING_ICON_MODE_NORMAL    = 0;
    public static final int ROAMING_ICON_MODE_FLASH     = 1;
    public int mRoamingIndicator;
    public int mIconIndex;
    public int mIconMode;
    public String mEriText;
    public int mCallPromptId;
    public int mAlertId;
    public EriInfo (int roamingIndicator, int iconIndex, int iconMode, String eriText,
            int callPromptId, int alertId) {
        this.mRoamingIndicator = roamingIndicator;
        this.mIconIndex = iconIndex;
        this.mIconMode = iconMode;
        this.mEriText = eriText;
        this.mCallPromptId = callPromptId;
        this.mAlertId = alertId;
    }
}
