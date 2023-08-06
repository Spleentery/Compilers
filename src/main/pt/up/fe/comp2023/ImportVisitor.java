package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
import java.util.Map;

public class ImportVisitor extends AJmmVisitor<List<String>, List<String>> {
    @Override
    protected void buildVisitor() {
        addVisit("ImportList", this::traverseImportList);
        //setDefaultVisit(this::dummyVisit);
    }
    private List<String> dummyVisit(JmmNode node, List<String> l) {
        return l;
    }

    private List<String> traverseImportList(JmmNode root, List<String> l) {

        for(JmmNode pckg : root.getChildren()) {
            JmmNode pid = pckg.getJmmChild(0);
            StringBuilder name = new StringBuilder(pid.get("id"));

            while(pid.getNumChildren() > 0) {
                pid = pid.getJmmChild(0);
                name.append('.').append(pid.get("id"));
            }

            l.add(name.toString());
        }

        return l;
    }
}
