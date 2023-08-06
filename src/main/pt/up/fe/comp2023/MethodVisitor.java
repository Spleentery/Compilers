package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodVisitor extends AJmmVisitor<Map<String, Method>, Map<String, Method>> {
    @Override
    protected void buildVisitor() {
        addVisit("MethodList", this::processMethodList);
        //setDefaultVisit(this::dummyVisit);
    }

    private Map<String, Method> dummyVisit(JmmNode node, Map<String, Method> map) {
        return map;
    }

    private Map<String, Method> processMethodList(JmmNode root, Map<String, Method> map) {
        for(JmmNode method : root.getChildren()) {
            Method m = processMethod(method);
            if(m != null) map.put(method.get("name"),processMethod(method));
        }
        return map;
    }

    private Method processMethod(JmmNode method) {
        String name = method.get("name");

        Type ret_type = null;
        List<Symbol> params = new ArrayList<>();
        List<Symbol> locals = new ArrayList<>();

        for(JmmNode child : method.getChildren()) {
            switch (child.getKind()) {
                case "TypeInteger", "TypeIntegerArray", "TypeBoolean", "TypeID", "TypeArray" ->
                        ret_type = TypeUtils.getType(child);
                case "ParameterList" -> params = TypeUtils.parseVars(child);
                case "VarList" -> locals = TypeUtils.parseVars(child);          // Same names and children, hee hoo.
            }
        }

        if(ret_type != null) {
            return new Method(name, ret_type, params, locals);
        }
        return null;
    }
}
