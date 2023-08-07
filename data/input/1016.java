public class MasterTime extends Updater {
    private final Model model;
    public MasterTime(Model model, double updatesPerSecond) {
        super(updatesPerSecond);
        this.model = model;
    }
    @Override
    public void doTimedTask() {
        model.update();
    }
}
