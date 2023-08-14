public final class PrimitiveContentHandler extends DefaultHandler {
    private Primitive mPrimitive;
    private String mCurTagName;
    private boolean mIsTransContent;
    private Stack<PrimitiveElement> mContentElementsStack;
    public PrimitiveContentHandler() {
        mPrimitive = new Primitive();
        mContentElementsStack = new Stack<PrimitiveElement>();
    }
    public void reset() {
        mPrimitive = new Primitive();
        mContentElementsStack.clear();
        mIsTransContent = false;
    }
    public Primitive getPrimitive() {
        return mPrimitive;
    }
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
        if (mIsTransContent) {
            if (mContentElementsStack.empty()) {
                mPrimitive.setContentElement(localName);
                mContentElementsStack.push(mPrimitive.getContentElement());
            } else {
                PrimitiveElement parentPrimitive = mContentElementsStack.peek();
                PrimitiveElement childPrimitive = new PrimitiveElement(
                        localName);
                parentPrimitive.addChild(childPrimitive);
                mContentElementsStack.push(childPrimitive);
            }
        } else {
            if (ImpsTags.TransactionContent.equals(localName)) {
                mIsTransContent = true;
            }
        }
        mCurTagName = localName;
    }
    @Override
    public void endElement(String uri, String localName, String qName) {
        if (ImpsTags.TransactionContent.equals(localName)) {
            mIsTransContent = false;
        }
        if (mIsTransContent) {
            if (!mContentElementsStack.empty()) {
                mContentElementsStack.pop();
            }
        }
        mCurTagName = null;
    }
    @Override
    public void characters(char[] ch, int start, int length) {
        String contentStr = ImpsUtils.trim(new String(ch, start, length));
        if (contentStr == null || contentStr.length() == 0) {
            return;
        }
        if (mIsTransContent) {
            if (!ImpsTags.TransactionContent.equals(mCurTagName)) {
                PrimitiveElement curPrimitive = mContentElementsStack.peek();
                curPrimitive.setContents(contentStr);
            }
        } else {
            if (ImpsTags.TransactionID.equals(mCurTagName)) {
                mPrimitive.setTransactionId(contentStr);
            } else if (ImpsTags.TransactionMode.equals(mCurTagName)) {
                mPrimitive.setTransactionMode(TransactionMode.valueOf(contentStr));
            } else if (ImpsTags.SessionID.equals(mCurTagName)) {
                mPrimitive.setSession(contentStr);
            } else if (ImpsTags.Poll.equals(mCurTagName)) {
                mPrimitive.setPoll(contentStr);
            } else if (ImpsTags.CIR.equals(mCurTagName)) {
                mPrimitive.setCir(contentStr);
            }
        }
    }
}
