public class IdentityAddressResolver implements org.jbpm.mail.AddressResolver {
    @Override
    public Object resolveAddress(String actorId) {
        EntityManager em = (EntityManager) Component.getInstance("entityManager", true);
        Query q = em.createQuery("from User u where u.userName = :userName");
        q.setParameter("userName", actorId);
        User user = null;
        try {
            user = (User) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        if (user.getEmail().isEmpty()) {
            return null;
        }
        return user.getEmail();
    }
}
