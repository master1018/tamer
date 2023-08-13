public class InternalBindingKey
{
    public NameComponent name;
    private int idLen;
    private int kindLen;
    private int hashVal;
    public InternalBindingKey() {}
    public InternalBindingKey(NameComponent n)
    {
        idLen = 0;
        kindLen = 0;
        setup(n);
    }
    protected void setup(NameComponent n) {
        this.name = n;
        if( this.name.id != null ) {
            idLen = this.name.id.length();
        }
        if( this.name.kind != null ) {
            kindLen = this.name.kind.length();
        }
        hashVal = 0;
        if (idLen > 0)
            hashVal += this.name.id.hashCode();
        if (kindLen > 0)
            hashVal += this.name.kind.hashCode();
    }
    public boolean equals(java.lang.Object o) {
        if (o == null)
            return false;
        if (o instanceof InternalBindingKey) {
            InternalBindingKey that = (InternalBindingKey)o;
            if (this.idLen != that.idLen || this.kindLen != that.kindLen) {
                return false;
            }
            if (this.idLen > 0 && this.name.id.equals(that.name.id) == false) {
                return false;
            }
            if (this.kindLen > 0 && this.name.kind.equals(that.name.kind) == false) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
    public int hashCode() {
        return this.hashVal;
    }
}
