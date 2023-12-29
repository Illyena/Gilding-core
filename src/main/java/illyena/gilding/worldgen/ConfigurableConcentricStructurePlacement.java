package illyena.gilding.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import illyena.gilding.config.option.ConfigOption;
import illyena.gilding.config.option.IntegerConfigOption;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryCodecs;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

import static illyena.gilding.worldgen.ModdedWorldGen.CONFIGURABLE_CONCENTRIC_RINGS;

public class ConfigurableConcentricStructurePlacement extends ConcentricRingsStructurePlacement {
    IntegerConfigOption spreadConfig;
    IntegerConfigOption countConfig;

    public static final Codec<ConfigurableConcentricStructurePlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConfigurableConcentricStructurePlacement::getDistance),
                    ConfigOption.CONFIG.getCodec().fieldOf("spread_config").forGetter(placement -> placement.spreadConfig),
                    ConfigOption.CONFIG.getCodec().fieldOf("count_config").forGetter(placement -> placement.countConfig),
                    RegistryCodecs.entryList(Registry.BIOME_KEY).fieldOf("preferred_biomes").forGetter(ConcentricRingsStructurePlacement::getPreferredBiomes))
            .apply(instance, ConfigurableConcentricStructurePlacement::new));

    public ConfigurableConcentricStructurePlacement(int distance, ConfigOption<Integer> spread, ConfigOption<Integer> count, RegistryEntryList<Biome> preferredBiomes) {
        super(distance, spread.getValue(), count.getValue(), preferredBiomes);
        this.spreadConfig = (IntegerConfigOption)spread;
        this.countConfig = (IntegerConfigOption)count;
    }

    @Override
    public StructurePlacementType<?> getType() { return CONFIGURABLE_CONCENTRIC_RINGS; }

    public int getSpread() { return this.spreadConfig.getValue(); }

    public int getCount() { return this.countConfig.getValue(); }

}
