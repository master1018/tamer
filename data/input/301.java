public final class JsCatalogResponseContract extends JavaScriptObject implements CatalogResponseContract {
    protected JsCatalogResponseContract() {
    }
    @Override
    public native String getError();
    @Override
    public native List<CatalogEntry> getResponse();
    public native JsArray<JavaScriptObject> getResponseAsJSOList();
    public native JsArrayBoolean getResponseAsBooleanList();
    @Override
    public List<String> getWarnings() {
        return JsArrayList.arrayAsListOfString(getWarningsArray());
    }
    public native JsArrayString getWarningsArray();
}
