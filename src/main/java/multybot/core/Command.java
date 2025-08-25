package dev.lcapi.multybot.core;

import java.util.Locale;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface Command {
    SlashCommandData slashData(Locale locale);
    void execute(CommandContext ctx) throws Exception;
    default String name() { return getClass().getSimpleName().replace("Command","").toLowerCase(); }
}
