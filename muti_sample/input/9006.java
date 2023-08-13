public class RelationService extends NotificationBroadcasterSupport
    implements RelationServiceMBean, MBeanRegistration, NotificationListener {
    private Map<String,Object> myRelId2ObjMap = new HashMap<String,Object>();
    private Map<String,String> myRelId2RelTypeMap = new HashMap<String,String>();
    private Map<ObjectName,String> myRelMBeanObjName2RelIdMap =
        new HashMap<ObjectName,String>();
    private Map<String,RelationType> myRelType2ObjMap =
        new HashMap<String,RelationType>();
    private Map<String,List<String>> myRelType2RelIdsMap =
        new HashMap<String,List<String>>();
    private final Map<ObjectName,Map<String,List<String>>>
        myRefedMBeanObjName2RelIdsMap =
            new HashMap<ObjectName,Map<String,List<String>>>();
    private boolean myPurgeFlag = true;
    private final AtomicLong atomicSeqNo = new AtomicLong();
    private ObjectName myObjName = null;
    private MBeanServer myMBeanServer = null;
    private MBeanServerNotificationFilter myUnregNtfFilter = null;
    private List<MBeanServerNotification> myUnregNtfList =
        new ArrayList<MBeanServerNotification>();
    public RelationService(boolean immediatePurgeFlag) {
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "RelationService");
        setPurgeFlag(immediatePurgeFlag);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "RelationService");
        return;
    }
    public void isActive()
        throws RelationServiceNotRegisteredException {
        if (myMBeanServer == null) {
            String excMsg =
                "Relation Service not registered in the MBean Server.";
            throw new RelationServiceNotRegisteredException(excMsg);
        }
        return;
    }
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name)
        throws Exception {
        myMBeanServer = server;
        myObjName = name;
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
    public boolean getPurgeFlag() {
        return myPurgeFlag;
    }
    public void setPurgeFlag(boolean purgeFlag) {
        myPurgeFlag = purgeFlag;
        return;
    }
    public void createRelationType(String relationTypeName,
                                   RoleInfo[] roleInfoArray)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (relationTypeName == null || roleInfoArray == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "createRelationType", relationTypeName);
        RelationType relType =
            new RelationTypeSupport(relationTypeName, roleInfoArray);
        addRelationTypeInt(relType);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "createRelationType");
        return;
    }
    public void addRelationType(RelationType relationTypeObj)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (relationTypeObj == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelationType");
        List<RoleInfo> roleInfoList = relationTypeObj.getRoleInfos();
        if (roleInfoList == null) {
            String excMsg = "No role info provided.";
            throw new InvalidRelationTypeException(excMsg);
        }
        RoleInfo[] roleInfoArray = new RoleInfo[roleInfoList.size()];
        int i = 0;
        for (RoleInfo currRoleInfo : roleInfoList) {
            roleInfoArray[i] = currRoleInfo;
            i++;
        }
        RelationTypeSupport.checkRoleInfos(roleInfoArray);
        addRelationTypeInt(relationTypeObj);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelationType");
        return;
     }
    public List<String> getAllRelationTypeNames() {
        ArrayList<String> result;
        synchronized(myRelType2ObjMap) {
            result = new ArrayList<String>(myRelType2ObjMap.keySet());
        }
        return result;
    }
    public List<RoleInfo> getRoleInfos(String relationTypeName)
        throws IllegalArgumentException,
               RelationTypeNotFoundException {
        if (relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoleInfos", relationTypeName);
        RelationType relType = getRelationType(relationTypeName);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRoleInfos");
        return relType.getRoleInfos();
    }
    public RoleInfo getRoleInfo(String relationTypeName,
                                String roleInfoName)
        throws IllegalArgumentException,
               RelationTypeNotFoundException,
               RoleInfoNotFoundException {
        if (relationTypeName == null || roleInfoName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoleInfo", new Object[] {relationTypeName, roleInfoName});
        RelationType relType = getRelationType(relationTypeName);
        RoleInfo roleInfo = relType.getRoleInfo(roleInfoName);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRoleInfo");
        return roleInfo;
    }
    public void removeRelationType(String relationTypeName)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RelationTypeNotFoundException {
        isActive();
        if (relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "removeRelationType", relationTypeName);
        RelationType relType = getRelationType(relationTypeName);
        List<String> relIdList = null;
        synchronized(myRelType2RelIdsMap) {
            List<String> relIdList1 =
                myRelType2RelIdsMap.get(relationTypeName);
            if (relIdList1 != null) {
                relIdList = new ArrayList<String>(relIdList1);
            }
        }
        synchronized(myRelType2ObjMap) {
            myRelType2ObjMap.remove(relationTypeName);
        }
        synchronized(myRelType2RelIdsMap) {
            myRelType2RelIdsMap.remove(relationTypeName);
        }
        if (relIdList != null) {
            for (String currRelId : relIdList) {
                try {
                    removeRelation(currRelId);
                } catch (RelationNotFoundException exc1) {
                    throw new RuntimeException(exc1.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "removeRelationType");
        return;
    }
    public void createRelation(String relationId,
                               String relationTypeName,
                               RoleList roleList)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RoleNotFoundException,
               InvalidRelationIdException,
               RelationTypeNotFoundException,
               InvalidRoleValueException {
        isActive();
        if (relationId == null ||
            relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "createRelation",
                new Object[] {relationId, relationTypeName, roleList});
        RelationSupport relObj = new RelationSupport(relationId,
                                               myObjName,
                                               relationTypeName,
                                               roleList);
        addRelationInt(true,
                       relObj,
                       null,
                       relationId,
                       relationTypeName,
                       roleList);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "createRelation");
        return;
    }
    public void addRelation(ObjectName relationObjectName)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               NoSuchMethodException,
               InvalidRelationIdException,
               InstanceNotFoundException,
               InvalidRelationServiceException,
               RelationTypeNotFoundException,
               RoleNotFoundException,
               InvalidRoleValueException {
        if (relationObjectName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelation", relationObjectName);
        isActive();
        if ((!(myMBeanServer.isInstanceOf(relationObjectName, "javax.management.relation.Relation")))) {
            String excMsg = "This MBean does not implement the Relation interface.";
            throw new NoSuchMethodException(excMsg);
        }
        String relId;
        try {
            relId = (String)(myMBeanServer.getAttribute(relationObjectName,
                                                        "RelationId"));
        } catch (MBeanException exc1) {
            throw new RuntimeException(
                                     (exc1.getTargetException()).getMessage());
        } catch (ReflectionException exc2) {
            throw new RuntimeException(exc2.getMessage());
        } catch (AttributeNotFoundException exc3) {
            throw new RuntimeException(exc3.getMessage());
        }
        if (relId == null) {
            String excMsg = "This MBean does not provide a relation id.";
            throw new InvalidRelationIdException(excMsg);
        }
        ObjectName relServObjName;
        try {
            relServObjName = (ObjectName)
                (myMBeanServer.getAttribute(relationObjectName,
                                            "RelationServiceName"));
        } catch (MBeanException exc1) {
            throw new RuntimeException(
                                     (exc1.getTargetException()).getMessage());
        } catch (ReflectionException exc2) {
            throw new RuntimeException(exc2.getMessage());
        } catch (AttributeNotFoundException exc3) {
            throw new RuntimeException(exc3.getMessage());
        }
        boolean badRelServFlag = false;
        if (relServObjName == null) {
            badRelServFlag = true;
        } else if (!(relServObjName.equals(myObjName))) {
            badRelServFlag = true;
        }
        if (badRelServFlag) {
            String excMsg = "The Relation Service referenced in the MBean is not the current one.";
            throw new InvalidRelationServiceException(excMsg);
        }
        String relTypeName;
        try {
            relTypeName = (String)(myMBeanServer.getAttribute(relationObjectName,
                                                              "RelationTypeName"));
        } catch (MBeanException exc1) {
            throw new RuntimeException(
                                     (exc1.getTargetException()).getMessage());
        }catch (ReflectionException exc2) {
            throw new RuntimeException(exc2.getMessage());
        } catch (AttributeNotFoundException exc3) {
            throw new RuntimeException(exc3.getMessage());
        }
        if (relTypeName == null) {
            String excMsg = "No relation type provided.";
            throw new RelationTypeNotFoundException(excMsg);
        }
        RoleList roleList;
        try {
            roleList = (RoleList)(myMBeanServer.invoke(relationObjectName,
                                                       "retrieveAllRoles",
                                                       null,
                                                       null));
        } catch (MBeanException exc1) {
            throw new RuntimeException(
                                     (exc1.getTargetException()).getMessage());
        } catch (ReflectionException exc2) {
            throw new RuntimeException(exc2.getMessage());
        }
        addRelationInt(false,
                       null,
                       relationObjectName,
                       relId,
                       relTypeName,
                       roleList);
        synchronized(myRelMBeanObjName2RelIdMap) {
            myRelMBeanObjName2RelIdMap.put(relationObjectName, relId);
        }
        try {
            myMBeanServer.setAttribute(relationObjectName,
                                       new Attribute(
                                         "RelationServiceManagementFlag",
                                         Boolean.TRUE));
        } catch (Exception exc) {
        }
        List<ObjectName> newRefList = new ArrayList<ObjectName>();
        newRefList.add(relationObjectName);
        updateUnregistrationListener(newRefList, null);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelation");
        return;
    }
    public ObjectName isRelationMBean(String relationId)
        throws IllegalArgumentException,
               RelationNotFoundException{
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "isRelationMBean", relationId);
        Object result = getRelation(relationId);
        if (result instanceof ObjectName) {
            return ((ObjectName)result);
        } else {
            return null;
        }
    }
    public String isRelation(ObjectName objectName)
        throws IllegalArgumentException {
        if (objectName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "isRelation", objectName);
        String result = null;
        synchronized(myRelMBeanObjName2RelIdMap) {
            String relId = myRelMBeanObjName2RelIdMap.get(objectName);
            if (relId != null) {
                result = relId;
            }
        }
        return result;
    }
    public Boolean hasRelation(String relationId)
        throws IllegalArgumentException {
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "hasRelation", relationId);
        try {
            Object result = getRelation(relationId);
            return true;
        } catch (RelationNotFoundException exc) {
            return false;
        }
    }
    public List<String> getAllRelationIds() {
        List<String> result;
        synchronized(myRelId2ObjMap) {
            result = new ArrayList<String>(myRelId2ObjMap.keySet());
        }
        return result;
    }
    public Integer checkRoleReading(String roleName,
                                    String relationTypeName)
        throws IllegalArgumentException,
               RelationTypeNotFoundException {
        if (roleName == null || relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "checkRoleReading", new Object[] {roleName, relationTypeName});
        Integer result;
        RelationType relType = getRelationType(relationTypeName);
        try {
            RoleInfo roleInfo = relType.getRoleInfo(roleName);
            result =  checkRoleInt(1,
                                   roleName,
                                   null,
                                   roleInfo,
                                   false);
        } catch (RoleInfoNotFoundException exc) {
            result = Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "checkRoleReading");
        return result;
    }
    public Integer checkRoleWriting(Role role,
                                    String relationTypeName,
                                    Boolean initFlag)
        throws IllegalArgumentException,
               RelationTypeNotFoundException {
        if (role == null ||
            relationTypeName == null ||
            initFlag == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "checkRoleWriting",
                new Object[] {role, relationTypeName, initFlag});
        RelationType relType = getRelationType(relationTypeName);
        String roleName = role.getRoleName();
        List<ObjectName> roleValue = role.getRoleValue();
        boolean writeChkFlag = true;
        if (initFlag.booleanValue()) {
            writeChkFlag = false;
        }
        RoleInfo roleInfo;
        try {
            roleInfo = relType.getRoleInfo(roleName);
        } catch (RoleInfoNotFoundException exc) {
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleWriting");
            return Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME);
        }
        Integer result = checkRoleInt(2,
                                      roleName,
                                      roleValue,
                                      roleInfo,
                                      writeChkFlag);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "checkRoleWriting");
        return result;
    }
    public void sendRelationCreationNotification(String relationId)
        throws IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendRelationCreationNotification", relationId);
        StringBuilder ntfMsg = new StringBuilder("Creation of relation ");
        ntfMsg.append(relationId);
        sendNotificationInt(1,
                            ntfMsg.toString(),
                            relationId,
                            null,
                            null,
                            null,
                            null);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendRelationCreationNotification");
        return;
    }
    public void sendRoleUpdateNotification(String relationId,
                                           Role newRole,
                                           List<ObjectName> oldValue)
        throws IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null ||
            newRole == null ||
            oldValue == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        if (!(oldValue instanceof ArrayList<?>))
            oldValue = new ArrayList<ObjectName>(oldValue);
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendRoleUpdateNotification",
                new Object[] {relationId, newRole, oldValue});
        String roleName = newRole.getRoleName();
        List<ObjectName> newRoleVal = newRole.getRoleValue();
        String newRoleValString = Role.roleValueToString(newRoleVal);
        String oldRoleValString = Role.roleValueToString(oldValue);
        StringBuilder ntfMsg = new StringBuilder("Value of role ");
        ntfMsg.append(roleName);
        ntfMsg.append(" has changed\nOld value:\n");
        ntfMsg.append(oldRoleValString);
        ntfMsg.append("\nNew value:\n");
        ntfMsg.append(newRoleValString);
        sendNotificationInt(2,
                            ntfMsg.toString(),
                            relationId,
                            null,
                            roleName,
                            newRoleVal,
                            oldValue);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendRoleUpdateNotification");
    }
    public void sendRelationRemovalNotification(String relationId,
                                                List<ObjectName> unregMBeanList)
        throws IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null) {
            String excMsg = "Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendRelationRemovalNotification",
                new Object[] {relationId, unregMBeanList});
        sendNotificationInt(3,
                            "Removal of relation " + relationId,
                            relationId,
                            unregMBeanList,
                            null,
                            null,
                            null);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendRelationRemovalNotification");
        return;
    }
    public void updateRoleMap(String relationId,
                              Role newRole,
                              List<ObjectName> oldValue)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RelationNotFoundException {
        if (relationId == null ||
            newRole == null ||
            oldValue == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "updateRoleMap", new Object[] {relationId, newRole, oldValue});
        isActive();
        Object result = getRelation(relationId);
        String roleName = newRole.getRoleName();
        List<ObjectName> newRoleValue = newRole.getRoleValue();
        List<ObjectName> oldRoleValue =
            new ArrayList<ObjectName>(oldValue);
        List<ObjectName> newRefList = new ArrayList<ObjectName>();
        for (ObjectName currObjName : newRoleValue) {
            int currObjNamePos = oldRoleValue.indexOf(currObjName);
            if (currObjNamePos == -1) {
                boolean isNewFlag = addNewMBeanReference(currObjName,
                                                        relationId,
                                                        roleName);
                if (isNewFlag) {
                    newRefList.add(currObjName);
                }
            } else {
                oldRoleValue.remove(currObjNamePos);
            }
        }
        List<ObjectName> obsRefList = new ArrayList<ObjectName>();
        for (ObjectName currObjName : oldRoleValue) {
            boolean noLongerRefFlag = removeMBeanReference(currObjName,
                                                          relationId,
                                                          roleName,
                                                          false);
            if (noLongerRefFlag) {
                obsRefList.add(currObjName);
            }
        }
        updateUnregistrationListener(newRefList, obsRefList);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "updateRoleMap");
        return;
    }
    public void removeRelation(String relationId)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RelationNotFoundException {
        isActive();
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "removeRelation", relationId);
        Object result = getRelation(relationId);
        if (result instanceof ObjectName) {
            List<ObjectName> obsRefList = new ArrayList<ObjectName>();
            obsRefList.add((ObjectName)result);
            updateUnregistrationListener(null, obsRefList);
        }
        sendRelationRemovalNotification(relationId, null);
        List<ObjectName> refMBeanList = new ArrayList<ObjectName>();
        List<ObjectName> nonRefObjNameList = new ArrayList<ObjectName>();
        synchronized(myRefedMBeanObjName2RelIdsMap) {
            for (ObjectName currRefObjName :
                     myRefedMBeanObjName2RelIdsMap.keySet()) {
                Map<String,List<String>> relIdMap =
                    myRefedMBeanObjName2RelIdsMap.get(currRefObjName);
                if (relIdMap.containsKey(relationId)) {
                    relIdMap.remove(relationId);
                    refMBeanList.add(currRefObjName);
                }
                if (relIdMap.isEmpty()) {
                    nonRefObjNameList.add(currRefObjName);
                }
            }
            for (ObjectName currRefObjName : nonRefObjNameList) {
                myRefedMBeanObjName2RelIdsMap.remove(currRefObjName);
            }
        }
        synchronized(myRelId2ObjMap) {
            myRelId2ObjMap.remove(relationId);
        }
        if (result instanceof ObjectName) {
            synchronized(myRelMBeanObjName2RelIdMap) {
                myRelMBeanObjName2RelIdMap.remove((ObjectName)result);
            }
        }
        String relTypeName;
        synchronized(myRelId2RelTypeMap) {
            relTypeName = myRelId2RelTypeMap.get(relationId);
            myRelId2RelTypeMap.remove(relationId);
        }
        synchronized(myRelType2RelIdsMap) {
            List<String> relIdList = myRelType2RelIdsMap.get(relTypeName);
            if (relIdList != null) {
                relIdList.remove(relationId);
                if (relIdList.isEmpty()) {
                    myRelType2RelIdsMap.remove(relTypeName);
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "removeRelation");
        return;
    }
    public void purgeRelations()
        throws RelationServiceNotRegisteredException {
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "purgeRelations");
        isActive();
        List<MBeanServerNotification> localUnregNtfList;
        synchronized(myRefedMBeanObjName2RelIdsMap) {
            localUnregNtfList =
                new ArrayList<MBeanServerNotification>(myUnregNtfList);
            myUnregNtfList = new ArrayList<MBeanServerNotification>();
        }
        List<ObjectName> obsRefList = new ArrayList<ObjectName>();
        Map<ObjectName,Map<String,List<String>>> localMBean2RelIdMap =
            new HashMap<ObjectName,Map<String,List<String>>>();
        synchronized(myRefedMBeanObjName2RelIdsMap) {
            for (MBeanServerNotification currNtf : localUnregNtfList) {
                ObjectName unregMBeanName = currNtf.getMBeanName();
                obsRefList.add(unregMBeanName);
                Map<String,List<String>> relIdMap =
                    myRefedMBeanObjName2RelIdsMap.get(unregMBeanName);
                localMBean2RelIdMap.put(unregMBeanName, relIdMap);
                myRefedMBeanObjName2RelIdsMap.remove(unregMBeanName);
            }
        }
        updateUnregistrationListener(null, obsRefList);
        for (MBeanServerNotification currNtf : localUnregNtfList) {
            ObjectName unregMBeanName = currNtf.getMBeanName();
            Map<String,List<String>> localRelIdMap =
                    localMBean2RelIdMap.get(unregMBeanName);
            for (Map.Entry<String,List<String>> currRel :
                        localRelIdMap.entrySet()) {
                final String currRelId = currRel.getKey();
                List<String> localRoleNameList = currRel.getValue();
                try {
                    handleReferenceUnregistration(currRelId,
                                                  unregMBeanName,
                                                  localRoleNameList);
                } catch (RelationNotFoundException exc1) {
                    throw new RuntimeException(exc1.getMessage());
                } catch (RoleNotFoundException exc2) {
                    throw new RuntimeException(exc2.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "purgeRelations");
        return;
    }
    public Map<String,List<String>>
        findReferencingRelations(ObjectName mbeanName,
                                 String relationTypeName,
                                 String roleName)
            throws IllegalArgumentException {
        if (mbeanName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "findReferencingRelations",
                new Object[] {mbeanName, relationTypeName, roleName});
        Map<String,List<String>> result = new HashMap<String,List<String>>();
        synchronized(myRefedMBeanObjName2RelIdsMap) {
            Map<String,List<String>> relId2RoleNamesMap =
                myRefedMBeanObjName2RelIdsMap.get(mbeanName);
            if (relId2RoleNamesMap != null) {
                Set<String> allRelIdSet = relId2RoleNamesMap.keySet();
                List<String> relIdList;
                if (relationTypeName == null) {
                    relIdList = new ArrayList<String>(allRelIdSet);
                } else {
                    relIdList = new ArrayList<String>();
                    for (String currRelId : allRelIdSet) {
                        String currRelTypeName;
                        synchronized(myRelId2RelTypeMap) {
                            currRelTypeName =
                                myRelId2RelTypeMap.get(currRelId);
                        }
                        if (currRelTypeName.equals(relationTypeName)) {
                            relIdList.add(currRelId);
                        }
                    }
                }
                for (String currRelId : relIdList) {
                    List<String> currRoleNameList =
                        relId2RoleNamesMap.get(currRelId);
                    if (roleName == null) {
                        result.put(currRelId,
                                   new ArrayList<String>(currRoleNameList));
                    }  else if (currRoleNameList.contains(roleName)) {
                        List<String> dummyList = new ArrayList<String>();
                        dummyList.add(roleName);
                        result.put(currRelId, dummyList);
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "findReferencingRelations");
        return result;
    }
    public Map<ObjectName,List<String>>
        findAssociatedMBeans(ObjectName mbeanName,
                             String relationTypeName,
                             String roleName)
            throws IllegalArgumentException {
        if (mbeanName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "findAssociatedMBeans",
                new Object[] {mbeanName, relationTypeName, roleName});
        Map<String,List<String>> relId2RoleNamesMap =
            findReferencingRelations(mbeanName,
                                     relationTypeName,
                                     roleName);
        Map<ObjectName,List<String>> result =
            new HashMap<ObjectName,List<String>>();
        for (String currRelId : relId2RoleNamesMap.keySet()) {
            Map<ObjectName,List<String>> objName2RoleNamesMap;
            try {
                objName2RoleNamesMap = getReferencedMBeans(currRelId);
            } catch (RelationNotFoundException exc) {
                throw new RuntimeException(exc.getMessage());
            }
            for (ObjectName currObjName : objName2RoleNamesMap.keySet()) {
                if (!(currObjName.equals(mbeanName))) {
                    List<String> currRelIdList = result.get(currObjName);
                    if (currRelIdList == null) {
                        currRelIdList = new ArrayList<String>();
                        currRelIdList.add(currRelId);
                        result.put(currObjName, currRelIdList);
                    } else {
                        currRelIdList.add(currRelId);
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "findAssociatedMBeans");
        return result;
    }
    public List<String> findRelationsOfType(String relationTypeName)
        throws IllegalArgumentException,
               RelationTypeNotFoundException {
        if (relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "findRelationsOfType");
        RelationType relType = getRelationType(relationTypeName);
        List<String> result;
        synchronized(myRelType2RelIdsMap) {
            List<String> result1 = myRelType2RelIdsMap.get(relationTypeName);
            if (result1 == null)
                result = new ArrayList<String>();
            else
                result = new ArrayList<String>(result1);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "findRelationsOfType");
        return result;
    }
    public List<ObjectName> getRole(String relationId,
                                    String roleName)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RelationNotFoundException,
               RoleNotFoundException {
        if (relationId == null || roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRole", new Object[] {relationId, roleName});
        isActive();
        Object relObj = getRelation(relationId);
        List<ObjectName> result;
        if (relObj instanceof RelationSupport) {
            result = cast(
                ((RelationSupport)relObj).getRoleInt(roleName,
                                                     true,
                                                     this,
                                                     false));
        } else {
            Object[] params = new Object[1];
            params[0] = roleName;
            String[] signature = new String[1];
            signature[0] = "java.lang.String";
            try {
                List<ObjectName> invokeResult = cast(
                    myMBeanServer.invoke(((ObjectName)relObj),
                                         "getRole",
                                         params,
                                         signature));
                if (invokeResult == null || invokeResult instanceof ArrayList<?>)
                    result = invokeResult;
                else
                    result = new ArrayList<ObjectName>(invokeResult);
            } catch (InstanceNotFoundException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (ReflectionException exc2) {
                throw new RuntimeException(exc2.getMessage());
            } catch (MBeanException exc3) {
                Exception wrappedExc = exc3.getTargetException();
                if (wrappedExc instanceof RoleNotFoundException) {
                    throw ((RoleNotFoundException)wrappedExc);
                } else {
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(), "getRole");
        return result;
    }
    public RoleResult getRoles(String relationId,
                               String[] roleNameArray)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null || roleNameArray == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoles", relationId);
        isActive();
        Object relObj = getRelation(relationId);
        RoleResult result;
        if (relObj instanceof RelationSupport) {
            result = ((RelationSupport)relObj).getRolesInt(roleNameArray,
                                                        true,
                                                        this);
        } else {
            Object[] params = new Object[1];
            params[0] = roleNameArray;
            String[] signature = new String[1];
            try {
                signature[0] = (roleNameArray.getClass()).getName();
            } catch (Exception exc) {
            }
            try {
                result = (RoleResult)
                    (myMBeanServer.invoke(((ObjectName)relObj),
                                          "getRoles",
                                          params,
                                          signature));
            } catch (InstanceNotFoundException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (ReflectionException exc2) {
                throw new RuntimeException(exc2.getMessage());
            } catch (MBeanException exc3) {
                throw new
                    RuntimeException((exc3.getTargetException()).getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(), "getRoles");
        return result;
    }
    public RoleResult getAllRoles(String relationId)
        throws IllegalArgumentException,
               RelationNotFoundException,
               RelationServiceNotRegisteredException {
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoles", relationId);
        Object relObj = getRelation(relationId);
        RoleResult result;
        if (relObj instanceof RelationSupport) {
            result = ((RelationSupport)relObj).getAllRolesInt(true, this);
        } else {
            try {
                result = (RoleResult)
                    (myMBeanServer.getAttribute(((ObjectName)relObj),
                                                "AllRoles"));
            } catch (Exception exc) {
                throw new RuntimeException(exc.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(), "getRoles");
        return result;
    }
    public Integer getRoleCardinality(String relationId,
                                      String roleName)
        throws IllegalArgumentException,
               RelationNotFoundException,
               RoleNotFoundException {
        if (relationId == null || roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRoleCardinality", new Object[] {relationId, roleName});
        Object relObj = getRelation(relationId);
        Integer result;
        if (relObj instanceof RelationSupport) {
            result = ((RelationSupport)relObj).getRoleCardinality(roleName);
        } else {
            Object[] params = new Object[1];
            params[0] = roleName;
            String[] signature = new String[1];
            signature[0] = "java.lang.String";
            try {
                result = (Integer)
                    (myMBeanServer.invoke(((ObjectName)relObj),
                                          "getRoleCardinality",
                                          params,
                                          signature));
            } catch (InstanceNotFoundException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (ReflectionException exc2) {
                throw new RuntimeException(exc2.getMessage());
            } catch (MBeanException exc3) {
                Exception wrappedExc = exc3.getTargetException();
                if (wrappedExc instanceof RoleNotFoundException) {
                    throw ((RoleNotFoundException)wrappedExc);
                } else {
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRoleCardinality");
        return result;
    }
    public void setRole(String relationId,
                        Role role)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RelationNotFoundException,
               RoleNotFoundException,
               InvalidRoleValueException {
        if (relationId == null || role == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "setRole", new Object[] {relationId, role});
        isActive();
        Object relObj = getRelation(relationId);
        if (relObj instanceof RelationSupport) {
            try {
                ((RelationSupport)relObj).setRoleInt(role,
                                                  true,
                                                  this,
                                                  false);
            } catch (RelationTypeNotFoundException exc) {
                throw new RuntimeException(exc.getMessage());
            }
        } else {
            Object[] params = new Object[1];
            params[0] = role;
            String[] signature = new String[1];
            signature[0] = "javax.management.relation.Role";
            try {
                myMBeanServer.setAttribute(((ObjectName)relObj),
                                           new Attribute("Role", role));
            } catch (InstanceNotFoundException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (ReflectionException exc3) {
                throw new RuntimeException(exc3.getMessage());
            } catch (MBeanException exc2) {
                Exception wrappedExc = exc2.getTargetException();
                if (wrappedExc instanceof RoleNotFoundException) {
                    throw ((RoleNotFoundException)wrappedExc);
                } else if (wrappedExc instanceof InvalidRoleValueException) {
                    throw ((InvalidRoleValueException)wrappedExc);
                } else {
                    throw new RuntimeException(wrappedExc.getMessage());
                }
            } catch (AttributeNotFoundException exc4) {
              throw new RuntimeException(exc4.getMessage());
            } catch (InvalidAttributeValueException exc5) {
              throw new RuntimeException(exc5.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(), "setRole");
        return;
    }
    public RoleResult setRoles(String relationId,
                               RoleList roleList)
        throws RelationServiceNotRegisteredException,
               IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null || roleList == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "setRoles", new Object[] {relationId, roleList});
        isActive();
        Object relObj = getRelation(relationId);
        RoleResult result;
        if (relObj instanceof RelationSupport) {
            try {
                result = ((RelationSupport)relObj).setRolesInt(roleList,
                                                            true,
                                                            this);
            } catch (RelationTypeNotFoundException exc) {
                throw new RuntimeException(exc.getMessage());
            }
        } else {
            Object[] params = new Object[1];
            params[0] = roleList;
            String[] signature = new String[1];
            signature[0] = "javax.management.relation.RoleList";
            try {
                result = (RoleResult)
                    (myMBeanServer.invoke(((ObjectName)relObj),
                                          "setRoles",
                                          params,
                                          signature));
            } catch (InstanceNotFoundException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (ReflectionException exc3) {
                throw new RuntimeException(exc3.getMessage());
            } catch (MBeanException exc2) {
                throw new
                    RuntimeException((exc2.getTargetException()).getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(), "setRoles");
        return result;
    }
    public Map<ObjectName,List<String>>
        getReferencedMBeans(String relationId)
            throws IllegalArgumentException,
        RelationNotFoundException {
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getReferencedMBeans", relationId);
        Object relObj = getRelation(relationId);
        Map<ObjectName,List<String>> result;
        if (relObj instanceof RelationSupport) {
            result = ((RelationSupport)relObj).getReferencedMBeans();
        } else {
            try {
                result = cast(
                    myMBeanServer.getAttribute(((ObjectName)relObj),
                                               "ReferencedMBeans"));
            } catch (Exception exc) {
                throw new RuntimeException(exc.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getReferencedMBeans");
        return result;
    }
    public String getRelationTypeName(String relationId)
        throws IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRelationTypeName", relationId);
        Object relObj = getRelation(relationId);
        String result;
        if (relObj instanceof RelationSupport) {
            result = ((RelationSupport)relObj).getRelationTypeName();
        } else {
            try {
                result = (String)
                    (myMBeanServer.getAttribute(((ObjectName)relObj),
                                                "RelationTypeName"));
            } catch (Exception exc) {
                throw new RuntimeException(exc.getMessage());
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRelationTypeName");
        return result;
    }
    public void handleNotification(Notification notif,
                                   Object handback) {
        if (notif == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "handleNotification", notif);
        if (notif instanceof MBeanServerNotification) {
            MBeanServerNotification mbsNtf = (MBeanServerNotification) notif;
            String ntfType = notif.getType();
            if (ntfType.equals(
                       MBeanServerNotification.UNREGISTRATION_NOTIFICATION )) {
                ObjectName mbeanName =
                    ((MBeanServerNotification)notif).getMBeanName();
                boolean isRefedMBeanFlag = false;
                synchronized(myRefedMBeanObjName2RelIdsMap) {
                    if (myRefedMBeanObjName2RelIdsMap.containsKey(mbeanName)) {
                        synchronized(myUnregNtfList) {
                            myUnregNtfList.add(mbsNtf);
                        }
                        isRefedMBeanFlag = true;
                    }
                    if (isRefedMBeanFlag && myPurgeFlag) {
                        try {
                            purgeRelations();
                        } catch (Exception exc) {
                            throw new RuntimeException(exc.getMessage());
                        }
                    }
                }
                String relId;
                synchronized(myRelMBeanObjName2RelIdMap){
                    relId = myRelMBeanObjName2RelIdMap.get(mbeanName);
                }
                if (relId != null) {
                    try {
                        removeRelation(relId);
                    } catch (Exception exc) {
                        throw new RuntimeException(exc.getMessage());
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "handleNotification");
        return;
    }
    public MBeanNotificationInfo[] getNotificationInfo() {
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getNotificationInfo");
        String ntfClass = "javax.management.relation.RelationNotification";
        String[] ntfTypes = new String[] {
            RelationNotification.RELATION_BASIC_CREATION,
            RelationNotification.RELATION_MBEAN_CREATION,
            RelationNotification.RELATION_BASIC_UPDATE,
            RelationNotification.RELATION_MBEAN_UPDATE,
            RelationNotification.RELATION_BASIC_REMOVAL,
            RelationNotification.RELATION_MBEAN_REMOVAL,
        };
        String ntfDesc = "Sent when a relation is created, updated or deleted.";
        MBeanNotificationInfo ntfInfo =
            new MBeanNotificationInfo(ntfTypes, ntfClass, ntfDesc);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getNotificationInfo");
        return new MBeanNotificationInfo[] {ntfInfo};
    }
    private void addRelationTypeInt(RelationType relationTypeObj)
        throws IllegalArgumentException,
               InvalidRelationTypeException {
        if (relationTypeObj == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelationTypeInt");
        String relTypeName = relationTypeObj.getRelationTypeName();
        try {
            RelationType relType = getRelationType(relTypeName);
            if (relType != null) {
                String excMsg = "There is already a relation type in the Relation Service with name ";
                StringBuilder excMsgStrB = new StringBuilder(excMsg);
                excMsgStrB.append(relTypeName);
                throw new InvalidRelationTypeException(excMsgStrB.toString());
            }
        } catch (RelationTypeNotFoundException exc) {
        }
        synchronized(myRelType2ObjMap) {
            myRelType2ObjMap.put(relTypeName, relationTypeObj);
        }
        if (relationTypeObj instanceof RelationTypeSupport) {
            ((RelationTypeSupport)relationTypeObj).setRelationServiceFlag(true);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelationTypeInt");
        return;
     }
    RelationType getRelationType(String relationTypeName)
        throws IllegalArgumentException,
               RelationTypeNotFoundException {
        if (relationTypeName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRelationType", relationTypeName);
        RelationType relType;
        synchronized(myRelType2ObjMap) {
            relType = (myRelType2ObjMap.get(relationTypeName));
        }
        if (relType == null) {
            String excMsg = "No relation type created in the Relation Service with the name ";
            StringBuilder excMsgStrB = new StringBuilder(excMsg);
            excMsgStrB.append(relationTypeName);
            throw new RelationTypeNotFoundException(excMsgStrB.toString());
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRelationType");
        return relType;
    }
    Object getRelation(String relationId)
        throws IllegalArgumentException,
               RelationNotFoundException {
        if (relationId == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "getRelation", relationId);
        Object rel;
        synchronized(myRelId2ObjMap) {
            rel = myRelId2ObjMap.get(relationId);
        }
        if (rel == null) {
            String excMsg = "No relation associated to relation id " + relationId;
            throw new RelationNotFoundException(excMsg);
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "getRelation");
        return rel;
    }
    private boolean addNewMBeanReference(ObjectName objectName,
                                         String relationId,
                                         String roleName)
        throws IllegalArgumentException {
        if (objectName == null ||
            relationId == null ||
            roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addNewMBeanReference",
                new Object[] {objectName, relationId, roleName});
        boolean isNewFlag = false;
        synchronized(myRefedMBeanObjName2RelIdsMap) {
            Map<String,List<String>> mbeanRefMap =
                myRefedMBeanObjName2RelIdsMap.get(objectName);
            if (mbeanRefMap == null) {
                isNewFlag = true;
                List<String> roleNames = new ArrayList<String>();
                roleNames.add(roleName);
                mbeanRefMap = new HashMap<String,List<String>>();
                mbeanRefMap.put(relationId, roleNames);
                myRefedMBeanObjName2RelIdsMap.put(objectName, mbeanRefMap);
            } else {
                List<String> roleNames = mbeanRefMap.get(relationId);
                if (roleNames == null) {
                    roleNames = new ArrayList<String>();
                    roleNames.add(roleName);
                    mbeanRefMap.put(relationId, roleNames);
                } else {
                    roleNames.add(roleName);
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addNewMBeanReference");
        return isNewFlag;
    }
    private boolean removeMBeanReference(ObjectName objectName,
                                         String relationId,
                                         String roleName,
                                         boolean allRolesFlag)
        throws IllegalArgumentException {
        if (objectName == null ||
            relationId == null ||
            roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "removeMBeanReference",
                new Object[] {objectName, relationId, roleName, allRolesFlag});
        boolean noLongerRefFlag = false;
        synchronized(myRefedMBeanObjName2RelIdsMap) {
            Map<String,List<String>> mbeanRefMap =
                (myRefedMBeanObjName2RelIdsMap.get(objectName));
            if (mbeanRefMap == null) {
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "removeMBeanReference");
                return true;
            }
            List<String> roleNames = null;
            if (!allRolesFlag) {
                roleNames = mbeanRefMap.get(relationId);
                int obsRefIdx = roleNames.indexOf(roleName);
                if (obsRefIdx != -1) {
                    roleNames.remove(obsRefIdx);
                }
            }
            if (roleNames.isEmpty() || allRolesFlag) {
                mbeanRefMap.remove(relationId);
            }
            if (mbeanRefMap.isEmpty()) {
                myRefedMBeanObjName2RelIdsMap.remove(objectName);
                noLongerRefFlag = true;
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "removeMBeanReference");
        return noLongerRefFlag;
    }
    private void updateUnregistrationListener(List<ObjectName> newRefList,
                                              List<ObjectName> obsoleteRefList)
        throws RelationServiceNotRegisteredException {
        if (newRefList != null && obsoleteRefList != null) {
            if (newRefList.isEmpty() && obsoleteRefList.isEmpty()) {
                return;
            }
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "updateUnregistrationListener",
                new Object[] {newRefList, obsoleteRefList});
        isActive();
        if (newRefList != null || obsoleteRefList != null) {
            boolean newListenerFlag = false;
            if (myUnregNtfFilter == null) {
                myUnregNtfFilter = new MBeanServerNotificationFilter();
                newListenerFlag = true;
            }
            synchronized(myUnregNtfFilter) {
                if (newRefList != null) {
                    for (ObjectName newObjName : newRefList)
                        myUnregNtfFilter.enableObjectName(newObjName);
                }
                if (obsoleteRefList != null) {
                    for (ObjectName obsObjName : obsoleteRefList)
                        myUnregNtfFilter.disableObjectName(obsObjName);
                }
                if (newListenerFlag) {
                    try {
                        myMBeanServer.addNotificationListener(
                                MBeanServerDelegate.DELEGATE_NAME,
                                this,
                                myUnregNtfFilter,
                                null);
                    } catch (InstanceNotFoundException exc) {
                        throw new
                       RelationServiceNotRegisteredException(exc.getMessage());
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "updateUnregistrationListener");
        return;
    }
    private void addRelationInt(boolean relationBaseFlag,
                                RelationSupport relationObj,
                                ObjectName relationObjName,
                                String relationId,
                                String relationTypeName,
                                RoleList roleList)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RoleNotFoundException,
               InvalidRelationIdException,
               RelationTypeNotFoundException,
               InvalidRoleValueException {
        if (relationId == null ||
            relationTypeName == null ||
            (relationBaseFlag &&
             (relationObj == null ||
              relationObjName != null)) ||
            (!relationBaseFlag &&
             (relationObjName == null ||
              relationObj != null))) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "addRelationInt", new Object[] {relationBaseFlag, relationObj,
                relationObjName, relationId, relationTypeName, roleList});
        isActive();
        try {
            Object rel = getRelation(relationId);
            if (rel != null) {
                String excMsg = "There is already a relation with id ";
                StringBuilder excMsgStrB = new StringBuilder(excMsg);
                excMsgStrB.append(relationId);
                throw new InvalidRelationIdException(excMsgStrB.toString());
            }
        } catch (RelationNotFoundException exc) {
        }
        RelationType relType = getRelationType(relationTypeName);
        List<RoleInfo> roleInfoList = new ArrayList<RoleInfo>(relType.getRoleInfos());
        if (roleList != null) {
            for (Role currRole : roleList.asList()) {
                String currRoleName = currRole.getRoleName();
                List<ObjectName> currRoleValue = currRole.getRoleValue();
                RoleInfo roleInfo;
                try {
                    roleInfo = relType.getRoleInfo(currRoleName);
                } catch (RoleInfoNotFoundException exc) {
                    throw new RoleNotFoundException(exc.getMessage());
                }
                Integer status = checkRoleInt(2,
                                              currRoleName,
                                              currRoleValue,
                                              roleInfo,
                                              false);
                int pbType = status.intValue();
                if (pbType != 0) {
                    throwRoleProblemException(pbType, currRoleName);
                }
                int roleInfoIdx = roleInfoList.indexOf(roleInfo);
                roleInfoList.remove(roleInfoIdx);
            }
        }
        initializeMissingRoles(relationBaseFlag,
                               relationObj,
                               relationObjName,
                               relationId,
                               relationTypeName,
                               roleInfoList);
        synchronized(myRelId2ObjMap) {
            if (relationBaseFlag) {
                myRelId2ObjMap.put(relationId, relationObj);
            } else {
                myRelId2ObjMap.put(relationId, relationObjName);
            }
        }
        synchronized(myRelId2RelTypeMap) {
            myRelId2RelTypeMap.put(relationId,
                                   relationTypeName);
        }
        synchronized(myRelType2RelIdsMap) {
            List<String> relIdList =
                myRelType2RelIdsMap.get(relationTypeName);
            boolean firstRelFlag = false;
            if (relIdList == null) {
                firstRelFlag = true;
                relIdList = new ArrayList<String>();
            }
            relIdList.add(relationId);
            if (firstRelFlag) {
                myRelType2RelIdsMap.put(relationTypeName, relIdList);
            }
        }
        for (Role currRole : roleList.asList()) {
            List<ObjectName> dummyList = new ArrayList<ObjectName>();
            try {
                updateRoleMap(relationId, currRole, dummyList);
            } catch (RelationNotFoundException exc) {
            }
        }
        try {
            sendRelationCreationNotification(relationId);
        } catch (RelationNotFoundException exc) {
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "addRelationInt");
        return;
    }
    private Integer checkRoleInt(int chkType,
                                 String roleName,
                                 List<ObjectName> roleValue,
                                 RoleInfo roleInfo,
                                 boolean writeChkFlag)
        throws IllegalArgumentException {
        if (roleName == null ||
            roleInfo == null ||
            (chkType == 2 && roleValue == null)) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "checkRoleInt", new Object[] {chkType, roleName,
                roleValue, roleInfo, writeChkFlag});
        String expName = roleInfo.getName();
        if (!(roleName.equals(expName))) {
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleInt");
            return Integer.valueOf(RoleStatus.NO_ROLE_WITH_NAME);
        }
        if (chkType == 1) {
            boolean isReadable = roleInfo.isReadable();
            if (!isReadable) {
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return Integer.valueOf(RoleStatus.ROLE_NOT_READABLE);
            } else {
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(0);
            }
        }
        if (writeChkFlag) {
            boolean isWritable = roleInfo.isWritable();
            if (!isWritable) {
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(RoleStatus.ROLE_NOT_WRITABLE);
            }
        }
        int refNbr = roleValue.size();
        boolean chkMinFlag = roleInfo.checkMinDegree(refNbr);
        if (!chkMinFlag) {
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleInt");
            return new Integer(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE);
        }
        boolean chkMaxFlag = roleInfo.checkMaxDegree(refNbr);
        if (!chkMaxFlag) {
            RELATION_LOGGER.exiting(RelationService.class.getName(),
                    "checkRoleInt");
            return new Integer(RoleStatus.MORE_THAN_MAX_ROLE_DEGREE);
        }
        String expClassName = roleInfo.getRefMBeanClassName();
        for (ObjectName currObjName : roleValue) {
            if (currObjName == null) {
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(RoleStatus.REF_MBEAN_NOT_REGISTERED);
            }
            try {
                boolean classSts = myMBeanServer.isInstanceOf(currObjName,
                                                              expClassName);
                if (!classSts) {
                    RELATION_LOGGER.exiting(RelationService.class.getName(),
                            "checkRoleInt");
                    return new Integer(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS);
                }
            } catch (InstanceNotFoundException exc) {
                RELATION_LOGGER.exiting(RelationService.class.getName(),
                        "checkRoleInt");
                return new Integer(RoleStatus.REF_MBEAN_NOT_REGISTERED);
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "checkRoleInt");
        return new Integer(0);
    }
    private void initializeMissingRoles(boolean relationBaseFlag,
                                        RelationSupport relationObj,
                                        ObjectName relationObjName,
                                        String relationId,
                                        String relationTypeName,
                                        List<RoleInfo> roleInfoList)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               InvalidRoleValueException {
        if ((relationBaseFlag &&
             (relationObj == null ||
              relationObjName != null)) ||
            (!relationBaseFlag &&
             (relationObjName == null ||
              relationObj != null)) ||
            relationId == null ||
            relationTypeName == null ||
            roleInfoList == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "initializeMissingRoles", new Object[] {relationBaseFlag,
                relationObj, relationObjName, relationId, relationTypeName,
                roleInfoList});
        isActive();
        for (RoleInfo currRoleInfo : roleInfoList) {
            String roleName = currRoleInfo.getName();
            List<ObjectName> emptyValue = new ArrayList<ObjectName>();
            Role role = new Role(roleName, emptyValue);
            if (relationBaseFlag) {
                try {
                    relationObj.setRoleInt(role, true, this, false);
                } catch (RoleNotFoundException exc1) {
                    throw new RuntimeException(exc1.getMessage());
                } catch (RelationNotFoundException exc2) {
                    throw new RuntimeException(exc2.getMessage());
                } catch (RelationTypeNotFoundException exc3) {
                    throw new RuntimeException(exc3.getMessage());
                }
            } else {
                Object[] params = new Object[1];
                params[0] = role;
                String[] signature = new String[1];
                signature[0] = "javax.management.relation.Role";
                try {
                    myMBeanServer.setAttribute(relationObjName,
                                               new Attribute("Role", role));
                } catch (InstanceNotFoundException exc1) {
                    throw new RuntimeException(exc1.getMessage());
                } catch (ReflectionException exc3) {
                    throw new RuntimeException(exc3.getMessage());
                } catch (MBeanException exc2) {
                    Exception wrappedExc = exc2.getTargetException();
                    if (wrappedExc instanceof InvalidRoleValueException) {
                        throw ((InvalidRoleValueException)wrappedExc);
                    } else {
                        throw new RuntimeException(wrappedExc.getMessage());
                    }
                } catch (AttributeNotFoundException exc4) {
                  throw new RuntimeException(exc4.getMessage());
                } catch (InvalidAttributeValueException exc5) {
                  throw new RuntimeException(exc5.getMessage());
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "initializeMissingRoles");
        return;
    }
    static void throwRoleProblemException(int pbType,
                                          String roleName)
        throws IllegalArgumentException,
               RoleNotFoundException,
               InvalidRoleValueException {
        if (roleName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        int excType = 0;
        String excMsgPart = null;
        switch (pbType) {
        case RoleStatus.NO_ROLE_WITH_NAME:
            excMsgPart = " does not exist in relation.";
            excType = 1;
            break;
        case RoleStatus.ROLE_NOT_READABLE:
            excMsgPart = " is not readable.";
            excType = 1;
            break;
        case RoleStatus.ROLE_NOT_WRITABLE:
            excMsgPart = " is not writable.";
            excType = 1;
            break;
        case RoleStatus.LESS_THAN_MIN_ROLE_DEGREE:
            excMsgPart = " has a number of MBean references less than the expected minimum degree.";
            excType = 2;
            break;
        case RoleStatus.MORE_THAN_MAX_ROLE_DEGREE:
            excMsgPart = " has a number of MBean references greater than the expected maximum degree.";
            excType = 2;
            break;
        case RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS:
            excMsgPart = " has an MBean reference to an MBean not of the expected class of references for that role.";
            excType = 2;
            break;
        case RoleStatus.REF_MBEAN_NOT_REGISTERED:
            excMsgPart = " has a reference to null or to an MBean not registered.";
            excType = 2;
            break;
        }
        StringBuilder excMsgStrB = new StringBuilder(roleName);
        excMsgStrB.append(excMsgPart);
        String excMsg = excMsgStrB.toString();
        if (excType == 1) {
            throw new RoleNotFoundException(excMsg);
        } else if (excType == 2) {
            throw new InvalidRoleValueException(excMsg);
        }
    }
    private void sendNotificationInt(int intNtfType,
                                     String message,
                                     String relationId,
                                     List<ObjectName> unregMBeanList,
                                     String roleName,
                                     List<ObjectName> roleNewValue,
                                     List<ObjectName> oldValue)
        throws IllegalArgumentException,
               RelationNotFoundException {
        if (message == null ||
            relationId == null ||
            (intNtfType != 3 && unregMBeanList != null) ||
            (intNtfType == 2 &&
             (roleName == null ||
              roleNewValue == null ||
              oldValue == null))) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "sendNotificationInt", new Object[] {intNtfType, message,
                relationId, unregMBeanList, roleName, roleNewValue, oldValue});
        String relTypeName;
        synchronized(myRelId2RelTypeMap) {
            relTypeName = (myRelId2RelTypeMap.get(relationId));
        }
        ObjectName relObjName = isRelationMBean(relationId);
        String ntfType = null;
        if (relObjName != null) {
            switch (intNtfType) {
            case 1:
                ntfType = RelationNotification.RELATION_MBEAN_CREATION;
                break;
            case 2:
                ntfType = RelationNotification.RELATION_MBEAN_UPDATE;
                break;
            case 3:
                ntfType = RelationNotification.RELATION_MBEAN_REMOVAL;
                break;
            }
        } else {
            switch (intNtfType) {
            case 1:
                ntfType = RelationNotification.RELATION_BASIC_CREATION;
                break;
            case 2:
                ntfType = RelationNotification.RELATION_BASIC_UPDATE;
                break;
            case 3:
                ntfType = RelationNotification.RELATION_BASIC_REMOVAL;
                break;
            }
        }
        Long seqNo = atomicSeqNo.incrementAndGet();
        Date currDate = new Date();
        long timeStamp = currDate.getTime();
        RelationNotification ntf = null;
        if (ntfType.equals(RelationNotification.RELATION_BASIC_CREATION) ||
            ntfType.equals(RelationNotification.RELATION_MBEAN_CREATION) ||
            ntfType.equals(RelationNotification.RELATION_BASIC_REMOVAL) ||
            ntfType.equals(RelationNotification.RELATION_MBEAN_REMOVAL))
            ntf = new RelationNotification(ntfType,
                                           this,
                                           seqNo.longValue(),
                                           timeStamp,
                                           message,
                                           relationId,
                                           relTypeName,
                                           relObjName,
                                           unregMBeanList);
        else if (ntfType.equals(RelationNotification.RELATION_BASIC_UPDATE)
                 ||
                 ntfType.equals(RelationNotification.RELATION_MBEAN_UPDATE))
            {
                ntf = new RelationNotification(ntfType,
                                               this,
                                               seqNo.longValue(),
                                               timeStamp,
                                               message,
                                               relationId,
                                               relTypeName,
                                               relObjName,
                                               roleName,
                                               roleNewValue,
                                               oldValue);
            }
        sendNotification(ntf);
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "sendNotificationInt");
        return;
    }
    private void handleReferenceUnregistration(String relationId,
                                               ObjectName objectName,
                                               List<String> roleNameList)
        throws IllegalArgumentException,
               RelationServiceNotRegisteredException,
               RelationNotFoundException,
               RoleNotFoundException {
        if (relationId == null ||
            roleNameList == null ||
            objectName == null) {
            String excMsg = "Invalid parameter.";
            throw new IllegalArgumentException(excMsg);
        }
        RELATION_LOGGER.entering(RelationService.class.getName(),
                "handleReferenceUnregistration",
                new Object[] {relationId, objectName, roleNameList});
        isActive();
        String currRelTypeName = getRelationTypeName(relationId);
        Object relObj = getRelation(relationId);
        boolean deleteRelFlag = false;
        for (String currRoleName : roleNameList) {
            if (deleteRelFlag) {
                break;
            }
            int currRoleRefNbr =
                (getRoleCardinality(relationId, currRoleName)).intValue();
            int currRoleNewRefNbr = currRoleRefNbr - 1;
            RoleInfo currRoleInfo;
            try {
                currRoleInfo = getRoleInfo(currRelTypeName,
                                           currRoleName);
            } catch (RelationTypeNotFoundException exc1) {
                throw new RuntimeException(exc1.getMessage());
            } catch (RoleInfoNotFoundException exc2) {
                throw new RuntimeException(exc2.getMessage());
            }
            boolean chkMinFlag = currRoleInfo.checkMinDegree(currRoleNewRefNbr);
            if (!chkMinFlag) {
                deleteRelFlag = true;
            }
        }
        if (deleteRelFlag) {
            removeRelation(relationId);
        } else {
            for (String currRoleName : roleNameList) {
                if (relObj instanceof RelationSupport) {
                    try {
                        ((RelationSupport)relObj).handleMBeanUnregistrationInt(
                                                  objectName,
                                                  currRoleName,
                                                  true,
                                                  this);
                    } catch (RelationTypeNotFoundException exc3) {
                        throw new RuntimeException(exc3.getMessage());
                    } catch (InvalidRoleValueException exc4) {
                        throw new RuntimeException(exc4.getMessage());
                    }
                } else {
                    Object[] params = new Object[2];
                    params[0] = objectName;
                    params[1] = currRoleName;
                    String[] signature = new String[2];
                    signature[0] = "javax.management.ObjectName";
                    signature[1] = "java.lang.String";
                    try {
                        myMBeanServer.invoke(((ObjectName)relObj),
                                             "handleMBeanUnregistration",
                                             params,
                                             signature);
                    } catch (InstanceNotFoundException exc1) {
                        throw new RuntimeException(exc1.getMessage());
                    } catch (ReflectionException exc3) {
                        throw new RuntimeException(exc3.getMessage());
                    } catch (MBeanException exc2) {
                        Exception wrappedExc = exc2.getTargetException();
                        throw new RuntimeException(wrappedExc.getMessage());
                    }
                }
            }
        }
        RELATION_LOGGER.exiting(RelationService.class.getName(),
                "handleReferenceUnregistration");
        return;
    }
}
