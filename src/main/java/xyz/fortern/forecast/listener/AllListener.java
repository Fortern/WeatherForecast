package xyz.fortern.forecast.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import xyz.fortern.forecast.Main;

public class AllListener implements Listener {

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        Bukkit.getScheduler().runTaskLater(
                Main.plugin,
                () -> world.getPlayers().forEach(player -> Main.plugin.notify(world, player)),
                2
        );
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        World world = event.getWorld();
        if (!world.hasStorm()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(
                Main.plugin,
                () -> world.getPlayers().forEach(player -> Main.plugin.notify(world, player)),
                2
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (Boolean.TRUE.equals(world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE)) && world.hasSkyLight()) {
            Main.plugin.notify(world, player);
        }
    }
}
