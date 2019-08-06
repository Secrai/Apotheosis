package shadows.ench;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import shadows.ench.objects.BlockHellBookshelf;

public class ContainerEnchantmentExt extends ContainerEnchantment {

	public ContainerEnchantmentExt(InventoryPlayer playerInv, World worldIn, BlockPos pos) {
		super(playerInv, worldIn, pos);
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		if (inventoryIn == this.tableInventory) {
			ItemStack itemstack = inventoryIn.getStackInSlot(0);

			if (!itemstack.isEmpty() && itemstack.isItemEnchantable()) {
				if (!this.world.isRemote) {

					float power = getEnchPower();
					this.rand.setSeed(this.xpSeed);

					for (int i1 = 0; i1 < 3; ++i1) {
						this.enchantLevels[i1] = EnchantmentHelper.calcItemStackEnchantability(this.rand, i1, (int) power, itemstack);
						this.enchantClue[i1] = -1;
						this.worldClue[i1] = -1;

						if (this.enchantLevels[i1] < i1 + 1) {
							this.enchantLevels[i1] = 0;
						}
						this.enchantLevels[i1] = ForgeEventFactory.onEnchantmentLevelSet(world, position, i1, (int) power, itemstack, enchantLevels[i1]);
					}

					for (int j1 = 0; j1 < 3; ++j1) {
						if (this.enchantLevels[j1] > 0) {
							List<EnchantmentData> list = this.getEnchantmentList(itemstack, j1, this.enchantLevels[j1]);

							if (list != null && !list.isEmpty()) {
								EnchantmentData enchantmentdata = list.get(this.rand.nextInt(list.size()));
								this.enchantClue[j1] = Enchantment.getEnchantmentID(enchantmentdata.enchantment);
								this.worldClue[j1] = enchantmentdata.enchantmentLevel;
							}
						}
					}

					this.detectAndSendChanges();
				}
			} else {
				for (int i = 0; i < 3; ++i) {
					this.enchantLevels[i] = 0;
					this.enchantClue[i] = -1;
					this.worldClue[i] = -1;
				}
			}
		}
	}

	private List<EnchantmentData> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
		this.rand.setSeed(this.xpSeed + enchantSlot);
		List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(this.rand, stack, level, false);

		if (stack.getItem() == Items.BOOK && list.size() > 1) {
			list.remove(this.rand.nextInt(list.size()));
		}

		return list;
	}

	public float getEnchPower() {
		float[] powers = { 0, 0 };

		for (int j = -1; j <= 1; ++j) {
			for (int k = -1; k <= 1; ++k) {
				if ((j != 0 || k != 0) && this.world.isAirBlock(this.position.add(k, 0, j)) && this.world.isAirBlock(this.position.add(k, 1, j))) {
					getSinglePower(powers, position.add(k * 2, 0, j * 2));
					getSinglePower(powers, position.add(k * 2, 1, j * 2));
					if (k != 0 && j != 0) {
						getSinglePower(powers, position.add(k * 2, 0, j));
						getSinglePower(powers, position.add(k * 2, 1, j));
						getSinglePower(powers, position.add(k, 0, j * 2));
						getSinglePower(powers, position.add(k, 1, j * 2));
					}
				}
			}
		}
		return Math.min(EnchModule.maxPower, Math.min(powers[0], EnchModule.maxNormalPower) + powers[1]);
	}

	public void getSinglePower(float[] powers, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		boolean hell = state.getBlock() instanceof BlockHellBookshelf;
		float power = state.getBlock().getEnchantPowerBonus(world, pos);
		powers[hell ? 1 : 0] += power;
	}
}
