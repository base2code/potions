package de.base2code.potions.listeners;

import de.base2code.potions.Potions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class PotionEffectListener implements Listener {
    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (Potions.getInstance().getBlacklistManager().containsUser(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
