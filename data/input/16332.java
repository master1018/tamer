public class TrailingSpaceTest {
    public static void main(String[] args) throws Exception {
        String[] input = {"cn=Tyler\\ ",
                        "cn=Ty ler",
                        "cn=Tyler\\\\  ",
                        "cn=Tyler\\\\\\ ",
                        "cn=   Tyler     ",
                        "cn=Tyler\\\\ \\ ",
                        "cn= ",
                        "cn=  \\     "
                    };
        String[] expected = { "Tyler ",
                                "Ty ler",
                                "Tyler\\",
                                "Tyler\\ ",
                                "Tyler",
                                "Tyler\\  ",
                                "",
                                " "
                            };
        try {
            System.out.println("*************************");
            System.out.println();
            for (int i = 0; i < input.length; i++) {
                Rdn rdn = new Rdn(input[i]);
                System.out.println((i + 1) + ") RDN string: [" +
                                        input[i] + "]");
                Object value = rdn.getValue();
                String escaped = Rdn.escapeValue(value);
                System.out.println("escaped: [" + escaped + "]");
                String unescaped = (String) Rdn.unescapeValue(escaped);
                System.out.println("unescaped: [" + unescaped + "]");
                System.out.println();
                System.out.println("*************************");
                System.out.println();
                if (!unescaped.equals(expected[i])) {
                   throw new Exception("Invalid unescaping for: " +
                                        " input #" + (i + 1));
                }
            }
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
    }
}
