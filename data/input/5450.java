class PublicSerializable
extends NonSerializable.PublicCtor implements Serializable
{
    int field1 = 5;
};
class ProtectedSerializable
extends NonSerializable.ProtectedCtor implements Serializable
{
    int field1 = 5;
};
class DifferentPackageSerializable
extends NonSerializable.PackageCtor implements Serializable
{
    int field1 = 5;
    DifferentPackageSerializable() {
        super(1);
    }
};
class SamePackageSerializable
extends Serialize.SamePackageCtor implements Serializable
{
    SamePackageSerializable() {
    }
};
class SamePackageProtectedCtor {
    protected SamePackageProtectedCtor() {
    }
};
class SamePackageProtectedSerializable
extends Serialize.SamePackageProtectedCtor implements Serializable
{
    SamePackageProtectedSerializable() {
    }
};
class SamePackagePrivateCtor {
    private SamePackagePrivateCtor() {
    }
    public SamePackagePrivateCtor(int l) {
    }
};
class SamePackagePrivateSerializable
extends Serialize.SamePackagePrivateCtor implements Serializable
{
    SamePackagePrivateSerializable() {
        super(1);
    }
};
class PrivateSerializable
extends NonSerializable.PrivateCtor implements Serializable
{
    int field1 = 5;
    PrivateSerializable() {
        super(1);
    }
};
class ExternalizablePublicCtor implements Externalizable {
    public ExternalizablePublicCtor() {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
class ExternalizableProtectedCtor implements Externalizable {
    protected ExternalizableProtectedCtor() {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
class ExternalizablePackageCtor implements Externalizable {
    ExternalizablePackageCtor() {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
class ExternalizablePrivateCtor implements Externalizable {
    private ExternalizablePrivateCtor() {
    }
    public ExternalizablePrivateCtor(int i) {
    }
    public void writeExternal(ObjectOutput out) throws IOException {
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
        {
        }
};
public class SubclassAcrossPackage {
    public static void main(String args[])
        throws IOException, ClassNotFoundException
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream out =   new ObjectOutputStream(baos);
            out.writeObject(new PublicSerializable());
            out.writeObject(new ProtectedSerializable());
            out.writeObject(new SamePackageSerializable());
            out.writeObject(new SamePackageProtectedSerializable());
            out.writeObject(new DifferentPackageSerializable());
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(is);
             in.readObject();
             in.readObject();
             in.readObject();
             in.readObject();
            try {
             in.readObject();
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new PrivateSerializable());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading PrivateSerialziable");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new SamePackagePrivateSerializable());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading PrivateSerialziable");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizablePublicCtor());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
             in.readObject();
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizableProtectedCtor());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading ExternalizableProtectedCtor");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizablePackageCtor());
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading ExternalizablePackageCtor");
            } catch (InvalidClassException e) {
            }
            in.close();
            baos.reset();
            out = new ObjectOutputStream(baos);
            out.writeObject(new ExternalizablePrivateCtor(2));
            in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            try {
                 in.readObject();
                throw new Error("Expected InvalidClassException reading ExternalizablePrivateCtor");
            } catch (InvalidClassException e) {
            }
            out.close();
            in.close();
        }
}
