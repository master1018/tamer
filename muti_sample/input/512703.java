public class DasmCatchBuilder implements CatchBuilder {
    private class UnprocessedCatch {
        String from;
        String to;
        Hashtable<CstType, String> type_branch = 
                new Hashtable<CstType, String>();
        UnprocessedCatch(String exception, String from, String to,
                String branch) {
            this.from = from;
            this.to = to;
            add(exception, branch);
        }
        void add(String exception, String branch) {
            CstType type;
            if (exception.compareToIgnoreCase("all") == 0)
                type = CstType.OBJECT;
            else
                type = CstType.intern(Type.internClassName(exception));
            String s = type_branch.get(type);
            if (s != null && s.compareToIgnoreCase(branch) != 0)
                throw new RuntimeException(
                        "Bad .catch directive: same exception (" + exception
                                + ") but different branch addresses (" + s
                                + " and " + branch + ")");
            type_branch.put(type, branch);
        }
    }
    private Vector<UnprocessedCatch> unprocessed_catches = 
            new Vector<UnprocessedCatch>();
    private Hashtable<String, LabelTableEntry> labels_table;
    public DasmCatchBuilder(Hashtable<String, LabelTableEntry> labels_table) {
        this.labels_table = labels_table;
    }
    public HashSet<Type> getCatchTypes() {
        int sz = unprocessed_catches.size();
        HashSet<Type> result = new HashSet<Type>(sz);
        for (int i = 0; i < sz; i++) {
            Enumeration<CstType> keys = unprocessed_catches.elementAt(i)
                    .type_branch.keys();
            while (keys.hasMoreElements()) {
                result.add(keys.nextElement().getClassType());
            }
        }
        return result;
    }
    public boolean hasAnyCatches() {
        return unprocessed_catches.size() != 0;
    }
    public void add(String exception, String start, String end, String branch) {
        int sz = unprocessed_catches.size();
        for (int i = 0; i < sz; i++) {
            UnprocessedCatch uc = unprocessed_catches.elementAt(i);
            if (uc.from.compareToIgnoreCase(start) == 0) {
                if (uc.to.compareToIgnoreCase(end) != 0)
                    throw new RuntimeException(
                            "Bad .catch directive: two blocks have the same "
                                    + "start address ("
                                    + uc.from
                                    + ") and different end addresses (" + uc.to
                                    + " and " + end + ")");
                uc.add(exception, branch);
                return;
            }
        }
        unprocessed_catches.add(new UnprocessedCatch(exception, start, end,
                branch));
    }
    public CatchTable build() {
        int sz = unprocessed_catches.size();
        CatchTable result = new CatchTable(sz);
        for (int i = 0; i < sz; i++) {
            UnprocessedCatch uc = unprocessed_catches.elementAt(i);
            LabelTableEntry lte = labels_table.get(uc.from);
            if (lte == null || lte.planted == false)
                throw new RuntimeException("Label " + uc.from + " not defined");
            CodeAddress from = lte.code_address;
            lte = labels_table.get(uc.to);
            if (lte == null || lte.planted == false)
                throw new RuntimeException("Label " + uc.to + " not defined");
            CodeAddress to = lte.code_address;
            CatchHandlerList chl = new CatchHandlerList(uc.type_branch.size());
            Enumeration<CstType> keys = uc.type_branch.keys();
            int j = 0;
            CatchHandlerList.Entry catch_all = null;
            while (keys.hasMoreElements()) {
                CstType type = keys.nextElement();
                String branch = uc.type_branch.get(type);
                lte = labels_table.get(branch);
                if (lte == null || lte.planted == false)
                    throw new RuntimeException("Label " + branch
                            + " not defined");
                CatchHandlerList.Entry chle = new CatchHandlerList.Entry(type,
                        lte.code_address.getAddress());
                if (type.equals(CstType.OBJECT)) {
                    catch_all = chle;
                } else {
                    chl.set(j, chle);
                    j++;
                }
            }
            if (catch_all != null) chl.set(j, catch_all);
            chl.setImmutable();
            CatchTable.Entry entry = new CatchTable.Entry(from.getAddress(), to
                    .getAddress(), chl);
            result.set(i, entry);
        }
        return result;
    }
}
