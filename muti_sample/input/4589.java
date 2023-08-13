public class GenSwingBeanInfo {
    private final static String BEANINFO_SUFFIX = "BeanInfo.java";
    private final static String TOK_BEANPACKAGE = "BeanPackageName";
    private final static String TOK_BEANCLASS = "BeanClassName";
    private final static String TOK_BEANOBJECT = "BeanClassObject";
    private final static String TOK_CLASSDESC = "ClassDescriptors";
    private final static String TOK_BEANDESC = "BeanDescription";
    private final static String TOK_PROPDESC = "BeanPropertyDescriptors";
    private final static String TOK_ENUMVARS = "EnumVariables";
    private String enumcode;  
    private boolean DEBUG = false;
    private String fileDir;
    private String templateFilename;
    public GenSwingBeanInfo(String fileDir, String templateFilename, boolean debug)  {
        this.fileDir = fileDir;
        this.templateFilename = templateFilename;
        this.DEBUG = debug;
    }
    private PrintStream initOutputFile(String classname) {
        try {
            OutputStream out = new FileOutputStream(fileDir + File.separator + classname + BEANINFO_SUFFIX);
            BufferedOutputStream bout = new BufferedOutputStream(out);
            return new PrintStream(out);
        } catch (IOException e){
        }
        return null;
    }
    private static void messageAndExit(String msg) {
        System.err.println("\n" + msg);
        System.exit(1);
    }
    private String loadTemplate() {
        String template = "<no template>";
        try {
            File file = new File(templateFilename);
            DataInputStream stream = new DataInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuffer buffer = new StringBuffer();
            int c;
            while((c = reader.read()) != -1) {
                buffer.append((char)c);
            }
            template = buffer.toString();
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            messageAndExit("GenSwingBeanInfo: Couldn't load template: " + templateFilename + e);
        }
        return template;
    }
    private String genBeanDescriptor(DocBeanInfo dbi) {
        String code = "";
        int beanflags = dbi.beanflags;
        if ((beanflags & DocBeanInfo.EXPERT) != 0)
            code += " sun.swing.BeanInfoUtils.EXPERT, Boolean.TRUE,\n";
        if ((beanflags & DocBeanInfo.HIDDEN) !=0)
            code += "                    sun.swing.BeanInfoUtils.HIDDEN, Boolean.TRUE,\n";
        if ((beanflags & DocBeanInfo.PREFERRED) !=0)
            code += "                 sun.swing.BeanInfoUtils.PREFERRED, Boolean.TRUE,\n";
        if (!(dbi.customizerclass.equals("null")))
            code += "            sun.swing.BeanInfoUtils.CUSTOMIZERCLASS, " + dbi.customizerclass + ".class,\n";
        if (dbi.attribs != null)  {
            code += genAttributes(dbi.attribs);
        }
        return code;
    }
    private String genAttributes(HashMap attribs)  {
        StringBuffer code = new StringBuffer();
        String key;
        String value;
        Iterator iterator = attribs.keySet().iterator();
        while(iterator.hasNext())  {
            key = (String)iterator.next();
            value = (String)attribs.get(key);
            if (value.equals("true") || value.equals("false"))  {
                if(value.equals("true"))
                    value = "Boolean.TRUE";
                else
                    value = "Boolean.FALSE";
                code.append("              \"").append(key).append("\", ").append(value).append(",\n");
            } else {
                code.append("              \"").append(key).append("\", \"").append(value).append("\",\n");
            }
        }
        return code.toString();
    }
    private String genEnumeration(String propName, HashMap enums)  {
        String objectName = propName + "Enumeration";
        String key;
        String value;
        StringBuffer code = new StringBuffer("\n\t\tObject[] ");
        code.append(objectName).append(" = new Object[] { \n");
        Iterator iterator = enums.keySet().iterator();
        while(iterator.hasNext())  {
            key = (String)iterator.next();
            value = (String)enums.get(key);
            code.append("\t\t\t\"").append(key).append("\" ,   new Integer(");
            code.append(value).append("), \"").append(value).append("\",\n");
        }
        code.replace(code.length() - 2, code.length(), "\n\t\t};\n");
        enumcode += code.toString();
        return "         \"enumerationValues\", " + objectName + ",\n";
    }
    private String genPropertyDescriptors(BeanInfo info, Hashtable dochash) {
        String code = "";
        enumcode = " "; 
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        boolean hash_match = false;
        DocBeanInfo dbi = null;
        for(int i = 0; i < pds.length; i++) {
            if (pds[i].getReadMethod() != null) {
                code += "\ncreatePropertyDescriptor(\"" + pds[i].getName() + "\", new Object[] {\n";
                if (DEBUG)
                    System.out.println("Introspected propertyDescriptor:  " + pds[i].getName());
                if (dochash.size() > 0 && dochash.containsKey(pds[i].getName())) {
                    dbi = (DocBeanInfo)dochash.remove(pds[i].getName());
                    setDocInfoProps(dbi, pds[i]);
                    hash_match = true;
                    if (DEBUG)
                        System.out.println("DocBeanInfo class exists for propertyDescriptor: " + pds[i].getName() + "\n");
                } else {
                    hash_match = false;
                }
                if (hash_match) {
                    if ((dbi.beanflags & DocBeanInfo.BOUND) != 0) {
                        code += "               sun.swing.BeanInfoUtils.BOUND, Boolean.TRUE,\n";
                    } else {
                        code += "               sun.swing.BeanInfoUtils.BOUND, Boolean.FALSE,\n";
                    }
                }
                if (pds[i].isConstrained()) {
                    code += "         sun.swing.BeanInfoUtils.CONSTRAINED, Boolean.TRUE,\n";
                }
                if (pds[i].getPropertyEditorClass() != null) {
                    String className = pds[i].getPropertyEditorClass().getName();
                    code += " sun.swing.BeanInfoUtils.PROPERTYEDITORCLASS, " + className + ".class,\n";
                } else if ((hash_match) && (!(dbi.propertyeditorclass.equals("null")))) {
                    code += " sun.swing.BeanInfoUtils.PROPERTYEDITORCLASS, " + dbi.propertyeditorclass + ".class,\n";
                }
                if ((hash_match) && (!(dbi.customizerclass.equals("null")))) {
                    code += " sun.swing.BeanInfoUtils.CUSTOMIZERCLASS, " + dbi.customizerclass + ".class,\n";
                }
                if ((hash_match) && (dbi.enums != null))  {
                    code += genEnumeration(pds[i].getName(), dbi.enums);
                }
                if (!pds[i].getDisplayName().equals(pds[i].getName())) {
                    code += "         sun.swing.BeanInfoUtils.DISPLAYNAME, \"" + pds[i].getDisplayName() + "\",\n";
                }
                if (pds[i].isExpert()) {
                    code += "              sun.swing.BeanInfoUtils.EXPERT, Boolean.TRUE,\n";
                }
                if (pds[i].isHidden()) {
                    code += "              sun.swing.BeanInfoUtils.HIDDEN, Boolean.TRUE,\n";
                }
                if (pds[i].isPreferred()) {
                    code += "           sun.swing.BeanInfoUtils.PREFERRED, Boolean.TRUE,\n";
                }
                if (hash_match) {
                    if (dbi.attribs != null)  {
                        code += genAttributes(dbi.attribs);
                    }
                }
                code += "    sun.swing.BeanInfoUtils.SHORTDESCRIPTION, \"" + pds[i].getShortDescription() + "\",\n";
                if (i == (pds.length - 1)) {
                    code += "  }\n)\n";
                } else {
                    code += "  }\n),\n";
                }
            } 
        } 
        return code;
    }
    private void setDocInfoProps(DocBeanInfo dbi, PropertyDescriptor pds) {
        int beanflags = dbi.beanflags;
        if ((beanflags & DocBeanInfo.BOUND) != 0)
            pds.setBound(true);
        if ((beanflags & DocBeanInfo.EXPERT) != 0)
            pds.setExpert(true);
        if ((beanflags & DocBeanInfo.CONSTRAINED) != 0)
            pds.setConstrained(true);
        if ((beanflags & DocBeanInfo.HIDDEN) !=0)
            pds.setHidden(true);
        if ((beanflags & DocBeanInfo.PREFERRED) !=0)
            pds.setPreferred(true);
        if (!(dbi.desc.equals("null"))){
            pds.setShortDescription(dbi.desc);
        }
        if (!(dbi.displayname.equals("null"))){
            pds.setDisplayName(dbi.displayname);
        }
    }
    public void genBeanInfo(String packageName, String classname, Hashtable dochash) {
        String beanClassName = "JInternalFrame";
        String beanClassObject = "javax.swing.JInternalFrame.class";
        String beanDescription = "<A description of this component>.";
        String beanPropertyDescriptors = "<createSwingPropertyDescriptor code>";
        String classPropertyDescriptors = "<createSwingClassPropertyDescriptor code>";
        Class cls = getClass(packageName, classname);
        if (cls == null){
            messageAndExit("Can't find class: " + classname);
        }
        PrintStream out = initOutputFile(classname);
        BeanInfo beanInfo = null;
        BeanDescriptor beanDescriptor = null;
        try {
            if (cls == javax.swing.JComponent.class)  {
                beanInfo = Introspector.getBeanInfo(cls);
            } else {
                beanInfo = Introspector.getBeanInfo(cls, cls.getSuperclass());
            }
            beanDescriptor = beanInfo.getBeanDescriptor();
            beanDescription = beanDescriptor.getShortDescription();
        } catch (IntrospectionException e) {
            messageAndExit("Introspection failed for " + cls.getName() + " " + e);
        }
        beanClassName = beanDescriptor.getName();
        beanClassObject = cls.getName() + ".class";
        if (DEBUG){
            System.out.println(">>>>GenSwingBeanInfo class: "  + beanClassName);
        }
        if (dochash.size() > 0) {
            if (dochash.containsKey(beanClassName)) {
                    DocBeanInfo dbi = (DocBeanInfo)dochash.remove(beanClassName);
                    classPropertyDescriptors = genBeanDescriptor(dbi);
                    if (DEBUG)
                        System.out.println("ClassPropertyDescriptors: " + classPropertyDescriptors);
                    if (!(dbi.desc.equals("null")))
                        beanDescription = dbi.desc;
            } else
                    beanDescription = beanDescriptor.getShortDescription();
        } else
            beanDescription = beanDescriptor.getShortDescription();
        beanPropertyDescriptors = genPropertyDescriptors(beanInfo,dochash);
        int currentIndex = 0;
        String template = loadTemplate();
        while (currentIndex < template.length()) {
            int tokenStart = template.indexOf("@(", currentIndex);
            if (tokenStart != -1) {
                out.print(template.substring(currentIndex, tokenStart));
                int tokenEnd = template.indexOf(")", tokenStart);
                if (tokenEnd == -1) {
                    messageAndExit("Bad @(<token>) beginning at " + tokenStart);
                }
                String token = template.substring(tokenStart+2, tokenEnd);
                if (token.equals(TOK_BEANCLASS)) {
                    out.print(beanClassName);
                } else if (token.equals(TOK_CLASSDESC)) {
                    if (!(classPropertyDescriptors.equals("<createSwingClassPropertyDescriptor code>"))) {
                        printDescriptors(out, classPropertyDescriptors, template, tokenStart);
                    }
                } else if (token.equals(TOK_BEANPACKAGE)){
                    out.print(packageName);
                } else if (token.equals(TOK_BEANOBJECT)) {
                    out.print(beanClassObject);
                } else if (token.equals(TOK_BEANDESC)) {
                    out.print(beanDescription);
                } else if (token.equals(TOK_ENUMVARS)){
                    out.print(enumcode);
                } else if (token.equals(TOK_PROPDESC)) {
                    printDescriptors(out, beanPropertyDescriptors, template, tokenStart);
                } else if (token.equals("#")) {
                } else {
                    messageAndExit("Unrecognized token @(" + token + ")");
                }
                currentIndex = tokenEnd + 1;
            } else {
                out.print(template.substring(currentIndex, template.length()));
                break;
            }
        }
        out.close();
    }
    private Class getClass(String packageName, String rootname)  {
        Class cls = null;
        String classname = rootname;
        if (packageName != null || !packageName.equals(""))  {
            classname = packageName + "." + rootname;
        }
        try {
            cls = Class.forName(classname);
        } catch (ClassNotFoundException e) {
        }
        return cls;
    }
    private void printDescriptors(PrintStream out, String s,
                                String template, int tokenStart)  {
            String indent = "";
            for (int i = tokenStart; i >= 0; i--) {
                if (template.charAt(i) == '\n') {
                        char[] chars = new char[tokenStart - i];
                        for (int j = 0; j < chars.length; j++) {
                            chars[j] = ' ';
                        }
                        indent = new String(chars);
                        break;
                }
            }
            int i = 0;
            while(i < s.length()) {
                int nlIndex = s.indexOf('\n', i);
                out.print(s.substring(i, nlIndex+1));
                out.print(indent);
                i = nlIndex + 1;
            }
    }
}
