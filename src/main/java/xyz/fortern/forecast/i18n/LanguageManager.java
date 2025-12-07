package xyz.fortern.forecast.i18n;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LanguageManager {
    private static final String BUILT_IN_FALLBACK_LOCALE = "zh_cn";
    private static final Set<String> BUILT_IN_LOCALES = Set.of(BUILT_IN_FALLBACK_LOCALE, "en_us");

    private final JavaPlugin plugin;
    private final Map<String, YamlConfiguration> languages = new HashMap<>();
    private final Set<String> missingKeyWarnings = new HashSet<>();
    private final boolean usePlayerLocale;
    private String defaultLocale;

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.defaultLocale = normalizeLocale(plugin.getConfig().getString("default-locale", BUILT_IN_FALLBACK_LOCALE));
        this.usePlayerLocale = plugin.getConfig().getBoolean("use-player-locale", true);
        loadLanguages();
    }

    public String message(Player player, String key) {
        return message(player, key, Map.of());
    }

    public String message(Player player, String key, Map<String, ?> placeholders) {
        String locale = usePlayerLocale ? normalizeLocale(player.getLocale()) : defaultLocale;
        return render(locale, key, placeholders);
    }

    public String message(String key) {
        return render(defaultLocale, key, Map.of());
    }

    private void loadLanguages() {
        for (String locale : BUILT_IN_LOCALES) {
            plugin.saveResource("lang/" + locale + ".yml", false);
        }

        File languageDirectory = new File(plugin.getDataFolder(), "lang");
        File[] languageFiles = languageDirectory.listFiles(
                file -> file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".yml")
        );
        if (languageFiles != null) {
            for (File languageFile : languageFiles) {
                String fileName = languageFile.getName();
                String locale = normalizeLocale(fileName.substring(0, fileName.length() - ".yml".length()));
                languages.put(locale, YamlConfiguration.loadConfiguration(languageFile));
            }
        }

        if (!languages.containsKey(defaultLocale)) {
            plugin.getLogger().warning(
                    "Default locale '" + defaultLocale + "' was not found; falling back to " + BUILT_IN_FALLBACK_LOCALE
            );
            defaultLocale = BUILT_IN_FALLBACK_LOCALE;
        }
    }

    private String render(String locale, String key, Map<String, ?> placeholders) {
        String template = getTemplate(locale, key);
        for (Map.Entry<String, ?> placeholder : placeholders.entrySet()) {
            template = template.replace(
                    "{" + placeholder.getKey() + "}",
                    String.valueOf(placeholder.getValue())
            );
        }
        return ChatColor.translateAlternateColorCodes('&', template);
    }

    private String getTemplate(String locale, String key) {
        YamlConfiguration language = languages.getOrDefault(locale, languages.get(defaultLocale));
        String template = language == null ? null : language.getString(key);
        if (template == null && !locale.equals(defaultLocale)) {
            YamlConfiguration fallbackLanguage = languages.get(defaultLocale);
            template = fallbackLanguage == null ? null : fallbackLanguage.getString(key);
        }
        if (template == null && missingKeyWarnings.add(key)) {
            plugin.getLogger().warning("Missing language key: " + key);
        }
        return template == null ? key : template;
    }

    private static String normalizeLocale(String locale) {
        return locale.toLowerCase(Locale.ROOT).replace('-', '_');
    }
}
