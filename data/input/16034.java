public class CaseConvertSameInstance {
    public static void main(String[] args) throws Exception {
        if ("foobar".toLowerCase() != "foobar")
            throw new Exception("toLowerCase returned different object");
        if ("FOOBAR".toUpperCase() != "FOOBAR")
            throw new Exception("toUpperCase returned different object");
        if (!("FooBar".toLowerCase().equals("foobar")))
            throw new Exception("toLowerCase broken");
        if (!("fooBar".toLowerCase().equals("foobar")))
            throw new Exception("toLowerCase broken");
        if (!("foobaR".toLowerCase().equals("foobar")))
            throw new Exception("toLowerCase broken");
        if (!("FOOBAR".toLowerCase().equals("foobar")))
            throw new Exception("toLowerCase broken");
        if (!("FooBar".toUpperCase().equals("FOOBAR")))
            throw new Exception("toUpperCase broken");
        if (!("fooBar".toUpperCase().equals("FOOBAR")))
            throw new Exception("toUpperCase broken");
        if (!("foobaR".toUpperCase().equals("FOOBAR")))
            throw new Exception("toUpperCase broken");
        if (!("foobar".toUpperCase().equals("FOOBAR")))
            throw new Exception("toUpperCase broken");
    }
}
