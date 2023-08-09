public class ObjectHeap {
  private static final boolean DEBUG;
  static {
    DEBUG = System.getProperty("sun.jvm.hotspot.oops.ObjectHeap.DEBUG") != null;
  }
  private OopHandle              methodKlassHandle;
  private OopHandle              constMethodKlassHandle;
  private OopHandle              methodDataKlassHandle;
  private OopHandle              constantPoolKlassHandle;
  private OopHandle              constantPoolCacheKlassHandle;
  private OopHandle              klassKlassHandle;
  private OopHandle              instanceKlassKlassHandle;
  private OopHandle              typeArrayKlassKlassHandle;
  private OopHandle              objArrayKlassKlassHandle;
  private OopHandle              boolArrayKlassHandle;
  private OopHandle              byteArrayKlassHandle;
  private OopHandle              charArrayKlassHandle;
  private OopHandle              intArrayKlassHandle;
  private OopHandle              shortArrayKlassHandle;
  private OopHandle              longArrayKlassHandle;
  private OopHandle              singleArrayKlassHandle;
  private OopHandle              doubleArrayKlassHandle;
  private OopHandle              arrayKlassKlassHandle;
  private OopHandle              compiledICHolderKlassHandle;
  private MethodKlass            methodKlassObj;
  private ConstMethodKlass       constMethodKlassObj;
  private MethodDataKlass        methodDataKlassObj;
  private ConstantPoolKlass      constantPoolKlassObj;
  private ConstantPoolCacheKlass constantPoolCacheKlassObj;
  private KlassKlass             klassKlassObj;
  private InstanceKlassKlass     instanceKlassKlassObj;
  private TypeArrayKlassKlass    typeArrayKlassKlassObj;
  private ObjArrayKlassKlass     objArrayKlassKlassObj;
  private TypeArrayKlass         boolArrayKlassObj;
  private TypeArrayKlass         byteArrayKlassObj;
  private TypeArrayKlass         charArrayKlassObj;
  private TypeArrayKlass         intArrayKlassObj;
  private TypeArrayKlass         shortArrayKlassObj;
  private TypeArrayKlass         longArrayKlassObj;
  private TypeArrayKlass         singleArrayKlassObj;
  private TypeArrayKlass         doubleArrayKlassObj;
  private ArrayKlassKlass        arrayKlassKlassObj;
  private CompiledICHolderKlass  compiledICHolderKlassObj;
  public void initialize(TypeDataBase db) throws WrongTypeException {
    Type universeType = db.lookupType("Universe");
    methodKlassHandle         = universeType.getOopField("_methodKlassObj").getValue();
    methodKlassObj            = new MethodKlass(methodKlassHandle, this);
    constMethodKlassHandle    = universeType.getOopField("_constMethodKlassObj").getValue();
    constMethodKlassObj       = new ConstMethodKlass(constMethodKlassHandle, this);
    constantPoolKlassHandle   = universeType.getOopField("_constantPoolKlassObj").getValue();
    constantPoolKlassObj      = new ConstantPoolKlass(constantPoolKlassHandle, this);
    constantPoolCacheKlassHandle = universeType.getOopField("_constantPoolCacheKlassObj").getValue();
    constantPoolCacheKlassObj = new ConstantPoolCacheKlass(constantPoolCacheKlassHandle, this);
    klassKlassHandle          = universeType.getOopField("_klassKlassObj").getValue();
    klassKlassObj             = new KlassKlass(klassKlassHandle, this);
    arrayKlassKlassHandle     = universeType.getOopField("_arrayKlassKlassObj").getValue();
    arrayKlassKlassObj        = new ArrayKlassKlass(arrayKlassKlassHandle, this);
    instanceKlassKlassHandle  = universeType.getOopField("_instanceKlassKlassObj").getValue();
    instanceKlassKlassObj     = new InstanceKlassKlass(instanceKlassKlassHandle, this);
    typeArrayKlassKlassHandle = universeType.getOopField("_typeArrayKlassKlassObj").getValue();
    typeArrayKlassKlassObj    = new TypeArrayKlassKlass(typeArrayKlassKlassHandle, this);
    objArrayKlassKlassHandle  = universeType.getOopField("_objArrayKlassKlassObj").getValue();
    objArrayKlassKlassObj     = new ObjArrayKlassKlass(objArrayKlassKlassHandle, this);
    boolArrayKlassHandle      = universeType.getOopField("_boolArrayKlassObj").getValue();
    boolArrayKlassObj         = new TypeArrayKlass(boolArrayKlassHandle, this);
    byteArrayKlassHandle      = universeType.getOopField("_byteArrayKlassObj").getValue();
    byteArrayKlassObj         = new TypeArrayKlass(byteArrayKlassHandle, this);
    charArrayKlassHandle      = universeType.getOopField("_charArrayKlassObj").getValue();
    charArrayKlassObj         = new TypeArrayKlass(charArrayKlassHandle, this);
    intArrayKlassHandle       = universeType.getOopField("_intArrayKlassObj").getValue();
    intArrayKlassObj          = new TypeArrayKlass(intArrayKlassHandle, this);
    shortArrayKlassHandle     = universeType.getOopField("_shortArrayKlassObj").getValue();
    shortArrayKlassObj        = new TypeArrayKlass(shortArrayKlassHandle, this);
    longArrayKlassHandle      = universeType.getOopField("_longArrayKlassObj").getValue();
    longArrayKlassObj         = new TypeArrayKlass(longArrayKlassHandle, this);
    singleArrayKlassHandle    = universeType.getOopField("_singleArrayKlassObj").getValue();
    singleArrayKlassObj       = new TypeArrayKlass(singleArrayKlassHandle, this);
    doubleArrayKlassHandle    = universeType.getOopField("_doubleArrayKlassObj").getValue();
    doubleArrayKlassObj       = new TypeArrayKlass(doubleArrayKlassHandle, this);
    if (!VM.getVM().isCore()) {
      methodDataKlassHandle   = universeType.getOopField("_methodDataKlassObj").getValue();
      methodDataKlassObj      = new MethodDataKlass(methodDataKlassHandle, this);
      compiledICHolderKlassHandle = universeType.getOopField("_compiledICHolderKlassObj").getValue();
      compiledICHolderKlassObj= new CompiledICHolderKlass(compiledICHolderKlassHandle ,this);
    }
  }
  public ObjectHeap(TypeDataBase db) throws WrongTypeException {
    oopSize     = VM.getVM().getOopSize();
    byteSize    = db.getJByteType().getSize();
    charSize    = db.getJCharType().getSize();
    booleanSize = db.getJBooleanType().getSize();
    intSize     = db.getJIntType().getSize();
    shortSize   = db.getJShortType().getSize();
    longSize    = db.getJLongType().getSize();
    floatSize   = db.getJFloatType().getSize();
    doubleSize  = db.getJDoubleType().getSize();
    initialize(db);
  }
  public boolean equal(Oop o1, Oop o2) {
    if (o1 != null) return o1.equals(o2);
    return (o2 == null);
  }
  private long oopSize;
  private long byteSize;
  private long charSize;
  private long booleanSize;
  private long intSize;
  private long shortSize;
  private long longSize;
  private long floatSize;
  private long doubleSize;
  public long getOopSize()     { return oopSize;     }
  public long getByteSize()    { return byteSize;    }
  public long getCharSize()    { return charSize;    }
  public long getBooleanSize() { return booleanSize; }
  public long getIntSize()     { return intSize;     }
  public long getShortSize()   { return shortSize;   }
  public long getLongSize()    { return longSize;    }
  public long getFloatSize()   { return floatSize;   }
  public long getDoubleSize()  { return doubleSize;  }
  public MethodKlass            getMethodKlassObj()            { return methodKlassObj; }
  public ConstMethodKlass       getConstMethodKlassObj()       { return constMethodKlassObj; }
  public MethodDataKlass        getMethodDataKlassObj()        { return methodDataKlassObj; }
  public ConstantPoolKlass      getConstantPoolKlassObj()      { return constantPoolKlassObj; }
  public ConstantPoolCacheKlass getConstantPoolCacheKlassObj() { return constantPoolCacheKlassObj; }
  public KlassKlass             getKlassKlassObj()             { return klassKlassObj; }
  public ArrayKlassKlass        getArrayKlassKlassObj()        { return arrayKlassKlassObj; }
  public InstanceKlassKlass     getInstanceKlassKlassObj()     { return instanceKlassKlassObj; }
  public ObjArrayKlassKlass     getObjArrayKlassKlassObj()     { return objArrayKlassKlassObj; }
  public TypeArrayKlassKlass    getTypeArrayKlassKlassObj()    { return typeArrayKlassKlassObj; }
  public TypeArrayKlass         getBoolArrayKlassObj()         { return boolArrayKlassObj; }
  public TypeArrayKlass         getByteArrayKlassObj()         { return byteArrayKlassObj; }
  public TypeArrayKlass         getCharArrayKlassObj()         { return charArrayKlassObj; }
  public TypeArrayKlass         getIntArrayKlassObj()          { return intArrayKlassObj; }
  public TypeArrayKlass         getShortArrayKlassObj()        { return shortArrayKlassObj; }
  public TypeArrayKlass         getLongArrayKlassObj()         { return longArrayKlassObj; }
  public TypeArrayKlass         getSingleArrayKlassObj()       { return singleArrayKlassObj; }
  public TypeArrayKlass         getDoubleArrayKlassObj()       { return doubleArrayKlassObj; }
  public CompiledICHolderKlass  getCompiledICHolderKlassObj()  {
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(!VM.getVM().isCore(), "must not be called for core build");
    }
    return compiledICHolderKlassObj;
  }
  public Klass typeArrayKlassObj(int t) {
    if (t == BasicType.getTBoolean()) return getBoolArrayKlassObj();
    if (t == BasicType.getTChar())    return getCharArrayKlassObj();
    if (t == BasicType.getTFloat())   return getSingleArrayKlassObj();
    if (t == BasicType.getTDouble())  return getDoubleArrayKlassObj();
    if (t == BasicType.getTByte())    return getByteArrayKlassObj();
    if (t == BasicType.getTShort())   return getShortArrayKlassObj();
    if (t == BasicType.getTInt())     return getIntArrayKlassObj();
    if (t == BasicType.getTLong())    return getLongArrayKlassObj();
    throw new RuntimeException("Illegal basic type " + t);
  }
  public static interface ObjectFilter {
    public boolean canInclude(Oop obj);
  }
  public void iterate(HeapVisitor visitor) {
    iterateLiveRegions(collectLiveRegions(), visitor, null);
  }
  public void iterate(HeapVisitor visitor, ObjectFilter of) {
    iterateLiveRegions(collectLiveRegions(), visitor, of);
  }
  public void iterateObjectsOfKlass(HeapVisitor visitor, final Klass k, boolean includeSubtypes) {
    if (includeSubtypes) {
      if (k.isFinal()) {
        iterateExact(visitor, k);
      } else {
        iterateSubtypes(visitor, k);
      }
    } else {
      if (!k.isAbstract() && !k.isInterface()) {
        iterateExact(visitor, k);
      }
    }
  }
  public void iterateObjectsOfKlass(HeapVisitor visitor, final Klass k) {
    iterateObjectsOfKlass(visitor, k, true);
  }
  public void iterateRaw(RawHeapVisitor visitor) {
    List liveRegions = collectLiveRegions();
    long totalSize = 0;
    for (int i = 0; i < liveRegions.size(); i += 2) {
      Address bottom = (Address) liveRegions.get(i);
      Address top    = (Address) liveRegions.get(i+1);
      totalSize += top.minus(bottom);
    }
    visitor.prologue(totalSize);
    for (int i = 0; i < liveRegions.size(); i += 2) {
      Address bottom = (Address) liveRegions.get(i);
      Address top    = (Address) liveRegions.get(i+1);
      while (bottom.lessThan(top)) {
        visitor.visitAddress(bottom);
        bottom = bottom.addOffsetTo(VM.getVM().getAddressSize());
      }
    }
    visitor.epilogue();
  }
  public void iteratePerm(HeapVisitor visitor) {
    CollectedHeap heap = VM.getVM().getUniverse().heap();
    List liveRegions = new ArrayList();
    addPermGenLiveRegions(liveRegions, heap);
    sortLiveRegions(liveRegions);
    iterateLiveRegions(liveRegions, visitor, null);
  }
  public boolean isValidMethod(OopHandle handle) {
    OopHandle klass = Oop.getKlassForOopHandle(handle);
    if (klass != null && klass.equals(methodKlassHandle)) {
      return true;
    }
    return false;
  }
  public Oop newOop(OopHandle handle) {
    if (handle == null) return null;
    if (handle.equals(methodKlassHandle))              return getMethodKlassObj();
    if (handle.equals(constMethodKlassHandle))         return getConstMethodKlassObj();
    if (handle.equals(constantPoolKlassHandle))        return getConstantPoolKlassObj();
    if (handle.equals(constantPoolCacheKlassHandle))   return getConstantPoolCacheKlassObj();
    if (handle.equals(instanceKlassKlassHandle))       return getInstanceKlassKlassObj();
    if (handle.equals(objArrayKlassKlassHandle))       return getObjArrayKlassKlassObj();
    if (handle.equals(klassKlassHandle))               return getKlassKlassObj();
    if (handle.equals(arrayKlassKlassHandle))          return getArrayKlassKlassObj();
    if (handle.equals(typeArrayKlassKlassHandle))      return getTypeArrayKlassKlassObj();
    if (handle.equals(boolArrayKlassHandle))           return getBoolArrayKlassObj();
    if (handle.equals(byteArrayKlassHandle))           return getByteArrayKlassObj();
    if (handle.equals(charArrayKlassHandle))           return getCharArrayKlassObj();
    if (handle.equals(intArrayKlassHandle))            return getIntArrayKlassObj();
    if (handle.equals(shortArrayKlassHandle))          return getShortArrayKlassObj();
    if (handle.equals(longArrayKlassHandle))           return getLongArrayKlassObj();
    if (handle.equals(singleArrayKlassHandle))         return getSingleArrayKlassObj();
    if (handle.equals(doubleArrayKlassHandle))         return getDoubleArrayKlassObj();
    if (!VM.getVM().isCore()) {
      if (handle.equals(compiledICHolderKlassHandle))  return getCompiledICHolderKlassObj();
      if (handle.equals(methodDataKlassHandle))        return getMethodDataKlassObj();
    }
    OopHandle klass = Oop.getKlassForOopHandle(handle);
    if (klass != null) {
      if (klass.equals(methodKlassHandle))              return new Method(handle, this);
      if (klass.equals(constMethodKlassHandle))         return new ConstMethod(handle, this);
      if (klass.equals(constantPoolKlassHandle))        return new ConstantPool(handle, this);
      if (klass.equals(constantPoolCacheKlassHandle))   return new ConstantPoolCache(handle, this);
      if (!VM.getVM().isCore()) {
        if (klass.equals(compiledICHolderKlassHandle))  return new CompiledICHolder(handle, this);
        if (klass.equals(methodDataKlassHandle))        return new MethodData(handle, this);
      }
      if (klass.equals(instanceKlassKlassHandle)) {
          InstanceKlass ik = new InstanceKlass(handle, this);
          if (ik.getName().asString().equals("java/lang/Class")) {
              return new InstanceMirrorKlass(handle, this);
          }
          return ik;
      }
      if (klass.equals(objArrayKlassKlassHandle))       return new ObjArrayKlass(handle, this);
      if (klass.equals(typeArrayKlassKlassHandle))      return new TypeArrayKlass(handle, this);
      OopHandle klassKlass = Oop.getKlassForOopHandle(klass);
      if (klassKlass != null) {
        if (klassKlass.equals(instanceKlassKlassHandle))    return new Instance(handle, this);
        if (klassKlass.equals(objArrayKlassKlassHandle))    return new ObjArray(handle, this);
        if (klassKlass.equals(typeArrayKlassKlassHandle))   return new TypeArray(handle, this);
      }
    }
    if (DEBUG) {
      System.err.println("Unknown oop at " + handle);
      System.err.println("Oop's klass is " + klass);
    }
    throw new UnknownOopException();
  }
  public void print() {
    HeapPrinter printer = new HeapPrinter(System.out);
    iterate(printer);
  }
  private void iterateExact(HeapVisitor visitor, final Klass k) {
    iterateLiveRegions(collectLiveRegions(), visitor, new ObjectFilter() {
          public boolean canInclude(Oop obj) {
            Klass tk = obj.getKlass();
            return (tk != null && tk.equals(k));
          }
        });
  }
  private void iterateSubtypes(HeapVisitor visitor, final Klass k) {
    iterateLiveRegions(collectLiveRegions(), visitor, new ObjectFilter() {
          public boolean canInclude(Oop obj) {
            Klass tk = obj.getKlass();
            return (tk != null && tk.isSubtypeOf(k));
          }
        });
  }
  private void iterateLiveRegions(List liveRegions, HeapVisitor visitor, ObjectFilter of) {
    long totalSize = 0;
    for (int i = 0; i < liveRegions.size(); i += 2) {
      Address bottom = (Address) liveRegions.get(i);
      Address top    = (Address) liveRegions.get(i+1);
      totalSize += top.minus(bottom);
    }
    visitor.prologue(totalSize);
    CompactibleFreeListSpace cmsSpaceOld = null;
    CompactibleFreeListSpace cmsSpacePerm = null;
    CollectedHeap heap = VM.getVM().getUniverse().heap();
    if (heap instanceof GenCollectedHeap) {
      GenCollectedHeap genHeap = (GenCollectedHeap) heap;
      Generation genOld = genHeap.getGen(1);
      Generation genPerm = genHeap.permGen();
      if (genOld instanceof ConcurrentMarkSweepGeneration) {
          ConcurrentMarkSweepGeneration concGen = (ConcurrentMarkSweepGeneration)genOld;
          cmsSpaceOld = concGen.cmsSpace();
      }
      if (genPerm instanceof ConcurrentMarkSweepGeneration) {
          ConcurrentMarkSweepGeneration concGen = (ConcurrentMarkSweepGeneration)genPerm;
          cmsSpacePerm = concGen.cmsSpace();
      }
    }
    for (int i = 0; i < liveRegions.size(); i += 2) {
      Address bottom = (Address) liveRegions.get(i);
      Address top    = (Address) liveRegions.get(i+1);
      try {
        OopHandle handle = bottom.addOffsetToAsOopHandle(0);
        while (handle.lessThan(top)) {
        Oop obj = null;
          try {
            obj = newOop(handle);
          } catch (UnknownOopException exp) {
            if (DEBUG) {
              throw new RuntimeException(" UnknownOopException  " + exp);
            }
          }
          if (obj == null) {
             System.err.println("Finding object size using Printezis bits and skipping over...");
             long size = 0;
             if ( (cmsSpaceOld != null) && cmsSpaceOld.contains(handle) ){
                 size = cmsSpaceOld.collector().blockSizeUsingPrintezisBits(handle);
             } else if ((cmsSpacePerm != null) && cmsSpacePerm.contains(handle) ){
                 size = cmsSpacePerm.collector().blockSizeUsingPrintezisBits(handle);
             }
             if (size <= 0) {
                throw new UnknownOopException();
             }
             handle = handle.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(size));
             continue;
          }
          if (of == null || of.canInclude(obj)) {
                  if (visitor.doObj(obj)) {
                          break;
                  }
          }
          if ( (cmsSpaceOld != null) && cmsSpaceOld.contains(handle) ||
               (cmsSpacePerm != null) && cmsSpacePerm.contains(handle) ) {
              handle = handle.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(obj.getObjectSize()) );
          } else {
              handle = handle.addOffsetToAsOopHandle(obj.getObjectSize());
          }
        }
      }
      catch (AddressException e) {
      }
      catch (UnknownOopException e) {
      }
    }
    visitor.epilogue();
  }
  private void addPermGenLiveRegions(List output, CollectedHeap heap) {
    LiveRegionsCollector lrc = new LiveRegionsCollector(output);
    if (heap instanceof GenCollectedHeap) {
       GenCollectedHeap genHeap = (GenCollectedHeap) heap;
       Generation gen = genHeap.permGen();
       gen.spaceIterate(lrc, true);
    } else if (heap instanceof ParallelScavengeHeap) {
       ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
       PSPermGen permGen = psh.permGen();
       addLiveRegions(permGen.objectSpace().getLiveRegions(), output);
    } else {
       if (Assert.ASSERTS_ENABLED) {
          Assert.that(false, "Expecting GenCollectedHeap or ParallelScavengeHeap, but got " +
                             heap.getClass().getName());
       }
    }
  }
  private void addLiveRegions(List input, List output) {
     for (Iterator itr = input.iterator(); itr.hasNext();) {
        MemRegion reg = (MemRegion) itr.next();
        Address top = reg.end();
        Address bottom = reg.start();
        if (Assert.ASSERTS_ENABLED) {
           Assert.that(top != null, "top address in a live region should not be null");
        }
        if (Assert.ASSERTS_ENABLED) {
           Assert.that(bottom != null, "bottom address in a live region should not be null");
        }
        output.add(top);
        output.add(bottom);
     }
  }
  private class LiveRegionsCollector implements SpaceClosure {
     LiveRegionsCollector(List l) {
        liveRegions = l;
     }
     public void doSpace(Space s) {
        addLiveRegions(s.getLiveRegions(), liveRegions);
     }
     private List liveRegions;
  }
  private List collectLiveRegions() {
    List liveRegions = new ArrayList();
    LiveRegionsCollector lrc = new LiveRegionsCollector(liveRegions);
    CollectedHeap heap = VM.getVM().getUniverse().heap();
    if (heap instanceof GenCollectedHeap) {
       GenCollectedHeap genHeap = (GenCollectedHeap) heap;
       for (int i = 0; i < genHeap.nGens(); i++) {
         Generation gen = genHeap.getGen(i);
         gen.spaceIterate(lrc, true);
       }
    } else if (heap instanceof ParallelScavengeHeap) {
       ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
       PSYoungGen youngGen = psh.youngGen();
       addLiveRegions(youngGen.edenSpace().getLiveRegions(), liveRegions);
       addLiveRegions(youngGen.fromSpace().getLiveRegions(), liveRegions);
       PSOldGen oldGen = psh.oldGen();
       addLiveRegions(oldGen.objectSpace().getLiveRegions(), liveRegions);
    } else {
       if (Assert.ASSERTS_ENABLED) {
          Assert.that(false, "Expecting GenCollectedHeap or ParallelScavengeHeap, but got " +
                              heap.getClass().getName());
       }
    }
    addPermGenLiveRegions(liveRegions, heap);
    if (VM.getVM().getUseTLAB()) {
      for (JavaThread thread = VM.getVM().getThreads().first(); thread != null; thread = thread.next()) {
        if (thread.isJavaThread()) {
          ThreadLocalAllocBuffer tlab = thread.tlab();
          if (tlab.start() != null) {
            if ((tlab.top() == null) || (tlab.end() == null)) {
              System.err.print("Warning: skipping invalid TLAB for thread ");
              thread.printThreadIDOn(System.err);
              System.err.println();
            } else {
              liveRegions.add(tlab.start());
              liveRegions.add(tlab.start());
              liveRegions.add(tlab.top());
              liveRegions.add(tlab.end());
            }
          }
        }
      }
    }
    sortLiveRegions(liveRegions);
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(liveRegions.size() % 2 == 0, "Must have even number of region boundaries");
    }
    return liveRegions;
  }
  private void sortLiveRegions(List liveRegions) {
    Collections.sort(liveRegions, new Comparator() {
        public int compare(Object o1, Object o2) {
          Address a1 = (Address) o1;
          Address a2 = (Address) o2;
          if (AddressOps.lt(a1, a2)) {
            return -1;
          } else if (AddressOps.gt(a1, a2)) {
            return 1;
          }
          return 0;
        }
      });
  }
}
