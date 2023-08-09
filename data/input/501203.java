class CommentsHandler extends DefaultHandler {
    public Comments comments_ = null;
    private List currSingleComment_ = null; 
    private boolean inText = false;
    private String currentText = null;
    private LinkedList tagStack = null;
    public CommentsHandler(Comments comments) {
        comments_ = comments;
        tagStack = new LinkedList();
    }   
    public void startDocument() {
    }
    public void endDocument() {
        if (trace)
            comments_.dump();
    }
    public void startElement(java.lang.String uri, java.lang.String localName,
                             java.lang.String qName, Attributes attributes) {
	if (localName.equals(""))
	    localName = qName;
        if (localName.compareTo("comments") == 0) {
            String commentsName = attributes.getValue("name");
            String version = attributes.getValue("jdversion"); 
            if (commentsName == null) {
                System.out.println("Error: no identifier found in the comments XML file.");
                System.exit(3);
            }
            int idx1 = JDiff.oldFileName.lastIndexOf('.');
            int idx2 = JDiff.newFileName.lastIndexOf('.');
            String filename2 = JDiff.oldFileName.substring(0, idx1) + 
                "_to_" + JDiff.newFileName.substring(0, idx2);
            if (filename2.compareTo(commentsName) != 0) {
                System.out.println("Warning: API identifier in the comments XML file (" + filename2 + ") differs from the name of the file.");
            }
        } else if (localName.compareTo("comment") == 0) {
            currSingleComment_ = new ArrayList(); 
        } else if (localName.compareTo("identifier") == 0) {
            String id = attributes.getValue("id");
            SingleComment newComment = new SingleComment(id, null);
            currSingleComment_.add(newComment);
        } else if (localName.compareTo("text") == 0) {
            inText = true;
            currentText = null;
        } else {
            if (inText) {
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
        if (localName.compareTo("text") == 0) {
            inText = false;
            addTextToComments();
        } else if (inText) {
            addEndTagToText(localName);
        }
    }
    public void characters(char[] ch, int start, int length) {
        if (inText) {
            String chunk = new String(ch, start, length);
            if (currentText == null)
                currentText = chunk;
            else
                currentText += chunk;
        }
    }
    public void addTextToComments() {
        currentText = currentText.trim();
        if (!currentText.endsWith(".") &&
            !currentText.endsWith("?") &&
            !currentText.endsWith("!") && 
            currentText.compareTo(Comments.placeHolderText) != 0) {
            System.out.println("Warning: text of comment does not end in a period: " + currentText);
        }
        Iterator iter = currSingleComment_.iterator();
        while (iter.hasNext()) {
            SingleComment currComment = (SingleComment)(iter.next());
            if (currComment.text_ == null)
                currComment.text_ = currentText;
            else
                currComment.text_ += currentText;
            comments_.addComment(currComment);
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
    public void warning(SAXParseException e) {
        System.out.println("Warning (" + e.getLineNumber() + "): parsing XML comments file:" + e);
        e.printStackTrace();
    }
    public void error(SAXParseException e) {
        System.out.println("Error (" + e.getLineNumber() + "): parsing XML comments file:" + e);
        e.printStackTrace();
        System.exit(1);
    }
    public void fatalError(SAXParseException e) {
        System.out.println("Fatal Error (" + e.getLineNumber() + "): parsing XML comments file:" + e);
        e.printStackTrace();
        System.exit(1);
    }    
    private static final boolean trace = false;
}
