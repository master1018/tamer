public class XMLToAPI {
    private static API api_ = null;
    private XMLToAPI() {
    }   
    public static API readFile(String filename, boolean createGlobalComments,
			       String apiName) {
        api_ = new API();
        api_.name_ = apiName; 
        try {
            XMLReader parser = null;
            DefaultHandler handler = new APIHandler(api_, createGlobalComments);
            try {
                String parserName = System.getProperty("org.xml.sax.driver");
                if (parserName == null) {
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                } else {
                    parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                }
            } catch (SAXException saxe) {
                System.out.println("SAXException: " + saxe);
                saxe.printStackTrace();
                System.exit(1);
            }
            if (validateXML) {
                parser.setFeature("http:
                parser.setFeature("http:
                parser.setFeature("http:
            }
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            parser.parse(new InputSource(new FileInputStream(new File(filename))));
        } catch(org.xml.sax.SAXNotRecognizedException snre) {
            System.out.println("SAX Parser does not recognize feature: " + snre);
            snre.printStackTrace();
            System.exit(1);
        } catch(org.xml.sax.SAXNotSupportedException snse) {
            System.out.println("SAX Parser feature is not supported: " + snse);
            snse.printStackTrace();
            System.exit(1);
        } catch(org.xml.sax.SAXException saxe) {
            System.out.println("SAX Exception parsing file '" + filename + "' : " + saxe);
            saxe.printStackTrace();
            System.exit(1);
        } catch(java.io.IOException ioe) {
            System.out.println("IOException parsing file '" + filename + "' : " + ioe);
            ioe.printStackTrace();
            System.exit(1);
        }
        addInheritedElements();
        return api_;
    } 
    public static void addInheritedElements() {
        Iterator iter = api_.packages_.iterator();
        while (iter.hasNext()) {
            PackageAPI pkg = (PackageAPI)(iter.next());
            Iterator iter2 = pkg.classes_.iterator();
            while (iter2.hasNext()) {
                ClassAPI cls = (ClassAPI)(iter2.next());
                if (cls.extends_ != null) {
                    ClassAPI parent = (ClassAPI)api_.classes_.get(cls.extends_);
                    if (parent != null)
                        addInheritedElements(cls, parent, cls.extends_);
                }
                if (cls.implements_.size() != 0) {
                    Iterator iter3 = cls.implements_.iterator();
                    while (iter3.hasNext()) {
                        String implName = (String)(iter3.next());
                        ClassAPI parent = (ClassAPI)api_.classes_.get(implName);
                        if (parent != null)
                            addInheritedElements(cls, parent, implName);
                    }
                }
            } 
        } 
    }
    public static void addInheritedElements(ClassAPI child, ClassAPI parent,
                                            String fqParentName) {
        if (parent.methods_.size() != 0) {
            Iterator iter = parent.methods_.iterator();
            while (iter.hasNext()) {
                MethodAPI m = (MethodAPI)(iter.next());
                boolean overridden = false;
                Iterator iter2 = child.methods_.iterator();
                while (iter2.hasNext()) {
                    MethodAPI localM = (MethodAPI)(iter2.next());
                    if (localM.name_.compareTo(m.name_) == 0 && 
                        localM.getSignature().compareTo(m.getSignature()) == 0)
                        overridden = true;
                }
                if (!overridden && m.inheritedFrom_ == null &&
                    m.modifiers_.visibility != null && 
                    m.modifiers_.visibility.compareTo("private") != 0) {
                    MethodAPI m2 = new MethodAPI(m);
                    m2.inheritedFrom_ = fqParentName;
                    child.methods_.add(m2);
                }
            }            
        }
        if (parent.fields_.size() != 0) {
            Iterator iter = parent.fields_.iterator();
            while (iter.hasNext()) {
                FieldAPI f = (FieldAPI)(iter.next());
                if (child.fields_.indexOf(f) == -1 &&
                    f.inheritedFrom_ == null &&
                    f.modifiers_.visibility != null && 
                    f.modifiers_.visibility.compareTo("private") != 0) {
                    FieldAPI f2 = new FieldAPI(f);
                    f2.inheritedFrom_ = fqParentName;
                    child.fields_.add(f2);
                }
            }            
        }
        if (parent.extends_ != null) {
            ClassAPI parent2 = (ClassAPI)api_.classes_.get(parent.extends_);
            if (parent2 != null)
                addInheritedElements(child, parent2, parent.extends_);
        }
        if (parent.implements_.size() != 0) {
            Iterator iter3 = parent.implements_.iterator();
            while (iter3.hasNext()) {
                String implName = (String)(iter3.next());
                ClassAPI parent2 = (ClassAPI)api_.classes_.get(implName);
                if (parent2 != null)
                    addInheritedElements(child, parent2, implName);
            }
        }
    }
    public static void nameAPI(String name) {
        if (name == null) {
            System.out.println("Error: no API identifier found in the XML file '" + api_.name_ + "'");
            System.exit(3);
        }
        String filename2 = name.replace(' ','_');
        filename2 += ".xml";
        if (filename2.compareTo(api_.name_) != 0) {
            System.out.println("Warning: API identifier in the XML file (" + 
                               name + ") differs from the name of the file '" +
                               api_.name_ + "'");
        }
        api_.name_ = name;
    }
    public static void addPackage(String name) {
        api_.currPkg_ = new PackageAPI(name);
        api_.packages_.add(api_.currPkg_);
    }
    public static void addClass(String name, String parent, 
                                boolean isAbstract,
                                Modifiers modifiers) {
        api_.currClass_ = new ClassAPI(name, parent, false, isAbstract, modifiers);
        api_.currPkg_.classes_.add(api_.currClass_);
        String fqName = api_.currPkg_.name_ + "." + name;
        ClassAPI caOld = (ClassAPI)api_.classes_.put(fqName, api_.currClass_);
        if (caOld != null) {
            System.out.println("Warning: duplicate class : " + fqName + " found. Using the first instance only.");
        }
    }
    public static void addInterface(String name, String parent, 
                                    boolean isAbstract,
                                    Modifiers modifiers) {
        api_.currClass_ = new ClassAPI(name, parent, true, isAbstract, modifiers);
        api_.currPkg_.classes_.add(api_.currClass_);
    }
    public static void addImplements(String name) {
       api_.currClass_.implements_.add(name);
    }
    public static void addCtor(String type, Modifiers modifiers) {
        String t = type;
        if (t == null)
            t = "void";
        api_.currCtor_ = new ConstructorAPI(t, modifiers);
        api_.currClass_.ctors_.add(api_.currCtor_);
    }
    public static void addMethod(String name, String returnType, 
                                 boolean isAbstract, boolean isNative, 
                                 boolean isSynchronized, Modifiers modifiers) {
        String rt = returnType;
        if (rt == null)
            rt = "void";
        api_.currMethod_ = new MethodAPI(name, rt, isAbstract, isNative,
                                         isSynchronized, modifiers);
        api_.currClass_.methods_.add(api_.currMethod_);
    }
    public static void addField(String name, String type, boolean isTransient,
                                boolean isVolatile, String value, Modifiers modifiers) {
        String t = type;
        if (t == null)
            t = "void";
        api_.currField_ = new FieldAPI(name, t, isTransient, isVolatile, value, modifiers);
        api_.currClass_.fields_.add(api_.currField_);
    }
    public static void addParam(String name, String type) {
        String t = type;
        if (t == null)
            t = "void";
        ParamAPI paramAPI = new ParamAPI(name, t);
        api_.currMethod_.params_.add(paramAPI);
    }
    public static void addException(String name, String type, String currElement) {
	String exceptionId = type;
	if (type == null || !showExceptionTypes)
	    exceptionId = name;
        if (currElement.compareTo("method") == 0) {
            if (api_.currMethod_.exceptions_.compareTo("no exceptions") == 0)
                api_.currMethod_.exceptions_ = exceptionId;
            else
                api_.currMethod_.exceptions_ += ", " + exceptionId;
        } else {
            if (api_.currCtor_.exceptions_.compareTo("no exceptions") == 0)
                api_.currCtor_.exceptions_ = exceptionId;
            else
                api_.currCtor_.exceptions_ += ", " + exceptionId;
        }
    }
    public static boolean validateXML = false;
    private static boolean showExceptionTypes = true;
}  
