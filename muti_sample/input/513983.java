public class UserDictionaryToolsEditEN extends UserDictionaryToolsEdit {
    public UserDictionaryToolsEditEN() {
        super();
        initialize();
    }
    public UserDictionaryToolsEditEN(View focusView, View focusPairView) {
        super(focusView, focusPairView);
        initialize();
    }
    public void initialize() {
        mListViewName = "jp.co.omronsoft.openwnn.EN.UserDictionaryToolsListEN";
        mPackageName  = "jp.co.omronsoft.openwnn";
    }
    @Override protected boolean sendEventToIME(OpenWnnEvent ev) {
        try {
            return OpenWnnEN.getInstance().onEvent(ev);
        } catch (Exception ex) {
        }
        return false;
    }
    @Override protected UserDictionaryToolsList createUserDictionaryToolsList() {
        return new UserDictionaryToolsListEN();
    }
}
