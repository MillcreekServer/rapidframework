package io.github.wysohn.rapidframework2.bukkit.testutils.manager;

import io.github.wysohn.rapidframework2.bukkit.testutils.AbstractBukkitTest;
import io.github.wysohn.rapidframework2.core.database.Database;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class AbstractBukkitManagerTest extends AbstractBukkitTest {
    protected static final String MESSAGE_JOIN = "join message";

    private static Database database;
    protected Database database(){
        if(database != null)
            return database;

        database = mock(Database.class);
        return database;
    }

    private static Database.DatabaseFactory dbFactory;
    protected Database.DatabaseFactory databaseFactory(Database database){
        if(dbFactory != null)
            return dbFactory;

        dbFactory = mock(Database.DatabaseFactory.class);
        when(dbFactory.getDatabase(Mockito.anyString())).thenReturn(database);

        return dbFactory;
    }

    protected final <T extends PluginMain.Manager> Consumer<T> initDbFactory(Object returnOnLoad) {
        return manager -> {
            try {
                Database.DatabaseFactory factory = databaseFactory(database());
                Mockito.when(database().load(Mockito.any(), Mockito.any())).thenReturn(returnOnLoad);
                PluginMain.Manager spy = PowerMockito.spy(manager);
                PowerMockito.doReturn(factory).when(spy, "createDatabaseFactory");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    protected AsyncPlayerPreLoginEvent login(){
        return spy(new AsyncPlayerPreLoginEvent(PLAYER_NAME, INET_ADDR, PLAYER_UUID));
    }

    protected PlayerJoinEvent join(Player player){
        return spy(new PlayerJoinEvent(player, MESSAGE_JOIN));
    }


}