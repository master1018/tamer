public class RoundStateImpl implements RoundState {
    private final boolean finalRound;
    private final boolean errorRaised;
    private final boolean sourceFilesCreated;
    private final boolean classFilesCreated;
    public RoundStateImpl(boolean errorRaised,
                          boolean sourceFilesCreated,
                          boolean classFilesCreated,
                          Map<String,String> options) {
        this.finalRound =
            errorRaised ||
            (!sourceFilesCreated &&
            !(classFilesCreated && options.keySet().contains("-XclassesAsDecls")) );
        this.errorRaised = errorRaised;
        this.sourceFilesCreated = sourceFilesCreated;
        this.classFilesCreated = classFilesCreated;
    }
    public boolean finalRound() {
        return finalRound;
    }
    public boolean errorRaised() {
        return errorRaised;
    }
    public boolean sourceFilesCreated() {
        return sourceFilesCreated;
    }
    public boolean classFilesCreated() {
        return classFilesCreated;
    }
    public String toString() {
        return
            "[final round: " +  finalRound +
            ", error raised: " +  errorRaised +
            ", source files created: " + sourceFilesCreated +
            ", class files created: " + classFilesCreated + "]";
    }
}
