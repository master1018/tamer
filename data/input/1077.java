public class NotebookCustodian implements MindRaiderConstants {
    private static final Category cat = Category.getInstance("com.emental.mindraider.notebook.NotebookCustodian");
    public static final String FILENAME_XML_RESOURCE = "notebook.xml";
    public static final String FILENAME_RDF_MODEL = "notebook.rdf.xml";
    public static final String FILENAME_DC = "dc.rdf.xml";
    public static final String DIRECTORY_CONCEPTS = "concepts";
    public static final String DIRECTORY_ANNOTATIONS = "annotations";
    public static String MR_DOC_FOLDER_LOCAL_NAME = "MR";
    public static String MR_DOC_NOTEBOOK_INTRODUCTION_LOCAL_NAME = "Introduction";
    public static String MR_DOC_NOTEBOOK_DOCUMENTATION_LOCAL_NAME = "MR_Documentation";
    public static String MR_DOC_NOTEBOOK_FOR_DEVELOPERS_LOCAL_NAME = "For_Developers";
    private String notebooksDirectory = null;
    private ArrayList subscribers;
    public NotebookResource activeNotebookResource;
    public NotebookCustodian(String notebooksDirectory) {
        this.notebooksDirectory = notebooksDirectory;
        subscribers = new ArrayList();
        cat.debug("  Notebooks directory is: " + notebooksDirectory);
        Utils.createDirectory(notebooksDirectory);
    }
    public String create(String label, String uri, String annotation, boolean renderUi) throws Exception {
        cat.debug("Creating new notebook: " + label + " (" + uri + ")");
        String notebookDirectory = getNotebookDirectory(uri);
        if (new File(notebookDirectory).exists()) {
            return "EXISTS";
        }
        Utils.createDirectory(notebookDirectory + DIRECTORY_CONCEPTS);
        Utils.createDirectory(notebookDirectory + DIRECTORY_ANNOTATIONS);
        Resource resource = new Resource();
        resource.metadata.author = new URI(MindRaider.profile.profileName);
        resource.metadata.created = System.currentTimeMillis();
        resource.metadata.revision = 1;
        resource.metadata.timestamp = resource.metadata.created;
        resource.metadata.uri = new URI(uri);
        resource.addProperty(new LabelProperty(label));
        resource.data.addPropertyGroup(new ResourcePropertyGroup(NotebookResource.PROPERTY_GROUP_LABEL_CONCEPTS, new URI(NotebookResource.PROPERTY_GROUP_URI_CONCEPTS)));
        if (annotation == null) {
            annotation = "'" + label + "' notebook.";
        }
        resource.addProperty(new AnnotationProperty(annotation));
        String notebookResourceFilename = notebookDirectory + FILENAME_XML_RESOURCE;
        resource.toXmlFile(notebookResourceFilename);
        String notebookModelFilename = getModelFilenameByDirectory(notebookDirectory);
        MindRaider.spidersGraph.newModel(notebookModelFilename);
        RdfModel rdfModel = MindRaider.spidersGraph.getRdfModel();
        rdfModel.setFilename(notebookModelFilename);
        rdfModel.setType(RdfModel.FILE_MODEL_TYPE);
        com.hp.hpl.jena.rdf.model.Resource rdfResource = (com.hp.hpl.jena.rdf.model.Resource) rdfModel.newResource(uri, false);
        ResourceDescriptor resourceDescriptor = new ResourceDescriptor(label, uri);
        resourceDescriptor.created = resource.metadata.created;
        resourceDescriptor.annotationCite = annotation;
        createNotebookRdfResource(resourceDescriptor, rdfModel.getModel(), rdfResource);
        rdfModel.save();
        MindRaider.profile.setActiveNotebook(new URI(uri));
        activeNotebookResource = new NotebookResource(resource);
        activeNotebookResource.rdfModel = rdfModel;
        if (renderUi) {
            MindRaider.spidersGraph.renderModel();
        }
        for (int i = 0; i < subscribers.size(); i++) {
            ((NotebookCustodianListener) subscribers.get(i)).notebookCreated(activeNotebookResource);
        }
        return uri;
    }
    public boolean conceptExists(String uri) {
        if (MindRaider.profile.activeNotebookUri != null && activeNotebookResource != null && activeNotebookResource.rdfModel != null) {
            if (activeNotebookResource.rdfModel.getModel().containsResource(ResourceFactory.createResource(uri))) {
                return true;
            }
        }
        return false;
    }
    public String getModelFilenameByDirectory(String notebookDirectory) {
        return notebookDirectory + FILENAME_RDF_MODEL;
    }
    public String getResourceFilenameByDirectory(String notebookDirectory) {
        return notebookDirectory + FILENAME_XML_RESOURCE;
    }
    public String getNotebookDirectory(String uri) {
        String notebookDirectory = notebooksDirectory + File.separator + Utils.getNcNameFromUri(uri) + File.separator;
        return notebookDirectory;
    }
    public String getResourceFilename(String uri) {
        return getResourceFilenameByDirectory(getNotebookDirectory(uri));
    }
    public String getModelFilename(String uri) {
        return getModelFilenameByDirectory(getNotebookDirectory(uri));
    }
    public Resource get(String uri) {
        String notebookPath = getResourceFilenameByDirectory(getNotebookDirectory(uri));
        if (notebookPath != null) {
            try {
                return new Resource(notebookPath);
            } catch (Exception e) {
                cat.debug("Unable to load notebook: " + uri, e);
            }
        }
        return null;
    }
    public ResourceDescriptor[] getConceptDescriptors() {
        if (activeNotebookResource != null) {
            String[] conceptUris = activeNotebookResource.getConceptUris();
            if (conceptUris != null && conceptUris.length > 0) {
                ArrayList result = new ArrayList();
                for (int i = 0; i < conceptUris.length; i++) {
                    try {
                        result.add(new ResourceDescriptor(activeNotebookResource.rdfModel.getModel().getResource(conceptUris[i]).getProperty(RDFS.label).getObject().toString(), conceptUris[i]));
                    } catch (Exception e) {
                        cat.debug("Error: ", e);
                    }
                }
                return (ResourceDescriptor[]) result.toArray(new ResourceDescriptor[result.size()]);
            }
        }
        return null;
    }
    public String fsGetPath(String uri) {
        if (uri != null) {
            File f = new File(notebooksDirectory);
            File[] s = f.listFiles();
            if (s != null) {
                Resource resource;
                for (int i = 0; i < s.length; i++) {
                    if (s[i].isDirectory()) {
                        String notebookPath = s[i].getAbsolutePath() + File.separator + FILENAME_XML_RESOURCE;
                        try {
                            resource = new Resource(notebookPath);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        if (uri.equals(resource.metadata.uri.toASCIIString())) {
                            cat.debug("  Got resource path: " + notebookPath);
                            return notebookPath;
                        }
                    }
                }
            }
        }
        return null;
    }
    public String[] fsGetNotebooksUris() {
        File f = new File(notebooksDirectory);
        File[] s = f.listFiles();
        ArrayList result = new ArrayList();
        if (s != null) {
            Resource resource;
            for (int i = 0; i < s.length; i++) {
                if (s[i].isDirectory()) {
                    try {
                        resource = new Resource(s[i].getAbsolutePath() + File.separator + FILENAME_XML_RESOURCE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    result.add(resource.metadata.uri.toASCIIString());
                }
            }
        }
        return (String[]) (result.toArray(new String[result.size()]));
    }
    public Resource fsGet(String uri) {
        if (uri != null) {
            File f = new File(notebooksDirectory);
            File[] s = f.listFiles();
            if (s != null) {
                Resource resource;
                for (int i = 0; i < s.length; i++) {
                    if (s[i].isDirectory()) {
                        try {
                            resource = new Resource(s[i].getAbsolutePath() + File.separator + FILENAME_XML_RESOURCE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        if (uri.equals(resource.metadata.uri.toASCIIString())) {
                            cat.debug("  Got resource for uri: " + uri);
                            return resource;
                        }
                    }
                }
            }
        }
        return null;
    }
    public String getNotebookUriByLocalName(String localName) throws Exception {
        return MindRaiderVocabulary.getNotebookUri(localName);
    }
    public void rename(String notebookUri, String newLabel) throws Exception {
        cat.debug("Renaming notebook '" + notebookUri + "' to '" + newLabel + "'");
        if (notebookUri != null && newLabel != null) {
            NotebookResource notebookResource = new NotebookResource(get(notebookUri));
            notebookResource.getLabelProperty().labelContent = newLabel;
            notebookResource.save();
            MindRaider.folderCustodian.renameNotebook(notebookUri, newLabel);
        }
    }
    public void loadNotebook(URI uri) {
        MindRaider.setModeMindRaider();
        if (uri == null || "".equals(uri)) {
            StatusBar.show("Unable to load Notebook - URI is null!");
            return;
        } else {
            if (uri.equals(MindRaider.profile.getActiveNotebook()) && activeNotebookResource != null) {
                StatusBar.show("Notebook '" + uri.toString() + "' already loaded ;-)");
                return;
            }
        }
        StatusBar.show("Loading notebook '" + uri + "'...");
        String notebookResourceFilename = getResourceFilenameByDirectory(getNotebookDirectory(uri.toString()));
        Resource resource;
        try {
            resource = new Resource(notebookResourceFilename);
        } catch (Exception e) {
            cat.debug("Unable to load notebook " + uri, e);
            StatusBar.show("Error: Unable to load notebook " + uri + "! " + e.getMessage(), Color.RED);
            return;
        }
        String notebookModelFilename = getModelFilenameByDirectory(getNotebookDirectory(uri.toString()));
        try {
            MindRaider.spidersGraph.load(notebookModelFilename);
        } catch (Exception e1) {
            cat.debug("Unable to load notebook model: " + e1.getMessage(), e1);
            MindRaider.profile.setActiveNotebook(null);
            activeNotebookResource = null;
            return;
        }
        MindRaider.spidersGraph.selectNodeByUri(uri.toString());
        MindRaider.masterToolBar.setModelLocation(notebookModelFilename);
        MindRaider.profile.setActiveNotebook(uri);
        activeNotebookResource = new NotebookResource(resource);
        activeNotebookResource.rdfModel = MindRaider.spidersGraph.getRdfModel();
        if (resource != null) {
            MindRaider.history.add(resource.metadata.uri.toString());
        } else {
            cat.error("Resource " + uri + "not loaded is null!");
        }
    }
    public void save(Resource resource) throws Exception {
        if (resource != null) {
            resource.toXmlFile(getResourceFilename(resource.metadata.uri.toString()));
        }
    }
    public void close() {
        MindRaider.profile.activeNotebookUri = null;
        activeNotebookResource = null;
        MindRaider.spidersGraph.clear();
        MindRaider.spidersGraph.renderModel();
    }
    public boolean exists(String uri) {
        return MindRaider.folderCustodian.exists(uri);
    }
    public String getActiveNotebookDirectory() {
        if (activeNotebookResource != null) {
            String uri = activeNotebookResource.resource.metadata.uri.toASCIIString();
            return notebooksDirectory + File.separator + Utils.getNcNameFromUri(uri);
        } else {
            return null;
        }
    }
    public void subscribe(NotebookCustodianListener listener) {
        subscribers.add(listener);
    }
    public String getActiveNotebookNcName() {
        if (activeNotebookResource != null) {
            return Utils.getNcNameFromUri(activeNotebookResource.resource.metadata.uri.toASCIIString());
        }
        return null;
    }
    public Model getModel(String uri) throws Exception {
        if (uri != null) {
            String notebookModelPath = getModelFilename(uri);
            RdfModel rdfModel = new RdfModel(notebookModelPath);
            if (rdfModel != null) {
                return rdfModel.getModel();
            }
        }
        return null;
    }
    public static final int FORMAT_TWIKI = 1;
    public static final int FORMAT_OPML = 2;
    public static final int FORMAT_OPML_HTML = 3;
    public static final int FORMAT_TWIKI_HTML = 5;
    public void importNotebook(int importType, String srcFileName, ProgressDialogJFrame progressDialogJFrame) {
        cat.debug("=-> notebook import: " + srcFileName);
        if (srcFileName == null) {
            return;
        }
        if (MindRaider.notebookCustodian.activeNotebookResource != null) {
            try {
                switch(importType) {
                    case FORMAT_TWIKI:
                        twikiImport(srcFileName, progressDialogJFrame);
                        break;
                }
            } catch (Exception e) {
                cat.debug("Unable to import: ", e);
                return;
            }
            MindRaider.mainJFrame.requestFocus();
        }
    }
    private void twikiImport(String srcFileName, ProgressDialogJFrame progressDialogJFrame) throws Exception {
        cat.debug("=-> TWiki import: " + srcFileName);
        FileReader fileReader = new FileReader(srcFileName);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String folderUri = MindRaider.folderCustodian.FOLDER_TWIKI_IMPORT_URI;
        String notebookUri = null;
        MindRaider.folderCustodian.create("TWiki Import", MindRaider.folderCustodian.FOLDER_TWIKI_IMPORT_URI);
        String[] parentConceptUris = new String[50];
        String notebookLabel, line;
        String lastConceptName = null;
        StringBuffer annotation = new StringBuffer();
        while ((line = bufferedReader.readLine()) != null) {
            if (Pattern.matches("^---[+]+ .*", line)) {
                if (Pattern.matches("^---[+]{1} .*", line)) {
                    notebookLabel = line.substring(5);
                    cat.debug("LABEL: " + notebookLabel);
                    notebookUri = MindRaiderVocabulary.getNotebookUri(Utils.toNcName(notebookLabel));
                    String createdUri;
                    while (MindRaiderConstants.EXISTS.equals(createdUri = create(notebookLabel, notebookUri, null, false))) {
                        notebookUri += "_";
                    }
                    notebookUri = createdUri;
                    MindRaider.folderCustodian.addNotebook(folderUri, notebookUri);
                    activeNotebookResource.resource.addProperty(new SourceTwikiFileProperty(srcFileName));
                    activeNotebookResource.save();
                    cat.debug("Notebook created: " + notebookUri);
                } else {
                    twikiImportProcessLine(progressDialogJFrame, notebookUri, parentConceptUris, lastConceptName, annotation);
                    lastConceptName = line;
                }
                cat.debug(" SECTION: " + line);
            } else {
                annotation.append(line);
                annotation.append("\n");
            }
        }
        twikiImportProcessLine(progressDialogJFrame, notebookUri, parentConceptUris, lastConceptName, annotation);
        fileReader.close();
        ExplorerJPanel.getInstance().refresh();
        NotebookOutlineJPanel.getInstance().refresh();
        MindRaider.spidersGraph.renderModel();
    }
    private void twikiImportProcessLine(ProgressDialogJFrame progressDialogJFrame, String notebookUri, String[] parentConceptUris, String lastConceptName, StringBuffer annotation) throws Exception {
        if (lastConceptName == null) {
            activeNotebookResource.setAnnotation(annotation.toString());
            activeNotebookResource.save();
            annotation.setLength(0);
        } else {
            cat.debug("ANNOTATION:\n" + annotation.toString());
            StatusBar.show("Creating concept '" + lastConceptName + "'...");
            int depth = lastConceptName.indexOf(' ');
            lastConceptName = lastConceptName.substring(depth + 1);
            depth -= 4;
            cat.debug("Depth is: " + depth);
            cat.debug("Label is: " + lastConceptName);
            parentConceptUris[depth] = MindRaider.conceptCustodian.create(activeNotebookResource, parentConceptUris[depth - 1], lastConceptName, MindRaiderVocabulary.getConceptUri(Utils.getNcNameFromUri(notebookUri), "tempConcept" + System.currentTimeMillis()), annotation.toString(), false);
            if (progressDialogJFrame != null) {
                progressDialogJFrame.setProgressMessage(lastConceptName);
            }
            annotation.setLength(0);
        }
    }
    public void exportNotebook(int exportType, String dstFileName) {
        cat.debug("=-> notebook export: " + dstFileName);
        if (MindRaider.notebookCustodian.activeNotebookResource != null) {
            NotebookResourceExpanded notebookResourceExpanded;
            String resourcesFilePrefix = MindRaider.installationDirectory + File.separator + "java" + File.separator + "src" + File.separator + "resources" + File.separator;
            String xslFilePrefix = resourcesFilePrefix + "xsl" + File.separator;
            String cssFilePrefix = resourcesFilePrefix + "css" + File.separator;
            String jsFilePrefix = resourcesFilePrefix + "js" + File.separator;
            try {
                switch(exportType) {
                    case FORMAT_TWIKI:
                        notebookResourceExpanded = new NotebookResourceExpanded(MindRaider.notebookCustodian.activeNotebookResource, TWikifier.getInstance());
                        notebookResourceExpanded.save(dstFileName, xslFilePrefix + "export2TWiki.xsl");
                        break;
                    case FORMAT_TWIKI_HTML:
                        notebookResourceExpanded = new NotebookResourceExpanded(MindRaider.notebookCustodian.activeNotebookResource, TWikifier.getInstance());
                        String tmpOpml = dstFileName + ".tmp";
                        notebookResourceExpanded.save(tmpOpml, xslFilePrefix + "export2TWiki.xsl");
                        FileInputStream fileInputStream = new FileInputStream(new File(tmpOpml));
                        String htmlContent = "<html>" + " <head>" + "   <style type='text/css'>" + "     ul, ol {" + "         margin-top: 0px;" + "         margin-bottom: 3px;" + "         margin-left: 25px;" + "     }" + "     body {" + "         font-family: arial, helvetica, sans-serif; " + "         font-size: small;" + "     }" + "   </style>" + " </head>" + "<body>\n" + TwikiToHtml.translate(fileInputStream) + "\n</body>" + "</html>";
                        File twikiHtmlFile = new File(dstFileName);
                        FileWriter fileWriter = null;
                        try {
                            fileWriter = new FileWriter(twikiHtmlFile);
                            fileWriter.write(htmlContent);
                        } finally {
                            fileWriter.flush();
                            fileWriter.close();
                        }
                        break;
                    case FORMAT_OPML:
                        notebookResourceExpanded = new NotebookResourceExpanded(MindRaider.notebookCustodian.activeNotebookResource, Opmlizer.getInstance());
                        notebookResourceExpanded.save(dstFileName, xslFilePrefix + "export2Opml.xsl");
                        break;
                    case FORMAT_OPML_HTML:
                        notebookResourceExpanded = new NotebookResourceExpanded(MindRaider.notebookCustodian.activeNotebookResource, Opmlizer.getInstance());
                        tmpOpml = dstFileName + ".tmp";
                        notebookResourceExpanded.save(tmpOpml, xslFilePrefix + "export2OpmlInternal.xsl");
                        Xsl.xsl(tmpOpml, dstFileName, xslFilePrefix + "opml2Html.xsl");
                        File dstDir = new File(dstFileName);
                        String dstDirectory = dstDir.getParent();
                        Utils.copyFile(cssFilePrefix + "opml.css", dstDirectory + File.separator + "opml.css");
                        Utils.copyFile(jsFilePrefix + "opml.js", dstDirectory + File.separator + "opml.js");
                        break;
                }
            } catch (Exception e) {
                cat.error("Unable to export notebook!", e);
            }
        }
    }
    public String getActiveNotebookLabel() {
        if (activeNotebookResource != null) {
            return activeNotebookResource.getLabel();
        } else {
            return null;
        }
    }
    public long getActiveNotebookCreationTimestamp() {
        if (activeNotebookResource != null) {
            return activeNotebookResource.resource.metadata.created;
        } else {
            return 0;
        }
    }
    public String getActiveNotebookAnnotation() {
        if (activeNotebookResource != null) {
            AnnotationProperty annotationProperty = activeNotebookResource.getAnnotationProperty();
            if (annotationProperty != null) {
                return annotationProperty.annotation;
            }
        }
        return null;
    }
    public int getActiveNotebookChildCount() {
        if (activeNotebookResource != null) {
            Seq seq = activeNotebookResource.rdfModel.getModel().getSeq(activeNotebookResource.resource.metadata.uri.toString());
            return seq.size();
        }
        return 0;
    }
    public ResourceDescriptor getActiveNotebookChildAt(int i) {
        if (activeNotebookResource != null) {
            Seq seq = activeNotebookResource.rdfModel.getModel().getSeq(activeNotebookResource.resource.metadata.uri.toString());
            return getFullResourceDescriptor(i, seq);
        }
        return null;
    }
    public ResourceDescriptor getActiveNotebookConceptChildAt(int i, String conceptUri) {
        if (activeNotebookResource != null && conceptUri != null) {
            Seq seq = activeNotebookResource.rdfModel.getModel().getSeq(conceptUri);
            return getFullResourceDescriptor(i, seq);
        }
        return null;
    }
    public int getActiveNotebookConceptChildCount(String conceptUri) {
        if (activeNotebookResource != null && conceptUri != null && conceptUri.length() > 0) {
            Seq seq = activeNotebookResource.rdfModel.getModel().getSeq(conceptUri);
            return seq.size();
        }
        return 0;
    }
    public ResourceDescriptor getFullResourceDescriptor(String conceptUri) {
        if (activeNotebookResource != null && conceptUri != null && conceptUri.length() > 0) {
            com.hp.hpl.jena.rdf.model.Resource resource = activeNotebookResource.rdfModel.getModel().getResource(conceptUri);
            return getFullResourceDescriptor(resource);
        }
        return null;
    }
    private ResourceDescriptor getFullResourceDescriptor(int i, Seq seq) {
        ResourceDescriptor result = null;
        if (i < seq.size()) {
            com.hp.hpl.jena.rdf.model.Resource resource = seq.getResource(i + 1);
            if (resource != null) {
                result = getFullResourceDescriptor(resource);
            }
        }
        return result;
    }
    public ResourceDescriptor getFullResourceDescriptor(com.hp.hpl.jena.rdf.model.Resource resource) {
        ResourceDescriptor result;
        Statement statement;
        result = new ResourceDescriptor();
        result.uri = resource.getURI();
        if ((statement = resource.getProperty(RDFS.label)) != null) {
            result.label = statement.getObject().toString();
        } else {
            result.label = Utils.getNcNameFromUri(result.uri);
        }
        if ((statement = resource.getProperty(RDFS.comment)) != null) {
            result.annotationCite = statement.getObject().toString();
        } else {
            result.annotationCite = "";
        }
        if ((statement = resource.getProperty(DC.date)) != null) {
            if (statement.getObject().toString() != null) {
                result.created = Long.valueOf(statement.getObject().toString()).longValue();
            }
        } else {
            result.created = 0;
        }
        return result;
    }
    public static void createNotebookRdfResource(ResourceDescriptor notebook, Model oldModel, com.hp.hpl.jena.rdf.model.Resource notebookRdf) {
        notebookRdf.addProperty(RDF.type, RDF.Seq);
        notebookRdf.addProperty(RDF.type, oldModel.createResource(MindRaiderConstants.MR_OWL_CLASS_NOTEBOOK));
        notebookRdf.addProperty(RDFS.label, oldModel.createLiteral(notebook.label));
        notebookRdf.addProperty(DC.date, oldModel.createLiteral(notebook.created));
        notebookRdf.addProperty(RDFS.comment, oldModel.createLiteral(NotebookOutlineTreeInstance.getAnnotationCite(notebook.annotationCite)));
    }
}
