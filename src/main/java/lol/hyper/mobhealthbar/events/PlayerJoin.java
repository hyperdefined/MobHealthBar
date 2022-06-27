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
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoin implements Listener {

    private final MobHealthBar mobHealthBar;

    public PlayerJoin(MobHealthBar mobHealthBar) {
        this.mobHealthBar = mobHealthBar;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // create the bossbar for the player to use
        BossBar newBossBar = BossBar.bossBar(Component.text("Mob Health"), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
        mobHealthBar.playerBossBars.put(player, newBossBar);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        mobHealthBar.playerBossBars.remove(player);
    }
}
