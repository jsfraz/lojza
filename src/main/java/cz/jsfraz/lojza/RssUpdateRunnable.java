package cz.jsfraz.lojza;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import net.dv8tion.jda.api.EmbedBuilder;
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
        List<DiscordGuild> guilds = db.getGuildsRss();

        // https://stackoverflow.com/questions/2016083/what-is-the-easiest-way-to-parallelize-a-task-in-java
        ExecutorService execGuild = Executors.newCachedThreadPool();
        List<Callable<Boolean>> guildTasks = new ArrayList<Callable<Boolean>>();
        for (DiscordGuild guild : guilds) {
            // check if channel exists
            if (settings.getJdaInstance().getTextChannelById(guild.getRssChannelId()) != null) {
                Callable<Boolean> guildCallable = new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {

                        ExecutorService execRss = Executors.newCachedThreadPool();
                        List<Callable<Boolean>> rssTasks = new ArrayList<Callable<Boolean>>();
                        for (RssFeed feed : guild.getRssFeeds()) {
                            Callable<Boolean> rssCallable = new Callable<Boolean>() {

                                @Override
                                public Boolean call() throws Exception {
                                    try {

                                        // set refresh date an hour ago
                                        Date now = new Date();
                                        Date lastRefresh = new Date(
                                                System.currentTimeMillis()
                                                        - settings.getRssRefreshMinutes() * 60 * 1000);
                                        // don't refresh if last refresh was less than hour ago (restarts and similiar
                                        // situations)
                                        if (feed.getUpdated().after(lastRefresh)) {
                                            return true;
                                        }

                                        // get feed released from hour ago to now, sort from oldest
                                        SyndFeed f = Tools.getRssFeed(feed.getUrl());
                                        List<SyndEntry> entries = f.getEntries().stream()
                                                .filter(x -> x.getPublishedDate().after(lastRefresh))
                                                .sorted(new Comparator<SyndEntry>() {
                                                    public int compare(SyndEntry o1, SyndEntry o2) {
                                                        return o1.getPublishedDate().compareTo(o2.getPublishedDate());
                                                    }
                                                }).toList();

                                        // update updated date
                                        db.updateRssUpdatedDate(guild.getGuildId(), feed.getUrl(), now);

                                        boolean success = false;

                                        for (SyndEntry entry : entries) {
                                            try {
                                                // create embed
                                                EmbedBuilder eb = new EmbedBuilder();
                                                eb.setTitle(entry.getTitle(), entry.getLink());
                                                eb.setColor(Color.yellow);
                                                eb.addField(f.getTitle(), entry.getDescription().getValue(), false);
                                                // set feed logo as thumbnail
                                                if (f.getImage() != null) {
                                                    eb.setThumbnail(f.getImage().getUrl());
                                                }
                                                // set feed entry image as embed image
                                                if (!entry.getEnclosures().isEmpty()) {
                                                    eb.setImage(entry.getEnclosures().get(0).getUrl());
                                                }
                                                eb.setFooter(entry.getPublishedDate().toString());

                                                // send annoucement
                                                settings.getJdaInstance().getTextChannelById(guild.getRssChannelId())
                                                        .sendMessageEmbeds(eb.build()).queue();

                                                success = true;
                                            } catch (Exception e) {
                                                // error
                                                e.printStackTrace();
                                                settings.getJdaInstance().getTextChannelById(guild.getRssChannelId())
                                                        .sendMessage(lm.getText(guild.getLocale(), "textRssError"))
                                                        .queue();
                                            }
                                        }

                                        return success;
                                    } catch (Exception e) {
                                        // error
                                        e.printStackTrace();
                                        settings.getJdaInstance().getTextChannelById(guild.getRssChannelId())
                                                .sendMessage(lm.getText(guild.getLocale(), "textRssError")).queue();
                                        return false;
                                    }
                                }
                            };
                            rssTasks.add(0, rssCallable);
                        }
                        execRss.invokeAll(rssTasks);

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
