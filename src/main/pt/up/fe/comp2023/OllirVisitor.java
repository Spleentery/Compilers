package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.sql.Array;
import java.util.*;

public class OllirVisitor extends AJmmVisitor<String, String> {
    private SymbolTable symbolTable;
    private int ifCounter;
    private int whileCounter;
    private int tmpVarCounter;
    private Map<String, String> classVars;
    private Map<String, String> localParams;
    private Map<String, String> localVars;
    private String currentType;
    public OllirVisitor(JmmSemanticsResult semantics){
        this.symbolTable = semantics.getSymbolTable();

        ifCounter = whileCounter = tmpVarCounter = 0;
        currentType = "";
        classVars = new HashMap<>();
        localParams = new HashMap<>();
        localVars = new HashMap<>();
    }

    @Override
    protected void buildVisitor() {
        addVisit("Program", this::parseProgram);
        addVisit("ImportList", this::parseImportList);
        addVisit("ClassDeclaration", this::parseClassDeclaration);
        addVisit("VarList", this::dummyParser);
        addVisit("VarDeclaration", this::parseVarDeclaration);
        addVisit("MethodList", this::dummyParser);
        addVisit("MethodDeclaration", this::parseMethodDeclaration);
        addVisit("Parameter", this::parseParameter);
        addVisit("Statement", this::parseStatement);
        addVisit("Expression", this::parseExpression);
    }

    private String parseClassDeclaration(JmmNode root, String s) {
        String ret = root.get("name");
        if(root.hasAttribute("superclass")) ret += " extends " + root.get("superclass");
        ret += " {\n";

        for(JmmNode child : root.getChildren()) {
            switch (child.getKind()) {
                case "VarList" -> {
                    for(JmmNode var : child.getChildren()) {
                        ret += "\t.field private " + visit(var, "") + ";\n";
                        classVars.put(var.get("name"), getTypeSuffix(var.getJmmChild(0)));
                    }
                }
                case "MethodList" -> {
                    ret += "\t.construct "+symbolTable.getClassName()+"().V {\n" +
                            "\t\tinvokespecial(this, \"<init>\").V;\n"+
                            "\t}\n";
                    for(JmmNode method : child.getChildren())
                        ret += visit(method, "\t");
                }
            }
            ret += "\n";
        }

        ret += "}\n";
        return ret;
    }
    private String dummyParser(JmmNode root, String s) { return "";}
    private String parseExpression(JmmNode root, String s) {
        String ret = "";

        switch(root.getKind()) {
            case "UnaryOp" -> {
                String t1 = "t" + tmpVarCounter; tmpVarCounter++;

                String t2 = "t" + tmpVarCounter;
                ret += visit(root.getJmmChild(0), s) + ";\n";

                ret += s + t1 + "." + currentType + ":=." + currentType + " "
                        + "!" + t2 + "." + currentType;
            }
            case "Parenthesis" -> {
                String t1 = "t" + tmpVarCounter; tmpVarCounter++;

                String t2 = "t" + tmpVarCounter;
                ret += visit(root.getJmmChild(0), s) + ";\n";

                ret += s + t1 + "." + currentType + ":=." + currentType + " "
                        + t2 + "." + currentType + "\n";
            }
            case "ArrayAccess" -> {
                String t1 = "t"+tmpVarCounter; tmpVarCounter++;

                String t2 = "t" + tmpVarCounter;
                ret += visit(root.getJmmChild(0), s) + ";\n";

                String t3 = "t" + tmpVarCounter;
                ret += visit(root.getJmmChild(1), s) + ";\n";

                ret += s + t1 + "." + currentType
                        + " :=." + currentType + " "
                        + "$1."+t2+"["+t3+".i32"+"]."+currentType;
            }
            case "Init" -> {
                String t1 = "t"+tmpVarCounter; tmpVarCounter++;

                ret += s + t1 +"."+currentType
                        + " :=."+currentType
                        + " new("+currentType+")."+currentType+";\n"
                        + "invokespecial("+t1+"."+currentType+",\"<init>\").V\n";
            }
            case "BinaryOp" -> {
                String t1 = "t"+tmpVarCounter; tmpVarCounter++;

                String t2 = "t"+tmpVarCounter;
                ret += visit(root.getJmmChild(0), s) + ";\n";

                String t3 = "t"+tmpVarCounter;
                ret += visit(root.getJmmChild(1), s) + ";\n";

                ret += s + t1 + "." + currentType + " :=." + currentType + " "
                        + t2 + "." + currentType + " "
                        + root.get("op").replaceAll("'", "") + "." + currentType + " "
                        + t3 + "." + currentType;
            }
            case "LogicalOp" -> {
                String t1 = "t"+tmpVarCounter; tmpVarCounter++;

                String t2 = "t"+tmpVarCounter;
                ret += visit(root.getJmmChild(0), s) + ";\n";

                String t3 = "t"+tmpVarCounter;
                ret += visit(root.getJmmChild(1), s) + ";\n";

                String opType = (root.get("op").equals("'>='")
                        || root.get("op").equals("'<='")
                        || root.get("op").equals("'>'")
                        || root.get("op").equals("'<'")
                        ? "bool" : currentType);

                ret += s + t1 + "." + currentType + ":=." + currentType + " "
                        + t2 + "." + currentType + " "
                        + root.get("op").replaceAll("'", "") + "." + opType + " "
                        + t3 + "." + currentType;
            }
            case "Length" -> {
                String t1 = "t" + tmpVarCounter; tmpVarCounter++;

                String t2 = "t"+tmpVarCounter;
                ret += visit(root.getJmmChild(0), s) + ";\n";

                ret += s + t1+"."+currentType + " :=."+currentType
                        + " arraylength($1."+t2+".array."+currentType+")."+currentType;
            }
            case "Function" -> {
                String t1 = "t" + tmpVarCounter; tmpVarCounter++;

                ArrayList<String> tEx = new ArrayList<>();
                for(JmmNode p : root.getChildren()) {
                    tEx.add("t" + tmpVarCounter);
                    ret += visit(p, s) + ";\n";
                }

                String invoke; //= (symbolTable.getMethods().contains(root.get("name")) ? "invokevirtual" : "invokestatic");
                String src, rType;
                if (symbolTable.getMethods().contains(root.get("name"))) {
                    invoke = "invokevirtual";
                    src = tEx.get(0) + "." + symbolTable.getClassName();
                    rType = currentType;
                } else {
                    invoke = "invokestatic";
                    src = root.getJmmChild(0).get("value");
                    rType = "V";
                }

                ret += s + t1+"."+currentType
                        + " :=."+currentType + " "
                        + invoke+"("+src+", \""+root.get("value")+"\"";
                tEx.remove(0);
                for(String p : tEx)
                    ret += ", "+p+"."+currentType;
                ret += ")."+rType;
            }
            case "Integer" -> {
                ret += s + "t" + tmpVarCounter + ".i32 :=.i32 " + root.get("value")+".i32";
                tmpVarCounter++;
            }
            case "BooleanLiteral" -> {
                ret += s + "t" + tmpVarCounter + ".bool :=.bool " + root.get("value")+".bool";
                tmpVarCounter++;
            }
            case "This" -> {
                // TODO
            }
            case "Identifier" -> {
                ret += s + "t" + tmpVarCounter + "." + currentType
                        + " :=." + currentType + " "
                        + root.get("value") + "." + currentType;
                tmpVarCounter++;
            }
        }
        return ret;
    }
    private String parseImportList(JmmNode root, String s) {
        String ret = "";

        for(JmmNode decl : root.getChildren()) {
            ret += s + "import ";

            List<String> packs = new ArrayList<>();
            JmmNode pckg = decl;
            do {
                pckg = pckg.getJmmChild(0);
                packs.add(pckg.get("id"));
            } while(pckg.getNumChildren() > 0);

            ret += String.join(".", packs) + ";\n";
        }
        return ret;
    }
    private String parseMethodDeclaration(JmmNode root, String s) {
        String ret = s;
        localVars = new HashMap<>();

        if(root.get("name").equals(symbolTable.getClassName()))
            ret += ".construct ";
        else
            ret += ".method ";

        if(root.hasAttribute("public"))
            ret += "public";
        else
            ret += "private ";

        if(root.hasAttribute("static"))
            ret += "static ";

        ret += root.get("name");

        String params = "";
        String vars = "";
        String stats = "";
        String retstat= "";
        for(JmmNode child : root.getChildren()) {
            switch(child.getKind()) {
                case "ParameterList" -> {
                    List<String> paramList = new ArrayList<>();
                    for(JmmNode param : child.getChildren())
                        paramList.add(visit(param, ""));
                    params = String.join(",", paramList);
                }
                case "VarList" -> {
                    for(JmmNode var : child.getChildren()) {
                        localVars.put(var.get("name"), getTypeSuffix(var.getJmmChild(0)));
                    }
                }
                case "StatementList" -> {
                    for(JmmNode stat : child.getChildren()) {
                        stats += visit(stat, s+"\t");
                    }
                }
                case "MethodReturn" -> {
                    currentType = getTypeSuffix(root.getJmmChild(0));
                    String rval = "t" + tmpVarCounter;
                    retstat = s + visit(child.getJmmChild(0), s) + ";\n";

                    retstat += s + "\tret." + currentType + " "
                            + rval + "." + currentType + ";\n";
                }
            }
        }

        return ret += "(" + params + ")." + getTypeSuffix(root.getJmmChild(0)) + " {\n"
                + vars
                + stats
                + retstat
                + s + "}\n";
    }
    private String parseProgram(JmmNode root, String s) {
        String ret = "";
        for (JmmNode child : root.getChildren()) {
            ret += visit(child, s) + "\n";
        }
        return ret;
    }
    private String parseStatement(JmmNode root, String s) {
        String ret = "";

        switch(root.getKind()) {
            case "BracketsStatement" -> {
                ret += s + "{\n" + visit(root.getJmmChild(0), s+"\t") + s + "}\n";
            }
            case "IfStatement" -> {
                String t1 = "t" + tmpVarCounter;
                ret += visit(root.getJmmChild(0),s) + ";\n";
                ret += s + "if("+ t1 + ") goto Else" + ifCounter + ";\n"
                        + visit(root.getJmmChild(1), s)
                        + s + "Else" + ifCounter + ":\n"
                        + visit(root.getJmmChild(2), s);

                ifCounter++;
            }
            case "WhileStatement" -> {
                String t1 = "t" + tmpVarCounter;
                ret += visit(root.getJmmChild(0),s) + ";\n";

                ret += s + "While" + whileCounter + ":\n"
                        + s+"\t"+ "if("+ t1 +") goto EndWhile" + whileCounter + ";\n"
                        + visit(root.getJmmChild(1), s+"\t")
                        + s + "EndWhile" + whileCounter + ":\n";

                whileCounter++;
            }
            case "ExpressionStatement" -> {
                ret += visit(root.getJmmChild(0),s) + ";\n";
            }
            case "AssignStatement" -> {
                String expVar = "t"+tmpVarCounter;

                if(localVars.containsKey(root.get("name"))) {
                    currentType = localVars.get(root.get("name"));
                    ret += visit(root.getJmmChild(0), s) + ";\n";
                    ret += s + root.get("name")+"."+currentType
                            +" :=."+currentType+" "
                            +expVar + "." + currentType +";\n";
                }
            }
            case "AccessAndAssignStatement" -> {
                if(localVars.containsKey(root.get("name"))) {
                    String expVar = "t"+tmpVarCounter;
                    currentType = localVars.get(root.get("name"));
                    ret += visit(root.getJmmChild(1), s) + ";\n";

                    String idxVar = "t"+tmpVarCounter;
                    ret += visit(root.getJmmChild(0), s) + ";\n";

                    ret += s + root.get("name")+"["+idxVar+".i32]."+currentType
                            +" :=."+currentType+" "
                            +expVar + "." + currentType +";\n";
                }
            }
        }
        return ret;
    }
    private String parseVarDeclaration(JmmNode root, String s) {
        return s + root.get("name") +"."+ getTypeSuffix(root.getJmmChild(0));
    }
    private String parseParameter(JmmNode root, String s) {
        return s + root.get("name") +"."+ getTypeSuffix(root.getJmmChild(0));
    }
    private String getTypeSuffix(JmmNode type) {
        switch(type.getKind()) {
            case "TypeIntegerArray" -> {
                return "array.i32";
            }
            case "TypeBoolean" -> {
                return "bool";
            }
            case "TypeInteger" -> {
                return "i32";
            }
            case "TypeArray" -> {
                return "array." + type.get("id");
            }
            case "TypeID" -> {
                if(type.get("id").equals("void"))
                    return "V";
                return type.get("id");
            }
            default -> {
                return "";
            }
        }
    }
}
