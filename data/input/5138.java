public class SetSelfNonExclusive {
    public static void main(String[] args) throws Exception {
        ModelPerformer performer = new ModelPerformer();
        performer.setSelfNonExclusive(true);
        if(performer.isSelfNonExclusive() != true)
            throw new RuntimeException("performer.isSelfNonExclusive() didn't return true!");
        performer.setSelfNonExclusive(false);
        if(performer.isSelfNonExclusive() != false)
            throw new RuntimeException("performer.isSelfNonExclusive() didn't return false!");
    }
}
