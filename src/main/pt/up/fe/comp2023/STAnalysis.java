package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class STAnalysis implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JavammSymbolTable symbolTable = new JavammSymbolTable(jmmParserResult.getRootNode());
        System.out.println(symbolTable.print());


        ASemanticVisitor semanticVisitor = new ASemanticVisitor(symbolTable);
        System.out.println(jmmParserResult.getRootNode().toTree());

        List<Report> semanticReports = new ArrayList<>();
        semanticVisitor.visit(jmmParserResult.getRootNode(), semanticReports);
        System.out.println("----------------------- Semantic Reports -----------------------\n"+semanticReports);
        return new JmmSemanticsResult(
                jmmParserResult,
                new JavammSymbolTable(jmmParserResult.getRootNode()),
                semanticReports
        );
    }
}
