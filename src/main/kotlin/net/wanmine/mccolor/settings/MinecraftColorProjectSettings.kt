package net.wanmine.mccolor.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import net.wanmine.mccolor.color.ColorVector
import kotlin.math.roundToInt

@State(
    name = "MinecraftColorFormatterSettings",
    storages = [Storage("minecraft-color-formatter.xml")]
)
class MinecraftColorProjectSettings : PersistentStateComponent<MinecraftColorProjectSettings.State> {

    data class State(
        var c0: String = DEFAULT_0,
        var c1: String = DEFAULT_1,
        var c2: String = DEFAULT_2,
        var c3: String = DEFAULT_3,
        var c4: String = DEFAULT_4,
        var c5: String = DEFAULT_5,
        var c6: String = DEFAULT_6,
        var c7: String = DEFAULT_7,
        var c8: String = DEFAULT_8,
        var c9: String = DEFAULT_9,
        var ca: String = DEFAULT_A,
        var cb: String = DEFAULT_B,
        var cc: String = DEFAULT_C,
        var cd: String = DEFAULT_D,
        var ce: String = DEFAULT_E,
        var cf: String = DEFAULT_F,

        // Bedrock-only material colors
        var minecoinGold: String = DEFAULT_MINECOIN_GOLD, // §g
        var materialQuartz: String = DEFAULT_MATERIAL_QUARTZ, // §h
        var materialIron: String = DEFAULT_MATERIAL_IRON, // §i
        var materialNetherite: String = DEFAULT_MATERIAL_NETHERITE, // §j
        var materialRedstone: String = DEFAULT_MATERIAL_REDSTONE, // §m
        var materialCopper: String = DEFAULT_MATERIAL_COPPER, // §n
        var materialGold: String = DEFAULT_MATERIAL_GOLD, // §p
        var materialEmerald: String = DEFAULT_MATERIAL_EMERALD, // §q
        var materialDiamond: String = DEFAULT_MATERIAL_DIAMOND, // §s
        var materialLapis: String = DEFAULT_MATERIAL_LAPIS, // §t
        var materialAmethyst: String = DEFAULT_MATERIAL_AMETHYST, // §u
        var materialResin: String = DEFAULT_MATERIAL_RESIN // §v
    )

    private var data = State()

    override fun getState(): State = data

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.data)
        migrateHexIfNeeded()
    }

    fun settingsState(): State = data

    private fun migrateHexIfNeeded() {
        // Backwards compatibility: older versions stored some materials as "#RRGGBB".
        data.minecoinGold = normalize(data.minecoinGold, DEFAULT_MINECOIN_GOLD)
        data.materialQuartz = normalize(data.materialQuartz, DEFAULT_MATERIAL_QUARTZ)
        data.materialIron = normalize(data.materialIron, DEFAULT_MATERIAL_IRON)
        data.materialNetherite = normalize(data.materialNetherite, DEFAULT_MATERIAL_NETHERITE)
        data.materialRedstone = normalize(data.materialRedstone, DEFAULT_MATERIAL_REDSTONE)
        data.materialCopper = normalize(data.materialCopper, DEFAULT_MATERIAL_COPPER)
        data.materialGold = normalize(data.materialGold, DEFAULT_MATERIAL_GOLD)
        data.materialEmerald = normalize(data.materialEmerald, DEFAULT_MATERIAL_EMERALD)
        data.materialDiamond = normalize(data.materialDiamond, DEFAULT_MATERIAL_DIAMOND)
        data.materialLapis = normalize(data.materialLapis, DEFAULT_MATERIAL_LAPIS)
        data.materialAmethyst = normalize(data.materialAmethyst, DEFAULT_MATERIAL_AMETHYST)
        data.materialResin = normalize(data.materialResin, DEFAULT_MATERIAL_RESIN)
    }

    private fun normalize(value: String, defaultValue: String): String {
        val v = value.trim()
        if (v.isEmpty()) return defaultValue
        if (v.startsWith("#") && v.length >= 7) return hexToVec(v) ?: defaultValue
        if (ColorVector.parse(v) != null) return v
        return defaultValue
    }

    private fun hexToVec(hex: String): String? {
        val cleaned = hex.removePrefix("#").take(6)
        val r = cleaned.substring(0, 2).toIntOrNull(16) ?: return null
        val g = cleaned.substring(2, 4).toIntOrNull(16) ?: return null
        val b = cleaned.substring(4, 6).toIntOrNull(16) ?: return null
        fun f(x: Int): Float = ((x / 255f) * 1000f).roundToInt() / 1000f
        return "[ ${f(r)}, ${f(g)}, ${f(b)} ]"
    }

    companion object {
        fun getInstance(project: Project): MinecraftColorProjectSettings =
            project.getService(MinecraftColorProjectSettings::class.java)

        // Defaults in the same format as Minecraft resource pack `_global_variables.json`.
        const val DEFAULT_0 = "[ 0.0, 0.0, 0.0 ]"
        const val DEFAULT_1 = "[ 0.0, 0.0, 0.667 ]"
        const val DEFAULT_2 = "[ 0.0, 0.667, 0.0 ]"
        const val DEFAULT_3 = "[ 0.0, 0.667, 0.667 ]"
        const val DEFAULT_4 = "[ 0.667, 0.0, 0.0 ]"
        const val DEFAULT_5 = "[ 0.667, 0.0, 0.667 ]"
        const val DEFAULT_6 = "[ 1.0, 0.667, 0.0 ]"
        const val DEFAULT_7 = "[ 0.776, 0.776, 0.776 ]"
        const val DEFAULT_8 = "[ 0.333, 0.333, 0.333 ]"
        const val DEFAULT_9 = "[ 0.333, 0.333, 1.0 ]"
        const val DEFAULT_A = "[ 0.333, 1.0, 0.333 ]"
        const val DEFAULT_B = "[ 0.333, 1.0, 1.0 ]"
        const val DEFAULT_C = "[ 1.0, 0.333, 0.333 ]"
        const val DEFAULT_D = "[ 1.0, 0.333, 1.0 ]"
        const val DEFAULT_E = "[ 1.0, 1.0, 0.333 ]"
        const val DEFAULT_F = "[ 1.0, 1.0, 1.0 ]"

        // Material defaults from `_global_variables.json` style values.
        const val DEFAULT_MINECOIN_GOLD = "[ 0.867, 0.839, 0.02 ]"
        const val DEFAULT_MATERIAL_QUARTZ = "[ 0.89, 0.831, 0.82 ]"
        const val DEFAULT_MATERIAL_IRON = "[ 0.808, 0.792, 0.792 ]"
        const val DEFAULT_MATERIAL_NETHERITE = "[ 0.267, 0.227, 0.231 ]"
        const val DEFAULT_MATERIAL_REDSTONE = "[ 0.592, 0.086, 0.027 ]"
        const val DEFAULT_MATERIAL_COPPER = "[ 0.706, 0.408, 0.302 ]"
        const val DEFAULT_MATERIAL_GOLD = "[ 0.871, 0.694, 0.176 ]"
        const val DEFAULT_MATERIAL_EMERALD = "[ 0.067, 0.627, 0.212 ]"
        const val DEFAULT_MATERIAL_DIAMOND = "[ 0.173, 0.729, 0.659 ]"
        const val DEFAULT_MATERIAL_LAPIS = "[ 0.129, 0.286, 0.482 ]"
        const val DEFAULT_MATERIAL_AMETHYST = "[ 0.604, 0.361, 0.776 ]"
        const val DEFAULT_MATERIAL_RESIN = "[ 0.92, 0.447, 0.078 ]"
    }
}
