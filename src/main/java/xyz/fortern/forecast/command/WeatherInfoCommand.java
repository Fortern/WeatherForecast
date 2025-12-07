package xyz.fortern.forecast.command;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.fortern.forecast.Main;

public class WeatherInfoCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You must be a player to use this command.");
            return true;
        }
        World world = player.getWorld();
        if (!world.hasSkyLight()) {
            player.sendMessage("当前世界没有天气变化。");
            return true;
        }
        if (Boolean.FALSE.equals(world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))) {
            player.sendMessage("没有开启天气循环。");
            return true;
        }
        Main.plugin.notify(world, player);
        return true;
    }
}
