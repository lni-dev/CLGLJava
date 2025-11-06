package de.linusdev.ljgel.nat.abi;

import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat3;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat4;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.nat.abi.DefaultABIOverwrites;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import org.junit.jupiter.api.Test;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class ABITest {

    public static class ExampleStruct extends ComplexStructure {
        @StructValue(0)
        public final BBInt1 value = BBInt1.newUnallocated();

        @StructValue(1)
        public final BBFloat3 vector1 = BBFloat3.newUnallocated();

        @StructValue(2)
        public final BBFloat4 vector2 = BBFloat4.newUnallocated();



        public ExampleStruct(Class<?> ABI) {
            super(false);
            init(SVWrapper.overwriteLayout(ABI), true, value, vector1, vector2);
        }
    }


    @Test
    public void test() {
        ExampleStruct openglStruct = allocate(new ExampleStruct(StandardUniformBlockABI.class));
        ExampleStruct cStruct = allocate(new ExampleStruct(DefaultABIOverwrites.MSVC_X64));


        System.out.println("openglStruct:\n " + openglStruct.toString());
        System.out.println("\n\n");
        System.out.println("cStruct:\n " + cStruct.toString());

    }

}
