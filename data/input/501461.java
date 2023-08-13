public abstract class FormatConversionProvider {
    public abstract AudioInputStream getAudioInputStream(
            AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream);
    public abstract AudioInputStream getAudioInputStream(
            AudioFormat targetFormat, AudioInputStream sourceStream);
    public abstract AudioFormat.Encoding[] getTargetEncodings(
            AudioFormat sourceFormat);
    public boolean isConversionSupported(AudioFormat.Encoding targetEncoding,
            AudioFormat sourceFormat) {
        AudioFormat.Encoding[] encodings = getTargetEncodings(sourceFormat);
        for (Encoding element : encodings) {
            if (targetEncoding.equals(element)) {
                return true;
            }
        }
        return false;
    }
    public abstract AudioFormat[] getTargetFormats(
            AudioFormat.Encoding targetFormat, AudioFormat sourceFormat);
    public boolean isConversionSupported(AudioFormat targetFormat,
            AudioFormat sourceFormat) {
        AudioFormat[] formats = getTargetFormats(targetFormat.getEncoding(),
                sourceFormat);
        for (AudioFormat element : formats) {
            if (targetFormat.equals(element)) {
                return true;
            }
        }
        return false;
    }
    public abstract AudioFormat.Encoding[] getSourceEncodings();
    public boolean isSourceEncodingSupported(AudioFormat.Encoding sourceEncoding) {
        AudioFormat.Encoding[] encodings = getSourceEncodings();
        for (Encoding element : encodings) {
            if (sourceEncoding.equals(element)) {
                return true;
            }
        }
        return false;
    }
    public abstract AudioFormat.Encoding[] getTargetEncodings();
    public boolean isTargetEncodingSupported(AudioFormat.Encoding targetEncoding) {
        AudioFormat.Encoding[] encodings = getTargetEncodings();
        for (Encoding element : encodings) {
            if (targetEncoding.equals(element)) {
                return true;
            }
        }
        return false;
    }
}
