public class
SimpleIdentityTransformer implements ClassFileTransformer {
    public SimpleIdentityTransformer() {
        super();
    }
    public byte[]
    transform(
        ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain    protectionDomain,
        byte[] classfileBuffer) {
        byte[] newBuffer = new byte[classfileBuffer.length];
        System.arraycopy(classfileBuffer, 0, newBuffer, 0, classfileBuffer.length);
        return newBuffer;
    }
}
