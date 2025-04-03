package de.linusdev.ljgel.engine;

import de.linusdev.ljgel.engine.async.BasicAsyncManager;
import de.linusdev.ljgel.engine.info.Game;
import de.linusdev.ljgel.engine.ticker.Ticker;
import de.linusdev.ljgel.engine.ticker.TickerImpl;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.async.manager.HasAsyncManager;
import de.linusdev.lutils.interfaces.AdvTRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class AbstractEngine<GAME extends Game> implements Engine<GAME>, HasAsyncManager {

    protected final @NotNull GAME game;
    protected final @NotNull Executor executor;
    protected final @NotNull BasicAsyncManager asyncManager;
    protected final @NotNull TickerImpl ticker;

    protected AbstractEngine(@NotNull GAME game) {
        this.game = game;
        this.executor = Executors.newWorkStealingPool(16);
        this.asyncManager = new BasicAsyncManager();
        this.ticker = new TickerImpl(game.getMillisPerTick());

    }

    @Override
    public @NotNull <R>  Future<R, Nothing> runSupervised(@NotNull AdvTRunnable<R, ?> runnable) {
        var future = CompletableFuture.<R, Nothing>create(getAsyncManager(), false);
        executor.execute(() -> {
            try {
                future.complete(runnable.run(), Nothing.INSTANCE, null);
            } catch (Throwable t) {
                LOG.throwable(new Exception("Uncaught exception in runSupervised runnable: ", t));
                future.complete(null, Nothing.INSTANCE, new ThrowableAsyncError(t));
            }
        });
        return future;
    }

    /* ================================================================================================= *\
    |                                                                                                     |
    |                                                Getter                                               |
    |                                                                                                     |
    \* ================================================================================================= */

    @Override
    public @NotNull AsyncManager getAsyncManager() {
        return asyncManager;
    }

    @Override
    public @NotNull GAME getGame() {
        return game;
    }

    public @NotNull Ticker getTicker() {
        return ticker;
    }
}
