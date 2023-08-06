package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class Method {

    private final String methodName;
    private final Type returnType;
    private final List<Symbol> parameters;
    private final List<Symbol> localVariables;

    public Method(String methodName, Type returnType, List<Symbol> parameters, List<Symbol> localVariables) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameters = parameters;
        this.localVariables = localVariables;
    }

    public String getName() {
        return methodName;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Symbol> getMethodParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return localVariables;
    }
}
