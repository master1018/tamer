public class RelationSupport
    implements RelationSupportMBean, MBeanRegistration {
    private String myRelId = null;
    private ObjectName myRelServiceName = null;
    private MBeanServer myRelServiceMBeanServer = null;
    private String myRelTypeName = null;
    private final Map<String,Role> myRoleName2ValueMap = new HashMap<String,Role>();
    private final AtomicBoolean myInRelServFlg = new AtomicBoolean();
    public RelationSupport(String relationId,
                        ObjectName relationServiceName,
                        String relationTypeName,
                        RoleList list)
        throws InvalidRoleValueException,
               IllegalArgumentException {
        super();
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "RelationSupport");
        initMembers(relationId,
                    relationServiceName,
                    null,
                    relationTypeName,
                    list);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "RelationSupport");
    }
    public RelationSupport(String relationId,
                        ObjectName relationServiceName,
                        MBeanServer relationServiceMBeanServer,
                        String relationTypeName,
                        RoleList list)
        throws InvalidRoleValueException,
               IllegalArgumentException {
        super();
        if (relationServiceMBeanServer == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "RelationSupport");
        initMembers(relationId,
                    relationServiceName,
                    relationServiceMBeanServer,
                    relationTypeName,
                    list);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "RelationSupport");
    }
    public List<ObjectName> getRole(String roleName)
        throws IllegalArgumentException,
               RoleNotFoundException,
               RelationServiceNotRegisteredException {
        if (roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getRole", roleName);
        List<ObjectName> result = cast(
            getRoleInt(roleName, false, null, false));
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "getRole");
        return result;
    }
    public RoleResult getRoles(String[] roleNameArray)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException {
        if (roleNameArray == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(), "getRoles");
        RoleResult result = getRolesInt(roleNameArray, false, null);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "getRoles");
        return result;
    }
    public RoleResult getAllRoles()
        throws RelationServiceNotRegisteredException {
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getAllRoles");
        RoleResult result = null;
        try {
            result = getAllRolesInt(false, null);
        } catch (IllegalArgumentException exc) {
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "getAllRoles");
        return result;
    }
    public RoleList retrieveAllRoles() {
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "retrieveAllRoles");
        RoleList result;
        synchronized(myRoleName2ValueMap) {
            result =
                new RoleList(new ArrayList<Role>(myRoleName2ValueMap.values()));
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "retrieveAllRoles");
        return result;
    }
    public Integer getRoleCardinality(String roleName)
        throws IllegalArgumentException,
               RoleNotFoundException {
        if (roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getRoleCardinality", roleName);
        Role role;
        synchronized(myRoleName2ValueMap) {
            role = (myRoleName2ValueMap.get(roleName));
        }
        if (role == null) {
            int pbType = RoleStatus.NO_ROLE_WITH_NAME;
            try {
                RelationService.throwRoleProblemException(pbType,
                                                          roleName);
            } catch (InvalidRoleValueException exc) {
            }
        }
        List<ObjectName> roleValue = role.getRoleValue();
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "getRoleCardinality");
        return roleValue.size();
    }
    public void setRole(Role role)
        throws IllegalArgumentException,
               RoleNotFoundException,
               RelationTypeNotFoundException,
               InvalidRoleValueException,
               RelationServiceNotRegisteredException,
               RelationNotFoundException {
        if (role == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "setRole", role);
        Object result = setRoleInt(role, false, null, false);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "setRole");
        return;
    }
    public RoleResult setRoles(RoleList list)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RelationTypeNotFoundException,
               RelationNotFoundException {
        if (list == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "setRoles", list);
        RoleResult result = setRolesInt(list, false, null);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "setRoles");
        return result;
    }
    public void handleMBeanUnregistration(ObjectName objectName,
                                          String roleName)
        throws IllegalArgumentException,
               RoleNotFoundException,
               InvalidRoleValueException,
               RelationServiceNotRegisteredException,
               RelationTypeNotFoundException,
               RelationNotFoundException {
        if (objectName == null || roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "handleMBeanUnregistration",
                new Object[]{objectName, roleName});
        handleMBeanUnregistrationInt(objectName,
                                     roleName,
                                     false,
                                     null);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "handleMBeanUnregistration");
        return;
    }
    public Map<ObjectName,List<String>> getReferencedMBeans() {
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getReferencedMBeans");
        Map<ObjectName,List<String>> refMBeanMap =
            new HashMap<ObjectName,List<String>>();
        synchronized(myRoleName2ValueMap) {
            for (Role currRole : myRoleName2ValueMap.values()) {
                String currRoleName = currRole.getRoleName();
                List<ObjectName> currRefMBeanList = currRole.getRoleValue();
                for (ObjectName currRoleObjName : currRefMBeanList) {
                    List<String> mbeanRoleNameList =
                        refMBeanMap.get(currRoleObjName);
                    boolean newRefFlg = false;
                    if (mbeanRoleNameList == null) {
                        newRefFlg = true;
                        mbeanRoleNameList = new ArrayList<String>();
                    }
                    mbeanRoleNameList.add(currRoleName);
                    if (newRefFlg) {
                        refMBeanMap.put(currRoleObjName, mbeanRoleNameList);
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "getReferencedMBeans");
        return refMBeanMap;
    }
    public String getRelationTypeName() {
        return myRelTypeName;
    }
    public ObjectName getRelationServiceName() {
        return myRelServiceName;
    }
    public String getRelationId() {
        return myRelId;
    }
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name)
        throws Exception {
        myRelServiceMBeanServer = server;
        return name;
    }
    public void postRegister(Boolean registrationDone) {
        return;
    }
    public void preDeregister()
        throws Exception {
        return;
    }
    public void postDeregister() {
        return;
    }
    public Boolean isInRelationService() {
        return myInRelServFlg.get();
    }
    public void setRelationServiceManagementFlag(Boolean flag)
        throws IllegalArgumentException {
        if (flag == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        myInRelServFlg.set(flag);
    }
    Object getRoleInt(String roleName,
                      boolean relationServCallFlg,
                      RelationService relationServ,
                      boolean multiRoleFlg)
        throws IllegalArgumentException,
               RoleNotFoundException,
               RelationServiceNotRegisteredException {
        if (roleName == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getRoleInt", roleName);
        int pbType = 0;
        Role role;
        synchronized(myRoleName2ValueMap) {
            role = (myRoleName2ValueMap.get(roleName));
        }
        if (role == null) {
                pbType = RoleStatus.NO_ROLE_WITH_NAME;
        } else {
            Integer status;
            if (relationServCallFlg) {
                try {
                    status = relationServ.checkRoleReading(roleName,
                                                         myRelTypeName);
                } catch (RelationTypeNotFoundException exc) {
                    throw new RuntimeException(exc.getMessage());
                }
            } else {
                Object[] params = new Object[2];
                params[0] = roleName;
                params[1] = myRelTypeName;
                String[] signature = new String[2];
                signature[0] = "java.lang.String";
                signature[1] = "java.lang.String";
                try {
                    status = (Integer)
                        (myRelServiceMBeanServer.invoke(myRelServiceName,
                                                        "checkRoleReading",
                                                        params,
                                                        signature));
                } catch (MBeanException exc1) {
                    throw new RuntimeException("incorrect relation type");
                } catch (ReflectionException exc2) {
                    throw new RuntimeException(exc2.getMessage());
                } catch (InstanceNotFoundException exc3) {
                    throw new RelationServiceNotRegisteredException(
                                                            exc3.getMessage());
                }
            }
            pbType = status.intValue();
        }
        Object result;
        if (pbType == 0) {
            if (!(multiRoleFlg)) {
                result = new ArrayList<ObjectName>(role.getRoleValue());
            } else {
                result = (Role)(role.clone());
            }
        } else {
            if (!(multiRoleFlg)) {
                try {
                    RelationService.throwRoleProblemException(pbType,
                                                              roleName);
                    return null;
                } catch (InvalidRoleValueException exc) {
                    throw new RuntimeException(exc.getMessage());
                }
            } else {
                result = new RoleUnresolved(roleName, null, pbType);
            }
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "getRoleInt");
        return result;
    }
    RoleResult getRolesInt(String[] roleNameArray,
                           boolean relationServCallFlg,
                           RelationService relationServ)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException {
        if (roleNameArray == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getRolesInt");
        RoleList roleList = new RoleList();
        RoleUnresolvedList roleUnresList = new RoleUnresolvedList();
        for (int i = 0; i < roleNameArray.length; i++) {
            String currRoleName = roleNameArray[i];
            Object currResult;
            try {
                currResult = getRoleInt(currRoleName,
                                        relationServCallFlg,
                                        relationServ,
                                        true);
            } catch (RoleNotFoundException exc) {
                return null; 
            }
            if (currResult instanceof Role) {
                try {
                    roleList.add((Role)currResult);
                } catch (IllegalArgumentException exc) {
                    throw new RuntimeException(exc.getMessage());
                }
            } else if (currResult instanceof RoleUnresolved) {
                try {
                    roleUnresList.add((RoleUnresolved)currResult);
                } catch (IllegalArgumentException exc) {
                    throw new RuntimeException(exc.getMessage());
                }
            }
        }
        RoleResult result = new RoleResult(roleList, roleUnresList);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "getRolesInt");
        return result;
    }
    RoleResult getAllRolesInt(boolean relationServCallFlg,
                              RelationService relationServ)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException {
        if (relationServCallFlg && relationServ == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "getAllRolesInt");
        List<String> roleNameList;
        synchronized(myRoleName2ValueMap) {
            roleNameList =
                new ArrayList<String>(myRoleName2ValueMap.keySet());
        }
        String[] roleNames = new String[roleNameList.size()];
        roleNameList.toArray(roleNames);
        RoleResult result = getRolesInt(roleNames,
                                        relationServCallFlg,
                                        relationServ);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "getAllRolesInt");
        return result;
    }
    Object setRoleInt(Role aRole,
                      boolean relationServCallFlg,
                      RelationService relationServ,
                      boolean multiRoleFlg)
        throws IllegalArgumentException,
               RoleNotFoundException,
               InvalidRoleValueException,
               RelationServiceNotRegisteredException,
               RelationTypeNotFoundException,
               RelationNotFoundException {
        if (aRole == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "setRoleInt", new Object[] {aRole, relationServCallFlg,
                relationServ, multiRoleFlg});
        String roleName = aRole.getRoleName();
        int pbType = 0;
        Role role;
        synchronized(myRoleName2ValueMap) {
            role = (myRoleName2ValueMap.get(roleName));
        }
        List<ObjectName> oldRoleValue;
        Boolean initFlg;
        if (role == null) {
            initFlg = true;
            oldRoleValue = new ArrayList<ObjectName>();
        } else {
            initFlg = false;
            oldRoleValue = role.getRoleValue();
        }
        try {
            Integer status;
            if (relationServCallFlg) {
                status = relationServ.checkRoleWriting(aRole,
                                                     myRelTypeName,
                                                     initFlg);
            } else {
                Object[] params = new Object[3];
                params[0] = aRole;
                params[1] = myRelTypeName;
                params[2] = initFlg;
                String[] signature = new String[3];
                signature[0] = "javax.management.relation.Role";
                signature[1] = "java.lang.String";
                signature[2] = "java.lang.Boolean";
                status = (Integer)
                    (myRelServiceMBeanServer.invoke(myRelServiceName,
                                                    "checkRoleWriting",
                                                    params,
                                                    signature));
            }
            pbType = status.intValue();
        } catch (MBeanException exc2) {
            Exception wrappedExc = exc2.getTargetException();
            if (wrappedExc instanceof RelationTypeNotFoundException) {
                throw ((RelationTypeNotFoundException)wrappedExc);
            } else {
                throw new RuntimeException(wrappedExc.getMessage());
            }
        } catch (ReflectionException exc3) {
            throw new RuntimeException(exc3.getMessage());
        } catch (RelationTypeNotFoundException exc4) {
            throw new RuntimeException(exc4.getMessage());
        } catch (InstanceNotFoundException exc5) {
            throw new RelationServiceNotRegisteredException(exc5.getMessage());
        }
        Object result = null;
        if (pbType == 0) {
            if (!(initFlg.booleanValue())) {
                sendRoleUpdateNotification(aRole,
                                           oldRoleValue,
                                           relationServCallFlg,
                                           relationServ);
                updateRelationServiceMap(aRole,
                                         oldRoleValue,
                                         relationServCallFlg,
                                         relationServ);
            }
            synchronized(myRoleName2ValueMap) {
                myRoleName2ValueMap.put(roleName,
                                        (Role)(aRole.clone()));
            }
            if (multiRoleFlg) {
                result = aRole;
            }
        } else {
            if (!(multiRoleFlg)) {
                RelationService.throwRoleProblemException(pbType,
                                                          roleName);
                return null;
            } else {
                result = new RoleUnresolved(roleName,
                                            aRole.getRoleValue(),
                                            pbType);
            }
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "setRoleInt");
        return result;
    }
    private void sendRoleUpdateNotification(Role newRole,
                                            List<ObjectName> oldRoleValue,
                                            boolean relationServCallFlg,
                                            RelationService relationServ)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RelationNotFoundException {
        if (newRole == null ||
            oldRoleValue == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "sendRoleUpdateNotification", new Object[] {newRole,
                oldRoleValue, relationServCallFlg, relationServ});
        if (relationServCallFlg) {
            try {
                relationServ.sendRoleUpdateNotification(myRelId,
                                                      newRole,
                                                      oldRoleValue);
            } catch (RelationNotFoundException exc) {
                throw new RuntimeException(exc.getMessage());
            }
        } else {
            Object[] params = new Object[3];
            params[0] = myRelId;
            params[1] = newRole;
            params[2] = oldRoleValue;
            String[] signature = new String[3];
            signature[0] = "java.lang.String";
            signature[1] = "javax.management.relation.Role";
            signature[2] = "java.util.List";
            try {
                myRelServiceMBeanServer.invoke(myRelServiceName,
                                               "sendRoleUpdateNotification",
                                               params,
                                               signature);
            } catch (ReflectionException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (InstanceNotFoundException exc2) {
                throw new RelationServiceNotRegisteredException(
                                                            exc2.getMessage());
            } catch (MBeanException exc3) {
                Exception wrappedExc = exc3.getTargetException();
                if (wrappedExc instanceof RelationNotFoundException) {
                    throw ((RelationNotFoundException)wrappedExc);
                } else {
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "sendRoleUpdateNotification");
        return;
    }
    private void updateRelationServiceMap(Role newRole,
                                          List<ObjectName> oldRoleValue,
                                          boolean relationServCallFlg,
                                          RelationService relationServ)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RelationNotFoundException {
        if (newRole == null ||
            oldRoleValue == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "updateRelationServiceMap", new Object[] {newRole,
                oldRoleValue, relationServCallFlg, relationServ});
        if (relationServCallFlg) {
            try {
                relationServ.updateRoleMap(myRelId,
                                         newRole,
                                         oldRoleValue);
            } catch (RelationNotFoundException exc) {
                throw new RuntimeException(exc.getMessage());
            }
        } else {
            Object[] params = new Object[3];
            params[0] = myRelId;
            params[1] = newRole;
            params[2] = oldRoleValue;
            String[] signature = new String[3];
            signature[0] = "java.lang.String";
            signature[1] = "javax.management.relation.Role";
            signature[2] = "java.util.List";
            try {
                myRelServiceMBeanServer.invoke(myRelServiceName,
                                               "updateRoleMap",
                                               params,
                                               signature);
            } catch (ReflectionException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (InstanceNotFoundException exc2) {
                throw new
                     RelationServiceNotRegisteredException(exc2.getMessage());
            } catch (MBeanException exc3) {
                Exception wrappedExc = exc3.getTargetException();
                if (wrappedExc instanceof RelationNotFoundException) {
                    throw ((RelationNotFoundException)wrappedExc);
                } else {
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "updateRelationServiceMap");
        return;
    }
    RoleResult setRolesInt(RoleList list,
                           boolean relationServCallFlg,
                           RelationService relationServ)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RelationTypeNotFoundException,
               RelationNotFoundException {
        if (list == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "setRolesInt",
                new Object[] {list, relationServCallFlg, relationServ});
        RoleList roleList = new RoleList();
        RoleUnresolvedList roleUnresList = new RoleUnresolvedList();
        for (Role currRole : list.asList()) {
            Object currResult = null;
            try {
                currResult = setRoleInt(currRole,
                                        relationServCallFlg,
                                        relationServ,
                                        true);
            } catch (RoleNotFoundException exc1) {
            } catch (InvalidRoleValueException exc2) {
            }
            if (currResult instanceof Role) {
                try {
                    roleList.add((Role)currResult);
                } catch (IllegalArgumentException exc) {
                    throw new RuntimeException(exc.getMessage());
                }
            } else if (currResult instanceof RoleUnresolved) {
                try {
                    roleUnresList.add((RoleUnresolved)currResult);
                } catch (IllegalArgumentException exc) {
                    throw new RuntimeException(exc.getMessage());
                }
            }
        }
        RoleResult result = new RoleResult(roleList, roleUnresList);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "setRolesInt");
        return result;
    }
    private void initMembers(String relationId,
                             ObjectName relationServiceName,
                             MBeanServer relationServiceMBeanServer,
                             String relationTypeName,
                             RoleList list)
        throws InvalidRoleValueException,
               IllegalArgumentException {
        if (relationId == null ||
            relationServiceName == null ||
            relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "initMembers", new Object[] {relationId, relationServiceName,
                relationServiceMBeanServer, relationTypeName, list});
        myRelId = relationId;
        myRelServiceName = relationServiceName;
        myRelServiceMBeanServer = relationServiceMBeanServer;
        myRelTypeName = relationTypeName;
        initRoleMap(list);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "initMembers");
        return;
    }
    private void initRoleMap(RoleList list)
        throws InvalidRoleValueException {
        if (list == null) {
            return;
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "initRoleMap", list);
        synchronized(myRoleName2ValueMap) {
            for (Role currRole : list.asList()) {
                String currRoleName = currRole.getRoleName();
                if (myRoleName2ValueMap.containsKey(currRoleName)) {
                    StringBuilder excMsgStrB = new StringBuilder("Role name ");
                    excMsgStrB.append(currRoleName);
                    excMsgStrB.append(" used for two roles.");
                    throw new InvalidRoleValueException(excMsgStrB.toString());
                }
                myRoleName2ValueMap.put(currRoleName,
                                        (Role)(currRole.clone()));
            }
        }
        RELATION_LOGGER.exiting(RelationSupport.class.getName(), "initRoleMap");
        return;
    }
    void handleMBeanUnregistrationInt(ObjectName objectName,
                                      String roleName,
                                      boolean relationServCallFlg,
                                      RelationService relationServ)
        throws IllegalArgumentException,
               RoleNotFoundException,
               InvalidRoleValueException,
               RelationServiceNotRegisteredException,
               RelationTypeNotFoundException,
               RelationNotFoundException {
        if (objectName == null ||
            roleName == null ||
            (relationServCallFlg && relationServ == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationSupport.class.getName(),
                "handleMBeanUnregistrationInt", new Object[] {objectName,
                roleName, relationServCallFlg, relationServ});
        Role role;
        synchronized(myRoleName2ValueMap) {
            role = (myRoleName2ValueMap.get(roleName));
        }
        if (role == null) {
            StringBuilder excMsgStrB = new StringBuilder();
            String excMsg = "No role with name ";
            excMsgStrB.append(excMsg);
            excMsgStrB.append(roleName);
            throw new RoleNotFoundException(excMsgStrB.toString());
        }
        List<ObjectName> currRoleValue = role.getRoleValue();
        List<ObjectName> newRoleValue = new ArrayList<ObjectName>(currRoleValue);
        newRoleValue.remove(objectName);
        Role newRole = new Role(roleName, newRoleValue);
        Object result =
            setRoleInt(newRole, relationServCallFlg, relationServ, false);
        RELATION_LOGGER.exiting(RelationSupport.class.getName(),
                "handleMBeanUnregistrationInt");
        return;
    }
}
