public class JSJavaFactoryImpl implements JSJavaFactory {
   public JSJavaObject newJSJavaObject(Oop oop) {
      if (oop == null) return null;
      SoftReference sref = (SoftReference) om.get(oop);
      JSJavaObject res = (sref != null)? (JSJavaObject) sref.get() : null;
      if (res == null) {
         if (oop instanceof TypeArray) {
            res = new JSJavaTypeArray((TypeArray)oop, this);
         } else if (oop instanceof ObjArray) {
             res = new JSJavaObjArray((ObjArray)oop, this);
         } else if (oop instanceof Instance) {
            res = newJavaInstance((Instance) oop);
         } else if (oop instanceof Method) {
            res = new JSJavaMethod((Method) oop, this);
         }
      }
      if (res != null) {
         om.put(oop, new SoftReference(res));
      }
      return res;
   }
   public JSJavaKlass newJSJavaKlass(Klass klass) {
      JSJavaKlass res = null;
      if (klass instanceof InstanceKlass) {
          res = new JSJavaInstanceKlass((InstanceKlass) klass, this);
      } else if (klass instanceof ObjArrayKlass) {
          res = new JSJavaObjArrayKlass((ObjArrayKlass) klass, this);
      } else if (klass instanceof TypeArrayKlass) {
          res = new JSJavaTypeArrayKlass((TypeArrayKlass) klass, this);
      }
      if (res != null) {
         om.put(klass, new SoftReference(res));
      }
      return res;
   }
   public JSJavaField newJSJavaField(Field field) {
      if (field == null) return null;
      return new JSJavaField(field, this);
   }
   public JSJavaThread newJSJavaThread(JavaThread jthread) {
      if (jthread == null) return null;
      return new JSJavaThread(jthread, this);
   }
   public JSJavaFrame newJSJavaFrame(JavaVFrame jvf) {
      if (jvf == null) return null;
      return new JSJavaFrame(jvf, this);
   }
   public JSList newJSList(List list) {
      if (list == null) return null;
      return new JSList(list, this);
   }
   public JSMap newJSMap(Map map) {
      if (map == null) return null;
      return new JSMap(map, this);
   }
   public Object newJSJavaWrapper(Object item) {
      if (item == null) return null;
      if (item instanceof Oop) {
         return newJSJavaObject((Oop) item);
      } else if (item instanceof Field) {
         return newJSJavaField((Field) item);
      } else if (item instanceof JavaThread) {
         return newJSJavaThread((JavaThread) item);
      } else if (item instanceof JavaVFrame) {
         return newJSJavaFrame((JavaVFrame) item);
      } else if (item instanceof List) {
         return newJSList((List) item);
      } else if (item instanceof Map) {
         return newJSMap((Map) item);
      } else {
         return item;
      }
   }
   public JSJavaHeap newJSJavaHeap() {
      return new JSJavaHeap(this);
   }
   public JSJavaVM newJSJavaVM() {
      return new JSJavaVM(this);
   }
   private Symbol javaLangString() {
      if (javaLangString == null) {
         javaLangString = getSymbol("java/lang/String");
      }
      return javaLangString;
   }
   private Symbol javaLangThread() {
      if (javaLangThread == null) {
         javaLangThread = getSymbol("java/lang/Thread");
      }
      return javaLangThread;
   }
   private Symbol javaLangClass() {
      if (javaLangClass == null) {
         javaLangClass = getSymbol("java/lang/Class");
      }
      return javaLangClass;
   }
   private Symbol getSymbol(String str) {
      return VM.getVM().getSymbolTable().probe(str);
   }
   private JSJavaObject newJavaInstance(Instance instance) {
      Symbol className = instance.getKlass().getName();
      if (Assert.ASSERTS_ENABLED) {
         Assert.that(className != null, "Null class name");
      }
      JSJavaObject res = null;
      if (className.equals(javaLangString())) {
         res = new JSJavaString(instance, this);
      } else if (className.equals(javaLangThread())) {
         res = new JSJavaThread(instance, this);
      } else if (className.equals(javaLangClass())) {
         Klass reflectedType = java_lang_Class.asKlass(instance);
         if (reflectedType != null) {
             JSJavaKlass jk = newJSJavaKlass(reflectedType);
             if (jk == null) return null;
             res = new JSJavaClass(instance, jk, this);
         } else {
             return null;
         }
      } else {
         Klass kls = instance.getKlass().getSuper();
         while (kls != null) {
            className = kls.getName();
            if (className.equals(javaLangThread())) {
               res = new JSJavaThread(instance, this);
               break;
            }
            kls = kls.getSuper();
         }
      }
      if (res == null) {
         res = new JSJavaInstance(instance, this);
      }
      return res;
   }
   private Map om = new HashMap();
   private Symbol javaLangString;
   private Symbol javaLangThread;
   private Symbol javaLangClass;
}
