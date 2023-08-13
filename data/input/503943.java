public class MoveParamCombiner {
    private final SsaMethod ssaMeth;
    public static void process(SsaMethod ssaMethod) {
        new MoveParamCombiner(ssaMethod).run();
    }
    private MoveParamCombiner(SsaMethod ssaMeth) {
        this.ssaMeth = ssaMeth;        
    }
    private void run() {
        final RegisterSpec[] paramSpecs
                = new RegisterSpec[ssaMeth.getParamWidth()];
        final HashSet<SsaInsn> deletedInsns = new HashSet();
        ssaMeth.forEachInsn(new SsaInsn.Visitor() {
            public void visitMoveInsn (NormalSsaInsn insn) {
            }
            public void visitPhiInsn (PhiInsn phi) {
            }
            public void visitNonMoveInsn (NormalSsaInsn insn) {
                if (insn.getOpcode().getOpcode() != RegOps.MOVE_PARAM) {
                    return;
                }
                int param = getParamIndex(insn);
                if (paramSpecs[param] == null) {
                    paramSpecs[param] = insn.getResult();
                } else {
                    final RegisterSpec specA = paramSpecs[param];
                    final RegisterSpec specB = insn.getResult();
                    LocalItem localA = specA.getLocalItem();
                    LocalItem localB = specB.getLocalItem();
                    LocalItem newLocal;
                    if (localA == null) {
                        newLocal = localB;
                    } else if (localB == null) {
                        newLocal = localA;
                    } else if (localA.equals(localB)) {
                        newLocal = localA;
                    } else {
                        return;
                    }
                    ssaMeth.getDefinitionForRegister(specA.getReg())
                            .setResultLocal(newLocal);
                    RegisterMapper mapper = new RegisterMapper() {
                        public int getNewRegisterCount() {
                            return ssaMeth.getRegCount();
                        }
                        public RegisterSpec map(RegisterSpec registerSpec) {
                            if (registerSpec.getReg() == specB.getReg()) {
                                return specA;
                            }
                            return registerSpec;
                        }
                    };
                    List<SsaInsn> uses
                            = ssaMeth.getUseListForRegister(specB.getReg());
                    for (int i = uses.size() - 1; i >= 0; i--) {
                        SsaInsn use = uses.get(i);                                
                        use.mapSourceRegisters(mapper);
                    }
                    deletedInsns.add(insn);
                }
            }
        });
        ssaMeth.deleteInsns(deletedInsns);
    }
    private int getParamIndex(NormalSsaInsn insn) {
        CstInsn cstInsn = (CstInsn)(insn.getOriginalRopInsn());
        int param = ((CstInteger)cstInsn.getConstant()).getValue();
        return param;
    }
}
