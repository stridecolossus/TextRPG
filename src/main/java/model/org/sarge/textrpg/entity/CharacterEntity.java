package org.sarge.textrpg.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.Check;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.Topic;
import org.sarge.textrpg.util.IntegerMap;

/**
 * Character entity.
 * @author Sarge
 */
public class CharacterEntity extends Creature {
	private final String name;
	private final Gender gender;
	private final Alignment align;
	private final Collection<Topic> topics;
	private final List<Entity> followers = new StrictList<>();

	private CharacterEntity following;
	private Optional<Entity> mount = Optional.empty();
	
	/**
	 * Constructor.
	 * @param name		Character name
	 * @param race		Race
	 * @param weight	Weight
	 * @param attrs		Attributes
	 * @param gender	Gender
	 * @param align		Alignment
	 * @param topics	Conversation topics
	 */
	public CharacterEntity(String name, Race race, IntegerMap<Attribute> attrs, EntityManager manager, Gender gender, Alignment align, Collection<Topic> topics) {
		super(race, attrs, manager);
		Check.notEmpty(name);
		Check.notNull(gender);
		Check.notNull(align);
		Check.notNull(topics);
		this.name = name;
		this.gender = gender;
		this.align = align;
		this.topics = new HashSet<>(topics);
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Gender getGender() {
		return gender;
	}
	
	@Override
	public Alignment getAlignment() {
		return align;
	}
	
	@Override
	protected String getDescriptionKey() {
		return "character";
	}
	
// TODO
//	@Override
//	public Description describe() {
//		final Description desc = super.describe();
//		desc.add("mount", mount);
//		return desc;
//	}
	
	@Override
	public Stream<Topic> getTopics() {
		return topics.stream();
	}
	
	@Override
	public Stream<Entity> getFollowers() {
		return followers.stream();
	}
	
	@Override
	public boolean isFollowing(Entity e) {
		return this.following == e;
	}
	
	@Override
	protected void follow(Entity e) throws ActionException {
		if(e == null) {
			// Stop following
			if(this.following == null) throw new ActionException("follow.not.following");
			this.following.followers.remove(this);
			this.following = null;
		}
		else
		if(e instanceof CharacterEntity) {
			// Follow character
			if(e == this) throw new IllegalArgumentException("Cannot follow self");
			followCharacter((CharacterEntity) e);
		}
		else {
			throw new ActionException("follow.entity.invalid");
		}
	}
	
	/**
	 * Follows the given character.
	 */
	private void followCharacter(CharacterEntity ch) throws ActionException {
		// Check can follow this character
		if((ch.getAlignment() != this.align) && (ch.getAlignment() != Alignment.NEUTRAL)) {
			throw new ActionException("follow.entity.invalid");
		}

		// Stop previous following
		if(this.following != null) {
			this.following.followers.remove(this);
		}
		
		// Start following
		ch.followers.add(this);
		this.following = ch;
	}
	
	/**
	 * @return Current mount
	 */
	public Optional<Entity> getMount() {
		return mount;
	}
	
	/**
	 * Sets the mount of this character.
	 * @param mount Mount or <tt>null</tt> to dismount
	 * @throws ActionException
	 */
	protected void setMount(Entity mount) throws ActionException {
		if(mount == null) {
			if(!this.mount.isPresent()) throw new ActionException("dismount.not.mounted");
			setStance(Stance.DEFAULT);
		}
		else {
			if(this.mount.isPresent()) throw new ActionException("mount.already.mounted");
			if(!mount.getRace().getAttributes().isMount()) throw new ActionException("mount.invalid.mount");
			if(!mount.isFollowing(this)) throw new ActionException("mount.not.leading");
			setStance(Stance.MOUNTED);
		}
		this.mount = Optional.ofNullable(mount);
	}
}
