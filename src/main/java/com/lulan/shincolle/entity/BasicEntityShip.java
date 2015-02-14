package com.lulan.shincolle.entity;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.ai.EntityAIShipSit;
import com.lulan.shincolle.client.inventory.ContainerShipInventory;
import com.lulan.shincolle.client.particle.EntityFXMiss;
import com.lulan.shincolle.client.particle.EntityFXSpray;
import com.lulan.shincolle.crafting.EquipCalc;
import com.lulan.shincolle.entity.EntityAbyssMissile;
import com.lulan.shincolle.handler.ConfigHandler;
import com.lulan.shincolle.init.ModBlocks;
import com.lulan.shincolle.init.ModItems;
import com.lulan.shincolle.network.CreatePacketS2C;
import com.lulan.shincolle.reference.AttrID;
import com.lulan.shincolle.reference.AttrValues;
import com.lulan.shincolle.reference.GUIs;
import com.lulan.shincolle.reference.Reference;
import com.lulan.shincolle.utility.LogHelper;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**SHIP DATA <br>
 * Explanation in crafting/ShipCalc.class
 */
public abstract class BasicEntityShip extends EntityTameable implements IEntityShip {

	protected ExtendShipProps ExtProps;	//entity�B�~NBT����
	
	//for attribute calc
	protected short ShipLevel;			//ship level
	protected int Kills;				//kill mobs
	protected int ExpCurrent;			//total number of attacks = experience
	protected int ExpNext;				//exp require for next level
	protected byte ShipType;			//ship type
	protected byte ShipID;
	//for AI calc
	protected int StartEmotion;			//�����}�l�ɶ�
	protected int NumAmmoLight;			//�u�Ħs�q
	protected int NumAmmoHeavy;			//�����u�Ħs�q
	protected int NumGrudge;			//fuel�s�q
	protected double ShipDepth;			//���`, �Ω�������קP�w
	protected double ShipPrevX;			//ship posX 5 sec ago
	protected double ShipPrevY;			//ship posY 5 sec ago
	protected double ShipPrevZ;			//ship posZ 5 sec ago
	//equip states: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
	protected float[] ArrayEquip;
	//final states: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
	protected float[] ArrayFinal;
	//EntityState: 0:State 1:Emotion 2:SwimType
	protected byte[] EntityState;
	//EntityFlag: 0:CanFloatUp 1:IsMarried 2:UseAmmoLight 3:UseAmmoHeavy 4:NoFuel
	protected boolean[] EntityFlag;
	//BonusPoint: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
	protected byte[] BonusPoint;
	//TypeModify: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
	protected float[] TypeModify;
	
	
	public BasicEntityShip(World world) {
		super(world);
		//init value
		isImmuneToFire = true;	//set ship immune to lava
		ShipLevel = 1;				//ship level
		Kills = 0;					//kill mobs
		ExpCurrent = 0;				//current exp
		ExpNext = 40;				//lv1 -> lv2 exp
		//equip states: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
		ArrayEquip = new float[] {0F, 0F, 0F, 0F, 0F, 0F};
		//final states: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
		ArrayFinal = new float[] {0F, 0F, 0F, 0F, 0F, 0F};
		//EntityState: 0:State 1:Emotion 2:SwimType
		EntityState = new byte[] {0, 0, 0};
		//EntityFlag: 0:CanFloatUp 1:IsMarried 2:UseAmmoLight 3:UseAmmoHeavy 4:NoFuel
		EntityFlag = new boolean[] {false, false, true, true, false};
		//BonusPoint: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
		BonusPoint = new byte[] {0, 0, 0, 0, 0, 0};
		//TypeModify: 0:HP 1:ATK 2:DEF 3:SPD 4:MOV 5:HIT
		TypeModify = new float[] {1F, 1F, 1F, 1F, 1F, 1F};
		//for AI
		NumAmmoLight = 0;
		NumAmmoHeavy = 0;
		NumGrudge = 0;
		StartEmotion = -1;		//emotion start time
		ShipDepth = 0D;			//water block above ship (within ship position)
		ShipPrevX = posX;		//ship position 5 sec ago
		ShipPrevY = posY;
		ShipPrevZ = posZ;
	}
	
	@Override
	public boolean isAIEnabled() {
		return true;
	}
	
	@Override
	public float getEyeHeight() {
		return this.height * 1F;
	}
	
	//get owner name (for player owner only)
	public String getOwnerName() {
		if(this.getOwner() != null) {
			if(this.getOwner() instanceof EntityPlayer) {
				return ((EntityPlayer)this.getOwner()).getDisplayName();
			}
		}		
		return null;
	}

	@Override
	public EntityAgeable createChild(EntityAgeable p_90011_1_) {
		return null;
	}
	
	//setup AI
	protected void setAIList() {
		this.getNavigator().setEnterDoors(true);
		this.getNavigator().setAvoidsWater(false);
		this.getNavigator().setCanSwim(true);
	}
	
	//setup target AI
	abstract protected void setAITargetList();

	//clear AI
	protected void clearAITasks() {
	   tasks.taskEntries.clear();
	}
	
	//clear target AI
	protected void clearAITargetTasks() {
	   targetTasks.taskEntries.clear();
	}
	
	//getter
	public ExtendShipProps getExtProps() {
		return ExtProps;
	}
	public short getShipLevel() {
		return ShipLevel;
	}
	public int getKills() {
		return Kills;
	}
	public int getExpCurrent() {
		return ExpCurrent;
	}
	public int getExpNext() {
		return ExpNext;
	}
	public byte getShipType() {
		return ShipType;
	}
	public byte getShipID() {
		return ShipID;
	}
	public int getStartEmotion() {
		return StartEmotion;
	}
	public int getNumAmmoLight() {
		return NumAmmoLight;
	}
	public int getNumAmmoHeavy() {
		return NumAmmoHeavy;
	}
	public int getNumGrudge() {
		return NumGrudge;
	}
	public boolean hasAmmoLight() {
		return NumAmmoLight > 0;
	}
	public boolean hasAmmoHeavy() {
		return NumAmmoHeavy > 0;
	}
	public double getShipDepth() {
		return ShipDepth;
	}
	public boolean getEntityFlag(int flag) {	//get flag (boolean)
		return EntityFlag[flag];		
	}
	public byte getEntityFlagI(int flag) {		//get flag (byte)
		if(EntityFlag[flag]) {
			return 1;
		}
		else {
			return 0;
		}
	}
	public float getEquipState(int state) {
		return ArrayEquip[state];
	}
	public float getFinalState(int state) {
		return ArrayFinal[state];
	}
	public byte getEntityState(int state) {
		return EntityState[state];
	}
	public byte getBonusPoint(int state) {
		return BonusPoint[state];
	}
	public float getTypeModify(int state) {
		return TypeModify[state];
	}
	
	//replace isInWater, check water block with NO extend AABB
	private void checkDepth() {
		Block BlockCheck = checkBlockWithOffset(0);
		
		if(BlockCheck == Blocks.water || BlockCheck == Blocks.lava) {
			ShipDepth = 1;
			for(int i=1; (this.posY+i)<255D; i++) {
				BlockCheck = checkBlockWithOffset(i);
				if(BlockCheck == Blocks.water || BlockCheck == Blocks.lava) {
					ShipDepth++;
				}
				else {
					if(BlockCheck == Blocks.air) {
						setEntityFlag(AttrID.F_CanFloatUp, true);
					}
					else {
						setEntityFlag(AttrID.F_CanFloatUp, false);
					}
					break;
				}
			}		
			ShipDepth = ShipDepth - (this.posY - (int)this.posY);
		}
		else {
			ShipDepth = 0;
		}
	}
	
	//check block from entity posY + offset
	public Block checkBlockWithOffset(int par1) {
		int blockX = MathHelper.floor_double(this.posX);
	    int blockY = MathHelper.floor_double(this.boundingBox.minY);
	    int blockZ = MathHelper.floor_double(this.posZ);
	    
	    return this.worldObj.getBlock(blockX, blockY + par1, blockZ);
	}
	
	//setter	
	//setting attributes, called at load nbt data & init mob
	public void calcShipAttributes(byte id) {
		//init or renew bonus value, for short value: discard decimal
		//HP = (base + (point + 1) * level * typeModify) * config HP ratio
		ArrayFinal[AttrID.HP] = ((float)AttrValues.BaseHP[id] + (float)(BonusPoint[AttrID.HP]+1) * (float)ShipLevel * TypeModify[AttrID.HP]) * ConfigHandler.hpRatio; 
		//ATK = base + ((point + 1) * level / 3 + equip) * typeModify
		ArrayFinal[AttrID.ATK] = (float)AttrValues.BaseATK[id] + ((float)(BonusPoint[AttrID.ATK]+1) * (((float)ShipLevel)/3F) + ArrayEquip[AttrID.ATK]) * TypeModify[AttrID.ATK];
		//DEF = base + ((point + 1) * level / 3 * 0.4 + equip) * typeModify
		ArrayFinal[AttrID.DEF] = (float)AttrValues.BaseDEF[id] + ((float)(BonusPoint[AttrID.DEF]+1) * (((float)ShipLevel)/3F) * 0.4F + ArrayEquip[AttrID.DEF]) * TypeModify[AttrID.DEF];
		//SPD = base + ((point + 1) * level / 10 * 0.02 + equip) * typeModify
		ArrayFinal[AttrID.SPD] = AttrValues.BaseSPD[id] + ((float)(BonusPoint[AttrID.SPD]+1) * (((float)ShipLevel)/10F) * 0.02F + ArrayEquip[AttrID.SPD]) * TypeModify[AttrID.SPD];
		//MOV = base + ((point + 1) * level / 10 * 0.01 + equip) * typeModify
		ArrayFinal[AttrID.MOV] = AttrValues.BaseMOV[id] + ((float)(BonusPoint[AttrID.MOV]+1) * (((float)ShipLevel)/10F) * 0.01F) * TypeModify[AttrID.MOV] + ArrayEquip[AttrID.MOV];
		//HIT = base + ((point + 1) * level / 10 * 0.8 + equip) * typeModify
		ArrayFinal[AttrID.HIT] = AttrValues.BaseHIT[id] + ((float)(BonusPoint[AttrID.HIT]+1) * (((float)ShipLevel)/10F) * 0.8F + ArrayEquip[AttrID.HIT]) * TypeModify[AttrID.HIT];
		//KB Resistance = Level / 10 * 0.04
		float resisKB = (((float)ShipLevel)/10F) * 0.067F;

		if(ArrayFinal[AttrID.DEF] > 95F) {
			ArrayFinal[AttrID.DEF] = 95F;	//max def = 95%
		}
		if(ArrayFinal[AttrID.SPD] > 2F) {
			ArrayFinal[AttrID.SPD] = 2F;	//min attack delay = 0.5 sec
		}
		if(ArrayFinal[AttrID.SPD] < 0F) {
			ArrayFinal[AttrID.SPD] = 0F;
		}
		if(ArrayFinal[AttrID.MOV] > 0.8F) {
			ArrayFinal[AttrID.MOV] = 0.8F;	//high move speed is buggy
		}
		if(ArrayFinal[AttrID.MOV] < 0F) {
			ArrayFinal[AttrID.MOV] = 0F;
		}
		
		this.jumpMovementFactor = (1F + ArrayFinal[AttrID.MOV]) * 0.03F;
		
		//set attribute by final value
		/**
		 * DO NOT SET ATTACK DAMAGE to non-EntityMob!!!!!
		 */
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(ArrayFinal[AttrID.HP]);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(ArrayFinal[AttrID.MOV]);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(ArrayFinal[AttrID.HIT]+16); //������ؼ�, ���|���d��
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(resisKB);
		//for new ship
		if(this.getHealth() == 20F) this.setHealth(this.getMaxHealth());
		
		//for server side
		if(!worldObj.isRemote) {
			clearAITargetTasks();	//reset target AI (update hit range)
			setAITargetList();	
			sendSyncPacket();		//sync nbt data
		}
	}
	
	//set exp value, no sync or update (for client load nbt data, gui display)
	public void setExpCurrent(int par1) {
		ExpCurrent = par1;
	}
	
	//set next exp value, no sync or update (for client load nbt data, gui display)
	public void setExpNext() {
		ExpNext = ShipLevel * 20 + 20;
	}
		
	//called when entity exp++
	public void addShipExp(int exp) {
		int CapLevel = getEntityFlag(AttrID.F_IsMarried) ? 150 : 100;
		
		if(ShipLevel != CapLevel && ShipLevel < 150) {	//level is not cap level
			ExpCurrent += exp;
			if(ExpCurrent >= ExpNext) {
				ExpCurrent -= ExpNext;	//level up
				ExpNext = (ShipLevel + 1) * 20 + 20;
				setShipLevel(++ShipLevel, true);
			}
		}	
	}
	
	//called when entity level up
	public void setShipLevel(short par1, boolean update) {
		//set level
		if(par1 < 151) {
			ShipLevel = par1;
		}
		//update attributes
		if(update) { 
			calcShipAttributes(ShipID); 
			this.setHealth(this.getMaxHealth());
		}
	}
	
	//called when a mob die near the entity (used in event handler)
	public void addKills() {
		Kills++;
	}	
	//called when load nbt data or client sync
	public void setKills(int par1) {
		Kills = par1;
	}
	//called when load nbt data or client sync
	public void setNumAmmoLight(int par1) {
		NumAmmoLight = par1;
	}	
	//called when load nbt data or client sync
	public void setNumAmmoHeavy(int par1) {
		NumAmmoHeavy = par1;
	}	
	//called when load nbt data or client sync
	public void setNumGrudge(int par1) {
		NumGrudge = par1;
	}
	//ship attribute setter, sync packet in method: calcShipAttributes 
	public void setFinalState(int state, float par1) {
		ArrayFinal[state] = par1;
	}
	public void setBonusPoint	(int state, byte par1) {
		BonusPoint[state] = par1;
	}
	//called when load nbt data or GUI click
	public void setEntityFlag(int flag, boolean par1) {
		this.EntityFlag[flag] = par1;	
	}
	//called when load nbt data or GUI click
	public void setEntityFlagI(int flag, int par1) {
		if(par1 == 1) {
			this.EntityFlag[flag] = true;
		}
		else {
			this.EntityFlag[flag] = false;
		}
	}
	
	//called when entity equip changed
	//this method get equip state from slots, no input parm
	public void setEquipAndUpdateState() {
		ItemStack itemstack = null;
		float[] equipStat = {0F,0F,0F,0F,0F,0F};
		ArrayEquip[AttrID.HP] = 0F;
		ArrayEquip[AttrID.ATK] = 0F;
		ArrayEquip[AttrID.DEF] = 0F;
		ArrayEquip[AttrID.SPD] = 0F;
		ArrayEquip[AttrID.MOV] = 0F;
		ArrayEquip[AttrID.HIT] = 0F;
		
		//calc equip slots
		for(int i=0; i<ContainerShipInventory.SLOTS_EQUIP; i++) {
			itemstack = this.ExtProps.slots[i];
			if(itemstack != null) {
				equipStat = EquipCalc.getEquipStat(itemstack);
				ArrayEquip[AttrID.HP] += equipStat[AttrID.HP];
				ArrayEquip[AttrID.ATK] += equipStat[AttrID.ATK];
				ArrayEquip[AttrID.DEF] += equipStat[AttrID.DEF];
				ArrayEquip[AttrID.SPD] += equipStat[AttrID.SPD];
				ArrayEquip[AttrID.MOV] += equipStat[AttrID.MOV];
				ArrayEquip[AttrID.HIT] += equipStat[AttrID.HIT];
			}
		}
		//update value
		calcShipAttributes(this.ShipID);
	}
	
	//called when entity spawn, set the type modify
	public void initTypeModify() {
		TypeModify[AttrID.HP] = AttrValues.ModHP[ShipID];
		TypeModify[AttrID.ATK] = AttrValues.ModATK[ShipID];
		TypeModify[AttrID.DEF] = AttrValues.ModDEF[ShipID];
		TypeModify[AttrID.SPD] = AttrValues.ModSPD[ShipID];
		TypeModify[AttrID.MOV] = AttrValues.ModMOV[ShipID];
		TypeModify[AttrID.HIT] = AttrValues.ModHIT[ShipID];
	}

	public void setEntityState(int par1, boolean sync) {
		EntityState[AttrID.State] = (byte)par1;	
		if(sync && !worldObj.isRemote) {
			CreatePacketS2C.sendS2CEntityStateSync(this);    
		}
	}
	
	public void setEntityEmotion(int par1, boolean sync) {
		EntityState[AttrID.Emotion] = (byte)par1;
		if(sync && !worldObj.isRemote) {
			CreatePacketS2C.sendS2CEntityStateSync(this);    
		}
	}
	
	public void setEntitySwimType(int par1, boolean sync) {
		EntityState[AttrID.SwimType] = (byte)par1;			
		if(sync && !worldObj.isRemote) {
			CreatePacketS2C.sendS2CEntityStateSync(this);    
		}   
	}
	
	//emotion start time (CLIENT ONLY), called from model class
	public void setStartEmotion(int par1) {
		StartEmotion = par1;
	}
	
	//manual send sync packet
	public void sendSyncPacket() {
		if (!worldObj.isRemote) {
			CreatePacketS2C.sendS2CEntitySync(this);     
		}
	}
	
	//right click on ship
	@Override
	public boolean interact(EntityPlayer player) {
		
		ItemStack itemstack = player.inventory.getCurrentItem();  //get item in hand
		
		//�p�G�w�g�Q�i�j, �A�I�@�U�i�H�Ѱ��i�j
		if(this.getLeashed() && this.getLeashedToEntity() == player) {
            this.clearLeashed(true, !player.capabilities.isCreativeMode);
            return true;
        }
	
		//shift+right click�ɥ��}GUI
		if(player.isSneaking() && this.getOwner() != null && player.getUniqueID().equals(this.getOwner().getUniqueID())) {  
			int eid = this.getEntityId();
			//player.openGui vs FMLNetworkHandler ?
		//	player.openGui(ShinColle.instance, GUIs.SHIPINVENTORY, this.worldObj, eid, 0, 0);
    		FMLNetworkHandler.openGui(player, ShinColle.instance, GUIs.SHIPINVENTORY, this.worldObj, this.getEntityId(), 0, 0);
    		return true;
		}
		
		//use item
		if(itemstack != null) {
			//use cake to change state
			if(itemstack.getItem() == Items.cake) {
				int ShipState = getEntityState(AttrID.State) - AttrValues.State.EQUIP;
				
				switch(getEntityState(AttrID.State)) {
				case AttrValues.State.NORMAL:			//�쥻�����, �אּ���
					setEntityState(AttrValues.State.EQUIP, true);
					break;
				case AttrValues.State.NORMAL_MINOR:
					setEntityState(AttrValues.State.EQUIP_MINOR, true);
					break;
				case AttrValues.State.NORMAL_MODERATE:
					setEntityState(AttrValues.State.EQUIP_MODERATE, true);
					break;
				case AttrValues.State.NORMAL_HEAVY:
					setEntityState(AttrValues.State.EQUIP_HEAVY, true);
					break;
				case AttrValues.State.EQUIP:			//�쥻��ܸ˳�, �אּ�����
					setEntityState(AttrValues.State.NORMAL, true);
					break;
				case AttrValues.State.EQUIP_MINOR:
					setEntityState(AttrValues.State.NORMAL_MINOR, true);
					break;
				case AttrValues.State.EQUIP_MODERATE:
					setEntityState(AttrValues.State.NORMAL_MODERATE, true);
					break;
				case AttrValues.State.EQUIP_HEAVY:
					setEntityState(AttrValues.State.NORMAL_HEAVY, true);
					break;			
				}
				return true;
			}
			
			//use repair bucket
			if(itemstack.getItem() == ModItems.BucketRepair) {	
				//hp����max hp�ɥi�H�ϥ�bucket
				if(this.getHealth() < this.getMaxHealth()) {
	                if (!player.capabilities.isCreativeMode) {  //stack-1 in non-creative mode
	                    --itemstack.stackSize;
	                }
	
	                if(this instanceof BasicEntitySmallShip) {
	                	this.heal(this.getMaxHealth() * 0.1F);	//1 bucket = 10% hp for small ship
	                }
	                else {
	                	this.heal(this.getMaxHealth() * 0.05F);	//1 bucket = 5% hp for large ship
	                }
	                
	                if (itemstack.stackSize <= 0) {  //���~�Χ��ɭn�]�w��null�M�Ÿ�slot
	                	player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack)null);
	                }
	                
	                return true;
	            }			
			}	
			
			//use lead
			if(itemstack.getItem() == Items.lead && this.allowLeashing()) {	
				this.setLeashedToEntity(player, true);
				return true;
	        }
			
		}
		
		//click ship without shift = sit
		if(!this.worldObj.isRemote && !player.isSneaking() && this.getOwner() != null && player.getUniqueID().equals(this.getOwner().getUniqueID())) {			
            this.setSitting(!this.isSitting());
            this.isJumping = false;
            this.setPathToEntity((PathEntity)null);
            this.setTarget((Entity)null);
            this.setAttackTarget((EntityLivingBase)null);
            return true;
        }

		return false;
	}
	
	//update entity position
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		this.checkDepth();	//check depth every tick

		Block CheckBlock = checkBlockWithOffset(0);
		if(this.getShipDepth() > 0D) {
			//�b�G�餤��������, �����۵M�U���t�׬�0.02
			this.motionY += 0.02D;
			//�b�����[�t
			this.motionX *= 1.1D;
			this.motionZ *= 1.1D;

			if(this.worldObj.isRemote) {
				//�����ʮ�, ���ͤ���S��
				//(�`�N��entity�]���]���D���t��s, client�ݤ��|��smotionX���ƭ�, �ݦۦ�p��)
				double motX = this.posX - this.prevPosX;
				double motZ = this.posZ - this.prevPosZ;
				double parH = this.posY - (int)this.posY;
				
				EntityFX particleSpray = new EntityFXSpray(worldObj, 
				          this.posX + motX*1.5D, this.posY + 0.5D, this.posZ + motZ*1.5D, 
				          -motX*0.5D, 0D, -motZ*0.5D,
				          1F, 1F, 1F, 1F);
				    
				if(motX != 0 || motZ != 0) {
					Minecraft.getMinecraft().effectRenderer.addEffect(particleSpray);
				}
			}
		}
	}

	//check entity state every tick
	@Override
	public void onLivingUpdate() {
        super.onLivingUpdate();
        
        //server side check
        if((!worldObj.isRemote)) {    	
        	//sync client and reset AI after server start 2 sec
        	if(ticksExisted == 40) {
        		clearAITasks();
        		clearAITargetTasks();	//reset AI for get owner after loading NBT data
        		setAIList();
        		setAITargetList();
        		sendSyncPacket();		//sync packet to client
        	}
        	    	
        	//check every 100 ticks
        	if(ticksExisted % 100 == 0) {
        		//roll emtion: hungry > T_T > bored > O_O
        		if(getEntityFlag(AttrID.F_NoFuel)) {
        			if(this.getEntityState(AttrID.Emotion) != AttrValues.Emotion.HUNGRY) {
        				LogHelper.info("DEBUG : set emotion HUNGRY");
	    				this.setEntityEmotion(AttrValues.Emotion.HUNGRY, true);
	    			}
        		}
        		else {
        			if(this.getHealth()/this.getMaxHealth() < 0.5F) {
    	    			if(this.getEntityState(AttrID.Emotion) != AttrValues.Emotion.T_T) {
    	    				LogHelper.info("DEBUG : set emotion T_T");
    	    				this.setEntityEmotion(AttrValues.Emotion.T_T, true);
    	    			}			
    	    		}
        			else {
        				if(this.isSitting() && this.getRNG().nextInt(3) > 1) {	//30% for bored
        	    			if(this.getEntityState(AttrID.Emotion) != AttrValues.Emotion.BORED) {
        	    				LogHelper.info("DEBUG : set emotion BORED");
        	    				this.setEntityEmotion(AttrValues.Emotion.BORED, true);
        	    			}
        	    		}
        	    		else {	//back to normal face
        	    			if(this.getEntityState(AttrID.Emotion) != AttrValues.Emotion.NORMAL) {
        	    				LogHelper.info("DEBUG : set emotion NORMAL");
        	    				this.setEntityEmotion(AttrValues.Emotion.NORMAL, true);
        	    			}
        	    		}
        			}     			
        		}

        		//set air value
        		if(this.getAir() < 300) {
                	setAir(300);
                }
        		
        		//get ammo if no ammo
        		if(!this.hasAmmoLight()) { this.decrAmmoNum(2); }
        		if(!this.hasAmmoHeavy()) { this.decrAmmoNum(3); }
        		
        		//calc move distance and eat grudge (check position 5 sec ago)
        		double distX = posX - ShipPrevX;
        		double distY = posY - ShipPrevY;
        		double distZ = posZ - ShipPrevZ;
        		ShipPrevX = posX;
        		ShipPrevY = posY;
        		ShipPrevZ = posZ;
	        	int distSqrt = (int) MathHelper.sqrt_double(distX*distX + distY*distY + distZ*distZ);
	        	decrGrudgeNum(distSqrt+5);	//eat grudge or change movement speed
        		
        	}//end every 100 ticks
        }//end if(server side)
    }

	//melee attack method, no ammo cost, no attack speed, damage = 12.5% atk
	@Override
	public boolean attackEntityAsMob(Entity target) {
		//get attack value
		float atk = ArrayFinal[AttrID.ATK] * 0.125F;
		//set knockback value (testing)
		float kbValue = 0.15F;
		
		//experience++
		addShipExp(1);
		
		//grudge--
		decrGrudgeNum(1);
				
	    //�Natk��attacker�ǵ��ؼЪ�attackEntityFrom��k, �b�ؼ�class���p��ˮ`
	    //�åB�^�ǬO�_���\�ˮ`��ؼ�
	    boolean isTargetHurt = target.attackEntityFrom(DamageSource.causeMobDamage(this), atk);

	    //play entity attack sound
        if(this.getRNG().nextInt(10) > 6) {
        	this.playSound(Reference.MOD_ID+":ship-hitsmall", 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        }
	    
	    //if attack success
	    if(isTargetHurt) {
	    	//calc kb effect
	        if(kbValue > 0) {
	            target.addVelocity((double)(-MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F) * kbValue), 
	                   0.1D, (double)(MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F) * kbValue));
	            motionX *= 0.6D;
	            motionZ *= 0.6D;
	        }

	        //send packet to client for display partical effect   
	        if (!worldObj.isRemote) {
				CreatePacketS2C.sendS2CAttackParticle(target, 1);     
			}
	    }

	    return isTargetHurt;
	}
	
	//range attack method, cost light ammo, attack delay = 20 / attack speed, damage = 100% atk 
	public boolean attackEntityWithAmmo(Entity target) {	
		//get attack value
		float atk = ArrayFinal[AttrID.ATK];
		//set knockback value (testing)
		float kbValue = 0.05F;
		//update entity look at vector (for particle spawn)
        //����k��getLook�٥��T (client sync���D)
        float lookX = (float) (target.posX - this.posX);
        float lookY = (float) (target.posY - this.posY);
        float lookZ = (float) (target.posZ - this.posZ);
        float lookDist = MathHelper.sqrt_float(lookX*lookX + lookY*lookY + lookZ*lookZ);
        lookX = lookX / lookDist;
        lookY = lookY / lookDist;
        lookZ = lookZ / lookDist;
        
        //�o�g�̷����S��
        CreatePacketS2C.sendS2CAttackParticle2(this.getEntityId(), this.posX, this.posY, this.posZ, lookX, lookY, lookZ, 6);

		//play cannon fire sound at attacker
        playSound(Reference.MOD_ID+":ship-firesmall", 0.4F, 0.7F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        //play entity attack sound
        if(this.rand.nextInt(10) > 7) {
        	this.playSound(Reference.MOD_ID+":ship-hitsmall", 1F, 1F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        }
        
        //experience++
  		addShipExp(2);
  		
  		//grudge--
  		decrGrudgeNum(1);
        
        //light ammo -1
        if(!decrAmmoNum(0)) {		//not enough ammo
        	atk = atk * 0.125F;	//reduce damage to 12.5%
        }
        
        //calc miss chance
        int missChance = (int) (ArrayFinal[AttrID.HIT] / (2F + 3F * (lookDist / ArrayFinal[AttrID.HIT])));
        if(missChance < 3) missChance = 3;	//max miss chance = 33% 
        if(this.rand.nextInt(missChance) == 0) {
        	atk = 0;	//still attack, but no damage
        	//spawn miss particle
    		EntityFX particleMiss = new EntityFXMiss(worldObj, 
    		          this.posX, this.posY+this.height, this.posZ, 1F);	    
    		Minecraft.getMinecraft().effectRenderer.addEffect(particleMiss);
        }

	    //�Natk��attacker�ǵ��ؼЪ�attackEntityFrom��k, �b�ؼ�class���p��ˮ`
	    //�åB�^�ǬO�_���\�ˮ`��ؼ�
	    boolean isTargetHurt = target.attackEntityFrom(DamageSource.causeMobDamage(this), atk);

	    //if attack success
	    if(isTargetHurt) {
	    	//calc kb effect
	        if(kbValue > 0) {
	            target.addVelocity((double)(-MathHelper.sin(rotationYaw * (float)Math.PI / 180.0F) * kbValue), 
	                   0.1D, (double)(MathHelper.cos(rotationYaw * (float)Math.PI / 180.0F) * kbValue));
	            motionX *= 0.6D;
	            motionZ *= 0.6D;
	        }
	        
        	//send packet to client for display partical effect  
        	CreatePacketS2C.sendS2CAttackParticle(target, 9);	//�ؼФ��u�S��
        }

	    return isTargetHurt;
	}

	//range attack method, cost heavy ammo, attack delay = 100 / attack speed, damage = 500% atk
	public boolean attackEntityWithHeavyAmmo(Entity target) {	
		//get attack value
		float atk = ArrayFinal[AttrID.ATK] * 5F;
		//set knockback value (testing)
		float kbValue = 0.15F;
		//���u�O�_�ĥΪ��g
		boolean isDirect = false;
		//�p��ؼжZ��
		double tarX = target.posX;	//for miss chance calc
		double tarY = target.posY;
		double tarZ = target.posZ;
        double distX = tarX - this.posX;
        double distY = tarY - this.posY;
        double distZ = tarZ - this.posZ;
        float distSqrt = MathHelper.sqrt_double(distX*distX + distY*distY + distZ*distZ);
        double launchPos = this.posY + this.height * 0.7D;
        
        //�W�L�@�w�Z��/���� , �h�ĥΩߪ��u,  �b�����ɵo�g���׸��C
        if((distX*distX+distY*distY+distZ*distZ) < 36) {
        	isDirect = true;
        }
        if(this.getShipDepth() > 0D) {
        	LogHelper.info("DEBUG : depth > 0 , use under missile");
        	isDirect = true;
        	launchPos = this.posY;
        }
		
		//experience++
		this.addShipExp(16);
		
		//grudge--
		decrGrudgeNum(1);
	
		//play cannon fire sound at attacker
        this.playSound(Reference.MOD_ID+":ship-fireheavy", 0.4F, 0.7F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        //play entity attack sound
        if(this.getRNG().nextInt(10) > 7) {
        	this.playSound(Reference.MOD_ID+":ship-hitsmall", 1F, 1F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        }
        
        //heavy ammo -1
        if(!decrAmmoNum(1)) {	//not enough ammo
        	atk = atk * 0.125F;	//reduce damage to 12.5%
        }
        
        //calc miss chance, miss: add random offset(0~6) to missile target 
        int missChance = (int) (ArrayFinal[AttrID.HIT] / (2F + 3F * (distSqrt / ArrayFinal[AttrID.HIT])));
        if(missChance < 3) missChance = 3;	//max miss chance = 33% 
        if(this.rand.nextInt(missChance) == 0) {
        	tarX = tarX - 3D + this.rand.nextDouble() * 6D;
        	tarY = tarY + this.rand.nextDouble() * 3D;
        	tarZ = tarZ - 3D + this.rand.nextDouble() * 6D;
        	//spawn miss particle
    		EntityFX particleMiss = new EntityFXMiss(worldObj, 
    		          this.posX, this.posY+this.height, this.posZ, 1F);	    
    		Minecraft.getMinecraft().effectRenderer.addEffect(particleMiss);
        }

        //spawn missile   NYI: target position + random offset by ship HIT value
        EntityAbyssMissile missile = new EntityAbyssMissile(this.worldObj, this, 
        		tarX, tarY+target.height*0.2D, tarZ, launchPos, atk, kbValue, isDirect);
        this.worldObj.spawnEntityInWorld(missile);
        
        return true;
	}
	
	//be attacked method, �]�A��Lentity����, anvil����, arrow����, fall damage���ϥΦ���k 
	@Override
    public boolean attackEntityFrom(DamageSource attacker, float atk) {		
		//�i��def�p��
        float reduceAtk = atk * (1F - this.ArrayFinal[AttrID.DEF] / 100F);    
        if(atk < 0) { atk = 0; }
        
        //�L�Ī�entity�ˮ`�L��
		if(this.isEntityInvulnerable()) {	
            return false;
        }
		else if(attacker.getSourceOfDamage() != null) { 
			//���|��ۤv�y���ˮ`
			if(attacker.getSourceOfDamage().equals(this)) {  
				return false;
			}
			
			//�Y������P�ˬ�ship, �h�ˮ`-95% (��ship vs ship�ॴ�[�@�I)
			if(attacker.getSourceOfDamage() instanceof BasicEntityShip) {
				reduceAtk *= 0.05F;
			}

			//�������U�ʧ@
            this.setSitting(false);
   
            //�����class���Q�����P�w, �]�A���mlove�ɶ�, �p����r�ܩ�, �p���K�z/�����ˮ`, 
            //hurtResistantTime(0.5sec�L�Įɶ�)�p��, 
            return super.attackEntityFrom(attacker, reduceAtk);
        }
		
		return false;
    }
	
	//decrese ammo number with type, or find ammo item from inventory
	private boolean decrAmmoNum(int type) {
		switch(type) {
		case 0:  //use 1 light ammo
			if(NumAmmoLight > 0) { 
				--NumAmmoLight;
				return true;
			}
			else {
				if(decrSupplies(0)) {  //find ammo item
					NumAmmoLight += 29;
					return true;
				}
				else if(decrSupplies(2)) {  //find ammo item
					NumAmmoLight += 269;
					return true;
				}
				else {				   //no ammo
					return false;
				}
			}
		case 1:  //use 1 heavy ammo
			if(NumAmmoHeavy > 0) { 
				--NumAmmoHeavy;
				return true;
			}
			else {
				if(decrSupplies(1)) {  //find ammo item
					NumAmmoHeavy += 14;
					return true;
				}
				else if(decrSupplies(3)) {  //find ammo item
					NumAmmoHeavy += 134;
					return true;
				}
				else {				   //no ammo
					return false;
				}
			}
		case 2:	//no ammo light, use item
			if(decrSupplies(0)) {  //find ammo item
				NumAmmoLight += 30;
				return true;
			}
			else if(decrSupplies(2)) {  //find ammo item
				NumAmmoLight += 270;
				return true;
			}
			else {				   //no ammo
				return false;
			}
		case 3:	//no ammo heavy, use item
			if(decrSupplies(1)) {  //find ammo item
				NumAmmoHeavy += 15;
				return true;
			}
			else if(decrSupplies(3)) {  //find ammo item
				NumAmmoHeavy += 135;
				return true;
			}
			else {				   //no ammo
				return false;
			}
		}
		
		return false;	//unknow attack type
	}
	
	//eat grudge and change movement speed
	private void decrGrudgeNum(int par1) {
		boolean PrevNoFuel = getEntityFlag(AttrID.F_NoFuel);
		
		if(par1 > 215) {	//max cost = 215 (calc from speed 1 moving 5 sec)
			par1 = 215;
		}
		
		if(NumGrudge > (int)par1) { //has enough fuel
			NumGrudge -= (int)par1;
		}
		else {
			if(decrSupplies(4)) {		//find grudge
				NumGrudge += 1200;
				NumGrudge -= (int)par1;
			}
			else if(decrSupplies(5)) {	//find grudge block
				NumGrudge += 10800;
				NumGrudge -= (int)par1;
			}
//			else if(decrSupplies(6)) {	//find grudge heavy block
//				NumGrudge += 97200;
//				NumGrudge -= (int)par1;
//			}
		}
		
		if(NumGrudge <= 0) {
			setEntityFlag(AttrID.F_NoFuel, true);
		}
		else {
			setEntityFlag(AttrID.F_NoFuel, false);
		}

		//get fuel, set AI
		if(!getEntityFlag(AttrID.F_NoFuel) && PrevNoFuel) {
LogHelper.info("DEBUG : !NoFuel set AI");
			clearAITasks();
			clearAITargetTasks();
			setAIList();
			setAITargetList();
			sendSyncPacket();
		}
		
		//no fuel, clear AI
		if(getEntityFlag(AttrID.F_NoFuel)) {
LogHelper.info("DEBUG : NoFuel clear AI");
			clearAITasks();
			clearAITargetTasks();
			sendSyncPacket();
		}
		
	}
	
	//decrese ammo amount with type, return true or false(not enough item)
	private boolean decrSupplies(int type) {
		boolean isEnoughItem = true;
		int itemNum = 1;
		ItemStack itemType = null;
		
		//find ammo
		switch(type) {
		case 0:	//use 1 light ammo
			itemType = new ItemStack(ModItems.Ammo,1,0);
			break;
		case 1: //use 1 heavy ammo
			itemType = new ItemStack(ModItems.Ammo,1,2);
			break;
		case 2:	//use 1 light ammo container
			itemType = new ItemStack(ModItems.Ammo,1,1);
			break;
		case 3: //use 1 heavy ammo container
			itemType = new ItemStack(ModItems.Ammo,1,3);
			break;
		case 4: //use 1 grudge
			itemType = new ItemStack(ModItems.Grudge,1);
			break;
		case 5: //use 1 grudge block
			itemType = new ItemStack(ModBlocks.BlockGrudge,1);
			break;
		case 6: //use 1 grudge block
			itemType = new ItemStack(ModBlocks.BlockGrudgeHeavy,1);
			break;
		}
		
		//search item in ship inventory
		int i = findItemInSlot(itemType);
		if(i == -1) {		//item not found
			return false;
		}
		
		//decr item stacksize
		ItemStack getItem = this.ExtProps.slots[i];

		if(getItem.stackSize >= itemNum) {
			getItem.stackSize -= itemNum;
		}
		else {	//not enough item
			getItem.stackSize = 0;
			isEnoughItem = false;
		}
				
		if(getItem.stackSize == 0) {
			getItem = null;
		}
		
		//save back itemstack
		//no need to sync because no GUI opened
		this.ExtProps.slots[i] = getItem;	
		
		return isEnoughItem;	
	}

	//find item in ship inventory
	private int findItemInSlot(ItemStack parItem) {
		ItemStack slotitem = null;

		//search ship inventory (except equip slots)
		for(int i=ContainerShipInventory.SLOTS_EQUIP; i<ContainerShipInventory.SLOTS_TOTAL; i++) {
			slotitem = this.ExtProps.slots[i];
			if(slotitem != null && 
			   slotitem.getItem().equals(parItem.getItem()) && 
			   slotitem.getItemDamage() == parItem.getItemDamage()) {
				return i;	//found item
			}		
		}	
		
		return -1;	//item not found
	}

	
}