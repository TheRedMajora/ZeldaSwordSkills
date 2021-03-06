/**
    Copyright (C) <2017> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.entity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.LootableEntityRegistry;
import zeldaswordskills.client.model.ModelDarknut;
import zeldaswordskills.client.model.ModelDekuBaba;
import zeldaswordskills.client.model.ModelDekuFire;
import zeldaswordskills.client.model.ModelDekuWithered;
import zeldaswordskills.client.model.ModelGoron;
import zeldaswordskills.client.model.ModelMaskSalesman;
import zeldaswordskills.client.model.ModelOctorok;
import zeldaswordskills.client.model.ModelWizzrobe;
import zeldaswordskills.client.render.RenderNothing;
import zeldaswordskills.client.render.entity.RenderCustomArrow;
import zeldaswordskills.client.render.entity.RenderDekuBaba;
import zeldaswordskills.client.render.entity.RenderEntityBomb;
import zeldaswordskills.client.render.entity.RenderEntityBoomerang;
import zeldaswordskills.client.render.entity.RenderEntityChu;
import zeldaswordskills.client.render.entity.RenderEntityFairy;
import zeldaswordskills.client.render.entity.RenderEntityHookShot;
import zeldaswordskills.client.render.entity.RenderEntityJar;
import zeldaswordskills.client.render.entity.RenderEntityKeese;
import zeldaswordskills.client.render.entity.RenderEntityMagicSpell;
import zeldaswordskills.client.render.entity.RenderEntityOctorok;
import zeldaswordskills.client.render.entity.RenderEntitySkulltula;
import zeldaswordskills.client.render.entity.RenderEntitySwordBeam;
import zeldaswordskills.client.render.entity.RenderEntityWhip;
import zeldaswordskills.client.render.entity.RenderEntityWizzrobe;
import zeldaswordskills.client.render.entity.RenderGenericLiving;
import zeldaswordskills.client.render.entity.RenderSnowballFactory;
import zeldaswordskills.entity.mobs.EntityBlackKnight;
import zeldaswordskills.entity.mobs.EntityChu;
import zeldaswordskills.entity.mobs.EntityDarknut;
import zeldaswordskills.entity.mobs.EntityDekuBaba;
import zeldaswordskills.entity.mobs.EntityDekuFire;
import zeldaswordskills.entity.mobs.EntityDekuWithered;
import zeldaswordskills.entity.mobs.EntityGrandWizzrobe;
import zeldaswordskills.entity.mobs.EntityKeese;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.entity.mobs.EntitySkulltula;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.entity.npc.EntityNpcOrca;
import zeldaswordskills.entity.npc.EntityNpcZelda;
import zeldaswordskills.entity.passive.EntityFairy;
import zeldaswordskills.entity.passive.EntityNavi;
import zeldaswordskills.entity.projectile.EntityArrowBomb;
import zeldaswordskills.entity.projectile.EntityArrowCustom;
import zeldaswordskills.entity.projectile.EntityArrowElemental;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.entity.projectile.EntityBombosFireball;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityCeramicJar;
import zeldaswordskills.entity.projectile.EntityCyclone;
import zeldaswordskills.entity.projectile.EntityHookShot;
import zeldaswordskills.entity.projectile.EntityLeapingBlow;
import zeldaswordskills.entity.projectile.EntityMagicSpell;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySwordBeam;
import zeldaswordskills.entity.projectile.EntityThrowingRock;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.LibPotionID;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.SpawnableEntityData;

public class ZSSEntities
{
	/** Spawn rates */
	private static int spawnGoron;

	public static int getGoronRatio() { return spawnGoron; }

	/** Array of default biomes each mob is allowed to spawn in */
	private static final Map<Class<? extends EntityLiving>, String[]> defaultSpawnLists = new HashMap<Class<? extends EntityLiving>, String[]>();

	/** Map of SpawnableEntityData for each entity class that can spawn naturally */
	private static final Map<Class<? extends EntityLiving>, SpawnableEntityData> spawnableEntityData = new HashMap<Class<? extends EntityLiving>, SpawnableEntityData>();

	/**
	 * Initializes entity spawn rates, spawn locations, and adds spawns.
	 */
	public static void postInit(Configuration config) {
		// REGISTER ENTITY SPAWN DATA
		String category = "mob spawns";
		int rate = config.getInt("[Spawn Rate] Chuchu Spawn Rate", category, 10, 0, 10000, "The spawn pool weight for Chuchu (0 to disable)", "config.zss.mob_spawns.chuchu.rate");
		addSpawnableEntityData(EntityChu.class, EnumCreatureType.MONSTER, 4, 4, rate);
		rate = config.getInt("[Spawn Rate] Darknut Spawn Rate", category, 5, 0, 10000, "The spawn pool weight for Darknut (0 to disable)", "config.zss.mob_spawns.darknut.rate");
		addSpawnableEntityData(EntityDarknut.class, EnumCreatureType.MONSTER, 1, 1, rate);
		rate = config.getInt("[Spawn Rate] Deku Baba Spawn Rate", category, 10, 0 , 10000, "The spawn pool weight for Deku Baba (0 to disable)", "config.zss.mob_spawns.dekubaba.rate");
		addSpawnableEntityData(EntityDekuBaba.class, EnumCreatureType.MONSTER, 1, 3, rate);
		rate = config.getInt("[Spawn Rate] Deku Baba (Fire) Spawn Rate", category, 2, 0, 10000, "The spawn pool weight for Deku Baba (Fire) (0 to disable)", "config.zss.mob_spawns.dekufire.rate");
		addSpawnableEntityData(EntityDekuFire.class, EnumCreatureType.MONSTER, 1, 3, rate);
		rate = config.getInt("[Spawn Rate] Deku Baba (Withered) Spawn Rate", category, 5, 0, 10000, "The spawn pool weight for Deku Baba (Withered) (0 to disable)", "config.zss.mob_spawns.dekuwithered.rate");
		addSpawnableEntityData(EntityDekuWithered.class, EnumCreatureType.MONSTER, 1, 3, rate);
		rate = config.getInt("[Spawn Rate] Fairy (wild) Spawn Rate", category, 1, 0, 10000, "The spawn pool weight for Fairy (wild) (0 to disable)", "config.zss.mob_spawns.fairy.rate");
		addSpawnableEntityData(EntityFairy.class, EnumCreatureType.AMBIENT, 1, 3, rate);
		// Gorons are an exception, as they are not spawned using vanilla mechanics
		spawnGoron = config.getInt("[Spawn Rate] Goron Spawn Rate", category, 4, 0, 1000, "Goron spawn rate, as a ratio of regular villagers to Gorons (a Goron per how many villagers) (0 to disable)", "config.zss.mob_spawns.goron.rate");
		rate = config.getInt("[Spawn Rate] Keese Spawn Rate", category, 1, 0, 10000, "The spawn pool weight for Keese (0 to disable)", "config.zss.mob_spawns.keese.rate");
		addSpawnableEntityData(EntityKeese.class, EnumCreatureType.AMBIENT, 4, 4, rate); // TODO should use monster type???
		rate = config.getInt("[Spawn Rate] Octorok Spawn Rate", category, 8, 0, 10000, "The spawn pool weight for Octorok (0 to disable)", "config.zss.mob_spawns.octorok.rate");
		addSpawnableEntityData(EntityOctorok.class, EnumCreatureType.WATER_CREATURE, 2, 4, rate);
		rate = config.getInt("[Spawn Rate] Skulltula Spawn Rate", category, 8, 0, 10000, "The spawn pool weight for Skulltula (0 to disable)", "config.zss.mob_spawns.skulltula.rate");
		addSpawnableEntityData(EntitySkulltula.class, EnumCreatureType.MONSTER, 2, 4, rate);
		rate = config.getInt("[Spawn Rate] Wizzrobe Spawn Rate", category, 10, 0, 10000, "The spawn pool weight for Wizzrobe (0 to disable)", "config.zss.mob_spawns.wizzrobe.rate");
		addSpawnableEntityData(EntityWizzrobe.class, EnumCreatureType.MONSTER, 1, 1, rate);
		// ALLOWED BIOMES
		for (Class<? extends EntityLiving> entity : defaultSpawnLists.keySet()) {
			String[] defaultBiomes = defaultSpawnLists.get(entity);
			SpawnableEntityData spawnData = spawnableEntityData.get(entity);
			if (defaultBiomes != null && spawnData != null && spawnData.spawnRate > 0) {
				
				//The name of each entity, pulled from the class name
				String name = entity.getName().substring(entity.getName().lastIndexOf(".") + 7);
				//The reference key for each Property to be created
				String propKey = "[Spawn Biomes] " + name + " Biomes";
				//The comment for each Property
				String comment = String.format("List of biomes in which %s are allowed to spawn", name);
				//The language key for each Property
				String langKey = "config.zss.mob_spawns." + name.toLowerCase() + ".biomes";
				
				String[] biomes = config.getStringList(propKey, category, defaultBiomes, comment, (String[]) null, langKey);
				if (biomes != null) {
					addSpawns(entity, biomes, spawnData);
				}
			}
		}

		// VANILLA LOOTABLE ENTITIES
		float f = Config.getVanillaWhipLootChance();
		if (f > 0) {
			// won't override entries if added elsewhere first, which gives other mods a chance to register special loot for vanilla mobs
			LootableEntityRegistry.addLootableEntity(EntityBlaze.class, f, new ItemStack(Items.blaze_rod));
			LootableEntityRegistry.addLootableEntity(EntityCaveSpider.class, f, new ItemStack(Items.spider_eye), new ItemStack(Items.string));
			LootableEntityRegistry.addLootableEntity(EntityCreeper.class, f, new ItemStack(Items.gunpowder));
			LootableEntityRegistry.addLootableEntity(EntityEnderman.class, f, new ItemStack(Items.ender_pearl));
			LootableEntityRegistry.addLootableEntity(EntityGhast.class, f, new ItemStack(Items.ghast_tear), new ItemStack(Items.gunpowder), new ItemStack(Items.gunpowder));
			LootableEntityRegistry.addLootableEntity(EntityGuardian.class, f, new ItemStack(Items.prismarine_crystals), new ItemStack(Blocks.sponge));
			LootableEntityRegistry.addLootableEntity(EntityIronGolem.class, f, new ItemStack(Items.iron_ingot));
			LootableEntityRegistry.addLootableEntity(EntityMagmaCube.class, f, new ItemStack(Items.magma_cream));
			LootableEntityRegistry.addLootableEntity(EntityPigZombie.class, f, new ItemStack(Items.gold_nugget), new ItemStack(Items.gold_nugget), new ItemStack(Items.gold_ingot));
			LootableEntityRegistry.addLootableEntity(EntitySkeleton.class, f, new ItemStack(Items.arrow), new ItemStack(Items.bone), new ItemStack(Items.flint));
			LootableEntityRegistry.addLootableEntity(EntitySlime.class, f, new ItemStack(Items.slime_ball));
			LootableEntityRegistry.addLootableEntity(EntitySnowman.class, f, new ItemStack(Items.snowball));
			LootableEntityRegistry.addLootableEntity(EntitySpider.class, f, new ItemStack(Items.spider_eye), new ItemStack(Items.string));
			LootableEntityRegistry.addLootableEntity(EntityWitch.class, f, new ItemStack(Items.potionitem,1,LibPotionID.HEALING.id), new ItemStack(Items.potionitem,1,LibPotionID.SWIFTNESS.id), new ItemStack(Items.potionitem,1,LibPotionID.FIRERESIST.id), new ItemStack(Items.potionitem,1,LibPotionID.WATER_BREATHING.id));
			LootableEntityRegistry.addLootableEntity(EntityZombie.class, f, new ItemStack(Items.iron_ingot), new ItemStack(Items.carrot), new ItemStack(Items.potato));
		}
	}

	private static void addSpawns(Class<? extends EntityLiving> entity, String[] biomes, SpawnableEntityData spawnData) {
		for (String name : biomes) {
			BiomeGenBase biome = getBiomeByName(name);
			if (biome != null) {
				EntityRegistry.addSpawn(entity, spawnData.spawnRate, spawnData.min, spawnData.max, spawnData.creatureType, biome);
			} else {
				ZSSMain.logger.warn(String.format("Unable to find matching biome for %s while adding spawns for %s!", name, entity.getName().substring(entity.getName().lastIndexOf(".") + 1)));
			}
		}
	}

	/**
	 * Retrieves the BiomeGenBase associated with the string given, or null if it was not found
	 */
	private static BiomeGenBase getBiomeByName(String name) {
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null && biome.biomeName != null && biome.biomeName.toLowerCase().replace(" ", "").equals(name.toLowerCase().replace(" ", ""))) {
				return biome;
			}
		}
		return null;
	}

	/**
	 * Registers all entities, entity eggs, and populates default spawn biome lists
	 */
	public static void preInit() {
		registerEntities();
		addSpawnLocations(EntityChu.class, EntityChu.getDefaultBiomes());
		addSpawnLocations(EntityDarknut.class, EntityDarknut.getDefaultBiomes());
		addSpawnLocations(EntityDekuBaba.class, EntityDekuBaba.getDefaultBiomes());
		addSpawnLocations(EntityDekuFire.class, EntityDekuFire.getDefaultBiomes());
		addSpawnLocations(EntityDekuWithered.class, EntityDekuWithered.getDefaultBiomes());
		addSpawnLocations(EntityFairy.class, BiomeType.RIVER.defaultBiomes);
		addSpawnLocations(EntityKeese.class, EntityKeese.getDefaultBiomes());
		addSpawnLocations(EntityOctorok.class, EntityOctorok.getDefaultBiomes());
		addSpawnLocations(EntitySkulltula.class, EntitySkulltula.getDefaultBiomes());
		addSpawnLocations(EntityWizzrobe.class, EntityWizzrobe.getDefaultBiomes());
	}

	private static void registerEntities() {
		int modEntityIndex = 0;
		// PROJECTILES
		EntityRegistry.registerModEntity(EntityArrowBomb.class, "arrow_bomb", ++modEntityIndex, ZSSMain.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityArrowCustom.class, "arrow_custom", ++modEntityIndex, ZSSMain.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityArrowElemental.class, "arrow_elemental", ++modEntityIndex, ZSSMain.instance, 64, 20, true);
		EntityRegistry.registerModEntity(EntityBomb.class, "bomb", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityBombosFireball.class, "bombos_fireball", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityBoomerang.class, "boomerang", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityCeramicJar.class, "jar", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityCyclone.class, "cyclone", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityHookShot.class, "hookshot", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityLeapingBlow.class, "leaping_blow", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityMagicSpell.class, "magic_spell", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntitySeedShot.class, "seedshot", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntitySwordBeam.class, "sword_beam", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityThrowingRock.class, "rock", ++modEntityIndex, ZSSMain.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityWhip.class, "whip", ++modEntityIndex, ZSSMain.instance, 64, 10, true);

		// NATURALLY SPAWNING MOBS
		EntityRegistry.registerModEntity(EntityDarknut.class, "darknut", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityDarknut.class, "darknut", 0x1E1E1E, 0x8B2500, 0x1E1E1E, 0xFB2500);
		EntitySpawnPlacementRegistry.setPlacementType(EntityDarknut.class, SpawnPlacementType.ON_GROUND);

		registerEntity(EntityFairy.class, "fairy", ++modEntityIndex, 80, 0xADFF2F, 0xFFFF00);
		EntitySpawnPlacementRegistry.setPlacementType(EntityFairy.class, SpawnPlacementType.ON_GROUND);
		EntityRegistry.registerModEntity(EntityNavi.class, "navi", ++modEntityIndex, ZSSMain.instance, 80, 3, true);

		EntityRegistry.registerModEntity(EntityChu.class, "chu", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityChu.class, "chu", 0x008000, 0xDC143C, 0x008000, 0x00EE00, 0x008000, 0x3A5FCD, 0x008000, 0xFFFF00);
		EntitySpawnPlacementRegistry.setPlacementType(EntityChu.class, SpawnPlacementType.ON_GROUND);

		registerEntity(EntityDekuBaba.class, "baba_deku", ++modEntityIndex, 80, 0x33CC33, 0x0000FF);
		registerEntity(EntityDekuFire.class, "baba_fire", ++modEntityIndex, 80, 0xFF0000, 0x0000FF);
		registerEntity(EntityDekuWithered.class, "baba_withered", ++modEntityIndex, 80, 0x8B5A00, 0x0000FF);

		EntityRegistry.registerModEntity(EntityKeese.class, "keese", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityKeese.class, "keese", 0x000000, 0x555555, 0x000000, 0xFF4500, 0x000000, 0x40E0D0, 0x000000, 0xFFD700, 0x000000, 0x800080);
		EntitySpawnPlacementRegistry.setPlacementType(EntityKeese.class, SpawnPlacementType.ON_GROUND);

		EntityRegistry.registerModEntity(EntityOctorok.class, "octorok", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityOctorok.class, "octorok", 0x68228B, 0xBA55D3, 0x68228B, 0xFF00FF);
		EntitySpawnPlacementRegistry.setPlacementType(EntityOctorok.class, SpawnPlacementType.IN_WATER);

		EntityRegistry.registerModEntity(EntitySkulltula.class, "skulltula", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntitySkulltula.class, "skulltula", 0x080808, 0xFFFF00, 0x080808, 0xE68A00);
		EntitySpawnPlacementRegistry.setPlacementType(EntitySkulltula.class, SpawnPlacementType.ON_GROUND);

		EntityRegistry.registerModEntity(EntityWizzrobe.class, "wizzrobe", ++modEntityIndex, ZSSMain.instance, 80, 3, true);
		CustomEntityList.addMapping(EntityWizzrobe.class, "wizzrobe", 0x8B2500, 0xFF0000, 0x8B2500, 0x00B2EE, 0x8B2500, 0xEEEE00, 0x8B2500, 0x00EE76);
		EntitySpawnPlacementRegistry.setPlacementType(EntityWizzrobe.class, SpawnPlacementType.ON_GROUND);

		// BOSSES
		registerEntity(EntityBlackKnight.class, "darknut_boss", ++modEntityIndex, 80, 0x1E1E1E, 0x000000);
		registerEntity(EntityGrandWizzrobe.class, "wizzrobe_grand", ++modEntityIndex, 80, 0x8B2500, 0x1E1E1E);

		// NPCS
		registerEntity(EntityGoron.class, "goron", ++modEntityIndex, 80, 0xB8860B, 0x8B5A00);
		EntitySpawnPlacementRegistry.setPlacementType(EntityGoron.class, SpawnPlacementType.ON_GROUND);
		registerEntity(EntityNpcBarnes.class, "npc.barnes", ++modEntityIndex, 80, 0x8B8378, 0xED9121);
		registerEntity(EntityNpcMaskTrader.class, "npc.mask_trader", ++modEntityIndex, 80, 0x0000EE, 0x00C957);
		registerEntity(EntityNpcOrca.class, "npc.orca", ++modEntityIndex, 80, 0x0000EE, 0x9A32CD);
		registerEntity(EntityNpcZelda.class, "npc.zelda", ++modEntityIndex, 80, 0xCC0099, 0xFFFFFF);
	}

	@SideOnly(Side.CLIENT) 
	public static void registerRenderers() {
		// PROJECTILES
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowCustom.class, new RenderCustomArrow.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityBomb.class, new RenderEntityBomb.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityBombosFireball.class, new RenderSnowballFactory(Items.fire_charge));
		RenderingRegistry.registerEntityRenderingHandler(EntityBoomerang.class, new RenderEntityBoomerang.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityCeramicJar.class, new RenderEntityJar.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityCyclone.class, new RenderNothing.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityHookShot.class, new RenderEntityHookShot.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityLeapingBlow.class, new RenderNothing.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicSpell.class, new RenderEntityMagicSpell.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntitySeedShot.class, new RenderSnowballFactory(ZSSItems.dekuNut));
		RenderingRegistry.registerEntityRenderingHandler(EntitySwordBeam.class, new RenderEntitySwordBeam.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityThrowingRock.class, new RenderSnowballFactory(ZSSItems.throwingRock));
		RenderingRegistry.registerEntityRenderingHandler(EntityWhip.class, new RenderEntityWhip.Factory());

		// NATURALLY SPAWNING MOBS
		RenderingRegistry.registerEntityRenderingHandler(EntityChu.class, new RenderEntityChu.Factory(new ModelSlime(16), 0.25F));
		RenderingRegistry.registerEntityRenderingHandler(EntityDarknut.class, new RenderGenericLiving.Factory( 
				new ModelDarknut(), 0.5F, 1.5F, ModInfo.ID + ":textures/entity/darknut_standard.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityDekuBaba.class, new RenderDekuBaba.Factory(
				new ModelDekuBaba(), 0.5F, 1.25F, ModInfo.ID + ":textures/entity/deku_baba.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityDekuFire.class, new RenderDekuBaba.Factory(
				new ModelDekuFire(), 0.5F, 1.25F, ModInfo.ID + ":textures/entity/deku_baba.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityDekuWithered.class, new RenderDekuBaba.Factory(
				new ModelDekuWithered(), 0.5F, 1.25F, ModInfo.ID + ":textures/entity/deku_withered.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, new RenderEntityFairy.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityGoron.class, new RenderGenericLiving.Factory( 
				new ModelGoron(), 0.5F, 1.5F, ModInfo.ID + ":textures/entity/goron.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityKeese.class, new RenderEntityKeese.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityNavi.class, new RenderEntityFairy.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityOctorok.class, new RenderEntityOctorok.Factory(new ModelOctorok(), 0.7F));
		RenderingRegistry.registerEntityRenderingHandler(EntitySkulltula.class, new RenderEntitySkulltula.Factory());
		RenderingRegistry.registerEntityRenderingHandler(EntityWizzrobe.class, new RenderEntityWizzrobe.Factory(new ModelWizzrobe(), 1.0F));

		// BOSSES
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackKnight.class, new RenderGenericLiving.Factory( 
				new ModelDarknut(), 0.5F, 1.8F, ModInfo.ID + ":textures/entity/darknut_standard.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityGrandWizzrobe.class, new RenderEntityWizzrobe.Factory(new ModelWizzrobe(), 1.5F));

		// NPCS
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcBarnes.class, new RenderGenericLiving.Factory( 
				new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_barnes.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcMaskTrader.class, new RenderGenericLiving.Factory( 
				new ModelMaskSalesman(), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_mask_salesman.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcOrca.class, new RenderGenericLiving.Factory( 
				new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_orca.png"));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcZelda.class, new RenderGenericLiving.Factory(
				new ModelBiped(0.0F, 0.0F, 64, 64), 0.5F, 1.0F, ModInfo.ID + ":textures/entity/npc_zelda.png"));
	}

	/**
	 * Registers a tracked entity with only one variety using the given colors for the spawn egg
	 */
	public static void registerEntity(Class<? extends EntityLiving> entityClass, String name, int modEntityIndex, int trackingRange, int primaryColor, int secondaryColor) {
		EntityRegistry.registerModEntity(entityClass, name, modEntityIndex, ZSSMain.instance, trackingRange, 3, true);
		CustomEntityList.addMapping(entityClass, name, primaryColor, secondaryColor);
	}

	/**
	 * Register an entity as a spawnable entity
	 */
	private static void addSpawnableEntityData(Class<? extends EntityLiving> entity, EnumCreatureType creatureType, int min, int max, int spawnRate) {
		if (spawnableEntityData.containsKey(entity)) {
			ZSSMain.logger.warn("Spawnable entity " + entity.getName().substring(entity.getName().lastIndexOf(".") + 1) + " has already been registered!");
		} else {
			spawnableEntityData.put(entity, new SpawnableEntityData(creatureType, min, max, spawnRate));
		}
	}

	/**
	 * Adds default biomes in which the entity is allowed to spawn, if any
	 */
	private static void addSpawnLocations(Class<? extends EntityLiving> entity, String... biomes) {
		if (biomes != null && biomes.length > 0) {
			if (defaultSpawnLists.containsKey(entity)) {
				ZSSMain.logger.warn(entity.getName().substring(entity.getName().lastIndexOf(".") + 1) + " already has an array of default spawn locations!");
			} else {
				defaultSpawnLists.put(entity, biomes);
			}
		}
	}
}
