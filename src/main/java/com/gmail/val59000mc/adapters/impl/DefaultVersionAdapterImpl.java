package com.gmail.val59000mc.adapters.impl;

import com.gmail.val59000mc.adapters.VersionAdapter;
import libs.com.pieterdebot.biomemapping.Biome;
import libs.com.pieterdebot.biomemapping.BiomeMappingAPI;

import io.papermc.lib.PaperLib;

import java.util.ArrayList;
import java.util.List;

/**
 * A default {@link VersionAdapter} implementation, used as a fallback.
 */
public class DefaultVersionAdapterImpl extends VersionAdapter {

    @Override
    public void removeOceans() {
        final int version = PaperLib.getMinecraftVersion();
        if (8 <= version && version <= 17) {
            removeOceansUsingBiomeMapping();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void removeOceansUsingBiomeMapping() {
        List<Biome> additional = List.of(
                Biome.DESERT,
                Biome.DESERT_HILLS,
                Biome.STONE_SHORE,
                Biome.BADLANDS,
                Biome.WOODED_BADLANDS_PLATEAU,
                Biome.BADLANDS_PLATEAU,
                Biome.DESERT_LAKES,
                Biome.ICE_SPIKES,
                Biome.ERODED_BADLANDS,
                Biome.MODIFIED_WOODED_BADLANDS_PLATEAU,
                Biome.MODIFIED_BADLANDS_PLATEAU
        );

        final BiomeMappingAPI biomeMapping = new BiomeMappingAPI();
        Biome replacementBiome = Biome.PLAINS;

        for (Biome biome : Biome.values()) {
            if ((biome.isOcean() || biome.isJungle() || additional.contains(biome)) && biomeMapping.biomeSupported(biome)) {
                try {
                    biomeMapping.replaceBiomes(biome, replacementBiome);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                replacementBiome = replacementBiome == Biome.PLAINS ? Biome.FOREST : Biome.PLAINS;
            }
        }
    }

}
