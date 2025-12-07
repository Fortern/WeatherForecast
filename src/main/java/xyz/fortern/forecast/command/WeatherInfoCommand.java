package xyz.fortern.forecast.command;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.fortern.forecast.Main;
import xyz.fortern.forecast.i18n.LanguageManager;

public class WeatherInfoCommand implements CommandExecutor {
    private final LanguageManager languageManager;

    public WeatherInfoCommand(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(languageManager.message("command.player-only"));
            return true;
        }
        World world = player.getWorld();
        if (!world.hasSkyLight()) {
            player.sendMessage(languageManager.message(player, "command.no-weather"));
            return true;
        }
        if (Boolean.FALSE.equals(world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE))) {
            player.sendMessage(languageManager.message(player, "command.weather-cycle-disabled"));
            return true;
        }
        Main.plugin.notify(world, player);
        return true;
    }
}
