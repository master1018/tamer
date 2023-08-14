public class UserDictionaryToolsListEN extends UserDictionaryToolsList {
    public UserDictionaryToolsListEN() {
		if (OpenWnnEN.getInstance() == null) {
			new OpenWnnEN(this);
		}
        mListViewName = "jp.co.omronsoft.openwnn.EN.UserDictionaryToolsListEN";
        mEditViewName = "jp.co.omronsoft.openwnn.EN.UserDictionaryToolsEditEN";
        mPackageName  = "jp.co.omronsoft.openwnn";
    }
    @Override protected void headerCreate() {
      getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
      R.layout.user_dictionary_tools_list_header);
    }
    @Override protected UserDictionaryToolsEdit createUserDictionaryToolsEdit(View view1, View view2) {
        return new UserDictionaryToolsEditEN(view1, view2);
    }
    @Override protected boolean sendEventToIME(OpenWnnEvent ev) {
        try {
            return OpenWnnEN.getInstance().onEvent(ev);
        } catch (Exception ex) {
        }
        return false;
    }
    @Override protected Comparator<WnnWord> getComparator() {
    	return new ListComparatorEN();
    }
    protected class ListComparatorEN implements Comparator<WnnWord>{
        public int compare(WnnWord word1, WnnWord word2) {
            return word1.stroke.compareTo(word2.stroke);
        };
    }
}
