package com.anionianonion.advanced_arpg_attributes_api.util;

import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttribute;
import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesMod;
import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class RandomHelpers {

    public static AdvancedARPGAttribute getValidAAAttribute(String attributeId) {
        ResourceLocation rl = ResourceLocation.tryParse(attributeId);
        if(rl == null) return null;

        var minecraftAttribute = ForgeRegistries.ATTRIBUTES.getValue(rl);
        if(minecraftAttribute == null) return null;

        return AdvancedARPGAttributesRegistry.get(rl);
    }

    public static ResourceLocation getResourceLocationOfValidAAAttribute(@NotNull AdvancedARPGAttribute attribute) {
        return attribute.getRl();
    }
}
