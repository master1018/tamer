public class JsPromptResult extends JsResult {
    private String mStringResult;
    public void confirm(String result) {
        mStringResult = result;
        confirm();
    }
     JsPromptResult(CallbackProxy proxy) {
        super(proxy,  false);
    }
     String getStringResult() {
        return mStringResult;
    }
    @Override
     void handleDefault() {
        mStringResult = null;
        super.handleDefault();
    }
}
