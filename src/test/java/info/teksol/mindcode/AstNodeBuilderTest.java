package info.teksol.mindcode;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AstNodeBuilderTest extends AbstractAstTest {
    @Test
    void parsesSimpleAssignment() {
        assertEquals(
                new Seq(
                        new VarAssignment(
                                "a",
                                new NumericLiteral("1")
                        )
                ),
                translateToAst("a = 1")

        );
    }

    @Test
    void parsesUsefulWhileLoop() {
        assertEquals(
                new Seq(
                        new VarAssignment("n", new NumericLiteral("5")),
                        new WhileStatement(
                                new BinaryOp(new VarRef("n"), ">", new NumericLiteral("0")),
                                new VarAssignment(
                                        "n",
                                        new BinaryOp(
                                                new VarRef("n"),
                                                "-",
                                                new NumericLiteral("1")
                                        )
                                ))
                ),
                translateToAst("n = 5\nwhile n > 0 {\nn -= 1\n}\n")
        );
    }

    @Test
    void parsesUnaryOperation() {
        assertEquals(
                new Seq(
                        new UnaryOp("not", new VarRef("ready"))
                ),
                translateToAst("not ready")
        );
    }

    @Test
    void parsesParenthesis() {
        assertEquals(
                new Seq(
                        new BinaryOp(
                                new BinaryOp(
                                        new NumericLiteral("1"),
                                        "+",
                                        new NumericLiteral("2")
                                ),
                                "*",
                                new NumericLiteral("3")
                        )
                ),
                translateToAst("(1 + 2) * 3")
        );
    }

    @Test
    void respectsArithmeticOrderOfOperations() {
        assertEquals(
                new Seq(
                        new BinaryOp(
                                new NumericLiteral("1"),
                                "+",
                                new BinaryOp(
                                        new NumericLiteral("2"),
                                        "*",
                                        new NumericLiteral("3")
                                )
                        )
                ),
                translateToAst("1 + 2 * 3")
        );
    }

    @Test
    void parsesComplexConditionalExpression() {
        assertEquals(
                new Seq(
                        new UnaryOp("not",
                                new BinaryOp(
                                        new BinaryOp(
                                                new VarRef("a"),
                                                "<",
                                                new VarRef("b")
                                        ),
                                        "and",
                                        new BinaryOp(
                                                new VarRef("c"),
                                                ">",
                                                new BinaryOp(
                                                        new BinaryOp(
                                                                new NumericLiteral("4"),
                                                                "*",
                                                                new VarRef("r")
                                                        ),
                                                        ">",
                                                        new NumericLiteral("5")
                                                )
                                        )
                                )
                        )
                ),
                translateToAst("not (a < b and c > (4 * r > 5))")
        );
    }

    @Test
    void parsesFunctionCalls() {
        assertEquals(
                new Seq(
                        new Seq(
                                new Seq(
                                        new FunctionCall(
                                                "print",
                                                List.of(
                                                        new StringLiteral("\"a\": "),
                                                        new VarRef("a")
                                                )
                                        ),
                                        new FunctionCall("random", List.of())
                                ),
                                new FunctionCall("print", List.of(new VarRef("r")))),
                        new FunctionCall(
                                "print",
                                List.of(
                                        new StringLiteral("\\nb: "),
                                        new BinaryOp(
                                                new VarRef("b"),
                                                "/",
                                                new NumericLiteral("3.1415")
                                        )
                                )
                        )


                ),
                translateToAst("print(\"\\\"a\\\": \", a)\nrandom()\nprint(r)\nprint(\"\\nb: \", b / 3.1415)")
        );
    }

    @Test
    void parsesSensorReading() {
        assertEquals(
                new Seq(
                        new BinaryOp(
                                new SensorReading("foundation1", "@copper"),
                                "<",
                                new SensorReading("foundation1", "itemCapacity")
                        ),
                        new BinaryOp(
                                new SensorReading("reactor1", "@cryofluid"),
                                "<",
                                new NumericLiteral("10")
                        )
                ),
                translateToAst("foundation1.copper < foundation1.itemCapacity\nreactor1.cryofluid < 10")
        );
    }

    @Test
    void parsesControl() {
        assertEquals(
                new Seq(
                        new Control(
                                "conveyor1",
                                "enabled",
                                new BinaryOp(
                                        new SensorReading("CORE","@copper"),
                                        "<",
                                        new SensorReading("CORE","itemCapacity")
                                )
                        )
                ),
                translateToAst("conveyor1.enabled = CORE.copper < CORE.itemCapacity")
        );
    }

    @Test
    void parsesHeapAccesses() {
        assertEquals(
                new Seq(
                        new HeapWrite(
                                "cell2", "1",
                                new HeapRead("cell3", "0")
                        )
                ),
                translateToAst("cell2[1] = cell3[0]")
        );
    }
}
