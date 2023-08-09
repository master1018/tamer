public class OopUtilities implements  JVMTIThreadState {
  private static IntField offsetField;
  private static IntField countField;
  private static OopField valueField;
  private static OopField threadGroupParentField;
  private static OopField threadGroupNameField;
  private static IntField threadGroupNThreadsField;
  private static OopField threadGroupThreadsField;
  private static IntField threadGroupNGroupsField;
  private static OopField threadGroupGroupsField;
  private static OopField threadNameField;
  private static OopField threadGroupField;
  private static LongField threadEETopField;
  private static IntField threadStatusField;
  private static OopField threadParkBlockerField;
  private static int THREAD_STATUS_NEW;
  private static OopField absOwnSyncOwnerThreadField;
  static {
    VM.registerVMInitializedObserver(new Observer() {
        public void update(Observable o, Object data) {
          initialize(VM.getVM().getTypeDataBase());
        }
      });
  }
  private static synchronized void initialize(TypeDataBase db) {
  }
  public static String charArrayToString(TypeArray charArray) {
    if (charArray == null) {
      return null;
    }
    return charArrayToString(charArray, 0, (int) charArray.getLength());
  }
  public static String charArrayToString(TypeArray charArray, int offset, int length) {
    if (charArray == null) {
      return null;
    }
    final int limit = offset + length;
    if (Assert.ASSERTS_ENABLED) {
      Assert.that(offset >= 0 && limit <= charArray.getLength(), "out of bounds");
    }
    StringBuffer buf = new StringBuffer(length);
    for (int i = offset; i < limit; i++) {
      buf.append(charArray.getCharAt(i));
    }
    return buf.toString();
  }
  public static String stringOopToString(Oop stringOop) {
    if (offsetField == null) {
      InstanceKlass k = (InstanceKlass) stringOop.getKlass();
      offsetField = (IntField) k.findField("offset", "I");
      countField  = (IntField) k.findField("count",  "I");
      valueField  = (OopField) k.findField("value",  "[C");
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(offsetField != null &&
                    countField != null &&
                    valueField != null, "must find all java.lang.String fields");
      }
    }
    return charArrayToString((TypeArray) valueField.getValue(stringOop),
                             offsetField.getValue(stringOop),
                             countField.getValue(stringOop));
  }
  private static void initThreadGroupFields() {
    if (threadGroupParentField == null) {
      SystemDictionary sysDict = VM.getVM().getSystemDictionary();
      InstanceKlass k = sysDict.getThreadGroupKlass();
      threadGroupParentField   = (OopField) k.findField("parent",   "Ljava/lang/ThreadGroup;");
      threadGroupNameField     = (OopField) k.findField("name",     "Ljava/lang/String;");
      threadGroupNThreadsField = (IntField) k.findField("nthreads", "I");
      threadGroupThreadsField  = (OopField) k.findField("threads",  "[Ljava/lang/Thread;");
      threadGroupNGroupsField  = (IntField) k.findField("ngroups",  "I");
      threadGroupGroupsField   = (OopField) k.findField("groups",   "[Ljava/lang/ThreadGroup;");
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(threadGroupParentField   != null &&
                    threadGroupNameField     != null &&
                    threadGroupNThreadsField != null &&
                    threadGroupThreadsField  != null &&
                    threadGroupNGroupsField  != null &&
                    threadGroupGroupsField   != null, "must find all java.lang.ThreadGroup fields");
      }
    }
  }
  public static Oop threadGroupOopGetParent(Oop threadGroupOop) {
    initThreadGroupFields();
    return threadGroupParentField.getValue(threadGroupOop);
  }
  public static String threadGroupOopGetName(Oop threadGroupOop) {
    initThreadGroupFields();
    return stringOopToString(threadGroupNameField.getValue(threadGroupOop));
  }
  public static Oop[] threadGroupOopGetThreads(Oop threadGroupOop) {
    initThreadGroupFields();
    int nthreads = threadGroupNThreadsField.getValue(threadGroupOop);
    Oop[] result = new Oop[nthreads];
    ObjArray threads = (ObjArray) threadGroupThreadsField.getValue(threadGroupOop);
    for (int i = 0; i < nthreads; i++) {
      result[i] = threads.getObjAt(i);
    }
    return result;
  }
  public static Oop[] threadGroupOopGetGroups(Oop threadGroupOop) {
    initThreadGroupFields();
    int ngroups = threadGroupNGroupsField.getValue(threadGroupOop);
    Oop[] result = new Oop[ngroups];
    ObjArray groups = (ObjArray) threadGroupGroupsField.getValue(threadGroupOop);
    for (int i = 0; i < ngroups; i++) {
      result[i] = groups.getObjAt(i);
    }
    return result;
  }
  private static void initThreadFields() {
    if (threadNameField == null) {
      SystemDictionary sysDict = VM.getVM().getSystemDictionary();
      InstanceKlass k = sysDict.getThreadKlass();
      threadNameField  = (OopField) k.findField("name", "[C");
      threadGroupField = (OopField) k.findField("group", "Ljava/lang/ThreadGroup;");
      threadEETopField = (LongField) k.findField("eetop", "J");
      threadStatusField = (IntField) k.findField("threadStatus", "I");
      threadParkBlockerField = (OopField) k.findField("parkBlocker",
                                     "Ljava/lang/Object;");
      TypeDataBase db = VM.getVM().getTypeDataBase();
      THREAD_STATUS_NEW = db.lookupIntConstant("java_lang_Thread::NEW").intValue();
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(threadNameField   != null &&
                    threadGroupField  != null &&
                    threadEETopField  != null, "must find all java.lang.Thread fields");
      }
    }
  }
  public static Oop threadOopGetThreadGroup(Oop threadOop) {
    initThreadFields();
    return threadGroupField.getValue(threadOop);
  }
  public static String threadOopGetName(Oop threadOop) {
    initThreadFields();
    return charArrayToString((TypeArray) threadNameField.getValue(threadOop));
  }
  public static JavaThread threadOopGetJavaThread(Oop threadOop) {
    initThreadFields();
    Address addr = threadOop.getHandle().getAddressAt(threadEETopField.getOffset());
    if (addr == null) {
      return null;
    }
    return VM.getVM().getThreads().createJavaThreadWrapper(addr);
  }
  public static int threadOopGetThreadStatus(Oop threadOop) {
    initThreadFields();
    if (threadStatusField != null) {
      return (int) threadStatusField.getValue(threadOop);
    } else {
      JavaThread thr = threadOopGetJavaThread(threadOop);
      if (thr == null) {
        return THREAD_STATUS_NEW;
      } else {
        return JVMTI_THREAD_STATE_ALIVE;
      }
    }
  }
  public static Oop threadOopGetParkBlocker(Oop threadOop) {
    initThreadFields();
    if (threadParkBlockerField != null) {
      return threadParkBlockerField.getValue(threadOop);
    }
    return null;
  }
  private static void initAbsOwnSyncFields() {
    if (absOwnSyncOwnerThreadField == null) {
       SystemDictionary sysDict = VM.getVM().getSystemDictionary();
       InstanceKlass k = sysDict.getAbstractOwnableSynchronizerKlass();
       absOwnSyncOwnerThreadField =
           (OopField) k.findField("exclusiveOwnerThread",
                                  "Ljava/lang/Thread;");
    }
  }
  public static Oop abstractOwnableSynchronizerGetOwnerThread(Oop oop) {
    initAbsOwnSyncFields();
    if (absOwnSyncOwnerThreadField == null) {
      return null; 
    } else {
      return absOwnSyncOwnerThreadField.getValue(oop);
    }
  }
}
