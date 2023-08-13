public class KeyboardListPreferenceJAJP extends ListPreference {
	public KeyboardListPreferenceJAJP(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public KeyboardListPreferenceJAJP(Context context) {
        this(context, null);
    }
    @Override protected void onDialogClosed(boolean positiveResult) {
    	super.onDialogClosed(positiveResult);
    	if (positiveResult) {
    		OpenWnnJAJP wnn = OpenWnnJAJP.getInstance();
        	int code = OpenWnnEvent.CHANGE_INPUT_VIEW;
        	OpenWnnEvent ev = new OpenWnnEvent(code);
        	try {
        		wnn.onEvent(ev);
        	} catch (Exception ex) {
        	}   		
    	}
    }
}
