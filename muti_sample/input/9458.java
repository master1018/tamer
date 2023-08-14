class comparator implements Comparator {
    public int compare(Object o1, Object o2) {
        ReferenceType rt1 = (ReferenceType)o1;
        ReferenceType rt2 = (ReferenceType)o2;
        return rt1.signature().compareTo(rt2.signature());
    }
    public boolean equals(Object oo) {
        return false;
    }
}
public class sagdoit {
    VirtualMachine myVm;
    int margin = 0;
    static String blanks = "                                                        ";
    static int nblanks = blanks.length();
    sagdoit(VirtualMachine vm) {
        super();
        myVm = vm;
    }
    void indent(int count) {
        margin += count;
    }
    void pp(String msg) {
        System.out.println(blanks.substring(nblanks - margin) + msg);
    }
    public void doAll() {
        doThreadGroups();
        doClasses();  
    }
    public void doThreadGroups() {
        doThreadGroupList(myVm.topLevelThreadGroups());
    }
    private void doThreadGroupList(List groups) {
        if (groups == null) {
            return;
        }
        Iterator myIter = groups.iterator();
        while(myIter.hasNext()) {
            ThreadGroupReference aGroup = (ThreadGroupReference)myIter.next();
            doOneThreadGroup(aGroup);
        }
    }
    public void doOneThreadGroup(ThreadGroupReference xx) {
        pp("threadGroup:" + xx.name());
        indent(4);
        pp("parent()       = " + xx.parent());
        pp("threads:");
        indent(4);
        doThreadList(xx.threads());
        indent(-4);
        pp("threadGroups:");
        indent(4);
        doThreadGroupList(xx.threadGroups());
        indent(-4);
        indent(-4);
    }
    public void doThreads() {
        doThreadList(myVm.allThreads());
    }
    public void doThreadList(List threads) {
        if (threads == null) {
            return;
        }
        Iterator myIter = threads.iterator();
        while(myIter.hasNext()) {
            ThreadReference aThread = (ThreadReference)myIter.next();
            doOneThread(aThread);
        }
    }
    public void doOneThread(ThreadReference xx) {
        pp("Thread: " + xx.name());
        indent(4);
        pp("suspendCount()      = " + xx.suspendCount());
        pp("status()            = " + xx.status());
        pp("isSuspended()       = " + xx.isSuspended());
        pp("isAtBreakpoint()    = " + xx.isAtBreakpoint());
        pp("threadGroup()       = " + xx.threadGroup());
        indent(-4);
        indent(4);
        try {
            List allFrames = xx.frames();
            for (int ii = 0; ii < xx.frameCount(); ii++) {
                StackFrame oneFrame = xx.frame(ii);
                pp("frame(" + ii + ") = " + oneFrame);
                doOneFrame(oneFrame);
            }
        } catch (IncompatibleThreadStateException ee) {
            pp("GOT IncompatibleThreadStateException: " + ee);
        }
        indent(-4);
    }
    public void doOneFrame(StackFrame frame) {
        List localVars = null;
        try {
            localVars = frame.visibleVariables();
        } catch (AbsentInformationException ee) {
            return;
        }
        indent(4);
        for (Iterator it = localVars.iterator(); it.hasNext();) {
            LocalVariable lv = (LocalVariable) it.next();
            pp("lv name = " + lv.name() +
               ", type =  " + lv.typeName() +
               ", sig =   " + lv.signature() +
               ", gsig =  " + lv.genericSignature() +
               ", isVis = " + lv.isVisible(frame) +
               ", isArg = " + lv.isArgument());
        }
        indent(-4);
    }
    public void doClasses() {
        List myClasses = myVm.allClasses();
        myClasses = new ArrayList(myClasses);
        Collections.sort(myClasses, new comparator());
        for (int ii = 0; ii < myClasses.size(); ii++) {
            ReferenceType aClass = (ReferenceType)myClasses.get(ii);
            System.out.println("class " + (ii + 1) + " is " + aClass.signature());
            doOneClass(aClass);
        }
    }
    public void doOneClass(ReferenceType xx) {
        indent(5);
        pp("toString()       = " + xx.toString());
        pp("virtualMachine() = " + xx.virtualMachine());
        pp("name()           = " + xx.name());
        pp("signature()      = " + xx.signature());
        doReferenceTypeFields(xx);
        String className = xx.getClass().getName();
        pp("subclass           = " + className);
         Class referenceType = null;
         Class arrayType = null;
         Class classType = null;
         Class interfaceType = null;
         try {
             referenceType = Class.forName("com.sun.jdi.ReferenceType");
             arrayType = Class.forName("com.sun.jdi.ArrayType");
             interfaceType = Class.forName("com.sun.jdi.InterfaceType");
             classType = Class.forName("com.sun.jdi.ClassType");
         } catch (ClassNotFoundException ee) {
         }
         if (referenceType.isInstance(xx)) {
             pp("ReferenceType fields");
             ReferenceType rr = (ReferenceType)xx;
             if (arrayType.isInstance(xx)) {
                 pp("ArrayType fields");
             }
             if (classType.isInstance(xx)) {
                 pp("ClassType fields");
             }
             if (interfaceType.isInstance(xx)) {
                 pp("InterfaceType fields");
             }
         }
        indent(-5);
    }
  public void doReferenceTypeFields(ReferenceType xx) {
    Object zz;
      pp("classLoader() = " + xx.classLoader());
      try {zz =xx.sourceName();} catch(AbsentInformationException ee) { zz = ee;} pp("sourceName() = " + zz);
      try {zz =xx.sourceNames("stratum");} catch(AbsentInformationException ee) { zz = ee;} pp("sourceNames() = " + zz);
      try {zz =xx.sourcePaths("stratum");} catch(AbsentInformationException ee) { zz = ee;} pp("sourcePaths() = " + zz);
      try {zz =xx.sourceDebugExtension();} catch(Exception ee) { zz = ee;} pp("sourceDebugExtension() = " + zz);
      pp("isStatic() = " + xx.isStatic());
      pp("isAbstract() = " + xx.isAbstract());
      pp("isFinal() = " + xx.isFinal());
      pp("isPrepared() = " + xx.isPrepared());
      pp("isVerified() = " + xx.isVerified());
      pp("isInitialized() = " + xx.isInitialized());
      pp("failedToInitialize() = " + xx.failedToInitialize());
      pp("fields() = " + xx.fields());
      pp("visibleFields() = " + xx.visibleFields());
      pp("allFields() = " + xx.allFields());
      pp("fieldByName(String fieldName) = " + xx.fieldByName("fieldName"));
      pp("methods() = " + xx.methods());
       List meths = xx.methods();
       Iterator iter = meths.iterator();
       while (iter.hasNext()) {
           Method mm = (Method)iter.next();
           pp("  name/sig:" + mm.name() + "/" + mm.signature());
       }
      pp(" visibleMethods() = " + xx.visibleMethods());
      pp("allMethods() = " + xx.allMethods());
      pp("methodsByName(String name) = " + xx.methodsByName("name"));
      pp("methodsByName(String name, String signature) = " + xx.methodsByName("name", "signature"));
      pp("nestedTypes() = " + xx.nestedTypes());
      pp("getValue(Field field) = " + "fixme: jjh");
      pp("getValues(List fields) = " + "fixme: jjh");
      pp("classObject() = " + xx.classObject());
      pp("availableStrata() = " + xx.availableStrata());
      pp("defaultStratum() = " + xx.defaultStratum());
      pp("equals(Object obj) = " + xx.equals(xx));
      pp("hashCode() = " + xx.hashCode());
  }
}
