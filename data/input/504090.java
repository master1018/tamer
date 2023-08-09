public class LinkReference {
    public String text;
    public String kind;
    public String label;
    public String href;
    public PackageInfo packageInfo;
    public ClassInfo classInfo;
    public MemberInfo memberInfo;
    public String referencedMemberName;
    public boolean good;
    private static final Pattern HREF_PATTERN
            = Pattern.compile("^<a href=\"([^\"]*)\">([^<]*)</a>[ \n\r\t]*$",
                              Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTE_PATTERN
            = Pattern.compile("^\"([^\"]*)\"[ \n\r\t]*$");
    public static LinkReference parse(String text, ContainerInfo base, SourcePositionInfo pos,
                                        boolean printOnErrors) {
        LinkReference result = new LinkReference();
        result.text = text;
        int index;
        int len = text.length();
        int pairs = 0;
        int pound = -1;
        done: {
            for (index=0; index<len; index++) {
                char c = text.charAt(index);
                switch (c)
                {
                    case '(':
                        pairs++;
                        break;
                    case '[':
                        pairs++;
                        break;
                    case ')':
                        pairs--;
                        break;
                    case ']':
                        pairs--;
                        break;
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        if (pairs == 0) {
                            break done;
                        }
                        break;
                    case '#':
                        if (pound < 0) {
                            pound = index;
                        }
                        break;
                }
            }
        }
        if (index == len && pairs != 0) {
            Errors.error(Errors.UNRESOLVED_LINK, pos,
                        "unable to parse link/see tag: " + text.trim());
            return result;
        }
        int linkend = index;
        for (; index<len; index++) {
            char c = text.charAt(index);
            if (!(c == ' ' || c == '\t' || c == '\r' || c == '\n')) {
                break;
            }
        }
        result.label = text.substring(index);
        String ref;
        String mem;
        if (pound == 0) {
            ref = null;
            mem = text.substring(1, linkend);
        }
        else if (pound > 0) {
            ref = text.substring(0, pound);
            mem = text.substring(pound+1, linkend);
        }
        else {
            ref = text.substring(0, linkend);
            mem = null;
        }
        String[] params = null;
        String[] paramDimensions = null;
        if (mem != null) {
            index = mem.indexOf('(');
            if (index > 0) {
                ArrayList<String> paramList = new ArrayList<String>();
                ArrayList<String> paramDimensionList = new ArrayList<String>();
                len = mem.length();
                int start = index+1;
                final int START = 0;
                final int TYPE = 1;
                final int NAME = 2;
                int dimension = 0;
                int arraypair = 0;
                int state = START;
                int typestart = 0;
                int typeend = -1;
                for (int i=start; i<len; i++) {
                    char c = mem.charAt(i);
                    switch (state)
                    {
                        case START:
                            if (c!=' ' && c!='\t' && c!='\r' && c!='\n') {
                                state = TYPE;
                                typestart = i;
                            }
                            break;
                        case TYPE:
                            if (c == '[') {
                                if (typeend < 0) {
                                    typeend = i;
                                }
                                dimension++;
                                arraypair++;
                            }
                            else if (c == ']') {
                                arraypair--;
                            }
                            else if (c==' ' || c=='\t' || c=='\r' || c=='\n') {
                                if (typeend < 0) {
                                    typeend = i;
                                }
                            }
                            else {
                                if (typeend >= 0 || c == ')' || c == ',') {
                                    if (typeend < 0) {
                                        typeend = i;
                                    }
                                    String s = mem.substring(typestart, typeend);
                                    paramList.add(s);
                                    s = "";
                                    for (int j=0; j<dimension; j++) {
                                        s += "[]";
                                    }
                                    paramDimensionList.add(s);
                                    state = START;
                                    typeend = -1;
                                    dimension = 0;
                                    if (c == ',' || c == ')') {
                                        state = START;
                                    } else {
                                        state = NAME;
                                    }
                                }
                            }
                            break;
                        case NAME:
                            if (c == ',' || c == ')') {
                                state = START;
                            }
                            break;
                    }
                }
                params = paramList.toArray(new String[paramList.size()]);
                paramDimensions = paramDimensionList.toArray(new String[paramList.size()]);
                mem = mem.substring(0, index);
            }
        }
        ClassInfo cl = null;
        if (base instanceof ClassInfo) {
            cl = (ClassInfo)base;
        }
        if (ref == null) {
            if (cl != null) {
                result.classInfo = cl;
            }
        } else {
            if (cl != null) {
                result.classInfo = cl.extendedFindClass(ref);
                if (result.classInfo == null) {
                    result.classInfo = cl.findClass(ref);
                }
                if (result.classInfo == null) {
                    result.classInfo = cl.findInnerClass(ref);
                }
            }
            if (result.classInfo == null) {
                result.classInfo = Converter.obtainClass(ref);
            }
            if (result.classInfo == null) {
                result.packageInfo = Converter.obtainPackage(ref);
            }
        }
        if (result.classInfo != null && mem != null) {
            if (params == null) {
                FieldInfo field = result.classInfo.findField(mem);
                if (field != null) {
                    result.classInfo = field.containingClass();
                    result.memberInfo = field;
                }
            }
            if (result.memberInfo == null) {
                MethodInfo method = result.classInfo.findMethod(mem, params, paramDimensions);
                if (method != null) {
                    result.classInfo = method.containingClass();
                    result.memberInfo = method;
                }
            }
        }
        result.referencedMemberName = mem;
        if (params != null) {
            result.referencedMemberName = result.referencedMemberName + '(';
            len = params.length;
            if (len > 0) {
                len--;
                for (int i=0; i<len; i++) {
                    result.referencedMemberName = result.referencedMemberName + params[i]
                            + paramDimensions[i] + ", ";
                }
                result.referencedMemberName = result.referencedMemberName + params[len]
                        + paramDimensions[len];
            }
            result.referencedMemberName = result.referencedMemberName + ")";
        }
        if (false) {
            result.label = result.label + "/" + ref + "/" + mem + '/';
            if (params != null) {
                for (int i=0; i<params.length; i++) {
                    result.label += params[i] + "|";
                }
            }
            FieldInfo f = (result.memberInfo instanceof FieldInfo)
                        ? (FieldInfo)result.memberInfo
                        : null;
            MethodInfo m = (result.memberInfo instanceof MethodInfo)
                        ? (MethodInfo)result.memberInfo
                        : null;
            result.label = result.label
                        + "/package=" + (result.packageInfo!=null?result.packageInfo.name():"")
                        + "/class=" + (result.classInfo!=null?result.classInfo.qualifiedName():"")
                        + "/field=" + (f!=null?f.name():"")
                        + "/method=" + (m!=null?m.name():"");
        }
        MethodInfo method = null;
        boolean skipHref = false;
        if (result.memberInfo != null && result.memberInfo.isExecutable()) {
           method = (MethodInfo)result.memberInfo;
        }
        if (text.startsWith("\"")) {
            Matcher matcher = QUOTE_PATTERN.matcher(text);
            if (! matcher.matches()) {
                Errors.error(Errors.UNRESOLVED_LINK, pos,
                        "unbalanced quoted link/see tag: " + text.trim());
                result.makeError();
                return result;
            }
            skipHref = true;
            result.label = matcher.group(1);
            result.kind = "@seeJustLabel";
        }
        else if (text.startsWith("<")) {
            Matcher matcher = HREF_PATTERN.matcher(text);
            if (! matcher.matches()) {
                Errors.error(Errors.UNRESOLVED_LINK, pos,
                        "invalid <a> link/see tag: " + text.trim());
                result.makeError();
                return result;
            }
            result.href = matcher.group(1);
            result.label = matcher.group(2);
            result.kind = "@seeHref";
        }
        else if (result.packageInfo != null) {
            result.href = result.packageInfo.htmlPage();
            if (result.label.length() == 0) {
                result.href = result.packageInfo.htmlPage();
                result.label = result.packageInfo.name();
            }
        }
        else if (result.classInfo != null && result.referencedMemberName == null) {
            if (result.label.length() == 0) {
                result.label = result.classInfo.name();
            }
            result.href = result.classInfo.htmlPage();
        }
        else if (result.memberInfo != null) {
            ClassInfo containing = result.memberInfo.containingClass();
            if (result.memberInfo.isExecutable()) {
                if (result.referencedMemberName.indexOf('(') < 0) {
                    result.referencedMemberName += method.flatSignature();
                }
            } 
            if (result.label.length() == 0) {
                result.label = result.referencedMemberName;
            }
            result.href = containing.htmlPage() + '#' + result.memberInfo.anchor();
        }
        if (result.href == null && !skipHref) {
            if (printOnErrors && (base == null || base.checkLevel())) {
                Errors.error(Errors.UNRESOLVED_LINK, pos,
                        "Unresolved link/see tag \"" + text.trim()
                        + "\" in " + ((base != null) ? base.qualifiedName() : "[null]"));
            }
            result.makeError();
        }
        else if (result.memberInfo != null && !result.memberInfo.checkLevel()) {
            if (printOnErrors && (base == null || base.checkLevel())) {
                Errors.error(Errors.HIDDEN_LINK, pos,
                        "Link to hidden member: " + text.trim());
                result.href = null;
            }
            result.kind = "@seeJustLabel";
        }
        else if (result.classInfo != null && !result.classInfo.checkLevel()) {
            if (printOnErrors && (base == null || base.checkLevel())) {
                Errors.error(Errors.HIDDEN_LINK, pos,
                        "Link to hidden class: " + text.trim() + " label=" + result.label);
                result.href = null;
            }
            result.kind = "@seeJustLabel";
        }
        else if (result.packageInfo != null && !result.packageInfo.checkLevel()) {
            if (printOnErrors && (base == null || base.checkLevel())) {
                Errors.error(Errors.HIDDEN_LINK, pos,
                        "Link to hidden package: " + text.trim());
                result.href = null;
            }
            result.kind = "@seeJustLabel";
        }
        result.good = true;
        return result;
    }
    public boolean checkLevel() {
        if (memberInfo != null) {
            return memberInfo.checkLevel();
        }
        if (classInfo != null) {
            return classInfo.checkLevel();
        }
        if (packageInfo != null) {
            return packageInfo.checkLevel();
        }
        return false;
    }
    private void makeError() {
        this.href = null;
        if (this.label == null) {
            this.label = "";
        }
        this.label = "ERROR(" + this.label + "/" + text.trim() + ")";
    }
    private LinkReference() {
    }
}
