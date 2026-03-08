package com.neofast.tech_revised.recipe;

import com.google.gson.JsonObject;
import com.neofast.tech_revised.TechRevised;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class ArgonOxygenDecarburizationConverterRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient primaryInput;
    private final Ingredient secondaryInput;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;
    private final int oxygenAmount;

    public ArgonOxygenDecarburizationConverterRecipe(ResourceLocation id,
                                                     Ingredient primaryInput,
                                                     Ingredient secondaryInput,
                                                     ItemStack result,
                                                     int processTicks,
                                                     int energyPerTick,
                                                     int oxygenAmount) {
        this.id = id;
        this.primaryInput = primaryInput;
        this.secondaryInput = secondaryInput;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
        this.oxygenAmount = oxygenAmount;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 2) {
            return false;
        }

        ItemStack first = container.getItem(0);
        ItemStack second = container.getItem(1);

        if (secondaryInput.isEmpty()) {
            return primaryInput.test(first);
        }

        return (primaryInput.test(first) && secondaryInput.test(second))
                || (primaryInput.test(second) && secondaryInput.test(first));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient getPrimaryInput() {
        return primaryInput;
    }

    public Ingredient getSecondaryInput() {
        return secondaryInput;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public int getOxygenAmount() {
        return oxygenAmount;
    }

    public boolean hasSecondaryInput() {
        return !secondaryInput.isEmpty();
    }

    public static class Type implements RecipeType<ArgonOxygenDecarburizationConverterRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "argon_oxygen_decarburization_converter";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer implements RecipeSerializer<ArgonOxygenDecarburizationConverterRecipe> {
        @Override
        public ArgonOxygenDecarburizationConverterRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient primaryInput = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "primary_input"));
            Ingredient secondaryInput = json.has("secondary_input")
                    ? Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "secondary_input"))
                    : Ingredient.EMPTY;
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 120);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 80);
            int oxygenAmount = GsonHelper.getAsInt(json, "oxygen_amount", 500);

            return new ArgonOxygenDecarburizationConverterRecipe(
                    recipeId,
                    primaryInput,
                    secondaryInput,
                    result,
                    processTicks,
                    energyPerTick,
                    oxygenAmount
            );
        }

        @Override
        public ArgonOxygenDecarburizationConverterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient primaryInput = Ingredient.fromNetwork(buffer);
            Ingredient secondaryInput = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            int oxygenAmount = buffer.readVarInt();

            return new ArgonOxygenDecarburizationConverterRecipe(
                    recipeId,
                    primaryInput,
                    secondaryInput,
                    result,
                    processTicks,
                    energyPerTick,
                    oxygenAmount
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ArgonOxygenDecarburizationConverterRecipe recipe) {
            recipe.primaryInput.toNetwork(buffer);
            recipe.secondaryInput.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
            buffer.writeVarInt(recipe.oxygenAmount);
        }
    }
}
