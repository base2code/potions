package de.base2code.potions.manager;

import de.base2code.potions.Potions;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BlacklistManager {
    private final File file = new File(Potions.getInstance().getDataFolder(), "blacklist.yml");
    private final YamlConfiguration configuration;

    public BlacklistManager() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        configuration = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Add a user to the blacklist
     * @param uuid The uuid of the user
     */
    public void addUser(UUID uuid) {
        configuration.set(uuid.toString(), true);
        asyncSave();
    }

    /**
     * Check if a user is on the blacklist
     * @param uuid The uuid of the user
     * @return true if the user is on the blacklist
     */
    public boolean containsUser(UUID uuid) {
        return configuration.contains(uuid.toString()) && configuration.getBoolean(uuid.toString());
    }

    /**
     * Remove a user from the blacklist
     * @param uuid The uuid of the user
     */
    public void removeUser(UUID uuid) {
        configuration.set(uuid.toString(), false);
        asyncSave();
    }

    /**
     * Get all users on the blacklist
     * @return A list of all users on the blacklist
     */
    public List<UUID> getBlockedUsers() {
        ArrayList<UUID> users = new ArrayList<>();
        for (String key : configuration.getKeys(false)) {
            if (configuration.getBoolean(key)) {
                users.add(UUID.fromString(key));
            }
        }
        return users;
    }

    /**
     * Save the blacklist async
     */
    public void asyncSave() {
        CompletableFuture.runAsync(this::save);
    }

    /**
     * Save the blacklist (blocking)
     */
    public void save() {
        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
