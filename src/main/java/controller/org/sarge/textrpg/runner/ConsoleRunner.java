package org.sarge.textrpg.runner;

import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;

import org.sarge.lib.collection.LoopIterator;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.Util;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.ActionException;
import org.sarge.textrpg.common.ActionResponse;
import org.sarge.textrpg.common.Clock;
import org.sarge.textrpg.common.Description;
import org.sarge.textrpg.common.DescriptionStore.Repository;
import org.sarge.textrpg.common.Message;
import org.sarge.textrpg.common.TimeCycle;
import org.sarge.textrpg.common.ToggleListener;
import org.sarge.textrpg.entity.Alignment;
import org.sarge.textrpg.entity.Attribute;
import org.sarge.textrpg.entity.EntityValue;
import org.sarge.textrpg.entity.EntityValueCalculator;
import org.sarge.textrpg.entity.Gender;
import org.sarge.textrpg.entity.Induction;
import org.sarge.textrpg.entity.MovementController;
import org.sarge.textrpg.entity.Player;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.entity.SkillSet;
import org.sarge.textrpg.entity.Stance;
import org.sarge.textrpg.loader.MainLoader;
import org.sarge.textrpg.loader.World;
import org.sarge.textrpg.object.Light;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.Config;
import org.sarge.textrpg.util.DataTable;
import org.sarge.textrpg.util.DataTableCalculator;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.world.PostManager;
import org.sarge.textrpg.world.Route;
import org.sarge.textrpg.world.Terrain;

public class ConsoleRunner {

	static {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("resources/log.config.properties"));
		}
		catch(final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final CommandParser parser;
	private final Player player;
	private final Device dev;

	public ConsoleRunner(CommandParser parser, Player player, Device dev) {
		this.parser = parser;
		this.player = player;
		this.dev = dev;
	}

	private static class Spinner implements Runnable {
		private final Iterator<String> icon = new LoopIterator<>(Arrays.asList(new String[]{"\\", "|", "/", "-"}));
		private final PrintWriter out;

		private boolean running;

		public Spinner(PrintWriter out) {
			this.out = out;
		}

//		public boolean isRunning() {
//			return running;
//		}

		@Override
		public void run() {
			running = true;
			while(running) {
				out.print(".");
				//out.flush();
				//out.print(icon.next());
				out.flush();

				Util.kip(500);
			}
		}

//		public void start() {
//			running = true;
//		}

		public void stop() {
			running = false;
		}
	}

	public static class Device {
		private final BufferedReader in;
		private final PrintWriter out;
		private final Spinner spinner;
		private Thread thread;

		public Device(InputStream in, OutputStream out) {
			this.in = new BufferedReader(new InputStreamReader(in));
			this.out = new PrintWriter(out, false);
			this.spinner = new Spinner(this.out);
		}

		public String readLine() throws IOException {
//			out.flush();
			return in.readLine();
		}

		public void write(String str) {
//			out.flush();
			out.print(str);
			out.flush();
		}

		public void setInduction(boolean induction) {
			if(induction) {
				if(thread == null) {
					thread = new Thread(spinner);
					thread.setDaemon(true);
					thread.start();
				}
			}
			else {
				spinner.stop();
				thread = null;
			}
		}
	}

	public void run() {
		while(true) {
			try {
				final Induction induction = player.getInduction();
				dev.write("\n");
				if(induction == null) {
					dev.write(">");
				}
				else {
					dev.setInduction(true);
				}
				final String line = dev.readLine();

				// Parse command
				final Command cmd = parser.parse(player, line);

				if(player.getInduction() != null) {
					dev.setInduction(false);
					player.interrupt();
				}

				// Execute command
				final ActionResponse response = cmd.execute(player, true);  // TODO - daylight

				// Display response
				// TODO
				if(response != null) {
					response.getDescriptions().forEach(desc -> player.handler().handle(desc.toNotification()));
					response.getInduction().ifPresent(i -> {
						final Induction intercept = new Induction() {
							@Override
							public Description complete() throws ActionException {
								stop();
								return i.complete();
							}

							@Override
							public void interrupt() {
								stop();
								i.interrupt();
							}

							private void stop() {
								dev.setInduction(false);
								dev.write(">");
							}
						};
						player.start(intercept, response.getDuration(), response.isRepeating());
					});
				}

				// Update previous object
				final WorldObject obj = cmd.getPreviousObject();
				if(obj != null) {
					player.setPreviousObject(obj);
				}
			}
			catch(final ActionException e) {
				// TODO
				player.handler().handle(Message.of(e));
				//dev.write("ERROR " + e.getMessage());
			}
			catch(final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {


		// TODO - load from file, ditto for length-of-game-hour?
		Clock.CLOCK.setDateTime(LocalDateTime.of(3019, 6, 1, 0, 0));

		final DataTable cycleTable = DataTable.load(new FileReader("resources/day.night.txt"));
		final List<TimeCycle.Period> periods = cycleTable.getRows().map(TimeCycle.PERIOD_MAPPER).collect(toList());
		final TimeCycle cycle = new TimeCycle(periods, 0);

		final DataTable terrain = DataTable.load(new FileReader("resources/terrain.txt"));
		final DataTable route = DataTable.load(new FileReader("resources/route.txt"));
		final DataTable stance = DataTable.load(new FileReader("resources/stance.txt"));
		@SuppressWarnings("unchecked")
		final DataTableCalculator move = new DataTableCalculator(new Map[]{
			terrain.getColumn("move", Terrain.class, Converter.INTEGER),
			route.getColumn("move", Route.class, Converter.FLOAT),
			stance.getColumn("move", Stance.class, Converter.FLOAT),
		});
		@SuppressWarnings("unchecked")
		final DataTableCalculator tracks = new DataTableCalculator(new Map[]{
				terrain.getColumn("tracks", Terrain.class, Percentile.CONVERTER),
				route.getColumn("tracks", Route.class, Percentile.CONVERTER),
				stance.getColumn("tracks", Stance.class, Percentile.CONVERTER),
		});
		final long lifetime = Duration.ofHours(1).toMillis();
		final MovementController mover = new MovementController(move, tracks, lifetime);

		// TODO - load
		final EntityValueCalculator calc = new EntityValueCalculator();
		calc.add(EntityValue.STAMINA, Attribute.ENDURANCE, 100);
		calc.add(EntityValue.POWER, Attribute.WILL, 5);

		final ActionContext ctx = new ActionContext(cycle, calc, mover, new PostManager());

		final MainLoader loader = new MainLoader(new File("resources"), ctx);
		final World world = loader.load();

		Clock.CLOCK.add(new ToggleListener(Light::toggleLampPosts, new int[]{7, 18}));

		final Properties cfg = new Properties();
		cfg.load(new FileInputStream(new File("resources/action.config.properties")));

		Player.repo = new Repository();
		Player.repo.load(new File("resources/common"));
		Player.repo.load(new File("resources/world"));

		final ActionsBuilder builder = new ActionsBuilder();
		final List<AbstractAction> actions = builder.build(new Config(cfg), world.getSkills(), world.getDescriptors());
		final CommandParser parser = new CommandParser(actions, Collections.emptySet(), Player.repo.find(Locale.UK));

		final SkillSet skills = new SkillSet();
		skills.increment(world.getSkills().find("ride"), 1);
		skills.increment(world.getSkills().find("wilderness.lore"), 1);
		skills.increment(world.getSkills().find("pick.lock"), 1);
		skills.increment(world.getSkills().find("sneak"), 1);
		skills.increment(world.getSkills().find("butcher"), 1);
		skills.increment(world.getSkills().find("fish"), 1);
		skills.increment(world.getSkills().find("swim"), 1);
		skills.increment(world.getSkills().find("cook"), 1);

		final Race race = new Race.Builder("man")
			.attribute(Attribute.ENDURANCE, 25)
			.attribute(Attribute.STRENGTH, 25)
			.attribute(Attribute.PERCEPTION, 100)
			.skills(skills)
			.build();

		final Device dev = new Device(System.in, System.out);

		final Player player = new Player("player", race, race.attributes().attributes(), Gender.MALE, Alignment.GOOD, dev);
		calc.init(player);

		// TODO
		final Clock.Listener listener = hour -> {
			final TimeCycle.Period prev = cycle.getPeriod();
			cycle.update(hour);
			if(prev != cycle.getPeriod()) {
				player.handler().handle(new Message("time." + cycle.getPeriod().getKey()));
			}
		};
		Clock.CLOCK.add(listener);

		//player.setParent(world.getLocations().find("bag.end.garden"));
		//player.setParent(world.getLocations().find("pony.reception"));
		//player.setParent(world.getLocations().find("shire.south.road.junction"));
		player.setParent(world.getLocations().find("durins.threshold"));
		//player.setParent(world.getLocations().find("stock"));
		//player.setParent(world.getLocations().find("hobbiton.centre"));
		//player.setParent(world.getLocations().find("combe"));

		final WorldRunner runner = new WorldRunner(ctx);
		runner.start();

		final ConsoleRunner console = new ConsoleRunner(parser, player, ctx, dev);
		console.run();
	}
}
