public class ExtractStringRefactoring extends Refactoring {
    public enum Mode {
        EDIT_SOURCE,
        SELECT_ID,
        SELECT_NEW_ID
    }
    private final Mode mMode;
    private String mXmlAttributeName;
    private final IFile mFile;
    private final IEditorPart mEditor;
    private final IProject mProject;
    private final int mSelectionStart;
    private final int mSelectionEnd;
    private ICompilationUnit mUnit;
    private String mTokenString;
    private String mXmlStringId;
    private String mXmlStringValue;
    private String mTargetXmlFileWsPath;
    private ArrayList<Change> mChanges;
    private XmlStringFileHelper mXmlHelper = new XmlStringFileHelper();
    private static final String KEY_MODE = "mode";              
    private static final String KEY_FILE = "file";              
    private static final String KEY_PROJECT = "proj";           
    private static final String KEY_SEL_START = "sel-start";    
    private static final String KEY_SEL_END = "sel-end";        
    private static final String KEY_TOK_ESC = "tok-esc";        
    private static final String KEY_XML_ATTR_NAME = "xml-attr-name";      
    public ExtractStringRefactoring(Map<String, String> arguments) throws NullPointerException {
        mMode = Mode.valueOf(arguments.get(KEY_MODE));
        IPath path = Path.fromPortableString(arguments.get(KEY_PROJECT));
        mProject = (IProject) ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (mMode == Mode.EDIT_SOURCE) {
            path = Path.fromPortableString(arguments.get(KEY_FILE));
            mFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(path);
            mSelectionStart = Integer.parseInt(arguments.get(KEY_SEL_START));
            mSelectionEnd   = Integer.parseInt(arguments.get(KEY_SEL_END));
            mTokenString    = arguments.get(KEY_TOK_ESC);
            mXmlAttributeName = arguments.get(KEY_XML_ATTR_NAME);
        } else {
            mFile = null;
            mSelectionStart = mSelectionEnd = -1;
            mTokenString = null;
            mXmlAttributeName = null;
        }
        mEditor = null;
    }
    private Map<String, String> createArgumentMap() {
        HashMap<String, String> args = new HashMap<String, String>();
        args.put(KEY_MODE,      mMode.name());
        args.put(KEY_PROJECT,   mProject.getFullPath().toPortableString());
        if (mMode == Mode.EDIT_SOURCE) {
            args.put(KEY_FILE,      mFile.getFullPath().toPortableString());
            args.put(KEY_SEL_START, Integer.toString(mSelectionStart));
            args.put(KEY_SEL_END,   Integer.toString(mSelectionEnd));
            args.put(KEY_TOK_ESC,   mTokenString);
            args.put(KEY_XML_ATTR_NAME, mXmlAttributeName);
        }
        return args;
    }
    public ExtractStringRefactoring(IFile file, IEditorPart editor, ITextSelection selection) {
        mMode = Mode.EDIT_SOURCE;
        mFile = file;
        mEditor = editor;
        mProject = file.getProject();
        mSelectionStart = selection.getOffset();
        mSelectionEnd = mSelectionStart + Math.max(0, selection.getLength() - 1);
    }
    public ExtractStringRefactoring(IProject project, boolean enforceNew) {
        mMode = enforceNew ? Mode.SELECT_NEW_ID : Mode.SELECT_ID;
        mFile = null;
        mEditor = null;
        mProject = project;
        mSelectionStart = mSelectionEnd = -1;
    }
    @Override
    public String getName() {
        if (mMode == Mode.SELECT_ID) {
            return "Create or USe Android String";
        } else if (mMode == Mode.SELECT_NEW_ID) {
            return "Create New Android String";
        }
        return "Extract Android String";
    }
    public Mode getMode() {
        return mMode;
    }
    public String getTokenString() {
        return mTokenString;
    }
    public String getXmlStringId() {
        return mXmlStringId;
    }
    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        mUnit = null;
        mTokenString = null;
        RefactoringStatus status = new RefactoringStatus();
        try {
            monitor.beginTask("Checking preconditions...", 6);
            if (mMode != Mode.EDIT_SOURCE) {
                monitor.worked(6);
                return status;
            }
            if (!checkSourceFile(mFile, status, monitor)) {
                return status;
            }
            try {
                mUnit = JavaCore.createCompilationUnitFrom(mFile);
                if (mUnit.isReadOnly()) {
                    status.addFatalError("The file is read-only, please make it writeable first.");
                    return status;
                }
                if (!findSelectionInJavaUnit(mUnit, status, monitor)) {
                    return status;
                }
            } catch (Exception e) {
            }
            if (mUnit != null) {
                monitor.worked(1);
                return status;
            }
            if (mFile != null && AndroidConstants.EXT_XML.equals(mFile.getFileExtension())) {
                IPath path = mFile.getFullPath();
                if (path.segmentCount() == 4) {
                    if (path.segment(1).equalsIgnoreCase(SdkConstants.FD_RESOURCES)) {
                        if (!findSelectionInXmlFile(mFile, status, monitor)) {
                            return status;
                        }
                    }
                }
            }
            if (!status.isOK()) {
                status.addFatalError(
                        "Selection must be inside a Java source or an Android Layout XML file.");
            }
        } finally {
            monitor.done();
        }
        return status;
    }
    private boolean findSelectionInJavaUnit(ICompilationUnit unit,
            RefactoringStatus status, IProgressMonitor monitor) {
        try {
            IBuffer buffer = unit.getBuffer();
            IScanner scanner = ToolFactory.createScanner(
                    false, 
                    false, 
                    false, 
                    false  
                    );
            scanner.setSource(buffer.getCharacters());
            monitor.worked(1);
            for(int token = scanner.getNextToken();
                    token != ITerminalSymbols.TokenNameEOF;
                    token = scanner.getNextToken()) {
                if (scanner.getCurrentTokenStartPosition() <= mSelectionStart &&
                        scanner.getCurrentTokenEndPosition() >= mSelectionEnd) {
                    if (token == ITerminalSymbols.TokenNameStringLiteral) {
                        mTokenString = new String(scanner.getCurrentTokenSource());
                    }
                    break;
                } else if (scanner.getCurrentTokenStartPosition() > mSelectionEnd) {
                    break;
                }
            }
        } catch (JavaModelException e1) {
        } catch (InvalidInputException e2) {
        } finally {
            monitor.worked(1);
        }
        if (mTokenString != null) {
            int len = mTokenString.length();
            if (len > 0 &&
                    mTokenString.charAt(0) == '"' &&
                    mTokenString.charAt(len - 1) == '"') {
                mTokenString = mTokenString.substring(1, len - 1);
            }
            if (mTokenString.length() == 0) {
                mTokenString = null;
            }
        }
        if (mTokenString == null) {
            status.addFatalError("Please select a Java string literal.");
        }
        monitor.worked(1);
        return status.isOK();
    }
    private boolean findSelectionInXmlFile(IFile file,
            RefactoringStatus status,
            IProgressMonitor monitor) {
        try {
            if (!(mEditor instanceof AndroidEditor)) {
                status.addFatalError("Only the Android XML Editor is currently supported.");
                return status.isOK();
            }
            AndroidEditor editor = (AndroidEditor) mEditor;
            IStructuredModel smodel = null;
            Node node = null;
            String currAttrName = null;
            try {
                smodel = editor.getModelForRead();
                if (smodel != null) {
                    for(int offset = mSelectionStart; offset >= 0 && node == null; --offset) {
                        node = (Node) smodel.getIndexedRegion(offset);
                    }
                    if (node == null) {
                        status.addFatalError(
                                "The selection does not match any element in the XML document.");
                        return status.isOK();
                    }
                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        status.addFatalError("The selection is not inside an actual XML element.");
                        return status.isOK();
                    }
                    IStructuredDocument sdoc = smodel.getStructuredDocument();
                    if (sdoc != null) {
                        int selStart = mSelectionStart;
                        IStructuredDocumentRegion region =
                            sdoc.getRegionAtCharacterOffset(selStart);
                        if (region != null &&
                                DOMRegionContext.XML_TAG_NAME.equals(region.getType())) {
                            currAttrName = findSelectionInRegion(region, selStart);
                            if (mTokenString == null) {
                                status.addFatalError(
                                    "The selection is not inside an actual XML attribute value.");
                            }
                        }
                    }
                    if (mTokenString != null && node != null && currAttrName != null) {
                        validateSelectedAttribute(editor, node, currAttrName, status);
                    } else {
                        mTokenString = null;
                    }
                }
            } finally {
                if (smodel != null) {
                    smodel.releaseFromRead();
                }
            }
        } finally {
            monitor.worked(1);
        }
        return status.isOK();
    }
    private String findSelectionInRegion(IStructuredDocumentRegion region, int selStart) {
        String currAttrName = null;
        int startInRegion = selStart - region.getStartOffset();
        int nb = region.getNumberOfRegions();
        ITextRegionList list = region.getRegions();
        String currAttrValue = null;
        for (int i = 0; i < nb; i++) {
            ITextRegion subRegion = list.get(i);
            String type = subRegion.getType();
            if (DOMRegionContext.XML_TAG_ATTRIBUTE_NAME.equals(type)) {
                currAttrName = region.getText(subRegion);
                if (subRegion.getStart() <= startInRegion &&
                        startInRegion < subRegion.getTextEnd()) {
                    if (i <= nb - 3 &&
                            DOMRegionContext.XML_TAG_ATTRIBUTE_EQUALS.equals(
                                                   list.get(i + 1).getType())) {
                        subRegion = list.get(i + 2);
                        type = subRegion.getType();
                        if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(
                                type)) {
                            currAttrValue = region.getText(subRegion);
                        }
                    }
                }
            } else if (subRegion.getStart() <= startInRegion &&
                    startInRegion < subRegion.getTextEnd() &&
                    DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(type)) {
                currAttrValue = region.getText(subRegion);
            }
            if (currAttrValue != null) {
                String text = currAttrValue;
                int len = text.length();
                if (len >= 2 &&
                        text.charAt(0) == '"' &&
                        text.charAt(len - 1) == '"') {
                    text = text.substring(1, len - 1);
                } else if (len >= 2 &&
                        text.charAt(0) == '\'' &&
                        text.charAt(len - 1) == '\'') {
                    text = text.substring(1, len - 1);
                }
                if (text.length() > 0 && currAttrName != null) {
                    mTokenString = text;
                }
                break;
            }
        }
        return currAttrName;
    }
    private void validateSelectedAttribute(AndroidEditor editor, Node node,
            String attrName, RefactoringStatus status) {
        UiElementNode rootUiNode = editor.getUiRootNode();
        UiElementNode currentUiNode =
            rootUiNode == null ? null : rootUiNode.findXmlNode(node);
        ReferenceAttributeDescriptor attrDesc = null;
        if (currentUiNode != null) {
            String name = attrName;
            int pos = name.indexOf(':');
            if (pos > 0 && pos < name.length() - 1) {
                name = name.substring(pos + 1);
            }
            for (UiAttributeNode attrNode : currentUiNode.getUiAttributes()) {
                if (attrNode.getDescriptor().getXmlLocalName().equals(name)) {
                    AttributeDescriptor desc = attrNode.getDescriptor();
                    if (desc instanceof ReferenceAttributeDescriptor) {
                        attrDesc = (ReferenceAttributeDescriptor) desc;
                    }
                    break;
                }
            }
        }
        if (attrDesc != null &&
                (attrDesc.getResourceType() == null ||
                 attrDesc.getResourceType() == ResourceType.STRING)) {
            if (mTokenString.startsWith("@")) {                             
                int pos1 = 0;
                if (mTokenString.length() > 1 && mTokenString.charAt(1) == '+') {
                    pos1++;
                }
                int pos2 = mTokenString.indexOf('/');
                if (pos2 > pos1) {
                    String kind = mTokenString.substring(pos1 + 1, pos2);
                    if (ResourceType.STRING.getName().equals(kind)) {                            
                        mTokenString = null;
                        status.addFatalError(String.format(
                                "The attribute %1$s already contains a %2$s reference.",
                                attrName,
                                kind));
                    }
                }
            }
            if (mTokenString != null) {
                mXmlAttributeName = attrName;
            }
        } else {
            mTokenString = null;
            status.addFatalError(String.format(
                    "The attribute %1$s does not accept a string reference.",
                    attrName));
        }
    }
    private boolean checkSourceFile(IFile file,
            RefactoringStatus status,
            IProgressMonitor monitor) {
        if (!file.isSynchronized(IResource.DEPTH_ZERO)) {
            status.addFatalError("The file is not synchronized. Please save it first.");
            return false;
        }
        monitor.worked(1);
        ResourceAttributes resAttr = file.getResourceAttributes();
        if (resAttr == null || resAttr.isReadOnly()) {
            status.addFatalError("The file is read-only, please make it writeable first.");
            return false;
        }
        monitor.worked(1);
        return true;
    }
    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        RefactoringStatus status = new RefactoringStatus();
        try {
            monitor.beginTask("Checking post-conditions...", 3);
            if (mXmlStringId == null || mXmlStringId.length() <= 0) {
                status.addFatalError("Missing replacement string ID");
            } else if (mTargetXmlFileWsPath == null || mTargetXmlFileWsPath.length() <= 0) {
                status.addFatalError("Missing target xml file path");
            }
            monitor.worked(1);
            IResource targetXml = getTargetXmlResource(mTargetXmlFileWsPath);
            if (targetXml != null) {
                if (targetXml.getType() != IResource.FILE) {
                    status.addFatalError(
                            String.format("XML file '%1$s' is not a file.", mTargetXmlFileWsPath));
                } else {
                    ResourceAttributes attr = targetXml.getResourceAttributes();
                    if (attr != null && attr.isReadOnly()) {
                        status.addFatalError(
                                String.format("XML file '%1$s' is read-only.",
                                        mTargetXmlFileWsPath));
                    }
                }
            }
            monitor.worked(1);
            if (status.hasError()) {
                return status;
            }
            mChanges = new ArrayList<Change>();
            if (mXmlHelper.valueOfStringId(mProject, mTargetXmlFileWsPath, mXmlStringId) == null) {
                Change change = createXmlChange((IFile) targetXml, mXmlStringId, mXmlStringValue,
                        status, SubMonitor.convert(monitor, 1));
                if (change != null) {
                    mChanges.add(change);
                }
            }
            if (status.hasError()) {
                return status;
            }
            if (mMode == Mode.EDIT_SOURCE) {
                List<Change> changes = null;
                if (mXmlAttributeName != null) {
                    changes = computeXmlSourceChanges(mFile,
                            mXmlStringId, mTokenString, mXmlAttributeName,
                            status, monitor);
                } else {
                    changes = computeJavaChanges(mUnit, mXmlStringId, mTokenString,
                            status, SubMonitor.convert(monitor, 1));
                }
                if (changes != null) {
                    mChanges.addAll(changes);
                }
            }
            monitor.worked(1);
        } finally {
            monitor.done();
        }
        return status;
    }
    private Change createXmlChange(IFile targetXml,
            String xmlStringId,
            String tokenString,
            RefactoringStatus status,
            SubMonitor subMonitor) {
        TextFileChange xmlChange = new TextFileChange(getName(), targetXml);
        xmlChange.setTextType("xml");   
        TextEdit edit = null;
        TextEditGroup editGroup = null;
        if (!targetXml.exists()) {
            StringBuilder content = new StringBuilder();
            content.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"); 
            content.append("<resources>\n");                                
            content.append("    <string name=\"").                          
                        append(xmlStringId).
                        append("\">").                                      
                        append(tokenString).
                        append("</string>\n");                              
            content.append("</resources>\n");                                
            edit = new InsertEdit(0, content.toString());
            editGroup = new TextEditGroup("Create <string> in new XML file", edit);
        } else {
            try {
                int[] indices = new int[2];
                if (findXmlOpeningTagPos(targetXml.getContents(), "resources", indices)) {  
                    int offset = indices[0];
                    int len = indices[1];
                    StringBuilder content = new StringBuilder();
                    content.append(">\n");                                      
                    content.append("    <string name=\"").                      
                                append(xmlStringId).
                                append("\">").                                  
                                append(tokenString).
                                append("</string>");                            
                    if (len == 2) {
                        content.append("\n</resources>");                       
                    }
                    edit = new ReplaceEdit(offset, len, content.toString());
                    editGroup = new TextEditGroup("Insert <string> in XML file", edit);
                }
            } catch (CoreException e) {
            }
        }
        if (edit == null) {
            status.addFatalError(String.format("Failed to modify file %1$s",
                    mTargetXmlFileWsPath));
            return null;
        }
        xmlChange.setEdit(edit);
        xmlChange.addTextEditChangeGroup(new TextEditChangeGroup(xmlChange, editGroup));
        subMonitor.worked(1);
        return xmlChange;
    }
    private boolean findXmlOpeningTagPos(InputStream contents, String tag, int[] indices) {
        BufferedReader br = new BufferedReader(new InputStreamReader(contents));
        StringBuilder sb = new StringBuilder(); 
        tag = "<" + tag;
        int tagLen = tag.length();
        int maxLen = tagLen < 3 ? 3 : tagLen;
        try {
            int offset = 0;
            int i = 0;
            char searching = '<'; 
            boolean capture = false;
            boolean inComment = false;
            boolean inTag = false;
            while ((i = br.read()) != -1) {
                char c = (char) i;
                if (c == searching) {
                    capture = true;
                }
                if (capture) {
                    sb.append(c);
                    int len = sb.length();
                    if (inComment && c == '>') {
                        if (len >= 3 && sb.substring(len-3).equals("-->")) {    
                            capture = false;
                            inComment = false;
                            sb.setLength(0);
                        }
                    } else if (inTag && c == '>') {
                        indices[0] = offset;
                        indices[1] = 1;
                        if (sb.charAt(len - 2) == '/') {
                            indices[0]--;
                            indices[1]++;
                        }
                        return true;
                    } else if (!inComment && !inTag) {
                        if (len == 3 && sb.equals("<--")) {                     
                            inComment = true;
                        } else if (len == tagLen && sb.toString().equals(tag)) {
                            inTag = true;
                        }
                        if (!inComment && !inTag) {
                            if (c == '>' || c == '?' || c == ' ' || c == '\n' || c == '\r') {
                                capture = false;
                                sb.setLength(0);
                            }
                        }
                    }
                    if (capture && len > maxLen) {
                        sb.deleteCharAt(0);
                    }
                }
                offset++;
            }
        } catch (IOException e) {
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        return false;
    }
    private List<Change> computeXmlSourceChanges(IFile sourceFile,
            String xmlStringId,
            String tokenString,
            String xmlAttrName,
            RefactoringStatus status,
            IProgressMonitor monitor) {
        if (!sourceFile.exists()) {
            status.addFatalError(String.format("XML file '%1$s' does not exist.",
                    sourceFile.getFullPath().toOSString()));
            return null;
        }
        HashSet<IFile> files = new HashSet<IFile>();
        files.add(sourceFile);
        if (AndroidConstants.EXT_XML.equals(sourceFile.getFileExtension())) {
            IPath path = sourceFile.getFullPath();
            if (path.segmentCount() == 4 && path.segment(1).equals(SdkConstants.FD_RESOURCES)) {
                IProject project = sourceFile.getProject();
                String filename = path.segment(3);
                String initialTypeName = path.segment(2);
                ResourceFolderType type = ResourceFolderType.getFolderType(initialTypeName);
                IContainer res = sourceFile.getParent().getParent();
                if (type != null && res != null && res.getType() == IResource.FOLDER) {
                    try {
                        for (IResource r : res.members()) {
                            if (r != null && r.getType() == IResource.FOLDER) {
                                String name = r.getName();
                                if (!name.equals(initialTypeName)) {
                                    ResourceFolderType t =
                                        ResourceFolderType.getFolderType(name);
                                    if (type.equals(t)) {
                                        IPath p = res.getProjectRelativePath().append(name).
                                                                               append(filename);
                                        IResource f = project.findMember(p);
                                        if (f != null && f instanceof IFile) {
                                            files.add((IFile) f);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (CoreException e) {
                    }
                }
            }
        }
        SubMonitor subMonitor = SubMonitor.convert(monitor, Math.min(1, files.size()));
        ArrayList<Change> changes = new ArrayList<Change>();
        try {
            IModelManager modelManager = StructuredModelManager.getModelManager();
            for (IFile file : files) {
                IStructuredDocument sdoc = modelManager.createStructuredDocumentFor(file);
                if (sdoc == null) {
                    status.addFatalError("XML structured document not found");     
                    return null;
                }
                TextFileChange xmlChange = new TextFileChange(getName(), file);
                xmlChange.setTextType("xml");   
                MultiTextEdit multiEdit = new MultiTextEdit();
                ArrayList<TextEditGroup> editGroups = new ArrayList<TextEditGroup>();
                String quotedReplacement = quotedAttrValue("@string/" + xmlStringId);
                try {
                    for (IStructuredDocumentRegion region : sdoc.getStructuredDocumentRegions()) {
                        if (!DOMRegionContext.XML_TAG_NAME.equals(region.getType())) {
                            continue;
                        }
                        int nb = region.getNumberOfRegions();
                        ITextRegionList list = region.getRegions();
                        String lastAttrName = null;
                        for (int i = 0; i < nb; i++) {
                            ITextRegion subRegion = list.get(i);
                            String type = subRegion.getType();
                            if (DOMRegionContext.XML_TAG_ATTRIBUTE_NAME.equals(type)) {
                                lastAttrName = region.getText(subRegion);
                            } else if (DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE.equals(type)) {
                                String text = region.getText(subRegion);
                                int len = text.length();
                                if (len >= 2 &&
                                        text.charAt(0) == '"' &&
                                        text.charAt(len - 1) == '"') {
                                    text = text.substring(1, len - 1);
                                } else if (len >= 2 &&
                                        text.charAt(0) == '\'' &&
                                        text.charAt(len - 1) == '\'') {
                                    text = text.substring(1, len - 1);
                                }
                                if (xmlAttrName.equals(lastAttrName) && tokenString.equals(text)) {
                                    TextEdit edit = new ReplaceEdit(
                                            region.getStartOffset() + subRegion.getStart(),
                                            subRegion.getTextLength(),
                                            quotedReplacement);
                                    TextEditGroup editGroup = new TextEditGroup(
                                            "Replace attribute string by ID",
                                            edit);
                                    multiEdit.addChild(edit);
                                    editGroups.add(editGroup);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    status.addFatalError(
                            String.format("XML refactoring error: %1$s", t.getMessage()));
                } finally {
                    if (multiEdit.hasChildren()) {
                        xmlChange.setEdit(multiEdit);
                        for (TextEditGroup group : editGroups) {
                            xmlChange.addTextEditChangeGroup(
                                    new TextEditChangeGroup(xmlChange, group));
                        }
                        changes.add(xmlChange);
                    }
                    subMonitor.worked(1);
                }
            } 
        } catch (IOException e) {
            status.addFatalError(String.format("XML model IO error: %1$s.", e.getMessage()));
        } catch (CoreException e) {
            status.addFatalError(String.format("XML model core error: %1$s.", e.getMessage()));
        } finally {
            if (changes.size() > 0) {
                return changes;
            }
        }
        return null;
    }
    private String quotedAttrValue(String attrValue) {
        if (attrValue.indexOf('"') == -1) {
            return '"' + attrValue + '"';
        }
        if (attrValue.indexOf('\'') == -1) {
            return '\'' + attrValue + '\'';
        }
        attrValue = attrValue.replace("\"", "&quot;");  
        return '"' + attrValue + '"';
    }
    private List<Change> computeJavaChanges(ICompilationUnit unit,
            String xmlStringId,
            String tokenString,
            RefactoringStatus status,
            SubMonitor subMonitor) {
        String packageName = null;
        String error = null;
        IResource manifestFile = mProject.findMember(AndroidConstants.FN_ANDROID_MANIFEST);
        if (manifestFile == null || manifestFile.getType() != IResource.FILE) {
            error = "File not found";
        } else {
            try {
                AndroidManifestParser manifest = AndroidManifestParser.parseForData(
                        (IFile) manifestFile);
                if (manifest == null) {
                    error = "Invalid content";
                } else {
                    packageName = manifest.getPackage();
                    if (packageName == null) {
                        error = "Missing package definition";
                    }
                }
            } catch (CoreException e) {
                error = e.getLocalizedMessage();
            }
        }
        if (error != null) {
            status.addFatalError(
                    String.format("Failed to parse file %1$s: %2$s.",
                            manifestFile == null ? "" : manifestFile.getFullPath(),  
                            error));
            return null;
        }
        ArrayList<Change> changes = new ArrayList<Change>();
        TextFileChange change = new TextFileChange(getName(), (IFile) unit.getResource());
        change.setTextType("java"); 
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setProject(unit.getJavaProject());
        parser.setSource(unit);
        parser.setResolveBindings(true);
        ASTNode node = parser.createAST(subMonitor.newChild(1));
        if (!(node instanceof CompilationUnit)) {
            status.addFatalError(String.format("Internal error: ASTNode class %s",  
                    node.getClass()));
            return null;
        }
        ImportRewrite importRewrite = ImportRewrite.create((CompilationUnit) node, true);
        String Rqualifier = packageName + ".R"; 
        Rqualifier = importRewrite.addImport(Rqualifier);
        AST ast = node.getAST();
        ASTRewrite astRewrite = ASTRewrite.create(ast);
        ArrayList<TextEditGroup> astEditGroups = new ArrayList<TextEditGroup>();
        ReplaceStringsVisitor visitor = new ReplaceStringsVisitor(
                ast, astRewrite, astEditGroups,
                tokenString, Rqualifier, xmlStringId);
        node.accept(visitor);
        try {
            MultiTextEdit edit = new MultiTextEdit();
            TextEdit subEdit = importRewrite.rewriteImports(subMonitor.newChild(1));
            if (subEdit.hasChildren()) {
                edit.addChild(subEdit);
            }
            subEdit = astRewrite.rewriteAST();
            if (subEdit.hasChildren()) {
                edit.addChild(subEdit);
            }
            if (edit.hasChildren()) {
                change.setEdit(edit);
                for (TextEditGroup editGroup : astEditGroups) {
                    change.addTextEditChangeGroup(new TextEditChangeGroup(change, editGroup));
                }
                changes.add(change);
            }
            subMonitor.worked(1);
            if (changes.size() > 0) {
                return changes;
            }
        } catch (CoreException e) {
            status.addFatalError(e.getMessage());
        }
        return null;
    }
    @Override
    public Change createChange(IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        try {
            monitor.beginTask("Applying changes...", 1);
            CompositeChange change = new CompositeChange(
                    getName(),
                    mChanges.toArray(new Change[mChanges.size()])) {
                @Override
                public ChangeDescriptor getDescriptor() {
                    String comment = String.format(
                            "Extracts string '%1$s' into R.string.%2$s",
                            mTokenString,
                            mXmlStringId);
                    ExtractStringDescriptor desc = new ExtractStringDescriptor(
                            mProject.getName(), 
                            comment, 
                            comment, 
                            createArgumentMap());
                    return new RefactoringChangeDescriptor(desc);
                }
            };
            monitor.worked(1);
            return change;
        } finally {
            monitor.done();
        }
    }
    private IResource getTargetXmlResource(String xmlFileWsPath) {
        IResource resource = mProject.getFile(xmlFileWsPath);
        return resource;
    }
    public void setNewStringId(String newStringId) {
        mXmlStringId = newStringId;
    }
    public void setNewStringValue(String newStringValue) {
        mXmlStringValue = newStringValue;
    }
    public void setTargetFile(String targetXmlFileWsPath) {
        mTargetXmlFileWsPath = targetXmlFileWsPath;
    }
}
