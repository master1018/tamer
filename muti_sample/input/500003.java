public class XPathTask extends Task {
    private Path mManifestFile;
    private String mProperty;
    private String mExpression;
    private String mDefault;
    public void setInput(Path manifestFile) {
        mManifestFile = manifestFile;
    }
    public void setOutput(String property) {
        mProperty = property;
    }
    public void setExpression(String expression) {
        mExpression = expression;
    }
    public void setDefault(String defaultValue) {
        mDefault = defaultValue;
    }
    @Override
    public void execute() throws BuildException {
        try {
            if (mManifestFile == null || mManifestFile.list().length == 0) {
                throw new BuildException("input attribute is missing!");
            }
            if (mProperty == null) {
                throw new BuildException("output attribute is missing!");
            }
            if (mExpression == null) {
                throw new BuildException("expression attribute is missing!");
            }
            XPath xpath = AndroidXPathFactory.newXPath();
            String file = mManifestFile.list()[0];
            String result = xpath.evaluate(mExpression, new InputSource(new FileInputStream(file)));
            if (result.length() == 0 && mDefault != null) {
                result = mDefault;
            }
            getProject().setProperty(mProperty, result);
        } catch (XPathExpressionException e) {
            throw new BuildException(e);
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        }
    }
}
