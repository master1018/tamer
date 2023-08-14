public abstract class AbstractExecutableMemberWriter extends AbstractMemberWriter {
    public AbstractExecutableMemberWriter(SubWriterHolderWriter writer,
                                     ClassDoc classdoc) {
        super(writer, classdoc);
    }
    public AbstractExecutableMemberWriter(SubWriterHolderWriter writer) {
        super(writer);
    }
    protected int addTypeParameters(ExecutableMemberDoc member, Content htmltree) {
        LinkInfoImpl linkInfo = new LinkInfoImpl(
            LinkInfoImpl.CONTEXT_MEMBER_TYPE_PARAMS, member, false);
        String typeParameters = writer.getTypeParameterLinks(linkInfo);
        if (linkInfo.displayLength > 0) {
            Content linkContent = new RawHtml(typeParameters);
            htmltree.addContent(linkContent);
            htmltree.addContent(writer.getSpace());
            writer.displayLength += linkInfo.displayLength + 1;
        }
        return linkInfo.displayLength;
    }
    protected Content getDeprecatedLink(ProgramElementDoc member) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        return writer.getDocLink(LinkInfoImpl.CONTEXT_MEMBER, (MemberDoc) emd,
                emd.qualifiedName() + emd.flatSignature());
    }
    protected void addSummaryLink(int context, ClassDoc cd, ProgramElementDoc member,
            Content tdSummary) {
        ExecutableMemberDoc emd = (ExecutableMemberDoc)member;
        String name = emd.name();
        Content strong = HtmlTree.STRONG(new RawHtml(
                writer.getDocLink(context, cd, (MemberDoc) emd,
                name, false)));
        Content code = HtmlTree.CODE(strong);
        writer.displayLength = name.length();
        addParameters(emd, false, code);
        tdSummary.addContent(code);
    }
    protected void addInheritedSummaryLink(ClassDoc cd,
            ProgramElementDoc member, Content linksTree) {
        linksTree.addContent(new RawHtml(
                writer.getDocLink(LinkInfoImpl.CONTEXT_MEMBER, cd, (MemberDoc) member,
                member.name(), false)));
    }
    protected void addParam(ExecutableMemberDoc member, Parameter param,
        boolean isVarArg, Content tree) {
        if (param.type() != null) {
            Content link = new RawHtml(writer.getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_EXECUTABLE_MEMBER_PARAM, param.type(),
                    isVarArg)));
            tree.addContent(link);
        }
        if(param.name().length() > 0) {
            tree.addContent(writer.getSpace());
            tree.addContent(param.name());
        }
    }
    protected void addParameters(ExecutableMemberDoc member, Content htmltree) {
        addParameters(member, true, htmltree);
    }
    protected void addParameters(ExecutableMemberDoc member,
            boolean includeAnnotations, Content htmltree) {
        htmltree.addContent("(");
        Parameter[] params = member.parameters();
        String indent = makeSpace(writer.displayLength);
        if (configuration().linksource) {
            indent+= makeSpace(member.name().length());
        }
        int paramstart;
        for (paramstart = 0; paramstart < params.length; paramstart++) {
            Parameter param = params[paramstart];
            if (!param.name().startsWith("this$")) {
                if (includeAnnotations) {
                    boolean foundAnnotations =
                            writer.addAnnotationInfo(indent.length(),
                            member, param, htmltree);
                    if (foundAnnotations) {
                        htmltree.addContent(DocletConstants.NL);
                        htmltree.addContent(indent);
                    }
                }
                addParam(member, param,
                    (paramstart == params.length - 1) && member.isVarArgs(), htmltree);
                break;
            }
        }
        for (int i = paramstart + 1; i < params.length; i++) {
            htmltree.addContent(",");
            htmltree.addContent(DocletConstants.NL);
            htmltree.addContent(indent);
            if (includeAnnotations) {
                boolean foundAnnotations =
                        writer.addAnnotationInfo(indent.length(), member, params[i],
                        htmltree);
                if (foundAnnotations) {
                    htmltree.addContent(DocletConstants.NL);
                    htmltree.addContent(indent);
                }
            }
            addParam(member, params[i], (i == params.length - 1) && member.isVarArgs(),
                    htmltree);
        }
        htmltree.addContent(")");
    }
    protected void addExceptions(ExecutableMemberDoc member, Content htmltree) {
        Type[] exceptions = member.thrownExceptionTypes();
        if(exceptions.length > 0) {
            LinkInfoImpl memberTypeParam = new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_MEMBER, member, false);
            int retlen = getReturnTypeLength(member);
            writer.getTypeParameterLinks(memberTypeParam);
            retlen += memberTypeParam.displayLength == 0 ?
                0 : memberTypeParam.displayLength + 1;
            String indent = makeSpace(modifierString(member).length() +
                    member.name().length() + retlen - 4);
            htmltree.addContent(DocletConstants.NL);
            htmltree.addContent(indent);
            htmltree.addContent("throws ");
            indent += "       ";
            Content link = new RawHtml(writer.getLink(new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_MEMBER, exceptions[0])));
            htmltree.addContent(link);
            for(int i = 1; i < exceptions.length; i++) {
                htmltree.addContent(",");
                htmltree.addContent(DocletConstants.NL);
                htmltree.addContent(indent);
                Content exceptionLink = new RawHtml(writer.getLink(new LinkInfoImpl(
                        LinkInfoImpl.CONTEXT_MEMBER, exceptions[i])));
                htmltree.addContent(exceptionLink);
            }
        }
    }
    protected int getReturnTypeLength(ExecutableMemberDoc member) {
        if (member instanceof MethodDoc) {
            MethodDoc method = (MethodDoc)member;
            Type rettype = method.returnType();
            if (rettype.isPrimitive()) {
                return rettype.typeName().length() +
                       rettype.dimension().length();
            } else {
                LinkInfoImpl linkInfo = new LinkInfoImpl(
                    LinkInfoImpl.CONTEXT_MEMBER, rettype);
                writer.getLink(linkInfo);
                return linkInfo.displayLength;
            }
        } else {   
            return -1;
        }
    }
    protected ClassDoc implementsMethodInIntfac(MethodDoc method,
                                                ClassDoc[] intfacs) {
        for (int i = 0; i < intfacs.length; i++) {
            MethodDoc[] methods = intfacs[i].methods();
            if (methods.length > 0) {
                for (int j = 0; j < methods.length; j++) {
                    if (methods[j].name().equals(method.name()) &&
                          methods[j].signature().equals(method.signature())) {
                        return intfacs[i];
                    }
                }
            }
        }
        return null;
    }
    protected String getErasureAnchor(ExecutableMemberDoc emd) {
        StringBuffer buf = new StringBuffer(emd.name() + "(");
        Parameter[] params = emd.parameters();
        boolean foundTypeVariable = false;
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            Type t = params[i].type();
            foundTypeVariable = foundTypeVariable || t.asTypeVariable() != null;
            buf.append(t.isPrimitive() ?
                t.typeName() : t.asClassDoc().qualifiedName());
            buf.append(t.dimension());
        }
        buf.append(")");
        return foundTypeVariable ? buf.toString() : null;
    }
}
