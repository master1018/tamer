public abstract class FoureverEditViewBase extends AbstractView implements FoureverEditView, PropertyChangeListener {
    protected ObjectModel objectModel;
    protected Instance selectedInstance;
    protected ObjectModelView objectModelView;
    protected ComplexInstanceView complexInstanceView;
    protected CreateNewEmptyModelFromSchemaCommand createFromSchemaCommand = null;
    protected CreateNewModelFromTemplateCommand createNewModelFromTemplateCommand = null;
    protected OpenObjectModelCommand openCommand = null;
    protected CloseCurrentObjectModelCommand closeCommand = null;
    protected CloseAllObjectModelsCommand closeAllCommand = null;
    protected LinkSubDocToCurrentFromTemplateCommand readSubDocFromTemplateCommand = null;
    protected LinkSubDocToCurrentFromFileCommand readSubDocFromFileCommand = null;
    protected SetCurrentFragmentLocationCommand setCurrentFragmentLocationCommand = null;
    protected SetCurrentDocumentRootLocationCommand setLocationOfCurrentDocRootCommand = null;
    protected ExportCommand exportCommand = null;
    protected CheckCommand checkCommand = null;
    protected UndoCommand undoCommand = null;
    protected RedoCommand redoCommand = null;
    protected SortCurrentComplexInstanceCommand sortCommand = null;
    protected MoveUpCommand moveUpCommand = null;
    protected MoveDownCommand moveDownCommand = null;
    protected ExitCommand exitCommand = null;
    protected SaveCurrentObjectModelCommand saveCommand = null;
    protected SetSelectedInstanceCommand setSelectedInstanceCommand = null;
    protected FoureverEditViewBase(GenericController ctrl) {
        super(ctrl);
        undoCommand = (UndoCommand) ctrl.getCommand("Undo");
        redoCommand = (RedoCommand) ctrl.getCommand("Redo");
        createFromSchemaCommand = (CreateNewEmptyModelFromSchemaCommand) ctrl.getCommand("CreateNewEmptyModelFromSchema");
        createNewModelFromTemplateCommand = (CreateNewModelFromTemplateCommand) ctrl.getCommand("CreateNewModelFromTemplate");
        openCommand = (OpenObjectModelCommand) ctrl.getCommand("OpenObjectModel");
        saveCommand = (SaveCurrentObjectModelCommand) ctrl.getCommand("SaveCurrentObjectModel");
        closeCommand = (CloseCurrentObjectModelCommand) ctrl.getCommand("CloseCurrentObjectModel");
        closeAllCommand = (CloseAllObjectModelsCommand) ctrl.getCommand("CloseAllObjectModels");
        exitCommand = (ExitCommand) ctrl.getCommand("Exit");
        sortCommand = (SortCurrentComplexInstanceCommand) ctrl.getCommand("SortCurrentComplexInstance");
        moveUpCommand = (MoveUpCommand) ctrl.getCommand("MoveUp");
        moveDownCommand = (MoveDownCommand) ctrl.getCommand("MoveDown");
        setSelectedInstanceCommand = (SetSelectedInstanceCommand) ctrl.getCommand("SetSelectedInstance");
        setCurrentFragmentLocationCommand = (SetCurrentFragmentLocationCommand) ctrl.getCommand("SetCurrentFragmentLocation");
        setLocationOfCurrentDocRootCommand = (SetCurrentDocumentRootLocationCommand) ctrl.getCommand("SetCurrentDocumentRootLocation");
        readSubDocFromFileCommand = (LinkSubDocToCurrentFromFileCommand) ctrl.getCommand("LinkSubDocToCurrentFromFile");
        readSubDocFromTemplateCommand = (LinkSubDocToCurrentFromTemplateCommand) ctrl.getCommand("LinkSubDocToCurrentFromTemplate");
        exportCommand = (ExportCommand) ctrl.getCommand("Export");
        checkCommand = (CheckCommand) ctrl.getCommand("Check");
        objectModelView = getViewManager().createObjectModelView(ctrl);
        complexInstanceView = getViewManager().createComplexInstanceView(ctrl);
        getController().addControllerPropertyListener("selectedInstance", this);
    }
    public abstract ViewManager getViewManager();
    public ObjectModel getObjectModel() {
        return objectModel;
    }
    public void setObjectModel(ObjectModel om) {
        if (om == objectModel) {
            return;
        }
        objectModel = om;
        objectModelView.setObjectModel(om);
    }
    public Instance getSelectedInstance() {
        return selectedInstance;
    }
    public void setSelectedInstance(Instance selected) {
        if (selected instanceof ComplexInstance) {
            selectedInstance = selected;
        } else {
            selectedInstance = null;
        }
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("selectedInstance")) {
            Instance selected = (Instance) evt.getNewValue();
            setSelectedInstance(selected);
        } else if (evt.getPropertyName().equals("currentObjectModel")) {
            objectModelView.setObjectModel((ObjectModel) evt.getNewValue());
        } else {
            assert (false);
        }
    }
    public void destroy() {
        openCommand = null;
        closeCommand = null;
        readSubDocFromTemplateCommand = null;
        readSubDocFromFileCommand = null;
        setCurrentFragmentLocationCommand = null;
        setLocationOfCurrentDocRootCommand = null;
        exportCommand = null;
        checkCommand = null;
        undoCommand = null;
        redoCommand = null;
        sortCommand = null;
        moveUpCommand = null;
        moveDownCommand = null;
        exitCommand = null;
        saveCommand = null;
        setSelectedInstanceCommand = null;
        complexInstanceView.destroy();
        complexInstanceView = null;
        objectModelView.destroy();
        objectModelView = null;
    }
}
