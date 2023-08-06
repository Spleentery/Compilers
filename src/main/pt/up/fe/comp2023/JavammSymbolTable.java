package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;
import java.util.stream.Collectors;

public class JavammSymbolTable implements SymbolTable{

    private List<String> imports;
    private ClassInfo class_info;
    private Map<String, Method> methods;

    private JmmNode rootNode;

    public JavammSymbolTable(JmmNode rootNode) {
        this.rootNode = rootNode;
        this.imports = new ArrayList<>();
        this.methods = new HashMap<>();
        ImportVisitor iVisitor = new ImportVisitor();
        ClassVisitor cVisitor = new ClassVisitor();
        MethodVisitor mVisitor = new MethodVisitor();

        for(JmmNode child : this.rootNode.getChildren()) {
            switch (child.getKind()) {
                case "ImportList" -> this.imports = iVisitor.visit(child, new ArrayList<>());
                case "ClassDeclaration" -> {
                    this.class_info = cVisitor.visit(child, null);
                    for (JmmNode c2 : child.getChildren()) {
                        if(c2.getKind().equals("MethodList"))
                            this.methods = mVisitor.visit(c2, new HashMap<>());
                    }
                }
            }
        }
    }

    public void getRootNode(JmmNode node) {
        this.rootNode = node;
    }
    @Override
    public List<String> getImports() {
        return this.imports;
    }
    @Override
    public String getClassName() {
        return this.class_info.getName();
    }

    @Override
    public String getSuper() {
        return this.class_info.getSuper_class();
    }

    @Override
    public List<Symbol> getFields() {
        return this.class_info.getFields();
    }

    @Override
    public List<String> getMethods() {
        if(this.methods == null) return new ArrayList<>();
        List<String> methodNames = new ArrayList<>();
         for (var m: this.methods.entrySet()) {
             methodNames.add(m.getKey());
         } return methodNames;
    }

    @Override
    public Type getReturnType(String s) {
        Method method = this.methods.get(s);
        if (method != null) {
            return method.getReturnType();
        } else {
            // Handle the case where method is null. Depending on your use case,
            // this could involve throwing an exception, logging an error, or returning a default value.
            return null; // or any default value
        }
    }

    @Override
    public List<Symbol> getParameters(String s) {
        Method method = this.methods.get(s);
        if (method != null) {
            return method.getMethodParameters();
        } else {
            // Handle the case where method is null. Depending on your use case,
            // this could involve throwing an exception, logging an error, or returning a default value.
            return null; // or any default value
        }
    }


    @Override
    public List<Symbol> getLocalVariables(String s) {
        return this.methods.get(s).getLocalVariables();
    }


}
