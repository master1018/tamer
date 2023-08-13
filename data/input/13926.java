class SeeTagImpl extends TagImpl implements SeeTag, LayoutCharacters {
    private String where;
    private String what;
    private PackageDoc referencedPackage;
    private ClassDoc referencedClass;
    private MemberDoc referencedMember;
    String label = "";
    SeeTagImpl(DocImpl holder, String name, String text) {
        super(holder, name, text);
        parseSeeString();
        if (where != null) {
            ClassDocImpl container = null;
            if (holder instanceof MemberDoc) {
                container =
                  (ClassDocImpl)((ProgramElementDoc)holder).containingClass();
            } else if (holder instanceof ClassDoc) {
                container = (ClassDocImpl)holder;
            }
            findReferenced(container);
        }
    }
    public String referencedClassName() {
        return where;
    }
    public PackageDoc referencedPackage() {
        return referencedPackage;
    }
    public ClassDoc referencedClass() {
        return referencedClass;
    }
    public String referencedMemberName() {
        return what;
    }
    public MemberDoc referencedMember() {
        return referencedMember;
    }
    private void parseSeeString() {
        int len = text.length();
        if (len == 0) {
            return;
        }
        switch (text.charAt(0)) {
            case '<':
                if (text.charAt(len-1) != '>') {
                    docenv().warning(holder,
                                     "tag.see.no_close_bracket_on_url",
                                     name, text);
                }
                return;
            case '"':
                if (len == 1 || text.charAt(len-1) != '"') {
                    docenv().warning(holder,
                                     "tag.see.no_close_quote",
                                     name, text);
                } else {
                }
                return;
        }
        int parens = 0;
        int commentstart = 0;
        int start = 0;
        int cp;
        for (int i = start; i < len ; i += Character.charCount(cp)) {
            cp = text.codePointAt(i);
            switch (cp) {
                case '(': parens++; break;
                case ')': parens--; break;
                case '[': case ']': case '.': case '#': break;
                case ',':
                    if (parens <= 0) {
                        docenv().warning(holder,
                                         "tag.see.malformed_see_tag",
                                         name, text);
                        return;
                    }
                    break;
                case ' ': case '\t': case '\n': case CR:
                    if (parens == 0) { 
                        commentstart = i;
                        i = len;
                    }
                    break;
                default:
                    if (!Character.isJavaIdentifierPart(cp)) {
                        docenv().warning(holder,
                                         "tag.see.illegal_character",
                                         name, ""+cp, text);
                    }
                    break;
            }
        }
        if (parens != 0) {
            docenv().warning(holder,
                             "tag.see.malformed_see_tag",
                             name, text);
            return;
        }
        String seetext = "";
        String labeltext = "";
        if (commentstart > 0) {
            seetext = text.substring(start, commentstart);
            labeltext = text.substring(commentstart + 1);
            for (int i = 0; i < labeltext.length(); i++) {
                char ch2 = labeltext.charAt(i);
                if (!(ch2 == ' ' || ch2 == '\t' || ch2 == '\n')) {
                    label = labeltext.substring(i);
                    break;
                }
            }
        } else {
            seetext = text;
            label = "";
        }
        int sharp = seetext.indexOf('#');
        if (sharp >= 0) {
            where = seetext.substring(0, sharp);
            what = seetext.substring(sharp + 1);
        } else {
            if (seetext.indexOf('(') >= 0) {
                docenv().warning(holder,
                                 "tag.see.missing_sharp",
                                 name, text);
                where = "";
                what = seetext;
            }
            else {
                where = seetext;
                what = null;
            }
        }
    }
    private void findReferenced(ClassDocImpl containingClass) {
        if (where.length() > 0) {
            if (containingClass != null) {
                referencedClass = containingClass.findClass(where);
            } else {
                referencedClass = docenv().lookupClass(where);
            }
            if (referencedClass == null && holder() instanceof ProgramElementDoc) {
                referencedClass = docenv().lookupClass(
                    ((ProgramElementDoc) holder()).containingPackage().name() + "." + where);
            }
            if (referencedClass == null) { 
                referencedPackage = docenv().lookupPackage(where);
                return;
            }
        } else {
            if (containingClass == null) {
                docenv().warning(holder,
                                 "tag.see.class_not_specified",
                                 name, text);
                return;
            } else {
                referencedClass = containingClass;
            }
        }
        where = referencedClass.qualifiedName();
        if (what == null) {
            return;
        } else {
            int paren = what.indexOf('(');
            String memName = (paren >= 0 ? what.substring(0, paren) : what);
            String[] paramarr;
            if (paren > 0) {
                paramarr = new ParameterParseMachine(what.
                        substring(paren, what.length())).parseParameters();
                if (paramarr != null) {
                    referencedMember = findExecutableMember(memName, paramarr,
                                                            referencedClass);
                } else {
                    referencedMember = null;
                }
            } else {
                referencedMember = findExecutableMember(memName, null,
                                                        referencedClass);
                FieldDoc fd = ((ClassDocImpl)referencedClass).
                                                            findField(memName);
                if (referencedMember == null ||
                    (fd != null &&
                     fd.containingClass()
                         .subclassOf(referencedMember.containingClass()))) {
                    referencedMember = fd;
                }
            }
            if (referencedMember == null) {
                docenv().warning(holder,
                                 "tag.see.can_not_find_member",
                                 name, what, where);
            }
        }
    }
    private MemberDoc findReferencedMethod(String memName, String[] paramarr,
                                           ClassDoc referencedClass) {
        MemberDoc meth = findExecutableMember(memName, paramarr, referencedClass);
        ClassDoc[] nestedclasses = referencedClass.innerClasses();
        if (meth == null) {
            for (int i = 0; i < nestedclasses.length; i++) {
                meth = findReferencedMethod(memName, paramarr, nestedclasses[i]);
                if (meth != null) {
                    return meth;
                }
            }
        }
        return null;
    }
    private MemberDoc findExecutableMember(String memName, String[] paramarr,
                                           ClassDoc referencedClass) {
        if (memName.equals(referencedClass.name())) {
            return ((ClassDocImpl)referencedClass).findConstructor(memName,
                                                                   paramarr);
        } else {   
            return ((ClassDocImpl)referencedClass).findMethod(memName,
                                                              paramarr);
        }
    }
    class ParameterParseMachine {
        static final int START = 0;
        static final int TYPE = 1;
        static final int NAME = 2;
        static final int TNSPACE = 3;  
        static final int ARRAYDECORATION = 4;
        static final int ARRAYSPACE = 5;
        String parameters;
        StringBuilder typeId;
        ListBuffer<String> paramList;
        ParameterParseMachine(String parameters) {
            this.parameters = parameters;
            this.paramList = new ListBuffer<String>();
            typeId = new StringBuilder();
        }
        public String[] parseParameters() {
            if (parameters.equals("()")) {
                return new String[0];
            }   
            int state = START;
            int prevstate = START;
            parameters = parameters.substring(1, parameters.length() - 1);
            int cp;
            for (int index = 0; index < parameters.length(); index += Character.charCount(cp)) {
                cp = parameters.codePointAt(index);
                switch (state) {
                    case START:
                        if (Character.isJavaIdentifierStart(cp)) {
                            typeId.append(Character.toChars(cp));
                            state = TYPE;
                        }
                        prevstate = START;
                        break;
                    case TYPE:
                        if (Character.isJavaIdentifierPart(cp) || cp == '.') {
                            typeId.append(Character.toChars(cp));
                        } else if (cp == '[') {
                            typeId.append('[');
                            state = ARRAYDECORATION;
                        } else if (Character.isWhitespace(cp)) {
                            state = TNSPACE;
                        } else if (cp == ',') {  
                            addTypeToParamList();
                            state = START;
                        }
                        prevstate = TYPE;
                        break;
                    case TNSPACE:
                        if (Character.isJavaIdentifierStart(cp)) { 
                            if (prevstate == ARRAYDECORATION) {
                                docenv().warning(holder,
                                                 "tag.missing_comma_space",
                                                 name,
                                                 "(" + parameters + ")");
                                return (String[])null;
                            }
                            addTypeToParamList();
                            state = NAME;
                        } else if (cp == '[') {
                            typeId.append('[');
                            state = ARRAYDECORATION;
                        } else if (cp == ',') {   
                            addTypeToParamList();
                            state = START;
                        } 
                        prevstate = TNSPACE;
                        break;
                    case ARRAYDECORATION:
                        if (cp == ']') {
                            typeId.append(']');
                            state = TNSPACE;
                        } else if (!Character.isWhitespace(cp)) {
                            docenv().warning(holder,
                                             "tag.illegal_char_in_arr_dim",
                                             name,
                                             "(" + parameters + ")");
                            return (String[])null;
                        }
                        prevstate = ARRAYDECORATION;
                        break;
                    case NAME:
                        if (cp == ',') {  
                            state = START;
                        }
                        prevstate = NAME;
                        break;
                }
            }
            if (state == ARRAYDECORATION ||
                (state == START && prevstate == TNSPACE)) {
                docenv().warning(holder,
                                 "tag.illegal_see_tag",
                                 "(" + parameters + ")");
            }
            if (typeId.length() > 0) {
                paramList.append(typeId.toString());
            }
            return paramList.toArray(new String[paramList.length()]);
        }
        void addTypeToParamList() {
            if (typeId.length() > 0) {
                paramList.append(typeId.toString());
                typeId.setLength(0);
            }
        }
    }
    @Override
    public String kind() {
        return "@see";
    }
    public String label() {
        return label;
    }
}
