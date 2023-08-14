public class ClassAttributeDescriptor extends TextAttributeDescriptor {
    private String mSuperClassName;
    private IPostTypeCreationAction mPostCreationAction;
    boolean mMandatory;
    private final boolean mDefaultToProjectOnly;
    public ClassAttributeDescriptor(String superClassName,
            String xmlLocalName,
            String uiName,
            String nsUri,
            String tooltip,
            boolean mandatory) {
        super(xmlLocalName, uiName, nsUri, tooltip);
        mSuperClassName = superClassName;
        mDefaultToProjectOnly = true;
    }
    public ClassAttributeDescriptor(String superClassName,
            IPostTypeCreationAction postCreationAction,
            String xmlLocalName,
            String uiName,
            String nsUri,
            String tooltip,
            boolean mandatory,
            boolean defaultToProjectOnly) {
        super(xmlLocalName, uiName, nsUri, tooltip);
        mSuperClassName = superClassName;
        mPostCreationAction = postCreationAction;
        mDefaultToProjectOnly = defaultToProjectOnly;
    }
    @Override
    public UiAttributeNode createUiNode(UiElementNode uiParent) {
        return new UiClassAttributeNode(mSuperClassName, mPostCreationAction,
                mMandatory, this, uiParent, mDefaultToProjectOnly);
    }
}
