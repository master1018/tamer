public abstract class AbstractDefaultControllerFactory<G extends Goban> extends TimeControlFactory implements KifooFactory<G> {
    public abstract G createGoban(int size, float komi);
    public abstract ContinuesSearch<G> createSearch(G goban);
    public Controller<G> createController(final G goban) {
        final SimpleSearchController<G> control = new SimpleSearchController<G>(createSearch(goban));
        control.setup(goban);
        return control;
    }
    public G createGoban(final int size, final float komi, final int handicap) {
        final G goban = createGoban(size, komi);
        goban.reset();
        return goban;
    }
}
