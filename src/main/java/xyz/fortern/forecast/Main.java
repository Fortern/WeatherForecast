package xyz.fortern.forecast;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.fortern.forecast.command.WeatherInfoCommand;
import xyz.fortern.forecast.i18n.LanguageManager;
import xyz.fortern.forecast.listener.AllListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Main extends JavaPlugin {
    public static Main plugin;

    private LanguageManager lang;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        saveDefaultConfig();
        lang = new LanguageManager(this);
        Objects.requireNonNull(Bukkit.getPluginCommand("forecast")).setExecutor(new WeatherInfoCommand(lang));
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new AllListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Notify players of weather
     *
     * @param world  world
     * @param player player
     */
    public void notify(World world, Player player) {
        boolean isClear = !world.hasStorm();
        int weatherDuration = world.getWeatherDuration();
        boolean isThundering = world.isThundering();
        int thunderDuration = world.getThunderDuration();
        List<String> transition = new ArrayList<>(3);
        if (isClear) {
            transition.add(lang.message(player, "forecast.current", Map.of("weather", weatherName(player, "clear"))));
            String nextWeatherKey;
            if (thunderDuration > weatherDuration) {
                if (isThundering) {
                    /* raining    ├──────█████
                     * thundering ████████████
                     *                   ↑
                     *            become thundering
                     */
                    nextWeatherKey = "thunder";
                } else {
                    /* raining    ├──────█████
                     * thundering ├───────────
                     *                   ↑
                     *            become raining
                     */
                    nextWeatherKey = "rain-without-thunder";
                }
            } else {
                if (isThundering) {
                    if (thunderDuration + 12000 > weatherDuration) {
                        /* raining    ├───────████████
                         * thundering █████──────█████
                         *                   ↑
                         *            become raining
                         */
                        nextWeatherKey = "rain-without-thunder";
                    } else {
                        /* raining    ├─────────███
                         * thundering ████────█████
                         *                      ↑
                         *            become raining (thundering or not)
                         */
                        nextWeatherKey = "rain";
                    }
                } else {
                    if (thunderDuration + 3600 > weatherDuration) {
                        /* raining    ├──────████
                         * thundering ├───███████
                         *                   ↑
                         *             become thundering
                         */
                        nextWeatherKey = "thunder";
                    } else {
                        /* raining    ├──────────███
                         * thundering ├───████──────
                         *                       ↑
                         *             become raining (thundering or not)
                         */
                        nextWeatherKey = "rain";
                    }
                }
            }
            transition.add(lang.message(player, "forecast.transition", Map.of(
                    "duration", convertTickFormat(player, weatherDuration),
                    "next_weather", weatherName(player, nextWeatherKey)
            )));
        } else {
            if (isThundering) {
                transition.add(lang.message(player, "forecast.current", Map.of("weather", weatherName(player, "thunder"))));
                if (thunderDuration < weatherDuration) {
                    /*
                     *              become clear
                     *                   ↓
                     * raining    ███████───
                     * thundering ████──────
                     *                ↑
                     *         become raining
                     */
                    transition.add(lang.message(player, "forecast.transition", Map.of(
                            "duration", convertTickFormat(player, thunderDuration),
                            "next_weather", weatherName(player, "rain-without-thunder")
                    )));
                    transition.add(lang.message(player, "forecast.transition", Map.of(
                            "duration", convertTickFormat(player, weatherDuration),
                            "next_weather", weatherName(player, "clear")
                    )));
                } else {
                    /*
                     * raining    ███──────
                     * thundering ██████───
                     *               ↑
                     *          become clear
                     */
                    transition.add(lang.message(player, "forecast.transition", Map.of(
                            "duration", convertTickFormat(player, weatherDuration),
                            "next_weather", weatherName(player, "clear")
                    )));
                }
            } else {
                transition.add(lang.message(player, "forecast.current", Map.of("weather", weatherName(player, "rain-without-thunder"))));
                if (thunderDuration < weatherDuration) {
                    /*
                     *             become clear
                     *                   ↓
                     * raining    ███████─────
                     * thundering ├───█████───
                     *                ↑
                     *          become thundering
                     */
                    transition.add(lang.message(player, "forecast.transition", Map.of(
                            "duration", convertTickFormat(player, thunderDuration),
                            "next_weather", weatherName(player, "thunder")
                    )));
                    transition.add(lang.message(player, "forecast.transition", Map.of(
                            "duration", convertTickFormat(player, weatherDuration),
                            "next_weather", weatherName(player, "clear")
                    )));
                } else {
                    /*
                     * raining    ████─────
                     * thundering ├────────
                     *                ↑
                     *          become clear
                     */
                    transition.add(lang.message(player, "forecast.transition", Map.of(
                            "duration", convertTickFormat(player, weatherDuration),
                            "next_weather", weatherName(player, "clear")
                    )));
                }
            }
        }
        player.sendMessage(String.join("\n", transition));
    }

    private String weatherName(Player player, String weather) {
        return lang.message(player, "weather." + weather);
    }

    private String convertTickFormat(Player player, int tick) {
        int days = tick / 24000;
        int inDay = tick % 24000;
        int hours = inDay / 1000;
        int inHour = inDay % 1000;
        int tenMin = inHour / 167 * 10;
        if (days > 0) {
            return lang.message(player, "duration.day-hour-minute", Map.of(
                    "days", days,
                    "hours", hours,
                    "minutes", tenMin
            ));
        }
        if (hours > 0) {
            return lang.message(player, "duration.hour-minute", Map.of(
                    "hours", hours,
                    "minutes", tenMin
            ));
        }
        int minutes = tenMin > 10 ? tenMin : (int) (inHour / 16.666);
        return lang.message(player, "duration.minute", Map.of("minutes", minutes));
    }
}
