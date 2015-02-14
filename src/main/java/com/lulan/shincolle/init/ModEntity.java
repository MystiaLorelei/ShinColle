package com.lulan.shincolle.init;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;

import com.lulan.shincolle.ShinColle;
import com.lulan.shincolle.entity.*;
import com.lulan.shincolle.entity.renderentity.*;
import com.lulan.shincolle.reference.Reference;
import com.lulan.shincolle.utility.LogHelper;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModEntity {

	public static void init() {	
		int modEntityID = 0;
		
		//register ship entity
		createEntity(EntityDestroyerI.class, "EntityDestroyerI", modEntityID++);
		createEntity(EntityHeavyCruiserRi.class, "EntityHeavyCruiserRi", modEntityID++);
				
		//register projectile entity
		createProjectileEntity(EntityAbyssMissile.class, "EntityAbyssMissile", modEntityID++);
	
		//register render entity
		createProjectileEntity(EntityRenderLargeShipyard.class, "EntityRenderLargeShipyard", modEntityID++);
		createProjectileEntity(EntityRenderVortex.class, "EntityRenderVortex", modEntityID++);
		
		//register test entity
		createEntityGlobalID(EntityTest.class, "EntityTest", 0x20FF45, 0x0040FF);
	
	}
	
	//�n���ͪ���k
	//�Ѽ�: �ӥͪ�class, �ͪ��W��, �Ǫ��J�I����, �Ǫ��J���I��
	public static void createEntity(Class entityClass, String entityName, int entityId){
		//�n���Ѽ�: �ͪ�class, �ͪ��W��, �ͪ�id, mod�ƥ�, �l�ܧ�s�Z��, ��s�ɶ����j, �O�_�o�e�P�B�ʥ](���tentity����true�~�|��ܥ���)
		EntityRegistry.registerModEntity(entityClass, entityName, entityId, ShinColle.instance, 64, 1, true);
	}
	
	//�n���D�ͪ���k (�L�ͩǳJ)
	//�Ѽ�: �ӥͪ�class, �ͪ��W��
	public static void createProjectileEntity(Class entityClass, String entityName, int entityId){
		//�n���Ѽ�: �ͪ�class, �ͪ��W��, �ͪ�id, mod�ƥ�, �l�ܧ�s�Z��, ��s�ɶ����j, �O�_�o�e�t�׫ʥ]
		EntityRegistry.registerModEntity(entityClass, entityName, entityId, ShinColle.instance, 128, 1, true);
	}
	
	//�ϥΩx��@�qid�n���ͪ�
	//�Ѽ�: �ӥͪ�class, �ͪ��W��
	public static void createEntityGlobalID(Class entityClass, String entityName, int backColor, int spotColor){
		int entityId = EntityRegistry.findGlobalUniqueEntityId();
		LogHelper.info("DEBUG : Register Entity with Global ID System "+entityId+" "+entityName);
		
		EntityRegistry.registerGlobalEntityID(entityClass, entityName, entityId);
		//�n���Ѽ�: �ͪ�class, �ͪ��W��, �ͪ�id, mod�ƥ�, �l�ܧ�s�Z��, ��s�ɶ����j, �O�_�o�e�t�׫ʥ]
		EntityRegistry.registerModEntity(entityClass, entityName, entityId, ShinColle.instance, 64, 1, false);
		//�n���Ǫ��ͪ��J: �ͪ�id, �ͦ��J��T(�ͪ�id,�I����,���I��)
		EntityList.entityEggs.put(Integer.valueOf(entityId), new EntityList.EntityEggInfo(entityId, backColor, spotColor));
	}
	

}