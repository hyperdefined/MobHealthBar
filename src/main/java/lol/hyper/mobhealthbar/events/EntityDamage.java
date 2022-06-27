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

package lol.hyper.mobhealthbar.events;

import lol.hyper.mobhealthbar.MobHealthBar;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;

public class EntityDamage implements Listener {

    private final MobHealthBar mobHealthBar;
    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;

    public EntityDamage(MobHealthBar mobHealthBar) {
        this.mobHealthBar = mobHealthBar;
        this.audiences = mobHealthBar.getAdventure();
        this.miniMessage = mobHealthBar.miniMessage;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // only look at living mobs
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        // player is hitting something
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (mobHealthBar.playerBossBarTasks.get(player) != null) {
                // cancel the previous task
                mobHealthBar.playerBossBarTasks.get(player).cancel();
            }
            String mobName = livingEntity.getName();
            AttributeInstance maxHealthInstance = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthInstance == null) {
                return;
            }
            double maxHealth = maxHealthInstance.getValue();
            double totalDamage = event.getFinalDamage();
            double health = livingEntity.getHealth() - totalDamage;
            // if the mob is dead, set health to 0
            if (health <= 0) {
                health = 0;
            }
            BossBar playerBar = mobHealthBar.playerBossBars.get(player);
            // set the progress
            playerBar.progress((float) (health / maxHealth));
            playerBar.name(format(mobName));
            audiences.player(player).showBossBar(playerBar);
            BukkitTask task = Bukkit.getScheduler().runTaskLater(mobHealthBar, () -> {
                audiences.player(player).hideBossBar(playerBar);
                mobHealthBar.playerBossBarTasks.put(player, null);
            }, 40);
            mobHealthBar.playerBossBarTasks.put(player, task);
        }
    }

    private Component format(String mobName) {
        String configTitle = mobHealthBar.config.getString("title");
        if (configTitle == null) {
            mobHealthBar.logger.warning("title is not set in the config! Using default.");
            return Component.text(mobName);
        }
        if (configTitle.contains("%mob%")) {
            configTitle = configTitle.replace("%mob%", mobName);
        }
        return miniMessage.deserialize(configTitle);
    }
}
