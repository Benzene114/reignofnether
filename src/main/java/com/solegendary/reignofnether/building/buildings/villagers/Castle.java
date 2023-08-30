package com.solegendary.reignofnether.building.buildings.villagers;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.ability.abilities.PromoteIllager;
import com.solegendary.reignofnether.building.*;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.research.ResearchClient;
import com.solegendary.reignofnether.research.researchItems.*;
import com.solegendary.reignofnether.resources.ResourceCost;
import com.solegendary.reignofnether.resources.ResourceCosts;
import com.solegendary.reignofnether.unit.units.monsters.WardenUnitProd;
import com.solegendary.reignofnether.unit.units.villagers.RavagerUnitProd;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public class Castle extends ProductionBuilding implements GarrisonableBuilding {

    public final static String buildingName = "Castle";
    public final static String structureName = "castle";
    public final static ResourceCost cost = ResourceCosts.CASTLE;

    private final static int MAX_OCCUPANTS = 10;

    public Castle(Level level, BlockPos originPos, Rotation rotation, String ownerName) {
        super(level, originPos, rotation, ownerName, getAbsoluteBlockData(getRelativeBlockData(level), level, originPos, rotation), false);
        this.name = buildingName;
        this.ownerName = ownerName;
        this.portraitBlock = Blocks.COBBLESTONE;
        this.icon = new ResourceLocation("minecraft", "textures/block/cobblestone.png");

        this.foodCost = cost.food;
        this.woodCost = cost.wood;
        this.oreCost = cost.ore;
        this.popSupply = cost.population;
        this.buildTimeModifier = 0.6f;

        this.startingBlockTypes.add(Blocks.STONE_BRICKS);
        this.startingBlockTypes.add(Blocks.STONE_BRICK_WALL);
        this.startingBlockTypes.add(Blocks.SPRUCE_SLAB);
        this.startingBlockTypes.add(Blocks.SPRUCE_PLANKS);
        this.startingBlockTypes.add(Blocks.DARK_OAK_PLANKS);

        Ability promoteIllager = new PromoteIllager(this);
        this.abilities.add(promoteIllager);

        if (level.isClientSide()) {
            this.productionButtons = Arrays.asList(
                RavagerUnitProd.getStartButton(this, Keybindings.keyQ),
                ResearchCastleFlag.getStartButton(this, Keybindings.keyW)
            );
            this.abilityButtons.add(promoteIllager.getButton(Keybindings.keyW));
        }
    }

    public static ArrayList<BuildingBlock> getRelativeBlockData(LevelAccessor level) {
        return BuildingBlockData.getBuildingBlocks(structureName, level);
    }

    public static AbilityButton getBuildButton(Keybinding hotkey) {
        return new AbilityButton(
                Castle.buildingName,
                new ResourceLocation("minecraft", "textures/block/cobblestone.png"),
                hotkey,
                () -> BuildingClientEvents.getBuildingToPlace() == Castle.class,
                () -> false,
                () -> (BuildingClientEvents.hasFinishedBuilding(Barracks.buildingName) &&
                        BuildingClientEvents.hasFinishedBuilding(ArcaneTower.buildingName)) ||
                        ResearchClient.hasCheat("modifythephasevariance"),
                () -> BuildingClientEvents.setBuildingToPlace(Castle.class),
                null,
                List.of(
                        FormattedCharSequence.forward(Castle.buildingName, Style.EMPTY.withBold(true)),
                        ResourceCosts.getFormattedCost(cost),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("A grand castle that can produce ravagers ", Style.EMPTY),
                        FormattedCharSequence.forward("and garrison up to " + MAX_OCCUPANTS + " units.", Style.EMPTY),
                        FormattedCharSequence.forward("", Style.EMPTY),
                        FormattedCharSequence.forward("Requires an arcane tower and a barracks.", Style.EMPTY)
                ),
                null
        );
    }

    public void changeStructure(String newStructureName) {
        ArrayList<BuildingBlock> newBlocks = BuildingBlockData.getBuildingBlocks(newStructureName, this.getLevel());
        this.blocks = getAbsoluteBlockData(newBlocks, this.getLevel(), originPos, rotation);
        super.refreshBlocks();
    }

    // check that the flag is built based on existing placed blocks
    public boolean isUpgraded() {
        for (BuildingBlock block : blocks)
            if (block.getBlockState().getBlock() == Blocks.WHITE_WOOL ||
                    block.getBlockState().getBlock() == Blocks.RED_WOOL ||
                    block.getBlockState().getBlock() == Blocks.LIGHT_GRAY_WOOL)
                return true;
        return false;
    }

    @Override
    public BlockPos getIndoorSpawnPoint(ServerLevel level) {
        return this.originPos.offset(getExitPosition());
    }

    @Override
    public BlockPos getEntryPosition() {
        return new BlockPos(5,16,5);
    }

    @Override
    public BlockPos getExitPosition() {
        return new BlockPos(5,2,5);
    }

    @Override
    public boolean isFull() { return GarrisonableBuilding.getNumOccupants(this) >= MAX_OCCUPANTS; }
}
