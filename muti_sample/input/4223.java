public class DocFinder {
    public static class Input {
        public MethodDoc method = null;
        public InheritableTaglet taglet = null;
        public String tagId = null;
        public Tag tag = null;
        public boolean isFirstSentence = false;
        public boolean isInheritDocTag = false;
        public boolean isTypeVariableParamTag = false;
        public Input() {}
        public Input(MethodDoc method, InheritableTaglet taglet, Tag tag,
                boolean isFirstSentence, boolean isInheritDocTag) {
            this.method = method;
            this.taglet = taglet;
            this.tag = tag;
            this.isFirstSentence = isFirstSentence;
            this.isInheritDocTag = isInheritDocTag;
        }
        public Input(MethodDoc method, InheritableTaglet taglet, String tagId) {
            this.method = method;
            this.taglet = taglet;
            this.tagId = tagId;
        }
        public Input(MethodDoc method, InheritableTaglet taglet, String tagId,
            boolean isTypeVariableParamTag) {
            this.method = method;
            this.taglet = taglet;
            this.tagId = tagId;
            this.isTypeVariableParamTag = isTypeVariableParamTag;
        }
        public Input(MethodDoc method, InheritableTaglet taglet) {
            this.method = method;
            this.taglet = taglet;
        }
        public Input(MethodDoc method) {
            this.method = method;
        }
        public Input(MethodDoc method, boolean isFirstSentence) {
            this.method = method;
            this.isFirstSentence = isFirstSentence;
        }
        public Input copy() {
            Input clone = new Input();
            clone.method = this.method;
            clone.taglet = this.taglet;
            clone.tagId = this.tagId;
            clone.tag = this.tag;
            clone.isFirstSentence = this.isFirstSentence;
            clone.isInheritDocTag = this.isInheritDocTag;
            clone.isTypeVariableParamTag = this.isTypeVariableParamTag;
            return clone;
        }
    }
    public static class Output {
        public Tag holderTag;
        public Doc holder;
        public Tag[] inlineTags = new Tag[] {};
        public boolean isValidInheritDocTag = true;
        public List<Tag> tagList  = new ArrayList<Tag>();
    }
    public static Output search(Input input) {
        Output output = new Output();
        if (input.isInheritDocTag) {
        } else if (input.taglet == null) {
            output.inlineTags = input.isFirstSentence ?
                input.method.firstSentenceTags() :
                input.method.inlineTags();
            output.holder = input.method;
        } else {
            input.taglet.inherit(input, output);
        }
        if (output.inlineTags != null && output.inlineTags.length > 0) {
            return output;
        }
        output.isValidInheritDocTag = false;
        Input inheritedSearchInput = input.copy();
        inheritedSearchInput.isInheritDocTag = false;
        if (input.method.overriddenMethod() != null) {
            inheritedSearchInput.method = input.method.overriddenMethod();
            output = search(inheritedSearchInput);
            output.isValidInheritDocTag = true;
            if (output != null && output.inlineTags.length > 0) {
                return output;
            }
        }
        MethodDoc[] implementedMethods =
            (new ImplementedMethods(input.method, null)).build(false);
        for (int i = 0; i < implementedMethods.length; i++) {
            inheritedSearchInput.method = implementedMethods[i];
            output = search(inheritedSearchInput);
            output.isValidInheritDocTag = true;
            if (output != null && output.inlineTags.length > 0) {
                return output;
            }
        }
        return output;
    }
}
