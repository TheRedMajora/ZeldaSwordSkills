/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.entity.mobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.IEntityBackslice;
import zeldaswordskills.api.entity.IEntityEvil;
import zeldaswordskills.api.entity.IEntityLootable;
import zeldaswordskills.api.entity.IParryModifier;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.IEntityVariant;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.ai.EntityAIPowerAttack;
import zeldaswordskills.entity.ai.IPowerAttacker;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityDarknut extends EntityMob implements IEntityBackslice, IEntityEvil, IEntityLootable, IParryModifier, IPowerAttacker, IEntityVariant
{
	/**
	 * Returns array of default biomes in which this entity may spawn naturally
	 */
	public static String[] getDefaultBiomes() {
		List<String> biomes = new ArrayList<String>();
		biomes.addAll(Arrays.asList(BiomeType.ARID.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.BEACH.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.FIERY.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.MOUNTAIN.defaultBiomes));
		biomes.addAll(Arrays.asList(BiomeType.PLAINS.defaultBiomes));
		return biomes.toArray(new String[biomes.size()]);
	}

	/** Bonus to knockback resistance when wearing armor */
	private static final UUID armorKnockbackModifierUUID = UUID.fromString("71AF0F88-82E5-49DE-B9CC-844048E33D69");
	private static final AttributeModifier armorKnockbackModifier = (new AttributeModifier(armorKnockbackModifierUUID, "Armor Knockback Resistance", 1.0D, 0)).setSaved(false);

	/** Movement bonus when not wearing armor */
	private static final UUID armorMoveBonusUUID = UUID.fromString("B6C8CCB6-AE7B-4F14-908A-2F41BDB4D720");
	private static final AttributeModifier armorMoveBonus = (new AttributeModifier(armorMoveBonusUUID, "Armor Movement Bonus", 0.35D, 1)).setSaved(false);

	/** Power Attack AI only used while wearing armor */
	private final EntityAIPowerAttack powerAttackAI;

	/** Attack flag for model animations, updated via health update */
	private static final byte ATTACK_FLAG = 0x5;

	/** Flag for model to animate power attack charging up, updated via health update */
	private static final byte POWER_UP_FLAG = 0x6;

	/** Flag for model to animate power attack swing, updated via health update */
	private static final byte POWER_ATTACK_FLAG = 0x7;

	/** Flag for model to animate parry motion, updated via health update */
	private static final byte PARRY_FLAG = 0x8;

	/** Flag for model to animate spin attack motion, updated via health update */
	private static final byte SPIN_FLAG = 0x9;

	/** DataWatcher for Darknut type: 0 - normal, 1 - Mighty */
	private final static int TYPE_INDEX = 16;

	/** DataWatcher for armor health */
	private final static int ARMOR_INDEX = 17;

	/** DataWatcher for cape */
	private final static int CAPE_INDEX = 18;

	/** Timer for attack animation; negative swings one way, positive the other */
	@SideOnly(Side.CLIENT)
	public int attackTimer;

	/** Timer for power attack charging animation */
	@SideOnly(Side.CLIENT)
	public int chargeTimer;

	/** Flag set when performing a power attack swing, so can use same attack timer */
	@SideOnly(Side.CLIENT)
	public boolean isPowerAttack;

	/** Highest parry chance when this timer is zero; set to 10 after each parry */
	public int parryTimer;

	/** Recently hit timer */
	private int recentHitTimer;

	/** Number of successive hits that fell within recent hit time */
	private int recentHits;

	/** Timer for spin attack */
	private int spinAttackTimer;

	/** List of spin attack targets */
	private List<EntityLivingBase> targets;

	public EntityDarknut(World world) {
		super(world);
		powerAttackAI = getNewPowerAttackAI();
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
		tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 1.0D));
		tasks.addTask(5, new EntityAIWander(this, 1.0D));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(6, new EntityAILookIdle(this));
		targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
		setSize(1.0F, 3.0F);
		experienceValue = 12;
	}

	protected EntityAIPowerAttack getNewPowerAttackAI() {
		return new EntityAIPowerAttack(this, 4.0D);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(TYPE_INDEX, (byte) 0);
		dataWatcher.addObject(ARMOR_INDEX, 20.0F);
		dataWatcher.addObject(CAPE_INDEX, (byte) 0);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50.0D);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5.0D); // unarmed
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.225D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.25D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
	}

	/**
	 * Returns the Darknut's type: 0 - normal, 1 - Mighty
	 */
	public int getType() {
		return (int) dataWatcher.getWatchableObjectByte(TYPE_INDEX);
	}

	/**
	 * Sets the Darknut's type: 0 - normal, 1 - Mighty
	 */
	@Override
	public void setType(int type) {
		dataWatcher.updateObject(TYPE_INDEX, (byte) type);
		setWearingCape((type > 0 ? (byte) 60 : (byte) 0));
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue((type > 0 ? 100.0D : 50.0D));
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue((type > 0 ? 7.0D : 5.0D));
		setHealth(getMaxHealth());
		experienceValue = (type > 0 ? 20 : 12);
	}

	protected float getArmorDamage() {
		return dataWatcher.getWatchableObjectFloat(ARMOR_INDEX);
	}

	protected void setArmorDamage(float value) {
		dataWatcher.updateObject(ARMOR_INDEX, value);
	}

	/**
	 * Returns true if the Darknut's original armor is still intact
	 */
	public boolean isArmored() {
		return getArmorDamage() > 0;
	}

	public boolean isSpinning() {
		return spinAttackTimer > 0;
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}

	@Override
	protected String getLivingSound() {
		return Sounds.DARKNUT_LIVING;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.DARKNUT_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.DARKNUT_DIE;
	}

	@Override
	public void addRandomArmor() {
		// don't use super.addRandomArmor, as Darknuts always have certain equipment
		setCurrentItemOrArmor(0, new ItemStack(ZSSItems.swordDarknut));
		setCurrentItemOrArmor(ArmorIndex.EQUIPPED_CHEST, new ItemStack(Items.iron_chestplate));
		applyArmorAttributeModifiers(true);
	}

	/**
	 * Adds or removes knockback resistance and movement attribute modifiers for wearing armor
	 * Also adds / removes specific AI tasks
	 */
	private void applyArmorAttributeModifiers(boolean wearingArmor) {
		IAttributeInstance moveAttribute = getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		moveAttribute.removeModifier(armorMoveBonus);
		IAttributeInstance knockbackAttribute = getEntityAttribute(SharedMonsterAttributes.knockbackResistance);
		knockbackAttribute.removeModifier(armorKnockbackModifier);
		tasks.removeTask(powerAttackAI);
		if (wearingArmor) {
			tasks.addTask(1, powerAttackAI);
			knockbackAttribute.applyModifier(armorKnockbackModifier);
		} else {
			moveAttribute.applyModifier(armorMoveBonus);
		}
	}

	@Override
	public int getTotalArmorValue() {
		return Math.min(20, super.getTotalArmorValue() + (2 * worldObj.difficultySetting.getDifficultyId()));
	}

	public boolean isWearingCape() {
		return (dataWatcher.getWatchableObjectByte(CAPE_INDEX) > (byte) 0);
	}

	/**
	 * Grants the Darknut a cape with the given amount for health (i.e. ticks of fire damage required to burn through it)
	 */
	protected void setWearingCape(byte ticksRequired) {
		dataWatcher.updateObject(CAPE_INDEX, ticksRequired);
	}

	/**
	 * Damages the cape by 1 point
	 */
	private void damageCape() {
		byte b = dataWatcher.getWatchableObjectByte(CAPE_INDEX);
		if (b > 0) {
			setWearingCape((byte)(b - 1));
			if (b == (byte) 1) {
				extinguish();
			}
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isEntityInvulnerable() || isSpinning()) {
			return false;
		} else if (source.getEntity() == null || source.isUnblockable() || source.isMagicDamage()) {
			return super.attackEntityFrom(source, amount);
		} else if (isWearingCape()) {
			if (source.isFireDamage()) {
				setFire(3);
			}
			return false;
		} else if (source.isExplosion() && isArmored()) {
			amount = damageDarknutArmor(amount * (1.25F - (0.25F * (float) worldObj.difficultySetting.getDifficultyId())));
			if (amount < 0.5F) {
				return false;
			} // otherwise, allow extra damage to bleed through
		} else if (!worldObj.isRemote) {
			if (isArmored()) {
				if (TargetUtils.isTargetInFrontOf(this, source.getEntity(), 120)) {
					WorldUtils.playSoundAtEntity(this, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
					return false;
				} else {
					// Allow attack to go through for BackSlice, otherwise it will never trigger
					if (source.getEntity() instanceof EntityPlayer && ZSSPlayerSkills.get((EntityPlayer) source.getEntity()).isSkillActive(SkillBase.backSlice)) {
						return super.attackEntityFrom(source, amount);
					} else if (amount > ((float) worldObj.difficultySetting.getDifficultyId() * 2.0F)) {
						WorldUtils.playSoundAtEntity(this, Sounds.ARMOR_BREAK, 0.4F, 0.5F);
						damageDarknutArmor(amount * 0.5F);
					}
					return false;
				}
			} else if (parryAttack(source.getEntity())) {
				return false;
			} else {
				if (recentHitTimer > 0) {
					if (++recentHits > 3 && rand.nextFloat() < 0.15F * ((float) recentHits)) {
						playLivingSound();
						worldObj.playSoundAtEntity(this, Sounds.SPIN_ATTACK, 1.0F, (rand.nextFloat() * 0.4F) + 0.5F);
						worldObj.setEntityState(this, SPIN_FLAG);
						spinAttackTimer = 12;
						recentHits = 0;
						targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(4.0D, 0.0D, 4.0D));
						if (targets.contains(this)) {
							targets.remove(this);
						}
					}
				} else {
					recentHits = 1;
				}
				recentHitTimer = 60;
			}
		}
		return super.attackEntityFrom(source, amount);
	}

	/**
	 * Returns true if the Darknut was able to parry the entity's attack, and
	 * may also disarm the attacker
	 */
	protected boolean parryAttack(Entity entity) {
		if (TargetUtils.isTargetInFrontOf(this, entity, 90) && rand.nextFloat() < (0.5F - (parryTimer * 0.05F))) {
			worldObj.setEntityState(this, PARRY_FLAG);
			parryTimer = 10;
			super.swingItem();
			attackTime = Math.max(attackTime, 5); // don't allow attacks until parry animation finishes
			if (entity instanceof EntityLivingBase) {
				EntityLivingBase attacker = (EntityLivingBase) entity;
				if (attacker.getEquipmentInSlot(0) != null) {
					WorldUtils.playSoundAtEntity(this, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
					float disarmChance = Parry.getDisarmModifier(this, attacker);
					if (rand.nextFloat() < disarmChance) {
						WorldUtils.dropHeldItem(attacker);
					}
				}
				TargetUtils.knockTargetBack(attacker, this);
			}
			return true;
		}
		return false;
	}

	@Override
	public float getOffensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return -(0.1F * (float) worldObj.difficultySetting.getDifficultyId());
	}

	@Override
	public float getDefensiveModifier(EntityLivingBase entity, ItemStack stack) {
		return (0.1F * (float) worldObj.difficultySetting.getDifficultyId());
	}

	/**
	 * Applies the amount of damage to the darknut's armor, returning any remaining
	 */
	protected float damageDarknutArmor(float amount) {
		float armorDamage = getArmorDamage();
		float f = (amount - armorDamage);
		armorDamage -= amount;
		if (armorDamage < 0.1F) {
			armorDamage = 0.0F;
			onArmorDestroyed();
		}
		setArmorDamage(armorDamage);
		return (f > 0F ? f : 0F);
	}

	/**
	 * Handle consequences of armor destroyed, e.g. setting current chest piece to null
	 */
	protected void onArmorDestroyed() {
		if (getType() > 0) {
			setCurrentItemOrArmor(ArmorIndex.EQUIPPED_CHEST, new ItemStack(Items.chainmail_chestplate));
		} else {
			setCurrentItemOrArmor(ArmorIndex.EQUIPPED_CHEST, null);
		}
		applyArmorAttributeModifiers(false);
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (attackTime <= 0) {
			attackTime = 20;
			worldObj.setEntityState(this, ATTACK_FLAG);
			if (TargetUtils.isTargetInFrontOf(this, entity, 60) && attackEntity(entity, ATTACK_FLAG)) {
				WorldUtils.playSoundAtEntity(this, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
				return true;
			} else {
				WorldUtils.playSoundAtEntity(this, Sounds.SWORD_MISS, 0.4F, 0.5F);
			}
		}
		return false;
	}

	/**
	 * Actually attacks the entity, performing all the attack and damage calculations
	 * @param flag	attack flag, e.g. POWER_ATTACK_FLAG
	 * @return		true if attack was successful (i.e. attackEntityFrom returned true
	 */
	protected boolean attackEntity(Entity entity, int flag) {
		float damage = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		int k = (flag == POWER_ATTACK_FLAG || flag == SPIN_FLAG ? 1 : 0); // knockback
		if (entity instanceof EntityLivingBase) {
			damage += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) entity);
			k += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) entity);
		}
		if (entity.attackEntityFrom(getDamageSource(flag), damage)) {
			if (k > 0) {
				entity.addVelocity((double)(-MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * (float) k * 0.5F), 0.1D, (double)(MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * (float) k * 0.5F));
				motionX *= 0.6D;
				motionZ *= 0.6D;
			}
			int j = EnchantmentHelper.getFireAspectModifier(this);
			if (j > 0) {
				entity.setFire(j * 4);
			}
			if (entity instanceof EntityLivingBase) {
				EnchantmentHelper.func_151384_a((EntityLivingBase) entity, this);
			}
			EnchantmentHelper.func_151385_b(this, entity);
			return true;
		}
		return false;
	}

	@Override
	public boolean isLightArrowFatal() {
		return true;
	}

	@Override
	public float getLightArrowDamage(float amount) {
		return 0;
	}

	/**
	 * Returns appropriate damage source based on attack flag
	 * @param flag	Same flag as passed to {@link #onMeleeImpact}
	 */
	protected DamageSource getDamageSource(int flag) {
		switch(flag) {
		case POWER_ATTACK_FLAG: return DamageSource.causeMobDamage(this).setDamageBypassesArmor();
		default: return DamageSource.causeMobDamage(this);
		}
	}

	@Override
	public boolean allowDamageMultiplier(EntityPlayer player) {
		return getArmorDamage() < 0.1F;
	}

	@Override
	public boolean allowDisarmorment(EntityPlayer player, float damage) {
		return getArmorDamage() < 0.1F;
	}

	@Override
	public float onBackSliced(EntityPlayer attacker, int level, float damage) {
		if (isArmored()) {
			damageDarknutArmor(getDamageArmorAmount());
			if (!isArmored()) {
				attacker.triggerAchievement(ZSSAchievements.orcaCanOpener);
			}
			return 0.0F;
		}
		return damage;
	}

	/**
	 * Amount to damage Darknut's armor when backsliced
	 */
	protected float getDamageArmorAmount() {
		return getArmorDamage();
	}

	@Override
	public void beginPowerAttack() {
		attackTime = getChargeTime(); // prevent regular attacks from occurring while charging up
		worldObj.setEntityState(this, POWER_UP_FLAG);
	}

	@Override
	public void cancelPowerAttack() {
		worldObj.setEntityState(this, POWER_UP_FLAG);
	}

	/**
	 * 3 extra ticks included for animation of raising arms up to position
	 */
	@Override
	public int getChargeTime() {
		return 28 - (worldObj.difficultySetting.getDifficultyId() * 5);
	}

	@Override
	public void performPowerAttack(EntityLivingBase target) {
		worldObj.setEntityState(this, POWER_ATTACK_FLAG);
		attackTime = 20;
		if (TargetUtils.isTargetInFrontOf(this, target, 60.0F) && attackEntity(target, POWER_ATTACK_FLAG)) {
			WorldUtils.playSoundAtEntity(this, Sounds.ARMOR_BREAK, 0.4F, 0.5F);
		} else {
			onAttackMissed();
		}
	}

	@Override
	public void onAttackMissed() {
		WorldUtils.playSoundAtEntity(this, Sounds.SWORD_MISS, 0.4F, 0.5F);
	}

	@Override
	public void swingItem() {} // don't allow item to swing as normal or it screws up the attack animations

	@Override
	protected void updateAITasks() {
		if (spinAttackTimer > 0) {
			++entityAge;
		} else {
			super.updateAITasks();
		}
	}

	@Override
	public void onLivingUpdate() {
		if (isWearingCape() && isBurning()) {
			damageCape();
		}
		super.onLivingUpdate();
		if (parryTimer > 0) {
			--parryTimer;
		}
		if (recentHitTimer > 0) {
			if (--recentHitTimer == 0) {
				recentHits = 0;
			}
		}
		if (spinAttackTimer > 0) {
			--spinAttackTimer;
			if (isEntityAlive()) {
				rotationYaw += 30.0F;
				while (rotationYaw > 360.0F) { rotationYaw -= 360.0F; }
				while (rotationYaw < -360.0F) { rotationYaw += 360.0F; }
				if (!worldObj.isRemote) {
					List<EntityLivingBase> list = TargetUtils.acquireAllLookTargets(this, 5, 1.0D);
					for (EntityLivingBase target : list) {
						if (targets != null && targets.contains(target)) {
							attackEntity(target, SPIN_FLAG);
							targets.remove(target);
						}
					}
				}
			}
		}
		if (worldObj.isRemote) {
			if (attackTimer > 0) {
				--attackTimer;
			} else if (attackTimer < 0) {
				++attackTimer;
			}
			if (chargeTimer > 0) {
				--chargeTimer;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte flag) {
		switch(flag) {
		case ATTACK_FLAG:
			isPowerAttack = false;
			if (attackTimer == 0) {
				attackTimer = (rand.nextFloat() < 0.5F ? 10 : -10);
			}
			break;
		case PARRY_FLAG:
			parryTimer = 10;
			super.swingItem();
			break;
		case POWER_UP_FLAG:
			// cancel or begin charge; 3 ticks up already included in charge time, 3 ticks down
			chargeTimer = (chargeTimer > 0 ? 0 : getChargeTime());
			break;
		case POWER_ATTACK_FLAG:
			isPowerAttack = true;
			attackTimer = 7;
			chargeTimer = 0;
			break;
		case SPIN_FLAG:
			spinAttackTimer = 12;
			break;
		default:
			super.handleHealthUpdate(flag);
		}
	}

	@Override
	public void setCurrentItemOrArmor(int slot, ItemStack stack) {
		super.setCurrentItemOrArmor(slot, stack);
		if (!worldObj.isRemote && slot == ArmorIndex.EQUIPPED_CHEST && stack == null && getArmorDamage() > 0) {
			applyArmorAttributeModifiers(false);
			setArmorDamage(0F);
		}
	}

	@Override
	protected void dropFewItems(boolean recentlyHit, int lootingLevel) {
		int n = rand.nextInt(Math.max(2, 3 + lootingLevel));
		switch(n) {
		case 0:	entityDropItem(new ItemStack(Items.flint), 0.0F); break;
		case 1: entityDropItem(new ItemStack(Items.coal), 0.0F); break;
		default: entityDropItem(new ItemStack(Items.iron_ingot), 0.0F); break;
		}
	}

	@Override
	protected void dropRareDrop(int rarity) {
		switch(rarity) {
		case 1: entityDropItem(new ItemStack(ZSSItems.treasure,1,Treasures.KNIGHTS_CREST.ordinal()), 0.0F); break;
		default: entityDropItem(new ItemStack(Items.painting), 0.0F);
		}
	}

	@Override
	public float getLootableChance(EntityPlayer player, WhipType whip) {
		return (getEquipmentInSlot(ArmorIndex.EQUIPPED_CHEST) == null ? 0.25F : 0.0F);
	}

	@Override
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip) {
		return new ItemStack(ZSSItems.treasure,1,Treasures.KNIGHTS_CREST.ordinal());
	}

	@Override
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen) {
		if (wasItemStolen) {
			player.triggerAchievement(ZSSAchievements.orcaDeknighted);
		}
		return wasItemStolen;
	}

	@Override
	public boolean isHurtOnTheft(EntityPlayer player, WhipType whip) {
		return false;
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
		data = super.onSpawnWithEgg(data);
		addRandomArmor();
		if (rand.nextFloat() < (0.05F * (float) worldObj.difficultySetting.getDifficultyId())) {
			setType(1);
		}
		return data;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setFloat("ArmorHealth", getArmorDamage());
		compound.setByte("DarknutType", dataWatcher.getWatchableObjectByte(TYPE_INDEX));
		compound.setByte("CapeHealth", dataWatcher.getWatchableObjectByte(CAPE_INDEX));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setArmorDamage(compound.getFloat("ArmorHealth"));
		applyArmorAttributeModifiers(isArmored());
		dataWatcher.updateObject(TYPE_INDEX, compound.getByte("DarknutType"));
		dataWatcher.updateObject(CAPE_INDEX, compound.getByte("CapeHealth"));
	}
}