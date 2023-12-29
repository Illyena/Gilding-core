package illyena.gilding.core.enchantment;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

@SuppressWarnings("unused")
public class GildingEnchantmentHelper extends EnchantmentHelper {

    public static boolean hasThunderous(ItemStack stack) { return getLevel(GildingEnchantments.THUNDEROUS, stack) > 0; }

    public static boolean hasRicochet(ItemStack stack) { return getLevel(GildingEnchantments.RICOCHET, stack) > 0; }

    public static int getRicochet(ItemStack stack) { return getLevel(GildingEnchantments.RICOCHET, stack); }

}
