package cz.jsfraz.lojza;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;

// https://www.baeldung.com/java-mongodb

public class Database implements IDatabase {
    private MongoDatabase database;
    private final String collectionSuffix = "Collection";

    public Database() {
        SettingSingleton settingSingleton = SettingSingleton.GetInstance();

        // https://www.mongodb.com/developer/languages/java/java-mapping-pojos/
        CodecRegistry pojoCodecRegistry = CodecRegistries
                .fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry);

        // https://stackoverflow.com/questions/65189429/mongodb-java-driver-4-1-1-how-to-configure-timeout-settings
        MongoClientSettings mongoSettings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(settingSingleton.getMongoTimeoutMS(), TimeUnit.MILLISECONDS);
                    builder.readTimeout(settingSingleton.getMongoTimeoutMS(), TimeUnit.MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(settingSingleton.getMongoTimeoutMS(),
                        TimeUnit.MILLISECONDS))
                .applyConnectionString(new ConnectionString(
                        "mongodb://" + settingSingleton.getMongoUser() + ":" + settingSingleton.getMongoPassword() + "@"
                                + settingSingleton.getMongoServer() + ":" + settingSingleton.getMongoPort()))
                .codecRegistry(codecRegistry)
                .build();
        this.database = MongoClients.create(mongoSettings).getDatabase(settingSingleton.getMongoDatabase());
    }

    // test database connection
    @Override
    public boolean testConnection() {
        try {
            Document document = new Document();
            document.put("ping", 1);
            database.runCommand(document);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // gets DiscordGuild collection
    private MongoCollection<DiscordGuild> getDiscordGuildCollection() {
        return database.getCollection(Introspector.decapitalize(DiscordGuild.class.getSimpleName()) + collectionSuffix,
                DiscordGuild.class);
    }

    // returns DiscordGuild or null
    private DiscordGuild getFirstOrDefault(long guildId) {
        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        return collection.find(Filters.eq("guildId", guildId)).first();
    }

    // updates DiscordGuild locale
    @Override
    public void updateGuildLocale(long guildId, Locale locale) throws MongoException {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("locale", locale));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId, locale);
            collection.insertOne(guild);
        }
    }

    // get DiscordGuild locale
    @Override
    public Locale getGuildLocale(long guildId) {
        try {
            // gets guild locale by id
            MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
            Bson filter = Filters.eq("guildId", guildId);
            Bson projection = Projections.fields(Projections.include("locale"));
            DiscordGuild guild = collection.find(filter).projection(projection).first();

            if (guild == null) {
                // if doesn't exist in database, return default
                return SettingSingleton.GetInstance().getDefaultLocale();
            } else {
                // if exists return locale
                return guild.getLocale();
            }
        } catch (Exception e) {
            return SettingSingleton.GetInstance().getDefaultLocale();
        }
    }

    // updates DiscordGuild rss
    @Override
    public void updateRss(long guildId, boolean value) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("rss", value));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId, SettingSingleton.GetInstance().getDefaultLocale());
            guild.setRss(value);
            collection.insertOne(guild);
        }
    }

    // updates DiscordGuild rssChannel
    @Override
    public void updateRssChannel(long guildId, long rssChannel) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("rssChannelId", rssChannel));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId, SettingSingleton.GetInstance().getDefaultLocale());
            guild.setRssChannelId(rssChannel);
            collection.insertOne(guild);
        }
    }

    // get DiscordGuild rssChannel
    @Override
    public long getRssChannel(long guildId) {
        try {
            // gets guild locale by id
            MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
            Bson filter = Filters.eq("guildId", guildId);
            Bson projection = Projections.fields(Projections.include("rssChannelId"));
            DiscordGuild guild = collection.find(filter).projection(projection).first();

            if (guild == null) {
                // if doesn't exist in database, return default
                return 0;
            } else {
                // if exists return
                return guild.getRssChannelId();
            }
        } catch (Exception e) {
            return 0;
        }
    }

    // check whether RSS feed URL exists in DiscordGuild rssFeeds
    @Override
    public boolean rssFeedExists(long guildId, String url) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        if (guild != null) {
            Optional<RssFeed> feed = guild.getRssFeeds().stream().filter(x -> x.getUrl().equals(url)).findFirst();
            return feed.isPresent();
        } else {
            return false;
        }
    }

    // updates DiscordGuild rssFeeds
    @Override
    public void updateRssFeeds(long guildId, String title, String url) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.addToSet("rssFeeds", new RssFeed(title, url)));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId,
                    SettingSingleton.GetInstance().getDefaultLocale());
            guild.getRssFeeds().add(new RssFeed(title, url));
            collection.insertOne(guild);
        }
    }

    // get DiscordGuild rssFeeds
    @Override
    public List<RssFeed> getRssFeeds(long guildId) {
        try {
            // gets guild locale by id
            MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
            Bson filter = Filters.eq("guildId", guildId);
            Bson projection = Projections.fields(Projections.include("rssFeeds"));
            DiscordGuild guild = collection.find(filter).projection(projection).first();

            if (guild == null) {
                // if doesn't exist in database, return default
                return new ArrayList<RssFeed>();
            } else {
                // if exists return
                return guild.getRssFeeds();
            }
        } catch (Exception e) {
            return new ArrayList<RssFeed>();
        }
    }

    // removes item from DiscordGuild rssFeeds by index
    @Override
    public void removeRssFeed(long guildId, int index) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // index
            String url = getRssFeeds(guildId).get(index).getUrl();
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.pull("rssFeeds", Filters.eq("url", url)));
        }
    }

    // set DiscordGuild rssFeeds to an empty array
    @Override
    public void clearRssFeeds(long guildId) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("rssFeeds", new ArrayList<RssFeed>()));
        }
    }

    // returns all guilds with enabled rss and non-empty feed list
    @Override
    public List<DiscordGuild> getGuildsRss() {
        MongoCursor<DiscordGuild> cursor = getDiscordGuildCollection()
                .find(Filters.and(Filters.eq("rss", true),
                        Filters.not(Filters.eq("rssFeeds", new ArrayList<RssFeed>()))))
                .projection(Projections.fields(Projections.include("guildId", "locale" , "rssChannelId", "rssFeeds"))).iterator();

        List<DiscordGuild> list = new ArrayList<DiscordGuild>();
        while (cursor.hasNext()) {
            list.add(cursor.next());
        }
        cursor.close();
        return list;
    }

    // updates rssFeeds update date
    @Override
    public void updateRssUpdatedDate(long guildId, String url, Date updated) {
        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        Bson filters = Filters.and(Filters.eq("rss", true), Filters.elemMatch("rssFeeds", Filters.eq("url", url)));
        DiscordGuild guild = collection.find(filters).projection(Projections.fields(Projections.include("rssFeeds"))).first();
        List<RssFeed> feeds = guild.getRssFeeds();
        int index = feeds.indexOf(feeds.stream().filter(x -> x.getUrl().equals(url)).findFirst().get());
        RssFeed feed = feeds.get(index);
        feed.setUpdated(updated);
        feeds.set(index, feed);
        collection.updateOne(filters, Updates.set("rssFeeds", feeds));
    }
}
