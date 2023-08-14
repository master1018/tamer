public class BidiBug {
  public static void main(String[] args) {
    try {
        byte buff[] = new byte[3000];
        java.text.Bidi bidi = new java.text.Bidi(new char[20],10,buff,Integer.MAX_VALUE-3,4,1);
    }
    catch (IllegalArgumentException e) {
        System.out.println("Passed: " + e);
        return; 
    }
    throw new RuntimeException("Failed: Bidi didn't throw error, though we didn't crash either");
  }
}
