abstract class InstructionAssembler extends InstructionSyntax {
    InstructionAssembler() {
    }
    public static String assemble(Element instructions, String pcAttrName,
            ClassSyntax.GetCPIndex getCPI) {
        int insCount = instructions.size();
        Element[] insElems = new Element[insCount];
        int[] elemToIndexMap;
        int[] insLocs;
        byte[] ops = new byte[insCount];
        int[] operands = new int[insCount];
        boolean[] isWide = new boolean[insCount];
        int[] branches;
        int[] branchInsLocs;
        HashMap<String, String> labels = new HashMap<String, String>();
        final int WIDE = 0xc4;
        final int GOTO = 0xa7;
        final int GOTO_W = 0xc8;
        final int GOTO_LEN = 3;
        final int GOTO_W_LEN = 5;
        assert ("wide".equals(bcNames[WIDE]));
        assert ("goto".equals(bcNames[GOTO]));
        assert ("goto_w".equals(bcNames[GOTO_W]));
        assert (bcFormats[GOTO].length() == GOTO_LEN);
        assert (bcFormats[GOTO_W].length() == GOTO_W_LEN);
        {
            elemToIndexMap = (pcAttrName != null) ? new int[insCount] : null;
            int[] buffer = operands;
            int id = 0;
            int branchCount = 0;
            for (int i = 0; i < insCount; i++) {
                Element ins = (Element) instructions.get(i);
                if (elemToIndexMap != null) {
                    elemToIndexMap[i] = (ins.getAttr(pcAttrName) != null ? id : -1);
                }
                String lab = ins.getAttr("pc");
                if (lab != null) {
                    labels.put(lab, String.valueOf(id));
                }
                int op = opCode(ins.getName());
                if (op < 0) {
                    assert (ins.getAttr(pcAttrName) != null
                            || ins.getName().equals("label"));
                    continue;  
                }
                if (op == WIDE) { 
                    isWide[id] = true;  
                    continue;
                }
                if (bcFormats[op].indexOf('o') >= 0) {
                    buffer[branchCount++] = id;
                }
                if (bcFormats[op] == bcWideFormats[op]) {
                    isWide[id] = false;
                }
                insElems[id] = ins;
                ops[id] = (byte) op;
                id++;
            }
            insCount = id;  
            branches = new int[branchCount + 1];
            System.arraycopy(buffer, 0, branches, 0, branchCount);
            branches[branchCount] = -1;  
        }
        insLocs = new int[insCount + 1];
        int loc = 0;
        for (int bn = 0, id = 0; id < insCount; id++) {
            insLocs[id] = loc;
            Element ins = insElems[id];
            int op = ops[id] & 0xFF;
            String format = opFormat(op, isWide[id]);
            for (int j = 1, jlimit = format.length(); j < jlimit; j++) {
                char fc = format.charAt(j);
                int x = 0;
                switch (fc) {
                    case 'l':
                        x = (int) ins.getAttrLong("loc");
                        assert (x >= 0);
                        if (x > 0xFF && !isWide[id]) {
                            isWide[id] = true;
                            format = opFormat(op, isWide[id]);
                        }
                        assert (x <= 0xFFFF);
                        break;
                    case 'k':
                        char fc2 = format.charAt(Math.min(j + 1, format.length() - 1));
                        x = getCPIndex(ins, fc2, getCPI);
                        if (x > 0xFF && j == jlimit - 1) {
                            assert (op == 0x12); 
                            ops[id] = (byte) (op = 0x13); 
                            format = opFormat(op);
                        }
                        assert (x <= 0xFFFF);
                        j++;  
                        break;
                    case 'x':
                        x = (int) ins.getAttrLong("num");
                        assert (x >= 0 && x <= ((j == jlimit - 1) ? 0xFF : 0xFFFF));
                        break;
                    case 's':
                        x = (int) ins.getAttrLong("num");
                        if (x != (byte) x && j == jlimit - 1) {
                            switch (op) {
                                case 0x10: 
                                    ops[id] = (byte) (op = 0x11); 
                                    break;
                                case 0x84: 
                                    isWide[id] = true;
                                    format = opFormat(op, isWide[id]);
                                    break;
                                default:
                                    assert (false);  
                            }
                        }
                        if (j == jlimit - 1) {
                            assert (x == (byte) x);
                            x = x & 0xFF;
                        } else {
                            assert (x == (short) x);
                            x = x & 0xFFFF;
                        }
                        break;
                    case 'o':
                        assert (branches[bn] == id);
                        bn++;
                        insElems[id] = ins = new Element(ins);
                        String newLab = labels.get(ins.getAttr("lab"));
                        assert (newLab != null);
                        ins.setAttr("lab", newLab);
                        int prevCas = 0;
                        int k = 0;
                        for (Element cas : ins.elements()) {
                            assert (cas.getName().equals("Case"));
                            ins.set(k++, cas = new Element(cas));
                            newLab = labels.get(cas.getAttr("lab"));
                            assert (newLab != null);
                            cas.setAttr("lab", newLab);
                            int thisCas = (int) cas.getAttrLong("num");
                            assert (op == 0xab
                                    || op == 0xaa && (k == 0 || thisCas == prevCas + 1));
                            prevCas = thisCas;
                        }
                        break;
                    case 't':
                        break;
                    default:
                        assert (false);
                }
                operands[id] = x;  
                while (j + 1 < jlimit && format.charAt(j + 1) == fc) {
                    ++j;
                }
            }
            switch (op) {
                case 0xaa: 
                    loc = switchBase(loc);
                    loc += 4 * (3 + ins.size());
                    break;
                case 0xab: 
                    loc = switchBase(loc);
                    loc += 4 * (2 + 2 * ins.size());
                    break;
                default:
                    if (isWide[id]) {
                        loc++;  
                    }
                    loc += format.length();
                    break;
            }
        }
        insLocs[insCount] = loc;
        for (int maxTries = 9, tries = 0;; ++tries) {
            boolean overflowing = false;
            boolean[] branchExpansions = null;
            for (int bn = 0; bn < branches.length - 1; bn++) {
                int id = branches[bn];
                Element ins = insElems[id];
                int insSize = insLocs[id + 1] - insLocs[id];
                int origin = insLocs[id];
                int target = insLocs[(int) ins.getAttrLong("lab")];
                int offset = target - origin;
                operands[id] = offset;
                assert (insSize == GOTO_LEN || insSize == GOTO_W_LEN || ins.getName().indexOf("switch") > 0);
                boolean thisOverflow = (insSize == GOTO_LEN && (offset != (short) offset));
                if (thisOverflow && !overflowing) {
                    overflowing = true;
                    branchExpansions = new boolean[branches.length];
                }
                if (thisOverflow || tries == maxTries - 1) {
                    assert (!(thisOverflow && isWide[id]));
                    isWide[id] = true;
                    branchExpansions[bn] = true;
                }
            }
            if (!overflowing) {
                break;  
            }
            assert (tries <= maxTries);
            int fixup = 0;
            for (int bn = 0, id = 0; id < insCount; id++) {
                insLocs[id] += fixup;
                if (branches[bn] == id) {
                    int op = ops[id] & 0xFF;
                    int wop;
                    boolean invert;
                    if (branchExpansions[bn]) {
                        switch (op) {
                            case GOTO: 
                                wop = GOTO_W; 
                                invert = false;
                                break;
                            case 0xa8: 
                                wop = 0xc9; 
                                invert = false;
                                break;
                            default:
                                wop = invertBranchOp(op);
                                invert = true;
                                break;
                        }
                        assert (op != wop);
                        ops[id] = (byte) wop;
                        isWide[id] = invert;
                        if (invert) {
                            fixup += GOTO_W_LEN;  
                        } else {
                            fixup += (GOTO_W_LEN - GOTO_LEN);
                        }
                    }
                    bn++;
                }
            }
            insLocs[insCount] += fixup;
        }
        if (elemToIndexMap != null) {
            for (int i = 0; i < elemToIndexMap.length; i++) {
                int id = elemToIndexMap[i];
                if (id >= 0) {
                    Element ins = (Element) instructions.get(i);
                    ins.setAttr(pcAttrName, "" + insLocs[id]);
                }
            }
            elemToIndexMap = null;  
        }
        StringBuffer sbuf = new StringBuffer(insLocs[insCount]);
        for (int bn = 0, id = 0; id < insCount; id++) {
            assert (sbuf.length() == insLocs[id]);
            Element ins;
            int pc = insLocs[id];
            int nextpc = insLocs[id + 1];
            int op = ops[id] & 0xFF;
            int opnd = operands[id];
            String format;
            if (branches[bn] == id) {
                bn++;
                sbuf.append((char) op);
                if (isWide[id]) {
                    int target = pc + opnd;
                    putInt(sbuf, nextpc - pc, -2);
                    assert (sbuf.length() == pc + GOTO_LEN);
                    sbuf.append((char) GOTO_W);
                    putInt(sbuf, target - (pc + GOTO_LEN), 4);
                } else if (op == 0xaa || 
                        op == 0xab) {  
                    ins = insElems[id];
                    for (int pad = switchBase(pc) - (pc + 1); pad > 0; pad--) {
                        sbuf.append((char) 0);
                    }
                    assert (pc + opnd == insLocs[(int) ins.getAttrLong("lab")]);
                    putInt(sbuf, opnd, 4); 
                    if (op == 0xaa) {  
                        Element cas0 = (Element) ins.get(0);
                        int lowCase = (int) cas0.getAttrLong("num");
                        Element casN = (Element) ins.get(ins.size() - 1);
                        int highCase = (int) casN.getAttrLong("num");
                        assert (highCase - lowCase + 1 == ins.size());
                        putInt(sbuf, lowCase, 4);
                        putInt(sbuf, highCase, 4);
                        int caseForAssert = lowCase;
                        for (Element cas : ins.elements()) {
                            int target = insLocs[(int) cas.getAttrLong("lab")];
                            assert (cas.getAttrLong("num") == caseForAssert++);
                            putInt(sbuf, target - pc, 4);
                        }
                    } else {  
                        int caseCount = ins.size();
                        putInt(sbuf, caseCount, 4);
                        for (Element cas : ins.elements()) {
                            int target = insLocs[(int) cas.getAttrLong("lab")];
                            putInt(sbuf, (int) cas.getAttrLong("num"), 4);
                            putInt(sbuf, target - pc, 4);
                        }
                    }
                    assert (nextpc == sbuf.length());
                } else {
                    putInt(sbuf, opnd, -(nextpc - (pc + 1)));
                }
            } else if (nextpc == pc + 1) {
                sbuf.append((char) op);
            } else {
                boolean wide = isWide[id];
                if (wide) {
                    sbuf.append((char) WIDE);
                    pc++;
                }
                sbuf.append((char) op);
                int opnd1;
                int opnd2 = opnd;
                switch (op) {
                    case 0x84:  
                        ins = insElems[id];
                        opnd1 = (int) ins.getAttrLong("loc");
                        if (isWide[id]) {
                            putInt(sbuf, opnd1, 2);
                            putInt(sbuf, opnd2, 2);
                        } else {
                            putInt(sbuf, opnd1, 1);
                            putInt(sbuf, opnd2, 1);
                        }
                        break;
                    case 0xc5: 
                        ins = insElems[id];
                        opnd1 = getCPIndex(ins, 'c', getCPI);
                        putInt(sbuf, opnd1, 2);
                        putInt(sbuf, opnd2, 1);
                        break;
                    case 0xb9: 
                        ins = insElems[id];
                        opnd1 = getCPIndex(ins, 'n', getCPI);
                        putInt(sbuf, opnd1, 2);
                        opnd2 = (int) ins.getAttrLong("num");
                        if (opnd2 == 0) {
                            opnd2 = ClassSyntax.computeInterfaceNum(ins.getAttr("val"));
                        }
                        putInt(sbuf, opnd2, 2);
                        break;
                    default:
                        putInt(sbuf, opnd, nextpc - (pc + 1));
                        break;
                }
            }
        }
        assert (sbuf.length() == insLocs[insCount]);
        return sbuf.toString();
    }
    static int getCPIndex(Element ins, char ctype,
            ClassSyntax.GetCPIndex getCPI) {
        int x = (int) ins.getAttrLong("ref");
        if (x == 0 && getCPI != null) {
            String val = ins.getAttr("val");
            if (val == null || val.equals("")) {
                val = ins.getText().toString();
            }
            byte tag;
            switch (ctype) {
                case 'k':
                    tag = (byte) ins.getAttrLong("tag");
                    break;
                case 'c':
                    tag = ClassSyntax.CONSTANT_Class;
                    break;
                case 'f':
                    tag = ClassSyntax.CONSTANT_Fieldref;
                    break;
                case 'm':
                    tag = ClassSyntax.CONSTANT_Methodref;
                    break;
                case 'n':
                    tag = ClassSyntax.CONSTANT_InterfaceMethodref;
                    break;
                default:
                    throw new Error("bad ctype " + ctype + " in " + ins);
            }
            x = getCPI.getCPIndex(tag, val);
        } else {
            assert (x > 0);
        }
        return x;
    }
    static void putInt(StringBuffer sbuf, int x, int len) {
        boolean isSigned = false;
        if (len < 0) {
            len = -len;
            isSigned = true;
        }
        assert (len == 1 || len == 2 || len == 4);
        int insig = ((4 - len) * 8);  
        int sx = x << insig;
        ;
        assert (x == (isSigned ? (sx >> insig) : (sx >>> insig)));
        for (int i = 0; i < len; i++) {
            sbuf.append((char) (sx >>> 24));
            sx <<= 8;
        }
    }
}
