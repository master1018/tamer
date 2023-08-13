class MemberDiff {
    public String name_;
    public String oldType_ = null;
    public String newType_ = null;
    public String oldSignature_ = null;
    public String newSignature_ = null;
    public String oldExceptions_ = null;
    public String newExceptions_ = null;
    public String documentationChange_ = null;
    public String modifiersChange_ = null;
    public String inheritedFrom_ = null;
    public MemberDiff(String name) {
        name_ = name;
    }   
    public void addModifiersChange(String commonModifierChanges) {
        if (commonModifierChanges != null) {
            if (modifiersChange_ == null)
                modifiersChange_ = commonModifierChanges;
            else
                modifiersChange_ += " " + commonModifierChanges;
        }
    }
}
