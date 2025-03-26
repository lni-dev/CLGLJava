package de.linusdev.ljgel.engine.info;

import org.jetbrains.annotations.NotNull;

public interface HasGame<G extends Game> {

    @NotNull G getGame();

}
