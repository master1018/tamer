public class test {
    public int getPass1FieldNos(int[] buf) {
        throw BindingSupportImpl.getInstance().invalidOperation("Not allowed to read/write to a instance marked for deletion");
    }
}
