class ApiDescrHandler extends DefaultHandler {
    private ApiList mApiList;
    private PackageInfo mCurrentPackage = null;
    private ClassInfo mCurrentClass = null;
    private MethodInfo mCurrentMethod = null;
    public ApiDescrHandler(ApiList apiList) {
        mApiList = apiList;
    }
    public ApiList getApiList() {
        return mApiList;
    }
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
        if (qName.equals("package")) {
            mCurrentPackage = mApiList.getOrCreatePackage(
                    attributes.getValue("name"));
        } else if (qName.equals("class") || qName.equals("interface")) {
            mCurrentClass = mCurrentPackage.getOrCreateClass(
                    attributes.getValue("name"),
                    attributes.getValue("extends"),
                    attributes.getValue("static"));
        } else if (qName.equals("implements")) {
            mCurrentClass.addInterface(attributes.getValue("name"));
        } else if (qName.equals("method")) {
            mCurrentMethod = new MethodInfo(attributes.getValue("name"),
                attributes.getValue("return"));
        } else if (qName.equals("constructor")) {
            mCurrentMethod = new MethodInfo("<init>", "void");
            String staticClass = mCurrentClass.getStatic();
            if (staticClass == null) {
            } else if ("false".equals(staticClass)) {
                String className = mCurrentClass.getName();
                int dollarIndex = className.indexOf('$');
                if (dollarIndex >= 0) {
                    String outerClass = className.substring(0, dollarIndex);
                    mCurrentMethod.addParameter(mCurrentPackage.getName() +
                        "." + outerClass);
                }
            }
        } else if (qName.equals("field")) {
            FieldInfo fInfo = new FieldInfo(attributes.getValue("name"),
                    attributes.getValue("type"));
            mCurrentClass.addField(fInfo);
        } else if (qName.equals("parameter")) {
            mCurrentMethod.addParameter(attributes.getValue("type"));
        }
    }
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equals("method") || qName.equals("constructor")) {
            mCurrentClass.addMethod(mCurrentMethod);
            mCurrentMethod = null;
        } else if (qName.equals("class") || qName.equals("interface")) {
            mCurrentClass = null;
        } else if (qName.equals("package")) {
            mCurrentPackage = null;
        }
    }
}
