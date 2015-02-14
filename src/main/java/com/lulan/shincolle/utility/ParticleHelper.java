package com.lulan.shincolle.utility;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

/**�ɤl�S�ĳB�zclass
 * �]�t�I�s�S��, ����S�Ħ�m(NxNxN����), 
 */
public class ParticleHelper {
	
	private static World world = Minecraft.getMinecraft().theWorld;
	private static Random rand = new Random();
	/**ROTATE PARTICLE POSITION (NxNxN)
	 * in:��l�y��, ���, �H�έn�઺���V 	out:�৹���s��m
	 * �{���q�S�����W�U½��, �ҥHy�Ȥ��|�ܰ�
	 * f = face = 0,4:north  1,5:east  2,6:south  3,7:west
	 */
	public static double[] getNewPosition(double x, double y, double z, int f, int len) {
		
		double[] newParm = new double[3];
		newParm[1] = y;
		
		//�̷ӭ��V, �����l��m
		switch(f) {
		case 1:		//turn east
		case 5:
			newParm[0] = (double)len - z;
			newParm[2] = x;
			break;
		case 2:		//turn south
		case 6:
			newParm[0] = (double)len - x;
			newParm[2] = (double)len - z;
			break;
		case 3:		//turn west
		case 7:
			newParm[0] = z;
			newParm[2] = (double)len - x;
			break;
		default:	//default north, no change
			newParm[0] = x;
			newParm[2] = z;
			break;			
		}
			
		return newParm;
	}
	
	/**SPAWN ATTACK PARTICLE WITH CUSTOM POSITION
	 * @parm posX, posY, posZ, lookX, lookY, lookZ, type
	 */
	public static void spawnAttackParticleCustomVector(Entity target, double posX, double posY, double posZ, double lookX, double lookY, double lookZ, byte type) {
		if(target != null && target instanceof EntityLivingBase) {
			((EntityLivingBase) target).attackTime = 30;
		}
		
		//spawn particle
		spawnAttackParticleAt(posX, posY, posZ, lookX, lookY, lookZ, type);
	}
	
	/**SPAWN ATTACK PARTICLE
	 * spawn particle and set attack time for model rendering
	 * @parm entity, type
	 */
	public static void spawnAttackParticle(Entity target, byte type) {

		//target look
		double lookX = 0;
		double lookY = 0;
		double lookZ = 0;
		
		//get target position
		if(target != null) {
			if(target.getLookVec() != null) {
				lookX = target.getLookVec().xCoord;
				lookY = target.getLookVec().yCoord;
				lookZ = target.getLookVec().zCoord;
			}
			//spawn particle
			spawnAttackParticleAt(target.posX, target.posY, target.posZ, lookX, lookY, lookZ, type);
		}		
	}
	
	/**Spawn particle at xyz position
	 * @parm world, posX, posY, posZ, particleID
	 */
	public static void spawnAttackParticleAt(double posX, double posY, double posZ, double lookX, double lookY, double lookZ, byte type) {
		//get target position
		double ran1 = 0D;
		double ran2 = 0D;
		
//		GL11.glDepthMask(true);
		//spawn particle
		switch(type) {
		case 0:	//explode
			world.spawnParticle("explode", posX, posY+2, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 1: //largeexplode
			world.spawnParticle("largeexplode", posX, posY+2, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 2: //hugeexplosion
			world.spawnParticle("hugeexplosion", posX, posY+1, posZ, 0.0D, 0.0D, 0.0D);
			for(int i=0;i<20;i++) {
				ran1 = rand.nextFloat() * 6F - 3F;
				ran2 = rand.nextFloat() * 6F - 3F;
				world.spawnParticle("lava", posX+ran1, posY+1, posZ+ran2, 0D, 0D, 0D);
			}
			break;
		case 3: //crit
			world.spawnParticle("crit", posX, posY+2, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 4: //magicCrit
			world.spawnParticle("magicCrit", posX, posY+2, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 5: //smoke
			world.spawnParticle("smoke", posX, posY+2, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 6: //largesmoke
			for(int i=0; i<20; i++) {
				ran1 = rand.nextFloat() - 0.5F;
				world.spawnParticle("largesmoke", posX+lookX-0.5D+0.05D*i, posY+0.8D+ran1, posZ+lookZ-0.5D+0.05D*i, lookX*0.2D, 0.05D, lookZ*0.2D);
			}	
			break;
		case 7: //angryVillager
			world.spawnParticle("angryVillager", posX, posY+1, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 8: //flame
			world.spawnParticle("flame", posX, posY+2, posZ, 0.0D, 0.0D, 0.0D);
			break;
		case 9: //lava + largeexplode
			world.spawnParticle("largeexplode", posX, posY+1.5, posZ, 0.0D, 0.0D, 0.0D);
			for(int i=0; i<12; i++) {
				ran1 = rand.nextFloat() * 3F - 1.5F;
				ran2 = rand.nextFloat() * 3F - 1.5F;
				world.spawnParticle("lava", posX+ran1, posY+1, posZ+ran2, 0D, 0D, 0D);
			}			
			break;
		default:
			break;		
		}	
	}

	
}