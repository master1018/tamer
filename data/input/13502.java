public class Inject implements RuntimeConstants {
    public static byte[] instrumentation(Options opt,
                                         ClassLoader loader,
                                         String className,
                                         byte[] classfileBuffer) {
        ClassReaderWriter c = new ClassReaderWriter(classfileBuffer);
        (new Inject(className, c, loader == null, opt)).doit();
        return c.result();
    }
    static boolean verbose = false;
    final String className;
    final ClassReaderWriter c;
    final boolean isSystem;
    final Options options;
    int constantPoolCount;
    int methodsCount;
    int methodsCountPos;
    int profiler;
    int wrappedTrackerIndex = 0;
    int thisClassIndex = 0;
    TrackerInjector callInjector;
    TrackerInjector allocInjector;
    TrackerInjector defaultInjector;
    static interface TrackerInjector extends Injector {
        void reinit(int tracker);
        int stackSize(int currentSize);
    }
    static class SimpleInjector implements TrackerInjector {
        byte[] injection;
        public int stackSize(int currentSize) {
            return currentSize;
        }
        public void reinit(int tracker) {
            injection = new byte[3];
            injection[0] = (byte)opc_invokestatic;
            injection[1] = (byte)(tracker >> 8);
            injection[2] = (byte)tracker;
        }
        public byte[] bytecodes(String className, String methodName, int location) {
            return injection;
        }
    }
    static class ObjectInjector implements TrackerInjector {
        byte[] injection;
        public int stackSize(int currentSize) {
            return currentSize + 1;
        }
        public void reinit(int tracker) {
            injection = new byte[4];
            injection[0] = (byte)opc_dup;
            injection[1] = (byte)opc_invokestatic;
            injection[2] = (byte)(tracker >> 8);
            injection[3] = (byte)tracker;
        }
        public byte[] bytecodes(String className, String methodName, int location) {
            return injection;
        }
    }
    class IndexedInjector implements TrackerInjector {
        int counter = 0;
        int tracker;
        List<Info> infoList = new ArrayList<>();
        public int stackSize(int currentSize) {
            return currentSize + 1;
        }
        public void reinit(int tracker) {
            this.tracker = tracker;
        }
        void dump(File outDir, String filename) throws IOException {
            try (FileOutputStream fileOut =
                     new FileOutputStream(new File(outDir, filename));
                 DataOutputStream dataOut = new DataOutputStream(fileOut))
            {
                String currentClassName = null;
                dataOut.writeInt(infoList.size());
                for (Iterator<Info> it = infoList.iterator(); it.hasNext(); ) {
                    Info info = it.next();
                    if (!info.className.equals(currentClassName)) {
                        dataOut.writeInt(123456); 
                        currentClassName = info.className;
                        dataOut.writeUTF(currentClassName);
                    }
                    dataOut.writeInt(info.location);
                    dataOut.writeUTF(info.methodName);
                }
            }
        }
        public byte[] bytecodes(String className, String methodName, int location) {
            byte[] injection = new byte[6];
            int injectedIndex = options.fixedIndex != 0? options.fixedIndex : ++counter;
            infoList.add(new Info(counter, className, methodName, location));
            injection[0] = (byte)opc_sipush;
            injection[1] = (byte)(injectedIndex >> 8);
            injection[2] = (byte)injectedIndex;
            injection[3] = (byte)opc_invokestatic;
            injection[4] = (byte)(tracker >> 8);
            injection[5] = (byte)tracker;
            return injection;
        }
    }
    Inject(String className, ClassReaderWriter c, boolean isSystem, Options options) {
        this.className = className;
        this.c = c;
        this.isSystem = isSystem;
        this.options = options;
    }
    void doit() {
        int i;
        c.copy(4 + 2 + 2); 
        int constantPoolCountPos = c.generatedPosition();
        constantPoolCount = c.copyU2();
        c.copyConstantPool(constantPoolCount);
        if (verbose) {
            System.out.println("ConstantPool expanded from: " +
                               constantPoolCount);
        }
        profiler = addClassToConstantPool(options.trackerClassName);
        if (options.shouldInstrumentNew || options.shouldInstrumentObjectInit) {
            if (options.shouldInstrumentIndexed) {
                if (allocInjector == null) {
                    allocInjector = new IndexedInjector();
                }
                int allocTracker = addMethodToConstantPool(profiler,
                                                           options.allocTrackerMethodName,
                                                           "(I)V");
                allocInjector.reinit(allocTracker);
            } else if (options.shouldInstrumentObject) {
                if (allocInjector == null) {
                    allocInjector = new ObjectInjector();
                }
                int allocTracker = addMethodToConstantPool(profiler,
                                                           options.allocTrackerMethodName,
                                                           "(Ljava/lang/Object;)V");
                allocInjector.reinit(allocTracker);
            } else {
                if (allocInjector == null) {
                    allocInjector = new SimpleInjector();
                }
                int allocTracker = addMethodToConstantPool(profiler,
                                                           options.allocTrackerMethodName,
                                                           "()V");
                allocInjector.reinit(allocTracker);
            }
            defaultInjector = allocInjector;
        }
        if (options.shouldInstrumentCall) {
            if (options.shouldInstrumentIndexed) {
                if (callInjector == null) {
                    callInjector = new IndexedInjector();
                }
                int callTracker = addMethodToConstantPool(profiler,
                                                          options.callTrackerMethodName,
                                                          "(I)V");
                callInjector.reinit(callTracker);
            } else {
                if (callInjector == null) {
                    callInjector = new SimpleInjector();
                }
                int callTracker = addMethodToConstantPool(profiler,
                                                          options.callTrackerMethodName,
                                                          "()V");
                callInjector.reinit(callTracker);
            }
            defaultInjector = callInjector;
        }
        if (verbose) {
            System.out.println("To: " + constantPoolCount);
        }
        c.setSection(1);
        c.copy(2 + 2 + 2);  
        int interfaceCount = c.copyU2();
        if (verbose) {
            System.out.println("interfaceCount: " + interfaceCount);
        }
        c.copy(interfaceCount * 2);
        copyFields(); 
        copyMethods(); 
        int attrCountPos = c.generatedPosition();
        int attrCount = c.copyU2();
        if (verbose) {
            System.out.println("class attrCount: " + attrCount);
        }
        copyAttrs(attrCount);
        c.randomAccessWriteU2(constantPoolCountPos, constantPoolCount);
    }
    void copyFields() {
        int count = c.copyU2();
        if (verbose) {
            System.out.println("fields count: " + count);
        }
        for (int i = 0; i < count; ++i) {
            c.copy(6); 
            int attrCount = c.copyU2();
            if (verbose) {
                System.out.println("field attr count: " + attrCount);
            }
            copyAttrs(attrCount);
        }
    }
    void copyMethods() {
        methodsCountPos = c.generatedPosition();
        methodsCount = c.copyU2();
        int initialMethodsCount = methodsCount;
        if (verbose) {
            System.out.println("methods count: " + methodsCount);
        }
        for (int i = 0; i < initialMethodsCount; ++i) {
            copyMethod();
        }
    }
    void copyMethod() {
        int accessFlags = c.copyU2();
        if (options.shouldInstrumentNativeMethods && (accessFlags & ACC_NATIVE) != 0) {
            wrapNativeMethod(accessFlags);
            return;
        }
        int nameIndex = c.copyU2();  
        String methodName = c.constantPoolString(nameIndex);
        c.copyU2();                  
        int attrCount = c.copyU2();  
        if (verbose) {
            System.out.println("methods attr count: " + attrCount);
        }
        for (int i = 0; i < attrCount; ++i) {
            copyAttrForMethod(methodName, accessFlags);
        }
    }
    void wrapNativeMethod(int accessFlags) {
        int nameIndex = c.readU2();        
        String methodName = c.constantPoolString(nameIndex);
        String wrappedMethodName = options.wrappedPrefix + methodName;
        int wrappedNameIndex = writeCPEntryUtf8(wrappedMethodName);
        c.writeU2(wrappedNameIndex);       
        int descriptorIndex = c.copyU2();  
        int attrCount = c.copyU2();        
        c.markLocalPositionStart();
        for (int i = 0; i < attrCount; ++i) {
            copyAttrForMethod(methodName, accessFlags);
        }
        if (true) {
            System.err.println("   wrapped: " + methodName);
        }
        c.writeU2(accessFlags & ~ACC_NATIVE);
        c.writeU2(nameIndex);           
        c.writeU2(descriptorIndex);     
        c.writeU2(attrCount + 1);       
        c.rewind();
        for (int i = 0; i < attrCount; ++i) {
            copyAttrForMethod(methodName, accessFlags);
        }
        int wrappedIndex = addMethodToConstantPool(getThisClassIndex(),
                                                   wrappedNameIndex,
                                                   descriptorIndex);
        String descriptor = c.constantPoolString(descriptorIndex);
        createWrapperCodeAttr(nameIndex, accessFlags, descriptor, wrappedIndex);
        c.randomAccessWriteU2(methodsCountPos, ++methodsCount);
    }
    void copyAttrs(int attrCount) {
        for (int i = 0; i < attrCount; ++i) {
            copyAttr();
        }
    }
    void copyAttr() {
        c.copy(2);             
        int len = c.copyU4();  
        if (verbose) {
            System.out.println("attr len: " + len);
        }
        c.copy(len);           
    }
    void copyAttrForMethod(String methodName, int accessFlags) {
        int nameIndex = c.copyU2();   
        if (nameIndex == c.codeAttributeIndex) {
            try {
                copyCodeAttr(methodName);
            } catch (IOException exc) {
                System.err.println("Code Exception - " + exc);
                System.exit(1);
            }
        } else {
            int len = c.copyU4();     
            if (verbose) {
                System.out.println("method attr len: " + len);
            }
            c.copy(len);              
        }
    }
    void copyAttrForCode(InjectBytecodes ib) throws IOException {
        int nameIndex = c.copyU2();   
        if (nameIndex == c.lineNumberAttributeIndex) {
            ib.copyLineNumberAttr();
        } else if (nameIndex == c.localVarAttributeIndex) {
            ib.copyLocalVarAttr();
        } else {
            int len = c.copyU4();     
            if (verbose) {
                System.out.println("code attr len: " + len);
            }
            c.copy(len);              
        }
    }
    void copyCodeAttr(String methodName) throws IOException {
        if (verbose) {
            System.out.println("Code attr found");
        }
        int attrLengthPos = c.generatedPosition();
        int attrLength = c.copyU4();        
        int maxStack = c.readU2();          
        c.writeU2(defaultInjector == null? maxStack :
                  defaultInjector.stackSize(maxStack));  
        c.copyU2();                         
        int codeLengthPos = c.generatedPosition();
        int codeLength = c.copyU4();        
        if (options.targetMethod != null && !options.targetMethod.equals(methodName)) {
            c.copy(attrLength - 8); 
            return;
        }
        if (isSystem) {
            if (codeLength == 1 && methodName.equals("finalize")) {
                if (verbose) {
                    System.out.println("empty system finalizer not instrumented");
                }
                c.copy(attrLength - 8); 
                return;
            }
            if (codeLength == 1 && methodName.equals("<init>")) {
                if (verbose) {
                    System.out.println("empty system constructor not instrumented");
                }
                if (!options.shouldInstrumentObjectInit) {
                    c.copy(attrLength - 8); 
                    return;
                }
            }
            if (methodName.equals("<clinit>")) {
                if (verbose) {
                    System.out.println("system class initializer not instrumented");
                }
                c.copy(attrLength - 8); 
                return;
            }
        }
        if (options.shouldInstrumentObjectInit
            && (!className.equals("java/lang/Object")
                || !methodName.equals("<init>"))) {
            c.copy(attrLength - 8); 
            return;
        }
        InjectBytecodes ib = new InjectBytecodes(c, codeLength, className, methodName);
        if (options.shouldInstrumentNew) {
            ib.injectAfter(opc_new, allocInjector);
            ib.injectAfter(opc_newarray, allocInjector);
            ib.injectAfter(opc_anewarray, allocInjector);
            ib.injectAfter(opc_multianewarray, allocInjector);
        }
        if (options.shouldInstrumentCall) {
            ib.inject(0, callInjector.bytecodes(className, methodName, 0));
        }
        if (options.shouldInstrumentObjectInit) {
            ib.inject(0, allocInjector.bytecodes(className, methodName, 0));
        }
        ib.adjustOffsets();
        int newCodeLength = c.generatedPosition() - (codeLengthPos + 4);
        c.randomAccessWriteU4(codeLengthPos, newCodeLength);
        if (verbose) {
            System.out.println("code length old: " + codeLength +
                               ", new: " + newCodeLength);
        }
        ib.copyExceptionTable();
        int attrCount = c.copyU2();
        for (int i = 0; i < attrCount; ++i) {
            copyAttrForCode(ib);
        }
        int newAttrLength = c.generatedPosition() - (attrLengthPos + 4);
        c.randomAccessWriteU4(attrLengthPos, newAttrLength);
        if (verbose) {
            System.out.println("attr length old: " + attrLength +
                               ", new: " + newAttrLength);
        }
    }
    int nextDescriptorIndex(String descriptor, int index) {
        switch (descriptor.charAt(index)) {
        case 'B': 
        case 'C': 
        case 'I': 
        case 'S': 
        case 'Z': 
        case 'F': 
        case 'D': 
        case 'J': 
            return index + 1;
        case 'L': 
            int i = index + 1;
            while (descriptor.charAt(i) != ';') {
                ++i;
            }
            return i + 1;
        case '[': 
            return nextDescriptorIndex(descriptor, index + 1);
        }
        throw new InternalError("should not reach here");
    }
    int getWrappedTrackerIndex() {
        if (wrappedTrackerIndex == 0) {
            wrappedTrackerIndex = addMethodToConstantPool(profiler,
                                                          options.wrappedTrackerMethodName,
                                                          "(Ljava/lang/String;I)V");
        }
        return wrappedTrackerIndex;
    }
    int getThisClassIndex() {
        if (thisClassIndex == 0) {
            thisClassIndex = addClassToConstantPool(className);
        }
        return thisClassIndex;
    }
    int computeMaxLocals(String descriptor, int accessFlags) {
        int index = 1;
        int slot = 0;
        if ((accessFlags & ACC_STATIC) == 0) {
            ++slot;
        }
        char type;
        while ((type = descriptor.charAt(index)) != ')') {
            switch (type) {
            case 'B': 
            case 'C': 
            case 'I': 
            case 'S': 
            case 'Z': 
            case 'F': 
            case 'L': 
            case '[': 
                ++slot;
                break;
            case 'D': 
            case 'J': 
                slot += 2;
                break;
            }
            index = nextDescriptorIndex(descriptor, index);
        }
        return slot;
    }
    void createWrapperCodeAttr(int methodNameIndex, int accessFlags,
                               String descriptor, int wrappedIndex) {
        int maxLocals = computeMaxLocals(descriptor, accessFlags);
        c.writeU2(c.codeAttributeIndex);        
        int attrLengthPos = c.generatedPosition();
        c.writeU4(0);                
        c.writeU2(maxLocals + 4);    
        c.writeU2(maxLocals);        
        int codeLengthPos = c.generatedPosition();
        c.writeU4(0);                
        int methodStringIndex = writeCPEntryString(methodNameIndex);
        c.writeU1(opc_ldc_w);
        c.writeU2(methodStringIndex);  
        c.writeU1(opc_sipush);
        c.writeU2(options.fixedIndex);
        c.writeU1(opc_invokestatic);
        c.writeU2(getWrappedTrackerIndex());
        int index = 1;
        int slot = 0;
        if ((accessFlags & ACC_STATIC) == 0) {
            c.writeU1(opc_aload_0);  
            ++slot;
        }
        char type;
        while ((type = descriptor.charAt(index)) != ')') {
            switch (type) {
            case 'B': 
            case 'C': 
            case 'I': 
            case 'S': 
            case 'Z': 
                c.writeU1(opc_iload);
                c.writeU1(slot);
                ++slot;
                break;
            case 'F': 
                c.writeU1(opc_fload);
                c.writeU1(slot);
                ++slot;
                break;
            case 'D': 
                c.writeU1(opc_dload);
                c.writeU1(slot);
                slot += 2;
                break;
            case 'J': 
                c.writeU1(opc_lload);
                c.writeU1(slot);
                slot += 2;
                break;
            case 'L': 
            case '[': 
                c.writeU1(opc_aload);
                c.writeU1(slot);
                ++slot;
                break;
            }
            index = nextDescriptorIndex(descriptor, index);
        }
        if ((accessFlags & ACC_STATIC) == 0) {
            c.writeU1(opc_invokevirtual);
        } else {
            c.writeU1(opc_invokestatic);
        }
        c.writeU2(wrappedIndex);
        switch (descriptor.charAt(index+1)) {
        case 'B': 
        case 'C': 
        case 'I': 
        case 'S': 
        case 'Z': 
            c.writeU1(opc_ireturn);
            break;
        case 'F': 
            c.writeU1(opc_freturn);
            break;
        case 'D': 
            c.writeU1(opc_dreturn);
            break;
        case 'J': 
            c.writeU1(opc_lreturn);
            break;
        case 'L': 
        case '[': 
            c.writeU1(opc_areturn);
            break;
        case 'V': 
            c.writeU1(opc_return);
            break;
        }
        int newCodeLength = c.generatedPosition() - (codeLengthPos + 4);
        c.randomAccessWriteU4(codeLengthPos, newCodeLength);
        c.writeU2(0);                
        c.writeU2(0);                
        int newAttrLength = c.generatedPosition() - (attrLengthPos + 4);
        c.randomAccessWriteU4(attrLengthPos, newAttrLength);
    }
    int addClassToConstantPool(String className) {
        int prevSection = c.setSection(0);
        int classNameIndex = writeCPEntryUtf8(className);
        int classIndex = writeCPEntryClass(classNameIndex);
        c.setSection(prevSection);
        return classIndex;
    }
    int addMethodToConstantPool(int classIndex,
                                String methodName,
                                String descr) {
        int prevSection = c.setSection(0);
        int methodNameIndex = writeCPEntryUtf8(methodName);
        int descrIndex = writeCPEntryUtf8(descr);
        c.setSection(prevSection);
        return addMethodToConstantPool(classIndex, methodNameIndex, descrIndex);
    }
    int addMethodToConstantPool(int classIndex,
                                int methodNameIndex,
                                int descrIndex) {
        int prevSection = c.setSection(0);
        int nameAndTypeIndex = writeCPEntryNameAndType(methodNameIndex,
                                                       descrIndex);
        int methodIndex = writeCPEntryMethodRef(classIndex, nameAndTypeIndex);
        c.setSection(prevSection);
        return methodIndex;
    }
    int writeCPEntryUtf8(String str) {
        int prevSection = c.setSection(0);
        int len = str.length();
        c.writeU1(CONSTANT_UTF8); 
        c.writeU2(len);
        for (int i = 0; i < len; ++i) {
            c.writeU1(str.charAt(i));
        }
        c.setSection(prevSection);
        return constantPoolCount++;
    }
    int writeCPEntryString(int utf8Index) {
        int prevSection = c.setSection(0);
        c.writeU1(CONSTANT_STRING);
        c.writeU2(utf8Index);
        c.setSection(prevSection);
        return constantPoolCount++;
    }
    int writeCPEntryClass(int classNameIndex) {
        int prevSection = c.setSection(0);
        c.writeU1(CONSTANT_CLASS);
        c.writeU2(classNameIndex);
        c.setSection(prevSection);
        return constantPoolCount++;
    }
    int writeCPEntryNameAndType(int nameIndex, int descrIndex) {
        int prevSection = c.setSection(0);
        c.writeU1(CONSTANT_NAMEANDTYPE);
        c.writeU2(nameIndex);
        c.writeU2(descrIndex);
        c.setSection(prevSection);
        return constantPoolCount++;
    }
    int writeCPEntryMethodRef(int classIndex, int nameAndTypeIndex) {
        int prevSection = c.setSection(0);
        c.writeU1(CONSTANT_METHOD);
        c.writeU2(classIndex);
        c.writeU2(nameAndTypeIndex);
        c.setSection(prevSection);
        return constantPoolCount++;
    }
}
