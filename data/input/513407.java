public class Perspective implements IPerspectiveFactory {
    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(false);
        String editorArea = layout.getEditorArea();
        IFolderLayout folder;
        folder = layout.createFolder("logcat", IPageLayout.BOTTOM, 0.8f, 
                editorArea);
        folder.addPlaceholder(LogCatView.ID + ":*"); 
        folder.addView(LogCatView.ID);
        folder = layout.createFolder("devices", IPageLayout.LEFT, 0.3f, 
                editorArea);
        folder.addPlaceholder(DeviceView.ID + ":*"); 
        folder.addView(DeviceView.ID);
        folder = layout.createFolder("emulator", IPageLayout.BOTTOM, 0.5f, 
                "devices");
        folder.addPlaceholder(EmulatorControlView.ID + ":*"); 
        folder.addView(EmulatorControlView.ID);
        folder = layout.createFolder("ddms-detail", IPageLayout.RIGHT, 0.5f, 
                editorArea);
        folder.addPlaceholder(ThreadView.ID + ":*"); 
        folder.addView(ThreadView.ID);
        folder.addView(HeapView.ID);
        folder.addView(AllocTrackerView.ID);
        folder.addView(FileExplorerView.ID);
        layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); 
        layout.addPerspectiveShortcut("org.eclipse.debug.ui.DebugPerspective"); 
        layout.addPerspectiveShortcut("org.eclipse.jdt.ui.JavaPerspective"); 
        layout.addShowViewShortcut(DeviceView.ID);
        layout.addShowViewShortcut(FileExplorerView.ID);
        layout.addShowViewShortcut(HeapView.ID);
        layout.addShowViewShortcut(AllocTrackerView.ID);
        layout.addShowViewShortcut(LogCatView.ID);
        layout.addShowViewShortcut(ThreadView.ID);
        layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
        layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    }
}
