class MergeChanges {
    public static void mergeRemoveAdd(APIDiff apiDiff) {
        Iterator iter = apiDiff.packagesChanged.iterator();
        while (iter.hasNext()) {
            PackageDiff pkgDiff = (PackageDiff)(iter.next());
            Iterator iter2 = pkgDiff.classesChanged.iterator();
            while (iter2.hasNext()) {
                ClassDiff classDiff = (ClassDiff)(iter2.next());
                ConstructorAPI[] ctorArr = new ConstructorAPI[classDiff.ctorsRemoved.size()];
                ctorArr = (ConstructorAPI[])classDiff.ctorsRemoved.toArray(ctorArr);
                for (int ctorIdx = 0; ctorIdx < ctorArr.length; ctorIdx++) {
                    ConstructorAPI removedCtor = ctorArr[ctorIdx];
                    mergeRemoveAddCtor(removedCtor, classDiff, pkgDiff);
                }
                MethodAPI[] methodArr = new MethodAPI[classDiff.methodsRemoved.size()];
                methodArr = (MethodAPI[])classDiff.methodsRemoved.toArray(methodArr);
                for (int methodIdx = 0; methodIdx < methodArr.length; methodIdx++) {
                    MethodAPI removedMethod = methodArr[methodIdx];
                    if (removedMethod.inheritedFrom_ == null)
                        mergeRemoveAddMethod(removedMethod, classDiff, pkgDiff);
                }
                FieldAPI[] fieldArr = new FieldAPI[classDiff.fieldsRemoved.size()];
                fieldArr = (FieldAPI[])classDiff.fieldsRemoved.toArray(fieldArr);
                for (int fieldIdx = 0; fieldIdx < fieldArr.length; fieldIdx++) {
                    FieldAPI removedField = fieldArr[fieldIdx]; 
                    if (removedField.inheritedFrom_ == null)
                        mergeRemoveAddField(removedField, classDiff, pkgDiff);
                }
            }
        }        
    }
    public static void mergeRemoveAddCtor(ConstructorAPI removedCtor, ClassDiff classDiff, PackageDiff pkgDiff) {
        int startRemoved = classDiff.ctorsRemoved.indexOf(removedCtor);
        int endRemoved = classDiff.ctorsRemoved.lastIndexOf(removedCtor);
        int startAdded = classDiff.ctorsAdded.indexOf(removedCtor);
        int endAdded = classDiff.ctorsAdded.lastIndexOf(removedCtor);
        if (startRemoved != -1 && startRemoved == endRemoved && 
            startAdded != -1 && startAdded == endAdded) {
            ConstructorAPI addedCtor = (ConstructorAPI)(classDiff.ctorsAdded.get(startAdded));
            MemberDiff ctorDiff = new MemberDiff(classDiff.name_);
            ctorDiff.oldType_ = removedCtor.type_;
            ctorDiff.newType_ = addedCtor.type_; 
            ctorDiff.oldExceptions_ = removedCtor.exceptions_;
            ctorDiff.newExceptions_ = addedCtor.exceptions_;
            ctorDiff.addModifiersChange(removedCtor.modifiers_.diff(addedCtor.modifiers_));
            if (APIComparator.docChanged(removedCtor.doc_, addedCtor.doc_)) {
                String type = ctorDiff.newType_;
                if (type.compareTo("void") == 0)
                    type = "";
                String fqName = pkgDiff.name_ + "." + classDiff.name_;
                String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
                String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + ".ctor_changed(" + type + ")\" class=\"hiddenlink\">";
                String id = pkgDiff.name_ + "." + classDiff.name_ + ".ctor(" + HTMLReportGenerator.simpleName(type) + ")";
                String title = link1 + "Class <b>" + classDiff.name_ + 
                    "</b></a>, " + link2 + "constructor <b>" + classDiff.name_ + "(" + HTMLReportGenerator.simpleName(type) + ")</b></a>";
                ctorDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, classDiff.name_, removedCtor.doc_, addedCtor.doc_, id, title);
            }
            classDiff.ctorsChanged.add(ctorDiff);
            classDiff.ctorsRemoved.remove(startRemoved);
            classDiff.ctorsAdded.remove(startAdded);
            if (trace && ctorDiff.modifiersChange_ != null)
                System.out.println("Merged the removal and addition of constructor into one change: " + ctorDiff.modifiersChange_);
        }
    }
    public static void mergeRemoveAddMethod(MethodAPI removedMethod, 
                                            ClassDiff classDiff, 
                                            PackageDiff pkgDiff) {
        mergeSingleMethods(removedMethod, classDiff, pkgDiff);
        mergeMultipleMethods(removedMethod, classDiff, pkgDiff);
    }
    public static void mergeSingleMethods(MethodAPI removedMethod, ClassDiff classDiff, PackageDiff pkgDiff) {
        int startRemoved = classDiff.methodsRemoved.indexOf(removedMethod);
        int endRemoved = classDiff.methodsRemoved.lastIndexOf(removedMethod);
        int startAdded = classDiff.methodsAdded.indexOf(removedMethod);
        int endAdded = classDiff.methodsAdded.lastIndexOf(removedMethod);
        if (startRemoved != -1 && startRemoved == endRemoved && 
            startAdded != -1 && startAdded == endAdded) {
            MethodAPI addedMethod = (MethodAPI)(classDiff.methodsAdded.get(startAdded));
            if (addedMethod.inheritedFrom_ == null) {
                MemberDiff methodDiff = new MemberDiff(removedMethod.name_);
                methodDiff.oldType_ = removedMethod.returnType_;
                methodDiff.newType_ = addedMethod.returnType_;
                methodDiff.oldSignature_ = removedMethod.getSignature();
                methodDiff.newSignature_ = addedMethod.getSignature();
                methodDiff.oldExceptions_ = removedMethod.exceptions_;
                methodDiff.newExceptions_ = addedMethod.exceptions_;
                diffMethods(methodDiff, removedMethod, addedMethod);
                methodDiff.addModifiersChange(removedMethod.modifiers_.diff(addedMethod.modifiers_));
                if (APIComparator.docChanged(removedMethod.doc_, addedMethod.doc_)) {
                    String sig = methodDiff.newSignature_;
                    if (sig.compareTo("void") == 0)
                        sig = "";
                    String fqName = pkgDiff.name_ + "." + classDiff.name_;
                    String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
                    String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + "." + addedMethod.name_ + "_changed(" + sig + ")\" class=\"hiddenlink\">";
                    String id = pkgDiff.name_ + "." + classDiff.name_ + ".dmethod." + addedMethod.name_ + "(" + HTMLReportGenerator.simpleName(sig) + ")";
                    String title = link1 + "Class <b>" + classDiff.name_ + "</b></a>, " +
                        link2 +  HTMLReportGenerator.simpleName(methodDiff.newType_) + " <b>" + addedMethod.name_ + "(" + HTMLReportGenerator.simpleName(sig) + ")</b></a>";
                    methodDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, classDiff.name_, removedMethod.doc_, addedMethod.doc_, id, title);
                }
                classDiff.methodsChanged.add(methodDiff);
                classDiff.methodsRemoved.remove(startRemoved);
                classDiff.methodsAdded.remove(startAdded);
                if (trace) {
                    System.out.println("Merged the removal and addition of method " + 
                                       removedMethod.name_ + 
                                       " into one change");
                }
            } 
        }
    }
    public static void mergeMultipleMethods(MethodAPI removedMethod, ClassDiff classDiff, PackageDiff pkgDiff) {
        int startRemoved = classDiff.methodsRemoved.indexOf(removedMethod);
        int endRemoved = classDiff.methodsRemoved.lastIndexOf(removedMethod);
        int startAdded = classDiff.methodsAdded.indexOf(removedMethod);
        int endAdded = classDiff.methodsAdded.lastIndexOf(removedMethod);
        if (startRemoved != -1 && endRemoved != -1 && 
            startAdded != -1 && endAdded != -1) {
            int removedIdx = -1;
            for (int i = startRemoved; i <= endRemoved; i++) {                
                if (removedMethod.equalSignatures(classDiff.methodsRemoved.get(i))) {
                    removedIdx = i;
                    break;
                }
            }
            if (removedIdx == -1) {
                System.out.println("Error: removed method index not found");
                System.exit(5);
            }
            int addedIdx = -1;
            for (int i = startAdded; i <= endAdded; i++) {
                MethodAPI addedMethod2 = (MethodAPI)(classDiff.methodsAdded.get(i));
                if (addedMethod2.inheritedFrom_ == null &&
                    removedMethod.equalSignatures(addedMethod2))
                    addedIdx = i;
                    break;
            }
            if (addedIdx == -1)
                return;
            MethodAPI addedMethod = (MethodAPI)(classDiff.methodsAdded.get(addedIdx));
            MemberDiff methodDiff = new MemberDiff(removedMethod.name_);
            methodDiff.oldType_ = removedMethod.returnType_;
            methodDiff.newType_ = addedMethod.returnType_;
            methodDiff.oldSignature_ = removedMethod.getSignature();
            methodDiff.newSignature_ = addedMethod.getSignature();
            methodDiff.oldExceptions_ = removedMethod.exceptions_;
            methodDiff.newExceptions_ = addedMethod.exceptions_;
                diffMethods(methodDiff, removedMethod, addedMethod);
            methodDiff.addModifiersChange(removedMethod.modifiers_.diff(addedMethod.modifiers_));
            if (APIComparator.docChanged(removedMethod.doc_, addedMethod.doc_)) {
                String sig = methodDiff.newSignature_;
                if (sig.compareTo("void") == 0)
                    sig = "";
                String fqName = pkgDiff.name_ + "." + classDiff.name_;
                String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
                String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + "." + addedMethod.name_ + "_changed(" + sig + ")\" class=\"hiddenlink\">";
                String id = pkgDiff.name_ + "." + classDiff.name_ + ".dmethod." + addedMethod.name_ + "(" + HTMLReportGenerator.simpleName(sig) + ")";
                String title = link1 + "Class <b>" + classDiff.name_ + "</b></a>, " +
                    link2 +  HTMLReportGenerator.simpleName(methodDiff.newType_) + " <b>" + addedMethod.name_ + "(" + HTMLReportGenerator.simpleName(sig) + ")</b></a>";
                methodDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, classDiff.name_, removedMethod.doc_, addedMethod.doc_, id, title);
            }
            classDiff.methodsChanged.add(methodDiff);
            classDiff.methodsRemoved.remove(removedIdx);
            classDiff.methodsAdded.remove(addedIdx);
            if (trace) {
                System.out.println("Merged the removal and addition of method " + 
                                   removedMethod.name_ + 
                                   " into one change. There were multiple methods of this name.");
            }
        }
    }
    public static void diffMethods(MemberDiff methodDiff, 
                                   MethodAPI oldMethod, 
                                   MethodAPI newMethod) {
        if (oldMethod.isAbstract_ != newMethod.isAbstract_) {
            String changeText = "";
            if (oldMethod.isAbstract_)
                changeText += "Changed from abstract to non-abstract.";
            else
                changeText += "Changed from non-abstract to abstract.";
            methodDiff.addModifiersChange(changeText);
        }
        if (Diff.showAllChanges && 
	    oldMethod.isNative_ != newMethod.isNative_) {
            String changeText = "";
            if (oldMethod.isNative_)
                changeText += "Changed from native to non-native.";
            else
                changeText += "Changed from non-native to native.";
            methodDiff.addModifiersChange(changeText);
        }
        if (Diff.showAllChanges && 
	    oldMethod.isSynchronized_ != newMethod.isSynchronized_) {
            String changeText = "";
            if (oldMethod.isSynchronized_)
                changeText += "Changed from synchronized to non-synchronized.";
            else
                changeText += "Changed from non-synchronized to synchronized.";
            methodDiff.addModifiersChange(changeText);
        }
    }
    public static void mergeRemoveAddField(FieldAPI removedField, ClassDiff classDiff, PackageDiff pkgDiff) {
        int startRemoved = classDiff.fieldsRemoved.indexOf(removedField);
        int endRemoved = classDiff.fieldsRemoved.lastIndexOf(removedField);
        int startAdded = classDiff.fieldsAdded.indexOf(removedField);
        int endAdded = classDiff.fieldsAdded.lastIndexOf(removedField);
        if (startRemoved != -1 && startRemoved == endRemoved && 
            startAdded != -1 && startAdded == endAdded) {
            FieldAPI addedField = (FieldAPI)(classDiff.fieldsAdded.get(startAdded));
            if (addedField.inheritedFrom_ == null) {
                MemberDiff fieldDiff = new MemberDiff(removedField.name_);
                fieldDiff.oldType_ = removedField.type_;
                fieldDiff.newType_ = addedField.type_;
                fieldDiff.addModifiersChange(removedField.modifiers_.diff(addedField.modifiers_));
                if (APIComparator.docChanged(removedField.doc_, addedField.doc_)) {
                    String fqName = pkgDiff.name_ + "." + classDiff.name_;
                    String link1 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "\" class=\"hiddenlink\">";
                    String link2 = "<a href=\"" + fqName + HTMLReportGenerator.reportFileExt + "#" + fqName + "." + addedField.name_ + "\" class=\"hiddenlink\">";
                    String id = pkgDiff.name_ + "." + classDiff.name_ + ".field." + addedField.name_;
                    String title = link1 + "Class <b>" + classDiff.name_ + "</b></a>, " +
                        link2 + HTMLReportGenerator.simpleName(fieldDiff.newType_) + " <b>" + addedField.name_ + "</b></a>";
                    fieldDiff.documentationChange_ = Diff.saveDocDiffs(pkgDiff.name_, classDiff.name_, removedField.doc_, addedField.doc_, id, title);
                }
                classDiff.fieldsChanged.add(fieldDiff);
                classDiff.fieldsRemoved.remove(startRemoved);
                classDiff.fieldsAdded.remove(startAdded);
                if (trace) {
                    System.out.println("Merged the removal and addition of field " + 
                                       removedField.name_ + 
                                       " into one change");
                }
            } 
        }
    }
    private static boolean trace = false;
}
