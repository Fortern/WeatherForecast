package xyz.fortern.forecast;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.fortern.forecast.command.WeatherInfoCommand;
import xyz.fortern.forecast.listener.AllListener;

import java.util.Objects;

public final class Main extends JavaPlugin {
    public static Main plugin;

    private static final TextComponent clearText;
    private static final TextComponent rainText;
    private static final TextComponent thunderText;

    static {
        clearText = new TextComponent("晴天");
        clearText.setColor(ChatColor.BLUE);
        rainText = new TextComponent("降雨");
        rainText.setColor(ChatColor.BLUE);
        thunderText = new TextComponent("雷暴");
        thunderText.setColor(ChatColor.BLUE);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        Objects.requireNonNull(Bukkit.getPluginCommand("forecast")).setExecutor(new WeatherInfoCommand());
        var pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new AllListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * 判断world此时的天气情况，并像player发送通知
     *
     * @param world  world
     * @param player player
     */
    public void notify(World world, Player player) {
        // 当前是否晴天
        boolean clearWeather = !world.hasStorm();
        // 当前晴雨转换的剩余时间
        int weatherDuration = world.getWeatherDuration();
        // 是否雷雨
        boolean thundering = world.isThundering();
        // 雷雨转换剩余时间
        int thunderDuration = world.getThunderDuration();
        if (clearWeather) {
            var durationText = new TextComponent(Main.plugin.convertTickFormat(weatherDuration));
            durationText.setColor(ChatColor.RED);
            player.spigot().sendMessage(
                    new ComponentBuilder("当前")
                            .append(clearText)
                            .append("，持续 ", ComponentBuilder.FormatRetention.NONE)
                            .append(durationText)
                            .build()
            );
            // 变成雨天的那一刻，是否在打雷呢？
            if (thunderDuration > weatherDuration) {
                if (thundering) {
                    // 将变成雷暴
                    player.spigot().sendMessage(new ComponentBuilder("之后变为").append(thunderText).build());
                } else {
                    //将变成普通下雨
                    player.spigot().sendMessage(new ComponentBuilder("之后变为").append(rainText).build());
                }
            } else {
                if (thundering) {
                    if (thunderDuration + 12000 > weatherDuration) {
                        player.spigot().sendMessage(new ComponentBuilder("之后变为").append(thunderText).append("(无雷)").build());
                    } else {
                        player.spigot().sendMessage(new ComponentBuilder("之后变为").append(rainText).build());
                    }
                } else {
                    if (thunderDuration + 3600 > weatherDuration) {
                        player.spigot().sendMessage(new ComponentBuilder("之后变为").append(thunderText).build());
                    } else {
                        player.spigot().sendMessage(new ComponentBuilder("之后变为").append(rainText).build());
                    }
                }
            }
        } else {
            var text = new ComponentBuilder("当前");
            if (thundering) {
                text.append(thunderText).append("，", ComponentBuilder.FormatRetention.NONE);
                if (thunderDuration < weatherDuration) {
                    TextComponent durationText = new TextComponent(convertTickFormat(thunderDuration));
                    durationText.setColor(ChatColor.RED);
                    text.append(durationText)
                            .append(" 后转为", ComponentBuilder.FormatRetention.NONE)
                            .append(rainText)
                            .append("，", ComponentBuilder.FormatRetention.NONE);
                }
            } else {
                text.append(rainText).append("，", ComponentBuilder.FormatRetention.NONE);
                if (thunderDuration < weatherDuration) {
                    TextComponent durationText = new TextComponent(convertTickFormat(thunderDuration));
                    durationText.setColor(ChatColor.RED);
                    text.append(durationText)
                            .append(" 后转为", ComponentBuilder.FormatRetention.NONE)
                            .append(thunderText)
                            .append("，", ComponentBuilder.FormatRetention.NONE);
                }
            }
            TextComponent durationText = new TextComponent(convertTickFormat(weatherDuration));
            durationText.setColor(ChatColor.RED);
            text.append(durationText)
                    .append(" 后转为", ComponentBuilder.FormatRetention.NONE)
                    .append(clearText);
            player.spigot().sendMessage(text.build());
        }
    }

    private String convertTickFormat(int tick) {
        // 天数
        int days = tick / 24000;
        // 不足1天的tick数
        int inDay = tick % 24000;
        // 小时数
        int hours = inDay / 1000;
        // 不足1小时的tick数
        int inHour = inDay % 1000;
        // 几个10分钟
        int tenMin = inHour / 167 * 10;
        String result;
        if (days > 0) {
            result = days + " day " + hours + " hour " + tenMin + " min";
        } else {
            if (hours > 0) {
                result = hours + " hour " + tenMin + " min";
            } else {
                if (tenMin > 10) {
                    result = tenMin + " min";
                } else {
                    result = (int) (inHour / 16.666) + " min";
                }
            }
        }
        return result;
    }
}
