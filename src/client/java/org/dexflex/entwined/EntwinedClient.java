package org.dexflex.entwined;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.particle.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;

public class EntwinedClient implements ClientModInitializer {

	public static final Identifier VINE_BASE_TEXTURE = new Identifier(Entwined.MOD_ID, "particle/vine_base");
	public static final Identifier DEAD_VINE_TEXTURE = new Identifier(Entwined.MOD_ID, "particle/dead_vine");
	public static final Identifier VINE_LEAF_TEXTURE = new Identifier(Entwined.MOD_ID, "particle/vine_leaf");

	@Override
	public void onInitializeClient() {
		ClientSpriteRegistryCallback.event(SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
			registry.register(VINE_BASE_TEXTURE);
			registry.register(DEAD_VINE_TEXTURE);
			registry.register(DEAD_VINE_TEXTURE);
			registry.register(VINE_LEAF_TEXTURE);
		});

		ParticleFactoryRegistry.getInstance().register(ModParticles.VINE_BASE, VineBaseParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.DEAD_VINE, DeadVineParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.DEAD_VINE_SHORT, DeadVineShortParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.VINE_LEAF, VineLeafParticle.Factory::new);
	}

	// Base vine particle
	public static class VineBaseParticle extends SpriteBillboardParticle {
		private final SpriteProvider spriteProvider;

		protected VineBaseParticle(ClientWorld world, double x, double y, double z,
								   double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
			super(world, x, y, z, 0, 0, 0);
			this.spriteProvider = spriteProvider;
			this.maxAge = 1;
			this.gravityStrength = 0f;
			this.collidesWithWorld = false;
			this.setSpriteForAge(spriteProvider);
			this.setVelocity(0, 0, 0);
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
		}

		@Override
		public void tick() {
			super.tick();
			this.setSpriteForAge(spriteProvider);
		}

		public static class Factory implements ParticleFactory<DefaultParticleType> {
			private final SpriteProvider spriteProvider;

			public Factory(SpriteProvider spriteProvider) {
				this.spriteProvider = spriteProvider;
			}

			@Override
			public Particle createParticle(DefaultParticleType parameters, ClientWorld world,
										   double x, double y, double z,
										   double velocityX, double velocityY, double velocityZ) {
				return new VineBaseParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
			}
		}
	}

	// Long-lived Dead Vine particle (stationary)
	public static class DeadVineParticle extends SpriteBillboardParticle {
		private final SpriteProvider spriteProvider;

		protected DeadVineParticle(ClientWorld world, double x, double y, double z,
								   double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
			super(world, x, y, z, 0, 0, 0);
			this.spriteProvider = spriteProvider;
			this.maxAge = 10000; // very long life
			this.gravityStrength = 0f;
			this.collidesWithWorld = false;
			this.setSpriteForAge(spriteProvider);
			this.angle = this.random.nextFloat() * 360.0F;
			this.prevAngle = this.angle;
			this.setVelocity(0, 0, 0);
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
		}

		@Override
		public void tick() {
			super.tick();
			this.setSpriteForAge(spriteProvider);
		}

		public static class Factory implements ParticleFactory<DefaultParticleType> {
			private final SpriteProvider spriteProvider;

			public Factory(SpriteProvider spriteProvider) {
				this.spriteProvider = spriteProvider;
			}

			@Override
			public Particle createParticle(DefaultParticleType parameters, ClientWorld world,
										   double x, double y, double z,
										   double velocityX, double velocityY, double velocityZ) {
				return new DeadVineParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
			}
		}
	}

	// Short-lived Dead Vine particle (1 tick lifetime)
	public static class DeadVineShortParticle extends SpriteBillboardParticle {
		private final SpriteProvider spriteProvider;

		protected DeadVineShortParticle(ClientWorld world, double x, double y, double z,
										double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
			super(world, x, y, z, velocityX, velocityY, velocityZ);
			this.spriteProvider = spriteProvider;
			this.maxAge = 1; // lives only 1 tick (instant)
			this.gravityStrength = 0f;
			this.collidesWithWorld = false;
			this.setSpriteForAge(spriteProvider);
			this.angle = this.random.nextFloat() * 360.0F;
			this.prevAngle = this.angle;
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
		}

		@Override
		public void tick() {
			super.tick();
			this.setSpriteForAge(spriteProvider);
		}

		public static class Factory implements ParticleFactory<DefaultParticleType> {
			private final SpriteProvider spriteProvider;

			public Factory(SpriteProvider spriteProvider) {
				this.spriteProvider = spriteProvider;
			}

			@Override
			public Particle createParticle(DefaultParticleType parameters, ClientWorld world,
										   double x, double y, double z,
										   double velocityX, double velocityY, double velocityZ) {
				return new DeadVineShortParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
			}
		}
	}

	// Vine leaf particle
	public static class VineLeafParticle extends SpriteBillboardParticle {
		private final SpriteProvider spriteProvider;
		private float angularVelocity; // degrees per tick

		protected VineLeafParticle(ClientWorld world, double x, double y, double z,
								   double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
			super(world, x, y, z, velocityX, velocityY, velocityZ);
			this.angularVelocity = (this.random.nextFloat() * .1f - .05f) * 2;
			this.spriteProvider = spriteProvider;
			this.maxAge = this.random.nextInt(25) + 75;
			this.gravityStrength = 0.1f;
			this.collidesWithWorld = false;
			this.setSpriteForAge(spriteProvider);
			this.scale(this.random.nextFloat() * .5f + .5f);
			this.angle = this.random.nextFloat() * 360.0F;
			this.prevAngle = this.angle;
			this.setVelocity(0, 0, 0);
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
		}

		@Override
		public void tick() {
			super.tick();
			this.prevAngle = this.angle;
			this.angle += this.angularVelocity;
			this.setSpriteForAge(spriteProvider);
		}

		public static class Factory implements ParticleFactory<DefaultParticleType> {
			private final SpriteProvider spriteProvider;

			public Factory(SpriteProvider spriteProvider) {
				this.spriteProvider = spriteProvider;
			}

			@Override
			public Particle createParticle(DefaultParticleType parameters, ClientWorld world,
										   double x, double y, double z,
										   double velocityX, double velocityY, double velocityZ) {
				return new VineLeafParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
			}
		}
	}
}
