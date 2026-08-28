package com.anionianonion.advanced_arpg_attributes_api.util;

import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttribute;
import com.anionianonion.advanced_arpg_attributes_api.AdvancedARPGAttributesMod;
import net.minecraft.resources.ResourceLocation;

public class RandomHelpers {

    public static AdvancedARPGAttribute getValidAAAttribute(String attributeId) {
        ResourceLocation rl = ResourceLocation.tryParse(attributeId);
        if(rl == null) return null;

        return AdvancedARPGAttribute.get(rl);
    }

    public static ResourceLocation getValidResourceLocationOfValidAAAttribute(String attributeId) {
        ResourceLocation rl = ResourceLocation.tryParse(attributeId);
        if(rl == null) {
            AdvancedARPGAttributesMod.LOGGER.info("cannot parse when trying to getValidResourceLocationOfValidAAAttribute " + attributeId);
            return null;
        }

        if(AdvancedARPGAttribute.get(rl) == null) {
            AdvancedARPGAttributesMod.LOGGER.info("in getValidResourceLocationOfValidAAAttribute, AAAttribute.get(rl) is null for " + attributeId);
            return null;
        }
        return rl;
    }
}
