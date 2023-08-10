public class Toggle extends Control implements Sensory {
    @Override
    protected List<String> getAvailableActions() {
        List<String> availableActions = new ArrayList<String>();
        availableActions.add("0");
        availableActions.add("1");
        availableActions.add("2");
        availableActions.add("status");
        return availableActions;
    }
    @Override
    public int fetchSensorID() {
        return getSensor().getSensorID();
    }
}
