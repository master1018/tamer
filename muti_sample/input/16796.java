public class PointerLocation {
  Address addr;
  CollectedHeap heap;
  Generation gen;
  Generation permGen;
  boolean inTLAB;
  JavaThread tlabThread;
  ThreadLocalAllocBuffer tlab;
  boolean inInterpreter;
  boolean inCodeCache;
  InterpreterCodelet interpreterCodelet;
  CodeBlob blob;
  boolean inBlobCode;
  boolean inBlobData;
  boolean inBlobOops;
  boolean inBlobUnknownLocation;
  boolean inStrongGlobalJNIHandleBlock;
  boolean inWeakGlobalJNIHandleBlock;
  boolean inLocalJNIHandleBlock;
  JNIHandleBlock handleBlock;
  sun.jvm.hotspot.runtime.Thread handleThread;
  public PointerLocation(Address addr) {
    this.addr = addr;
  }
  public boolean isInHeap() {
    return (heap != null || (gen != null) || (permGen != null));
  }
  public boolean isInNewGen() {
    return ((gen != null) && (gen.level() == 0));
  }
  public boolean isInOldGen() {
    return ((gen != null) && (gen.level() == 1));
  }
  public boolean isInPermGen() {
    return (permGen != null);
  }
  public boolean inOtherGen() {
    return (!isInNewGen() && !isInOldGen() && !isInPermGen());
  }
  public Generation getGeneration() {
    if (gen != null) {
      return gen;
    } else {
      return permGen;
    }
  }
  public boolean isInTLAB() {
    return inTLAB;
  }
  public JavaThread getTLABThread() {
    return tlabThread;
  }
  public ThreadLocalAllocBuffer getTLAB() {
    return tlab;
  }
  public boolean isInInterpreter() {
    return inInterpreter;
  }
  public InterpreterCodelet getInterpreterCodelet() {
    return interpreterCodelet;
  }
  public boolean isInCodeCache() {
    return inCodeCache;
  }
  public CodeBlob getCodeBlob() {
    return blob;
  }
  public boolean isInBlobCode() {
    return inBlobCode;
  }
  public boolean isInBlobData() {
    return inBlobData;
  }
  public boolean isInBlobOops() {
    return inBlobOops;
  }
  public boolean isInBlobUnknownLocation() {
    return inBlobUnknownLocation;
  }
  public boolean isInStrongGlobalJNIHandleBlock() {
    return inStrongGlobalJNIHandleBlock;
  }
  public boolean isInWeakGlobalJNIHandleBlock() {
    return inWeakGlobalJNIHandleBlock;
  }
  public boolean isInLocalJNIHandleBlock() {
    return inLocalJNIHandleBlock;
  }
  public JNIHandleBlock getJNIHandleBlock() {
    return handleBlock;
  }
  public sun.jvm.hotspot.runtime.Thread getJNIHandleThread() {
    return handleThread;
  }
  public boolean isUnknown() {
    return (!(isInHeap() || isInInterpreter() || isInCodeCache() ||
              isInStrongGlobalJNIHandleBlock() || isInWeakGlobalJNIHandleBlock() || isInLocalJNIHandleBlock()));
  }
  public String toString() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    printOn(new PrintStream(bos));
    return bos.toString();
  }
  public void print() {
    printOn(System.out);
  }
  public void printOn(PrintStream tty) {
    tty.print("Address ");
    if (addr == null) {
      tty.print("0x0");
    } else {
      tty.print(addr.toString());
    }
    tty.print(": ");
    if (isInHeap()) {
      if (isInTLAB()) {
        tty.print("In thread-local allocation buffer for thread \"" +
                  getTLABThread().getThreadName() + "\" (");
        getTLABThread().printThreadIDOn(tty);
        tty.print(") ");
        getTLAB().printOn(tty);
      } else {
        if (isInNewGen()) {
          tty.print("In new generation ");
        } else if (isInOldGen()) {
          tty.print("In old generation ");
        } else if (isInPermGen()) {
          tty.print("In perm generation ");
        } else if (gen != null) {
          tty.print("In Generation " + getGeneration().level());
        } else {
          tty.print("In unknown section of Java heap");
        }
        if (getGeneration() != null) {
          getGeneration().printOn(tty);
        }
      }
    } else if (isInInterpreter()) {
      tty.println("In interpreter codelet \"" + interpreterCodelet.getDescription() + "\"");
      interpreterCodelet.printOn(tty);
    } else if (isInCodeCache()) {
      CodeBlob b = getCodeBlob();
      tty.print("In ");
      if (isInBlobCode()) {
        tty.print("code");
      } else if (isInBlobData()) {
        tty.print("data");
      } else if (isInBlobOops()) {
        tty.print("oops");
      } else {
        if (Assert.ASSERTS_ENABLED) {
          Assert.that(isInBlobUnknownLocation(), "Should have known location in CodeBlob");
        }
        tty.print("unknown location");
      }
      tty.print(" in ");
      b.printOn(tty);
    } else if (isInStrongGlobalJNIHandleBlock() ||
               isInWeakGlobalJNIHandleBlock() ||
               isInLocalJNIHandleBlock()) {
      tty.print("In ");
      if (isInStrongGlobalJNIHandleBlock()) {
        tty.print("strong global");
      } else if (isInWeakGlobalJNIHandleBlock()) {
        tty.print("weak global");
      } else {
        tty.print("thread-local");
      }
      tty.print(" JNI handle block (" + handleBlock.top() + " handle slots present)");
      if (isInLocalJNIHandleBlock()) {
        if (handleThread.isJavaThread()) {
          tty.print(" for JavaThread ");
          ((JavaThread) handleThread).printThreadIDOn(tty);
        } else {
          tty.print("for a non-Java Thread");
        }
      }
    } else {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(isUnknown(), "Should have unknown location");
      }
      tty.print("In unknown location");
    }
  }
}
