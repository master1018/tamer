public class GroupImpl implements Group {
    private Vector groupMembers;
    private String group;
    public GroupImpl(String s) {
        groupMembers = new Vector(50, 100);
        group = s;
    }
    public boolean addMember(Principal principal) {
        if(groupMembers.contains(principal))
            return false;
        if(group.equals(principal.toString())) {
            throw new IllegalArgumentException();
        } else {
            groupMembers.addElement(principal);
            return true;
        }
    }
    public boolean removeMember(Principal principal) {
        return groupMembers.removeElement(principal);
    }
    public Enumeration members() {
        return groupMembers.elements();
    }
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!(obj instanceof Group)) {
            return false;
        } else {
            Group group1 = (Group)obj;
            return group.equals(group1.toString());
        }
    }
    public boolean equals(Group group1) {
        return equals(group1);
    }
    public String toString() {
        return group;
    }
    public int hashCode() {
        return group.hashCode();
    }
    public boolean isMember(Principal principal) {
        if(groupMembers.contains(principal)) {
            return true;
        } else {
            Vector vector = new Vector(10);
            return isMemberRecurse(principal, vector);
        }
    }
    public String getName() {
        return group;
    }
    boolean isMemberRecurse(Principal principal, Vector vector) {
        for(Enumeration enumeration = members(); enumeration.hasMoreElements();) {
            boolean flag = false;
            Principal principal1 = (Principal)enumeration.nextElement();
            if(principal1.equals(principal))
                return true;
            if(principal1 instanceof GroupImpl) {
                GroupImpl groupimpl = (GroupImpl)principal1;
                vector.addElement(this);
                if(!vector.contains(groupimpl))
                    flag = groupimpl.isMemberRecurse(principal, vector);
            } else if(principal1 instanceof Group) {
                Group group1 = (Group)principal1;
                if(!vector.contains(group1)) flag = group1.isMember(principal);
            } 
            if(flag) return flag;
        }
        return false;
    }
}