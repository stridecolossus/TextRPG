package org.sarge.textrpg.runner;

import java.util.Set;
import java.util.function.Consumer;

import org.sarge.lib.collection.StrictSet;
import org.sarge.textrpg.common.Alignment;
import org.sarge.textrpg.common.Gender;
import org.sarge.textrpg.common.Relationship;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.entity.EntityValueController;
import org.sarge.textrpg.entity.PlayerCharacter;
import org.sarge.textrpg.entity.PlayerCharacter.PlayerEntityDescriptor;
import org.sarge.textrpg.entity.PlayerCharacter.PlayerModel;
import org.sarge.textrpg.entity.PlayerSettings.Setting;
import org.sarge.textrpg.entity.StarterArea;
import org.sarge.textrpg.util.Event;
import org.sarge.textrpg.util.ServiceComponent;
import org.sarge.textrpg.world.Faction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty("console.mode")
public class ConsoleSession implements ServiceComponent {
	/////////////
	@Autowired private SessionManager manager;
	@Value("#{skills.get('common')}") private Skill common;
	@Autowired private Event.Queue.Manager queueManager;
	@Autowired private Set<StarterArea> starters;
	@Autowired private EntityValueController init;
	@Autowired private PlayScreen play;
	/////////////

	@Override
	public void start() {
		final Event.Queue queue = queueManager.queue("player");

		final StarterArea starter = starters.iterator().next();

		final Set<Skill> skills = new StrictSet<>();
		skills.add(common);
		final PlayerModel model = new PlayerModel(new Faction.Association(starter.faction(), Relationship.FRIENDLY));

		final Connection con = new ConsoleConnection();
		final Session session = new Session(con, play);
		final Connection.Listener listener = manager.add(session);

		final Consumer<Response> handler = response -> play.display(session, response);

		final PlayerEntityDescriptor descriptor = new PlayerEntityDescriptor(starter.race(), "player", starter.race().characteristics().attributes(), Gender.FEMALE, Alignment.EVIL, starter.faction());
		final PlayerCharacter player = new PlayerCharacter(descriptor, queue, handler, model);

		player.parent(starter.location());
		init.init(player);
		player.settings().modify(Setting.CASH, 123);

		session.set(player);
		play.init(session);
		con.start(listener);
	}
}
