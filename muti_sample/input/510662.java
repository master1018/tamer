package proguard.classfile.io;
import java.io.*;
final class RuntimeDataOutput
{
    private final DataOutput dataOutput;
    public RuntimeDataOutput(DataOutput dataOutput)
    {
        this.dataOutput = dataOutput;
    }
    public void write(byte[] b)
    {
        try
        {
            dataOutput.write(b);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void write(byte[] b, int off, int len)
    {
        try
        {
            dataOutput.write(b, off, len);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void write(int b)
    {
        try
        {
            dataOutput.write(b);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeBoolean(boolean v)
    {
        try
        {
            dataOutput.writeBoolean(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeByte(int v)
    {
        try
        {
            dataOutput.writeByte(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeBytes(String s)
    {
        try
        {
            dataOutput.writeBytes(s);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeChar(int v)
    {
        try
        {
            dataOutput.writeChar(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeChars(String s)
    {
        try
        {
            dataOutput.writeChars(s);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeDouble(double v)
    {
        try
        {
            dataOutput.writeDouble(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeFloat(float v)
    {
        try
        {
            dataOutput.writeFloat(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeInt(int v)
    {
        try
        {
            dataOutput.writeInt(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeLong(long v)
    {
        try
        {
            dataOutput.writeLong(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeShort(int v)
    {
        try
        {
            dataOutput.writeShort(v);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
    public void writeUTF(String str)
    {
        try
        {
            dataOutput.writeUTF(str);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
