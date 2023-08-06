package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ASemanticVisitor extends AJmmVisitor<List<Report>, Type> {
    private List<Report> reports;
    private JavammSymbolTable symbolTable;
    private JmmNode currentMethod;
    private List<Type> classFields;
    private List<Type> methodParameters;
    private HashMap<String, List<Type>> methodVariables;

    public ASemanticVisitor(JavammSymbolTable symbolTable) {
        reports = new ArrayList<>();
        this.symbolTable = symbolTable;
        currentMethod = null;
        methodVariables = new HashMap<>();
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::visitDefault);
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportList", this::dealWithImportList);
        addVisit("ImportDeclaration", this::dealWithImportDeclaration);
        addVisit("PackageId", this::dealWithPackageId);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("VarList", this::dealWithVarList);
        addVisit("VarDeclaration", this::dealWithVarDeclaration);
        addVisit("TypeIntegerArray", this::dealWithIntegerArray);
        addVisit("BracketsStatement", this::dealWithBracketsStatement);
        addVisit("Parentheses", this::dealWithParentheses);
        // ----- Methods -----
        addVisit("MethodList", this::dealWithMethodList);
        addVisit("MethodDeclaration", this::dealWithMethodDeclaration);
        addVisit("ParameterList", this::dealWithParameterList);
        addVisit("StatementList", this::dealWithStatementList);
        addVisit("IfStatement", this::dealWithIfStatement);
        addVisit("WhileStatement", this::dealWithWhileStatement);
        addVisit("AssignStatement", this::dealWithAssignStatement);
        addVisit("ExpressionStatement", this::dealWithExpressionStatement);
        addVisit("Init", this::dealWithInit);
        addVisit("ArrayInit", this::dealWithArrayInit);
        addVisit("Identifier", this::dealWithIdentifier);
        addVisit("TypeInteger", this::dealWithInteger);
        addVisit("Integer", this::dealWithInteger);
        addVisit("BooleanLiteral", this::dealWithBoolean);
        addVisit("TypeBoolean", this::dealWithBoolean);
        addVisit("BinaryOp", this::dealWithBinaryOp);
        addVisit("ComparisonOp", this::dealWithComparisonOp);
        addVisit("MethodReturn", this::dealWithMethodReturn);
        addVisit("Function", this::dealWithFunction);
        addVisit("MethodCall", this::dealWithMethodCall);
        addVisit("ArrayAccess", this::dealWithArrayAccess);
        addVisit("AccessAndAssignStatement", this::dealWithAccessAndAssignStatement);
    }


    private Type dealWithAccessAndAssignStatement(JmmNode jmmNode, List<Report> reports) {
        JmmNode leftchild = jmmNode.getJmmChild(0);
        JmmNode rightchild = jmmNode.getJmmChild(1);
        Type leftType = visit(leftchild, reports);
        Type rightType = visit(rightchild, reports);
        if(Integer.parseInt(leftchild.get("value")) < Integer.parseInt(rightchild.get("value"))-1){
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Array index out of bounds");
            reports.add(report);
            return null;
        }
        return leftType;
    }

    private Type dealWithExpressionStatement(JmmNode jmmNode, List<Report> reports) {
        return visit(jmmNode.getJmmChild(0), reports);
    }

    private Type dealWithInit(JmmNode jmmNode, List<Report> reports) {
        if (jmmNode.hasAttribute("value")) {
            String typeName = jmmNode.get("value");
            return new Type(typeName, false);
        }
        JmmNode child = jmmNode.getJmmChild(0);
        return visit(child, reports);
    }

    private Type dealWithArrayInit(JmmNode jmmNode, List<Report> reports) {
        if (jmmNode.hasAttribute("value")) {
            String typeName = jmmNode.get("value");
            return new Type(typeName, true);
        }
        JmmNode child = jmmNode.getJmmChild(0);
        Type childType = visit(child, reports);
        return new Type(childType.getName(), true);
    }

    private Type dealWithParentheses(JmmNode jmmNode, List<Report> reports) {
        return visit(jmmNode.getJmmChild(0), reports);
    }

    private Type dealWithBracketsStatement(JmmNode jmmNode, List<Report> reports) {
        for (JmmNode child : jmmNode.getChildren()) {
            return visit(child, reports);
        }
        return null;
    }

    private Type dealWithWhileStatement(JmmNode jmmNode, List<Report> reports) {
        JmmNode child = jmmNode.getJmmChild(0);
        Type conditionType = visit(child, reports);
        if (conditionType == null) {
            return null;
        }

        if(child.getKind().equals("ComparisonOp") || child.getKind().equals("LogicalOp")){

        }
        else if (!conditionType.getName().equals("boolean")) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Condition must be a boolean");
            reports.add(report);
            return null;
        }
        visit(jmmNode.getJmmChild(1), reports);
        return null;
    }

    private Type dealWithIfStatement(JmmNode jmmNode, List<Report> reports) {
        JmmNode child = jmmNode.getJmmChild(0);
        Type conditionType = visit(child, reports);
        if (conditionType == null) {
            return null;
        }
        if(child.getKind().equals("ComparisonOp") || child.getKind().equals("LogicalOp")){

        }
        else if (!conditionType.getName().equals("boolean")) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Condition must be a boolean");
            reports.add(report);
            return null;
        }
        visit(jmmNode.getJmmChild(1), reports);
        return null;
    }


    public void addTypeToMethodVariables(String key, Type type) {
        methodVariables.computeIfAbsent(key, k -> new ArrayList<>()).add(type);
    }


    private Type visitDefault(JmmNode programNode, List<Report> reports) {
        return null;
    }

    private Type dealWithProgram(JmmNode jmmNode, List<Report> reports) {

        Type nodeType = null;
        for (var node : jmmNode.getChildren()) {
            nodeType = visit(node, reports);
            if (node.getKind().equals("ClassDeclaration")) {
                return nodeType;
            }
        }
        return nodeType;
    }

    /*
     * Basically there's no need to visit the ImportList, but let's do it anyway
     */
    private Type dealWithImportList(JmmNode jmmNode, List<Report> reports) {
        for (JmmNode importDeclaration : jmmNode.getChildren()) {
            visit(importDeclaration, reports);
        }

        return visit(jmmNode.getJmmChild(0), reports);
    }

    private Type dealWithImportDeclaration(JmmNode jmmNode, List<Report> reports) {
        JmmNode packageId = jmmNode.getJmmChild(0);
        Type packageType = visit(packageId, reports);
        return packageType;
    }

    private Type dealWithPackageId(JmmNode jmmNode, List<Report> reports) {

        Type lastChildType = null;

        // Visit each child node
        for (JmmNode child : jmmNode.getChildren()) {
            lastChildType = visitAllChildren(child, reports);
        }

        return lastChildType;
    }

    /*
     * Recursively visits all children and grandchildren of a node
     */
    public Type visitAllChildren(JmmNode jmmNode, List<Report> reports) {
        // Visit the current node
        Type childType = visit(jmmNode, reports);

        // Visit each child node
        for (JmmNode child : jmmNode.getChildren()) {
            childType = visitAllChildren(child, reports);
        }

        return childType;
    }


    private Type dealWithClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        // if class is empty, report warning
        if (jmmNode.getChildren().size() == 0) {
            Report report = new Report(ReportType.WARNING, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Class is empty");
            reports.add(report);
            return null;
        }

        for (var child : jmmNode.getChildren()) {
            // visit VarList if it exists
            if (child.getKind().equals("VarList")) {
                visit(child, reports);
            }
            // visit MethodList if it exists
            if (child.getKind().equals("MethodList")) {
                visit(child, reports);
            }
        }

        return visit(jmmNode.getJmmChild(0), reports);
    }

    private Type dealWithVarList(JmmNode jmmNode, List<Report> reports) {
        Type lastChildType = null;
        for (var child : jmmNode.getChildren()) {
            lastChildType = visit(child, reports);
        }
        return lastChildType;
    }

    private Type dealWithVarDeclaration(JmmNode jmmNode, List<Report> reports) {

        JmmNode child = jmmNode.getJmmChild(0); // VarDeclaration can only have one child
        Type childType = visit(child, reports);
        if (currentMethod == null) {
            return null;
        }

        if (this.currentMethod != null) {
            addTypeToMethodVariables(currentMethod.get("name"), childType);
        }
        return childType;
    }

    private Type dealWithMethodList(JmmNode jmmNode, List<Report> reports) {
        Type lastChildType = null;
        for (var child : jmmNode.getChildren()) {
            lastChildType = visit(child, reports);
        }
        return lastChildType;
    }

    private Type dealWithMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        this.currentMethod = jmmNode;
        Type lastChildType = null;
        for (var child : jmmNode.getChildren()) {
            lastChildType = visit(child, reports);
        }
        return lastChildType;
    }


    private Type dealWithParameterList(JmmNode jmmNode, List<Report> reports) {
        JmmNode child = jmmNode.getJmmChild(0);
        return visit(child, reports);
    }

    private Type dealWithStatementList(JmmNode jmmNode, List<Report> reports) {
        Type lastChildType = null;
        for (var child : jmmNode.getChildren()) {
            lastChildType = visit(child, reports);
        }
        return lastChildType;
    }


    private Type dealWithAssignStatement(JmmNode jmmNode, List<Report> reports) {
        JmmNode sourceVariable = jmmNode.getJmmChild(0);
        Type sourceType = visit(sourceVariable, reports);
        if(sourceType == null) {
            return null;
        }

        String targetVariableName = jmmNode.get("name");
        Type targetType = null;
        if (this.currentMethod == null || this.symbolTable == null) {
            return null;
        }

        boolean isStatic = false;
        // check if we're dealing with a static method
        if(currentMethod.hasAttribute("stat")){
            if(currentMethod.get("stat").equals("static")){
                isStatic = true;
            }
        }
        if(isStatic){
            if(jmmNode.hasAttribute("value")){
                if(jmmNode.get("value").equals("this")){
                    Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Cannot use 'this' in a static method");
                    reports.add(report);
                    return null;
                }
            }
        }


        /*
         * check if target is declared
         * in method vars
         * in method parameters
         * in class fields
         * in imports
         */
        // check method variables
        List<Symbol> localVariables = symbolTable.getLocalVariables(currentMethod.get("name"));
        for (var localvar : localVariables) {
            if (localvar.getName().equals(targetVariableName)) {
                targetType = localvar.getType();
                break;
            }
        }
        // check method parameters
        List<Symbol> parameters = symbolTable.getParameters(currentMethod.get("name"));
        for (var parameter : parameters) {
            if (parameter.getName().equals(targetVariableName)) {
                targetType = parameter.getType();
                break;
            }
        }

        // check imports
        List<String> imports = symbolTable.getImports();
        if (imports.contains(targetVariableName)) {
            targetType = new Type(targetVariableName, false);
        }

        /*
         * if the variable is not declared in the local vars, parameters, imports
         * but declared in a class field, but the method is static
         * then it's an error
         */
        if(!isStatic) {
            // check class fields
            List<Symbol> classFields = symbolTable.getFields();
            for (var field : classFields) {
                if (field.getName().equals(targetVariableName)) {
                    targetType = field.getType();
                    break;
                }
            }
        }

        if (targetType == null) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Variable " + targetVariableName + " is not declared");
            reports.add(report);
            return null;
        }

        if (targetType.equals(sourceType))
            return targetType;

        // if source type extends target type
        String extend = symbolTable.getSuper();
        String className = symbolTable.getClassName();
        if (targetType.getName().equals(extend)) {
            if (className.equals(sourceType.getName())) {
                return targetType;
            }
        }

        // check if both variables are imported
        if (imports.contains(targetType.getName()) && imports.contains(sourceType.getName())) {
            return targetType;
        }
        Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Variable " + targetVariableName + " is not of type " + sourceType.getName());
        reports.add(report);
        return null;
    }

    private Type dealWithIdentifier(JmmNode jmmNode, List<Report> reports) {
        String variableName = jmmNode.get("value");
        List<Symbol> localVariables = symbolTable.getLocalVariables(currentMethod.get("name"));
        /*
         * Check if the variable exists in the method variable/parameter list/class fields/ or in the parent class fields
         */
        // check method variables
        for (var variable : localVariables) {
            if (variable.getName().equals(variableName)) {
                return variable.getType();
            }
        }
        // check method parameters
        List<Symbol> parameters = symbolTable.getParameters(currentMethod.get("name"));
        for (var parameter : parameters) {
            if (parameter.getName().equals(variableName)) {
                return parameter.getType();
            }
        }
        // check class fields
        List<Symbol> classFields = symbolTable.getFields();
        for (var field : classFields) {
            if (field.getName().equals(variableName)) {
                return field.getType();
            }
        }
        // check parent class fields
        List<String> imports = symbolTable.getImports();
        for (var importClass : imports) {
            if (importClass.equals(variableName)) {
                return new Type(importClass, false);
            }
        }

        // if variable is not found, report error
        Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Variable " + variableName + " is not declared");
        reports.add(report);
        return null;
    }

    private Type dealWithInteger(JmmNode jmmNode, List<Report> reports) {
        return new Type("int", false);
    }

    private Type dealWithBoolean(JmmNode jmmNode, List<Report> reports) {
        return new Type("boolean", false);
    }

    private Type dealWithIntegerArray(JmmNode jmmNode, List<Report> reports) {
        return new Type("int", true);
    }

    private Type dealWithBinaryOp(JmmNode jmmNode, List<Report> reports) {
        /*
         * BinaryOp always has two children
         * Identifier, Integer, Boolean
         */
        JmmNode leftChild = jmmNode.getJmmChild(0);
        JmmNode rightChild = jmmNode.getJmmChild(1);

        Type leftChildType = visit(leftChild, reports);
        Type rightChildType = visit(rightChild, reports);


        /*
         * Operands of an operation must have types compatible with the operation
         * 1. Currently, we only support operations between two integer literals
         * Not boolean
         * Not arrays
         */
        if(leftChildType != null && rightChildType != null) {
            if (!leftChildType.getName().equals("int") || !rightChildType.getName().equals("int")) {
                Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Operands of an operation must have types compatible with the operation" + leftChildType.getName() + " " + leftChildType + " " + rightChildType.getName() + " " + rightChildType);
                reports.add(report);
                return null;
            } else if (leftChildType.isArray() || rightChildType.isArray()) {
                Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Operands of an operation must have types compatible with the operation (e.g. int + boolean\n" +
                        "is an error because + expects two integers.)" + " - " + leftChild + " " + leftChildType + " " + rightChild + " " + rightChildType);
                reports.add(report);
                return null;
            }
        }

        // otherwise, everything is good, return any of the two types
        return leftChildType;
    }
    private Type dealWithComparisonOp(JmmNode jmmNode, List<Report> reports) {
        return visit(jmmNode.getJmmChild(0), reports);
    }

    private Type dealWithMethodReturn(JmmNode jmmNode, List<Report> reports) {

        if (currentMethod == null || symbolTable == null) {
            return null;
        }

        Type expectedReturn = symbolTable.getReturnType(currentMethod.get("name"));

        Type actualReturn = null;
        String functionName = "";
        JmmNode child = jmmNode.getJmmChild(0);
        actualReturn = visit(child, reports);
        if (actualReturn == null) {
            return null;
        }

        // If the returned variable is imported, assume correct
        List<String> imports = symbolTable.getImports();
        for (var imp : imports) {
            if (imp.equals(actualReturn.getName())) {
                return expectedReturn;
            }
        }

        if (!expectedReturn.equals(actualReturn)) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Return type of method " + currentMethod.get("name") + " is " + expectedReturn + " but return type of the provided method is " + actualReturn);
            reports.add(report);
            return null;
        }
        return actualReturn;
    }

    private Type dealWithFunction(JmmNode jmmNode, List<Report> reports) {
        JmmNode child = jmmNode.getJmmChild(0);
        String callerName = jmmNode.getJmmChild(0).get("value");
        String calleeName = jmmNode.get("value");


        if (symbolTable == null || currentMethod == null) {
            return null;
        }

        boolean isStatic = false;
        // check if we're dealing with a static method
        if(currentMethod.hasAttribute("stat")){
            if(currentMethod.get("stat").equals("static")){
                isStatic = true;
            }
        }
        // can't use 'this' in static context
        if(isStatic){
            if(callerName.equals("this")){
                Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Can't use 'this' in static context");
                reports.add(report);
                return null;
            }
        }

        // check the parameter types
        List<Symbol> STparameterSymbols = symbolTable.getParameters(calleeName);
        if(STparameterSymbols != null) {

            List<Type> STparameterTypes = STparameterSymbols.stream()
                    .map(Symbol::getType)
                    .collect(Collectors.toList());

        List<Type> parameterTypes = new ArrayList<>();
        List<JmmNode> children = jmmNode.getChildren();
        for (int i = 1; i < children.size(); i++) {  // Start from index 1 (second child)
            JmmNode parameter = children.get(i);
            Type childType = visit(parameter, reports);
            parameterTypes.add(childType);
        }
        // the number of parameters must be the same
        if(STparameterTypes.size() != parameterTypes.size()){
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method expects " + STparameterTypes.size() + " parameters, " + parameterTypes.size() + " given");
            reports.add(report);
            return null;
        }
        // the types of the parameters must be the same
        for(int i = 0; i < parameterTypes.size(); i++){
            if(!STparameterTypes.get(i).equals(parameterTypes.get(i))){
                Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Parameter " + i + " of method " + calleeName + " is of type " + STparameterTypes.get(i) + ", " + parameterTypes.get(i) + " given");
                reports.add(report);
                return null;
            }
        }
        }

        Type caleeReturnType = null;
        caleeReturnType = symbolTable.getReturnType(calleeName);
        // Check if the method is declared in the class
        List<String> methods = symbolTable.getMethods();
        for (var method : methods) {
            if (method.equals(calleeName)) {
                Type returnType = symbolTable.getReturnType(calleeName);
                return returnType;
            }
        }

        // Check if the caller variable is a declared imported class
        List<String> imports = symbolTable.getImports();
        var allLocalVars = symbolTable.getLocalVariables(currentMethod.get("name"));
        Type callerType = null;
        for (var localvar : allLocalVars) {
            if (localvar.getName().equals(callerName)) {
                callerType = localvar.getType();
            }
        }
        if (callerType != null) {
            for (var importClass : imports) {
                if (importClass.equals(callerType.getName())) {
                    return visit(child, reports);
                }
            }
        }

        // Check if the caller variable is an undeclared imported class
        if(imports.contains(callerName)){
            return visit(child, reports);
        }

        /*
         * check if the caller variable is an instance of a class that extends import
         * if so, assume all methods are declared
         */
        if (callerType != null) {
            var extend = symbolTable.getSuper();
            var classname = symbolTable.getClassName();
            if (callerType.getName().equals(classname)) {
                if (imports.contains(extend)) {
                    return visit(child, reports);
                }
            }
        }

        // Otherwise the method is not declared
        Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + calleeName + " is not declared");
        reports.add(report);
        return null;
    }
    private Type dealWithMethodCall(JmmNode jmmNode, List<Report> reports) {
        String methodName = jmmNode.get("value");
        var allmethod = symbolTable.getMethods();
        if(!allmethod.contains(methodName)){
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method " + methodName + " is not declared");
            reports.add(report);
            return null;
        }
        // check the parameter types
        List<Symbol> STparameterSymbols = symbolTable.getParameters(methodName);
        List<Type> STparameterTypes = STparameterSymbols.stream()
                .map(Symbol::getType)
                .collect(Collectors.toList());

        List<Type> parameterTypes = new ArrayList<>();
        for(var child: jmmNode.getChildren()){
            Type childType = visit(child, reports);
            parameterTypes.add(childType);
        }
        // the number of parameters must be the same
        if(STparameterTypes.size() != parameterTypes.size()){
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Method expects " + STparameterTypes.size() + " parameters, " + parameterTypes.size() + " given");
            reports.add(report);
            return null;
        }
        // the types of the parameters must be the same
        for(int i = 0; i < parameterTypes.size(); i++){
            if(!STparameterTypes.get(i).equals(parameterTypes.get(i))){
                Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), "Parameter " + i + " of method " + methodName + " is of type " + STparameterTypes.get(i) + ", " + parameterTypes.get(i) + " given");
                reports.add(report);
                return null;
            }
        }
        return null;
    }

    private Type dealWithArrayAccess(JmmNode jmmNode, List<Report> reports) {
        JmmNode child = jmmNode.getJmmChild(0);
        Type childType = visit(child, reports);
        if (childType == null) {
            return null;
        }
        // Check if the child is an array
        if (!childType.isArray()) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), childType + " is not an array");
            reports.add(report);
            return null;
        }
        // Check if the index is an integer
        JmmNode index = jmmNode.getJmmChild(1);
        Type indexType = visit(index, reports);
        if (indexType == null) {
            return null;
        }
        if (!indexType.getName().equals("int")) {
            Report report = new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(jmmNode.get("lineStart")), Integer.parseInt(jmmNode.get("colStart")), indexType + " is not an integer");
            reports.add(report);
            return null;
        }
        return new Type("int", false);
    }
}