import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class AntiAdminDiscordBot {
    public static void main(String[] args) throws Exception {
        JDA api = JDABuilder.createDefault("INSERT BOT KEY HERE")
                .addEventListeners(new BotListener())
                .build()
                .awaitReady();
    }
}
