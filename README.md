# WeatherForecast

A weather forecast plugin that tells players exactly when the weather will change next.

## Usage

Players receive a weather forecast when they join the game and whenever the weather changes.
## Languages

The plugin includes built-in support for `zh_cn` and `en_us`. By default, messages are sent in the language configured in each player's Minecraft client.
Language files are generated in `plugins/WeatherReport/lang` on first startup.

Player language detection can be disabled and the default language can be changed in `config.yml`. To add another language,
name the YAML file after its Minecraft language code (for example, `de_de.yml`). Missing languages or message keys fall back to the default language.

## Details

This plugin is implemented entirely with the Spigot API. Its forecasts assume that the vanilla weather mechanics, including the frequency of weather changes, have not been modified.
If another plugin or server modification changes these mechanics, the forecasts may be inaccurate.

Rather than using reflection to read weather-change intervals from Minecraft internals, the plugin uses hard-coded vanilla values.
This avoids relying on version-specific internal APIs and maintains compatibility across Minecraft versions without requiring new compatibility code for every update.
