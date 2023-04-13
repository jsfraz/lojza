package cz.jsfraz.lojza;

import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
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
                    builder.connectTimeout(1000, TimeUnit.MILLISECONDS);
                    builder.readTimeout(1000, TimeUnit.MILLISECONDS);
                })
                .applyToClusterSettings(builder -> builder.serverSelectionTimeout(1000, TimeUnit.MILLISECONDS))
                .applyConnectionString(new ConnectionString(
                        "mongodb://" + settingSingleton.getMongoUser() + ":" + settingSingleton.getMongoPassword() + "@"
                                + settingSingleton.getMongoServer() + ":" + settingSingleton.getMongoPort()))
                .codecRegistry(codecRegistry)
                .build();
        this.database = MongoClients.create(mongoSettings).getDatabase(settingSingleton.getMongoDatabase());
    }

    // test database connection
    public void testConnection() throws Exception {
        Document document = new Document();
        document.put("ping", 1);
        database.runCommand(document);
    }

    // gets DiscordGuild collection
    private MongoCollection<DiscordGuild> getDiscordGuildCollection() {
        return database.getCollection(DiscordGuild.class.getSimpleName() + collectionSuffix, DiscordGuild.class);
    }

    // returns DiscordGuild or null
    private DiscordGuild getFirstOrDefault(long guildId) {
        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        return collection.find(Filters.eq("guildId", guildId)).first();
    }

    // updates DiscordGuild locale
    public void updateGuildLocale(long guildId, Locale locale) {
        // gets guild by id
        DiscordGuild guild = getFirstOrDefault(guildId);

        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        if (guild != null) {
            // update if exists
            collection.updateOne(Filters.eq("guildId", guildId), Updates.set("locale", locale));
        } else {
            // insert if doesn't exist
            guild = new DiscordGuild(guildId, Locale.en);
            collection.insertOne(guild);
        }
    }

    // get DiscordGuild locale
    public Locale getGuildLocale(long guildId) {
        // gets guild locale by id
        MongoCollection<DiscordGuild> collection = getDiscordGuildCollection();
        Bson filter = Filters.eq("guildId", guildId);
        Bson projection = Projections.fields(Projections.include("locale"));
        DiscordGuild guild = collection.find(filter).projection(projection).first();

        if (guild == null) {
            // if doesn't exist in database, return default
            return Locale.en;
        } else {
            // if exists return locale
            return guild.locale;
        }
    }
}
