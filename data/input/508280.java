public class Comments {
    public static Hashtable allPossibleComments = new Hashtable();
    private static Comments oldComments_ = null;
    public Comments() {
        commentsList_ = new ArrayList(); 
    }   
    public List commentsList_ = null; 
    public static Comments readFile(String filename) {
        if (XMLToAPI.validateXML) {
            writeXSD(filename);
        }
        File f = new File(filename);
        if (!f.exists())
            return null;
        oldComments_ = new Comments();
        try {
            DefaultHandler handler = new CommentsHandler(oldComments_);
            XMLReader parser = null;
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
            if (XMLToAPI.validateXML) {
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
        Collections.sort(oldComments_.commentsList_);
        return oldComments_;
    } 
    public static void writeXSD(String filename) {
        String xsdFileName = filename;
        int idx = xsdFileName.lastIndexOf('\\');
        int idx2 = xsdFileName.lastIndexOf('/');
        if (idx == -1 && idx2 == -1) {
            xsdFileName = "";
        } else if (idx == -1 && idx2 != -1) {
            xsdFileName = xsdFileName.substring(0, idx2+1);
        } else if (idx != -1  && idx2 == -1) {
            xsdFileName = xsdFileName.substring(0, idx+1);
        } else if (idx != -1  && idx2 != -1) {
            int max = idx2 > idx ? idx2 : idx;
            xsdFileName = xsdFileName.substring(0, max+1);
        }
        xsdFileName += "comments.xsd";
        try {
            FileOutputStream fos = new FileOutputStream(xsdFileName);
            PrintWriter xsdFile = new PrintWriter(fos);
            xsdFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
            xsdFile.println("<xsd:schema xmlns:xsd=\"http:
            xsdFile.println();
            xsdFile.println("<xsd:annotation>");
            xsdFile.println("  <xsd:documentation>");
            xsdFile.println("  Schema for JDiff comments.");
            xsdFile.println("  </xsd:documentation>");
            xsdFile.println("</xsd:annotation>");
            xsdFile.println();
            xsdFile.println("<xsd:element name=\"comments\" type=\"commentsType\"/>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"commentsType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"comment\" type=\"commentType\" minOccurs='0' maxOccurs='unbounded'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("  <xsd:attribute name=\"name\" type=\"xsd:string\"/>");
            xsdFile.println("  <xsd:attribute name=\"jdversion\" type=\"xsd:string\"/>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"commentType\">");
            xsdFile.println("  <xsd:sequence>");
            xsdFile.println("    <xsd:element name=\"identifier\" type=\"identifierType\" minOccurs='1' maxOccurs='unbounded'/>");
            xsdFile.println("    <xsd:element name=\"text\" type=\"xsd:string\" minOccurs='1' maxOccurs='1'/>");
            xsdFile.println("  </xsd:sequence>");
            xsdFile.println("</xsd:complexType>");
            xsdFile.println();
            xsdFile.println("<xsd:complexType name=\"identifierType\">");
            xsdFile.println("  <xsd:attribute name=\"id\" type=\"xsd:string\"/>");
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
    public void addComment(SingleComment comment) {
        commentsList_.add(comment); 
    }
    public static final String placeHolderText = "InsertCommentsHere";
    public static String getComment(Comments comments, String id) {
        if (comments == null)
            return placeHolderText;
        SingleComment key = new SingleComment(id, null);
        int idx = Collections.binarySearch(comments.commentsList_, key);
        if (idx < 0) {
            return placeHolderText;
        } else {
            int startIdx = comments.commentsList_.indexOf(key);
            int endIdx = comments.commentsList_.indexOf(key);
            int numIdx = endIdx - startIdx + 1;
            if (numIdx != 1) {
                System.out.println("Warning: " + numIdx + " identical ids in the existing comments file. Using the first instance.");
            }
            SingleComment singleComment = (SingleComment)(comments.commentsList_.get(idx));
            return singleComment.text_;
        }
    }
    public static String convertAtLinks(String text, String currentElement, 
                                        PackageAPI pkg, ClassAPI cls) {
        if (text == null)
            return null;
        StringBuffer result = new StringBuffer();
        int state = -1;
        final int NORMAL_TEXT = -1;
        final int IN_LINK = 1;
        final int IN_LINK_IDENTIFIER = 2;
        final int IN_LINK_IDENTIFIER_REFERENCE = 3;
        final int IN_LINK_IDENTIFIER_REFERENCE_PARAMS = 6;
        final int IN_LINK_LINKTEXT = 4;
        final int END_OF_LINK = 5;
        StringBuffer identifier = null;
        StringBuffer identifierReference = null;
        StringBuffer linkText = null;
        String ref = "";
        if (currentElement.compareTo("class") == 0 ||
            currentElement.compareTo("interface") == 0) {
	    ref = pkg.name_ + "." + cls.name_ + ".";
        } else if (currentElement.compareTo("package") == 0) {
	    ref = pkg.name_ + ".";
        }
        ref = ref.replace('.', '/');        
        for (int i=0; i < text.length(); i++) {
	    char c = text.charAt( i);
	    char nextChar = i < text.length()-1 ? text.charAt( i+1) : (char)-1;
	    int remainingChars = text.length() - i;
	    switch (state) {
	    case NORMAL_TEXT:
		if (c == '{' && remainingChars >= 5) {
		    if ("{@link".equals(text.substring(i, i + 6))) {
			state = IN_LINK;
			identifier = null;
			identifierReference = null;
			linkText = null;
			i += 5;
			continue;
		    }
		}
		result.append( c);
		break;
	    case IN_LINK:
		if (Character.isWhitespace(nextChar)) continue;
		if (nextChar == '}') {
		    state = END_OF_LINK;
		} else if (!Character.isWhitespace(nextChar)) {
		    state = IN_LINK_IDENTIFIER;
		}
		break;
            case IN_LINK_IDENTIFIER:
		if (identifier == null) {
		    identifier = new StringBuffer();
		}
		if (c == '#') {
		    state = IN_LINK_IDENTIFIER_REFERENCE;
		    continue;
		} else if (Character.isWhitespace(c)) {
		    state = IN_LINK_LINKTEXT;
		    continue;
		}
		identifier.append(c);              
		if (nextChar == '}') {
		    state = END_OF_LINK;
		}
		break;
            case IN_LINK_IDENTIFIER_REFERENCE:
		if (identifierReference == null) {
		    identifierReference = new StringBuffer();
		}
		if (Character.isWhitespace(c)) {
		    state = IN_LINK_LINKTEXT;
		    continue;
		}
		identifierReference.append(c);
		if (c == '(') {
		    state = IN_LINK_IDENTIFIER_REFERENCE_PARAMS;
		}
		if (nextChar == '}') {
		    state = END_OF_LINK;
		}
		break;
            case IN_LINK_IDENTIFIER_REFERENCE_PARAMS:
		if (c == ')') {
		    state = IN_LINK_IDENTIFIER_REFERENCE;
		}
		identifierReference.append(c);
		if (nextChar == '}') {
		    state = END_OF_LINK;
		}
		break;
            case IN_LINK_LINKTEXT:
		if (linkText == null) linkText = new StringBuffer();
		linkText.append(c);
		if (nextChar == '}') {
		    state = END_OF_LINK;
		}
		break;
            case END_OF_LINK:
		if (identifier != null) {
		    result.append("<A HREF=\"");
		    result.append(HTMLReportGenerator.newDocPrefix);
		    result.append(ref);
		    result.append(identifier.toString().replace('.', '/'));
		    result.append(".html");
		    if (identifierReference != null) {
			result.append("#");
			result.append(identifierReference);
		    }
		    result.append("\">");   
		    result.append("<TT>");
		    if (linkText != null) {
			result.append(linkText);
		    } else {
			result.append(identifier);
			if (identifierReference != null) {
			    result.append(".");
			    result.append(identifierReference);
			}
		    }
		    result.append("</TT>");
		    result.append("</A>");
		}
		state = NORMAL_TEXT;
		break;
	    }
        }
        return result.toString();
    }
    public static boolean writeFile(String outputFileName, 
                                    Comments newComments) {
        try {
            FileOutputStream fos = new FileOutputStream(outputFileName);
            outputFile = new PrintWriter(fos);
            newComments.emitXMLHeader(outputFileName);
            newComments.emitComments();
            newComments.emitXMLFooter();
            outputFile.close();
        } catch(IOException e) {
            System.out.println("IO Error while attempting to create " + outputFileName);
            System.out.println("Error: "+ e.getMessage());
            System.exit(1);
        }
        return true;
    }
    public void emitComments() {
        Iterator iter = commentsList_.iterator();
        while (iter.hasNext()) {
            SingleComment currComment = (SingleComment)(iter.next());
            if (!currComment.isUsed_)
                outputFile.println("<!-- This comment is no longer used ");
            outputFile.println("<comment>");
            outputFile.println("  <identifier id=\"" + currComment.id_ + "\"/>");
            outputFile.println("  <text>");
            outputFile.println("    " + currComment.text_);
            outputFile.println("  </text>");
            outputFile.println("</comment>");
            if (!currComment.isUsed_)
                outputFile.println("-->");
        }        
    }
    public void dump() {
        Iterator iter = commentsList_.iterator();
        int i = 0;
        while (iter.hasNext()) {
            i++;
            SingleComment currComment = (SingleComment)(iter.next());
            System.out.println("Comment " + i);
            System.out.println("id = " + currComment.id_);
            System.out.println("text = \"" + currComment.text_ + "\"");
            System.out.println("isUsed = " + currComment.isUsed_);
        }        
    }
    public static void noteDifferences(Comments oldComments, Comments newComments) {
        if (oldComments == null) {
            System.out.println("Note: all the comments have been newly generated");
            return;
        }
        Iterator iter = oldComments.commentsList_.iterator();
        while (iter.hasNext()) {
            SingleComment oldComment = (SingleComment)(iter.next());
            int idx = Collections.binarySearch(newComments.commentsList_, oldComment);
            if (idx < 0) {
                System.out.println("Warning: comment \"" + oldComment.id_ + "\" is no longer used.");
                oldComment.isUsed_ = false;
                newComments.commentsList_.add(oldComment);
            }
        }        
    }
    public void emitXMLHeader(String filename) {
        outputFile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>");
        outputFile.println("<comments");
        outputFile.println("  xmlns:xsi='" + RootDocToXML.baseURI + "/2001/XMLSchema-instance'");
        outputFile.println("  xsi:noNamespaceSchemaLocation='comments.xsd'");
        int idx = filename.lastIndexOf('.');
        String apiIdentifier = filename.substring(0, idx);
        if (HTMLReportGenerator.commentsDir != null)
	    apiIdentifier = apiIdentifier.substring(HTMLReportGenerator.commentsDir.length()+1);
        else if (HTMLReportGenerator.outputDir != null)
            apiIdentifier = apiIdentifier.substring(HTMLReportGenerator.outputDir.length()+1);
        apiIdentifier = apiIdentifier.substring(18);
        outputFile.println("  name=\"" + apiIdentifier + "\"");
        outputFile.println("  jdversion=\"" + JDiff.version + "\">");
        outputFile.println();
        outputFile.println("<!-- Use this file to enter an API change description. For example, when you remove a class, ");
        outputFile.println("     you can enter a comment for that class that points developers to the replacement class. ");
        outputFile.println("     You can also provide a change summary for modified API, to give an overview of the changes ");
        outputFile.println("     why they were made, workarounds, etc.  -->");
        outputFile.println();
        outputFile.println("<!-- When the API diffs report is generated, the comments in this file get added to the tables of ");
        outputFile.println("     removed, added, and modified packages, classes, methods, and fields. This file does not ship ");
        outputFile.println("     with the final report. -->");
        outputFile.println();
        outputFile.println("<!-- The id attribute in an identifier element identifies the change as noted in the report. ");
        outputFile.println("     An id has the form package[.class[.[ctor|method|field].signature]], where [] indicates optional ");
        outputFile.println("     text. A comment element can have multiple identifier elements, which will will cause the same ");
        outputFile.println("     text to appear at each place in the report, but will be converted to separate comments when the ");
        outputFile.println("     comments file is used. -->");
        outputFile.println();
        outputFile.println("<!-- HTML tags in the text field will appear in the report. You also need to close p HTML elements, ");
        outputFile.println("     used for paragraphs - see the top-level documentation. -->");
        outputFile.println();
        outputFile.println("<!-- You can include standard javadoc links in your change descriptions. You can use the @first command  ");
        outputFile.println("     to cause jdiff to include the first line of the API documentation. You also need to close p HTML ");
        outputFile.println("     elements, used for paragraphs - see the top-level documentation. -->");
        outputFile.println();
    }
    public void emitXMLFooter() {
        outputFile.println();
        outputFile.println("</comments>");
    }
    private static List oldAPIList = null;
    private static List newAPIList = null;
    public static boolean isMinimizedTag(String tag) {
        if (tag.equalsIgnoreCase("p") ||
            tag.equalsIgnoreCase("br") ||
            tag.equalsIgnoreCase("hr")
            ) {
            return true;
	}
        return false;
    }
    private static PrintWriter outputFile = null;
}
