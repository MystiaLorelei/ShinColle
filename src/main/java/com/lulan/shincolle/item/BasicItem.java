package com.lulan.shincolle.item;

import com.lulan.shincolle.creativetab.CreativeTabSC;
import com.lulan.shincolle.reference.Reference;
import com.lulan.shincolle.utility.LogHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//��item class �w�q�U��item�򥻤�k
public class BasicItem extends Item {
	
	public BasicItem() {
		super();	//���]�@���쥻��item��l�� �U���~�~��]�w�Բ�item�ƭ� �p���|�Ƶ�
		this.setCreativeTab(CreativeTabSC.SC_TAB);	//�[�J��creative tab��
	}
	
	//name�]�w�Τ�k: �h��.���e���r�� �H�K�t�~��Wmod�W�٧Φ����r��
	protected String getUnwrappedUnlocalizedName(String unlocalizedName) {
		return unlocalizedName.substring(unlocalizedName.indexOf(".")+1);
	}
	
	//�Nname�a�Wmod�W�� �Ω󤧫ᵹ�U�y�t�ɮש�W���T�W��
	//�榡��item.MOD�W��:���~�W��.name
	@Override
	public String getUnlocalizedName() {
		return String.format("item.%s%s", Reference.MOD_ID+":", getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}
	
	//�PgetUnlocalizedName() �����[�Witemstack����
	//�榡��item.MOD�W��:���~�W��.name
	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		int meta = itemstack.getItemDamage();
		if(meta>0) {
			return String.format("item.%s%s", Reference.MOD_ID+":", getUnwrappedUnlocalizedName(super.getUnlocalizedName())+meta);
		}
		else {
			return String.format("item.%s%s", Reference.MOD_ID+":", getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
		}		
	}
	
	//���~�ϥܵn��
	//���X���~�W��(���tmod�W��)�@���Ѽƥᵹicon register�ӵn��icon
	//�`�Nicon�u�bclient�ݤ~�ݭn����
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".")+1));
	}

}