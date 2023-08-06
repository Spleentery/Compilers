package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

public class OllirGenerator implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        OllirVisitor visitor = new OllirVisitor(jmmSemanticsResult);

        return new OllirResult(
                visitor.visit(jmmSemanticsResult.getRootNode(), ""),
                jmmSemanticsResult.getConfig()
        );
    }
}
