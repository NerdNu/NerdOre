package nu.nerd.nerdore;

import org.bukkit.Material;

// ----------------------------------------------------------------------------
/**
 * The material to find or replace.
 * 
 * In the original OrePlus sources, an OPMaterial included a 4 bit data value to
 * distinguish block variants. This is no longer supported, and in most cases is
 * not necessary. For instance, all of the stone variants - andesite, diorite
 * and granite - have their own Material enum values in the 1.13 Bukkit API.
 * 
 * However, Material cannot, on its own, express BlockData states such as
 * Waterlogged. For this reason, CustomMaterial is retained in this source to
 * more easily allow it to be adapted to support BlockData in the future, even
 * though currently CustomMaterial is essentially just a Material.
 */
public class CustomMaterial {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param type the Bukkit Material.
     */
    public CustomMaterial(Material type) {
        _type = type;
    }

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     * 
     * @param materialName the name of the Material.
     */
    public CustomMaterial(String materialName) {
        try {
            _type = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            NerdOre.PLUGIN.getLogger().warning("Invalid material: " + materialName);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the material is valid.
     * 
     * @return true if the material is valid.
     */
    public boolean isValid() {
        return _type != null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the Bukkit Material.
     * 
     * @return the Bukkit Material.
     */
    public Material getType() {
        return _type;
    }

    // ------------------------------------------------------------------------
    /**
     * The Bukkit Material.
     */
    private Material _type;
} // class CustomMaterial
