public class ThrowsTaglet extends BaseExecutableMemberTaglet
    implements InheritableTaglet {
    public ThrowsTaglet() {
        name = "throws";
    }
    public void inherit(DocFinder.Input input, DocFinder.Output output) {
        ClassDoc exception;
        if (input.tagId == null) {
            ThrowsTag throwsTag = (ThrowsTag) input.tag;
            exception = throwsTag.exception();
            input.tagId = exception == null ?
                throwsTag.exceptionName() :
                throwsTag.exception().qualifiedName();
        } else {
            exception = input.method.containingClass().findClass(input.tagId);
        }
        ThrowsTag[] tags = input.method.throwsTags();
        for (int i = 0; i < tags.length; i++) {
            if (input.tagId.equals(tags[i].exceptionName()) ||
                (tags[i].exception() != null &&
                    (input.tagId.equals(tags[i].exception().qualifiedName())))) {
                output.holder = input.method;
                output.holderTag = tags[i];
                output.inlineTags = input.isFirstSentence ?
                    tags[i].firstSentenceTags() : tags[i].inlineTags();
                output.tagList.add(tags[i]);
            } else if (exception != null && tags[i].exception() != null &&
                    tags[i].exception().subclassOf(exception)) {
                output.tagList.add(tags[i]);
            }
        }
    }
    private TagletOutput linkToUndocumentedDeclaredExceptions(
            Type[] declaredExceptionTypes, Set<String> alreadyDocumented,
            TagletWriter writer) {
        TagletOutput result = writer.getOutputInstance();
        for (int i = 0; i < declaredExceptionTypes.length; i++) {
            if (declaredExceptionTypes[i].asClassDoc() != null &&
                ! alreadyDocumented.contains(
                        declaredExceptionTypes[i].asClassDoc().name()) &&
                ! alreadyDocumented.contains(
                    declaredExceptionTypes[i].asClassDoc().qualifiedName())) {
                if (alreadyDocumented.size() == 0) {
                    result.appendOutput(writer.getThrowsHeader());
                }
                result.appendOutput(writer.throwsTagOutput(declaredExceptionTypes[i]));
                alreadyDocumented.add(declaredExceptionTypes[i].asClassDoc().name());
            }
        }
        return result;
    }
    private TagletOutput inheritThrowsDocumentation(Doc holder,
            Type[] declaredExceptionTypes, Set<String> alreadyDocumented,
            TagletWriter writer) {
        TagletOutput result = writer.getOutputInstance();
        if (holder instanceof MethodDoc) {
            Set<Tag> declaredExceptionTags = new LinkedHashSet<Tag>();
            for (int j = 0; j < declaredExceptionTypes.length; j++) {
                DocFinder.Output inheritedDoc =
                    DocFinder.search(new DocFinder.Input((MethodDoc) holder, this,
                        declaredExceptionTypes[j].typeName()));
                if (inheritedDoc.tagList.size() == 0) {
                    inheritedDoc = DocFinder.search(new DocFinder.Input(
                        (MethodDoc) holder, this,
                        declaredExceptionTypes[j].qualifiedTypeName()));
                }
                declaredExceptionTags.addAll(inheritedDoc.tagList);
            }
            result.appendOutput(throwsTagsOutput(
                declaredExceptionTags.toArray(new ThrowsTag[] {}),
                writer, alreadyDocumented, false));
        }
        return result;
    }
    public TagletOutput getTagletOutput(Doc holder, TagletWriter writer) {
        ExecutableMemberDoc execHolder = (ExecutableMemberDoc) holder;
        ThrowsTag[] tags = execHolder.throwsTags();
        TagletOutput result = writer.getOutputInstance();
        HashSet<String> alreadyDocumented = new HashSet<String>();
        if (tags.length > 0) {
            result.appendOutput(throwsTagsOutput(
                execHolder.throwsTags(), writer, alreadyDocumented, true));
        }
        result.appendOutput(inheritThrowsDocumentation(holder,
            execHolder.thrownExceptionTypes(), alreadyDocumented, writer));
        result.appendOutput(linkToUndocumentedDeclaredExceptions(
            execHolder.thrownExceptionTypes(), alreadyDocumented, writer));
        return result;
    }
    protected TagletOutput throwsTagsOutput(ThrowsTag[] throwTags,
        TagletWriter writer, Set<String> alreadyDocumented, boolean allowDups) {
        TagletOutput result = writer.getOutputInstance();
        if (throwTags.length > 0) {
            for (int i = 0; i < throwTags.length; ++i) {
                ThrowsTag tt = throwTags[i];
                ClassDoc cd = tt.exception();
                if ((!allowDups) && (alreadyDocumented.contains(tt.exceptionName()) ||
                    (cd != null && alreadyDocumented.contains(cd.qualifiedName())))) {
                    continue;
                }
                if (alreadyDocumented.size() == 0) {
                    result.appendOutput(writer.getThrowsHeader());
                }
                result.appendOutput(writer.throwsTagOutput(tt));
                alreadyDocumented.add(cd != null ?
                    cd.qualifiedName() : tt.exceptionName());
            }
        }
        return result;
    }
}
