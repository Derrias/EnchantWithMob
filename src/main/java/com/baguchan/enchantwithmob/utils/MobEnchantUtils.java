package com.baguchan.enchantwithmob.utils;

import com.baguchan.enchantwithmob.capability.MobEnchantCapability;
import com.baguchan.enchantwithmob.capability.MobEnchantHandler;
import com.baguchan.enchantwithmob.mobenchant.MobEnchant;
import com.baguchan.enchantwithmob.registry.MobEnchants;
import com.baguchan.enchantwithmob.registry.ModItems;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MobEnchantUtils {
    public static final String TAG_MOBENCHANT = "MobEnchant";
    public static final String TAG_ENCHANT_LEVEL = "EnchantLevel";
    public static final String TAG_STORED_MOBENCHANTS = "StoredMobEnchants";

    /**
     * get MobEnchant From NBT
     *
     * @param tag
     */
    @Nullable
    public static MobEnchant getEnchantFromNBT(@Nullable CompoundNBT tag) {
        if (tag != null && MobEnchants.getRegistry().containsKey(ResourceLocation.tryCreate(tag.getString(TAG_MOBENCHANT)))) {
            return MobEnchants.getRegistry().getValue(ResourceLocation.tryCreate(tag.getString(TAG_MOBENCHANT)));
        } else {
            return null;
        }
    }

    /**
     * get MobEnchant Level From NBT
     *
     * @param tag
     */
    public static int getEnchantLevelFromNBT(@Nullable CompoundNBT tag) {
        if (tag != null) {
            return tag.getInt(TAG_ENCHANT_LEVEL);
        } else {
            return 0;
        }
    }

    /**
     * get MobEnchant From String
     *
     * @param id
     */
    @Nullable
    public static MobEnchant getEnchantFromString(@Nullable String id) {
        if (id != null && MobEnchants.getRegistry().containsKey(ResourceLocation.tryCreate(id))) {
            return MobEnchants.getRegistry().getValue(ResourceLocation.tryCreate(id));
        } else {
            return null;
        }
    }

    /**
     * check ItemStack has Mob Enchant
     *
     * @param stack
     */
    public static boolean hasMobEnchant(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTag();
        return compoundnbt != null && compoundnbt.contains(TAG_STORED_MOBENCHANTS);
    }

    /**
     * check NBT has Mob Enchant
     *
     * @param compoundnbt
     */
    public static ListNBT getEnchantmentListForNBT(CompoundNBT compoundnbt) {
        return compoundnbt != null ? compoundnbt.getList(TAG_STORED_MOBENCHANTS, 10) : new ListNBT();
    }

    /**
     * get Mob Enchantments From ItemStack
     *
     * @param stack
     */
    public static Map<MobEnchant, Integer> getEnchantments(ItemStack stack) {
        ListNBT listnbt = getEnchantmentListForNBT(stack.getTag());
        return makeMobEnchantListFromListNBT(listnbt);
    }

    /**
     * set Mob Enchantments From ItemStack
     *
     * @param enchMap MobEnchants and those level map
     * @param stack   MobEnchanted Item
     */
    public static void setEnchantments(Map<MobEnchant, Integer> enchMap, ItemStack stack) {
        ListNBT listnbt = new ListNBT();

        for (Map.Entry<MobEnchant, Integer> entry : enchMap.entrySet()) {
            MobEnchant enchantment = entry.getKey();
            if (enchantment != null) {
                int i = entry.getValue();
                CompoundNBT compoundnbt = new CompoundNBT();
                compoundnbt.putString(TAG_MOBENCHANT, String.valueOf((Object) MobEnchants.getRegistry().getKey(enchantment)));
                compoundnbt.putShort(TAG_ENCHANT_LEVEL, (short) i);
                listnbt.add(compoundnbt);
                if (stack.getItem() == ModItems.MOB_ENCHANT_BOOK) {
                    addMobEnchantToItemStack(stack, enchantment, i);
                }
            }
        }

        if (listnbt.isEmpty()) {
            stack.removeChildTag(TAG_STORED_MOBENCHANTS);
        }
    }

    private static Map<MobEnchant, Integer> makeMobEnchantListFromListNBT(ListNBT p_226652_0_) {
        Map<MobEnchant, Integer> map = Maps.newLinkedHashMap();

        for (int i = 0; i < p_226652_0_.size(); ++i) {
            CompoundNBT compoundnbt = p_226652_0_.getCompound(i);
            MobEnchant mobEnchant = getEnchantFromString(compoundnbt.getString(TAG_MOBENCHANT));
            map.put(mobEnchant, compoundnbt.getInt(TAG_ENCHANT_LEVEL));

        }

        return map;
    }

    //add MobEnchantToItemstack (example,this method used to MobEnchantBook)
    public static void addMobEnchantToItemStack(ItemStack itemIn, MobEnchant mobenchant, int level) {
        ListNBT listnbt = getEnchantmentListForNBT(itemIn.getTag());

        boolean flag = true;
        ResourceLocation resourcelocation = MobEnchants.getRegistry().getKey(mobenchant);


        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            ResourceLocation resourcelocation1 = ResourceLocation.tryCreate(compoundnbt.getString("MobEnchant"));
            if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
                if (compoundnbt.getInt(TAG_ENCHANT_LEVEL) < level) {
                    compoundnbt.putInt(TAG_ENCHANT_LEVEL, level);
                }

                flag = false;
                break;
            }
        }

        if (flag) {
            CompoundNBT compoundnbt1 = new CompoundNBT();
            compoundnbt1.putString(TAG_MOBENCHANT, String.valueOf((Object) resourcelocation));
            compoundnbt1.putInt(TAG_ENCHANT_LEVEL, level);
            listnbt.add(compoundnbt1);
        }

        itemIn.getTag().put(TAG_STORED_MOBENCHANTS, listnbt);
    }

    public static void addMobEnchantToEntityFromItem(ItemStack stack, LivingEntity target, MobEnchantCapability cap) {
        if (cap.hasEnchant()) {
            boolean flag = true;
            for (MobEnchantHandler mobEnchant : cap.mobEnchants) {
                if (mobEnchant.getMobEnchant() != null) {

                    for (MobEnchant enchantment : MobEnchantUtils.getEnchantments(stack).keySet()) {
                        if (enchantment == mobEnchant.getMobEnchant() || !mobEnchant.getMobEnchant().isCompatibleWith(enchantment)) {
                            flag = false;
                            break;
                        }
                    }
                }
            }
            if (flag) {
                MobEnchantUtils.addMobEnchantToEntity(stack, target, cap);
            }
        } else {
            MobEnchantUtils.addMobEnchantToEntity(stack, target, cap);
        }
    }

    public static void addMobEnchantToEntity(ItemStack itemIn, LivingEntity entity, MobEnchantCapability capability) {
        ListNBT listnbt = getEnchantmentListForNBT(itemIn.getTag());
        for (int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            capability.addMobEnchant(entity, MobEnchantUtils.getEnchantFromNBT(compoundnbt), MobEnchantUtils.getEnchantLevelFromNBT(compoundnbt));
        }
    }

    public static void addRandomEnchantmentToEntity(LivingEntity livingEntity, MobEnchantCapability capability, Random random, int level, boolean allowRare) {
        List<MobEnchantmentData> list = buildEnchantmentList(random, level, allowRare);

        for (MobEnchantmentData enchantmentdata : list) {
            capability.addMobEnchant(livingEntity, enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
        }
    }

    public static ItemStack addRandomEnchantmentToItemStack(Random random, ItemStack stack, int level, boolean allowRare) {
        List<MobEnchantmentData> list = buildEnchantmentList(random, level, allowRare);

        for (MobEnchantmentData enchantmentdata : list) {
            addMobEnchantToItemStack(stack, enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
        }

        return stack;
    }

    public static boolean findMobEnchant(List<MobEnchant> list, MobEnchant findMobEnchant) {
        for (MobEnchant mobEnchant : list) {
            if (mobEnchant.equals(findMobEnchant)) {
                return true;
            }
        }
        return false;
    }

    public static boolean findMobEnchantFromHandler(List<MobEnchantHandler> list, MobEnchant findMobEnchant) {
        for (MobEnchantHandler mobEnchant : list) {
            if (mobEnchant != null) {
                if (mobEnchant.getMobEnchant().equals(findMobEnchant)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getMobEnchantLevelFromHandler(List<MobEnchantHandler> list, MobEnchant findMobEnchant) {
        for (MobEnchantHandler mobEnchant : list) {
            if (mobEnchant != null) {
                if (mobEnchant.getMobEnchant().equals(findMobEnchant)) {
                    return mobEnchant.getEnchantLevel();
                }
            }
        }
        return 0;
    }

    /*
     * build MobEnchantment list like vanilla's enchantment
     */
    public static List<MobEnchantmentData> buildEnchantmentList(Random randomIn, int level, boolean allowRare) {
        List<MobEnchantmentData> list = Lists.newArrayList();
        int i = 1; //Enchantability
        if (i <= 0) {
            return list;
        } else {
            level = level + 1 + randomIn.nextInt(i / 4 + 1) + randomIn.nextInt(i / 4 + 1);
            float f = (randomIn.nextFloat() + randomIn.nextFloat() - 1.0F) * 0.15F;
            level = MathHelper.clamp(Math.round((float) level + (float) level * f), 1, Integer.MAX_VALUE);
            List<MobEnchantmentData> list1 = getMobEnchantmentDatas(level, allowRare);
            if (!list1.isEmpty()) {
                list.add(WeightedRandom.getRandomItem(randomIn, list1));

                while (randomIn.nextInt(50) <= level) {
                    removeIncompatible(list1, Util.getLast(list));
                    if (list1.isEmpty()) {
                        break;
                    }

                    list.add(WeightedRandom.getRandomItem(randomIn, list1));
                    level /= 2;
                }
            }

            return list;
        }
    }

    /*
     * get MobEnchantment data.
     * when not allow rare enchantment,Ignore rare enchantment
     */
    public static List<MobEnchantmentData> getMobEnchantmentDatas(int p_185291_0_, boolean allowRare) {
        List<MobEnchantmentData> list = Lists.newArrayList();

        for (MobEnchant enchantment : MobEnchants.getRegistry().getValues()) {
            if ((enchantment.getRarity() != MobEnchant.Rarity.RARE && enchantment.getRarity() != MobEnchant.Rarity.VERY_RARE || allowRare)) {
                for (int i = enchantment.getMaxLevel(); i > enchantment.getMinLevel() - 1; --i) {
                    if (p_185291_0_ >= enchantment.getMinEnchantability(i) && p_185291_0_ <= enchantment.getMaxEnchantability(i)) {
                        list.add(new MobEnchantmentData(enchantment, i));
                        break;
                    }
                }
            }
        }

        return list;
    }

    public static void removeIncompatible(List<MobEnchantmentData> dataList, MobEnchantmentData data) {
        Iterator<MobEnchantmentData> iterator = dataList.iterator();

        //TODO need to Incompatible System on MobEnchantment?

        while (iterator.hasNext()) {
            if (data.enchantment == iterator.next().enchantment) {
                iterator.remove();
            }
        }

    }
}
