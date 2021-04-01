package info.teksol.mindcode.mindustry;

import info.teksol.mindcode.ast.Seq;
import org.junit.jupiter.api.Test;

import java.util.List;

class OptimizeSetThenWriteTest extends AbstractGeneratorTest {
    private final AccumulatingLogicInstructionPipeline terminus = new AccumulatingLogicInstructionPipeline();
    private final LogicInstructionPipeline pipeline = new OptimizeSetThenWrite(terminus);

    @Test
    void optimizesSetThenWriteValueOut() {
        LogicInstructionGenerator.generateInto(pipeline, (Seq) translateToAst("cell1[42] = 36"));

        assertLogicInstructionsMatch(
                List.of(
                        new LogicInstruction("write", "36", "cell1", "42"),
                        new LogicInstruction("end")
                ),
                terminus.getResult()
        );
    }

    @Test
    void leavesUnrelatedSetAlone() {
        LogicInstructionGenerator.generateInto(pipeline, (Seq) translateToAst("n = 41\ncell1[42] = 36"));
        pipeline.flush();

        assertLogicInstructionsMatch(
                List.of(
                        new LogicInstruction("set", var(0), "41"),
                        new LogicInstruction("set", "n", var(0)),
                        new LogicInstruction("write", "36", "cell1", "42"),
                        new LogicInstruction("end")
                ),
                terminus.getResult()
        );
    }
}