public class GivenMovieMiniController extends AbstractMiniController<GivenMovieDetailDTO> {
    public GivenMovieMiniController(IController<?> parent) {
        super(parent, "GivenMovieDetail");
    }
    @Override
    public int getWidth() {
        return 230;
    }
    @Override
    public IBrowser<GivenMovieDetailDTO> createBrowser() {
        return new GivenMovieMiniBrowser(this);
    }
    @Override
    public IEditor<GivenMovieDetailDTO> createEditor() {
        return null;
    }
    @Override
    public void beforeEvent(Event event) {
        switch(event.getType()) {
            case SpecialEvent.Delete:
                {
                    GivenMovieDetailDTO detail = (GivenMovieDetailDTO) event.getModel();
                    if (detail.getId() != null && !detail.getTransId().equals(detail.getMovie().getLastTransId())) {
                        event.setCancel(true);
                        Utils.showAlert(Tags.get("cantDeleteThisMovie"));
                        break;
                    }
                }
        }
    }
}
