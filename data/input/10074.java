public class Bug6850113 {
    public static void main(String[] args) {
        boolean err = false;
        for (int i = 0xff21, j = 10; i <= 0xff3a; i++, j++) {
            if (UCharacter.digit(i, 36) != j) {
                err = true;
                System.out.println("Error: UCharacter.digit(0x" +
                    Integer.toHexString(i) + ", 36) returned " +
                    UCharacter.digit(i, 36) + ", expected=" + j);
            }
        }
        for (int i = 0xff41, j = 10; i <= 0xff5a; i++, j++) {
            if (UCharacter.digit(i, 36) != j) {
                err = true;
                System.out.println("Error: UCharacter.digit(0x" +
                    Integer.toHexString(i) + ", 36) returned " +
                    UCharacter.digit(i, 36) + ", expected=" + j);
            }
        }
        if (err) {
            throw new RuntimeException("UCharacter.digit():  Wrong return value");
        }
   }
}
