package net.wanmine.mccolor.color

import com.intellij.ui.JBColor
import java.awt.Color

/**
 * Minecraft Bedrock legacy color codes using the section sign '§' (U+00A7).
 * Only color codes are mapped here (no bold/italic/etc formatting).
 */
object LegacyColorCodeResolver {

    // Based on the Bedrock Edition color code table.
    private val BEDROCK_COLORS: Map<Char, JBColor> = mapOf(
        '0' to fixed(0x00, 0x00, 0x00), // black
        '1' to fixed(0x00, 0x00, 0xAA), // dark_blue
        '2' to fixed(0x00, 0xAA, 0x00), // dark_green
        '3' to fixed(0x00, 0xAA, 0xAA), // dark_aqua
        '4' to fixed(0xAA, 0x00, 0x00), // dark_red
        '5' to fixed(0xAA, 0x00, 0xAA), // dark_purple
        '6' to fixed(0xFF, 0xAA, 0x00), // gold
        '7' to fixed(0xAA, 0xAA, 0xAA), // gray
        '8' to fixed(0x55, 0x55, 0x55), // dark_gray
        '9' to fixed(0x55, 0x55, 0xFF), // blue
        'a' to fixed(0x55, 0xFF, 0x55), // green
        'b' to fixed(0x55, 0xFF, 0xFF), // aqua
        'c' to fixed(0xFF, 0x55, 0x55), // red
        'd' to fixed(0xFF, 0x55, 0xFF), // light_purple
        'e' to fixed(0xFF, 0xFF, 0x55), // yellow
        'f' to fixed(0xFF, 0xFF, 0xFF), // white

        // Bedrock-only material colors.
        'g' to fixed(0xDD, 0xD6, 0x05), // minecoin_gold
        'h' to fixed(0xE3, 0xD4, 0xD1), // material_quartz
        'i' to fixed(0xCE, 0xCA, 0xCA), // material_iron
        'j' to fixed(0x44, 0x3A, 0x3B), // material_netherite
        'm' to fixed(0x97, 0x16, 0x07), // material_redstone
        'n' to fixed(0xB4, 0x68, 0x4D), // material_copper
        'p' to fixed(0xDE, 0xB1, 0x2D), // material_gold
        'q' to fixed(0x47, 0xA0, 0x36), // material_emerald
        's' to fixed(0x2C, 0xBA, 0xA8), // material_diamond
        't' to fixed(0x21, 0x49, 0x7B), // material_lapis
        'u' to fixed(0x9A, 0x5C, 0xC6), // material_amethyst
        'v' to fixed(0xEB, 0x71, 0x14)  // material_resin
    )

    fun resolve(code: Char): Color? = BEDROCK_COLORS[code.lowercaseChar()]

    private fun fixed(r: Int, g: Int, b: Int): JBColor {
        val c = Color(r, g, b)
        return JBColor(c, c)
    }
}
