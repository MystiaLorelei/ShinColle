package com.lulan.shincolle.init;

import net.minecraft.item.Item;

import com.lulan.shincolle.item.*;
import com.lulan.shincolle.reference.Reference;

import cpw.mods.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)	//�n��object holder��mod������e���y�q ��L�H�i�H����Ū���Ӫ���
public class ModItems {

	//spawn egg
	public static final ShipSpawnEgg ShipSpawnEgg = new ShipSpawnEgg();
	//materials
	public static final BasicItem AbyssMetal = new AbyssMetal();
	public static final BasicItem Ammo = new Ammo();
	public static final BasicItem BucketRepair = new BucketRepair();
	public static final BasicItem Grudge = new Grudge();
	//equip	
	public static final BasicItem EquipCannon = new EquipCannon();
	

	//�n��item��C���� (�bpre init���q�n��)
	public static void init() {
		//spawn egg
		GameRegistry.registerItem(ShipSpawnEgg, "ShipSpawnEgg");
		//materials
		GameRegistry.registerItem(AbyssMetal, "AbyssMetal");
		GameRegistry.registerItem(Ammo, "Ammo");
		GameRegistry.registerItem(BucketRepair, "BucketRepair");
		GameRegistry.registerItem(Grudge, "Grudge");
		//equip		
		GameRegistry.registerItem(EquipCannon, "EquipCannon");
		
	}
	
}