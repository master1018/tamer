public class XmlErrorHandler extends DefaultHandler {
    private IFile mFile;
    private XmlErrorListener mErrorListener;
    public interface XmlErrorListener {
        public void errorFound();
    }
    public static class BasicXmlErrorListener implements XmlErrorListener {
        public boolean mHasXmlError = false;
        public void errorFound() {
            mHasXmlError = true;
        }
    }
    public XmlErrorHandler(IFile file, XmlErrorListener errorListener) {
        mFile = file;
        mErrorListener = errorListener;
    }
    @Override
    public void error(SAXParseException exception) throws SAXException {
        handleError(exception, exception.getLineNumber());
    }
    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        handleError(exception, exception.getLineNumber());
    }
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        if (mFile != null) {
            BaseProjectHelper.markResource(mFile,
                    AndroidConstants.MARKER_XML,
                    exception.getMessage(),
                    exception.getLineNumber(),
                    IMarker.SEVERITY_WARNING);
        }
    }
    protected final IFile getFile() {
        return mFile;
    }
    protected void handleError(Exception exception, int lineNumber) {
        if (mErrorListener != null) {
            mErrorListener.errorFound();
        }
        String message = exception.getMessage();
        if (message == null) {
            message = "Unknown error " + exception.getClass().getCanonicalName();
        }
        if (mFile != null) {
            BaseProjectHelper.markResource(mFile,
                    AndroidConstants.MARKER_XML,
                    message,
                    lineNumber,
                    IMarker.SEVERITY_ERROR);
        }
    }
}
