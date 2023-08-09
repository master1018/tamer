public final class Frame {
    private final LocalsArray locals;
    private final ExecutionStack stack;
    private final IntList subroutines;
    private Frame(LocalsArray locals, ExecutionStack stack) {
        this(locals, stack, IntList.EMPTY);
    }
    private Frame(LocalsArray locals,
            ExecutionStack stack, IntList subroutines) {
        if (locals == null) {
            throw new NullPointerException("locals == null");
        }
        if (stack == null) {
            throw new NullPointerException("stack == null");
        }
        subroutines.throwIfMutable();
        this.locals = locals;
        this.stack = stack;
        this.subroutines = subroutines;
    }
    public Frame(int maxLocals, int maxStack) {
        this(new OneLocalsArray(maxLocals), new ExecutionStack(maxStack));
    }
    public Frame copy() {
        return new Frame(locals.copy(), stack.copy(), subroutines);
    }
    public void setImmutable() {
        locals.setImmutable();
        stack.setImmutable();
    }
    public void makeInitialized(Type type) {
        locals.makeInitialized(type);
        stack.makeInitialized(type);
    }
    public LocalsArray getLocals() {
        return locals;
    }
    public ExecutionStack getStack() {
        return stack;
    }
    public IntList getSubroutines() {
        return subroutines;
    }
    public void initializeWithParameters(StdTypeList params) {
        int at = 0;
        int sz = params.size();
        for (int i = 0; i < sz; i++) {
             Type one = params.get(i);
             locals.set(at, one);
             at += one.getCategory();
        }
    }
    public Frame subFrameForLabel(int startLabel, int subLabel) {
        LocalsArray subLocals = null;
        if (locals instanceof LocalsArraySet) {
            subLocals = ((LocalsArraySet)locals).subArrayForLabel(subLabel);
        }
        IntList newSubroutines;
        try {
            newSubroutines = subroutines.mutableCopy();
            if (newSubroutines.pop() != startLabel) {
                throw new RuntimeException("returning from invalid subroutine");
            }
            newSubroutines.setImmutable();
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("returning from invalid subroutine");
        } catch (NullPointerException ex) {
            throw new NullPointerException("can't return from non-subroutine");
        }
        return (subLocals == null) ? null
                : new Frame(subLocals, stack, newSubroutines);
    }
    public Frame mergeWith(Frame other) {
        LocalsArray resultLocals;
        ExecutionStack resultStack;
        IntList resultSubroutines;
        resultLocals = getLocals().merge(other.getLocals());
        resultStack = getStack().merge(other.getStack());
        resultSubroutines = mergeSubroutineLists(other.subroutines);
        resultLocals = adjustLocalsForSubroutines(
                resultLocals, resultSubroutines);
        if ((resultLocals == getLocals())
                && (resultStack == getStack())
                && subroutines == resultSubroutines) {
            return this;
        }
        return new Frame(resultLocals, resultStack, resultSubroutines);
    }
    private IntList mergeSubroutineLists(IntList otherSubroutines) {
        if (subroutines.equals(otherSubroutines)) {
            return subroutines;
        }
        IntList resultSubroutines = new IntList();
        int szSubroutines = subroutines.size();
        int szOthers = otherSubroutines.size();
        for (int i = 0; i < szSubroutines && i < szOthers
                && (subroutines.get(i) == otherSubroutines.get(i)); i++) {
            resultSubroutines.add(i);
        }
        resultSubroutines.setImmutable();
        return resultSubroutines;
    }
    private static LocalsArray adjustLocalsForSubroutines(
            LocalsArray locals, IntList subroutines) {
        if (! (locals instanceof LocalsArraySet)) {
            return locals;
        }
        LocalsArraySet laSet = (LocalsArraySet)locals;
        if (subroutines.size() == 0) {
            return laSet.getPrimary();
        }
        return laSet;
    }
    public Frame mergeWithSubroutineCaller(Frame other, int subLabel,
            int predLabel) {
        LocalsArray resultLocals;
        ExecutionStack resultStack;
        resultLocals = getLocals().mergeWithSubroutineCaller(
                other.getLocals(), predLabel);
        resultStack = getStack().merge(other.getStack());
        IntList newOtherSubroutines = other.subroutines.mutableCopy();
        newOtherSubroutines.add(subLabel);
        newOtherSubroutines.setImmutable();
        if ((resultLocals == getLocals())
                && (resultStack == getStack())
                && subroutines.equals(newOtherSubroutines)) {
            return this;
        }
        IntList resultSubroutines;
        if (subroutines.equals(newOtherSubroutines)) {
            resultSubroutines = subroutines;
        } else {
            IntList nonResultSubroutines;
            if (subroutines.size() > newOtherSubroutines.size()) {
                resultSubroutines = subroutines;
                nonResultSubroutines = newOtherSubroutines;
            } else {
                resultSubroutines = newOtherSubroutines;
                nonResultSubroutines = subroutines;
            }
            int szResult = resultSubroutines.size();
            int szNonResult = nonResultSubroutines.size();
            for (int i = szNonResult - 1; i >=0; i-- ) {
                if (nonResultSubroutines.get(i)
                        != resultSubroutines.get(
                        i + (szResult - szNonResult))) {
                    throw new
                            RuntimeException("Incompatible merged subroutines");
                }
            }
        }
        return new Frame(resultLocals, resultStack, resultSubroutines);
    }
    public Frame makeNewSubroutineStartFrame(int subLabel, int callerLabel) {
        IntList newSubroutines = subroutines.mutableCopy();
        newSubroutines.add(subLabel);
        Frame newFrame = new Frame(locals.getPrimary(), stack,
                IntList.makeImmutable(subLabel));
        return newFrame.mergeWithSubroutineCaller(this, subLabel, callerLabel);
    }
    public Frame makeExceptionHandlerStartFrame(CstType exceptionClass) {
        ExecutionStack newStack = getStack().copy();
        newStack.clear();
        newStack.push(exceptionClass);
        return new Frame(getLocals(), newStack, subroutines);
    }
    public void annotate(ExceptionWithContext ex) {
        locals.annotate(ex);
        stack.annotate(ex);
    }
}
