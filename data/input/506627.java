import proguard.classfile.*;
import java.io.*;
public class ManifestRewriter extends DataEntryRewriter
{
    public ManifestRewriter(ClassPool       classPool,
                            DataEntryWriter dataEntryWriter)
    {
        super(classPool, dataEntryWriter);
    }
    protected void copyData(Reader reader,
                            Writer writer)
    throws IOException
    {
        super.copyData(new SplitLineReader(reader),
                       new SplitLineWriter(writer));
    }
    private static class SplitLineReader extends FilterReader
    {
        private char[] buffer      = new char[2];
        private int    bufferIndex = 0;
        private int    bufferSize  = 0;
        public SplitLineReader(Reader reader)
        {
            super(reader);
        }
        public int read() throws IOException
        {
            while (true)
            {
                if (bufferIndex < bufferSize)
                {
                    return buffer[bufferIndex++];
                }
                int c1 = super.read();
                if (c1 != '\n' && c1 != '\r')
                {
                    return c1;
                }
                bufferIndex = 0;
                bufferSize  = 0;
                buffer[bufferSize++] = '\n';
                int c2 = super.read();
                if (c2 == ' ')
                {
                    bufferSize = 0;
                    continue;
                }
                if (c1 != '\r' || c2 != '\n')
                {
                    buffer[bufferSize++] = (char)c2;
                    continue;
                }
                int c3 = super.read();
                if (c3 == ' ')
                {
                    bufferSize = 0;
                    continue;
                }
                buffer[bufferSize++] = (char)c3;
            }
        }
        public int read(char[] cbuf, int off, int len) throws IOException
        {
            int count = 0;
            while (count < len)
            {
                int c = read();
                if (c == -1)
                {
                    break;
                }
                cbuf[off + count++] = (char)c;
            }
            return count;
        }
        public long skip(long n) throws IOException
        {
            int count = 0;
            while (count < n)
            {
                int c = read();
                if (c == -1)
                {
                    break;
                }
                count++;
            }
            return count;
        }
    }
    private static class SplitLineWriter extends FilterWriter
    {
        private int counter = 0;
        public SplitLineWriter(Writer writer)
        {
            super(writer);
        }
        public void write(int c) throws IOException
        {
            if (c == '\n')
            {
                counter = 0;
            }
            else if (counter == 70)
            {
                super.write('\n');
                super.write(' ');
                counter = 2;
            }
            else
            {
                counter++;
            }
            super.write(c);
        }
        public void write(char[] cbuf, int off, int len) throws IOException
        {
            for (int count = 0; count < len; count++)
            {
                write(cbuf[off + count]);
            }
        }
        public void write(String str, int off, int len) throws IOException
        {
            write(str.toCharArray(), off, len);
        }
    }
}