/*
 * This file is part of MobHealthBar.
 *
 * MobHealthBar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MobHealthBar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MobHealthBar.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.mobhealthbar;

import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.mobhealthbar.events.EntityDamage;
import lol.hyper.mobhealthbar.events.PlayerJoin;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public final class MobHealthBar extends JavaPlugin {

    public final Logger logger = this.getLogger();
    private final File configFile = new File(this.getDataFolder(), "config.yml");
    public FileConfiguration config;

    public final MiniMessage miniMessage = MiniMessage.miniMessage();
    private BukkitAudiences adventure;

    public final HashMap<Player, BossBar> playerBossBars = new HashMap<>();
    public final HashMap<Player, BukkitTask> playerBossBarTasks = new HashMap<>();

    public EntityDamage entityDamage;
    public PlayerJoin playerJoin;

    @Override
    public void onEnable() {
        adventure = BukkitAudiences.create(this);

        loadConfig();
        entityDamage = new EntityDamage(this);
        playerJoin = new PlayerJoin(this);

        Bukkit.getServer().getPluginManager().registerEvents(entityDamage, this);
        Bukkit.getServer().getPluginManager().registerEvents(playerJoin, this);

        Bukkit.getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        new Metrics(this, 15603);
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            this.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        if (config.getInt("config-version") != 1) {
            logger.warning("Your config file is outdated! Please regenerate the config.");
        }
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("MobHealthBar", "hyperdefined");
        } catch (IOException e) {
            logger.warning("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(this.getDescription().getVersion());
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warning("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warning("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }

    public BukkitAudiences getAdventure() {
        if(this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }
}
