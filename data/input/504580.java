public class ToStringExternalizer implements IApiDeltaExternalizer {
    public void externalize(String location, IApiDelta delta)
            throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                new FileOutputStream(location));
        if (delta == null) {
            outputStreamWriter.write("No delta found!");
        } else {
            outputStreamWriter.write(delta.toString());
        }
        outputStreamWriter.close();
    }
}
