public class ConversationListTests 
        extends ActivityInstrumentationTestCase2<ConversationList> {
    private Context mContext;
    public ConversationListTests() {
        super("com.android.mms", ConversationList.class);
    }
    @Override
    protected void setUp() throws Exception {
    	super.setUp();
    	mContext = getInstrumentation().getTargetContext();
    }
}
