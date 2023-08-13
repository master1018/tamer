public class MethodSelector {
    public String obtainReplacement(String buffer) {
        IMethod method = selectMethod();
        if (method == null) {
            return null;
        }
        boolean shortOnly = false;
        int annotPos = buffer.lastIndexOf("@TestInfo");
        if (annotPos != -1) {
            String sub = buffer.substring(annotPos);
            String[] lines = sub.split("\n");
            for (int i = lines.length - 6; i < lines.length; i++) {
                String line = lines[i];
                if (line.contains("@TestTarget")) {
                    shortOnly = true;
                }
            }
        }
        return generateAnnotation(shortOnly, method);
    }
    private String generateAnnotation(boolean shortOnly, IMethod method) {
        String[] ptypes = method.getParameterTypes();
        String param = "";
        for (int i = 0; i < ptypes.length; i++) {
            String ptype = ptypes[i];
            String sig = Signature.toString(ptype);
            if (sig.length() == 1) {
                ITypeParameter tps = method.getTypeParameter(sig);
                sig = "Object";
                if (tps != null && tps.exists()) {
                    try {
                        String[] bounds = tps.getBounds();
                        if (bounds.length > 0) {
                            sig = bounds[0];
                        }
                    } catch (JavaModelException e) {
                        e.printStackTrace();
                    }
                }
            }
            sig = sig.replaceAll("<.*>", "");
            param += (i > 0 ? ", " : "") + sig + ".class";
        }
        String IND = "    ";
        String targ = "@TestTarget(\n" + IND + "      methodName = \""
                + method.getElementName() + "\",\n" + IND
                + "      methodArgs = {" + param + "}\n" + IND + "    )\n";
        String s;
        if (shortOnly) {
            s = targ;
        } else {
            s = "@TestInfo(\n" + IND + "  status = TestStatus.TBR,\n" + IND
                    + "  notes = \"\",\n" + IND + "  targets = {\n" + IND
                    + "    " + targ + IND + "})";
        }
        return s;
    }
    private IMethod selectMethod() {
        IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActiveEditor();
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        IEditorInput ei = part.getEditorInput();
        final ICompilationUnit cu = JavaPlugin.getDefault()
                .getWorkingCopyManager().getWorkingCopy(ei);
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setSource(cu);
        parser.setResolveBindings(true);
        CompilationUnit unit = (CompilationUnit) parser.createAST(null);
        class MHolder {
            IMethod method;
        }
        final MHolder mholder = new MHolder();
        class FHolder {
            boolean foundClassAnnotation;
        }
        final FHolder fholder = new FHolder();
        unit.accept(new ASTVisitor() {
            public boolean visit(SingleMemberAnnotation node) {
                String name = node.getTypeName().getFullyQualifiedName();
                if (!name.equals("TestTargetClass")) {
                    return false;
                }
                fholder.foundClassAnnotation = true;
                Expression targetClassE = node.getValue();
                ITypeBinding ty = targetClassE.resolveTypeBinding();
                if (ty == null) {
                    return false;
                }
                ITypeBinding[] classTypes = ty.getTypeArguments();
                if (classTypes.length > 0) {
                    ITypeBinding tp = classTypes[0];
                    String qname = tp.getQualifiedName();
                    System.out.println("qname:" + qname);
                    IJavaProject myProject = cu.getJavaProject();
                    try {
                        IType myType = myProject.findType(qname);
                        if (myType != null) {
                            Shell parent = PlatformUI.getWorkbench()
                                    .getActiveWorkbenchWindow().getShell();
                            ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                                    parent,
                                    new JavaElementLabelProvider(
                                            JavaElementLabelProvider.SHOW_PARAMETERS
                                                    | JavaElementLabelProvider.SHOW_OVERLAY_ICONS
                                                    | JavaElementLabelProvider.SHOW_RETURN_TYPE));
                            IMethod[] allMeth = myType.getMethods();
                            List<IMethod> pubproMethods = new ArrayList<IMethod>();
                            for (int i = 0; i < allMeth.length; i++) {
                                IMethod method = allMeth[i];
                                if ((method.getFlags() & (Flags.AccPublic | Flags.AccProtected)) != 0) {
                                    pubproMethods.add(method);
                                }
                            }
                            IMethod[] res = pubproMethods
                                    .toArray(new IMethod[pubproMethods.size()]);
                            dialog.setIgnoreCase(true);
                            dialog.setBlockOnOpen(true);
                            dialog.setElements(res);
                            dialog.setFilter("");
                            dialog.setTitle(qname);
                            if (dialog.open() != IDialogConstants.CANCEL_ID) {
                                Object[] types = dialog.getResult();
                                System.out.println("selected:" + types[0]);
                                IMethod method = (IMethod) types[0];
                                mholder.method = method;
                            } else {
                            }
                        }
                    } catch (JavaModelException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        if (!fholder.foundClassAnnotation) {
            MessageDialog.openInformation(shell, "Class Annotation missing",
                    "@TestTargetClass(...) is missing");
            return null;
        }
        return mholder.method;
    }
}
