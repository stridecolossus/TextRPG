package org.sarge.textrpg.object;

import java.util.Optional;

import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Actor;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.common.Event;
import org.sarge.textrpg.common.EventQueue;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.util.Percentile;

/**
 * Light source.
 * @author Sarge
 */
public class Light extends WorldObject {
	/**
	 * Lights event queue.
	 */
	public static final EventQueue QUEUE = new EventQueue();

	/**
	 * Lamp-posts.
	 */
	private static boolean LAMP_POSTS_LIT = true;
	
	/**
	 * Global lamp-post toggle.
	 * @param snuff Snuff or light
	 */
	public static void toggleLampPosts(boolean snuff) {
		Light.LAMP_POSTS_LIT = !snuff;
	}

	/**
	 * Light operations.
	 */
	public static enum Operation {
		LIGHT {
			@Override
			protected void execute(Light light, long time) throws ActionException {
				// Check can be lit
				if(light.lit) throw new ActionException("light.already.lit");
				if(light.lifetime == 0) throw new ActionException("light.lit.expired");
				assert light.lifetime > 0;
				
				// Light
				light.lit = true;
				light.start = time;
				
				// Register expiry event
				final Event event = () -> {
					light.lit = false;
					light.lifetime = 0;
					light.alert("light.expired");
				};
				final EventQueue.Entry entry = QUEUE.add(event, light.lifetime);
				light.expiry.set(entry);
				
				// Register warning event
				final long when = time + (long) (light.lifetime * 0.9f);
				final Event warning = () -> light.alert("light.expire.warning");
				final EventQueue.Entry warnEntry = QUEUE.add(warning, when);
				light.warning.set(warnEntry);
			}
		},
		
		SNUFF {
			@Override
			protected void execute(Light light, long time) throws ActionException {
				if(!light.lit) throw new ActionException("snuff.not.lit");
				light.lit = false;
				light.lifetime -= time - light.start;
				light.expiry.cancel();
				light.warning.cancel();
			}
		},

		COVER {
			@Override
			protected void execute(Light light, long time) throws ActionException {
				if(!light.isLantern()) throw new ActionException("cover.not.lantern");
				if(light.covered) throw new ActionException("cover.already.covered");
				light.covered = true;
			}
		},
		
		UNCOVER {
			@Override
			protected void execute(Light light, long time) throws ActionException {
				if(!light.isLantern()) throw new ActionException("uncover.not.lantern");
				if(!light.covered) throw new ActionException("uncover.not.covered");
				light.covered = false;
			}
		};
		
		/**
		 * Performs this operation on the given light.
		 * @param ctx		Context
		 * @param time		Current time
		 * @throws ActionException if the operation cannot be performed
		 */
		protected abstract void execute(Light light, long time) throws ActionException;
	}

	/**
	 * Light sub-types.
	 * @author Sarge
	 */
	public enum Type {
		GENERAL,
		LANTERN,
		CAMPFIRE,
		LAMP_POST
	}
	
	/**
	 * Light descriptor.
	 */
	public static class Descriptor extends ObjectDescriptor {
		private final long lifetime;
		private final Type type;

		/**
		 * Constructor.
		 * @param descriptor		Object descriptor
		 * @param lifetime			Maximum lifetime (minutes)
		 * @param type				Type of light
		 * @throws IllegalArgumentException if the object descriptor does not specify a {@link Emission.Type#LIGHT} emission
		 */
		public Descriptor(ObjectDescriptor descriptor, long lifetime, Type type) {
			super(descriptor);
			if(!descriptor.getCharacteristics().getEmission(Emission.Type.LIGHT).isPresent()) {
				throw new IllegalArgumentException("Missing light emission");
			}
			Check.oneOrMore(lifetime);
			Check.notNull(type);
			this.lifetime = lifetime;
			this.type = type;
		}
		
		@Override
		public String getDescriptionKey() {
			return "light";
		}
		
		@Override
		public WorldObject create() {
			return new Light(this);
		}
	}

	private final Type type;
	private final Event.Holder expiry = new Event.Holder();
	private final Event.Holder warning = new Event.Holder();
	
	private long lifetime;
	private long start;

	private boolean lit;
	private boolean covered;
	
	/**
	 * Constructor.
	 * @param descriptor Light descriptor
	 */
	public Light(Descriptor descriptor) {
		super(descriptor);
		this.lifetime = descriptor.lifetime;
		this.type = descriptor.type;
		if(descriptor.type == Type.CAMPFIRE) lit = true;
	}
	
	@Override
	public int getWeight() {
		if(isLantern()) {
			return super.getWeight() + (int) lifetime;
		}
		else {
			return super.getWeight();
		}
	}
	
	@Override
	public Percentile getVisibility() {
		if(isLit() && !covered) {
			return Percentile.ONE;
		}
		else {
			return super.getVisibility();
		}
	}
	
	@Override
	public Optional<Emission> getEmission(Emission.Type type) {
		switch(type) {
		case LIGHT:
		case SMOKE:
			if(!isLit() || covered) {
				return Optional.empty();
			}
			break;
		}

		return super.getEmission(type);
	}

	@Override
	protected String getFullDescriptionKey() {
		if(lit && (type == Type.CAMPFIRE)) {
			return super.getFullDescriptionKey() + ".lit";
		}
		else {
			return super.getFullDescriptionKey();
		}
	}

	@Override
	protected void describe(Description.Builder description) {
		if(covered) {
			description.wrap("light.covered", "light.covered");
		}
		else
		if(isLit()) {
			description.wrap("light.lit", "light.lit");
		}
		description.add("light.lifetime", lifetime);
	}

	/**
	 * @return Whether this light is a lantern
	 * @see Type#LANTERN
	 */
	public boolean isLantern() {
		return type == Type.LANTERN;
	}

	/**
	 * @return Remaining lifetime of this light
	 */
	public long getLifetime() {
		return lifetime;
	}

	/**
	 * @return Whether this light is lit
	 */
	public boolean isLit() {
		if(type == Type.LAMP_POST) {
			return LAMP_POSTS_LIT;
		}
		else {
			return lit;
		}
	}
	
	/**
	 * @return Whether this light is covered
	 */
	public boolean isCovered() {
		return covered;
	}

	/**
	 * Performs the given operation on this light.
	 * @param op		Operation
	 * @param time		Current time
	 * @throws ActionException if the operation cannot be performed
	 */
	// TODO - public
	public void execute(Operation op, long time) throws ActionException {
		if(type == Type.LAMP_POST) throw new ActionException("light.lamp.post");
		op.execute(this, time);
		assert lifetime >= 0;
	}
	
	/**
	 * Sends an alert to the owner of this light.
	 * @param message Message
	 */
	private void alert(String message) {
		if(!isDead()) {
			final Actor owner = (Actor) getOwner();
			if(owner != null) {
				owner.alert(new Message(message, this));
			}
		}
	}
	
	/**
	 * Fills this light from the given source receptacle.
	 * @param src Source receptacle
	 * @throws ActionException if this light cannot be re-fueled or is already full, or the source is not oil or is empty
	 * @see Liquid#OIL
	 */
	protected void fill(Receptacle src) throws ActionException {
		// Check this light can be filled
		final Descriptor light = (Descriptor) getDescriptor();
		if(this.type != Type.LANTERN) throw new ActionException("fill.not.lantern");
		if(this.lifetime == light.lifetime) throw new ActionException("light.fill.full");
		
		// Check source can be used
		final Receptacle.Descriptor rec = src.getDescriptor();
		if(rec.getLiquid() != Liquid.OIL) throw new ActionException("light.fill.oil");
		if(src.getLevel() == 0) throw new ActionException("light.fill.empty");

		// Re-fuel light from receptacle
		final int amount = src.consume((int) (light.lifetime - this.lifetime));
		this.lifetime += amount;
		assert (this.lifetime >= 0) && (this.lifetime <= light.lifetime);
	}

	/**
	 * Test helper.
	 */
	protected void empty() {
		lifetime = 0;
	}
	
	@Override
	protected void destroy() {
		this.lit = false;
		this.lifetime = 0;
		super.destroy();
	}
}