public class CRTable
implements CRTFlags {
    private final boolean crtDebug = false;
    private ListBuffer<CRTEntry> entries = new ListBuffer<CRTEntry>();
    private Map<Object,SourceRange> positions = new HashMap<Object,SourceRange>();
    private Map<JCTree, Integer> endPositions;
    JCTree.JCMethodDecl methodTree;
    public CRTable(JCTree.JCMethodDecl tree, Map<JCTree, Integer> endPositions) {
        this.methodTree = tree;
        this.endPositions = endPositions;
    }
    public void put(Object tree, int flags, int startPc, int endPc) {
        entries.append(new CRTEntry(tree, flags, startPc, endPc));
    }
    public int writeCRT(ByteBuffer databuf, Position.LineMap lineMap, Log log) {
        int crtEntries = 0;
        new SourceComputer().csp(methodTree);
        for (List<CRTEntry> l = entries.toList(); l.nonEmpty(); l = l.tail) {
            CRTEntry entry = l.head;
            if (entry.startPc == entry.endPc)
                continue;
            SourceRange pos = positions.get(entry.tree);
            Assert.checkNonNull(pos, "CRT: tree source positions are undefined");
            if ((pos.startPos == Position.NOPOS) || (pos.endPos == Position.NOPOS))
                continue;
            if (crtDebug) {
                System.out.println("Tree: " + entry.tree + ", type:" + getTypes(entry.flags));
                System.out.print("Start: pos = " + pos.startPos + ", pc = " + entry.startPc);
            }
            int startPos = encodePosition(pos.startPos, lineMap, log);
            if (startPos == Position.NOPOS)
                continue;
            if (crtDebug) {
                System.out.print("End:   pos = " + pos.endPos + ", pc = " + (entry.endPc - 1));
            }
            int endPos = encodePosition(pos.endPos, lineMap, log);
            if (endPos == Position.NOPOS)
                continue;
            databuf.appendChar(entry.startPc);
            databuf.appendChar(entry.endPc - 1);
            databuf.appendInt(startPos);
            databuf.appendInt(endPos);
            databuf.appendChar(entry.flags);
            crtEntries++;
        }
        return crtEntries;
    }
    public int length() {
        return entries.length();
    }
    private String getTypes(int flags) {
        String types = "";
        if ((flags & CRT_STATEMENT)       != 0) types += " CRT_STATEMENT";
        if ((flags & CRT_BLOCK)           != 0) types += " CRT_BLOCK";
        if ((flags & CRT_ASSIGNMENT)      != 0) types += " CRT_ASSIGNMENT";
        if ((flags & CRT_FLOW_CONTROLLER) != 0) types += " CRT_FLOW_CONTROLLER";
        if ((flags & CRT_FLOW_TARGET)     != 0) types += " CRT_FLOW_TARGET";
        if ((flags & CRT_INVOKE)          != 0) types += " CRT_INVOKE";
        if ((flags & CRT_CREATE)          != 0) types += " CRT_CREATE";
        if ((flags & CRT_BRANCH_TRUE)     != 0) types += " CRT_BRANCH_TRUE";
        if ((flags & CRT_BRANCH_FALSE)    != 0) types += " CRT_BRANCH_FALSE";
        return types;
    }
     private int encodePosition(int pos, Position.LineMap lineMap, Log log) {
         int line = lineMap.getLineNumber(pos);
         int col = lineMap.getColumnNumber(pos);
         int new_pos = Position.encodePosition(line, col);
         if (crtDebug) {
             System.out.println(", line = " + line + ", column = " + col +
                                ", new_pos = " + new_pos);
         }
         if (new_pos == Position.NOPOS)
             log.warning(pos, "position.overflow", line);
        return new_pos;
     }
    class SourceComputer extends JCTree.Visitor {
        SourceRange result;
        public SourceRange csp(JCTree tree) {
            if (tree == null) return null;
            tree.accept(this);
            if (result != null) {
                positions.put(tree, result);
            }
            return result;
        }
        public SourceRange csp(List<? extends JCTree> trees) {
            if ((trees == null) || !(trees.nonEmpty())) return null;
            SourceRange list_sr = new SourceRange();
            for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) {
                list_sr.mergeWith(csp(l.head));
            }
            positions.put(trees, list_sr);
            return list_sr;
        }
        public SourceRange cspCases(List<JCCase> trees) {
            if ((trees == null) || !(trees.nonEmpty())) return null;
            SourceRange list_sr = new SourceRange();
            for (List<JCCase> l = trees; l.nonEmpty(); l = l.tail) {
                list_sr.mergeWith(csp(l.head));
            }
            positions.put(trees, list_sr);
            return list_sr;
        }
        public SourceRange cspCatchers(List<JCCatch> trees) {
            if ((trees == null) || !(trees.nonEmpty())) return null;
            SourceRange list_sr = new SourceRange();
            for (List<JCCatch> l = trees; l.nonEmpty(); l = l.tail) {
                list_sr.mergeWith(csp(l.head));
            }
            positions.put(trees, list_sr);
            return list_sr;
        }
        public void visitMethodDef(JCMethodDecl tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitVarDef(JCVariableDecl tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            csp(tree.vartype);
            sr.mergeWith(csp(tree.init));
            result = sr;
        }
        public void visitSkip(JCSkip tree) {
            SourceRange sr = new SourceRange(startPos(tree), startPos(tree));
            result = sr;
        }
        public void visitBlock(JCBlock tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            csp(tree.stats);    
            result = sr;
        }
        public void visitDoLoop(JCDoWhileLoop tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.body));
            sr.mergeWith(csp(tree.cond));
            result = sr;
        }
        public void visitWhileLoop(JCWhileLoop tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.cond));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitForLoop(JCForLoop tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.init));
            sr.mergeWith(csp(tree.cond));
            sr.mergeWith(csp(tree.step));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitForeachLoop(JCEnhancedForLoop tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.var));
            sr.mergeWith(csp(tree.expr));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitLabelled(JCLabeledStatement tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitSwitch(JCSwitch tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.selector));
            sr.mergeWith(cspCases(tree.cases));
            result = sr;
        }
        public void visitCase(JCCase tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.pat));
            sr.mergeWith(csp(tree.stats));
            result = sr;
        }
        public void visitSynchronized(JCSynchronized tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.lock));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitTry(JCTry tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.resources));
            sr.mergeWith(csp(tree.body));
            sr.mergeWith(cspCatchers(tree.catchers));
            sr.mergeWith(csp(tree.finalizer));
            result = sr;
        }
        public void visitCatch(JCCatch tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.param));
            sr.mergeWith(csp(tree.body));
            result = sr;
        }
        public void visitConditional(JCConditional tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.cond));
            sr.mergeWith(csp(tree.truepart));
            sr.mergeWith(csp(tree.falsepart));
            result = sr;
        }
        public void visitIf(JCIf tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.cond));
            sr.mergeWith(csp(tree.thenpart));
            sr.mergeWith(csp(tree.elsepart));
            result = sr;
        }
        public void visitExec(JCExpressionStatement tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.expr));
            result = sr;
        }
        public void visitBreak(JCBreak tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            result = sr;
        }
        public void visitContinue(JCContinue tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            result = sr;
        }
        public void visitReturn(JCReturn tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.expr));
            result = sr;
        }
        public void visitThrow(JCThrow tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.expr));
            result = sr;
        }
        public void visitAssert(JCAssert tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.cond));
            sr.mergeWith(csp(tree.detail));
            result = sr;
        }
        public void visitApply(JCMethodInvocation tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.meth));
            sr.mergeWith(csp(tree.args));
            result = sr;
        }
        public void visitNewClass(JCNewClass tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.encl));
            sr.mergeWith(csp(tree.clazz));
            sr.mergeWith(csp(tree.args));
            sr.mergeWith(csp(tree.def));
            result = sr;
        }
        public void visitNewArray(JCNewArray tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.elemtype));
            sr.mergeWith(csp(tree.dims));
            sr.mergeWith(csp(tree.elems));
            result = sr;
        }
        public void visitParens(JCParens tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.expr));
            result = sr;
        }
        public void visitAssign(JCAssign tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.lhs));
            sr.mergeWith(csp(tree.rhs));
            result = sr;
        }
        public void visitAssignop(JCAssignOp tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.lhs));
            sr.mergeWith(csp(tree.rhs));
            result = sr;
        }
        public void visitUnary(JCUnary tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.arg));
            result = sr;
        }
        public void visitBinary(JCBinary tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.lhs));
            sr.mergeWith(csp(tree.rhs));
            result = sr;
        }
        public void visitTypeCast(JCTypeCast tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.clazz));
            sr.mergeWith(csp(tree.expr));
            result = sr;
        }
        public void visitTypeTest(JCInstanceOf tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.expr));
            sr.mergeWith(csp(tree.clazz));
            result = sr;
        }
        public void visitIndexed(JCArrayAccess tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.indexed));
            sr.mergeWith(csp(tree.index));
            result = sr;
        }
        public void visitSelect(JCFieldAccess tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.selected));
            result = sr;
        }
        public void visitIdent(JCIdent tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            result = sr;
        }
        public void visitLiteral(JCLiteral tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            result = sr;
        }
        public void visitTypeIdent(JCPrimitiveTypeTree tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            result = sr;
        }
        public void visitTypeArray(JCArrayTypeTree tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.elemtype));
            result = sr;
        }
        public void visitTypeApply(JCTypeApply tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.clazz));
            sr.mergeWith(csp(tree.arguments));
            result = sr;
        }
        public void visitTypeParameter(JCTypeParameter tree) {
            SourceRange sr = new SourceRange(startPos(tree), endPos(tree));
            sr.mergeWith(csp(tree.bounds));
            result = sr;
        }
        public void visitWildcard(JCWildcard tree) {
            result = null;
        }
        public void visitErroneous(JCErroneous tree) {
            result = null;
        }
        public void visitTree(JCTree tree) {
            Assert.error();
        }
        public int startPos(JCTree tree) {
            if (tree == null) return Position.NOPOS;
            return tree.pos;
        }
        public int endPos(JCTree tree) {
            if (tree == null) return Position.NOPOS;
            if (tree.getTag() == JCTree.BLOCK)
                return ((JCBlock) tree).endpos;
            Integer endpos = endPositions.get(tree);
            if (endpos != null)
                return endpos.intValue();
            return Position.NOPOS;
        }
    }
    static class CRTEntry {
        Object tree;
        int flags;
        int startPc;
        int endPc;
        CRTEntry(Object tree, int flags, int startPc, int endPc) {
            this.tree = tree;
            this.flags = flags;
            this.startPc = startPc;
            this.endPc = endPc;
        }
    }
    static class SourceRange {
        int startPos;
        int endPos;
        SourceRange() {
            startPos = Position.NOPOS;
            endPos = Position.NOPOS;
        }
        SourceRange(int startPos, int endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }
        SourceRange mergeWith(SourceRange sr) {
            if (sr == null) return this;
            if (startPos == Position.NOPOS)
                startPos = sr.startPos;
            else if (sr.startPos != Position.NOPOS)
                startPos = (startPos < sr.startPos ? startPos : sr.startPos);
            if (endPos == Position.NOPOS)
                endPos = sr.endPos;
            else if (sr.endPos != Position.NOPOS)
                endPos = (endPos > sr.endPos ? endPos : sr.endPos);
            return this;
        }
    }
}
