public abstract class BaseIndexScaleDispAddress extends IndirectAddress {
   private final Register base, index;
   private final int      scale;
   private final long     disp;
   private boolean  isAutoIncr;
   private boolean  isAutoDecr;
   public BaseIndexScaleDispAddress(Register base, Register index, long disp, int scale) {
      this.base = base;
      this.index = index;
      this.disp = disp;
      this.scale = scale;
   }
   public BaseIndexScaleDispAddress(Register base, Register index, long disp) {
      this(base, index, disp, 1);
   }
   public BaseIndexScaleDispAddress(Register base, Register index) {
      this(base, index, 0L, 1);
   }
   public BaseIndexScaleDispAddress(Register base, long disp) {
      this(base, null, disp, 1);
   }
   public Register getBase() {
      return base;
   }
   public Register getIndex() {
      return index;
   }
   public int      getScale() {
      return scale;
   }
   public long     getDisplacement() {
      return disp;
   }
   public boolean  isAutoIncrement() {
      return isAutoIncr;
   }
   public void setAutoIncrement(boolean value) {
      isAutoIncr = value;
   }
   public boolean  isAutoDecrement() {
      return isAutoDecr;
   }
   public void setAutoDecrement(boolean value) {
      isAutoDecr = value;
   }
}
