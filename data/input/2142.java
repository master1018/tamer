public class RoleList extends ArrayList<Object> {
    private transient boolean typeSafe;
    private transient boolean tainted;
    private static final long serialVersionUID = 5568344346499649313L;
    public RoleList() {
        super();
    }
    public RoleList(int initialCapacity) {
        super(initialCapacity);
    }
    public RoleList(List<Role> list) throws IllegalArgumentException {
        if (list == null)
            throw new IllegalArgumentException("Null parameter");
        checkTypeSafe(list);
        super.addAll(list);
    }
    @SuppressWarnings("unchecked")
    public List<Role> asList() {
        if (!typeSafe) {
            if (tainted)
                checkTypeSafe(this);
            typeSafe = true;
        }
        return Util.cast(this);
    }
    public void add(Role role)
        throws IllegalArgumentException {
        if (role == null) {
            String excMsg = "Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        super.add(role);
    }
    public void add(int index,
                    Role role)
        throws IllegalArgumentException,
               IndexOutOfBoundsException {
        if (role == null) {
            String excMsg = "Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        super.add(index, role);
    }
     public void set(int index,
                     Role role)
         throws IllegalArgumentException,
                IndexOutOfBoundsException {
        if (role == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        super.set(index, role);
     }
    public boolean addAll(RoleList roleList)
        throws IndexOutOfBoundsException {
        if (roleList == null) {
            return true;
        }
        return (super.addAll(roleList));
    }
    public boolean addAll(int index,
                          RoleList roleList)
        throws IllegalArgumentException,
               IndexOutOfBoundsException {
        if (roleList == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        return (super.addAll(index, roleList));
    }
    @Override
    public boolean add(Object o) {
        if (!tainted)
            tainted = isTainted(o);
        if (typeSafe)
            checkTypeSafe(o);
        return super.add(o);
    }
    @Override
    public void add(int index, Object element) {
        if (!tainted)
            tainted = isTainted(element);
        if (typeSafe)
            checkTypeSafe(element);
        super.add(index, element);
    }
    @Override
    public boolean addAll(Collection<?> c) {
        if (!tainted)
            tainted = isTainted(c);
        if (typeSafe)
            checkTypeSafe(c);
        return super.addAll(c);
    }
    @Override
    public boolean addAll(int index, Collection<?> c) {
        if (!tainted)
            tainted = isTainted(c);
        if (typeSafe)
            checkTypeSafe(c);
        return super.addAll(index, c);
    }
    @Override
    public Object set(int index, Object element) {
        if (!tainted)
            tainted = isTainted(element);
        if (typeSafe)
            checkTypeSafe(element);
        return super.set(index, element);
    }
    private static void checkTypeSafe(Object o) {
        try {
            o = (Role) o;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }
    private static void checkTypeSafe(Collection<?> c) {
        try {
            Role r;
            for (Object o : c)
                r = (Role) o;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(e);
        }
    }
    private static boolean isTainted(Object o) {
        try {
            checkTypeSafe(o);
        } catch (IllegalArgumentException e) {
            return true;
        }
        return false;
    }
    private static boolean isTainted(Collection<?> c) {
        try {
            checkTypeSafe(c);
        } catch (IllegalArgumentException e) {
            return true;
        }
        return false;
    }
}
