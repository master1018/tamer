public class test {
    public void returnState(byte[] state) {
        mux.getChannel().returnState(state, id);
    }
}
