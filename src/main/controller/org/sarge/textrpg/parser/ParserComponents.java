package org.sarge.textrpg.parser;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.object.ObjectDescriptor;
import org.sarge.textrpg.runner.ActionDescriptor;
import org.sarge.textrpg.runner.ActionDescriptorProcessor;
import org.sarge.textrpg.util.DataSource;
import org.sarge.textrpg.util.NameStore;
import org.sarge.textrpg.util.NameStoreLoader;
import org.sarge.textrpg.world.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Parser components.
 * @author Sarge
 */
@Configuration
public class ParserComponents {
	private static final Logger LOG = LoggerFactory.getLogger(ParserComponents.class);

	/**
	 * Generates descriptors for the given actions.
	 * @param actions Actions
	 * @return Descriptors
	 */
	@Bean
	public static List<ActionDescriptor> build(List<AbstractAction> actions) {
		return actions.stream()
			.map(ActionDescriptorProcessor::new)
			.flatMap(ActionDescriptorProcessor::build)
			.peek(action -> LOG.info("Registered action: " + action))
			.sorted(ActionDescriptor.ORDER)
			.collect(toList());
	}

	@Bean
	public ArgumentParser.Registry registry(NumericArgumentParser numeric, List<ArgumentParser<ObjectDescriptor.Filter>> filters) {
		final Map<Class<?>, List<ArgumentParser<?>>> map = new HashMap<>();
		final List<ArgumentParser<?>> copy = List.copyOf(filters);
		map.put(Direction.class, List.of(new EnumArgumentParser<>("direction", Direction.class)));
		map.put(ObjectDescriptor.Filter.class, copy);
		map.put(Integer.class, List.of(numeric));
//		map.put(int.class, List.of(new IntegerArgumentParser()));
		return type -> map.computeIfAbsent(type, ignore -> List.of());
	}

	@Bean
	public NameStore defaultNameStore(DataSource src) throws IOException {
		final NameStoreLoader loader = new NameStoreLoader();
		final DataSource folder = src.folder("stores");
		for(String f : folder.enumerate()) {
			LOG.info("Loading global name-store: " + f);
			loader.load(folder.open(f));
		}
		return loader.build();
	}
}
