package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassVisitor extends AJmmVisitor<ClassInfo, ClassInfo> {
    @Override
    protected void buildVisitor() {
        addVisit("ClassDeclaration", this::processClass);
        //setDefaultVisit(this::dummyVisit);
    }
    private ClassInfo dummyVisit(JmmNode node, ClassInfo c) {
        return c;
    }

    private ClassInfo processClass(JmmNode root, ClassInfo c) {
        String name = root.get("name");
        String super_class = "";
        if(root.hasAttribute("superclass")){
            super_class = root.get("superclass");
        }

        List<Symbol> fields = new ArrayList<>();

        for(JmmNode child : root.getChildren()) {
            if (child.getKind().equals("VarList")) {
                fields = TypeUtils.parseVars(child);
                break;
            }
        }

        return new ClassInfo(name, super_class, fields);
    }
}
