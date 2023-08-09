public class SetDefaultConnectionsEnabled {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setDefaultConnectionsEnabled(true);
        if(performer.isDefaultConnectionsEnabled() != true)
            throw new RuntimeException("performer.isAddDefaultConnectionsEnabled() didn't return true!");
        performer.setDefaultConnectionsEnabled(false);
        if(performer.isDefaultConnectionsEnabled() != false)
            throw new RuntimeException("performer.isAddDefaultConnectionsEnabled() didn't return false!");
    }
}
