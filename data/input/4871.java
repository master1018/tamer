public class PStack extends Tool {
   public PStack(boolean v, boolean concurrentLocks) {
      this.verbose = v;
      this.concurrentLocks = concurrentLocks;
   }
   public PStack() {
      this(true, true);
   }
   public void run() {
      run(System.out);
   }
   public void run(PrintStream out) {
      Debugger dbg = getAgent().getDebugger();
      run(out, dbg, getAgent().isJavaMode());
   }
   public void run(PrintStream out, Debugger dbg) {
      run(out, dbg, true);
   }
   private void run(PrintStream out, Debugger dbg, final boolean isJava) {
      CDebugger cdbg = dbg.getCDebugger();
      if (cdbg != null) {
         ConcurrentLocksPrinter concLocksPrinter = null;
         if (isJava) {
            initJFrameCache();
            if (concurrentLocks) {
               concLocksPrinter = new ConcurrentLocksPrinter();
            }
            try {
               DeadlockDetector.print(out);
            } catch (Exception exp) {
               out.println("can't print deadlock information: " + exp.getMessage());
            }
         }
         List l = cdbg.getThreadList();
         final boolean cdbgCanDemangle = cdbg.canDemangle();
         for (Iterator itr = l.iterator() ; itr.hasNext();) {
            ThreadProxy th = (ThreadProxy) itr.next();
            try {
               CFrame f = cdbg.topFrameForThread(th);
               out.print("----------------- ");
               out.print(th);
               out.println(" -----------------");
               while (f != null) {
                  ClosestSymbol sym = f.closestSymbolToPC();
                  Address pc = f.pc();
                  out.print(pc + "\t");
                  if (sym != null) {
                     String name = sym.getName();
                     if (cdbgCanDemangle) {
                        name = cdbg.demangle(name);
                     }
                     out.print(name);
                     long diff = sym.getOffset();
                     if (diff != 0L) {
                        out.print(" + 0x" + Long.toHexString(diff));
                     }
                     out.println();
                  } else {
                     if (isJava) {
                        String[] names = null;
                        Interpreter interp = VM.getVM().getInterpreter();
                        if (interp.contains(pc)) {
                           names = getJavaNames(th, f.localVariableBase());
                           if (names == null || names.length == 0) {
                              out.print("<interpreter> ");
                              InterpreterCodelet ic = interp.getCodeletContaining(pc);
                              if (ic != null) {
                                 String desc = ic.getDescription();
                                 if (desc != null) out.print(desc);
                              }
                              out.println();
                           }
                        } else {
                           CodeCache c = VM.getVM().getCodeCache();
                           if (c.contains(pc)) {
                              CodeBlob cb = c.findBlobUnsafe(pc);
                              if (cb.isNMethod()) {
                                 names = getJavaNames(th, f.localVariableBase());
                                 if (names == null || names.length == 0) {
                                    out.println("<Unknown compiled code>");
                                 }
                              } else if (cb.isBufferBlob()) {
                                 out.println("<StubRoutines>");
                              } else if (cb.isRuntimeStub()) {
                                 out.println("<RuntimeStub>");
                              } else if (cb.isDeoptimizationStub()) {
                                 out.println("<DeoptimizationStub>");
                              } else if (cb.isUncommonTrapStub()) {
                                 out.println("<UncommonTrap>");
                              } else if (cb.isExceptionStub()) {
                                 out.println("<ExceptionStub>");
                              } else if (cb.isSafepointStub()) {
                                 out.println("<SafepointStub>");
                              } else {
                                 out.println("<Unknown code blob>");
                              }
                           } else {
                              printUnknown(out);
                           }
                        }
                        if (names != null && names.length != 0) {
                           for (int i = 0; i < names.length; i++) {
                               out.println(names[i]);
                           }
                        }
                     } else {
                        printUnknown(out);
                     }
                  }
                  f = f.sender();
               }
            } catch (Exception exp) {
               exp.printStackTrace();
            }
            if (isJava && concurrentLocks) {
               JavaThread jthread = (JavaThread) proxyToThread.get(th);
               if (jthread != null) {
                   concLocksPrinter.print(jthread, out);
               }
            }
         } 
      } else {
          if (getDebugeeType() == DEBUGEE_REMOTE) {
              out.println("remote configuration is not yet implemented");
          } else {
              out.println("not yet implemented (debugger does not support CDebugger)!");
          }
      }
   }
   protected boolean requiresVM() {
      return false;
   }
   public static void main(String[] args) throws Exception {
      PStack t = new PStack();
      t.start(args);
      t.stop();
   }
   private Map jframeCache; 
   private Map proxyToThread; 
   private PrintStream out;
   private boolean verbose;
   private boolean concurrentLocks;
   private void initJFrameCache() {
      jframeCache = new HashMap();
      proxyToThread = new HashMap();
      Threads threads = VM.getVM().getThreads();
      for (JavaThread cur = threads.first(); cur != null; cur = cur.next()) {
         List tmp = new ArrayList(10);
         try {
            for (JavaVFrame vf = cur.getLastJavaVFrameDbg(); vf != null; vf = vf.javaSender()) {
               tmp.add(vf);
            }
         } catch (Exception exp) {
            exp.printStackTrace();
         }
         JavaVFrame[] jvframes = new JavaVFrame[tmp.size()];
         System.arraycopy(tmp.toArray(), 0, jvframes, 0, jvframes.length);
         jframeCache.put(cur.getThreadProxy(), jvframes);
         proxyToThread.put(cur.getThreadProxy(), cur);
      }
   }
   private void printUnknown(PrintStream out) {
      out.println("\t????????");
   }
   private String[] getJavaNames(ThreadProxy th, Address fp) {
      if (fp == null) {
         return null;
      }
      JavaVFrame[] jvframes = (JavaVFrame[]) jframeCache.get(th);
      if (jvframes == null) return null; 
      List names = new ArrayList(10);
      for (int fCount = 0; fCount < jvframes.length; fCount++) {
         JavaVFrame vf = jvframes[fCount];
         Frame f = vf.getFrame();
         if (fp.equals(f.getFP())) {
            StringBuffer sb = new StringBuffer();
            Method method = vf.getMethod();
            sb.append("* ");
            sb.append(method.externalNameAndSignature());
            sb.append(" bci:" + vf.getBCI());
            int lineNumber = method.getLineNumberFromBCI(vf.getBCI());
            if (lineNumber != -1) {
                sb.append(" line:" + lineNumber);
            }
            if (verbose) {
               sb.append(" methodOop:" + method.getHandle());
            }
            if (vf.isCompiledFrame()) {
               sb.append(" (Compiled frame");
               if (vf.isDeoptimized()) {
                 sb.append(" [deoptimized]");
               }
            } else if (vf.isInterpretedFrame()) {
               sb.append(" (Interpreted frame");
            }
            if (vf.mayBeImpreciseDbg()) {
               sb.append("; information may be imprecise");
            }
            sb.append(")");
            names.add(sb.toString());
         }
      }
      String[] res = new String[names.size()];
      System.arraycopy(names.toArray(), 0, res, 0, res.length);
      return res;
   }
}
