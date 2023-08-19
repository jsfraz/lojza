package cz.jsfraz.lojza.listeners;

import java.util.List;

import cz.jsfraz.lojza.database.Database;
import cz.jsfraz.lojza.database.IDatabase;
import cz.jsfraz.lojza.utils.SettingSingleton;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionResumeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SessionEventListener extends ListenerAdapter {
    private IDatabase db;
    private SettingSingleton settings;

    public SessionEventListener() {
        this.db = new Database();
        this.settings = SettingSingleton.GetInstance();
    }

    @Override
    public void onReady(ReadyEvent event) {
        updateGuildRss();
    }

    @Override
    public void onSessionResume(SessionResumeEvent event) {
        updateGuildRss();
    }

    // sets guild's rss to false
    private void updateGuildRss() {
        // check and set rss to false for invalid guilds
        List<Long> guildIds = db.getAllGuildIds();
        for (long guildId : guildIds) {
            if (settings.getJdaInstance().getGuildById(guildId) != null) {
                db.updateRssById(guildId, false);
            }
        }
    }
}
