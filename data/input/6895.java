public class ReversePtrsAnalysis {
  private static final boolean DEBUG = false;
  public ReversePtrsAnalysis() {
  }
  public void setHeapProgressThunk(HeapProgressThunk thunk) {
    progressThunk = thunk;
  }
  public void run() {
    if (VM.getVM().getRevPtrs() != null) {
      return; 
    }
    VM vm = VM.getVM();
    rp = new ReversePtrs();
    vm.setRevPtrs(rp);
    Universe universe = vm.getUniverse();
    CollectedHeap collHeap = universe.heap();
    usedSize = collHeap.used();
    visitedSize = 0;
    if (progressThunk != null) {
      progressThunk.heapIterationFractionUpdate(0);
    }
    markBits = new MarkBits(collHeap);
    heap = vm.getObjectHeap();
    for (JavaThread thread = VM.getVM().getThreads().first();
         thread != null;
         thread = thread.next()) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      thread.printThreadIDOn(new PrintStream(bos));
      String threadDesc =
        " in thread \"" + thread.getThreadName() +
        "\" (id " + bos.toString() + ")";
      doStack(thread,
              new RootVisitor("Stack root" + threadDesc));
      doJNIHandleBlock(thread.activeHandles(),
                       new RootVisitor("JNI handle root" + threadDesc));
    }
    JNIHandles handles = VM.getVM().getJNIHandles();
    doJNIHandleBlock(handles.globalHandles(),
                     new RootVisitor("Global JNI handle root"));
    doJNIHandleBlock(handles.weakGlobalHandles(),
                     new RootVisitor("Weak global JNI handle root"));
    heap.iteratePerm(new DefaultHeapVisitor() {
        public boolean doObj(Oop obj) {
          if (obj instanceof InstanceKlass) {
            final InstanceKlass ik = (InstanceKlass) obj;
            ik.iterateStaticFields(
               new DefaultOopVisitor() {
                   public void doOop(OopField field, boolean isVMField) {
                     Oop next = field.getValue(getObj());
                     LivenessPathElement lp = new LivenessPathElement(null,
                             new NamedFieldIdentifier("Static field \"" +
                                                field.getID().getName() +
                                                "\" in class \"" +
                                                ik.getName().asString() + "\""));
                     rp.put(lp, next);
                     try {
                       markAndTraverse(next);
                     } catch (AddressException e) {
                       System.err.print("RevPtrs analysis: WARNING: AddressException at 0x" +
                                        Long.toHexString(e.getAddress()) +
                                        " while traversing static fields of InstanceKlass ");
                       ik.printValueOn(System.err);
                       System.err.println();
                     } catch (UnknownOopException e) {
                       System.err.println("RevPtrs analysis: WARNING: UnknownOopException while " +
                                          "traversing static fields of InstanceKlass ");
                       ik.printValueOn(System.err);
                       System.err.println();
                     }
                   }
                 });
          }
                  return false;
        }
      });
    if (progressThunk != null) {
      progressThunk.heapIterationComplete();
    }
    markBits = null;
  }
  private HeapProgressThunk   progressThunk;
  private long                usedSize;
  private long                visitedSize;
  private double              lastNotificationFraction;
  private static final double MINIMUM_NOTIFICATION_FRACTION = 0.01;
  private ObjectHeap          heap;
  private MarkBits            markBits;
  private int                 depth; 
  private ReversePtrs         rp;
  private void markAndTraverse(OopHandle handle) {
    try {
      markAndTraverse(heap.newOop(handle));
    } catch (AddressException e) {
      System.err.println("RevPtrs analysis: WARNING: AddressException at 0x" +
                         Long.toHexString(e.getAddress()) +
                         " while traversing oop at " + handle);
    } catch (UnknownOopException e) {
      System.err.println("RevPtrs analysis: WARNING: UnknownOopException for " +
                         "oop at " + handle);
    }
  }
  private void printHeader() {
    for (int i = 0; i < depth; i++) {
      System.err.print(" ");
    }
  }
  private void markAndTraverse(final Oop obj) {
    if (obj == null) {
      return;
    }
    if (!markBits.mark(obj)) {
      return;
    }
    final Stack workList = new Stack();
    Oop next = obj;
    try {
      while (true) {
        final Oop currObj = next;
        if (progressThunk != null) {
          visitedSize += currObj.getObjectSize();
          double curFrac = (double) visitedSize / (double) usedSize;
          if (curFrac >
              lastNotificationFraction + MINIMUM_NOTIFICATION_FRACTION) {
            progressThunk.heapIterationFractionUpdate(curFrac);
            lastNotificationFraction = curFrac;
          }
        }
        if (DEBUG) {
          ++depth;
          printHeader();
          System.err.println("ReversePtrs.markAndTraverse(" +
              currObj.getHandle() + ")");
        }
        currObj.iterate(new DefaultOopVisitor() {
          public void doOop(OopField field, boolean isVMField) {
            Oop next = field.getValue(currObj);
            rp.put(new LivenessPathElement(currObj, field.getID()), next);
            if ((next != null) && markBits.mark(next)) {
              workList.push(next);
            }
          }
        }, false);
        if (DEBUG) {
          --depth;
        }
        next = (Oop) workList.pop();
      }
    } catch (EmptyStackException e) {
    } catch (NullPointerException e) {
      System.err.println("ReversePtrs: WARNING: " + e +
        " during traversal");
    } catch (Exception e) {
      System.err.println("ReversePtrs: WARNING: " + e +
        " during traversal");
    }
  }
  class RootVisitor implements AddressVisitor {
    RootVisitor(String baseRootDescription) {
      this.baseRootDescription = baseRootDescription;
    }
    public void visitAddress(Address addr) {
      Oop next = heap.newOop(addr.getOopHandleAt(0));
      LivenessPathElement lp = new LivenessPathElement(null,
                                        new NamedFieldIdentifier(baseRootDescription +
                                                                 " @ " + addr));
      rp.put(lp, next);
      markAndTraverse(next);
    }
    public void visitCompOopAddress(Address addr) {
      Oop next = heap.newOop(addr.getCompOopHandleAt(0));
      LivenessPathElement lp = new LivenessPathElement(null,
                                        new NamedFieldIdentifier(baseRootDescription +
                                                                 " @ " + addr));
      rp.put(lp, next);
      markAndTraverse(next);
    }
    private String baseRootDescription;
  }
  private void doStack(JavaThread thread, AddressVisitor oopVisitor) {
    for (StackFrameStream fst = new StackFrameStream(thread); !fst.isDone(); fst.next()) {
      fst.getCurrent().oopsDo(oopVisitor, fst.getRegisterMap());
    }
  }
  private void doJNIHandleBlock(JNIHandleBlock handles, AddressVisitor oopVisitor) {
    handles.oopsDo(oopVisitor);
  }
}
