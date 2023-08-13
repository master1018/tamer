class APIHandler extends DefaultHandler {
    public API api_;
    public APIHandler(API api, boolean createGlobalComments) {
        api_ = api;
        createGlobalComments_ = createGlobalComments;
        tagStack = new LinkedList();
    }   
    public static boolean checkIsSentence = false;
    private String currentElement = null;
    private boolean createGlobalComments_ = false;
    private boolean inDoc = false;
    private String currentText = null;
    private String currentDepText = null;
    private LinkedList tagStack = null;
    public void startDocument() {
    }
    public void endDocument() {
        if (trace)
            api_.dump();
        System.out.println(" finished");
    }
    public void startElement(java.lang.String uri, java.lang.String localName,
                             java.lang.String qName, Attributes attributes) {
	if (localName.equals(""))
	    localName = qName;
        if (localName.compareTo("api") == 0) {
            String apiName = attributes.getValue("name");
            String version = attributes.getValue("jdversion"); 
            XMLToAPI.nameAPI(apiName);
        } else if (localName.compareTo("package") == 0) {
            currentElement = localName;
            String pkgName = attributes.getValue("name");
            XMLToAPI.addPackage(pkgName);
        } else if (localName.compareTo("class") == 0) {
            currentElement = localName;
            String className = attributes.getValue("name");
            String parentName = attributes.getValue("extends");
            boolean isAbstract = false;
            if (attributes.getValue("abstract").compareTo("true") == 0)
                isAbstract = true;
            XMLToAPI.addClass(className, parentName, isAbstract, getModifiers(attributes));
        } else if (localName.compareTo("interface") == 0) {
            currentElement = localName;
            String className = attributes.getValue("name");
            String parentName = attributes.getValue("extends");
            boolean isAbstract = false;
            if (attributes.getValue("abstract").compareTo("true") == 0)
                isAbstract = true;
            XMLToAPI.addInterface(className, parentName, isAbstract, getModifiers(attributes));
        } else if (localName.compareTo("implements") == 0) {
            String interfaceName = attributes.getValue("name");
            XMLToAPI.addImplements(interfaceName);
        } else if (localName.compareTo("constructor") == 0) {
            currentElement = localName;
            String ctorType = attributes.getValue("type");
            XMLToAPI.addCtor(ctorType, getModifiers(attributes));
        } else if (localName.compareTo("method") == 0) {
            currentElement = localName;
            String methodName = attributes.getValue("name");
            String returnType = attributes.getValue("return");
            boolean isAbstract = false;
            if (attributes.getValue("abstract").compareTo("true") == 0)
                isAbstract = true;
            boolean isNative = false;
            if (attributes.getValue("native").compareTo("true") == 0)
                isNative = true;
            boolean isSynchronized = false;
            if (attributes.getValue("synchronized").compareTo("true") == 0)
                isSynchronized = true;
            XMLToAPI.addMethod(methodName, returnType, isAbstract, isNative, 
                               isSynchronized, getModifiers(attributes));
        } else if (localName.compareTo("field") == 0) {
            currentElement = localName;
            String fieldName = attributes.getValue("name");
            String fieldType = attributes.getValue("type");
            boolean isTransient = false;
            if (attributes.getValue("transient").compareTo("true") == 0)
                isTransient = true;
            boolean isVolatile = false;
            if (attributes.getValue("volatile").compareTo("true") == 0)
                isVolatile = true;
            String value = attributes.getValue("value");
            XMLToAPI.addField(fieldName, fieldType, isTransient, isVolatile, 
                              value, getModifiers(attributes));
        } else if (localName.compareTo("param") == 0) {
            String paramName = attributes.getValue("name");
            String paramType = attributes.getValue("type");
            XMLToAPI.addParam(paramName, paramType);
        } else if (localName.compareTo("exception") == 0) {
            String paramName = attributes.getValue("name");
            String paramType = attributes.getValue("type");
            XMLToAPI.addException(paramName, paramType, currentElement);
        } else if (localName.compareTo("doc") == 0) {
            inDoc = true;
            currentText = null;
        } else {
            if (inDoc) {
                addStartTagToText(localName, attributes);
            } else {
                System.out.println("Error: unknown element type: " + localName);
                System.exit(-1);
            }
        }
    }
    public void endElement(java.lang.String uri, java.lang.String localName, 
                           java.lang.String qName) {
	if (localName.equals(""))
	    localName = qName;
        if (localName.compareTo("doc") == 0) {
            inDoc = false;
            addTextToComments();
        } else if (inDoc) {
            addEndTagToText(localName);
        } else if (currentElement.compareTo("constructor") == 0 && 
                   localName.compareTo("constructor") == 0) {
            currentElement = "class";
        } else if (currentElement.compareTo("method") == 0 && 
                   localName.compareTo("method") == 0) {
            currentElement = "class";
        } else if (currentElement.compareTo("field") == 0 && 
                   localName.compareTo("field") == 0) {
            currentElement = "class";
        } else if (currentElement.compareTo("class") == 0 ||
                   currentElement.compareTo("interface") == 0) {
            if (localName.compareTo("class") == 0 || 
                localName.compareTo("interface") == 0) {
                currentElement = "package";
            }
        }
    }
    public void characters(char[] ch, int start, int length) {
         if (inDoc) {
            String chunk = new String(ch, start, length);
            if (currentText == null)
                currentText = chunk;
            else
                currentText += chunk;
         }
    }
    public void addTextToComments() {
        currentText = currentText.trim();        
        if (convertAtLinks) {
            currentText = Comments.convertAtLinks(currentText, currentElement, 
                                                  api_.currPkg_, api_.currClass_);
        }
        if (checkIsSentence && !currentText.endsWith(".") && 
            currentText.compareTo(Comments.placeHolderText) != 0) {
            System.out.println("Warning: text of comment does not end in a period: " + currentText);
        }
        String commentID = null;
        if (currentElement.compareTo("package") == 0) {
            api_.currPkg_.doc_ = currentText;
            commentID = api_.currPkg_.name_;
        } else if (currentElement.compareTo("class") == 0 ||
                   currentElement.compareTo("interface") == 0) {
            api_.currClass_.doc_ = currentText;
            commentID = api_.currPkg_.name_ + "." + api_.currClass_.name_;
        } else if (currentElement.compareTo("constructor") == 0) {
            api_.currCtor_.doc_ = currentText;
            commentID = api_.currPkg_.name_ + "." + api_.currClass_.name_ +
                ".ctor_changed(";
            if (api_.currCtor_.type_.compareTo("void") == 0)
                commentID = commentID + ")";
            else
                commentID = commentID + api_.currCtor_.type_ + ")";
        } else if (currentElement.compareTo("method") == 0) {
            api_.currMethod_.doc_ = currentText;
            commentID = api_.currPkg_.name_ + "." + api_.currClass_.name_ +
                "." + api_.currMethod_.name_ + "_changed(" + 
                api_.currMethod_.getSignature() + ")";
        } else if (currentElement.compareTo("field") == 0) {
            api_.currField_.doc_ = currentText;
            commentID = api_.currPkg_.name_ + "." + api_.currClass_.name_ +
                "." + api_.currField_.name_;
        }            
        if (createGlobalComments_ && commentID != null) {
            String ct = currentText;
            if (currentDepText != null) {
                ct = currentDepText;
                currentDepText = null; 
            }
            String ctOld = (String)(Comments.allPossibleComments.put(commentID, ct));
            if (ctOld != null) {
                System.out.println("Error: duplicate comment id: " + commentID);
                System.exit(5);
            }
        }
    }
    public void addStartTagToText(String localName, Attributes attributes) {
        String currentHTMLTag = localName;
        tagStack.add(currentHTMLTag);
        String tag = "<" + currentHTMLTag;
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            String name = attributes.getLocalName(i);
            String value = attributes.getValue(i);
            tag += " " + name + "=\"" + value+ "\"";
        }
        if (Comments.isMinimizedTag(currentHTMLTag)) {
            tag += "/>";
        } else {
            tag += ">";
        }
        if (currentText == null)
            currentText = tag;
        else
            currentText += tag;
    }
    public void addEndTagToText(String localName) {
        String currentHTMLTag = (String)(tagStack.removeLast());
        if (!Comments.isMinimizedTag(currentHTMLTag))
            currentText += "</" + currentHTMLTag + ">";
    }
    public Modifiers getModifiers(Attributes attributes) {
        Modifiers modifiers = new Modifiers();
        modifiers.isStatic = false;
        if (attributes.getValue("static").compareTo("true") == 0)
            modifiers.isStatic = true;
        modifiers.isFinal = false;
        if (attributes.getValue("final").compareTo("true") == 0)
            modifiers.isFinal = true;
        modifiers.isDeprecated = false;
        String cdt = attributes.getValue("deprecated");
        if (cdt.compareTo("not deprecated") == 0) {
            modifiers.isDeprecated = false;
            currentDepText = null;
        } else if (cdt.compareTo("deprecated, no comment") == 0) {
            modifiers.isDeprecated = true;
            currentDepText = null;
        } else {
            modifiers.isDeprecated = true;
            currentDepText = API.showHTMLTags(cdt);
        }
        modifiers.visibility = attributes.getValue("visibility");
        return modifiers;
    }
    public void warning(SAXParseException e) {
        System.out.println("Warning (" + e.getLineNumber() + "): parsing XML API file:" + e);
        e.printStackTrace();
    }
    public void error(SAXParseException e) {
        System.out.println("Error (" + e.getLineNumber() + "): parsing XML API file:" + e);
        e.printStackTrace();
        System.exit(1);
    }
    public void fatalError(SAXParseException e) {
        System.out.println("Fatal Error (" + e.getLineNumber() + "): parsing XML API file:" + e);
        e.printStackTrace();
        System.exit(1);
    }    
    private static boolean convertAtLinks = true;
    private static boolean trace = false;
}
