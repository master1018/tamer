public class Perspective implements IPerspectiveFactory {
    @Override
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.setFixed(true);
        layout.addView(NotesViewPart.VIEW_ID, IPageLayout.TOP, 0.3f, editorArea);
        layout.addView(CalendarViewPart.VIEW_ID, IPageLayout.BOTTOM, 0.5f, editorArea);
    }
}
