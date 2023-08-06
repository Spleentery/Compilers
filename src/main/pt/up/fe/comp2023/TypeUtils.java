package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;

public class TypeUtils {
    public static Type getType(JmmNode node) {
        switch(node.getKind()) {
            case "TypeInteger" -> { return new Type("int", false); }
            case "TypeIntegerArray" -> { return new Type("int", true); }
            case "TypeBoolean" -> { return new Type("boolean", false); }
            case "TypeID" -> { return new Type(node.get("id"), false); }
            case "TypeArray" -> { return new Type(node.get("id"), true); }

            default -> { return null; }
        }
    }

    public static List<Symbol> parseVars(JmmNode root) {
        List<Symbol> ret = new ArrayList<>();

        for(JmmNode child : root.getChildren()) {
            ret.add(new Symbol(
                    getType(child.getJmmChild(0)),
                    child.get("name")
            ));
        }
        return ret;
    }
}
