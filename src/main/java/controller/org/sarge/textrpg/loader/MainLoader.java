package org.sarge.textrpg.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.textrpg.common.ActionContext;
import org.sarge.textrpg.common.Size;
import org.sarge.textrpg.entity.RaceLoader;
import org.sarge.textrpg.object.ScriptLoader;
import org.sarge.textrpg.util.DataTable;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.TableConverter;
import org.sarge.textrpg.util.TextNode;
import org.sarge.textrpg.util.TextParser;
import org.sarge.textrpg.world.Area;

/**
 * World loader.
 * @author Sarge
 */
public class MainLoader {
	private final TextParser loader = new TextParser();

	private final File root;
	private final ActionContext ctx;
	
	public MainLoader(File root, ActionContext ctx) {
		if(!root.isDirectory()) throw new IllegalArgumentException("Invalid root directory");
		Check.notNull(ctx);
		this.root = root;
		this.ctx = ctx;
	}

	/**
	 * Loads XML.
	 * @param file Filename
	 * @return Element
	 */
	private TextNode open(String file) {
		try {
			return loader.parse(new BufferedReader(new FileReader(root.getPath() + File.separator + file + ".txt")));
		}
		catch(IOException e) {
			throw new RuntimeException("Error opening file: " + file, e);
		}
	}
	
	/**
	 * Loads the world.
	 * @throws IOException
	 */
	public World load() throws IOException {
		final World world = new World(ctx);
		
		// Init loaders
		final ConditionLoader conditionLoader = new ConditionLoader();
		final ScriptLoader scriptLoader = new ScriptLoader(world, conditionLoader);
		
		// Load liquid descriptors
		final LiquidLoader liquidLoader = new LiquidLoader();
		liquidLoader.loadAll(open("liquids"));

		// Load skill definitions
		final SkillsLoader skillsLoader = new SkillsLoader(conditionLoader);
		open("skills").children().map(skillsLoader::load).forEach(world.getSkills()::add);
		final SkillSetLoader skillSetLoader = new SkillSetLoader(world.getSkills());
		
		// Init table converters
		final DataTable descriptorTable = DataTable.load(new FileReader("resources/objects.txt"));
		final DataTable sizeTable = DataTable.load(new FileReader("resources/size.txt"));
		final DataTable difficultyTable = DataTable.load(new FileReader("resources/difficulty.txt"));

		final Converter<Percentile> difficulty = new TableConverter<>(Percentile.CONVERTER, difficultyTable.getColumn("difficulty", Percentile.CONVERTER));

		// Load pre-defined object descriptors
		final ObjectDescriptorLoader descriptorLoader = new ObjectDescriptorLoader(conditionLoader);
		descriptorLoader.setWeightConverter(new TableConverter<>(Converter.INTEGER, sizeTable.getColumn("weight", Converter.INTEGER, true)));
		descriptorLoader.setValueConverter(new TableConverter<>(Converter.INTEGER, descriptorTable.getColumn("value", Converter.INTEGER)));
		descriptorLoader.setArmourConverter(new TableConverter<>(Converter.INTEGER, descriptorTable.getColumn("armour", Converter.INTEGER)));
		descriptorLoader.setStrengthConverter(new TableConverter<>(Converter.INTEGER, descriptorTable.getColumn("strength", Converter.INTEGER)));
		descriptorLoader.setLightConverter(new TableConverter<>(Percentile.CONVERTER, descriptorTable.getColumn("light", Percentile.CONVERTER)));
		
		final ObjectLoader objectLoader = new ObjectLoader(descriptorLoader, scriptLoader, liquidLoader);
		objectLoader.setDifficultyConverter(difficulty);
		objectLoader.setDurabilityConverter(new TableConverter<>(Converter.INTEGER, descriptorTable.getColumn("durability", Converter.INTEGER)));
		open("objects").children().map(objectLoader::load).forEach(world.getDescriptors()::add);

		// Create loot-factory loader
		final ObjectLoaderAdapter adapter = new ObjectLoaderAdapter(objectLoader, world.getDescriptors(), world.getObjects());
		final LootFactoryLoader lootLoader = new LootFactoryLoader(adapter);
		
		// Load racial descriptors
		final Map<Size, Integer> weight = DataTable.load(new FileReader("resources/size.txt")).getColumn("weight", Size.class, Converter.INTEGER);
		final RaceLoader raceLoader = new RaceLoader(skillSetLoader, adapter, lootLoader);
		open("races").children().map(raceLoader::load).forEach(world.getRaces()::add);
		final EntityLoader entityLoader = new EntityLoader(world, weight, adapter, scriptLoader);
		
		// Load world
		final LocationLoader locationLoader = new LocationLoader(adapter, lootLoader, entityLoader, world);
		final LinkLoader linkLoader = new LinkLoader(adapter, world);
		linkLoader.setDifficultyConverter(difficulty);
		
		final AreaLoader areaLoader = new AreaLoader(locationLoader, linkLoader, world);
		final Path path = Paths.get("resources", "world").toAbsolutePath();
		areaLoader.load(path, Area.ROOT);
		
		return world;
	}
}
