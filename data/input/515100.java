public class WnnClause extends WnnWord {
    public WnnClause(String candidate, String stroke, WnnPOS posTag, int frequency) {
        super(candidate,
              stroke,
              posTag,
              frequency);
    }
    public WnnClause (String stroke, WnnWord stem) {
        super(stem.id,
              stem.candidate,
              stroke,
              stem.partOfSpeech,
              stem.frequency,
              0);
    }
    public WnnClause (String stroke, WnnWord stem, WnnWord fzk) {
        super(stem.id,
              stem.candidate + fzk.candidate,
              stroke,
              new WnnPOS(stem.partOfSpeech.left, fzk.partOfSpeech.right),
              stem.frequency,
              1);
    }
}
