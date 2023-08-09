final class TextAreaOutputStream extends FilterOutputStream implements Runnable
{
    private final JTextArea textArea;
    public TextAreaOutputStream(JTextArea textArea)
    {
        super(new ByteArrayOutputStream());
        this.textArea = textArea;
    }
    public void flush() throws IOException
    {
        super.flush();
        try
        {
            SwingUtil.invokeAndWait(this);
        }
        catch (Exception e)
        {
        }
    }
    public void run()
    {
        ByteArrayOutputStream out = (ByteArrayOutputStream)super.out;
        String text = out.toString();
        if (text.length() > 0)
        {
            textArea.append(text);
            out.reset();
        }
    }
}
