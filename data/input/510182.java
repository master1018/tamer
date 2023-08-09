public class EventLogView extends SelectionDependentViewPart {
    private EventLogPanel mLogPanel;
    @Override
    public void createPartControl(Composite parent) {
        ImageLoader loader = DdmsPlugin.getImageLoader();
        CommonAction optionsAction = new CommonAction("Options...");
        optionsAction.setToolTipText("Opens the options panel");
        optionsAction.setImageDescriptor(loader
                .loadDescriptor("edit.png")); 
        CommonAction clearLogAction = new CommonAction("Clear Log");
        clearLogAction.setToolTipText("Clears the event log");
        clearLogAction.setImageDescriptor(loader
                .loadDescriptor("clear.png")); 
        CommonAction saveAction = new CommonAction("Save Log");
        saveAction.setToolTipText("Saves the event log");
        saveAction.setImageDescriptor(loader
                .loadDescriptor("save.png")); 
        CommonAction loadAction = new CommonAction("Load Log");
        loadAction.setToolTipText("Loads an event log");
        loadAction.setImageDescriptor(loader
                .loadDescriptor("load.png")); 
        CommonAction importBugAction = new CommonAction("Import Bug Report Log");
        importBugAction.setToolTipText("Imports a bug report.");
        importBugAction.setImageDescriptor(loader
                .loadDescriptor("importBug.png")); 
        placeActions(optionsAction, clearLogAction, saveAction, loadAction, importBugAction);
        mLogPanel = new EventLogPanel(DdmsPlugin.getImageLoader());
        mLogPanel.setActions(optionsAction, clearLogAction, saveAction, loadAction, importBugAction);
        mLogPanel.createPanel(parent);
        setSelectionDependentPanel(mLogPanel);
    }
    @Override
    public void setFocus() {
        mLogPanel.setFocus();
    }
    @Override
    public void dispose() {
        if (mLogPanel != null) {
            mLogPanel.stopEventLog(true);
        }
    }
    private void placeActions(IAction optionAction, IAction clearAction, IAction saveAction,
            IAction loadAction, CommonAction importBugAction) {
        IActionBars actionBars = getViewSite().getActionBars();
        IMenuManager menuManager = actionBars.getMenuManager();
        menuManager.add(clearAction);
        menuManager.add(new Separator());
        menuManager.add(saveAction);
        menuManager.add(loadAction);
        menuManager.add(importBugAction);
        menuManager.add(new Separator());
        menuManager.add(optionAction);
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(clearAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(saveAction);
        toolBarManager.add(loadAction);
        toolBarManager.add(importBugAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(optionAction);
    }
}
