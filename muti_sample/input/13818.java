public abstract class LinkFactory {
    protected abstract LinkOutput getOutputInstance();
    public LinkOutput getLinkOutput(LinkInfo linkInfo) {
        if (linkInfo.type != null) {
            Type type = linkInfo.type;
            LinkOutput linkOutput = getOutputInstance();
            if (type.isPrimitive()) {
                linkInfo.displayLength += type.typeName().length();
                linkOutput.append(type.typeName());
            } else if (type.asWildcardType() != null) {
                linkInfo.isTypeBound = true;
                linkInfo.displayLength += 1;
                linkOutput.append("?");
                WildcardType wildcardType = type.asWildcardType();
                Type[] extendsBounds = wildcardType.extendsBounds();
                for (int i = 0; i < extendsBounds.length; i++) {
                    linkInfo.displayLength += i > 0 ? 2 : 9;
                    linkOutput.append(i > 0 ? ", " : " extends ");
                    setBoundsLinkInfo(linkInfo, extendsBounds[i]);
                    linkOutput.append(getLinkOutput(linkInfo));
                }
                Type[] superBounds = wildcardType.superBounds();
                for (int i = 0; i < superBounds.length; i++) {
                    linkInfo.displayLength += i > 0 ? 2 : 7;
                    linkOutput.append(i > 0 ? ", " : " super ");
                    setBoundsLinkInfo(linkInfo, superBounds[i]);
                    linkOutput.append(getLinkOutput(linkInfo));
                }
            } else if (type.asTypeVariable()!= null) {
                linkInfo.isTypeBound = true;
                Doc owner = type.asTypeVariable().owner();
                if ((! linkInfo.excludeTypeParameterLinks) &&
                        owner instanceof ClassDoc) {
                    linkInfo.classDoc = (ClassDoc) owner;
                    linkInfo.label = type.typeName();
                    linkOutput.append(getClassLink(linkInfo));
                } else {
                    linkInfo.displayLength += type.typeName().length();
                    linkOutput.append(type.typeName());
                }
                Type[] bounds = type.asTypeVariable().bounds();
                if (! linkInfo.excludeTypeBounds) {
                    linkInfo.excludeTypeBounds = true;
                    for (int i = 0; i < bounds.length; i++) {
                        linkInfo.displayLength += i > 0 ? 2 : 9;
                        linkOutput.append(i > 0 ? " & " : " extends ");
                        setBoundsLinkInfo(linkInfo, bounds[i]);
                        linkOutput.append(getLinkOutput(linkInfo));
                    }
                }
            } else if (type.asClassDoc() != null) {
                if (linkInfo.isTypeBound &&
                        linkInfo.excludeTypeBoundsLinks) {
                    linkInfo.displayLength += type.typeName().length();
                    linkOutput.append(type.typeName());
                    linkOutput.append(getTypeParameterLinks(linkInfo));
                    return linkOutput;
                } else {
                    linkInfo.classDoc = type.asClassDoc();
                    linkOutput = getClassLink(linkInfo);
                    if (linkInfo.includeTypeAsSepLink) {
                        linkOutput.append(getTypeParameterLinks(linkInfo, false));
                    }
                }
            }
            if (linkInfo.isVarArg) {
                if (type.dimension().length() > 2) {
                    linkInfo.displayLength += type.dimension().length()-2;
                    linkOutput.append(type.dimension().substring(2));
                }
                linkInfo.displayLength += 3;
                linkOutput.append("...");
            } else {
                linkInfo.displayLength += type.dimension().length();
                linkOutput.append(type.dimension());
            }
            return linkOutput;
        } else if (linkInfo.classDoc != null) {
            LinkOutput linkOutput = getClassLink(linkInfo);
            if (linkInfo.includeTypeAsSepLink) {
                linkOutput.append(getTypeParameterLinks(linkInfo, false));
            }
            return linkOutput;
        } else {
            return null;
        }
    }
    private void setBoundsLinkInfo(LinkInfo linkInfo, Type bound) {
        linkInfo.classDoc = null;
        linkInfo.label = null;
        linkInfo.type = bound;
    }
    protected abstract LinkOutput getClassLink(LinkInfo linkInfo);
    protected abstract LinkOutput getTypeParameterLink(LinkInfo linkInfo,
        Type typeParam);
    public LinkOutput getTypeParameterLinks(LinkInfo linkInfo) {
        return getTypeParameterLinks(linkInfo, true);
    }
    public LinkOutput getTypeParameterLinks(LinkInfo linkInfo, boolean isClassLabel) {
        LinkOutput output = getOutputInstance();
        Type[] vars;
        if (linkInfo.executableMemberDoc != null) {
            vars = linkInfo.executableMemberDoc.typeParameters();
        } else if (linkInfo.type != null &&
                linkInfo.type.asParameterizedType() != null){
            vars =  linkInfo.type.asParameterizedType().typeArguments();
        } else if (linkInfo.classDoc != null){
            vars = linkInfo.classDoc.typeParameters();
        } else {
            return output;
        }
        if (((linkInfo.includeTypeInClassLinkLabel && isClassLabel) ||
             (linkInfo.includeTypeAsSepLink && ! isClassLabel)
              )
            && vars.length > 0) {
            linkInfo.displayLength += 1;
            output.append(getLessThanString());
            for (int i = 0; i < vars.length; i++) {
                if (i > 0) {
                    linkInfo.displayLength += 1;
                    output.append(",");
                }
                output.append(getTypeParameterLink(linkInfo, vars[i]));
            }
            linkInfo.displayLength += 1;
            output.append(getGreaterThanString());
        }
        return output;
    }
    protected String getLessThanString() {
        return "&lt;";
    }
    protected String getGreaterThanString() {
        return "&gt;";
    }
}
