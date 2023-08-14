public final class ConservativeTranslationAdvice
        implements TranslationAdvice {
    public static final ConservativeTranslationAdvice THE_ONE =
        new ConservativeTranslationAdvice();
    private ConservativeTranslationAdvice() {
    }
    public boolean hasConstantOperation(Rop opcode,
            RegisterSpec sourceA, RegisterSpec sourceB) {
        return false;
    }
    public boolean requiresSourcesInOrder(Rop opcode,
            RegisterSpecList sources) {
        return false;
    }
    public int getMaxOptimalRegisterCount() {
        return Integer.MAX_VALUE;
    }
}
