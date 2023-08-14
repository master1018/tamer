public class SmileyResources implements AbstractMessageParser.Resources {
    private HashMap<String, Integer> mSmileyToRes = new HashMap<String, Integer>();
    public SmileyResources(String[] smilies, int[] smileyResIds) {
        for (int i = 0; i < smilies.length; i++) {
            TrieNode.addToTrie(smileys, smilies[i], "");
            mSmileyToRes.put(smilies[i], smileyResIds[i]);
        }
    }
    public int getSmileyRes(String smiley) {
        Integer i = mSmileyToRes.get(smiley);
        if (i == null) {
            return -1;
        }
        return i.intValue();
    }
    private final TrieNode smileys = new TrieNode();
    public Set<String> getSchemes() {
        return null;
    }
    public TrieNode getDomainSuffixes() {
        return null;
    }
    public TrieNode getSmileys() {
        return smileys;
    }
    public TrieNode getAcronyms() {
        return null;
    }
}
