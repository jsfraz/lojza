package cz.jsfraz.lojza.bot.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cz.jsfraz.lojza.database.Database;
import cz.jsfraz.lojza.database.IDatabase;
import cz.jsfraz.lojza.database.models.DiscordGuild;
import cz.jsfraz.lojza.utils.ILocalizationManager;
import cz.jsfraz.lojza.utils.LocalizationManager;
import cz.jsfraz.lojza.utils.SettingSingleton;
import cz.jsfraz.lojza.utils.Utils;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class RssUpdateRunnable implements Runnable {
    private ILocalizationManager lm;
    private IDatabase db;
    private SettingSingleton settings;

    public RssUpdateRunnable() {
        this.lm = new LocalizationManager();
        this.db = new Database();
        this.settings = SettingSingleton.GetInstance();
    }

    @Override
    public void run() {
        List<DiscordGuild> guilds = db.getGuildsRssBy();

        // https://stackoverflow.com/questions/2016083/what-is-the-easiest-way-to-parallelize-a-task-in-java
        ExecutorService execGuild = Executors.newCachedThreadPool();
        List<Callable<Boolean>> guildTasks = new ArrayList<Callable<Boolean>>();
        for (DiscordGuild guild : guilds) {
            // check if channel exists
            if (settings.getJdaInstance().getTextChannelById(guild.getRssChannelId()) != null) {
                Callable<Boolean> guildCallable = new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {
                        Utils.sendGuildRssAnnoucement(lm, db, settings, guild);

                        return true;
                    }
                };
                guildTasks.add(guildCallable);
            } else {
                // send message to first channel
                for (TextChannel channel : settings.getJdaInstance().getTextChannels()) {
                    if (channel.canTalk()) {
                        channel.sendMessage(lm.getText(guild.getLocale(), "textRssChannelNotSet")).queue();
                        break;
                    }
                }
            }
        }
        try {
            execGuild.invokeAll(guildTasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
