public class InvalidCipherTextException 
    extends CryptoException
{
    public InvalidCipherTextException()
    {
    }
    public InvalidCipherTextException(
        String  message)
    {
        super(message);
    }
}
