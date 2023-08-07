public final class SpellCheckerFactory {
    private SpellCheckerFactory() {
    }
    public static SpellChecker create() {
        return new JazzySpellChecker();
    }
}
