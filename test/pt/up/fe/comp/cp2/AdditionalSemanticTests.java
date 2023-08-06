package pt.up.fe.comp.cp2;

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AdditionalSemanticTests {

    @Test
    public void arrayIndexBad() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/arrayIndexBad.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void arrayIndexBad2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/arrayIndexBad2.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void arrayIndexBad3() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/arrayIndexBad3.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void assignmentCorrect() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/assignmentCorrect.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void fieldInStatic() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/fieldInStatic.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void methodCallSimple() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/methodCallSimple.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void methodSignatureSimple() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/methodSignatureSimple.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void methodSignatureSimple2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/methodSignatureSimple2.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void methodSignatureWrongLiteralType() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/methodSignatureWrongLiteralType.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void methodSignatureWrongLiteralType2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/methodSignatureWrongLiteralType2.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void thisAsCommonExpression() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/thisAsCommonExpression.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void thisInMain() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/thisInMain.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void thisInMain2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/thisInMain2.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void whileIfArrayAccessArrayInteger() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/whileIfArrayAccessArrayInteger.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void whileIfArrayAccessArrayInteger2() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/whileIfArrayAccessArrayInteger2.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void test() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/test.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void arrayAccessOnInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/arrayAccessOnInt.jmm"));
        TestUtils.mustFail(result);
    }

    @Test
    public void arrayIndexGood() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/arrayIndexGood.jmm"));
        TestUtils.noErrors(result);
    }
    @Test
    public void assignmentArrayBad() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/assignmentArrayBad.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void variableUndefinedInt() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/variableUndefinedInt.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void whileIfArrayIfCondNotBool() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/whileIfArrayIfCondNotBool.jmm"));
        TestUtils.mustFail(result);
    }
    @Test
    public void whileIfArrayAccessArrayNotInteger() {
        var result = TestUtils.analyse(SpecsIo.getResource("pt/up/fe/comp/cp2/semanticanalysis/additionalTests/whileIfArrayAccessArrayNotInteger.jmm"));
        TestUtils.mustFail(result);
    }



}