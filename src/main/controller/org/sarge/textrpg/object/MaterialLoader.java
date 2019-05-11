package org.sarge.textrpg.object;

import org.sarge.lib.xml.Element;
import org.sarge.textrpg.common.Damage;
import org.sarge.textrpg.common.Emission;
import org.sarge.textrpg.util.LoaderHelper;
import org.springframework.stereotype.Service;

/**
 * Loader for materials.
 * @author Sarge
 */
@Service
public class MaterialLoader {
	/**
	 * Loads a material descriptor.
	 * @param xml XML
	 * @return Material
	 */
	public Material load(Element xml) {
		// Start material
		final String name = xml.attribute("name").toText();
		final Material.Builder builder = new Material.Builder(name);

		// Load material properties
		builder.strength(xml.attribute("strength").toInteger(0));
		if(xml.attribute("floats").optional().isPresent()) {
			builder.floats();
		}

		// Load damage-types
		LoaderHelper.enumeration(xml, "damaged", Damage.Type.CONVERTER).forEach(builder::damaged);

		// Load transparencies
		LoaderHelper.enumeration(xml, "transparent", Emission.CONVERTER).forEach(builder::transparent);

		// Build material
		return builder.build();
	}
}
