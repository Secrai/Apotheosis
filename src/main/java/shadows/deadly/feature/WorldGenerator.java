package shadows.deadly.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import shadows.deadly.DeadlyModule;

public class WorldGenerator implements IWorldGenerator {

	public static final List<WorldFeature> FEATURES = new ArrayList<>();
	public static final BrutalSpawner BRUTAL_SPAWNER = new BrutalSpawner();
	public static final BossFeature BOSS_GENERATOR = new BossFeature();
	public static final SwarmSpawner SWARM_SPAWNER = new SwarmSpawner();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (!world.isRemote && world.provider.getDimension() == 0) WorldGenerator.run(world, new BlockPos(chunkX << 4, 0, chunkZ << 4));
	}

	public static void run(World world, BlockPos pos) {
		for (WorldFeature feature : FEATURES)
			feature.generate(world, pos);
	}

	/**
	 * Builds a glass pillar from the given location up to layer 127.
	 */
	public static void debugPillar(World world, BlockPos pos) {
		MutableBlockPos mPos = new MutableBlockPos(pos);
		DeadlyModule.LOGGER.info("Marking! " + pos.toString());
		while (mPos.getY() < 127)
			world.setBlockState(mPos.setPos(mPos.getX(), mPos.getY() + 1, mPos.getZ()), Blocks.GLASS.getDefaultState());
	}

	public static void init() {
		if (BRUTAL_SPAWNER.isEnabled()) FEATURES.add(BRUTAL_SPAWNER);
		if (SWARM_SPAWNER.isEnabled()) FEATURES.add(SWARM_SPAWNER);
		WorldFeature f = new BossFeature();
		if (f.isEnabled()) FEATURES.add(f);
	}
}