public class UserDictionaryToolsEditJAJP extends UserDictionaryToolsEdit {
    public UserDictionaryToolsEditJAJP() {
        super();
        initialize();
    }
    public UserDictionaryToolsEditJAJP(View focusView, View focusPairView) {
        super(focusView, focusPairView);
        initialize();
    }
    public void initialize() {
        mListViewName = "jp.co.omronsoft.openwnn.JAJP.UserDictionaryToolsListJAJP";
        mPackageName  = "jp.co.omronsoft.openwnn";
    }
    @Override protected boolean sendEventToIME(OpenWnnEvent ev) {
        try {
            return OpenWnnJAJP.getInstance().onEvent(ev);
        } catch (Exception ex) {
        }
        return false;
    }
    @Override protected UserDictionaryToolsList createUserDictionaryToolsList() {
        return new UserDictionaryToolsListJAJP();
    }
}
