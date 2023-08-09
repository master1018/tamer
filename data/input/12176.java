public class HeapSummary extends Tool {
   public static void main(String[] args) {
      HeapSummary hs = new HeapSummary();
      hs.start(args);
      hs.stop();
   }
   public void run() {
      CollectedHeap heap = VM.getVM().getUniverse().heap();
      VM.Flag[] flags = VM.getVM().getCommandLineFlags();
      Map flagMap = new HashMap();
      if (flags == null) {
         System.out.println("WARNING: command line flags are not available");
      } else {
         for (int f = 0; f < flags.length; f++) {
            flagMap.put(flags[f].getName(), flags[f]);
         }
      }
      System.out.println();
      printGCAlgorithm(flagMap);
      System.out.println();
      System.out.println("Heap Configuration:");
      printValue("MinHeapFreeRatio = ", getFlagValue("MinHeapFreeRatio", flagMap));
      printValue("MaxHeapFreeRatio = ", getFlagValue("MaxHeapFreeRatio", flagMap));
      printValMB("MaxHeapSize      = ", getFlagValue("MaxHeapSize", flagMap));
      printValMB("NewSize          = ", getFlagValue("NewSize", flagMap));
      printValMB("MaxNewSize       = ", getFlagValue("MaxNewSize", flagMap));
      printValMB("OldSize          = ", getFlagValue("OldSize", flagMap));
      printValue("NewRatio         = ", getFlagValue("NewRatio", flagMap));
      printValue("SurvivorRatio    = ", getFlagValue("SurvivorRatio", flagMap));
      printValMB("PermSize         = ", getFlagValue("PermSize", flagMap));
      printValMB("MaxPermSize      = ", getFlagValue("MaxPermSize", flagMap));
      System.out.println();
      System.out.println("Heap Usage:");
      if (heap instanceof GenCollectedHeap) {
         GenCollectedHeap genHeap = (GenCollectedHeap) heap;
         for (int n = 0; n < genHeap.nGens(); n++) {
            Generation gen = genHeap.getGen(n);
            if (gen instanceof sun.jvm.hotspot.memory.DefNewGeneration) {
               System.out.println("New Generation (Eden + 1 Survivor Space):");
               printGen(gen);
               ContiguousSpace eden = ((DefNewGeneration)gen).eden();
               System.out.println("Eden Space:");
               printSpace(eden);
               ContiguousSpace from = ((DefNewGeneration)gen).from();
               System.out.println("From Space:");
               printSpace(from);
               ContiguousSpace to = ((DefNewGeneration)gen).to();
               System.out.println("To Space:");
               printSpace(to);
            } else {
               System.out.println(gen.name() + ":");
               printGen(gen);
            }
         }
         Generation permGen = genHeap.permGen();
         System.out.println("Perm Generation:");
         printGen(permGen);
      } else if (heap instanceof ParallelScavengeHeap) {
         ParallelScavengeHeap psh = (ParallelScavengeHeap) heap;
         PSYoungGen youngGen = psh.youngGen();
         printPSYoungGen(youngGen);
         PSOldGen oldGen = psh.oldGen();
         long oldFree = oldGen.capacity() - oldGen.used();
         System.out.println("PS Old Generation");
         printValMB("capacity = ", oldGen.capacity());
         printValMB("used     = ", oldGen.used());
         printValMB("free     = ", oldFree);
         System.out.println(alignment + (double)oldGen.used() * 100.0 / oldGen.capacity() + "% used");
         PSPermGen permGen = psh.permGen();
         long permFree = permGen.capacity() - permGen.used();
         System.out.println("PS Perm Generation");
         printValMB("capacity = ", permGen.capacity());
         printValMB("used     = ", permGen.used());
         printValMB("free     = ", permFree);
         System.out.println(alignment + (double)permGen.used() * 100.0 / permGen.capacity() + "% used");
      } else {
         throw new RuntimeException("unknown heap type : " + heap.getClass());
      }
   }
   private void printGCAlgorithm(Map flagMap) {
       long l = getFlagValue("UseParNewGC", flagMap);
       if (l == 1L) {
          System.out.println("using parallel threads in the new generation.");
       }
       l = getFlagValue("UseTLAB", flagMap);
       if (l == 1L) {
          System.out.println("using thread-local object allocation.");
       }
       l = getFlagValue("UseConcMarkSweepGC", flagMap);
       if (l == 1L) {
          System.out.println("Concurrent Mark-Sweep GC");
          return;
       }
       l = getFlagValue("UseParallelGC", flagMap);
       if (l == 1L) {
          System.out.print("Parallel GC ");
          l = getFlagValue("ParallelGCThreads", flagMap);
          System.out.println("with " + l + " thread(s)");
          return;
       }
       System.out.println("Mark Sweep Compact GC");
   }
   private void printPSYoungGen(PSYoungGen youngGen) {
      System.out.println("PS Young Generation");
      MutableSpace eden = youngGen.edenSpace();
      System.out.println("Eden Space:");
      printMutableSpace(eden);
      MutableSpace from = youngGen.fromSpace();
      System.out.println("From Space:");
      printMutableSpace(from);
      MutableSpace to = youngGen.toSpace();
      System.out.println("To Space:");
      printMutableSpace(to);
   }
   private void printMutableSpace(MutableSpace space) {
      printValMB("capacity = ", space.capacity());
      printValMB("used     = ", space.used());
      long free = space.capacity() - space.used();
      printValMB("free     = ", free);
      System.out.println(alignment + (double)space.used() * 100.0 / space.capacity() + "% used");
   }
   private static String alignment = "   ";
   private void printGen(Generation gen) {
      printValMB("capacity = ", gen.capacity());
      printValMB("used     = ", gen.used());
      printValMB("free     = ", gen.free());
      System.out.println(alignment + (double)gen.used() * 100.0 / gen.capacity() + "% used");
   }
   private void printSpace(ContiguousSpace space) {
      printValMB("capacity = ", space.capacity());
      printValMB("used     = ", space.used());
      printValMB("free     = ", space.free());
      System.out.println(alignment +  (double)space.used() * 100.0 / space.capacity() + "% used");
   }
   private static final double FACTOR = 1024*1024;
   private void printValMB(String title, long value) {
      if (value < 0) {
        System.out.println(alignment + title +   (value >>> 20)  + " MB");
      } else {
        double mb = value/FACTOR;
        System.out.println(alignment + title + value + " (" + mb + "MB)");
      }
   }
   private void printValue(String title, long value) {
      System.out.println(alignment + title + value);
   }
   private long getFlagValue(String name, Map flagMap) {
      VM.Flag f = (VM.Flag) flagMap.get(name);
      if (f != null) {
         if (f.isBool()) {
            return f.getBool()? 1L : 0L;
         } else {
            return Long.parseLong(f.getValue());
         }
      } else {
         return -1;
      }
   }
}
