public class UUIDGenerator implements IdentifierGenerator {
    public Serializable generate(SessionImplementor arg0, Object arg1) throws HibernateException {
        UUID uuid = UUID.randomUUID();
        String sud = uuid.toString();
        System.out.println("uuid=" + uuid);
        sud = sud.replaceAll("-", "");
        BigInteger integer = new BigInteger(sud, 16);
        System.out.println("bi =" + integer.toString());
        return integer;
    }
}
