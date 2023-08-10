public class ExamContext {
    private DictionaryEntry dictionnaryEntry;
    private Knowledge knowledge;
    private int knowledgePos;
    private boolean hintNeeded;
    public ExamContext(Knowledge knowledge, DictionaryEntry dictionnaryEntry, int knowledgePos) {
        this.knowledge = knowledge;
        this.dictionnaryEntry = dictionnaryEntry;
        this.knowledgePos = knowledgePos;
    }
    public DictionaryEntry getDictionnaryEntry() {
        return dictionnaryEntry;
    }
    public int getKnowledgePos() {
        return knowledgePos;
    }
    public Knowledge getKnowledge() {
        return knowledge;
    }
    public boolean isHintNeeded() {
        return hintNeeded;
    }
    public void setHintNeeded(boolean hintNeeded) {
        this.hintNeeded = hintNeeded;
    }
}
