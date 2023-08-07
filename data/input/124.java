public class Todo extends _Todo {
    public void setPersistenceState(int state) {
        super.setPersistenceState(state);
        if (state == PersistenceState.NEW) {
            this.setIsDone(Boolean.FALSE);
        }
    }
}
