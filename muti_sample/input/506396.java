public class API {
    public List packages_; 
    public Hashtable classes_;
    public String name_ = null;
    public PackageAPI currPkg_ = null;
    public ClassAPI currClass_ = null;
    public ConstructorAPI currCtor_ = null;
    public MethodAPI currMethod_ = null;
    public FieldAPI currField_ = null;
    public API() {
        packages_ = new ArrayList(); 
        classes_ = new Hashtable(); 
    }   
    public static final int indentInc = 2;
    public void dump() {
        int indent = 0;
        Iterator iter = packages_.iterator();
        while (iter.hasNext()) {
            dumpPackage((PackageAPI)(iter.next()), indent);
        }
    }
    public void dumpPackage(PackageAPI pkg, int indent) {
        for (int i = 0; i < indent; i++) System.out.print(" ");
        System.out.println("Package Name: " + pkg.name_);
        Iterator iter = pkg.classes_.iterator();
        while (iter.hasNext()) {
            dumpClass((ClassAPI)(iter.next()), indent + indentInc);
        }
        if (pkg.doc_ != null) {
            System.out.print("Package doc block:");
            System.out.println("\"" + pkg.doc_ + "\"");
        }
    }
    public static void dumpClass(ClassAPI c, int indent) {
        for (int i = 0; i < indent; i++) System.out.print(" ");
        if (c.isInterface_)
            System.out.println("Interface name: " + c.name_);
        else
            System.out.println("Class Name: " + c.name_);
        if (c.extends_ != null) {
            for (int i = 0; i < indent; i++) System.out.print(" ");
            System.out.println("Extends: " + c.extends_);
        }
        if (c.implements_.size() != 0) {
            for (int i = 0; i < indent; i++) System.out.print(" ");
            System.out.println("Implements: ");
            Iterator iter = c.implements_.iterator();
            while (iter.hasNext()) {
                String interfaceImpl = (String)(iter.next());
                for (int i = 0; i < indent + 2; i++) System.out.print(" ");
                System.out.println("  " + interfaceImpl);
            }
        }
        if (c.isAbstract_)
            System.out.print("abstract ");
        dumpModifiers(c.modifiers_, indent);
        Iterator iter = c.ctors_.iterator();
        while (iter.hasNext()) {
            dumpCtor((ConstructorAPI)(iter.next()), indent + indentInc);
        }
        iter = c.methods_.iterator();
        while (iter.hasNext()) {
            dumpMethod((MethodAPI)(iter.next()), indent + indentInc);
        }
        iter = c.fields_.iterator();
        while (iter.hasNext()) {
            dumpField((FieldAPI)(iter.next()), indent + indentInc);
        }
        if (c.doc_ != null) {
            System.out.print("Class doc block:");
            System.out.println("\"" + c.doc_ + "\"");
        } else
            System.out.println();
    }
    public static void dumpModifiers(Modifiers m, int indent) {
        for (int i = 0; i < indent; i++) System.out.print(" ");
        if (m.isStatic)
            System.out.print("static ");
        if (m.isFinal)
            System.out.print("final ");
        if (m.visibility != null)
            System.out.print("visibility = " + m.visibility + " ");
        System.out.println();
    }
    public static void dumpCtor(ConstructorAPI c, int indent) {
        for (int i = 0; i < indent; i++) System.out.print(" ");
        System.out.println("Ctor type: " + c.type_);
        System.out.print("exceptions: " + c.exceptions_ + " ");
        dumpModifiers(c.modifiers_, indent);
        if (c.doc_ != null) {
            System.out.print("Ctor doc block:");
            System.out.println("\"" + c.doc_ + "\"");
        }
    }
    public static void dumpMethod(MethodAPI m, int indent) {
        if (m.inheritedFrom_ != null)
            return;
        for (int i = 0; i < indent; i++) System.out.print(" ");
        System.out.print("Method Name: " + m.name_);
        if (m.inheritedFrom_ != null)
            System.out.println(", inherited from: " + m.inheritedFrom_);
        if (m.returnType_ != null)
            System.out.println(", return type: " + m.returnType_);
        else
            System.out.println();
        if (m.isAbstract_)
            System.out.print("abstract ");
        if (m.isNative_)
            System.out.print("native ");
        if (m.isSynchronized_)
            System.out.print("synchronized ");
        System.out.print("exceptions: " + m.exceptions_ + " ");
        dumpModifiers(m.modifiers_, indent);
        Iterator iter = m.params_.iterator();
        while (iter.hasNext()) {
            dumpParam((ParamAPI)(iter.next()), indent + indentInc);
        }
        if (m.doc_ != null) {
            System.out.print("Method doc block:");
            System.out.println("\"" + m.doc_ + "\"");
        }
    }
    public static void dumpField(FieldAPI f, int indent) {
        if (f.inheritedFrom_ != null)
            return;
        for (int i = 0; i < indent; i++) System.out.print(" ");
        System.out.println("Field Name: " + f.name_ + ", type: " + f.type_);
        if (f.inheritedFrom_ != null)
            System.out.println(", inherited from: " + f.inheritedFrom_);
        if (f.isTransient_)
            System.out.print("transient ");
        if (f.isVolatile_)
            System.out.print("volatile ");
        dumpModifiers(f.modifiers_, indent);
        if (f.doc_ != null)
            System.out.print("Field doc block:");
            System.out.println("\"" + f.doc_ + "\"");
    }
    public static void dumpParam(ParamAPI p, int indent) {
        for (int i = 0; i < indent; i++) System.out.print(" ");
        System.out.println("Param Name: " + p.name_ + ", type: " + p.type_);
    }
    public static String stuffHTMLTags(String htmlText) {
        if (htmlText.indexOf("]]>") != -1) {
            System.out.println("Warning: illegal string ]]> found in text. Ignoring the comment.");
            return "";
        }
        return "<![CDATA[" + htmlText + "]]>";
    }
    public static String hideHTMLTags(String htmlText) {
        StringBuffer sb = new StringBuffer(htmlText);
        int i = 0;
        while (i < sb.length()) {
            if (sb.charAt(i) == '<') {
                sb.setCharAt(i ,'l');
                sb.insert(i+1, "EsS_tHaN");
            } else if (sb.charAt(i) == '&') {
                sb.setCharAt(i ,'a');
                sb.insert(i+1, "Nd_cHaR");
            } else if (sb.charAt(i) == '"') {
                sb.setCharAt(i ,'q');
                sb.insert(i+1, "uote_cHaR");
            }
            i++;
        }
        return sb.toString();
    }
    public static String showHTMLTags(String text) {
        StringBuffer sb = new StringBuffer(text);
        StringBuffer res = new StringBuffer();
        int len = sb.length();
        res.setLength(len);
        int i = 0;
        int resIdx = 0;
        while (i < len) {
            char c = sb.charAt(i);
            if (len - i > 8 && c == 'l' && 
                sb.charAt(i+1) == 'E' &&
                sb.charAt(i+2) == 's' &&
                sb.charAt(i+3) == 'S' &&
                sb.charAt(i+4) == '_' &&
                sb.charAt(i+5) == 't' &&
                sb.charAt(i+6) == 'H' &&
                sb.charAt(i+7) == 'a' &&
                sb.charAt(i+8) == 'N') {
                res.setCharAt(resIdx ,'<');
                i += 8;
            } else if (len - i > 9 && c == 'q' && 
                sb.charAt(i+1) == 'U' &&
                sb.charAt(i+2) == 'o' &&
                sb.charAt(i+3) == 'T' &&
                sb.charAt(i+4) == 'e' &&
                sb.charAt(i+5) == '_' &&
                sb.charAt(i+6) == 'c' &&
                sb.charAt(i+7) == 'H' &&
                sb.charAt(i+8) == 'a' &&
                sb.charAt(i+9) == 'R') {
                res.setCharAt(resIdx ,'"');
                i += 9;
            } else if (len - i > 7 && c == 'a' && 
                sb.charAt(i+1) == 'N' &&
                sb.charAt(i+2) == 'd' &&
                sb.charAt(i+3) == '_' &&
                sb.charAt(i+4) == 'c' &&
                sb.charAt(i+5) == 'H' &&
                sb.charAt(i+6) == 'a' &&
                sb.charAt(i+7) == 'R') {
                res.setCharAt(resIdx ,'&');
                i += 7;
            } else {
                res.setCharAt(resIdx, c);
            }
            i++;
            resIdx++;
        }
        res.setLength(resIdx);
        return res.toString();
    }
    public static String convertHTMLTagsToXHTML(String htmlText) {
        StringBuffer sb = new StringBuffer(htmlText);
        int i = 0;
        boolean inTag = false;
        String tag = null;
        while (i < sb.length()) {
            char c = sb.charAt(i);
            if (inTag) {
                if (c == '>') {
                    if (Comments.isMinimizedTag(tag) &&
                        htmlText.indexOf("</" + tag + ">", i) == -1)
                        sb.insert(i, "/");
                    inTag = false;
                } else {
                    tag += c;
                }
            }
            if (c == '<') {
                inTag = true;
                tag = "";
            }
            if (c == '-' && i > 0 && sb.charAt(i-1) == '-') {
                if (!(i > 1 && sb.charAt(i-2) == '!')) { 
                    sb.setCharAt(i, '&');
                    sb.insert(i+1, "#045;");
                    i += 5;
                }
            }
            i++;
        }
        if (inTag) {
            sb.insert(i, ">");
        }
        return sb.toString();
    }
}
