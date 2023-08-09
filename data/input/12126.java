public class ModelPatch extends Patch {
    private boolean percussion = false;
    public ModelPatch(int bank, int program) {
        super(bank, program);
    }
    public ModelPatch(int bank, int program, boolean percussion) {
        super(bank, program);
        this.percussion = percussion;
    }
    public boolean isPercussion() {
        return percussion;
    }
}
