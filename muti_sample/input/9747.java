public class CompactibleFreeListSpace extends CompactibleSpace {
   private static AddressField collectorField;
   private static AddressField indexedFreeListField;
   private static AddressField dictionaryField;
   private static long         smallLinearAllocBlockFieldOffset;
   private static long indexedFreeListSizeOf;
   private int    heapWordSize;     
   private int    IndexSetStart;    
   private int    IndexSetSize;
   private int    IndexSetStride;
   static {
      VM.registerVMInitializedObserver(new Observer() {
         public void update(Observable o, Object data) {
            initialize(VM.getVM().getTypeDataBase());
         }
      });
   }
   private static synchronized void initialize(TypeDataBase db) {
      long sizeofFreeChunk = db.lookupType("FreeChunk").getSize();
      VM vm = VM.getVM();
      MinChunkSizeInBytes = numQuanta(sizeofFreeChunk, vm.getMinObjAlignmentInBytes()) *
                     vm.getMinObjAlignmentInBytes();
     Type type = db.lookupType("CompactibleFreeListSpace");
     collectorField = type.getAddressField("_collector");
     collectorField       = type.getAddressField("_collector");
     dictionaryField      = type.getAddressField("_dictionary");
     indexedFreeListField = type.getAddressField("_indexedFreeList[0]");
     smallLinearAllocBlockFieldOffset = type.getField("_smallLinearAllocBlock").getOffset();
   }
   public CompactibleFreeListSpace(Address addr) {
      super(addr);
      VM vm = VM.getVM();
      heapWordSize   = vm.getHeapWordSize();
      IndexSetStart  = vm.getMinObjAlignmentInBytes() / heapWordSize;
      IndexSetStride = IndexSetStart;
      IndexSetSize   = 257;
   }
   public CMSCollector collector() {
    return (CMSCollector) VMObjectFactory.newObject(
                                 CMSCollector.class,
                                 collectorField.getValue(addr));
   }
   public long free0() {
     return capacity() - used0();
   }
   public long used() {
     return capacity() - free();
   }
   public long used0() {
      List regions = getLiveRegions();
      long usedSize = 0L;
      for (Iterator itr = regions.iterator(); itr.hasNext();) {
         MemRegion mr = (MemRegion) itr.next();
         usedSize += mr.byteSize();
      }
      return usedSize;
   }
   public long free() {
      long size = 0;
      Address cur = addr.addOffsetTo( indexedFreeListField.getOffset() );
      cur = cur.addOffsetTo(IndexSetStart*FreeList.sizeOf());
      for (int i=IndexSetStart; i<IndexSetSize; i += IndexSetStride) {
         FreeList freeList = (FreeList) VMObjectFactory.newObject(FreeList.class, cur);
         size += i*freeList.count();
         cur= cur.addOffsetTo(IndexSetStride*FreeList.sizeOf());
      }
      BinaryTreeDictionary bfbd = (BinaryTreeDictionary) VMObjectFactory.newObject(BinaryTreeDictionary.class,
                                                                                   dictionaryField.getValue(addr));
      size += bfbd.size();
      LinearAllocBlock lab = (LinearAllocBlock) VMObjectFactory.newObject(LinearAllocBlock.class,
                                                                          addr.addOffsetTo(smallLinearAllocBlockFieldOffset));
      size += lab.word_size();
      return size*heapWordSize;
  }
   public void printOn(PrintStream tty) {
      tty.print("free-list-space");
      tty.print("[ " + bottom() + " , " + end() + " ) ");
      long cap = capacity();
      long used_size = used();
      long free_size = free();
      int  used_perc = (int)((double)used_size/cap*100);
      tty.print("space capacity = " + cap + " used(" + used_perc + "%)= " + used_size + " ");
      tty.print("free= " + free_size );
      tty.print("\n");
   }
   public Address skipBlockSizeUsingPrintezisBits(Address pos) {
       CMSCollector collector = collector();
       long size = 0;
       Address addr = null;
       if (collector != null) {
         size = collector.blockSizeUsingPrintezisBits(pos);
         if (size >= 3) {
           addr = pos.addOffsetTo(adjustObjectSizeInBytes(size));
         }
       }
       return addr;
   }
   public List getLiveRegions() {
      List res = new ArrayList(); 
      VM vm = VM.getVM();
      Debugger dbg = vm.getDebugger();
      ObjectHeap heap = vm.getObjectHeap();
      Address cur = bottom();
      Address regionStart = cur;
      Address limit = end();
      final long addressSize = vm.getAddressSize();
      for (; cur.lessThan(limit);) {
         Address klassOop = cur.getAddressAt(addressSize);
         if (FreeChunk.indicatesFreeChunk(cur)) {
            if (! cur.equals(regionStart)) {
               res.add(new MemRegion(regionStart, cur));
            }
            FreeChunk fc = (FreeChunk) VMObjectFactory.newObject(FreeChunk.class, cur);
            long chunkSize = fc.size();
            if (Assert.ASSERTS_ENABLED) {
               Assert.that(chunkSize > 0, "invalid FreeChunk size");
            }
            cur = cur.addOffsetTo(chunkSize * addressSize);
            regionStart = cur;
         } else if (klassOop != null) {
            Oop obj = heap.newOop(cur.addOffsetToAsOopHandle(0));
            long objectSize = obj.getObjectSize();
            cur = cur.addOffsetTo(adjustObjectSizeInBytes(objectSize));
         } else {
            long size = collector().blockSizeUsingPrintezisBits(cur);
            if (size == -1) {
              System.err.println("Printezis bits not set...");
              break;
            }
            cur = cur.addOffsetTo(adjustObjectSizeInBytes(size));
         }
      }
      return res;
   }
   private static long numQuanta(long x, long y) {
      return  ((x+y-1)/y);
   }
   public static long adjustObjectSizeInBytes(long sizeInBytes) {
      return Oop.alignObjectSize(Math.max(sizeInBytes, MinChunkSizeInBytes));
   }
   private static long MinChunkSizeInBytes;
}
