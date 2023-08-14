public final class EclipseUiHelper {
    public static final String CONTENT_OUTLINE_VIEW_ID = "org.eclipse.ui.views.ContentOutline";
    public static final String PROPERTY_SHEET_VIEW_ID  = "org.eclipse.ui.views.PropertySheet";
    private EclipseUiHelper() {
    }
    public static void showView(String viewId, boolean activate) {
        IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (win != null) {
            IWorkbenchPage page = win.getActivePage();
            if (page != null) {
                try {
                    IViewPart part = page.showView(viewId,
                            null ,
                            activate ? IWorkbenchPage.VIEW_ACTIVATE : IWorkbenchPage.VIEW_VISIBLE);
                } catch (PartInitException e) {
                }
            }
        }
    }
}
