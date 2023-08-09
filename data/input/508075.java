package proguard.classfile.io;
import java.io.*;
final class RuntimeDataInput
{
    private final DataInput dataInput;
    public RuntimeDataInput(DataInput dataInput)
    {
        this.dataInput = dataInput;
    }
    public boolean readBoolean()
    {
        try
        {
            return dataInput.readBoolean();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public byte readByte()
    {
        try
        {
            return dataInput.readByte();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public char readChar()
    {
        try
        {
            return dataInput.readChar();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public double readDouble()
    {
        try
        {
            return dataInput.readDouble();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public float readFloat()
    {
        try
        {
            return dataInput.readFloat();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void readFully(byte[] b)
    {
        try
        {
            dataInput.readFully(b);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void readFully(byte[] b, int off, int len)
    {
        try
        {
            dataInput.readFully(b, off, len);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public int readInt()
    {
        try
        {
            return dataInput.readInt();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public String readLine()
    {
        try
        {
            return dataInput.readLine();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public long readLong()
    {
        try
        {
            return dataInput.readLong();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public short readShort()
    {
        try
        {
            return dataInput.readShort();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public int readUnsignedByte()
    {
        try
        {
            return dataInput.readUnsignedByte();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public int readUnsignedShort()
    {
        try
        {
            return dataInput.readUnsignedShort();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public String readUTF()
    {
        try
        {
            return dataInput.readUTF();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public int skipBytes(int n)
    {
        try
        {
            return dataInput.skipBytes(n);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
