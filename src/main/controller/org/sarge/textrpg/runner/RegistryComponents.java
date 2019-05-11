package org.sarge.textrpg.runner;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.sarge.lib.xml.Element;
import org.sarge.lib.xml.Element.ElementException;
import org.sarge.lib.xml.ElementLoader;
import org.sarge.textrpg.common.Liquid;
import org.sarge.textrpg.common.LiquidLoader;
import org.sarge.textrpg.common.Skill;
import org.sarge.textrpg.common.SkillLoader;
import org.sarge.textrpg.entity.CalculationLoader;
import org.sarge.textrpg.entity.Race;
import org.sarge.textrpg.entity.RaceLoader;
import org.sarge.textrpg.object.ArchetypeLoader;
import org.sarge.textrpg.object.Material;
import org.sarge.textrpg.object.MaterialLoader;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.object.ObjectDescriptorLoader;
import org.sarge.textrpg.util.Calendar;
import org.sarge.textrpg.util.DataSource;
import org.sarge.textrpg.util.Registry;
import org.sarge.textrpg.util.RegistryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registry components.
 * @author Sarge
 */
@Configuration
public class RegistryComponents {
	private static final Logger LOG = LoggerFactory.getLogger(RegistryComponents.class);

	private final ElementLoader xmlLoader = new ElementLoader();

	private final DataSource src;

	/**
	 * Constructor.
	 * @param src Data-source
	 */
	public RegistryComponents(DataSource src) {
		this.src = src.folder("data");
	}

	@Bean
	public RegistryLoader<Liquid> liquids(LiquidLoader loader) throws IOException {
		LOG.info("Loading liquids");
		final Registry.Builder<Liquid> builder = load(loader::load, Liquid::name, "liquids.xml");
		builder.add(Liquid.WATER);
		builder.add(Liquid.OIL);
		return new RegistryLoader<>(builder.build(), loader::load, "liquid");
	}

	@Bean
	public RegistryLoader<Material> materials(MaterialLoader loader) throws IOException {
		LOG.info("Loading materials");
		final Registry<Material> registry = load(loader::load, Material::name, "materials.xml").build();
		return new RegistryLoader<>(registry, loader::load, "material");
	}

	@Bean
	public Registry<Skill> skills() throws IOException {
		LOG.info("Loading skills");
		final SkillLoader loader = new SkillLoader(new CalculationLoader());
		final Element xml = xmlLoader.load(src.open("skill.xml"));
		xml.children("skill").forEach(loader::load);
		return loader.registry();
	}

	@Bean
	public Registry<ObjectDescriptor> descriptors(ArchetypeLoader loader) throws IOException {
		LOG.info("Loading pre-defined object descriptors");
		return load(xml -> loader.load(xml, ObjectDescriptorLoader.Policy.ANY), ObjectDescriptor::name, "objects.xml").build();
	}

	@Bean
	public Registry<Race> races(RaceLoader loader) throws IOException {
		LOG.info("Loading racial descriptors");
		return load(loader::load, Race::name, "races.xml").build();
	}

	@Bean
	public Registry<Calendar> calendars(List<Calendar> calendars) {
		final var builder = new Registry.Builder<>(Calendar::name);
		calendars.forEach(builder::add);
		return builder.build();
	}

	/**
	 * Loads a registry from an XML file.
	 * @param loader		Loader for an entry
	 * @param mapper		Extracts the name from an entry
	 * @param filename		XML filename
	 * @param <T> Registry type
	 * @return Registry builder
	 * @throws IOException if the registry cannot be loaded
	 */
	private <T> Registry.Builder<T> load(Function<Element, T> loader, Function<T, String> mapper, String filename) throws IOException {
		final Element xml = xmlLoader.load(src.open(filename));
		final Registry.Builder<T> builder = new Registry.Builder<>(mapper);
		try {
			xml.children().map(loader).forEach(builder::add);
		}
		catch(ElementException e) {
			e.setFile(filename);
			throw e;
		}
		return builder;
	}
}
