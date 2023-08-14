final class RBCollationTables {
    public RBCollationTables(String rules, int decmp) throws ParseException {
        this.rules = rules;
        RBTableBuilder builder = new RBTableBuilder(new BuildAPI());
        builder.build(rules, decmp); 
    }
    final class BuildAPI {
        private BuildAPI() {
        }
        void fillInTables(boolean f2ary,
                          boolean swap,
                          UCompactIntArray map,
                          Vector cTbl,
                          Vector eTbl,
                          IntHashtable cFlgs,
                          short mso,
                          short mto) {
            frenchSec = f2ary;
            seAsianSwapping = swap;
            mapping = map;
            contractTable = cTbl;
            expandTable = eTbl;
            contractFlags = cFlgs;
            maxSecOrder = mso;
            maxTerOrder = mto;
        }
    }
    public String getRules()
    {
        return rules;
    }
    public boolean isFrenchSec() {
        return frenchSec;
    }
    public boolean isSEAsianSwapping() {
        return seAsianSwapping;
    }
    Vector getContractValues(int ch)
    {
        int index = mapping.elementAt(ch);
        return getContractValuesImpl(index - CONTRACTCHARINDEX);
    }
    private Vector getContractValuesImpl(int index)
    {
        if (index >= 0)
        {
            return (Vector)contractTable.elementAt(index);
        }
        else 
        {
            return null;
        }
    }
    boolean usedInContractSeq(int c) {
        return contractFlags.get(c) == 1;
    }
    int getMaxExpansion(int order)
    {
        int result = 1;
        if (expandTable != null) {
            for (int i = 0; i < expandTable.size(); i++) {
                int[] valueList = (int [])expandTable.elementAt(i);
                int length = valueList.length;
                if (length > result && valueList[length-1] == order) {
                    result = length;
                }
            }
        }
        return result;
    }
    final int[] getExpandValueList(int order) {
        return (int[])expandTable.elementAt(order - EXPANDCHARINDEX);
    }
    int getUnicodeOrder(int ch)
    {
        return mapping.elementAt(ch);
    }
    short getMaxSecOrder() {
        return maxSecOrder;
    }
    short getMaxTerOrder() {
        return maxTerOrder;
    }
    static void reverse (StringBuffer result, int from, int to)
    {
        int i = from;
        char swap;
        int j = to - 1;
        while (i < j) {
            swap =  result.charAt(i);
            result.setCharAt(i, result.charAt(j));
            result.setCharAt(j, swap);
            i++;
            j--;
        }
    }
    final static int getEntry(Vector list, String name, boolean fwd) {
        for (int i = 0; i < list.size(); i++) {
            EntryPair pair = (EntryPair)list.elementAt(i);
            if (pair.fwd == fwd && pair.entryName.equals(name)) {
                return i;
            }
        }
        return UNMAPPED;
    }
    final static int EXPANDCHARINDEX = 0x7E000000; 
    final static int CONTRACTCHARINDEX = 0x7F000000;  
    final static int UNMAPPED = 0xFFFFFFFF;
    final static int PRIMARYORDERMASK = 0xffff0000;
    final static int SECONDARYORDERMASK = 0x0000ff00;
    final static int TERTIARYORDERMASK = 0x000000ff;
    final static int PRIMARYDIFFERENCEONLY = 0xffff0000;
    final static int SECONDARYDIFFERENCEONLY = 0xffffff00;
    final static int PRIMARYORDERSHIFT = 16;
    final static int SECONDARYORDERSHIFT = 8;
    private String rules = null;
    private boolean frenchSec = false;
    private boolean seAsianSwapping = false;
    private UCompactIntArray mapping = null;
    private Vector contractTable = null;
    private Vector expandTable = null;
    private IntHashtable contractFlags = null;
    private short maxSecOrder = 0;
    private short maxTerOrder = 0;
}
