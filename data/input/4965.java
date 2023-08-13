public class Factory {
    public static void main(String[] args) throws Exception {
        if (Boolean.valueOf(true) != Boolean.TRUE)
            throw new Exception("Truth failure");
        if (Boolean.valueOf(false) != Boolean.FALSE)
            throw new Exception("Major fallacy");
    }
}
