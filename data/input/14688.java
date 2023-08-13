public final class Test4625418 implements ExceptionListener {
    public static void main(String[] args) {
        new Test4625418(args[0]).test(createString(0x10000));
        System.out.println("Test passed: " + args[0]);
    }
    private static String createString(int length) {
        StringBuilder sb = new StringBuilder(length);
        while (0 < length--)
            sb.append((char) length);
        return sb.toString();
    }
    private final String encoding;
    private Test4625418(String encoding) {
        this.encoding = encoding;
    }
    private void test(String string) {
        try {
            File file = new File("4625418." + this.encoding + ".xml");
            FileOutputStream output = new FileOutputStream(file);
            XMLEncoder encoder = new XMLEncoder(output, this.encoding, true, 0);
            encoder.setExceptionListener(this);
            encoder.writeObject(string);
            encoder.close();
            FileInputStream input = new FileInputStream(file);
            XMLDecoder decoder = new XMLDecoder(input);
            decoder.setExceptionListener(this);
            Object object = decoder.readObject();
            decoder.close();
            if (!string.equals(object))
                throw new Error(this.encoding + " - can't read properly");
            file.delete();
        }
        catch (FileNotFoundException exception) {
            throw new Error(this.encoding + " - file not found", exception);
        }
        catch (IllegalCharsetNameException exception) {
            throw new Error(this.encoding + " - illegal charset name", exception);
        }
        catch (UnsupportedCharsetException exception) {
            throw new Error(this.encoding + " - unsupported charset", exception);
        }
        catch (UnsupportedOperationException exception) {
            throw new Error(this.encoding + " - unsupported encoder", exception);
        }
    }
    public void exceptionThrown(Exception exception) {
        throw new Error(this.encoding + " - internal", exception);
    }
}
