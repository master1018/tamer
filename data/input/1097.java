public class WaitAction extends BaseAction {
    private final Timer timer;
    public WaitAction(int timeToWait) {
        timer = new Timer(timeToWait);
    }
    @Override
    public void update(long time) {
        if (!timer.isActive()) {
            timer.setActive(true);
        }
        if (timer.action(time)) {
            setCompleted();
        }
    }
}
