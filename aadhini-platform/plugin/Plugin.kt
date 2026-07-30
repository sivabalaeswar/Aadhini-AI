package ai.aadhini.platform.plugin

data class Plugin(
    val id: PluginId,
    val type: PluginType,
    val name: String,
    val properties: PluginProperties = emptyMap()
)