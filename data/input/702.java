class DefaultPackagePublicConstructor {
    public DefaultPackagePublicConstructor() {
    }
};
class DefaultPackageProtectedConstructor {
    protected DefaultPackageProtectedConstructor() {
    }
};
class DefaultPackageDefaultAccessConstructor {
    DefaultPackageDefaultAccessConstructor() {
    }
};
class DefaultPackagePrivateConstructor {
    private DefaultPackagePrivateConstructor() {
    }
    protected DefaultPackagePrivateConstructor(int i) {
    }
};
class DefaultPublicSerializable
extends DefaultPackagePublicConstructor implements Serializable
{
    int field1 = 5;
};
class DefaultProtectedSerializable
extends DefaultPackageProtectedConstructor implements Serializable
{
    int field1 = 5;
};
class DefaultAccessSerializable
extends DefaultPackageDefaultAccessConstructor implements Serializable
{
    int field1 = 5;
};
class DefaultPrivateSerializable
extends DefaultPackagePrivateConstructor implements Serializable
{
    int field1 = 5;
    DefaultPrivateSerializable() {
        super(1);
    }
};
class ExternalizablePublicConstructor implements Externalizable {
    public ExternalizablePublicConstructor() {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
class ExternalizableProtectedConstructor implements Externalizable {
    protected ExternalizableProtectedConstructor() {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
class ExternalizableAccessConstructor implements Externalizable {
    ExternalizableAccessConstructor() {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
class ExternalizablePrivateConstructor implements Externalizable {
    private ExternalizablePrivateConstructor() {
    }
    public ExternalizablePrivateConstructor(int i) {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
public class DefaultPackage {
    public static void main(String args[])
        throws IOException, ClassNotFoundException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out =   new ObjectOutputStream(baos);
            out.writeObject(new DefaultPublicSerializable());
            out.writeObject(new DefaultProtectedSerializable());
            out.writeObject(new DefaultAccessSerializable());
            out.writeObject(new DefaultPrivateSerializable());
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(is);
             in.readObject();
             in.readObject();
             in.readObject();
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading DefaultPrivateSerialziable");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizablePublicConstructor());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
             in.readObject();
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizableProtectedConstructor());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading ExternalizableProtectedConstructor");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizableAccessConstructor());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading ExternalizableAccessConstructor");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizablePrivateConstructor(2));
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading ExternalizablePrivateConstructor");
            } catch (InvalidClassException e) {
            }
            out.close();
            in.close();
        }
}
