public class AddUserRoleListener extends AbstractListener implements IAddUserRoleListener {
    @Override
    public void userRoleAdded(IUser user) {
        if (user != null && user.getSystemRoles() != null) {
            for (ISystemRole systemRole : user.getSystemRoles()) {
                String channel = Channels.getAddSystemUserChannel(systemRole);
                publish(user, channel);
            }
        }
    }
}
