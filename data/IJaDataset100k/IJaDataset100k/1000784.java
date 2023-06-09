package gnu.java.security.key.dss;

import gnu.java.security.Configuration;
import gnu.java.security.Registry;
import gnu.java.security.action.GetPropertyAction;
import gnu.java.security.key.IKeyPairCodec;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivateKey;
import java.security.interfaces.DSAPrivateKey;

/**
 * An object that embodies a DSS (Digital Signature Standard) private key.
 * 
 * @see #getEncoded
 */
public class DSSPrivateKey extends DSSKey implements PrivateKey, DSAPrivateKey {

    /**
   * A randomly or pseudorandomly generated integer with <code>0 &lt; x &lt;
   * q</code>.
   */
    private final BigInteger x;

    /** String representation of this key. Cached for speed. */
    private transient String str;

    /**
   * Convenience constructor. Calls the constructor with 5 arguments passing
   * {@link Registry#RAW_ENCODING_ID} as the identifier of the preferred
   * encoding format.
   * 
   * @param p the public modulus.
   * @param q the public prime divisor of <code>p-1</code>.
   * @param g a generator of the unique cyclic group <code>Z<sup>*</sup>
   *          <sub>p</sub></code>.
   * @param x the private key part.
   */
    public DSSPrivateKey(BigInteger p, BigInteger q, BigInteger g, BigInteger x) {
        this(Registry.RAW_ENCODING_ID, p, q, g, x);
    }

    /**
   * Constructs a new instance of a <code>DSSPrivateKey</code> given the
   * designated arguments.
   * 
   * @param preferredFormat the indetifier of the preferred encoding format to
   *          use when externalizing this key.
   * @param p the public modulus.
   * @param q the public prime divisor of <code>p-1</code>.
   * @param g a generator of the unique cyclic group <code>Z<sup>*</sup>
   *          <sub>p</sub></code>.
   * @param x the private key part.
   */
    public DSSPrivateKey(int preferredFormat, BigInteger p, BigInteger q, BigInteger g, BigInteger x) {
        super(preferredFormat == Registry.ASN1_ENCODING_ID ? Registry.PKCS8_ENCODING_ID : preferredFormat, p, q, g);
        this.x = x;
    }

    /**
   * A class method that takes the output of the <code>encodePrivateKey()</code>
   * method of a DSS keypair codec object (an instance implementing
   * {@link gnu.java.security.key.IKeyPairCodec} for DSS keys, and re-constructs
   * an instance of this object.
   * 
   * @param k the contents of a previously encoded instance of this object.
   * @exception ArrayIndexOutOfBoundsException if there is not enough bytes, in
   *              <code>k</code>, to represent a valid encoding of an
   *              instance of this object.
   * @exception IllegalArgumentException if the byte sequence does not represent
   *              a valid encoding of an instance of this object.
   */
    public static DSSPrivateKey valueOf(byte[] k) {
        if (k[0] == Registry.MAGIC_RAW_DSS_PRIVATE_KEY[0]) try {
            return (DSSPrivateKey) new DSSKeyPairRawCodec().decodePrivateKey(k);
        } catch (IllegalArgumentException ignored) {
        }
        return (DSSPrivateKey) new DSSKeyPairPKCS8Codec().decodePrivateKey(k);
    }

    public BigInteger getX() {
        return x;
    }

    /**
   * Returns the encoded form of this private key according to the designated
   * format.
   * 
   * @param format the desired format identifier of the resulting encoding.
   * @return the byte sequence encoding this key according to the designated
   *         format.
   * @exception IllegalArgumentException if the format is not supported.
   * @see DSSKeyPairRawCodec
   */
    public byte[] getEncoded(int format) {
        byte[] result;
        switch(format) {
            case IKeyPairCodec.RAW_FORMAT:
                result = new DSSKeyPairRawCodec().encodePrivateKey(this);
                break;
            case IKeyPairCodec.PKCS8_FORMAT:
                result = new DSSKeyPairPKCS8Codec().encodePrivateKey(this);
                break;
            default:
                throw new IllegalArgumentException("Unsupported encoding format: " + format);
        }
        return result;
    }

    /**
   * Returns <code>true</code> if the designated object is an instance of
   * {@link DSAPrivateKey} and has the same DSS (Digital Signature Standard)
   * parameter values as this one.
   * 
   * @param obj the other non-null DSS key to compare to.
   * @return <code>true</code> if the designated object is of the same type
   *         and value as this one.
   */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof DSAPrivateKey)) return false;
        DSAPrivateKey that = (DSAPrivateKey) obj;
        return super.equals(that) && x.equals(that.getX());
    }

    public String toString() {
        if (str == null) {
            String ls = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
            str = new StringBuilder(this.getClass().getName()).append("(").append(super.toString()).append(",").append(ls).append("x=0x").append(Configuration.DEBUG ? x.toString(16) : "**...*").append(ls).append(")").toString();
        }
        return str;
    }
}
