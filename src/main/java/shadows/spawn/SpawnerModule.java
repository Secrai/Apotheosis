package shadows.spawn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.handler.codec.http2.Http2FrameReader.Configuration;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.Apotheosis;
import shadows.Apotheosis.ApotheosisInit;
import shadows.Apotheosis.ApotheosisPreInit;
import shadows.ApotheosisObjects;
import shadows.spawn.modifiers.SpawnerModifier;

public class SpawnerModule {

	public static final Logger LOG = LogManager.getLogger("Apotheosis : Spawner");

	public static Configuration config;
	public static int spawnerSilkLevel = 1;

	@SubscribeEvent
	public void preInit(ApotheosisPreInit e) {
		//config = new Configuration(new File(Apotheosis.configDir, "spawner.cfg"));
		//if (Apotheosis.enableSpawner) {
		//	TileEntity.register("mob_spawner", TileSpawnerExt.class);
		//}
		TileEntityType.MOB_SPAWNER.factory = TileSpawnerExt::new;
	}

	@SubscribeEvent
	public void init(ApotheosisInit e) {
		//spawnerSilkLevel = config.getInt("Spawner Silk Level", "general", 1, -1, 127, "The level of silk touch needed to harvest a spawner.  Set to -1 to disable, 0 to always drop.  The enchantment module can increase the max level of silk touch.");
		SpawnerModifiers.init();
		//if (config.hasChanged()) config.save();
	}

	@SubscribeEvent
	public void blocks(Register<Block> e) {
		Apotheosis.registerOverrideBlock(e.getRegistry(), new BlockSpawnerExt(), Apotheosis.MODID);
	}

	@SubscribeEvent
	public void enchants(Register<Enchantment> e) {
		e.getRegistry().register(new EnchantmentCapturing().setRegistryName(Apotheosis.MODID, "capturing"));
	}

	@SubscribeEvent
	public void handleCapturing(LivingDropsEvent e) {
		Entity killer = e.getSource().getTrueSource();
		if (killer instanceof LivingEntity) {
			int level = EnchantmentHelper.getEnchantmentLevel(ApotheosisObjects.CAPTURING, ((LivingEntity) killer).getHeldItemMainhand());
			if (e.getEntityLiving().world.rand.nextFloat() < level / 250F) {
				LivingEntity killed = e.getEntityLiving();
				ItemStack egg = new ItemStack(SpawnEggItem.getEgg(killed.getType()));
				e.getDrops().add(new ItemEntity(killed.world, killed.posX, killed.posY, killed.posZ, egg));
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void drops(BlockEvent.HarvestDropsEvent e) {
		if (e.getState().getBlock() == Blocks.SPAWNER && e.getHarvester() != null) {
			if (SpawnerModule.spawnerSilkLevel != -1 && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, e.getHarvester().getHeldItemMainhand()) >= SpawnerModule.spawnerSilkLevel) {
				e.getDrops().clear();
			}
		}
	}

	@SubscribeEvent
	public void handleUseItem(RightClickBlock e) {
		TileEntity te;
		if ((te = e.getWorld().getTileEntity(e.getPos())) instanceof TileSpawnerExt) {
			ItemStack s = e.getItemStack();
			boolean inverse = SpawnerModifiers.inverseItem.test(e.getEntityPlayer().getHeldItem(e.getHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
			for (SpawnerModifier sm : SpawnerModifiers.MODIFIERS)
				if (sm.canModify((TileSpawnerExt) te, s, inverse)) e.setUseBlock(Result.ALLOW);
		}
	}

}
