public class ActionHandlerOpenViewTourBook extends AbstractHandler {
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        Util.showView(TourBookView.ID);
        return null;
    }
}
