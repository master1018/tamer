public class ASDPhraseNode implements Cloneable {
    public ASDPhraseNode() {
        nodeWord = "";
        nodeInstance = null;
        nodeNext = null;
        nodeSubphrase = null;
        nodeValue = null;
    }
    public ASDGrammarNode instance() {
        return nodeInstance;
    }
    public ASDPhraseNode nextNode() {
        return nodeNext;
    }
    public ASDPhraseNode subphrase() {
        return nodeSubphrase;
    }
    public Object value() {
        return nodeValue;
    }
    public String word() {
        return nodeWord;
    }
    void setInstance(ASDGrammarNode node) {
        nodeInstance = node;
    }
    void setNextNode(ASDPhraseNode next) {
        nodeNext = next;
    }
    void setSubphrase(ASDPhraseNode sub) {
        nodeSubphrase = sub;
    }
    void setValue(Object newValue) {
        nodeValue = newValue;
    }
    void setWord(String newWord) {
        nodeWord = newWord;
    }
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
    public void showTree(ASDPhraseNode currentNode) {
        showTreeMark("", currentNode);
        System.out.println();
    }
    private void showTreeMark(String indentString, ASDPhraseNode aNode) {
        System.out.println();
        if (this == aNode) System.out.print("*->"); else System.out.print("   ");
        System.out.print(indentString + nodeWord + " ");
        if (nodeInstance != null) System.out.print(nodeInstance.instance()); else System.out.print("nil");
        if (nodeSubphrase != null) nodeSubphrase.showTreeMark(indentString + "   ", aNode);
        if (nodeNext != null) nodeNext.showTreeMark(indentString, aNode);
    }
    private String nodeWord;
    private ASDGrammarNode nodeInstance;
    private ASDPhraseNode nodeNext;
    private ASDPhraseNode nodeSubphrase;
    private Object nodeValue;
}
