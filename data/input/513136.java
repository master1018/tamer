public class RootDocToXML {
    public RootDocToXML() {
    }
    public static boolean writeXML(RootDoc root) {
    	String tempFileName = outputFileName;
    	if (outputDirectory != null) {
	    tempFileName = outputDirectory;
	    if (!tempFileName.endsWith(JDiff.DIR_SEP)) 
		tempFileName += JDiff.DIR_SEP;
	    tempFileName += outputFileName;
    	}
        try {
            FileOutputStream fos = new FileOutputStream(tempFileName);
            outputFile = new PrintWriter(fos);
            System.out.println("JDiff: writing the API to file '" + tempFileName + "'...");
            if (root.specifiedPackages().length != 0 || root.specifiedClasses().length != 0) {
                RootDocToXML apiWriter = new RootDocToXML();
                apiWriter.emitXMLHeader();
                apiWriter.logOptions();
                apiWriter.processPackages(root);
                apiWriter.emitXMLFooter();
            }
            outputFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + tempFileName);
            System.out.println("Error: " +  e.getMessage());
            System.exit(1);
        }
        if (XMLToAPI.validateXML) {
            writeXSD();
        }
        return true;
    }
    public static void writeXSD() {
        String xsdFileName = outputFileName;
        if (outputDirectory == null) {
	    int idx = xsdFileName.lastIndexOf('\\');
	    int idx2 = xsdFileName.lastIndexOf('/');
	    if (idx == -1 && idx2 == -1) {
		xsdFileName = "";
	    } else if (idx == -1 && idx2 != -1) {
		xsdFileName = xsdFileName.substring(0, idx2);
	    } else if (idx != -1  && idx2 == -1) {
		xsdFileName = xsdFileName.substring(0, idx);
	    } else if (idx != -1  && idx2 != -1) {
		int max = idx2 > idx ? idx2 : idx;
		xsdFileName = xsdFileName.substring(0, max);
	    }
	} else {
	    xsdFileName = outputDirectory;
	    if (!xsdFileName.endsWith(JDiff.DIR_SEP)) 
		 xsdFileName += JDiff.DIR_SEP;
	}
        xsdFileName += "api.xsd";
        try {
            FileOutputStream fos = new FileOutputStream(xsdFileName);
            PrintWriter xsdFile = new PrintWriter(fos);
            xsdFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
            xsdFile.println("<xsd:schema xmlns:xsd=\"http:
            xsdFile.println("");
            xsdFile.println("<xsd:annotation>");
            xsdFile.println("  <xsd:documentation>");
            xsdFile.println("  Schema for JDiff API representation.");
            xsdFile.println("  </xsd:documentation>");
            xsdFile.println("</xsd:annotation>");
            xsdFile.println();
            xsdFile.println("<xsd:element name=\"api\" type=\"apiType\"/>");
            xsdFile.println("");
            xsdFile.println("<xsd:complexType name=\"apiType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"package\" type=\"packageType\" minOccurs='1' maxOccurs='unbounded'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"jdversion\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"packageType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:choice maxOccurs='unbounded'>");
            xsdFile.println("      <xsd:element name=\"class\" type=\"classType\"/>");
            xsdFile.println("      <xsd:element name=\"interface\" type=\"classType\"/>");
            xsdFile.println("    </xsd:choice>");
            xsdFile.println("    <xsd:element name=\"doc\" type=\"xsd:string\" minOccurs='0' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"classType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"implements\" type=\"interfaceTypeName\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"constructor\" type=\"constructorType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"method\" type=\"methodType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"field\" type=\"fieldType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"doc\" type=\"xsd:string\" minOccurs='0' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"extends\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"abstract\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"src\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"static\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"final\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"deprecated\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"visibility\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"interfaceTypeName\">");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"constructorType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"exception\" type=\"exceptionType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"doc\" type=\"xsd:string\" minOccurs='0' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"type\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"src\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"static\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"final\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"deprecated\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"visibility\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"paramsType\">");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"type\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"exceptionType\">");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"type\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"methodType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"param\" type=\"paramsType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"exception\" type=\"exceptionType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"doc\" type=\"xsd:string\" minOccurs='0' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"return\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"abstract\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"native\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"synchronized\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"src\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"static\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"final\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"deprecated\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"visibility\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"fieldType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"doc\" type=\"xsd:string\" minOccurs='0' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"type\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"transient\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"volatile\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"value\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"src\" type=\"xsd:string\" use='optional'/>");
            xsdFile.println("  <xsd:attribute name=\"static\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"final\" type=\"xsd:boolean\"/>");
            xsdFile.println("  <xsd:attribute name=\"deprecated\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"visibility\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("</xsd:schema>");
            xsdFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + xsdFileName);
            System.out.println("Error: " +  e.getMessage());
            System.exit(1);
        }
    }
    public void logOptions() {
        outputFile.print("<!-- ");
        outputFile.print(" Command line arguments = " + Options.cmdOptions);
        outputFile.println(" -->");
    }
    public void processPackages(RootDoc root) {
        PackageDoc[] specified_pd = root.specifiedPackages();
	Map pdl = new TreeMap();
        for (int i = 0; specified_pd != null && i < specified_pd.length; i++) {
	    pdl.put(specified_pd[i].name(), specified_pd[i]);
	}
        ClassDoc[] cd = root.specifiedClasses();
	Map classesToUse = new HashMap();
        for (int i = 0; cd != null && i < cd.length; i++) {
	    PackageDoc cpd = cd[i].containingPackage();
	    if (cpd == null && !packagesOnly) {
		cpd = root.packageNamed("anonymous");
	    }
            String pkgName = cpd.name();
            String className = cd[i].name();
	    if (trace) System.out.println("Found package " + pkgName + " for class " + className);
	    if (!pdl.containsKey(pkgName)) {
		if (trace) System.out.println("Adding new package " + pkgName);
		pdl.put(pkgName, cpd);
	    }
	    List classes;
	    if (classesToUse.containsKey(pkgName)) {
		classes = (ArrayList) classesToUse.get(pkgName);
	    } else {
		classes = new ArrayList();
	    }
	    classes.add(cd[i]);
	    classesToUse.put(pkgName, classes);
	}
	PackageDoc[] pd = (PackageDoc[]) pdl.values().toArray(new PackageDoc[0]);
        for (int i = 0; pd != null && i < pd.length; i++) {
            String pkgName = pd[i].name();
            if (!shownElement(pd[i], null))
                continue;
            if (trace) System.out.println("PROCESSING PACKAGE: " + pkgName);
            outputFile.println("<package name=\"" + pkgName + "\">");
            int tagCount = pd[i].tags().length;
            if (trace) System.out.println("#tags: " + tagCount);
            List classList;
	    if (classesToUse.containsKey(pkgName)) {
		System.out.println("Using the specified classes");
		classList = (ArrayList) classesToUse.get(pkgName);
	    } else {
		classList = new LinkedList(Arrays.asList(pd[i].allClasses()));
	    }
            Collections.sort(classList);
            ClassDoc[] classes = new ClassDoc[classList.size()];
            classes = (ClassDoc[])classList.toArray(classes);
            processClasses(classes, pkgName);
            addPkgDocumentation(root, pd[i], 2);
            outputFile.println("</package>");
        }
    } 
    public void processClasses(ClassDoc[] cd, String pkgName) {
        if (cd.length == 0)
            return;
        if (trace) System.out.println("PROCESSING CLASSES, number=" + cd.length);
        for (int i = 0; i < cd.length; i++) {
            String className = cd[i].name();
            if (trace) System.out.println("PROCESSING CLASS/IFC: " + className);
            if (!shownElement(cd[i], classVisibilityLevel))
                continue;
            boolean isInterface = false;
            if (cd[i].isInterface())
                isInterface = true;
            if (isInterface) {
                outputFile.println("  <!-- start interface " + pkgName + "." + className + " -->");
                outputFile.print("  <interface name=\"" + className + "\"");
            } else {
                outputFile.println("  <!-- start class " + pkgName + "." + className + " -->");
                outputFile.print("  <class name=\"" + className + "\"");
            }
            Type parent = cd[i].superclassType();
            if (parent != null)
                outputFile.println(" extends=\"" + buildEmittableTypeString(parent) + "\"");
            outputFile.println("    abstract=\"" + cd[i].isAbstract() + "\"");
            addCommonModifiers(cd[i], 4);
            outputFile.println(">");
            processInterfaces(cd[i].interfaceTypes());
            processConstructors(cd[i].constructors());
            processMethods(cd[i], cd[i].methods());
            processFields(cd[i].fields());
            addDocumentation(cd[i], 4);
            if (isInterface) {
                outputFile.println("  </interface>");
                outputFile.println("  <!-- end interface " + pkgName + "." + className + " -->");
            } else {
                outputFile.println("  </class>");
                outputFile.println("  <!-- end class " + pkgName + "." + className + " -->");
            }
        }
    }
    public void addCommonModifiers(ProgramElementDoc ped, int indent) {
        addSourcePosition(ped, indent);
        for (int i = 0; i < indent; i++) outputFile.print(" ");
        outputFile.print("static=\"" + ped.isStatic() + "\"");
        outputFile.print(" final=\"" + ped.isFinal() + "\"");
        String visibility = null;
        if (ped.isPublic())
            visibility = "public";
        else if (ped.isProtected())
            visibility = "protected";
        else if (ped.isPackagePrivate())
            visibility = "package";
        else if (ped.isPrivate())
            visibility = "private";
        outputFile.println(" visibility=\"" + visibility + "\"");
        for (int i = 0; i < indent; i++) outputFile.print(" ");
        boolean isDeprecated = false;
        Tag[] ta = ((Doc)ped).tags("deprecated");
        if (ta.length != 0) {
            isDeprecated = true;
        }
        if (ta.length > 1) {
            System.out.println("JDiff: warning: multiple @deprecated tags found in comments for " + ped.name() + ". Using the first one only.");
            System.out.println("Text is: " + ((Doc)ped).getRawCommentText());
        }
        if (isDeprecated) {
            String text = ta[0].text(); 
            if (text != null && text.compareTo("") != 0) {
                int idx = endOfFirstSentence(text);
                if (idx == 0) {
                    outputFile.print("deprecated=\"deprecated, no comment\"");
                } else {
                    String fs = null;
                    if (idx == -1)
                        fs = text;
                    else
                        fs = text.substring(0, idx+1);
                    String st = API.hideHTMLTags(fs);
                    outputFile.print("deprecated=\"" + st + "\"");
                }
            } else {
                outputFile.print("deprecated=\"deprecated, no comment\"");
            }
        } else {
            outputFile.print("deprecated=\"not deprecated\"");
        }
    } 
    public void addSourcePosition(ProgramElementDoc ped, int indent) {
        if (!addSrcInfo)
            return;
        if (JDiff.javaVersion.startsWith("1.1") || 
            JDiff.javaVersion.startsWith("1.2") || 
            JDiff.javaVersion.startsWith("1.3")) {
            return; 
        }
        try {
            Class c = ProgramElementDoc.class;
            Method m = c.getMethod("position", (Class[]) null);
            Object sp = m.invoke(ped, (Object[]) null);
            if (sp != null) {
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("src=\"" + sp + "\"");
            }
        } catch (NoSuchMethodException e2) {
            System.err.println("Error: method \"position\" not found");
            e2.printStackTrace();
        } catch (IllegalAccessException e4) {
            System.err.println("Error: class not permitted to be instantiated");
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            System.err.println("Error: method \"position\" could not be invoked");
            e5.printStackTrace();
        } catch (Exception e6) {
            System.err.println("Error: ");
            e6.printStackTrace();
        }
    }
    public void processInterfaces(Type[] ifaces) {
        if (trace) System.out.println("PROCESSING INTERFACES, number=" + ifaces.length);
        for (int i = 0; i < ifaces.length; i++) {
            String ifaceName = buildEmittableTypeString(ifaces[i]);
            if (trace) System.out.println("PROCESSING INTERFACE: " + ifaceName);
            outputFile.println("    <implements name=\"" + ifaceName + "\"/>");
        }
    }
    public void processConstructors(ConstructorDoc[] ct) {
        if (trace) System.out.println("PROCESSING CONSTRUCTORS, number=" + ct.length);
        for (int i = 0; i < ct.length; i++) {
            String ctorName = ct[i].name();
            if (trace) System.out.println("PROCESSING CONSTRUCTOR: " + ctorName);
            if (!shownElement(ct[i], memberVisibilityLevel))
                continue;
            outputFile.print("    <constructor name=\"" + ctorName + "\"");
            Parameter[] params = ct[i].parameters();
            boolean first = true;
            if (params.length != 0) {
                outputFile.print(" type=\"");
                for (int j = 0; j < params.length; j++) {
                    if (!first)
                        outputFile.print(", ");
                    emitType(params[j].type());
                    first = false;
                }
                outputFile.println("\"");
            } else
                outputFile.println();
            addCommonModifiers(ct[i], 6);
            outputFile.println(">");
            processExceptions(ct[i].thrownExceptions());
            addDocumentation(ct[i], 6);
            outputFile.println("    </constructor>");
        }
    }
    public void processExceptions(ClassDoc[] cd) {
        if (trace) System.out.println("PROCESSING EXCEPTIONS, number=" + cd.length);
        for (int i = 0; i < cd.length; i++) {
            String exceptionName = cd[i].name();
            if (trace) System.out.println("PROCESSING EXCEPTION: " + exceptionName);
            outputFile.print("      <exception name=\"" + exceptionName + "\" type=\"");
            emitType(cd[i]);
            outputFile.println("\"/>");
        }
    }
    public void processMethods(ClassDoc cd, MethodDoc[] md) {
        if (trace) System.out.println("PROCESSING " +cd.name()+" METHODS, number = " + md.length);
        for (int i = 0; i < md.length; i++) {
            String methodName = md[i].name();
            if (trace) System.out.println("PROCESSING METHOD: " + methodName);
            if (methodName.startsWith("<"))
                continue;
            if (!shownElement(md[i], memberVisibilityLevel))
                continue;
            outputFile.print("    <method name=\"" + methodName + "\"");
            com.sun.javadoc.Type retType = md[i].returnType();
            if (retType.qualifiedTypeName().compareTo("void") == 0) {
                outputFile.println();
            } else {
                outputFile.print(" return=\"");
                emitType(retType);
                outputFile.println("\"");
            }
            outputFile.print("      abstract=\"" + md[i].isAbstract() + "\"");
            outputFile.print(" native=\"" + md[i].isNative() + "\"");
            outputFile.println(" synchronized=\"" + md[i].isSynchronized() + "\"");
            addCommonModifiers(md[i], 6);
            outputFile.println(">");
            Parameter[] params = md[i].parameters();
            for (int j = 0; j < params.length; j++) {
                outputFile.print("      <param name=\"" + params[j].name() + "\"");
                outputFile.print(" type=\"");
                emitType(params[j].type());
                outputFile.println("\"/>");
            }
            processExceptions(md[i].thrownExceptions());
            addDocumentation(md[i], 6);
            outputFile.println("    </method>");
        }
    }
    public void processFields(FieldDoc[] fd) {
        if (trace) System.out.println("PROCESSING FIELDS, number=" + fd.length);
        for (int i = 0; i < fd.length; i++) {
            String fieldName = fd[i].name();
            if (trace) System.out.println("PROCESSING FIELD: " + fieldName);
            if (!shownElement(fd[i], memberVisibilityLevel))
                continue;
            outputFile.print("    <field name=\"" + fieldName + "\"");
            outputFile.print(" type=\"");
            emitType(fd[i].type());
            outputFile.println("\"");
            outputFile.print("      transient=\"" + fd[i].isTransient() + "\"");
            outputFile.println(" volatile=\"" + fd[i].isVolatile() + "\"");
            addCommonModifiers(fd[i], 6);
            outputFile.println(">");
            addDocumentation(fd[i], 6);
            outputFile.println("    </field>");
        }
    }
    public void emitType(com.sun.javadoc.Type type) {
        String name = buildEmittableTypeString(type);
        if (name == null)
            return;
        outputFile.print(name);
    }
    private String buildEmittableTypeString(com.sun.javadoc.Type type) {
        if (type == null) {
    	    return null;
        }
      String name = type.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;");
      if (name.startsWith("<<ambiguous>>")) {
          name = name.substring(13);
      }
      return name;
    }    
    public void emitXMLHeader() {
        outputFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
        outputFile.println("<!-- Generated by the JDiff Javadoc doclet -->");
        outputFile.println("<!-- (" + JDiff.jDiffLocation + ") -->");
        outputFile.println("<!-- on " + new Date() + " -->");
        outputFile.println();
        outputFile.println("<api");
        outputFile.println("  xmlns:xsi='" + baseURI + "/2001/XMLSchema-instance'");
        outputFile.println("  xsi:noNamespaceSchemaLocation='api.xsd'");
        outputFile.println("  name=\"" + apiIdentifier + "\"");
        outputFile.println("  jdversion=\"" + JDiff.version + "\">");
        outputFile.println();
    }
    public void emitXMLFooter() {
        outputFile.println();
        outputFile.println("</api>");
    }
    public boolean shownElement(Doc doc, String visLevel) {
	if (doExclude && excludeTag != null && doc != null) {
            String rct = doc.getRawCommentText();
            if (rct != null && rct.indexOf(excludeTag) != -1) {
                return false;
	    }
	}  
	if (visLevel == null) {
	    return true;
	}
	ProgramElementDoc ped = null;
	if (doc instanceof ProgramElementDoc) {
	    ped = (ProgramElementDoc)doc;
	}
        if (visLevel.compareTo("private") == 0)
            return true;
        if (visLevel.compareTo("package") == 0)
            return !ped.isPrivate();
        if (visLevel.compareTo("protected") == 0)
            return !(ped.isPrivate() || ped.isPackagePrivate());
        if (visLevel.compareTo("public") == 0)
            return ped.isPublic();
        return false;
    } 
    public String stripNonPrintingChars(String s, Doc doc) {
        if (!stripNonPrintables)
            return s;
        char[] sa = s.toCharArray();
        for (int i = 0; i < sa.length; i++) {
            char c = sa[i];
            if (Character.isLetterOrDigit(c))
                continue;
            if (c == ' ' ||
                c == '.' ||
                c == ',' ||
                c == '\r' ||
                c == '\t' ||
                c == '\n' ||
                c == '!' ||
                c == '?' ||
                c == ';' ||
                c == ':' ||
                c == '[' ||
                c == ']' ||
                c == '(' ||
                c == ')' ||
                c == '~' ||
                c == '@' ||
                c == '#' ||
                c == '$' ||
                c == '%' ||
                c == '^' ||
                c == '&' ||
                c == '*' ||
                c == '-' ||
                c == '=' ||
                c == '+' ||
                c == '_' ||
                c == '|' ||
                c == '\\' ||
                c == '/' ||
                c == '\'' ||
                c == '}' ||
                c == '{' ||
                c == '"' ||
                c == '<' ||
                c == '>' ||
                c == '`'
                )
                continue;
            sa[i] = '#';
        }
        return new String(sa);
    }
    public boolean inRange(int val, int min, int max) {
        if (val < min)
            return false;
        if (val > max)
            return false;
        return true;
    }
    public void addDocumentation(ProgramElementDoc ped, int indent) {
        String rct = ((Doc)ped).getRawCommentText();
        if (rct != null) {
            rct = stripNonPrintingChars(rct, (Doc)ped);
            rct = rct.trim();
            if (rct.compareTo("") != 0 && 
                rct.indexOf(Comments.placeHolderText) == -1 &&
                rct.indexOf("InsertOtherCommentsHere") == -1) {
                int idx = endOfFirstSentence(rct);
                if (idx == 0)
                    return;
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("<doc>");
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                String firstSentence = null;
                if (idx == -1)
                    firstSentence = rct;
                else
                    firstSentence = rct.substring(0, idx+1);
                boolean checkForAts = false;
                if (checkForAts && firstSentence.indexOf("@") != -1 && 
                    firstSentence.indexOf("@link") == -1) {
                    System.out.println("Warning: @ tag seen in comment: " + 
                                       firstSentence);
                }
                String firstSentenceNoTags = API.stuffHTMLTags(firstSentence);
                outputFile.println(firstSentenceNoTags);
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("</doc>");
            }
        }
    }
    public void addPkgDocumentation(RootDoc root, PackageDoc pd, int indent) {
        String rct = null;
        String filename = pd.name();
        try {
            String srcLocation = null;
            String[][] options = root.options();
            for (int opt = 0; opt < options.length; opt++) {
                if ((options[opt][0]).compareTo("-sourcepath") == 0) {
                    srcLocation = options[opt][1];
                    break;
                }
            }
            filename = filename.replace('.', JDiff.DIR_SEP.charAt(0));
            if (srcLocation != null) {
                if (srcLocation.startsWith("..")) {
                    String curDir = System.getProperty("user.dir");
                    while (srcLocation.startsWith("..")) {
                        srcLocation = srcLocation.substring(3);
                        int idx = curDir.lastIndexOf(JDiff.DIR_SEP);
                        curDir = curDir.substring(0, idx+1);
                    }
                    srcLocation = curDir + srcLocation;
                }
                filename = srcLocation + JDiff.DIR_SEP + filename;
            }
            filename += JDiff.DIR_SEP + "package.htm";
            File f2 = new File(filename);
            if (!f2.exists()) {
                filename += "l";
            }
            FileInputStream f = new FileInputStream(filename);
            BufferedReader d = new BufferedReader(new InputStreamReader(f));
            String str = d.readLine();
	    boolean inBody = false;
	    while(str != null) {
                if (!inBody) {
		    if (str.toLowerCase().trim().startsWith("<body")) {
			inBody = true;
		    }
		    str = d.readLine(); 
		    continue; 
		} else {
		    if (str.toLowerCase().trim().startsWith("</body")) {
			inBody = false;
			continue; 
		    }
		}
                if (rct == null)
                    rct = str + "\n";
                else
                    rct += str + "\n";
                str = d.readLine();
            }
        }  catch(java.io.FileNotFoundException e) {
            if (trace)
                System.out.println("No package level documentation file at '" + filename + "'");
        } catch(java.io.IOException e) {
            System.out.println("Error reading file \"" + filename + "\": " + e.getMessage());
            System.exit(5);
        }     
        if (rct != null) {
            rct = stripNonPrintingChars(rct, (Doc)pd);
            rct = rct.trim();
            if (rct.compareTo("") != 0 &&
                rct.indexOf(Comments.placeHolderText) == -1 &&
                rct.indexOf("InsertOtherCommentsHere") == -1) {
                int idx = endOfFirstSentence(rct);
                if (idx == 0)
                    return;
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("<doc>");
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                String firstSentence = null;
                if (idx == -1)
                    firstSentence = rct;
                else
                    firstSentence = rct.substring(0, idx+1);
                String firstSentenceNoTags = API.stuffHTMLTags(firstSentence);
                outputFile.println(firstSentenceNoTags);
                for (int i = 0; i < indent; i++) outputFile.print(" ");
                outputFile.println("</doc>");
            }
        }
    }
    public static int endOfFirstSentence(String text) {
        return endOfFirstSentence(text, true);
    }
    public static int endOfFirstSentence(String text, boolean writingToXML) {
        if (saveAllDocs && writingToXML)
            return -1;
	int textLen = text.length();
	if (textLen == 0)
	    return 0;
        int index = -1;
        int fromindex = 0;
        int ellipsis = text.indexOf(". . ."); 
        if (ellipsis != -1)
            fromindex = ellipsis + 5;
        int i = 0;
        while (i < textLen && text.charAt(i) == ' ') {
            i++;
        }
        if (text.charAt(i) == '@' && fromindex < textLen-1)
            fromindex = i + 1;
        index = minIndex(index, text.indexOf("? ", fromindex));
        index = minIndex(index, text.indexOf("?\t", fromindex));
        index = minIndex(index, text.indexOf("?\n", fromindex));
        index = minIndex(index, text.indexOf("?\r", fromindex));
        index = minIndex(index, text.indexOf("?\f", fromindex));
        index = minIndex(index, text.indexOf("! ", fromindex));
        index = minIndex(index, text.indexOf("!\t", fromindex));
        index = minIndex(index, text.indexOf("!\n", fromindex));
        index = minIndex(index, text.indexOf("!\r", fromindex));
        index = minIndex(index, text.indexOf("!\f", fromindex));
        index = minIndex(index, text.indexOf(". ", fromindex));
        index = minIndex(index, text.indexOf(".\t", fromindex));
        index = minIndex(index, text.indexOf(".\n", fromindex));
        index = minIndex(index, text.indexOf(".\r", fromindex));
        index = minIndex(index, text.indexOf(".\f", fromindex));
        index = minIndex(index, text.indexOf("@param", fromindex));
        index = minIndex(index, text.indexOf("@return", fromindex));
        index = minIndex(index, text.indexOf("@throw", fromindex));
        index = minIndex(index, text.indexOf("@serial", fromindex));
        index = minIndex(index, text.indexOf("@exception", fromindex));
        index = minIndex(index, text.indexOf("@deprecate", fromindex));
        index = minIndex(index, text.indexOf("@author", fromindex));
        index = minIndex(index, text.indexOf("@since", fromindex));
        index = minIndex(index, text.indexOf("@see", fromindex));
        index = minIndex(index, text.indexOf("@version", fromindex));
        if (doExclude && excludeTag != null)
            index = minIndex(index, text.indexOf(excludeTag));
        index = minIndex(index, text.indexOf("@vtexclude", fromindex));
        index = minIndex(index, text.indexOf("@vtinclude", fromindex));
        index = minIndex(index, text.indexOf("<p>", 2)); 
        index = minIndex(index, text.indexOf("<P>", 2)); 
        index = minIndex(index, text.indexOf("<blockquote", 2));  
        index = minIndex(index, text.indexOf("<pre", fromindex)); 
        if (index != -1 &&  
            (text.charAt(index) == '@' || text.charAt(index) == '<')) {
            if (index != 0)
                index--;
        }
        return index;
    }
    public static int minIndex(int i, int j) {
        if (i == -1) return j;
        if (j == -1) return i;
        return Math.min(i,j);
    }
    public static String outputFileName = null;
    public static String apiIdentifier = null;
    private static PrintWriter outputFile = null;
    public static String outputDirectory = null;
    public static String classVisibilityLevel = "protected";
    public static String memberVisibilityLevel = "protected";
    public static boolean saveAllDocs = true;
    public static boolean doExclude = false;
    public static String excludeTag = null;
    public static String baseURI = "http:
    static boolean stripNonPrintables = true;
    static boolean addSrcInfo = false;
    static boolean packagesOnly = false;
    private static boolean trace = false;
} 
