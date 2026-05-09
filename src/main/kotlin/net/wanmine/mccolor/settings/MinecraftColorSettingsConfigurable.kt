package net.wanmine.mccolor.settings

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import net.wanmine.mccolor.color.ColorVector
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel

class MinecraftColorSettingsConfigurable(
    private val project: Project
) : SearchableConfigurable {

    private var component: JComponent? = null

    private data class Entry(
        val label: String,
        val key: String,
        val get: (MinecraftColorProjectSettings.State) -> String,
        val set: (MinecraftColorProjectSettings.State, String) -> Unit,
        val field: JBTextField = JBTextField(),
        val preview: JPanel = JPanel()
    )

    private val entries: List<Entry> = listOf(
        // Color format colors ($0_color_format ... $f_color_format)
        entry("§0 black", "\$0_color_format", { it.c0 }, { s, v -> s.c0 = v }),
        entry("§1 dark_blue", "\$1_color_format", { it.c1 }, { s, v -> s.c1 = v }),
        entry("§2 dark_green", "\$2_color_format", { it.c2 }, { s, v -> s.c2 = v }),
        entry("§3 dark_aqua", "\$3_color_format", { it.c3 }, { s, v -> s.c3 = v }),
        entry("§4 dark_red", "\$4_color_format", { it.c4 }, { s, v -> s.c4 = v }),
        entry("§5 dark_purple", "\$5_color_format", { it.c5 }, { s, v -> s.c5 = v }),
        entry("§6 gold", "\$6_color_format", { it.c6 }, { s, v -> s.c6 = v }),
        entry("§7 gray", "\$7_color_format", { it.c7 }, { s, v -> s.c7 = v }),
        entry("§8 dark_gray", "\$8_color_format", { it.c8 }, { s, v -> s.c8 = v }),
        entry("§9 blue", "\$9_color_format", { it.c9 }, { s, v -> s.c9 = v }),
        entry("§a green", "\$a_color_format", { it.ca }, { s, v -> s.ca = v }),
        entry("§b aqua", "\$b_color_format", { it.cb }, { s, v -> s.cb = v }),
        entry("§c red", "\$c_color_format", { it.cc }, { s, v -> s.cc = v }),
        entry("§d light_purple", "\$d_color_format", { it.cd }, { s, v -> s.cd = v }),
        entry("§e yellow", "\$e_color_format", { it.ce }, { s, v -> s.ce = v }),
        entry("§f white", "\$f_color_format", { it.cf }, { s, v -> s.cf = v }),

        // Bedrock-only material colors
        entry("§g minecoin_gold", "\$minecoin_gold_color", { it.minecoinGold }, { s, v -> s.minecoinGold = v }),
        entry("§h material_quartz", "\$material_quartz_color", { it.materialQuartz }, { s, v -> s.materialQuartz = v }),
        entry("§i material_iron", "\$material_iron_color", { it.materialIron }, { s, v -> s.materialIron = v }),
        entry("§j material_netherite", "\$material_netherite_color", { it.materialNetherite }, { s, v -> s.materialNetherite = v }),
        entry("§m material_redstone", "\$material_redstone_color", { it.materialRedstone }, { s, v -> s.materialRedstone = v }),
        entry("§n material_copper", "\$material_copper_color", { it.materialCopper }, { s, v -> s.materialCopper = v }),
        entry("§p material_gold", "\$material_gold_color", { it.materialGold }, { s, v -> s.materialGold = v }),
        entry("§q material_emerald", "\$material_emerald_color", { it.materialEmerald }, { s, v -> s.materialEmerald = v }),
        entry("§s material_diamond", "\$material_diamond_color", { it.materialDiamond }, { s, v -> s.materialDiamond = v }),
        entry("§t material_lapis", "\$material_lapis_color", { it.materialLapis }, { s, v -> s.materialLapis = v }),
        entry("§u material_amethyst", "\$material_amethyst_color", { it.materialAmethyst }, { s, v -> s.materialAmethyst = v }),
        entry("§v material_resin", "\$material_resin_color", { it.materialResin }, { s, v -> s.materialResin = v })
    )

    override fun getId(): String = "net.wanmine.mccolor.settings"

    override fun getDisplayName(): String = "Minecraft Color Formatter"

    override fun createComponent(): JComponent {
        if (component == null) {
            component = panel {
                row {
                    button("Import from _global_variables.json") {
                        importFromFile()
                    }
                    button("Reset to defaults") {
                        resetToDefaults()
                    }
                }
                group("Color codes") {
                    entries.forEach { e ->
                        row("${e.label} (${e.key})") {
                            cell(e.field)
                                .align(AlignX.FILL)
                                .applyToComponent { toolTipText = "Format: [ r, g, b ] with floats 0.0..1.0" }
                            cell(e.preview)
                                .applyToComponent {
                                    preferredSize = Dimension(28, 18)
                                    minimumSize = Dimension(28, 18)
                                }
                        }
                    }
                }
            }
            reset()
            updatePreviews()
        }
        return component!!
    }

    override fun isModified(): Boolean {
        val s = MinecraftColorProjectSettings.getInstance(project).settingsState()
        return entries.any { e -> normalize(e.field.text) != normalize(e.get(s)) }
    }

    override fun apply() {
        val settings = MinecraftColorProjectSettings.getInstance(project)
        val s = settings.settingsState()
        entries.forEach { e ->
            val vec = ColorVector.parse(e.field.text)
            if (vec != null) {
                // Normalize everything into the vanilla JSON float format: [ r, g, b ]
                e.set(s, vec.toGlobalVariablesFormat())
                e.field.text = vec.toGlobalVariablesFormat()
            } else {
                e.set(s, normalize(e.field.text))
            }
        }
        updatePreviews()
    }

    override fun reset() {
        val s = MinecraftColorProjectSettings.getInstance(project).settingsState()
        entries.forEach { e -> e.field.text = e.get(s) }
        updatePreviews()
    }

    override fun disposeUIResources() {
        component = null
    }

    private fun importFromFile() {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle("Import colors from _global_variables.json")
            .withDescription("Select a Minecraft resource pack _global_variables.json (or any json containing the color variables).")

        val file = FileChooser.chooseFile(descriptor, project, null) ?: return
        val text = runCatching { file.inputStream.readBytes().toString(Charsets.UTF_8) }.getOrNull() ?: return

        val imported = GlobalVariablesImporter.parse(text)
        if (imported.isEmpty()) return

        // Apply imported values to fields (only known keys).
        for (e in entries) {
            val vec = imported[e.key] ?: continue
            e.field.text = vec.toGlobalVariablesFormat()
        }
        updatePreviews()
    }

    private fun resetToDefaults() {
        val settings = MinecraftColorProjectSettings.getInstance(project)
        val s = settings.settingsState()

        // 0-f
        s.c0 = MinecraftColorProjectSettings.DEFAULT_0
        s.c1 = MinecraftColorProjectSettings.DEFAULT_1
        s.c2 = MinecraftColorProjectSettings.DEFAULT_2
        s.c3 = MinecraftColorProjectSettings.DEFAULT_3
        s.c4 = MinecraftColorProjectSettings.DEFAULT_4
        s.c5 = MinecraftColorProjectSettings.DEFAULT_5
        s.c6 = MinecraftColorProjectSettings.DEFAULT_6
        s.c7 = MinecraftColorProjectSettings.DEFAULT_7
        s.c8 = MinecraftColorProjectSettings.DEFAULT_8
        s.c9 = MinecraftColorProjectSettings.DEFAULT_9
        s.ca = MinecraftColorProjectSettings.DEFAULT_A
        s.cb = MinecraftColorProjectSettings.DEFAULT_B
        s.cc = MinecraftColorProjectSettings.DEFAULT_C
        s.cd = MinecraftColorProjectSettings.DEFAULT_D
        s.ce = MinecraftColorProjectSettings.DEFAULT_E
        s.cf = MinecraftColorProjectSettings.DEFAULT_F

        // Materials
        s.minecoinGold = MinecraftColorProjectSettings.DEFAULT_MINECOIN_GOLD
        s.materialQuartz = MinecraftColorProjectSettings.DEFAULT_MATERIAL_QUARTZ
        s.materialIron = MinecraftColorProjectSettings.DEFAULT_MATERIAL_IRON
        s.materialNetherite = MinecraftColorProjectSettings.DEFAULT_MATERIAL_NETHERITE
        s.materialRedstone = MinecraftColorProjectSettings.DEFAULT_MATERIAL_REDSTONE
        s.materialCopper = MinecraftColorProjectSettings.DEFAULT_MATERIAL_COPPER
        s.materialGold = MinecraftColorProjectSettings.DEFAULT_MATERIAL_GOLD
        s.materialEmerald = MinecraftColorProjectSettings.DEFAULT_MATERIAL_EMERALD
        s.materialDiamond = MinecraftColorProjectSettings.DEFAULT_MATERIAL_DIAMOND
        s.materialLapis = MinecraftColorProjectSettings.DEFAULT_MATERIAL_LAPIS
        s.materialAmethyst = MinecraftColorProjectSettings.DEFAULT_MATERIAL_AMETHYST
        s.materialResin = MinecraftColorProjectSettings.DEFAULT_MATERIAL_RESIN

        // Reflect into UI fields immediately.
        entries.forEach { e -> e.field.text = e.get(s) }
        updatePreviews()
    }

    private fun updatePreviews() {
        entries.forEach { e ->
            val vec = ColorVector.parse(e.field.text)
            if (vec == null) {
                e.preview.background = JBColor.RED
                e.preview.toolTipText = "Invalid format. Expected: [ r, g, b ]"
                e.field.toolTipText = "Invalid format. Expected: [ r, g, b ]"
            } else {
                val c = vec.toJBColor()
                e.preview.background = c
                e.preview.toolTipText = vec.toGlobalVariablesFormat()
                e.field.toolTipText = "Format: [ r, g, b ] with floats 0.0..1.0"
            }
        }
    }

    private fun entry(
        label: String,
        key: String,
        get: (MinecraftColorProjectSettings.State) -> String,
        set: (MinecraftColorProjectSettings.State, String) -> Unit
    ): Entry = Entry(label = label, key = key, get = get, set = set)

    private fun normalize(s: String): String = s.trim().replace(Regex("\\s+"), " ")
}
