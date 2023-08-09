public class HyperRef extends IElement.Factory.ElementImpl
{
    public HyperRef (final String href, final String text, final boolean nbsp)
    {
        super (Tag.A, AttributeSet.create ());
        if ((href == null) || (href.length () == 0))
            throw new IllegalArgumentException ("null or empty input: href");
        if ((text == null) || (text.length () == 0))
            throw new IllegalArgumentException ("null or empty input: text");
        getAttributes ().set (Attribute.HREF, href);
        setText (text, nbsp); 
    }
} 
