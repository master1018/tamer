public class MsgNative2ascii extends ListResourceBundle {
    public Object[][] getContents() {
        Object[][] temp = new Object[][] {
        {"err.bad.arg", "-encoding requires argument"},
        {"err.cannot.read",  "{0} could not be read."},
        {"err.cannot.write", "{0} could not be written."},
        {"usage", "Usage: native2ascii" +
         " [-reverse] [-encoding encoding] [inputfile [outputfile]]"},
        };
        return temp;
    }
}
