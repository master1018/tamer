public class TestPerformer {
    private static final String UML_META_MODEL = UML2MetamodelPlugin.ID;
    private static final String ECORE_META_MODEL = EcoreMetamodelPlugin.ID;
    private static Logger log = Logger.getLogger(TestPerformer.class);
    private IMetamodel metaModel;
    private static URI instanceRoot;
    private IModelProvider modelProvider;
    private IModel testModel;
    IModelInstanceProvider modelInstanceProvider;
    private IModelInstance testModelInstance;
    private IOclInterpreter interpreter;
    private static IModelInstanceRegistry mir;
    private static TestEnvironment testEnv;
    private static IModelRegistry modelRegistry;
    private static URI dummyFile;
    static {
        testEnv = new TestEnvironment();
        PropertyConfigurator.configure("log4j.properties");
        mir = ModelBusPlugin.getModelInstanceRegistry();
        modelRegistry = ModelBusPlugin.getModelRegistry();
        dummyFile = URI.createURI("dummy.ocl");
        String bundleRoot = Platform.getBundle(TestautomationPlugin.PLUGIN_ID).getLocation().substring(10);
        URI bundleRootUri = URI.createURI(bundleRoot);
        URI goUpUri = URI.createURI("..");
        instanceRoot = goUpUri.resolve(bundleRootUri);
        assert (!instanceRoot.isFile());
        assert (instanceRoot.isHierarchical() && !instanceRoot.isRelative());
    }
    public TestPerformer(String modelPath, String modelInstancePath) throws TestInitializationException {
        this.initModelProvider(modelPath);
        this.modelInstanceProvider = new JavaModelInstanceProvider();
        if (modelPath == null || modelPath.isEmpty()) {
            throw new TestInitializationException("Model must not be null");
        }
        this.loadModel(URI.createURI(modelPath));
        if (modelInstancePath != null && !modelInstancePath.isEmpty()) {
            this.initInstanceAndInterpreter(URI.createURI(modelInstancePath));
        }
    }
    private void initModelProvider(String modelFile) {
        int index = modelFile.lastIndexOf('.');
        if (index == -1) {
            throw new RuntimeException("Unknown model file extension. Cannot determine meta model");
        }
        String ext = modelFile.substring(index + 1).toLowerCase();
        if (ext.equals("ecore")) {
            this.metaModel = ModelBusPlugin.getMetamodelRegistry().getMetamodel(ECORE_META_MODEL);
        } else if (ext.equals("uml")) {
            this.metaModel = ModelBusPlugin.getMetamodelRegistry().getMetamodel(UML_META_MODEL);
        } else {
            throw new RuntimeException("Unknown model file extension. Cannot load meta model for extension " + ext);
        }
        this.modelProvider = this.metaModel.getModelProvider();
    }
    private void initInstanceAndInterpreter(URI modelInstancePath) throws TestInitializationException {
        this.loadModelInstance(modelInstancePath);
        this.createInterpreter();
    }
    private void createInterpreter() throws TestInitializationException {
        try {
            this.interpreter = OclInterpreterPlugin.createInterpreter(this.testModelInstance);
        } catch (Throwable e) {
            throw new TestInitializationException(e);
        }
    }
    private void loadModel(URI modelPath) throws TestInitializationException {
        this.testModel = testEnv.getModelIfLoaded(modelPath);
        if (this.testModel == null) {
            String absoluteModelPath = this.getAbsolutePathForRelativeURI(modelPath);
            File modelFile = new File(absoluteModelPath);
            if (!modelFile.exists()) {
                throw new TestInitializationException("Model file " + modelFile.getAbsolutePath() + " doesn't exist");
            }
            try {
                this.testModel = this.modelProvider.getModel(modelFile);
                testEnv.addModel(modelPath, this.testModel);
            } catch (ModelAccessException e) {
                throw new TestInitializationException(e);
            }
        }
        this.refreshLoadedModel();
    }
    public void refreshLoadedModel() {
        modelRegistry.removeModel(this.testModel);
        modelRegistry.addModel(this.testModel);
        modelRegistry.setActiveModel(this.testModel);
    }
    private void loadModelInstance(URI filePath) throws TestInitializationException {
        if (this.testModel == null) {
            throw new TestInitializationException("no model found, can't load model instance");
        }
        this.testModelInstance = testEnv.getModelInstanceIfLoaded(filePath);
        if (this.testModelInstance != null) {
            return;
        }
        try {
            URI absolutePath = this.resolveAgainstInstanceURI(filePath);
            URL url = new URL(absolutePath.toString());
            this.testModelInstance = this.modelInstanceProvider.getModelInstance(url, this.testModel);
            testEnv.addModelInstance(filePath, this.testModelInstance);
            mir.addModelInstance(this.testModelInstance);
        } catch (ModelAccessException e) {
            throw new TestInitializationException(e);
        } catch (MalformedURLException e) {
            System.out.println("malformed url");
        }
    }
    public Constraint parseConstraint(String statement) throws TestExecutionException {
        ResourceSet rs = new ResourceSetImpl();
        OclResource resource = new OclResource(dummyFile);
        try {
            InputStream inputStream = this.createStatementInStream(statement);
            resource.reload(inputStream, null);
            rs.getResources().add(resource);
            resource.setModel(this.testModel);
            List<Constraint> parsedConstraints = OclPostParser.concreteSyntaxToEssentialOcl(resource);
            if (parsedConstraints.size() == 0) {
                throw new TestExecutionException("no successfully parsed constraint");
            }
            if (parsedConstraints.size() > 1) {
                throw new TestExecutionException("there is more than one parsed constraint " + "--> check how the constraint file is split");
            }
            return parsedConstraints.get(0);
        } catch (Throwable e) {
            String errors = this.getParseErrors(resource);
            throw new TestExecutionException(errors, e);
        }
    }
    private String getParseErrors(OclResource resource) {
        EList<Diagnostic> resourceErrors = resource.getErrors();
        StringBuilder builder = new StringBuilder();
        builder.append("Errors that occoured during parsing\n");
        if (!resourceErrors.isEmpty()) {
            for (Diagnostic error : resourceErrors) {
                builder.append("\t");
                builder.append(error.getMessage());
                builder.append("\n");
            }
        } else {
            builder.append("\t-- unknown.");
        }
        return builder.toString();
    }
    private InputStream createStatementInStream(String statement) throws UnsupportedEncodingException {
        log.debug(statement);
        return new ByteArrayInputStream(statement.getBytes("UTF-8"));
    }
    public IInterpretationResult doInterpret(Constraint constraint) {
        IModelInstanceObject obj = this.testModelInstance.getAllModelInstanceObjects().get(0);
        return this.interpreter.interpretConstraint(constraint, obj);
    }
    private URI resolveAgainstInstanceURI(URI path) {
        return path.resolve(instanceRoot);
    }
    private String getAbsolutePathForRelativeURI(URI path) {
        URI resolved = this.resolveAgainstInstanceURI(path);
        return resolved.path();
    }
}
