package net.wanmine.mccolor.color

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import net.wanmine.mccolor.settings.MinecraftColorProjectSettings

/**
 * Minecraft Bedrock legacy color codes using the section sign '§' (U+00A7).
 * Only color codes are mapped here (no bold/italic/etc formatting).
 */
object LegacyColorCodeResolver {

    fun resolve(code: Char, project: Project?) : JBColor? {
        val c = code.lowercaseChar()
        val s = project?.let { MinecraftColorProjectSettings.getInstance(it).settingsState() }

        val raw = when (c) {
            '0' -> s?.c0 ?: MinecraftColorProjectSettings.DEFAULT_0
            '1' -> s?.c1 ?: MinecraftColorProjectSettings.DEFAULT_1
            '2' -> s?.c2 ?: MinecraftColorProjectSettings.DEFAULT_2
            '3' -> s?.c3 ?: MinecraftColorProjectSettings.DEFAULT_3
            '4' -> s?.c4 ?: MinecraftColorProjectSettings.DEFAULT_4
            '5' -> s?.c5 ?: MinecraftColorProjectSettings.DEFAULT_5
            '6' -> s?.c6 ?: MinecraftColorProjectSettings.DEFAULT_6
            '7' -> s?.c7 ?: MinecraftColorProjectSettings.DEFAULT_7
            '8' -> s?.c8 ?: MinecraftColorProjectSettings.DEFAULT_8
            '9' -> s?.c9 ?: MinecraftColorProjectSettings.DEFAULT_9
            'a' -> s?.ca ?: MinecraftColorProjectSettings.DEFAULT_A
            'b' -> s?.cb ?: MinecraftColorProjectSettings.DEFAULT_B
            'c' -> s?.cc ?: MinecraftColorProjectSettings.DEFAULT_C
            'd' -> s?.cd ?: MinecraftColorProjectSettings.DEFAULT_D
            'e' -> s?.ce ?: MinecraftColorProjectSettings.DEFAULT_E
            'f' -> s?.cf ?: MinecraftColorProjectSettings.DEFAULT_F

            'g' -> s?.minecoinGold ?: MinecraftColorProjectSettings.DEFAULT_MINECOIN_GOLD
            'h' -> s?.materialQuartz ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_QUARTZ
            'i' -> s?.materialIron ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_IRON
            'j' -> s?.materialNetherite ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_NETHERITE
            'm' -> s?.materialRedstone ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_REDSTONE
            'n' -> s?.materialCopper ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_COPPER
            'p' -> s?.materialGold ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_GOLD
            'q' -> s?.materialEmerald ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_EMERALD
            's' -> s?.materialDiamond ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_DIAMOND
            't' -> s?.materialLapis ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_LAPIS
            'u' -> s?.materialAmethyst ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_AMETHYST
            'v' -> s?.materialResin ?: MinecraftColorProjectSettings.DEFAULT_MATERIAL_RESIN
            else -> return null
        }

        val vec = ColorVector.parse(raw) ?: return null
        return vec.toJBColor()
    }
}
