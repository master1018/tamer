public class X86RegisterPart extends X86Register {
   private int startBit;
   private int length;
   public X86RegisterPart(int num, String name, int startBit, int length) {
      super(num, name);
      this.startBit = startBit;
      this.length = length;
   }
   public boolean is32BitRegister() {
      return ( length == 32);
   }
   public boolean is16BitRegister() {
      return ( length == 16);
   }
   public boolean is8BitRegister() {
      return ( length == 8);
   }
}
