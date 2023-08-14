public class UserDictionaryToolsListJAJP extends UserDictionaryToolsList {
    public UserDictionaryToolsListJAJP() {
        mListViewName = "jp.co.omronsoft.openwnn.JAJP.UserDictionaryToolsListJAJP";
        mEditViewName = "jp.co.omronsoft.openwnn.JAJP.UserDictionaryToolsEditJAJP";
        mPackageName  = "jp.co.omronsoft.openwnn";
    }
    @Override protected void headerCreate() {
      getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
    		  R.layout.user_dictionary_tools_list_header_ja);
    }
    @Override protected UserDictionaryToolsEdit createUserDictionaryToolsEdit(View view1, View view2) {
        return new UserDictionaryToolsEditJAJP(view1, view2);
    }
    @Override protected boolean sendEventToIME(OpenWnnEvent ev) {
        try {
            return OpenWnnJAJP.getInstance().onEvent(ev);
        } catch (Exception ex) {
        }
        return false;
    }
    @Override protected Comparator<WnnWord> getComparator() {
    	return new ListComparatorJAJP();
    }
    protected class ListComparatorJAJP implements Comparator<WnnWord>{
        public int compare(WnnWord word1, WnnWord word2) {
            return word1.stroke.compareTo(word2.stroke);
        };
    }
}
