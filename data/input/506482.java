public class PhiTypeResolver {
    SsaMethod ssaMeth;
    private final BitSet worklist;
    public static void process (SsaMethod ssaMeth) {
        new PhiTypeResolver(ssaMeth).run();
    }
    private PhiTypeResolver(SsaMethod ssaMeth) {
        this.ssaMeth = ssaMeth;
        worklist = new BitSet(ssaMeth.getRegCount());
    }
    private void run() {
        int regCount = ssaMeth.getRegCount();
        for (int reg = 0; reg < regCount; reg++) {
            SsaInsn definsn = ssaMeth.getDefinitionForRegister(reg);
            if (definsn != null
                    && (definsn.getResult().getBasicType() == Type.BT_VOID)) {
                worklist.set(reg);
            }
        }
        int reg;
        while ( 0 <= (reg = worklist.nextSetBit(0))) {
            worklist.clear(reg);
            PhiInsn definsn = (PhiInsn)ssaMeth.getDefinitionForRegister(reg);
            if (resolveResultType(definsn)) {
                List<SsaInsn> useList = ssaMeth.getUseListForRegister(reg);
                int sz = useList.size();
                for (int i = 0; i < sz; i++ ) {
                    SsaInsn useInsn = useList.get(i);
                    RegisterSpec resultReg = useInsn.getResult();
                    if (resultReg != null && useInsn instanceof PhiInsn) {
                        worklist.set(resultReg.getReg());
                    }
                }
            }
        }
    }
    private static boolean equalsHandlesNulls(LocalItem a, LocalItem b) {
        return (a == b) || ((a != null) && a.equals(b));
    }
    boolean resolveResultType(PhiInsn insn) {
        insn.updateSourcesToDefinitions(ssaMeth);
        RegisterSpecList sources = insn.getSources();
        RegisterSpec first = null;
        int firstIndex = -1;
        int szSources = sources.size();
        for (int i = 0 ; i <szSources ; i++) {
            RegisterSpec rs = sources.get(i);
            if (rs.getBasicType() != Type.BT_VOID) {
                first = rs;
                firstIndex = i;
            }
        }
        if (first == null) {
            return false;
        }
        LocalItem firstLocal = first.getLocalItem();
        TypeBearer mergedType = first.getType();
        boolean sameLocals = true;
        for (int i = 0 ; i < szSources ; i++) {
            if (i == firstIndex) {
                continue;
            }
            RegisterSpec rs = sources.get(i);
            if (rs.getBasicType() == Type.BT_VOID){
                continue;
            }
            sameLocals = sameLocals
                    && equalsHandlesNulls(firstLocal, rs.getLocalItem());
            mergedType = Merger.mergeType(mergedType, rs.getType());
        }
        TypeBearer newResultType;
        if (mergedType != null) {
            newResultType = mergedType;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < szSources; i++) {
                sb.append(sources.get(i).toString());
                sb.append(' ');
            }
            throw new RuntimeException ("Couldn't map types in phi insn:" + sb);
        }
        LocalItem newLocal = sameLocals ? firstLocal : null;
        RegisterSpec result = insn.getResult();
        if ((result.getTypeBearer() == newResultType)
                && equalsHandlesNulls(newLocal, result.getLocalItem())) {
            return false;
        }
        insn.changeResultType(newResultType, newLocal);
        return true;
    }
}
