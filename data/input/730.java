public class PermStat extends Tool {
   boolean verbose = true;
   public static void main(String[] args) {
      PermStat ps = new PermStat();
      ps.start(args);
      ps.stop();
   }
   private static class ClassData {
      Klass klass;
      long  size;
      ClassData(Klass klass, long size) {
         this.klass = klass; this.size = size;
      }
   }
   private static class LoaderData {
      long     numClasses;
      long     classSize;
      List     classDetail = new ArrayList(); 
   }
   public void run() {
      printInternStringStatistics();
      printClassLoaderStatistics();
   }
   private void printInternStringStatistics() {
      class StringStat implements StringTable.StringVisitor {
         private int count;
         private long size;
         private OopField stringValueField;
         StringStat() {
            VM vm = VM.getVM();
            SystemDictionary sysDict = vm.getSystemDictionary();
            InstanceKlass strKlass = sysDict.getStringKlass();
            stringValueField = (OopField) strKlass.findField("value", "[C");
         }
         private long stringSize(Instance instance) {
            return instance.getObjectSize() +
                   stringValueField.getValue(instance).getObjectSize();
         }
         public void visit(Instance str) {
            count++;
            size += stringSize(str);
         }
         public void print() {
            System.out.println(count +
                  " intern Strings occupying " + size + " bytes.");
         }
      }
      StringStat stat = new StringStat();
      StringTable strTable = VM.getVM().getStringTable();
      strTable.stringsDo(stat);
      stat.print();
   }
   private void printClassLoaderStatistics() {
      final PrintStream out = System.out;
      final PrintStream err = System.err;
      final Map loaderMap = new HashMap();
      final LoaderData bootstrapLoaderData = new LoaderData();
      if (verbose) {
         err.print("finding class loader instances ..");
      }
      VM vm = VM.getVM();
      ObjectHeap heap = vm.getObjectHeap();
      Klass classLoaderKlass = vm.getSystemDictionary().getClassLoaderKlass();
      try {
         heap.iterateObjectsOfKlass(new DefaultHeapVisitor() {
                         public boolean doObj(Oop oop) {
                            loaderMap.put(oop, new LoaderData());
                                                        return false;
                         }
                      }, classLoaderKlass);
      } catch (Exception se) {
         se.printStackTrace();
      }
      if (verbose) {
         err.println("done.");
         err.print("computing per loader stat ..");
      }
      SystemDictionary dict = VM.getVM().getSystemDictionary();
      dict.classesDo(new SystemDictionary.ClassAndLoaderVisitor() {
                        public void visit(Klass k, Oop loader) {
                           if (! (k instanceof InstanceKlass)) {
                              return;
                           }
                           LoaderData ld = (loader != null) ? (LoaderData)loaderMap.get(loader)
                                                            : bootstrapLoaderData;
                           if (ld != null) {
                              ld.numClasses++;
                              long size = computeSize((InstanceKlass)k);
                              ld.classDetail.add(new ClassData(k, size));
                              ld.classSize += size;
                           }
                        }
                     });
      if (verbose) {
         err.println("done.");
         err.print("please wait.. computing liveness");
      }
      ReversePtrsAnalysis analysis = new ReversePtrsAnalysis();
      if (verbose) {
         analysis.setHeapProgressThunk(new HeapProgressThunk() {
            public void heapIterationFractionUpdate(double fractionOfHeapVisited) {
               err.print('.');
            }
            public void heapIterationComplete() {
               err.println("done.");
            }
         });
      }
      try {
         analysis.run();
      } catch (Exception e) {
         if (verbose)
           err.println("liveness analysis may be inaccurate ...");
      }
      ReversePtrs liveness = VM.getVM().getRevPtrs();
      out.println("class_loader\tclasses\tbytes\tparent_loader\talive?\ttype");
      out.println();
      long numClassLoaders = 1L;
      long totalNumClasses = bootstrapLoaderData.numClasses;
      long totalClassSize  = bootstrapLoaderData.classSize;
      long numAliveLoaders = 1L;
      long numDeadLoaders  = 0L;
      out.print("<bootstrap>");
      out.print('\t');
      out.print(bootstrapLoaderData.numClasses);
      out.print('\t');
      out.print(bootstrapLoaderData.classSize);
      out.print('\t');
      out.print("  null  ");
      out.print('\t');
      out.print("live");
      out.print('\t');
      out.println("<internal>");
      for (Iterator keyItr = loaderMap.keySet().iterator(); keyItr.hasNext();) {
         Oop loader = (Oop) keyItr.next();
         LoaderData data = (LoaderData) loaderMap.get(loader);
         numClassLoaders ++;
         totalNumClasses += data.numClasses;
         totalClassSize  += data.classSize;
         out.print(loader.getHandle());
         out.print('\t');
         out.print(data.numClasses);
         out.print('\t');
         out.print(data.classSize);
         out.print('\t');
         class ParentFinder extends DefaultOopVisitor {
            public void doOop(OopField field, boolean isVMField) {
               if (field.getID().getName().equals("parent")) {
                  parent = field.getValue(getObj());
               }
            }
            private Oop parent = null;
            public Oop getParent() { return parent; }
         }
         ParentFinder parentFinder = new ParentFinder();
         loader.iterate(parentFinder, false);
         Oop parent = parentFinder.getParent();
         out.print((parent != null)? parent.getHandle().toString() : "  null  ");
         out.print('\t');
         boolean alive = (liveness != null) ? (liveness.get(loader) != null) : true;
         out.print(alive? "live" : "dead");
         if (alive) numAliveLoaders++; else numDeadLoaders++;
         out.print('\t');
         Klass loaderKlass = loader.getKlass();
         if (loaderKlass != null) {
            out.print(loaderKlass.getName().asString());
            out.print('@');
            out.print(loader.getKlass().getHandle());
         } else {
            out.print("    null!    ");
         }
         out.println();
      }
      out.println();
      out.print("total = ");
      out.print(numClassLoaders);
      out.print('\t');
      out.print(totalNumClasses);
      out.print('\t');
      out.print(totalClassSize);
      out.print('\t');
      out.print("    N/A    ");
      out.print('\t');
      out.print("alive=");
      out.print(numAliveLoaders);
      out.print(", dead=");
      out.print(numDeadLoaders);
      out.print('\t');
      out.print("    N/A    ");
      out.println();
   }
   private static long objectSize(Oop oop) {
      return oop == null ? 0L : oop.getObjectSize();
   }
   private static long arraySize(Array arr) {
     return arr.getLength() != 0L ? arr.getObjectSize() : 0L;
   }
   private long computeSize(InstanceKlass k) {
      long size = 0L;
      size += k.getObjectSize();
      ConstantPool cp = k.getConstants();
      size += cp.getObjectSize();
      size += objectSize(cp.getCache());
      size += objectSize(cp.getTags());
      size += arraySize(k.getLocalInterfaces());
      size += arraySize(k.getTransitiveInterfaces());
      size += objectSize(k.getInnerClasses());
      size += objectSize(k.getFields());
      ObjArray methods = k.getMethods();
      int nmethods = (int) methods.getLength();
      if (nmethods != 0L) {
         size += methods.getObjectSize();
         for (int i = 0; i < nmethods; ++i) {
            Method m = (Method) methods.getObjAt(i);
            size += m.getObjectSize();
            size += objectSize(m.getConstMethod());
         }
      }
      size += arraySize(k.getMethodOrdering());
      return size;
   }
}
