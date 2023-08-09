final class WriterToUTF8Buffered extends Writer implements WriterChain
{
  private static final int BYTES_MAX=16*1024;
  private static final int CHARS_MAX=(BYTES_MAX/3);
  private final OutputStream m_os;
  private final byte m_outputBytes[];
  private final char m_inputChars[];
  private int count;
  public WriterToUTF8Buffered(OutputStream out)
  {
      m_os = out;
      m_outputBytes = new byte[BYTES_MAX + 3];
      m_inputChars = new char[CHARS_MAX + 2];
      count = 0;
  }
  public void write(final int c) throws IOException
  {
    if (count >= BYTES_MAX)
        flushBuffer();
    if (c < 0x80)
    {
       m_outputBytes[count++] = (byte) (c);
    }
    else if (c < 0x800)
    {
      m_outputBytes[count++] = (byte) (0xc0 + (c >> 6));
      m_outputBytes[count++] = (byte) (0x80 + (c & 0x3f));
    }
    else if (c < 0x10000)
    {
      m_outputBytes[count++] = (byte) (0xe0 + (c >> 12));
      m_outputBytes[count++] = (byte) (0x80 + ((c >> 6) & 0x3f));
      m_outputBytes[count++] = (byte) (0x80 + (c & 0x3f));
    }
	else
	{
	  m_outputBytes[count++] = (byte) (0xf0 + (c >> 18));
	  m_outputBytes[count++] = (byte) (0x80 + ((c >> 12) & 0x3f));
	  m_outputBytes[count++] = (byte) (0x80 + ((c >> 6) & 0x3f));
	  m_outputBytes[count++] = (byte) (0x80 + (c & 0x3f));
	}
  }
  public void write(final char chars[], final int start, final int length)
          throws java.io.IOException
  {
    int lengthx3 = 3*length;
    if (lengthx3 >= BYTES_MAX - count)
    {
      flushBuffer();
      if (lengthx3 > BYTES_MAX)
      {
        int split = length/CHARS_MAX; 
        final int chunks;
        if (length % CHARS_MAX > 0) 
            chunks = split + 1;
        else
            chunks = split;
        int end_chunk = start;
        for (int chunk = 1; chunk <= chunks; chunk++)
        {
            int start_chunk = end_chunk;
            end_chunk = start + (int) ((((long) length) * chunk) / chunks);
            final char c = chars[end_chunk - 1]; 
            int ic = chars[end_chunk - 1];
            if (c >= 0xD800 && c <= 0xDBFF) {
                if (end_chunk < start + length) {
                    end_chunk++;
                } else {
                    end_chunk--;
                }
            }
            int len_chunk = (end_chunk - start_chunk);
            this.write(chars,start_chunk, len_chunk);
        }
        return;
      }
    }
    final int n = length+start;
    final byte[] buf_loc = m_outputBytes; 
    int count_loc = count;      
    int i = start;
    {
        char c;
        for(; i < n && (c = chars[i])< 0x80 ; i++ )
            buf_loc[count_loc++] = (byte)c;
    }
    for (; i < n; i++)
    {
      final char c = chars[i];
      if (c < 0x80)
        buf_loc[count_loc++] = (byte) (c);
      else if (c < 0x800)
      {
        buf_loc[count_loc++] = (byte) (0xc0 + (c >> 6));
        buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
      }
      else if (c >= 0xD800 && c <= 0xDBFF) 
      {
          char high, low;
          high = c;
          i++;
          low = chars[i];
          buf_loc[count_loc++] = (byte) (0xF0 | (((high + 0x40) >> 8) & 0xf0));
          buf_loc[count_loc++] = (byte) (0x80 | (((high + 0x40) >> 2) & 0x3f));
          buf_loc[count_loc++] = (byte) (0x80 | ((low >> 6) & 0x0f) + ((high << 4) & 0x30));
          buf_loc[count_loc++] = (byte) (0x80 | (low & 0x3f));
      }
      else
      {
        buf_loc[count_loc++] = (byte) (0xe0 + (c >> 12));
        buf_loc[count_loc++] = (byte) (0x80 + ((c >> 6) & 0x3f));
        buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
      }
    }
    count = count_loc;
  }
  public void write(final String s) throws IOException
  {
    final int length = s.length();
    int lengthx3 = 3*length;
    if (lengthx3 >= BYTES_MAX - count)
    {
      flushBuffer();
      if (lengthx3 > BYTES_MAX)
      {
         final int start = 0;
         int split = length/CHARS_MAX; 
         final int chunks;
         if (length % CHARS_MAX > 0) 
             chunks = split + 1;
         else
             chunks = split;
         int end_chunk = 0;
         for (int chunk = 1; chunk <= chunks; chunk++)
         {
             int start_chunk = end_chunk;
             end_chunk = start + (int) ((((long) length) * chunk) / chunks);
             s.getChars(start_chunk,end_chunk, m_inputChars,0);
             int len_chunk = (end_chunk - start_chunk);
             final char c = m_inputChars[len_chunk - 1];
             if (c >= 0xD800 && c <= 0xDBFF) {
                 end_chunk--;
                 len_chunk--;
                 if (chunk == chunks) {
                 }
             }
             this.write(m_inputChars,0, len_chunk);
         }
         return;
      }
    }
    s.getChars(0, length , m_inputChars, 0);
    final char[] chars = m_inputChars;
    final int n = length;
    final byte[] buf_loc = m_outputBytes; 
    int count_loc = count;      
    int i = 0;
    {
        char c;
        for(; i < n && (c = chars[i])< 0x80 ; i++ )
            buf_loc[count_loc++] = (byte)c;
    }
    for (; i < n; i++)
    {
      final char c = chars[i];
      if (c < 0x80)
        buf_loc[count_loc++] = (byte) (c);
      else if (c < 0x800)
      {
        buf_loc[count_loc++] = (byte) (0xc0 + (c >> 6));
        buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
      }
    else if (c >= 0xD800 && c <= 0xDBFF) 
    {
        char high, low;
        high = c;
        i++;
        low = chars[i];
        buf_loc[count_loc++] = (byte) (0xF0 | (((high + 0x40) >> 8) & 0xf0));
        buf_loc[count_loc++] = (byte) (0x80 | (((high + 0x40) >> 2) & 0x3f));
        buf_loc[count_loc++] = (byte) (0x80 | ((low >> 6) & 0x0f) + ((high << 4) & 0x30));
        buf_loc[count_loc++] = (byte) (0x80 | (low & 0x3f));
    }
      else
      {
        buf_loc[count_loc++] = (byte) (0xe0 + (c >> 12));
        buf_loc[count_loc++] = (byte) (0x80 + ((c >> 6) & 0x3f));
        buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
      }
    }
    count = count_loc;
  }
  public void flushBuffer() throws IOException
  {
    if (count > 0)
    {
      m_os.write(m_outputBytes, 0, count);
      count = 0;
    }
  }
  public void flush() throws java.io.IOException
  {
    flushBuffer();
    m_os.flush();
  }
  public void close() throws java.io.IOException
  {
    flushBuffer();
    m_os.close();
  }
  public OutputStream getOutputStream()
  {
    return m_os;
  }
  public Writer getWriter()
  {
    return null;
  }
}
