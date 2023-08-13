public abstract class IdentifiableBase implements Identifiable,
    WriteContents
{
    final public void write( OutputStream os )
    {
        EncapsulationUtility.writeEncapsulation( (WriteContents)this, os ) ;
    }
}
