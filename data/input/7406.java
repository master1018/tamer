public class ValueTaglet extends BaseInlineTaglet {
    public ValueTaglet() {
        name = "value";
    }
    public boolean inMethod() {
        return true;
    }
    public boolean inConstructor() {
        return true;
    }
    public boolean inOverview() {
        return true;
    }
    public boolean inPackage() {
        return true;
    }
    public boolean inType() {
        return true;
    }
    private FieldDoc getFieldDoc(Configuration config, Tag tag, String name) {
        if (name == null || name.length() == 0) {
            if (tag.holder() instanceof FieldDoc) {
                return (FieldDoc) tag.holder();
            } else {
                throw new DocletAbortException();
            }
        }
        StringTokenizer st = new StringTokenizer(name, "#");
        String memberName = null;
        ClassDoc cd = null;
        if (st.countTokens() == 1) {
            Doc holder = tag.holder();
            if (holder instanceof MemberDoc) {
                cd = ((MemberDoc) holder).containingClass();
            } else if (holder instanceof ClassDoc) {
                cd = (ClassDoc) holder;
            }
            memberName = st.nextToken();
        } else {
            cd = config.root.classNamed(st.nextToken());
            memberName = st.nextToken();
        }
        if (cd == null) {
            return null;
        }
        FieldDoc[] fields = cd.fields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].name().equals(memberName)) {
                return fields[i];
            }
        }
        return null;
    }
    public TagletOutput getTagletOutput(Tag tag, TagletWriter writer) {
        FieldDoc field = getFieldDoc(
            writer.configuration(), tag, tag.text());
        if (field == null) {
            writer.getMsgRetriever().warning(tag.holder().position(),
                "doclet.value_tag_invalid_reference", tag.text());
        } else if (field.constantValue() != null) {
            return writer.valueTagOutput(field,
                Util.escapeHtmlChars(field.constantValueExpression()),
                ! field.equals(tag.holder()));
        } else {
            writer.getMsgRetriever().warning(tag.holder().position(),
                "doclet.value_tag_invalid_constant", field.name());
        }
        return writer.getOutputInstance();
    }
}
