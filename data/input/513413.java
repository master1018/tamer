class SignatureSourcer implements SignatureVisitor {
    private final StringBuilder mBuf = new StringBuilder();
    private final StringBuilder mFormalsBuf = new StringBuilder();
    private boolean mWritingFormals;
    private int mArgumentStack;
    private SignatureSourcer mReturnType;
    private SignatureSourcer mSuperClass;
    private ArrayList<SignatureSourcer> mParameters = new ArrayList<SignatureSourcer>();
    public SignatureSourcer() {
    }
    private StringBuilder getBuf() {
        if (mWritingFormals) {
            return mFormalsBuf;
        } else {
            return mBuf;
        }
    }
    @Override
    public String toString() {
        return mBuf.toString();
    }
    public SignatureSourcer getReturnType() {
        return mReturnType;
    }
    public ArrayList<SignatureSourcer> getParameters() {
        return mParameters;
    }
    public boolean hasFormalsContent() {
        return mFormalsBuf.length() > 0;
    }
    public String formalsToString() {
        return mFormalsBuf.toString();
    }
    public SignatureSourcer getSuperClass() {
        return mSuperClass;
    }
    public void visitFormalTypeParameter(final String name) {
        if (!mWritingFormals) {
            mWritingFormals = true;
            getBuf().append('<');
        } else {
            getBuf().append(", ");
        }
        getBuf().append(name);
        getBuf().append(" extends ");
    }
    public SignatureVisitor visitClassBound() {
        return this;
    }
    public SignatureVisitor visitInterfaceBound() {
        return this;
    }
    public SignatureVisitor visitSuperclass() {
        endFormals();
        SignatureSourcer sourcer = new SignatureSourcer();
        assert mSuperClass == null;
        mSuperClass = sourcer;
        return sourcer;
    }
    public SignatureVisitor visitInterface() {
        return this;
    }
    public SignatureVisitor visitParameterType() {
        endFormals();
        SignatureSourcer sourcer = new SignatureSourcer();
        mParameters.add(sourcer);
        return sourcer;
    }
    public SignatureVisitor visitReturnType() {
        endFormals();
        SignatureSourcer sourcer = new SignatureSourcer();
        assert mReturnType == null;
        mReturnType = sourcer;
        return sourcer;
    }
    public SignatureVisitor visitExceptionType() {
        getBuf().append('^');
        return this;
    }
    public void visitBaseType(final char descriptor) {
        getBuf().append(Type.getType(Character.toString(descriptor)).getClassName());
    }
    public void visitTypeVariable(final String name) {
        getBuf().append(name.replace('/', '.'));
    }
    public SignatureVisitor visitArrayType() {
        getBuf().append('[');
        return this;
    }
    public void visitClassType(final String name) {
        getBuf().append(name.replace('/', '.'));
        mArgumentStack *= 2;
    }
    public void visitInnerClassType(final String name) {
        endArguments();
        getBuf().append('.');
        getBuf().append(name.replace('/', '.'));
        mArgumentStack *= 2;
    }
    public void visitTypeArgument() {
        if (mArgumentStack % 2 == 0) {
            ++mArgumentStack;
            getBuf().append('<');
        } else {
            getBuf().append(", ");
        }
        getBuf().append('*');
    }
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        if (mArgumentStack % 2 == 0) {
            ++mArgumentStack;
            getBuf().append('<');
        } else {
            getBuf().append(", ");
        }
        if (wildcard != '=') {
            if (wildcard == '+') {
                getBuf().append("? extends ");
            } else if (wildcard == '-') {
                getBuf().append("? super ");
            } else {
                getBuf().append(wildcard);
            }
        }
        return this;
    }
    public void visitEnd() {
        endArguments();
    }
    private void endFormals() {
        if (mWritingFormals) {
            getBuf().append('>');
            mWritingFormals = false;
        }
    }
    private void endArguments() {
        if (mArgumentStack % 2 != 0) {
            getBuf().append('>');
        }
        mArgumentStack /= 2;
    }
}
