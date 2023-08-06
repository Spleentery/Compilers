package pt.up.fe.comp2023;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.Method;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import javax.lang.model.element.TypeElement;
import java.util.*;

public class JasminBackendImplemented
        implements JasminBackend {

    private String className;
    private String superName;
    private ArrayList<String> imports;
    private int limitMethodStack;
    private int limitMethodLocal;
    private int currentMethodStackValues;
    private int methodLabelCounter;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {

        ClassUnit ollirClassUnit = ollirResult.getOllirClass();
        this.className = ollirClassUnit.getClassName();
        this.imports = ollirClassUnit.getImports();
        String jasminCode = ollirCodeToJasminCode(ollirClassUnit);
        List<Report> reports = ollirResult.getReports();
        Map<String, String> config = ollirResult.getConfig();

        return new JasminResult(this.className, jasminCode, reports, config);
    }

    public String ollirCodeToJasminCode(ClassUnit ollirClassUnit) {

        // Build jasmin code in sequence: class -> fields -> methods
        String classesJasmin = convertClassToJasmin(ollirClassUnit);
        String fieldsJasmin = convertFieldsToJasmin(ollirClassUnit);
        String methodsJasmin = convertMethodsToJasmin(ollirClassUnit);

        return classesJasmin.concat(fieldsJasmin).concat(methodsJasmin);
    }

    // --------------------------------------------------------------------
    // ----------------------- Access Modifiers ---------------------------
    // --------------------------------------------------------------------

    public String dealWithAccessModifier(AccessModifiers accessModifier) {

        // Get access modifier
        String accessModifierString = String.valueOf(accessModifier);

        // Deal with special "DEFAULT" case
        if (accessModifierString.equals("DEFAULT")) {
            accessModifierString = "public";
        }

        return accessModifierString.toLowerCase();
    }

    // --------------------------------------------------------------------
    // ----------------------------- Types --------------------------------
    // --------------------------------------------------------------------

    // Convert Ollir types to Jasmin types
    public String dealWithType(Type type) {

        ElementType elemType = type.getTypeOfElement();

        // Integers
        if (elemType == ElementType.INT32) {
            return "I";
        }
        // Booleans
        if (elemType == ElementType.BOOLEAN) {
            return "Z";
        }
        // Arrays
        if (elemType == ElementType.ARRAYREF) {
            return "[".concat(dealWithType(((ArrayType) type).getElementType()));
        }
        // This
        if (elemType == ElementType.THIS) {
            return "L".concat(this.className).concat(";");
        }
        // References to objects
        if (elemType == ElementType.OBJECTREF || elemType == ElementType.CLASS) {
            return "L".concat(getClassFQN(((ClassType) type).getName())).concat(";");
        }
        // Strings
        if (elemType == ElementType.STRING) {
            return "Ljava/lang/String;";
        }
        // Void
        if (elemType == ElementType.VOID) {
            return "V";
        }
        return "";
    }

    // Depending on return type, find suitable char to concat with return (ex. ireturn)
    public String convertReturnTypeToJasmin(Type type) {

        String returnTypeJasmin = "";
        ElementType elementType = type.getTypeOfElement();

        if (elementType.equals(ElementType.INT32) || elementType.equals(ElementType.BOOLEAN)) {
            returnTypeJasmin = "i";
        }
        if (elementType.equals(ElementType.ARRAYREF) || elementType.equals(ElementType.OBJECTREF) || elementType.equals(ElementType.CLASS) || elementType.equals(ElementType.THIS) || elementType.equals(ElementType.STRING)) {
            returnTypeJasmin = "a";
        }
        if (elementType.equals(ElementType.VOID)) {
            returnTypeJasmin = "";
        }
        return returnTypeJasmin;
    }

    // --------------------------------------------------------------------
    // ----------------------------- Class --------------------------------
    // --------------------------------------------------------------------

    public String convertClassToJasmin(ClassUnit ollirClassUnit) {

        String className = this.className;
        // Get access modifier
        String classAccessModifier = dealWithAccessModifier(ollirClassUnit.getClassAccessModifier());
        
        String superClass = ollirClassUnit.getSuperClass();
        // Get super class name
        this.superName = superClass;

        // Build class string with access modifier and name
        String classJasmin = ".class ".concat(classAccessModifier).concat(" ");

        // Check for keywords
        if (ollirClassUnit.isStaticClass()) {
            classJasmin = classJasmin.concat("static ");
        }
        if (ollirClassUnit.isFinalClass()) {
            classJasmin = classJasmin.concat("final ");
        }

        classJasmin = classJasmin.concat(className).concat("\n");

        // If no super class, add common Java Object superclass
        if (superClass == null) {
            superName = "java/lang/Object";
        }
        // Add Super class
        String superJasmin = ".super ".concat(superName).concat("\n");
        return classJasmin.concat(superJasmin);
    }

    // --------------------------------------------------------------------
    // ----------------------------- Field --------------------------------
    // --------------------------------------------------------------------

    public String convertFieldsToJasmin(ClassUnit ollirClassUnit) {
        ArrayList<Field> fields = ollirClassUnit.getFields();

        String fieldsJasmin = "";

        // Build the string for each field
        for (Field field : fields) {
            String fieldJasmin = convertFieldToJasmin(field);
            fieldsJasmin = fieldsJasmin.concat(fieldJasmin);
        }

        return fieldsJasmin.concat("\n");
    }

    public String convertFieldToJasmin(Field field) {

        String fieldName = field.getFieldName();
        // Get access Modifier
        String fieldAccessModifier = dealWithAccessModifier(field.getFieldAccessModifier());

        String fieldTypeJasmin = dealWithType(field.getFieldType());
        // Initiate string with access modifier
        String fieldJasmin = ".field ".concat(fieldAccessModifier).concat(" ");

        // Add keywords
        if (field.isStaticField()) {
            fieldJasmin = fieldTypeJasmin.concat("static ");
        }
        if (field.isFinalField()) {
            fieldJasmin = fieldTypeJasmin.concat("final ");
        }

        // Add field name and type
        fieldJasmin = fieldJasmin.concat(fieldName).concat(" ").concat(fieldTypeJasmin);

        // If field is initialized, add initial value
        if (field.isInitialized()) {
            int initialFieldValue = field.getInitialValue();
            fieldJasmin = fieldJasmin.concat(" = ").concat(String.valueOf(initialFieldValue));
        }
        return fieldJasmin.concat("\n");
    }

    // --------------------------------------------------------------------
    // ----------------------------- Method -------------------------------
    // --------------------------------------------------------------------

    public String convertMethodsToJasmin(ClassUnit ollirClassUnit) {
        ArrayList<Method> methods = ollirClassUnit.getMethods();

        String methodsJasmin = "";

        // Build jasmin String for each method restarting the stack and local values counters
        for (Method method : methods) {
            resetMethodLimitValues();
            String methodJasmin = convertMethodToJasmin(method);
            methodsJasmin = methodsJasmin.concat(methodJasmin);
        }

        return methodsJasmin;
    }

    public String convertMethodToJasmin(Method method) {

        // (\w+)\(\)\[I

        String methodAccessModifier = dealWithAccessModifier(method.getMethodAccessModifier());
        String methodName = method.getMethodName();

        // Start method name with access modifier
        String methodJasmin = ".method ".concat(methodAccessModifier).concat(" ");

        // Add needed keywords
        if (method.isStaticMethod()) {
            methodJasmin = methodJasmin.concat("static ");
        }
        if (method.isFinalMethod()) {
            methodJasmin = methodJasmin.concat("final ");
        }

        // Check if it's a constructor method
        if (method.isConstructMethod()) {
            methodJasmin = methodJasmin.concat("<init>");
        }
        else {
            methodJasmin = methodJasmin.concat(methodName);
        }

        methodJasmin = methodJasmin.concat("(");
        ArrayList<Element> parameters = method.getParams();

        // Add parameters to function
        for (Element parameter : parameters) {
            methodJasmin = methodJasmin.concat(dealWithType(parameter.getType()));
        }

        /////////////////////////////////////////////////////////////////

        // Add return type
        String methodReturnType = dealWithType(method.getReturnType());
        methodJasmin = methodJasmin.concat(")").concat(methodReturnType).concat("\n");

        /////////////////////////////////////////////////////////////////

        HashMap<String, Descriptor> varTable = method.getVarTable();
        ArrayList<Instruction> instructions = method.getInstructions();
        boolean isReturning = false;

        String methodInstructions = "";

        // Deal with each instruction of the method
        for (Instruction instruction : instructions) {

            String instructionJasmin = "";

            List<String> instructionLabel = method.getLabels(instruction);
            if (!instructionLabel.isEmpty()) {
                instructionJasmin = instructionJasmin.concat(instructionLabel.get(0)).concat(":\n");
            }

            instructionJasmin = instructionJasmin.concat(convertInstructionToJasmin(instruction, varTable));
            methodInstructions = methodInstructions.concat(instructionJasmin);
            InstructionType instructionType = instruction.getInstType();

            // Check if it's a no operation instruction, and skips to the next one if it is
            if (instructionType != InstructionType.NOPER) {
                methodInstructions = methodInstructions.concat("\n");
            }
            // Check if the instruction is returning
            if (instructionType == InstructionType.RETURN) {
                isReturning = true;
            }
            // Add pop instruction to discard the return value to keep stack consistent
            if (instructionType == InstructionType.CALL && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {
                consumeStack(1);
                methodInstructions = methodInstructions.concat("pop\n");
            }
        }

        /////////////////////////////////////////////////////////////////

        // Temporary Stack and Local Values
        //this.limitMethodStack = 99;
        this.limitMethodLocal = 97;

        // Add Calculated stack and local values to method
        methodJasmin = methodJasmin.concat(".limit stack ").concat(String.valueOf(this.limitMethodStack)).concat("\n");
        methodJasmin = methodJasmin.concat(".limit locals ").concat(String.valueOf(this.limitMethodLocal)).concat("\n\n");

        // Add Instructions to method
        methodJasmin = methodJasmin.concat(methodInstructions);

        // Add returning statement if method returns
        if (!isReturning) {
            methodJasmin = methodJasmin.concat("return\n");
        }

        // Finish Method
        return methodJasmin.concat(".end method\n\n");
    }

    // --------------------------------------------------------------------
    // -------------------------- Method Limits ---------------------------
    // --------------------------------------------------------------------

    private void calculateMethodLocals() {

    }

    private void resetMethodLimitValues () {
        this.currentMethodStackValues = 0;
        this.limitMethodLocal = 0;
        this.limitMethodStack = 0;
    }

    // If current stack counter is greater than the limit, update the limit
    private void increaseStack(int i){
        this.currentMethodStackValues = this.currentMethodStackValues + i;
        if (this.currentMethodStackValues > this.limitMethodStack ) {
            this.limitMethodStack = this.currentMethodStackValues;
        }
    }

    // Subtract stack registers
    private void consumeStack(int i){
        this.currentMethodStackValues = this.currentMethodStackValues - i;
    }

    // --------------------------------------------------------------------
    // ------------------------ Stack Management --------------------------
    // --------------------------------------------------------------------

    private String dealWithCastingForStacking(Element operand, HashMap<String, Descriptor> varTable) {
        if (operand instanceof LiteralElement) {
            return stacker((LiteralElement) operand);
        }
        return stacker((Operand) operand, varTable);
    }

    // Add literals to the stack and up count the stack registers
    public String stacker(LiteralElement literalElement) {

        String stackJasmin = "";
        ElementType elementType = literalElement.getType().getTypeOfElement();
        String literal = literalElement.getLiteral();

        if (elementType.equals(ElementType.INT32) || elementType.equals(ElementType.BOOLEAN)) {
            stackJasmin = stackJasmin.concat(optimizeLiteralIntPush(literal));
        }
        else {
            stackJasmin = stackJasmin.concat("ldc ").concat(literal);
        }
        increaseStack(1);
        return stackJasmin.concat("\n");
    }

    // Add an operand to the stack and up count the stack registers
    public String stacker(Operand operand, HashMap<String, Descriptor> varTable) {

        String stackJasmin = "";
        String name = (operand).getName();
        int varNum = varTable.get(name).getVirtualReg();
        ElementType elementType = operand.getType().getTypeOfElement();

        if (elementType.equals(ElementType.INT32) || elementType.equals(ElementType.BOOLEAN)) {
            stackJasmin = stackJasmin.concat("iload");
        }
        else if (elementType.equals(ElementType.THIS)) {
            stackJasmin = stackJasmin.concat("aload");
            varNum = 0;
        }
        else {
            stackJasmin = stackJasmin.concat("aload");
        }

        if (operand instanceof ArrayOperand) {
            stackJasmin = stackJasmin.concat(stacker((Operand) ((ArrayOperand) operand).getIndexOperands().get(0), varTable));
            stackJasmin = stackJasmin.concat("iaload\n");
            increaseStack(1);
        }

        increaseStack(1);
        return stackJasmin.concat(optimizeShort(varNum)).concat(String.valueOf(varNum)).concat("\n");
    }

    // --------------------------------------------------------------------
    // --------------------------- Optimization ---------------------------
    // --------------------------------------------------------------------

    public String optimizeShort(int varNum) {
        String optimizedChar = " ";
        if (0 <= varNum && varNum <= 3) {
            optimizedChar = "_";
        }
        return optimizedChar;
    }

    public String optimizeLiteralIntPush(String literal) {

        String optimizedPush = "";
        int literalInt = Integer.parseInt(literal);

        if (-1 <= literalInt && literalInt <= 5) {
            optimizedPush = optimizedPush.concat("iconst_");
            if (literalInt == -1) {
                optimizedPush = optimizedPush.concat("m");
            }
            optimizedPush = optimizedPush.concat(literal);
        }
        else if (-128 <= literalInt && literalInt <= 127) {
            optimizedPush = optimizedPush.concat("bipush ").concat(literal);
        }
        else if (-32768 <= literalInt && literalInt <= 32767) {
            optimizedPush = optimizedPush.concat("sipush ").concat(literal);
        }
        else {
            optimizedPush = optimizedPush.concat("ldc ").concat(literal);
        }
        return optimizedPush;
    }

    private Boolean checkIfZero(Element operand, HashMap<String, Descriptor> varTable) {
        if (operand.isLiteral()) {
            return ((LiteralElement) operand).getLiteral().equals("0");
        } else {
            return varTable.get(((Operand) operand).getName()).getVirtualReg() == 0;
        }
    }

    private String pickBestComparison(Element leftOperand, Element rightOperand, HashMap<String, Descriptor> varTable) {

        String bestComparator = "";
        boolean isLeftOperandZero = checkIfZero(leftOperand, varTable);
        boolean isRightOperandZero = checkIfZero(rightOperand, varTable);
        String stackedLeftOperand = dealWithCastingForStacking(leftOperand, varTable);
        String stackedRightOperand = dealWithCastingForStacking(rightOperand, varTable);

        if (isLeftOperandZero) {
            bestComparator = bestComparator.concat(stackedRightOperand).concat("ifgt");
        } else if (isRightOperandZero) {
            bestComparator = bestComparator.concat(stackedLeftOperand).concat("iglt");
        } else {
            bestComparator = bestComparator.concat(stackedLeftOperand);
            bestComparator = bestComparator.concat(stackedRightOperand);
            bestComparator = bestComparator.concat("if_icmplt");
        }
        return bestComparator;
    }

    // --------------------------------------------------------------------
    // --------------------------- Instructions ---------------------------
    // --------------------------------------------------------------------

    public String convertInstructionToJasmin(Instruction instruction, HashMap<String, Descriptor> varTable) {

        String instructionJasmin = "";
        InstructionType instructionType = instruction.getInstType();

        // Deal with each instruction type:

        if (instructionType.equals(InstructionType.ASSIGN)) {
            instructionJasmin = instructionJasmin.concat(assignInstructionToJasmin((AssignInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.CALL)) {
            instructionJasmin = instructionJasmin.concat(callInstructionToJasmin((CallInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.GOTO)) {
            instructionJasmin = instructionJasmin.concat(gotoInstructionToJasmin((GotoInstruction) instruction));
        }
        if (instructionType.equals(InstructionType.BRANCH)) {
            instructionJasmin = instructionJasmin.concat(branchInstructionToJasmin((CondBranchInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.RETURN)) {
            instructionJasmin = instructionJasmin.concat(returnInstructionToJasmin((ReturnInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.PUTFIELD)) {
            instructionJasmin = instructionJasmin.concat(putFieldInstructionToJasmin((PutFieldInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.GETFIELD)) {
            instructionJasmin = instructionJasmin.concat(getFieldInstructionToJasmin((GetFieldInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.UNARYOPER)) {
            instructionJasmin = instructionJasmin.concat(unaryOperInstructionToJasmin((UnaryOpInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.BINARYOPER)) {
            instructionJasmin = instructionJasmin.concat(binaryOperInstructionToJasmin((BinaryOpInstruction) instruction, varTable));
        }
        if (instructionType.equals(InstructionType.NOPER)) {
            instructionJasmin = instructionJasmin.concat(nOperInstructionToJasmin((SingleOpInstruction) instruction, varTable));
        }
        return instructionJasmin;
    }

    // --------------------- Assign ---------------------

    public String assignInstructionToJasmin(AssignInstruction instruction, HashMap<String, Descriptor> varTable) {

        String assignInsJasmin = "";
        Type assignType = instruction.getTypeOfAssign();
        Operand dest = (Operand) instruction.getDest();
        Instruction rhs = instruction.getRhs();

        assignInsJasmin = assignInsJasmin.concat(convertInstructionToJasmin(rhs, varTable));

        if (rhs.getInstType() != InstructionType.NOPER) {
            assignInsJasmin = assignInsJasmin.concat("");
        }

        int varNum = varTable.get(dest.getName()).getVirtualReg();
        return assignInsJasmin.concat(convertReturnTypeToJasmin(assignType)).concat("store").concat(optimizeShort(varNum)).concat(String.valueOf(varNum)).concat("\n");
    }

    // ---------------------- Call ----------------------

    public  String dealWithCallType(CallType invocationType, Operand firstArg) {

        if (invocationType.equals(CallType.invokevirtual)) {
            return "invokevirtual ";
        }
        if (invocationType.equals(CallType.invokeinterface)) {
            return "invokeinterface ";
        }
        if (invocationType.equals(CallType.invokespecial)) {
            return "invokespecial ";
        }
        if (invocationType.equals(CallType.invokestatic)) {
            return "invokestatic ";
        }
        if (invocationType.equals(CallType.NEW)) {
            if (firstArg.getType().getTypeOfElement() == ElementType.ARRAYREF) {
                consumeStack(1);
                return "newarray int";
            }
            return "new ";
        }
        if (invocationType.equals(CallType.arraylength)) {
            consumeStack(1);
            return "arraylength ";
        }
        else {
            return "ldc ";
        }
    }

    // ---------------------------------------------------

    public String getClassFQN(String className) {

        if (className.equals("this")) {
            return this.className;
        }

        for (String importedClass : this.imports) {
            if (importedClass.endsWith(className)) {
                return importedClass.replace("\\.", "/");
            }
        }
        return className;
    }

    // ---------------------------------------------------

    public String callInstructionToJasmin(CallInstruction instruction, HashMap<String, Descriptor> varTable) {

        String callInsJasmin = "";

        CallType invocationType = instruction.getInvocationType();
        Operand firstArg = (Operand) instruction.getFirstArg();

        if (invocationType == CallType.invokespecial || invocationType == CallType.invokevirtual) {
            callInsJasmin = callInsJasmin.concat(stacker(firstArg, varTable));
        }

        ArrayList<Element> operands = instruction.getListOfOperands();

        for (Element operand : operands) {
            callInsJasmin = callInsJasmin.concat(dealWithCastingForStacking(operand, varTable));
        }

        /////////////////////////////////////////////////////////////////

        callInsJasmin = callInsJasmin.concat(dealWithCallType(invocationType, firstArg));

        Type firstArgClassType = firstArg.getType();
        LiteralElement secondArg = (LiteralElement) instruction.getSecondArg();

        if (invocationType == CallType.invokespecial && firstArgClassType.getTypeOfElement() == ElementType.THIS) {
            if (this.superName != null) {
                callInsJasmin = callInsJasmin.concat(this.superName);
            }
        }
        else if (invocationType == CallType.invokevirtual || invocationType == CallType.invokespecial) {
            callInsJasmin = callInsJasmin.concat(getClassFQN(((ClassType) firstArgClassType).getName()));
        }
        else {
            callInsJasmin = callInsJasmin.concat(getClassFQN(firstArg.getName()));
        }

        /////////////////////////////////////////////////////////////////

        if (invocationType == CallType.invokespecial) {
            callInsJasmin = callInsJasmin.concat("/<init>(");
        }
        else if (invocationType != CallType.NEW) {
            callInsJasmin = callInsJasmin.concat("/").concat(secondArg.getLiteral().replace("\"", "")).concat("(");
        }
        else {
            callInsJasmin = callInsJasmin.concat("\ndup");
        }

        /////////////////////////////////////////////////////////////////

        for (Element operand : operands) {
            callInsJasmin = callInsJasmin.concat(dealWithType(operand.getType()));
        }

        /////////////////////////////////////////////////////////////////

        Type returnType = instruction.getReturnType();

        if (invocationType != CallType.NEW) {
            callInsJasmin = callInsJasmin.concat(")").concat(dealWithType(returnType));
        }

        return callInsJasmin.concat("\n");
    }

    // --------------------- Go To ----------------------

    public String gotoInstructionToJasmin(GotoInstruction instruction) {

        String gotoInsJasmin = "";
        String gotoLabel = instruction.getLabel();
        gotoInsJasmin = gotoInsJasmin.concat("goto ").concat(gotoLabel);

        return gotoInsJasmin.concat("\n");
    }

    // --------------------- Branch ---------------------

    public String branchInstructionToJasmin(CondBranchInstruction instruction, HashMap<String, Descriptor> varTable) {

        String branchInsJasmin = "";
        List<Element> operands = instruction.getOperands();
        Element operand1 = operands.get(0);
        String label = instruction.getLabel();

        // Deal with 1 operand conditions
        if (operands.size() == 1) {
            branchInsJasmin = branchInsJasmin.concat(dealWithCastingForStacking(operand1, varTable));
        }

        // Deal with 2 operands conditions
        if (operands.size() == 2) {
            Element operand2 = operands.get(1);
            Instruction branchCondition = instruction.getCondition();

            if (branchCondition instanceof BinaryOpInstruction) {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) branchCondition;
                OperationType binaryOperation = binaryOpInstruction.getOperation().getOpType();
                String resultOp = booleanBinaryOperationToJasmin(binaryOperation, operand1, operand2, varTable);
                branchInsJasmin = branchInsJasmin.concat(resultOp);
            }
        }

        branchInsJasmin = branchInsJasmin.concat("ifne ").concat(label);

        return branchInsJasmin.concat("\n");
    }

    // --------------------- Return --------------------- X

    public String returnInstructionToJasmin(ReturnInstruction instruction, HashMap<String, Descriptor> varTable) {

        String returnInsJasmin = "";
        Element operand = instruction.getOperand();
        Type returnType = instruction.getReturnType();

        if (instruction.getElementType() == ElementType.VOID) {
            returnInsJasmin = returnInsJasmin.concat("");
        } else if (operand.isLiteral()) {
            returnInsJasmin = returnInsJasmin.concat(stacker((LiteralElement) operand));
        } else {
            returnInsJasmin = returnInsJasmin.concat(stacker((Operand) operand, varTable));
        }

        returnInsJasmin = returnInsJasmin.concat(convertReturnTypeToJasmin(returnType)).concat("return");
        return returnInsJasmin.concat("\n");
    }

    // -------------------- Put Field ------------------- X

    public String putFieldInstructionToJasmin(PutFieldInstruction instruction, HashMap<String, Descriptor> varTable) {

        String putFieldInsJasmin = "";
        Operand firstOperand = (Operand) instruction.getFirstOperand();
        Operand secondOperand = (Operand) instruction.getSecondOperand();
        LiteralElement thirdOperand = (LiteralElement) instruction.getThirdOperand();

        putFieldInsJasmin = putFieldInsJasmin.concat(stacker(firstOperand, varTable)).concat(stacker(thirdOperand));

        String fstOpName = firstOperand.getName();
        String fstOpFQN = getClassFQN(fstOpName);
        String sndOpName = secondOperand.getName();
        Type sndOpType = secondOperand.getType();

        // Consumes object reference and new field value
        consumeStack(2);

        return putFieldInsJasmin.concat("putfield ").concat(fstOpFQN).concat("/").concat(sndOpName).concat(" ").concat(dealWithType(sndOpType));
    }

    // -------------------- Get Field ------------------- X

    public String getFieldInstructionToJasmin(GetFieldInstruction instruction, HashMap<String, Descriptor> varTable) {

        String getFieldInsJasmin = "";
        Operand firstOperand = (Operand) instruction.getFirstOperand();
        Operand secondOperand = (Operand) instruction.getSecondOperand();
        Type fieldType = instruction.getFieldType();

        getFieldInsJasmin = getFieldInsJasmin.concat(stacker(firstOperand, varTable));

        String fstOpName = firstOperand.getName();
        String fstOpFQN = getClassFQN(fstOpName);
        String sndOpName = secondOperand.getName();

        // Redundancy used to better explain how this instruction interacts with the stack
        // Consumes Object Reference
        consumeStack(1);
        // Pushes the field value onto the stack
        increaseStack(1);

        return getFieldInsJasmin.concat("getField ").concat(fstOpFQN).concat("/").concat(sndOpName).concat(" ").concat(dealWithType(fieldType));
    }

    // ---------------------- Unary --------------------- X

    private String addNegationJasmin() {

        String operandNegation = "";
        String methodLabelCounter = String.valueOf(this.methodLabelCounter);

        operandNegation = operandNegation.concat("ifeq ifFalse").concat(methodLabelCounter).concat(":\n");
        // Push "false" to stack
        operandNegation = operandNegation.concat("iconst_0\n");
        operandNegation = operandNegation.concat("goto endLabel").concat(methodLabelCounter).concat(":\n");
        operandNegation = operandNegation.concat("ifFalse").concat(methodLabelCounter).concat(":\n");
        // Push "true" to stack
        operandNegation = operandNegation.concat("iconst_1\n");
        operandNegation = operandNegation.concat("endLabel").concat(methodLabelCounter).concat(":\n");

        // Consume value to negate
        consumeStack(1);
        // Push result
        increaseStack(1);

        this.methodLabelCounter = this.methodLabelCounter + 1;
        return operandNegation;
    }

    public String unaryOperInstructionToJasmin(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {

        String unaryOperInsJasmin = "";
        Element operand = instruction.getOperand();
        Operation operation = instruction.getOperation();
        OperationType operationType = operation.getOpType();
        unaryOperInsJasmin = unaryOperInsJasmin.concat(stacker((Operand) operand, varTable));

        // Only unary operation implemented is the "NOT"
        if (operationType == OperationType.NOT || operationType == OperationType.NOTB) {
            unaryOperInsJasmin = unaryOperInsJasmin.concat(addNegationJasmin());
        }
        return unaryOperInsJasmin.concat("\n");
    }

    // ---------------------- Binary -------------------- X

    private String binaryOperInstructionToJasmin(BinaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {

        String binaryOperInsJasmin = "";
        Element leftOperand = instruction.getLeftOperand();
        Element rightOperand = instruction.getRightOperand();
        Operation operation = instruction.getOperation();
        OperationType operationType = operation.getOpType();

        if (operationType == OperationType.LTH || operationType == OperationType.GTE || operationType == OperationType.ANDB) {
            String booleanBinaryOperationString = booleanBinaryOperationToJasmin(operationType, leftOperand, rightOperand, varTable);
            binaryOperInsJasmin = binaryOperInsJasmin.concat(booleanBinaryOperationString);
        } else {
            String integerBinaryOperationString = integerBinaryOperationToJasmin(operationType, leftOperand, rightOperand, varTable);
            binaryOperInsJasmin = binaryOperInsJasmin.concat(integerBinaryOperationString);
        }

        // Consume top 2 stack values
        consumeStack(2);
        // Pushes result
        increaseStack(1);

        return binaryOperInsJasmin.concat("\n");
    }

    // ---------------------- No Op --------------------- X

    public String nOperInstructionToJasmin(SingleOpInstruction instruction, HashMap<String, Descriptor> varTable) {

        String nOperInsJasmin = "";
        Element singleOperand = instruction.getSingleOperand();
        // Load into stack (adds 1 to stack limit)
        nOperInsJasmin = nOperInsJasmin.concat(dealWithCastingForStacking(singleOperand,varTable));
        return nOperInsJasmin;
    }

    // --------------------------------------------------------------------
    // ---------------------------- Operations ----------------------------
    // --------------------------------------------------------------------

    private String makeComparison() {
        String comparison = "";
        String methodLabelCounter = String.valueOf(this.methodLabelCounter);

        comparison = comparison.concat(" ifFalse").concat(methodLabelCounter).concat("\n");
        // Push "true" to stack
        comparison = comparison.concat("iconst_0\n");
        comparison = comparison.concat("goto endLabel").concat(methodLabelCounter).concat("\n");
        comparison = comparison.concat("ifFalse").concat(methodLabelCounter).concat(":\n");
        // Push "false" to stack
        comparison = comparison.concat("iconst_1\n");
        comparison = comparison.concat("endLabel").concat(methodLabelCounter).concat(":\n");

        // Consume value to negate
        consumeStack(1);
        // Push result
        increaseStack(1);

        this.methodLabelCounter = this.methodLabelCounter + 1;
        return comparison;
    }

    public String booleanBinaryOperationToJasmin(OperationType operationType, Element leftOperand, Element rightOperand, HashMap<String, Descriptor> varTable) {

        String booleanOperationJasmin = "";

        String stackedLeftOperand = dealWithCastingForStacking(leftOperand, varTable);
        String stackedRightOperand = dealWithCastingForStacking(rightOperand, varTable);

        if (operationType == OperationType.ANDB) {
            booleanOperationJasmin = booleanOperationJasmin.concat(stackedLeftOperand);
            booleanOperationJasmin = booleanOperationJasmin.concat(stackedRightOperand);
            booleanOperationJasmin = booleanOperationJasmin.concat("iand");
        } else {
            if (operationType == OperationType.LTH) {
                booleanOperationJasmin = booleanOperationJasmin.concat(pickBestComparison(leftOperand, rightOperand, varTable));
                booleanOperationJasmin = booleanOperationJasmin.concat(makeComparison());
            } else {
                booleanOperationJasmin = booleanOperationJasmin.concat(pickBestComparison(rightOperand, leftOperand, varTable));
                booleanOperationJasmin = booleanOperationJasmin.concat(makeComparison());
            }
        }

        return booleanOperationJasmin.concat("\n");
    }

    public String integerBinaryOperationToJasmin(OperationType operationType, Element leftOperand, Element rightOperand, HashMap<String, Descriptor> varTable) {

        String operationJasmin = "";

        // Push operand values into stack
        operationJasmin = operationJasmin.concat(dealWithCastingForStacking(leftOperand,varTable));
        operationJasmin = operationJasmin.concat(dealWithCastingForStacking(rightOperand,varTable));

        // Add Operation to be executed
        if (operationType.equals(OperationType.ADD)) {
            operationJasmin = operationJasmin.concat("iadd");
        }
        if (operationType.equals(OperationType.SUB)) {
            operationJasmin = operationJasmin.concat("isub");
        }
        if (operationType.equals(OperationType.MUL)) {
            operationJasmin = operationJasmin.concat("imul");
        }
        if (operationType.equals(OperationType.DIV)) {
            operationJasmin = operationJasmin.concat("idiv");
        }
        return operationJasmin;
    }
    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
}
