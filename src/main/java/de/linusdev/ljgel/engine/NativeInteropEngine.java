package de.linusdev.ljgel.engine;

import de.linusdev.ljgel.nat.Load;
import de.linusdev.ljgel.nat.NativeUtils;
import de.linusdev.ljgel.nat.abi.ABISelector;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.interfaces.TConsumer;
import de.linusdev.lutils.interfaces.TFunction;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;

public interface NativeInteropEngine {

    class StaticSetup {

        static {
            setup();
        }

        private static boolean staticSetupDone = false;

        public static synchronized void checkSetup() {
            if(!staticSetupDone)
                throw new IllegalStateException("NativeInteropEngine.StaticSetup.setup() must be called as first line in main.");
        }

        public static synchronized void setup() {
            if(staticSetupDone)
                return;
            ABISelector.retrieveAndSetDefaultABI();
            Load.init();
            BufferUtils.setByteBufferFromPointerMethod(NativeUtils::getBufferFromPointer);
            staticSetupDone = true;
        }
    }

    @NotNull <R> Future<R, Nothing> runSupervised(@NotNull TFunction<Stack, R, ?> runnable);

    default @NotNull Future<Nothing,Nothing> runSupervisedV(@NotNull TConsumer<Stack, ?> runnable) {
        return runSupervised((stack) -> {
            runnable.consume(stack);
            return Nothing.INSTANCE;
        });
    }

}
