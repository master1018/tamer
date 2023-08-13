public abstract class MemberInfo extends DocInfo implements Comparable, Scoped
{
    public MemberInfo(String rawCommentText, String name, String signature,
                        ClassInfo containingClass, ClassInfo realContainingClass,
                        boolean isPublic, boolean isProtected,
                        boolean isPackagePrivate, boolean isPrivate,
                        boolean isFinal, boolean isStatic, boolean isSynthetic,
                        String kind,
                        SourcePositionInfo position,
                        AnnotationInstanceInfo[] annotations)
    {
        super(rawCommentText, position);
        mName = name;
        mSignature = signature;
        mContainingClass = containingClass;
        mRealContainingClass = realContainingClass;
        mIsPublic = isPublic;
        mIsProtected = isProtected;
        mIsPackagePrivate = isPackagePrivate;
        mIsPrivate = isPrivate;
        mIsFinal = isFinal;
        mIsStatic = isStatic;
        mIsSynthetic = isSynthetic;
        mKind = kind;
        mAnnotations = annotations;
    }
    public abstract boolean isExecutable();
    public String anchor()
    {
        if (mSignature != null) {
            return mName + mSignature;
        } else {
            return mName;
        }
    }
    public String htmlPage() {
        return mContainingClass.htmlPage() + "#" + anchor();
    }
    public int compareTo(Object that) {
        return this.htmlPage().compareTo(((MemberInfo)that).htmlPage());
    }
    public String name()
    {
        return mName;
    }
    public String signature()
    {
        return mSignature;
    }
    public ClassInfo realContainingClass()
    {
        return mRealContainingClass;
    }
    public ClassInfo containingClass()
    {
        return mContainingClass;
    }
    public boolean isPublic()
    {
        return mIsPublic;
    }
    public boolean isProtected()
    {
        return mIsProtected;
    }
    public boolean isPackagePrivate()
    {
        return mIsPackagePrivate;
    }
    public boolean isPrivate()
    {
        return mIsPrivate;
    }
    public boolean isStatic()
    {
        return mIsStatic;
    }
    public boolean isFinal()
    {
        return mIsFinal;
    }
    public boolean isSynthetic()
    {
        return mIsSynthetic;
    }
    @Override
    public ContainerInfo parent()
    {
        return mContainingClass;
    }
    public boolean checkLevel()
    {
        return DroidDoc.checkLevel(mIsPublic, mIsProtected,
                mIsPackagePrivate, mIsPrivate, isHidden());
    }
    public String kind()
    {
        return mKind;
    }
    public AnnotationInstanceInfo[] annotations()
    {
        return mAnnotations;
    }
    ClassInfo mContainingClass;
    ClassInfo mRealContainingClass;
    String mName;
    String mSignature;
    boolean mIsPublic;
    boolean mIsProtected;
    boolean mIsPackagePrivate;
    boolean mIsPrivate;
    boolean mIsFinal;
    boolean mIsStatic;
    boolean mIsSynthetic;
    String mKind;
    private AnnotationInstanceInfo[] mAnnotations;
}
