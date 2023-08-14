public final class ManifestEditor extends AndroidEditor {
    public static final String ID = AndroidConstants.EDITORS_NAMESPACE + ".manifest.ManifestEditor"; 
    private final static String EMPTY = ""; 
    private UiElementNode mUiManifestNode;
    private ApplicationPage mAppPage;
    private OverviewPage mOverviewPage;
    private PermissionPage mPermissionPage;
    private InstrumentationPage mInstrumentationPage;
    private IFileListener mMarkerMonitor;
    public ManifestEditor() {
        super();
    }
    @Override
    public void dispose() {
        super.dispose();
        GlobalProjectMonitor.getMonitor().removeFileListener(mMarkerMonitor);
    }
    @Override
    public UiElementNode getUiRootNode() {
        return mUiManifestNode;
    }
    public AndroidManifestDescriptors getManifestDescriptors() {
        AndroidTargetData data = getTargetData();
        if (data != null) {
            return data.getManifestDescriptors();
        }
        return null;
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }
    @Override
    protected void createFormPages() {
        try {
            addPage(mOverviewPage = new OverviewPage(this));
            addPage(mAppPage = new ApplicationPage(this));
            addPage(mPermissionPage = new PermissionPage(this));
            addPage(mInstrumentationPage = new InstrumentationPage(this));
        } catch (PartInitException e) {
            AdtPlugin.log(e, "Error creating nested page"); 
        }
    }
    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        IFile inputFile = getInputFile();
        if (inputFile != null) {
            startMonitoringMarkers();
            setPartName(String.format("%1$s Manifest", inputFile.getProject().getName()));
        }
    }
    @Override
    protected void xmlModelChanged(Document xml_doc) {
        initUiRootNode(false );
        loadFromXml(xml_doc);
        super.xmlModelChanged(xml_doc);
    }
    private void loadFromXml(Document xmlDoc) {
        mUiManifestNode.setXmlDocument(xmlDoc);
        Node node = getManifestXmlNode(xmlDoc);
        if (node != null) {
            mUiManifestNode.loadFromXmlNode(node);
        }
    }
    private Node getManifestXmlNode(Document xmlDoc) {
        if (xmlDoc != null) {
            ElementDescriptor manifest_desc = mUiManifestNode.getDescriptor();
            try {
                XPath xpath = AndroidXPathFactory.newXPath();
                Node node = (Node) xpath.evaluate("/" + manifest_desc.getXmlName(),  
                        xmlDoc,
                        XPathConstants.NODE);
                assert node != null && node.getNodeName().equals(manifest_desc.getXmlName());
                return node;
            } catch (XPathExpressionException e) {
                AdtPlugin.log(e, "XPath error when trying to find '%s' element in XML.", 
                        manifest_desc.getXmlName());
            }
        }
        return null;
    }
    private void onDescriptorsChanged() {
        Node node = getManifestXmlNode(getXmlDocument(getModelForRead()));
        mUiManifestNode.reloadFromXmlNode(node);
        if (mOverviewPage != null) {
            mOverviewPage.refreshUiApplicationNode();
        }
        if (mAppPage != null) {
            mAppPage.refreshUiApplicationNode();
        }
        if (mPermissionPage != null) {
            mPermissionPage.refreshUiNode();
        }
        if (mInstrumentationPage != null) {
            mInstrumentationPage.refreshUiNode();
        }
    }
    private void startMonitoringMarkers() {
        final IFile inputFile = getInputFile();
        if (inputFile != null) {
            updateFromExistingMarkers(inputFile);
            mMarkerMonitor = new IFileListener() {
                public void fileChanged(IFile file, IMarkerDelta[] markerDeltas, int kind) {
                    if (file.equals(inputFile)) {
                        processMarkerChanges(markerDeltas);
                    }
                }
            };
            GlobalProjectMonitor.getMonitor().addFileListener(mMarkerMonitor, IResourceDelta.CHANGED);
        }
    }
    private void updateFromExistingMarkers(IFile inputFile) {
        try {
            IMarker[] markers = inputFile.findMarkers(AndroidConstants.MARKER_ANDROID, true,
                    IResource.DEPTH_ZERO);
            AndroidManifestDescriptors desc = getManifestDescriptors();
            if (desc != null) {
                ElementDescriptor appElement = desc.getApplicationElement();
                if (appElement != null) {
                    UiElementNode app_ui_node = mUiManifestNode.findUiChildNode(
                            appElement.getXmlName());
                    List<UiElementNode> children = app_ui_node.getUiChildren();
                    for (IMarker marker : markers) {
                        processMarker(marker, children, IResourceDelta.ADDED);
                    }
                }
            }
        } catch (CoreException e) {
        }
    }
    private void processMarkerChanges(IMarkerDelta[] markerDeltas) {
        AndroidManifestDescriptors descriptors = getManifestDescriptors();
        if (descriptors != null && descriptors.getApplicationElement() != null) {
            UiElementNode app_ui_node = mUiManifestNode.findUiChildNode(
                    descriptors.getApplicationElement().getXmlName());
            List<UiElementNode> children = app_ui_node.getUiChildren();
            for (IMarkerDelta markerDelta : markerDeltas) {
                processMarker(markerDelta.getMarker(), children, markerDelta.getKind());
            }
        }
    }
    private void processMarker(IMarker marker, List<UiElementNode> nodeList, int kind) {
        String nodeType = marker.getAttribute(AndroidConstants.MARKER_ATTR_TYPE, EMPTY);
        if (nodeType == EMPTY) {
            return;
        }
        String className = marker.getAttribute(AndroidConstants.MARKER_ATTR_CLASS, EMPTY);
        if (className == EMPTY) {
            return;
        }
        for (UiElementNode ui_node : nodeList) {
            if (ui_node.getDescriptor().getXmlName().equals(nodeType)) {
                for (UiAttributeNode attr : ui_node.getUiAttributes()) {
                    if (attr.getDescriptor().getXmlLocalName().equals(
                            AndroidManifestDescriptors.ANDROID_NAME_ATTR)) {
                        if (attr.getCurrentValue().equals(className)) {
                            if (kind == IResourceDelta.REMOVED) {
                                attr.setHasError(false);
                            } else {
                                attr.setHasError(true);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
    @Override
    protected void initUiRootNode(boolean force) {
        if (mUiManifestNode != null && force == false) {
            return;
        }
        AndroidManifestDescriptors manifestDescriptor = getManifestDescriptors();
        if (manifestDescriptor != null) {
            ElementDescriptor manifestElement = manifestDescriptor.getManifestElement();
            mUiManifestNode = manifestElement.createUiNode();
            mUiManifestNode.setEditor(this);
            ElementDescriptor appElement = manifestDescriptor.getApplicationElement();
            boolean present = false;
            for (UiElementNode ui_node : mUiManifestNode.getUiChildren()) {
                if (ui_node.getDescriptor() == appElement) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                mUiManifestNode.appendNewUiChild(appElement);
            }
            appElement = manifestDescriptor.getUsesSdkElement();
            present = false;
            for (UiElementNode ui_node : mUiManifestNode.getUiChildren()) {
                if (ui_node.getDescriptor() == appElement) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                mUiManifestNode.appendNewUiChild(appElement);
            }
            onDescriptorsChanged();
        } else {
            ElementDescriptor desc = new ElementDescriptor("manifest", 
                    "temporary descriptors due to missing decriptors", 
                    null , null , null ,
                    null , false );
            mUiManifestNode = desc.createUiNode();
            mUiManifestNode.setEditor(this);
        }
    }
    private IFile getInputFile() {
        IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput) input;
            return fileInput.getFile();
        }
        return null;
    }
}
