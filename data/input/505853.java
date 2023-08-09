public class LiteralOpUpgrader {
    private final SsaMethod ssaMeth;
    public static void process(SsaMethod ssaMethod) {
        LiteralOpUpgrader dc;
        dc = new LiteralOpUpgrader(ssaMethod);
        dc.run();
    }
    private LiteralOpUpgrader(SsaMethod ssaMethod) {
        this.ssaMeth = ssaMethod;
    }
    private static boolean isConstIntZeroOrKnownNull(RegisterSpec spec) {
        TypeBearer tb = spec.getTypeBearer();
        if (tb instanceof CstLiteralBits) {
            CstLiteralBits clb = (CstLiteralBits) tb;
            return (clb.getLongBits() == 0);
        }
        return false;
    }
    private void run() {
        final TranslationAdvice advice = Optimizer.getAdvice();
        ssaMeth.forEachInsn(new SsaInsn.Visitor() {
            public void visitMoveInsn(NormalSsaInsn insn) {
            }
            public void visitPhiInsn(PhiInsn insn) {
            }
            public void visitNonMoveInsn(NormalSsaInsn insn) {
                Insn originalRopInsn = insn.getOriginalRopInsn();
                Rop opcode = originalRopInsn.getOpcode();
                RegisterSpecList sources = insn.getSources();
                if (sources.size() != 2 ) {
                    return;
                }
                if (opcode.getBranchingness() == Rop.BRANCH_IF) {
                    if (isConstIntZeroOrKnownNull(sources.get(0))) {
                        replacePlainInsn(insn, sources.withoutFirst(),
                                RegOps.flippedIfOpcode(opcode.getOpcode()));
                    } else if (isConstIntZeroOrKnownNull(sources.get(1))) {
                        replacePlainInsn(insn, sources.withoutLast(),
                                opcode.getOpcode());
                    }
                } else if (advice.hasConstantOperation(
                        opcode, sources.get(0), sources.get(1))) {
                    insn.upgradeToLiteral();
                } else  if (opcode.isCommutative()
                        && advice.hasConstantOperation(
                        opcode, sources.get(1), sources.get(0))) {
                    insn.setNewSources(
                            RegisterSpecList.make(
                                    sources.get(1), sources.get(0)));
                    insn.upgradeToLiteral();
                }
            }
        });
    }
    private void replacePlainInsn(NormalSsaInsn insn,
            RegisterSpecList newSources, int newOpcode) {
        Insn originalRopInsn = insn.getOriginalRopInsn();
        Rop newRop = Rops.ropFor(newOpcode,
                insn.getResult(), newSources, null);
        Insn newRopInsn = new PlainInsn(newRop,
                originalRopInsn.getPosition(), insn.getResult(),
                newSources);
        NormalSsaInsn newInsn
                = new NormalSsaInsn(newRopInsn, insn.getBlock());
        List<SsaInsn> insns = insn.getBlock().getInsns();
        ssaMeth.onInsnRemoved(insn);
        insns.set(insns.lastIndexOf(insn), newInsn);
        ssaMeth.onInsnAdded(newInsn);
    }
}
