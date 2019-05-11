package org.sarge.textrpg.object;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.sarge.lib.util.Check.notNull;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.OpeningTimes;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.InventoryController;
import org.sarge.textrpg.entity.LocationTriggerController;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.PeriodModel;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.FacilityRegistry;
import org.sarge.textrpg.world.Faction;
import org.sarge.textrpg.world.LoaderContext;
import org.sarge.textrpg.world.Location;
import org.springframework.stereotype.Service;

/**
 * Loader for a {@link Shop}.
 * @author Sarge
 */
@Service
public class ShopLoader {
	/**
	 * Location trigger for a repair shop.
	 */
	private static class RepairLocationTrigger implements Entity.LocationTrigger {
		private final RepairShop repair;

		/**
		 * Constructor.
		 * @param repair Repair shop
		 */
		private RepairLocationTrigger(RepairShop repair) {
			this.repair = notNull(repair);
		}

		@Override
		public void trigger(Entity actor) {
			final var repaired = repair.repaired(actor);
			final InventoryController controller = new InventoryController("repair");
			controller.take(actor, repaired);
			// TODO - display
		}
	}
	// TODO - re-use for auction-house

	private final DefaultObjectDescriptorLoader loader;
	private final FacilityRegistry registry;
	private final LocationTriggerController controller;

	/**
	 * Constructor.
	 * @param loader			Descriptor registry/loader
	 * @param registry			Shop registry
	 * @param controller		Location trigger controller for repair shops
	 */
	public ShopLoader(DefaultObjectDescriptorLoader loader, FacilityRegistry registry, LocationTriggerController controller) {
		this.loader = notNull(loader);
		this.registry = notNull(registry);
		this.controller = notNull(controller);
	}

	/**
	 * Loads a shop.
	 * @param xml XML
	 * @param loc Location
	 * @param ctx Context
	 */
	public Shop load(Element xml, Location loc, LoaderContext ctx) {
		// Load stock
		final AtomicInteger index = new AtomicInteger(1);
		final Function<Element, Shop.Stock> loadStock = e -> {
			final int num = e.attribute("num").toInteger(1);
			final ObjectDescriptor descriptor = loader.load(e, ObjectDescriptorLoader.Policy.OBJECT);
			return new Shop.Stock(index.getAndIncrement(), descriptor, num);
		};
		final var stock = xml.children("stock").map(loadStock).collect(toList());

		// Load stock categories
		final var cats = loadCategories(xml);

		// Load optional repair shop
		final RepairShop repair = xml.find("repair").map(ShopLoader::loadCategories).map(RepairShop::new).orElse(null);

		// Create shop
		final Shop shop = new Shop(stock, cats, repair);
		registry.add(loc, shop);

		// Register location trigger for repair shops
		shop.repair().map(RepairLocationTrigger::new).ifPresent(t -> controller.add(loc, t));

		// Register open/close listener
		final String name = xml.attribute("name").toText();
		final PeriodModel<OpeningTimes> times = ctx.faction().map(Faction::opening).orElseThrow(() -> xml.exception("No faction for shop opening-times"));
		final PeriodModel.Listener<OpeningTimes> listener = period -> {
			shop.setOpen(period.isOpen());
			final String key = TextHelper.join("shop", String.valueOf(period.isOpen()));
			final Description alert = new Description(key, name);
			loc.broadcast(null, alert);
		};
		times.add(listener);
		shop.setOpen(times.current().isOpen());

		return shop;
	}

	/**
	 * Loads a set of object categories.
	 * @param xml XML
	 * @return Categories
	 */
	private static Set<String> loadCategories(Element xml) {
		return xml.children("cat").map(Element::text).collect(toSet());
	}
}
