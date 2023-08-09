public class StackFrameImpl extends MirrorImpl
                            implements StackFrame
{
    private boolean isValid = true;
    private final ThreadReferenceImpl thread;
    private final JavaVFrame saFrame;
    private final Location location;
    private Map visibleVariables =  null;
    private ObjectReference thisObject = null;
    StackFrameImpl(VirtualMachine vm, ThreadReferenceImpl thread,
                   JavaVFrame jvf) {
        super(vm);
        this.thread = thread;
        this.saFrame = jvf;
        sun.jvm.hotspot.oops.Method SAMethod = jvf.getMethod();
        ReferenceType rt = ((VirtualMachineImpl)vm).referenceType(SAMethod.getMethodHolder());
        this.location = new LocationImpl(vm, rt, SAMethod, (long)jvf.getBCI());
    }
    private void validateStackFrame() {
        if (!isValid) {
            throw new InvalidStackFrameException("Thread has been resumed");
        }
    }
    JavaVFrame getJavaVFrame() {
        return saFrame;
    }
    public Location location() {
        validateStackFrame();
        return location;
    }
    public ThreadReference thread() {
        validateStackFrame();
        return thread;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof StackFrameImpl)) {
            StackFrameImpl other = (StackFrameImpl)obj;
            return (saFrame.equals(other.saFrame));
        } else {
            return false;
        }
    }
    public int hashCode() {
        return saFrame.hashCode();
    }
    public ObjectReference thisObject() {
        validateStackFrame();
        MethodImpl currentMethod = (MethodImpl)location.method();
        if (currentMethod.isStatic() || currentMethod.isNative()) {
            return null;
        }
        if (thisObject == null) {
            StackValueCollection values = saFrame.getLocals();
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(values.size() > 0, "this is missing");
            }
            OopHandle handle = values.oopHandleAt(0);
            ObjectHeap heap = vm.saObjectHeap();
            thisObject = vm.objectMirror(heap.newOop(handle));
        }
        return thisObject;
    }
    private void createVisibleVariables() throws AbsentInformationException {
        if (visibleVariables == null) {
            List allVariables = location.method().variables();
            Map map = new HashMap(allVariables.size());
            Iterator iter = allVariables.iterator();
            while (iter.hasNext()) {
                LocalVariableImpl variable = (LocalVariableImpl)iter.next();
                String name = variable.name();
                if (variable.isVisible(this)) {
                    LocalVariable existing = (LocalVariable)map.get(name);
                    if ((existing == null) ||
                        variable.hides(existing)) {
                        map.put(name, variable);
                    }
                }
            }
            visibleVariables = map;
        }
    }
    public List visibleVariables() throws AbsentInformationException {
        validateStackFrame();
        createVisibleVariables();
        List mapAsList = new ArrayList(visibleVariables.values());
        Collections.sort(mapAsList);
        return mapAsList;
    }
    public LocalVariable visibleVariableByName(String name) throws AbsentInformationException  {
        validateStackFrame();
        createVisibleVariables();
        return (LocalVariable)visibleVariables.get(name);
    }
    public Value getValue(LocalVariable variable) {
        List list = new ArrayList(1);
        list.add(variable);
        Map map = getValues(list);
        return (Value)map.get(variable);
    }
    public Map getValues(List variables) {
        validateStackFrame();
        StackValueCollection values = saFrame.getLocals();
        int count = variables.size();
        Map map = new HashMap(count);
        for (int ii=0; ii<count; ++ii) {
            LocalVariableImpl variable = (LocalVariableImpl)variables.get(ii);
            if (!variable.isVisible(this)) {
                throw new IllegalArgumentException(variable.name() +
                                 " is not valid at this frame location");
            }
            ValueImpl valueImpl;
            int ss = variable.slot();
            char c = variable.signature().charAt(0);
            BasicType variableType = BasicType.charToBasicType(c);
            valueImpl = getSlotValue(values, variableType, ss);
            map.put(variable, valueImpl);
        }
        return map;
    }
    public List getArgumentValues() {
        validateStackFrame();
        StackValueCollection values = saFrame.getLocals();
        MethodImpl mmm = (MethodImpl)location.method();
        List argSigs = mmm.argumentSignatures();
        int count = argSigs.size();
        List res = new ArrayList(0);
        int slot = mmm.isStatic()? 0 : 1;
        for (int ii = 0; ii < count; ++slot, ++ii) {
            char sigChar = ((String)argSigs.get(ii)).charAt(0);
            BasicType variableType = BasicType.charToBasicType(sigChar);
            res.add(getSlotValue(values, variableType, slot));
            if (sigChar == 'J' || sigChar == 'D') {
                slot++;
            }
        }
        return res;
    }
    private ValueImpl getSlotValue(StackValueCollection values,
                       BasicType variableType, int ss) {
        ValueImpl valueImpl = null;
        OopHandle handle = null;
        ObjectHeap heap = vm.saObjectHeap();
        if (variableType == BasicType.T_BOOLEAN) {
            valueImpl = (BooleanValueImpl) vm.mirrorOf(values.booleanAt(ss));
        } else if (variableType == BasicType.T_CHAR) {
            valueImpl = (CharValueImpl) vm.mirrorOf(values.charAt(ss));
        } else if (variableType == BasicType.T_FLOAT) {
            valueImpl = (FloatValueImpl) vm.mirrorOf(values.floatAt(ss));
        } else if (variableType == BasicType.T_DOUBLE) {
            valueImpl = (DoubleValueImpl) vm.mirrorOf(values.doubleAt(ss));
        } else if (variableType == BasicType.T_BYTE) {
            valueImpl = (ByteValueImpl) vm.mirrorOf(values.byteAt(ss));
        } else if (variableType == BasicType.T_SHORT) {
            valueImpl = (ShortValueImpl) vm.mirrorOf(values.shortAt(ss));
        } else if (variableType == BasicType.T_INT) {
            valueImpl = (IntegerValueImpl) vm.mirrorOf(values.intAt(ss));
        } else if (variableType == BasicType.T_LONG) {
            valueImpl = (LongValueImpl) vm.mirrorOf(values.longAt(ss));
        } else if (variableType == BasicType.T_OBJECT) {
            handle = values.oopHandleAt(ss);
            valueImpl = (ObjectReferenceImpl) vm.objectMirror(heap.newOop(handle));
        } else if (variableType == BasicType.T_ARRAY) {
            handle = values.oopHandleAt(ss);
            valueImpl = vm.arrayMirror((Array)heap.newOop(handle));
        } else if (variableType == BasicType.T_VOID) {
            valueImpl = new VoidValueImpl(vm);
        } else {
            throw new RuntimeException("Should not read here");
        }
        return valueImpl;
    }
    public void setValue(LocalVariable variableIntf, Value valueIntf)
        throws InvalidTypeException, ClassNotLoadedException {
        vm.throwNotReadOnlyException("StackFrame.setValue()");
    }
    public String toString() {
        return location.toString() + " in thread " + thread.toString();
    }
}
