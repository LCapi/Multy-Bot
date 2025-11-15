package multybot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Contexto por interacción slash. Encapsula el acceso a JDA y
 * ofrece helpers seguros para responder y leer opciones.
 */
public class CommandContext {

    private final SlashCommandInteractionEvent event;
    private final Locale locale;

    private CommandContext(SlashCommandInteractionEvent event, Locale locale) {
        this.event = event;
        this.locale = locale;
    }

    /** Fábrica principal */
    public static CommandContext from(SlashCommandInteractionEvent event) {
        return new CommandContext(event, resolveLocale(event));
    }

    /** Alias de compatibilidad */
    public static CommandContext fromEvent(SlashCommandInteractionEvent event) {
        return from(event);
    }

    /** Resolver locale en orden: usuario → guild → default */
    private static Locale resolveLocale(SlashCommandInteractionEvent e) {
        DiscordLocale user = e.getUserLocale();
        if (user != null) return Locale.forLanguageTag(user.getLocale());

        Guild g = e.getGuild();
        if (g != null && g.getLocale() != null) {
            return Locale.forLanguageTag(g.getLocale().getLocale());
        }
        return Locale.getDefault();
    }

    /* ----------------- Getters de contexto ----------------- */

    public SlashCommandInteractionEvent event() { return event; }
    public Locale locale() { return locale; }
    public JDA jda() { return event.getJDA(); }
    public Guild guild() { return event.getGuild(); }
    public Member member() { return event.getMember(); }
    public User user() { return event.getUser(); }
    public boolean isGuild() { return guild() != null; }
    public String username() { return user() != null ? user().getName() : "unknown"; }

    /* ----------------- Helpers de respuesta ----------------- */

    /** Respuesta rápida (pública si no hubo defer) */
    public void reply(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content).queue();
        } else {
            event.reply(content).queue();
        }
    }

    /** Respuesta efímera (privada) */
    public void replyEphemeral(String content) {
        if (event.isAcknowledged()) {
            hook().sendMessage(content).setEphemeral(true).queue();
        } else {
            event.reply(content).setEphemeral(true).queue();
        }
    }

    /** Edita el mensaje original tras defer/reply */
    public void editOriginal(String content) {
        hook().editOriginal(content).queue();
    }

    /** Mensaje de error consistente */
    public void error(String msg) {
        if (event.isAcknowledged()) {
            hook().editOriginal("X " + msg).queue();
        } else {
            event.reply("X " + msg).setEphemeral(true).queue();
        }
    }

    /** Asegura reconocimiento y devuelve hook */
    public InteractionHook hook() {
        ensureAcknowledged();
        return event.getHook();
    }

    /** Mensaje adicional tras defer/reply */
    public void followup(String content) {
        hook().sendMessage(content).queue();
    }

    /** Reconoce la interacción si aún no lo está (defer público) */
    public void ensureAcknowledged() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }

    /** Defer explícito (público) */
    public void defer() {
        if (!event.isAcknowledged()) {
            event.deferReply().queue();
        }
    }

    /** Defer explícito efímero (privado) */
    public void deferEphemeral() {
        if (!event.isAcknowledged()) {
            event.deferReply(true).queue();
        }
    }

    /* ----------------- Lectura de opciones ----------------- */

    public Optional<OptionMapping> option(String name) {
        return Optional.ofNullable(event.getOption(name));
    }

    public String optionStr(String name, String def) {
        var opt = event.getOption(name);
        return opt != null ? opt.getAsString() : def;
    }

    public Integer optionInt(String name, Integer def) {
        var opt = event.getOption(name);
        return opt != null ? (int) opt.getAsLong() : def; // getAsInt no existe; JDA usa long
    }

    public Boolean optionBool(String name, Boolean def) {
        var opt = event.getOption(name);
        return opt != null ? opt.getAsBoolean() : def;
    }

    public User optionUser(String name) {
        var opt = event.getOption(name);
        return opt != null ? opt.getAsUser() : null;
    }

    public Role optionRole(String name) {
        var opt = event.getOption(name);
        return opt != null ? opt.getAsRole() : null;
    }

    public GuildChannel optionChannel(String name) {
        var opt = event.getOption(name);
        return opt != null ? opt.getAsChannel() : null;
    }

    /** Si esperas canal de texto, castea con seguridad (JDA 5 union type) */
    public GuildMessageChannelUnion optionTextChannel(String name) {
        var ch = optionChannel(name);
        return (ch instanceof GuildMessageChannelUnion union) ? union : null;
    }

    /* ----------------- Guardarraíles comunes ----------------- */

    /** Exige guild; si es DM lanza error amigable y devuelve false */
    public boolean requireGuild() {
        if (guild() == null) {
            error("Este comando solo puede usarse en un servidor (no en DM).");
            return false;
        }
        return true;
    }

    /* ----------------- Cargas largas / async ----------------- */

    /**
     * Ejecuta trabajo pesado sin timeouts:
     * - Hace defer efímero si hacía falta.
     * - Ejecuta en otro hilo y edita el original con el resultado.
     */
    public <T> void runAsync(Supplier<T> supplier, java.util.function.Function<T, String> render) {
        if (!event.isAcknowledged()) {
            // efímero por defecto para tareas largas
            event.deferReply(true).queue();
        }
        CompletableFuture
                .supplyAsync(supplier)
                .thenApply(render)
                .thenAccept(this::editOriginal)
                .exceptionally(ex -> {
                    error("Se produjo un error procesando la petición.");
                    return null;
                });
    }
}