package cz.jsfraz.lojza.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import cz.jsfraz.lojza.database.Database;
import cz.jsfraz.lojza.database.IDatabase;
import cz.jsfraz.lojza.database.models.Locale;
import cz.jsfraz.lojza.utils.ILocalizationManager;
import cz.jsfraz.lojza.utils.LocalizationManager;
import cz.jsfraz.lojza.utils.SettingSingleton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class GuildEventListener extends ListenerAdapter {
    private ILocalizationManager lm;
    private IDatabase db;
    private SettingSingleton settings;

    public GuildEventListener() {
        this.lm = new LocalizationManager();
        this.db = new Database();
        this.settings = SettingSingleton.GetInstance();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        // send message to first channel
        for (TextChannel channel : event.getGuild().getTextChannels()) {
            if (channel.canTalk()) {
                Locale locale = db.getGuildLocaleById(event.getGuild().getIdLong());

                // embed
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(Color.decode("#2b2d31"));
                eb.addField(lm.getText(locale, "textJoinGreet"), lm.getText(locale, "textJoin"), false);

                // buttons
                List<ItemComponent> components = new ArrayList<ItemComponent>() {
                    {
                        add(Button.primary("help", lm.getText(locale, "textHelpTitle")));
                        add(Button.primary("setup", lm.getText(locale, "textSetupTitle")));
                        add(Button.link(settings.getProperties().getProperty("url"), lm.getText(locale, "GitHub")));
                    }
                };

                channel.sendMessageEmbeds(eb.build()).addActionRow(components).queue();
                break;
            }
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        // disable rss
        db.updateRssById(event.getGuild().getIdLong(), false);
    }

}
