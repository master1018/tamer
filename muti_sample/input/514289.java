public class APIComparator {
    public APIDiff apiDiff;
    public PackageDiff pkgDiff;
    public APIComparator() {
        apiDiff = new APIDiff();
    }   
    private static API oldAPI_;
    private static API newAPI_;
    public void compareAPIs(API oldAPI, API newAPI) {
        System.out.println("JDiff: comparing the old and new APIs ...");
        oldAPI_ = oldAPI;
        newAPI_ = newAPI;
        double differs = 0.0;
        apiDiff.oldAPIName_ = oldAPI.name_;
        apiDiff.newAPIName_ = newAPI.name_;
        Collections.sort(oldAPI.packages_);
        Collections.sort(newAPI.packages_);
        Iterator iter = oldAPI.packages_.iterator();
        while (iter.hasNext()) {
            PackageAPI oldPkg = (PackageAPI)(iter.next());
            int idx = Collections.binarySearch(newAPI.packages_, oldPkg);
            if (idx < 0) {
                int existsNew = newAPI.packages_.indexOf(oldPkg);
                if (existsNew != -1) {
                    differs += 2.0 * comparePackages(oldPkg, (PackageAPI)(newAPI.packages_.get(existsNew)));
                }  else {
                    if (trace)
                        System.out.println("Package " + oldPkg.name_ + " was removed");
                    apiDiff.packagesRemoved.add(oldPkg);
                    differs += 1.0;
                }
            } else {
                differs += 2.0 * comparePackages(oldPkg, (PackageAPI)(newAPI.packages_.get(idx)));
            }
        } 
        iter = newAPI.packages_.iterator();
        while (iter.hasNext()) {
            PackageAPI newPkg = (PackageAPI)(iter.next());
            int idx = Collections.binarySearch(oldAPI.packages_, newPkg);
            if (idx < 0) {
                int existsOld = oldAPI.packages_.indexOf(newPkg);
                if (existsOld != -1) {
                } else {
                    if (trace)
                        System.out.println("Package " + newPkg.name_ + " was added");
                    apiDiff.packagesAdded.add(newPkg);
                    differs += 1.0;
                }
            } else {
            }
        } 
        MergeChanges.mergeRemoveAdd(apiDiff);
        Long denom = new Long(oldAPI.packages_.size() + newAPI.packages_.size());
        if (denom.intValue() == 0) {
            System.out.println("Error: no packages found in the APIs.");
            return;
        }
        if (trace)
            System.out.println("Top level changes: " + differs + "/" + denom.intValue());
        differs = (100.0 * differs)/denom.doubleValue();
        apiDiff.pdiff = differs;
        Double percentage = new Double(differs);
        int approxPercentage = percentage.intValue();
        if (approxPercentage == 0)
            System.out.println(" Approximately " + percentage + "% difference between the APIs");
        else
            System.out.println(" Approximately " + approxPercentage + "% difference between the APIs");
        Diff.closeDiffFile();
    }   
    public double comparePackages(PackageAPI oldPkg, PackageAPI newPkg) {
        if (trace)
            System.out.println("Comparing old package " + oldPkg.name_ + 
                               " and new package " + newPkg.name_);
        pkgDiff = new PackageDiff(oldPkg.name_);
        double differs = 0.0;
        Collections.sort(oldPkg.classes_);
        Collections.sort(newPkg.classes_);
        Iterator iter = oldPkg.classes_.iterator();
        while (iter.hasNext()) {
            ClassAPI oldClass = (ClassAPI)(iter.next());
            int idx = Collections.binarySearch(newPkg.classes_, oldClass);
            if (idx < 0) {
                int existsNew = newPkg.classes_.indexOf(oldClass);
                if (existsNew != -1) {
                    differs += 2.0 * compareClasses(oldClass, (ClassAPI)(newPkg.classes_.get(existsNew)), pkgDiff);
                }  else {
                    if (trace)
                        System.out.println("  Class " + oldClass.name_ + " was removed");
                    pkgDiff.classesRemoved.add(oldClass);
                    differs += 1.0;
                }
            } else {
                differs += 2.0 * compareClasses(oldClass, (ClassAPI)(newPkg.classes_.get(idx)), pkgDiff);
            }
        } 
        iter = newPkg.classes_.iterator();
        while (iter.hasNext()) {
            ClassAPI newClass = (ClassAPI)(iter.next());
            int idx = Collections.binarySearch(oldPkg.classes_, newClass);
            if (idx < 0) {
                int existsOld = oldPkg.classes_.indexOf(newClass);
                if (existsOld != -1) {
                } else {
                    if (trace)
                        System.out.println("  Class " + newClass.name_ + " was added");
                    pkgDiff.classesAdded.add(newClass);
                    differs += 1.0;
                }
            } else {
            }
        } 
        boolean differsFlag = false;
        if (docChanged(oldPkg.doc_, newPkg.doc_)) {
            String link = "<a href=\"pkg_" + oldPkg.name_ + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
            String id = oldPkg.name_ + "!package";
            String title = link + "Package <b>" + oldPkg.name_ + "</b></a>";
            pkgDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, null, oldPkg.doc_, newPkg.doc_, id, title);
            differsFlag = true;
        }
        if (differs != 0.0 || differsFlag) 
            apiDiff.packagesChanged.add(pkgDiff);
        Long denom = new Long(oldPkg.classes_.size() + newPkg.classes_.size());
        if (denom.intValue() == 0) {
            System.out.println("Warning: no classes found in the package " + oldPkg.name_);
            return 0.0;
        }
        if (trace)
            System.out.println("Package " + pkgDiff.name_ + " had a difference of " + differs + "/" + denom.intValue());
        pkgDiff.pdiff = 100.0 * differs/denom.doubleValue();
        return differs/denom.doubleValue();
    } 
    public double compareClasses(ClassAPI oldClass, ClassAPI newClass, PackageDiff pkgDiff) {
        if (trace)
            System.out.println("  Comparing old class " + oldClass.name_ + 
                               " and new class " + newClass.name_);
        boolean differsFlag = false;
        double differs = 0.0;
        ClassDiff classDiff = new ClassDiff(oldClass.name_);
        classDiff.isInterface_ = newClass.isInterface_; 
        if (oldClass.isInterface_ != newClass.isInterface_) {
            classDiff.modifiersChange_  = "Changed from ";
            if (oldClass.isInterface_)
                classDiff.modifiersChange_ += "an interface to a class.";
            else
                classDiff.modifiersChange_ += "a class to an interface.";
            differsFlag = true;
        }
        String inheritanceChange = ClassDiff.diff(oldClass, newClass);
        if (inheritanceChange != null) {
            classDiff.inheritanceChange_ = inheritanceChange;
            differsFlag = true;
        }
        if (oldClass.isAbstract_ != newClass.isAbstract_) {
            String changeText = "";
            if (oldClass.isAbstract_)
                changeText += "Changed from abstract to non-abstract.";
            else
                changeText += "Changed from non-abstract to abstract.";
            classDiff.addModifiersChange(changeText);
            differsFlag = true;
        }
        if (docChanged(oldClass.doc_, newClass.doc_)) {
            String fqName = pkgDiff.name_ + "." + classDiff.name_;
            String link = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
            String id = pkgDiff.name_ + "." + classDiff.name_ + "!class";
            String title = link + "Class <b>" + classDiff.name_ + "</b></a>";
            classDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_,
 classDiff.name_, oldClass.doc_, newClass.doc_, id, title);
            differsFlag = true;
        }
        String modifiersChange = oldClass.modifiers_.diff(newClass.modifiers_);
        if (modifiersChange != null) {
            differsFlag = true;
            if (modifiersChange.indexOf("Change from deprecated to undeprecated") != -1) {
                System.out.println("JDiff: warning: change from deprecated to undeprecated for class " + pkgDiff.name_ + "." + newClass.name_);
            }
        }
        classDiff.addModifiersChange(modifiersChange);
        boolean differsCtors = 
            compareAllCtors(oldClass, newClass, classDiff);
        boolean differsMethods = 
            compareAllMethods(oldClass, newClass, classDiff);
        boolean differsFields = 
            compareAllFields(oldClass, newClass, classDiff);
        if (differsCtors || differsMethods || differsFields) 
            differsFlag = true;
        if (trace) {
            System.out.println("  Ctors differ? " + differsCtors + 
                ", Methods differ? " + differsMethods + 
                ", Fields differ? " + differsFields);
        }
        if (differsFlag) 
            pkgDiff.classesChanged.add(classDiff);
         differs = 
            classDiff.ctorsRemoved.size() + classDiff.ctorsAdded.size() +
            classDiff.ctorsChanged.size() +
            classDiff.methodsRemoved.size() + classDiff.methodsAdded.size() +
            classDiff.methodsChanged.size() +
            classDiff.fieldsRemoved.size() + classDiff.fieldsAdded.size() +
            classDiff.fieldsChanged.size();
         Long denom = new Long(
             oldClass.ctors_.size() + 
             numLocalMethods(oldClass.methods_) + 
             numLocalFields(oldClass.fields_) +
             newClass.ctors_.size() + 
             numLocalMethods(newClass.methods_) + 
             numLocalFields(newClass.fields_));
         if (denom.intValue() == 0) {
             if (differsFlag) {
                 classDiff.pdiff = 0.0; 
                 return 1.0;
             } else {
                 return 0.0;
             }
         }
         if (differsFlag && differs == 0.0) {
             differs = 1.0;
         }
         if (trace)
             System.out.println("  Class " + classDiff.name_ + " had a difference of " + differs + "/" + denom.intValue());
         classDiff.pdiff = 100.0 * differs/denom.doubleValue();
         return differs/denom.doubleValue();
    } 
    public boolean compareAllCtors(ClassAPI oldClass, ClassAPI newClass, 
                                   ClassDiff classDiff) {
        if (trace)
            System.out.println("    Comparing constructors: #old " + 
              oldClass.ctors_.size() + ", #new " + newClass.ctors_.size());
        boolean differs = false;
        boolean singleCtor = false; 
        Collections.sort(oldClass.ctors_);
        Collections.sort(newClass.ctors_);
        Iterator iter = oldClass.ctors_.iterator();
        while (iter.hasNext()) {
            ConstructorAPI oldCtor = (ConstructorAPI)(iter.next());
            int idx = Collections.binarySearch(newClass.ctors_, oldCtor);
            if (idx < 0) {
                int oldSize = oldClass.ctors_.size();
                int newSize = newClass.ctors_.size();
                if (oldSize == 1 && oldSize == newSize) {
                    MemberDiff memberDiff = new MemberDiff(oldClass.name_);
                    memberDiff.oldType_ = oldCtor.type_;
                    memberDiff.oldExceptions_ = oldCtor.exceptions_;
                    ConstructorAPI newCtor  = (ConstructorAPI)(newClass.ctors_.get(0));
                    memberDiff.newType_ = newCtor.type_;
                    memberDiff.newExceptions_ = newCtor.exceptions_;
                    if (docChanged(oldCtor.doc_, newCtor.doc_)) {
                        String type = memberDiff.newType_;
                        if (type.compareTo("void") == 0)
                            type = "";
                        String fqName = pkgDiff.name_ + "." + classDiff.name_;
                        String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
                        String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + ".ctor_changed(" + type + ")\" class=\"hiddenlink\">";
                        String id = pkgDiff.name_ + "." + classDiff.name_ + ".ctor(" + HTMLReportGenerator.simpleName(type) + ")";
                        String title = link1 + "Class <b>" + classDiff.name_ + 
                            "</b></a>, " + link2 + "constructor <b>" + classDiff.name_ + "(" + HTMLReportGenerator.simpleName(type) + ")</b></a>";
                        memberDiff.documentationChange_ = Diff.saveDocDiffs(
                            pkgDiff.name_, classDiff.name_, oldCtor.doc_, newCtor.doc_, id, title);
                    }
                    String modifiersChange = oldCtor.modifiers_.diff(newCtor.modifiers_);
                    if (modifiersChange != null && modifiersChange.indexOf("Change from deprecated to undeprecated") != -1) {
                        System.out.println("JDiff: warning: change from deprecated to undeprecated for a constructor in class" + newClass.name_);
                    }
                    memberDiff.addModifiersChange(modifiersChange);
                    if (trace)
                        System.out.println("    The single constructor was changed");
                    classDiff.ctorsChanged.add(memberDiff);
                    singleCtor = true;
                } else {
                    if (trace)
                        System.out.println("    Constructor " + oldClass.name_ + " was removed");
                    classDiff.ctorsRemoved.add(oldCtor);
                }
                differs = true;
            }
        } 
        iter = newClass.ctors_.iterator();
        while (iter.hasNext()) {
            ConstructorAPI newCtor = (ConstructorAPI)(iter.next());
            int idx = Collections.binarySearch(oldClass.ctors_, newCtor);
            if (idx < 0) {
                if (!singleCtor) {
                    if (trace)
                        System.out.println("    Constructor " + oldClass.name_ + " was added");
                    classDiff.ctorsAdded.add(newCtor);
                    differs = true;
                }
            }
        } 
        return differs;
    } 
    public boolean compareAllMethods(ClassAPI oldClass, ClassAPI newClass, ClassDiff classDiff) {
        if (trace)
            System.out.println("    Comparing methods: #old " + 
                               oldClass.methods_.size() + ", #new " +
                               newClass.methods_.size());
        boolean differs = false;
        Collections.sort(oldClass.methods_);
        Collections.sort(newClass.methods_);
        Iterator iter = oldClass.methods_.iterator();
        while (iter.hasNext()) {
            MethodAPI oldMethod = (MethodAPI)(iter.next());
            int idx = -1;
            MethodAPI[] methodArr = new MethodAPI[newClass.methods_.size()];
            methodArr = (MethodAPI[])newClass.methods_.toArray(methodArr);
            for (int methodIdx = 0; methodIdx < methodArr.length; methodIdx++) {
                MethodAPI newMethod = methodArr[methodIdx];
                if (oldMethod.compareTo(newMethod) == 0) {
                    idx  = methodIdx;
                    break;
                }
            }
            if (idx < 0) {
                int startOld = oldClass.methods_.indexOf(oldMethod); 
                int endOld = oldClass.methods_.lastIndexOf(oldMethod);
                int startNew = newClass.methods_.indexOf(oldMethod); 
                int endNew = newClass.methods_.lastIndexOf(oldMethod);
                if (startOld != -1 && startOld == endOld && 
                    startNew != -1 && startNew == endNew) {
                    MethodAPI newMethod = (MethodAPI)(newClass.methods_.get(startNew));
                    if (oldMethod.inheritedFrom_ == null || 
                        newMethod.inheritedFrom_ == null) {
                        compareMethods(oldMethod, newMethod, classDiff);
                        differs = true;
                    }
                } else if (oldMethod.inheritedFrom_ == null) {
                    if (trace)
                        System.out.println("    Method " + oldMethod.name_ + 
                                           "(" + oldMethod.getSignature() + 
                                           ") was removed");
                    classDiff.methodsRemoved.add(oldMethod);
                    differs = true;
                }
            }
        } 
        iter = newClass.methods_.iterator();
        while (iter.hasNext()) {
            MethodAPI newMethod = (MethodAPI)(iter.next());
            if (newMethod.inheritedFrom_ != null)
                continue;
            int idx = -1;
            MethodAPI[] methodArr = new MethodAPI[oldClass.methods_.size()];
            methodArr = (MethodAPI[])oldClass.methods_.toArray(methodArr);
            for (int methodIdx = 0; methodIdx < methodArr.length; methodIdx++) {
                MethodAPI oldMethod = methodArr[methodIdx];
                if (newMethod.compareTo(oldMethod) == 0) {
                    idx  = methodIdx;
                    break;
                }
            }
            if (idx < 0) {
                int startOld = oldClass.methods_.indexOf(newMethod); 
                int endOld = oldClass.methods_.lastIndexOf(newMethod);
                int startNew = newClass.methods_.indexOf(newMethod); 
                int endNew = newClass.methods_.lastIndexOf(newMethod);
                if (startOld != -1 && startOld == endOld && 
                    startNew != -1 && startNew == endNew) {
                } else {
                    if (trace)
                        System.out.println("    Method " + newMethod.name_ + 
                                           "(" + newMethod.getSignature() + ") was added");
                    classDiff.methodsAdded.add(newMethod);
                    differs = true;
                }
            }
        } 
        return differs;
    } 
    public boolean compareMethods(MethodAPI oldMethod, MethodAPI newMethod, ClassDiff classDiff) {
        MemberDiff methodDiff = new MemberDiff(oldMethod.name_);
        boolean differs = false;
        methodDiff.oldType_ = oldMethod.returnType_;
        methodDiff.newType_ = newMethod.returnType_;
        if (oldMethod.returnType_.compareTo(newMethod.returnType_) != 0) {
            differs = true;
        }
        String oldSig = oldMethod.getSignature();
        String newSig = newMethod.getSignature();
        methodDiff.oldSignature_ = oldSig;
        methodDiff.newSignature_ = newSig;
        if (oldSig.compareTo(newSig) != 0) {
            differs = true;
        }
        int inh = changedInheritance(oldMethod.inheritedFrom_, newMethod.inheritedFrom_);
        if (inh != 0)
            differs = true;
        if (inh == 1) {
            methodDiff.addModifiersChange("Method was locally defined, but is now inherited from " + linkToClass(newMethod, true) + ".");
            methodDiff.inheritedFrom_ = newMethod.inheritedFrom_;
        } else if (inh == 2) {
            methodDiff.addModifiersChange("Method was inherited from " + linkToClass(oldMethod, false) + ", but is now defined locally.");
        } else if (inh == 3) {
            methodDiff.addModifiersChange("Method was inherited from " + 
                                          linkToClass(oldMethod, false) + ", and is now inherited from " + linkToClass(newMethod, true) + ".");
            methodDiff.inheritedFrom_ = newMethod.inheritedFrom_;
        }
        if (oldMethod.isAbstract_ != newMethod.isAbstract_) {
            String changeText = "";
            if (oldMethod.isAbstract_)
                changeText += "Changed from abstract to non-abstract.";
            else
                changeText += "Changed from non-abstract to abstract.";
            methodDiff.addModifiersChange(changeText);
            differs = true;
        }
        if (Diff.showAllChanges && 
	    oldMethod.isNative_ != newMethod.isNative_) {
            String changeText = "";
            if (oldMethod.isNative_)
                changeText += "Changed from native to non-native.";
            else
                changeText += "Changed from non-native to native.";
            methodDiff.addModifiersChange(changeText);
            differs = true;
        }
        if (Diff.showAllChanges && 
	    oldMethod.isSynchronized_ != newMethod.isSynchronized_) {
            String changeText = "";
            if (oldMethod.isSynchronized_)
                changeText += "Changed from synchronized to non-synchronized.";
            else
                changeText += "Changed from non-synchronized to synchronized.";
            methodDiff.addModifiersChange(changeText);
            differs = true;
        }
        methodDiff.oldExceptions_ = oldMethod.exceptions_;
        methodDiff.newExceptions_ = newMethod.exceptions_;
        if (oldMethod.exceptions_.compareTo(newMethod.exceptions_) != 0) {
            differs = true;
        }
        if (docChanged(oldMethod.doc_, newMethod.doc_)) {
            String sig = methodDiff.newSignature_;
            if (sig.compareTo("void") == 0)
                sig = "";
            String fqName = pkgDiff.name_ + "." + classDiff.name_;
            String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
            String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + "." + newMethod.name_ + "_changed(" + sig + ")\" class=\"hiddenlink\">";
            String id = pkgDiff.name_ + "." + classDiff.name_ + ".dmethod." + newMethod.name_ + "(" + HTMLReportGenerator.simpleName(sig) + ")";
            String title = link1 + "Class <b>" + classDiff.name_ + "</b></a>, " +
                link2 + HTMLReportGenerator.simpleName(methodDiff.newType_) + " <b>" + newMethod.name_ + "(" + HTMLReportGenerator.simpleName(sig) + ")</b></a>";
            methodDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, classDiff.name_, oldMethod.doc_, newMethod.doc_, id, title);
            differs = true;
        }
        String modifiersChange = oldMethod.modifiers_.diff(newMethod.modifiers_);
        if (modifiersChange != null) {
            differs = true;
            if (modifiersChange.indexOf("Change from deprecated to undeprecated") != -1) {
                System.out.println("JDiff: warning: change from deprecated to undeprecated for method " +  classDiff.name_ + "." + newMethod.name_);
            }
        }
        methodDiff.addModifiersChange(modifiersChange);
        if (differs) {
            if (trace) {
                System.out.println("    Method " + newMethod.name_ + 
                    " was changed: old: " + 
                   oldMethod.returnType_ + "(" + oldSig + "), new: " +
                   newMethod.returnType_ + "(" + newSig + ")");
                if (methodDiff.modifiersChange_ != null)
                    System.out.println("    Modifier change: " + methodDiff.modifiersChange_);
            }
            classDiff.methodsChanged.add(methodDiff);
        }
        return differs;
    } 
    public boolean compareAllFields(ClassAPI oldClass, ClassAPI newClass, 
                                    ClassDiff classDiff) {
        if (trace)
            System.out.println("    Comparing fields: #old " + 
                               oldClass.fields_.size() + ", #new " 
                               + newClass.fields_.size());
        boolean differs = false;
        Collections.sort(oldClass.fields_);
        Collections.sort(newClass.fields_);
        Iterator iter = oldClass.fields_.iterator();
        while (iter.hasNext()) {
            FieldAPI oldField = (FieldAPI)(iter.next());
            int idx = Collections.binarySearch(newClass.fields_, oldField);
            if (idx < 0) {
                int existsNew = newClass.fields_.indexOf(oldField);
                if (existsNew != -1) {
                    FieldAPI newField = (FieldAPI)(newClass.fields_.get(existsNew));
                    if (oldField.inheritedFrom_ == null || 
                        newField.inheritedFrom_ == null) {
                        MemberDiff memberDiff = new MemberDiff(oldField.name_);
                        memberDiff.oldType_ = oldField.type_;
                        memberDiff.newType_ = newField.type_;
                        int inh = changedInheritance(oldField.inheritedFrom_, newField.inheritedFrom_);
                        if (inh != 0)
                            differs = true;
                        if (inh == 1) {
                            memberDiff.addModifiersChange("Field was locally defined, but is now inherited from " + linkToClass(newField, true) + ".");
                            memberDiff.inheritedFrom_ = newField.inheritedFrom_;
                        } else if (inh == 2) {
                            memberDiff.addModifiersChange("Field was inherited from " + linkToClass(oldField, false) + ", but is now defined locally.");
                        } else if (inh == 3) {
                            memberDiff.addModifiersChange("Field was inherited from " + linkToClass(oldField, false) + ", and is now inherited from " + linkToClass(newField, true) + ".");
                            memberDiff.inheritedFrom_ = newField.inheritedFrom_;
                        }
                        if (oldField.isTransient_ != newField.isTransient_) {
                            String changeText = "";
                            if (oldField.isTransient_)
                                changeText += "Changed from transient to non-transient.";
                            else
                                changeText += "Changed from non-transient to transient.";
                            memberDiff.addModifiersChange(changeText);
                            differs = true;
                        }
                        if (oldField.isVolatile_ != newField.isVolatile_) {
                            String changeText = "";
                            if (oldField.isVolatile_)
                                changeText += "Changed from volatile to non-volatile.";
                            else
                                changeText += "Changed from non-volatile to volatile.";
                            memberDiff.addModifiersChange(changeText);
                            differs = true;
                        }
                        if (oldField.value_ != null &&
                            newField.value_ != null &&
                            oldField.value_.compareTo(newField.value_) != 0) {
                            String changeText = "Changed in value from " + oldField.value_
                                + " to " + newField.value_ +".";
                            memberDiff.addModifiersChange(changeText);
                            differs = true;
                        }
                        if (docChanged(oldField.doc_, newField.doc_)) {
                            String fqName = pkgDiff.name_ + "." + classDiff.name_;
                            String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
                            String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + "." + newField.name_ + "\" class=\"hiddenlink\">";
                            String id = pkgDiff.name_ + "." + classDiff.name_ + ".field." + newField.name_;
                            String title = link1 + "Class <b>" + classDiff.name_ + "</b></a>, " +
                                link2 + HTMLReportGenerator.simpleName(memberDiff.newType_) + " <b>" + newField.name_ + "</b></a>";
                            memberDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, classDiff.name_, oldField.doc_, newField.doc_, id, title);
                            differs = true;
                        }
                        String modifiersChange = oldField.modifiers_.diff(newField.modifiers_);
                        memberDiff.addModifiersChange(modifiersChange);
                        if (modifiersChange != null && modifiersChange.indexOf("Change from deprecated to undeprecated") != -1) {
                            System.out.println("JDiff: warning: change from deprecated to undeprecated for class " + newClass.name_ + ", field " + newField.name_);
                        }
                        if (trace)
                            System.out.println("    Field " + newField.name_ + " was changed");
                        classDiff.fieldsChanged.add(memberDiff);
                        differs = true;
                    }
                } else if (oldField.inheritedFrom_ == null) {
                    if (trace)
                        System.out.println("    Field " + oldField.name_ + " was removed");
                    classDiff.fieldsRemoved.add(oldField);
                    differs = true;
                }
            }
        } 
        iter = newClass.fields_.iterator();
        while (iter.hasNext()) {
            FieldAPI newField = (FieldAPI)(iter.next());
            if (newField.inheritedFrom_ != null)
                continue;
            int idx = Collections.binarySearch(oldClass.fields_, newField);
            if (idx < 0) {
                int existsOld = oldClass.fields_.indexOf(newField);
                if (existsOld != -1) {
                } else {
                    if (trace)
                        System.out.println("    Field " + newField.name_ + " was added");
                    classDiff.fieldsAdded.add(newField);
                    differs = true;
                }
            }
        } 
        return differs;
    } 
    public static boolean docChanged(String oldDoc, String newDoc) {
        if (!HTMLReportGenerator.reportDocChanges)
            return false; 
        if (oldDoc == null && newDoc != null)
            return true;
        if (oldDoc != null && newDoc == null)
            return true;
        if (oldDoc != null && newDoc != null && oldDoc.compareTo(newDoc) != 0)
            return true;
        return false;
    }
    public static int changedInheritance(String oldInherit, String newInherit) {
        if (oldInherit == null && newInherit == null)
            return 0;
        if (oldInherit == null && newInherit != null)
            return 1;
        if (oldInherit != null && newInherit == null)
            return 2;
        if (oldInherit.compareTo(newInherit) == 0)
            return 0;
        else
            return 3;
    }
    public static String linkToClass(MethodAPI m, boolean useNew) {
        String sig = m.getSignature();
        if (sig.compareTo("void") == 0)
            sig = "";
        return linkToClass(m.inheritedFrom_, m.name_, sig, useNew);
    }
    public static String linkToClass(FieldAPI m, boolean useNew) {
        return linkToClass(m.inheritedFrom_, m.name_, null, useNew);
    }
    public static String linkToClass(String className, String memberName, 
                                     String memberType, boolean useNew) {
        if (!useNew && HTMLReportGenerator.oldDocPrefix == null) {
            return "<tt>" + className + "</tt>"; 
        }
        API api = oldAPI_;
        String prefix = HTMLReportGenerator.oldDocPrefix;
        if (useNew) {
            api = newAPI_;
            prefix = HTMLReportGenerator.newDocPrefix;
        }
        ClassAPI cls = (ClassAPI)api.classes_.get(className);
        if (cls == null) {
            if (useNew)
                System.out.println("Warning: class " + className + " not found in the new API when creating Javadoc link");
            else
                System.out.println("Warning: class " + className + " not found in the old API when creating Javadoc link");
            return "<tt>" + className + "</tt>";
        }
        int clsIdx = className.indexOf(cls.name_);
        if (clsIdx != -1) {
            String pkgRef = className.substring(0, clsIdx);
            pkgRef = pkgRef.replace('.', '/');
            String res = "<a href=\"" + prefix + pkgRef + cls.name_ + ".html#" + memberName;
            if (memberType != null)
                res += "(" + memberType + ")";
            res += "\" target=\"_top\">" + "<tt>" + cls.name_ + "</tt></a>";
            return res;
        }
        return "<tt>" + className + "</tt>";
    }    
    public int numLocalMethods(List methods) {
        int res = 0;
        Iterator iter = methods.iterator();
        while (iter.hasNext()) {
            MethodAPI m = (MethodAPI)(iter.next());
            if (m.inheritedFrom_ == null) 
                res++;
        }
        return res;
    }
    public int numLocalFields(List fields) {
        int res = 0;
        Iterator iter = fields.iterator();
        while (iter.hasNext()) {
            FieldAPI f = (FieldAPI)(iter.next());
            if (f.inheritedFrom_ == null) 
                res++;
        }
        return res;
    }
    private boolean trace = false;
}
