package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Set;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ArgumentParser;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.PlayerSettings.Setting;
import org.sarge.textrpg.parser.EnumArgumentParser;
import org.sarge.textrpg.util.ActionException;
import org.sarge.textrpg.util.Description;
import org.sarge.textrpg.util.TextHelper;
import org.sarge.textrpg.world.Terrain;
import org.springframework.stereotype.Component;

/**
 * Action to set player preferences.
 * @author Sarge
 */
@Component
@RequiresActor
public class PreferenceAction extends AbstractAction {
	private static final ArgumentParser.Registry PARSERS = ArgumentParser.Registry.of(Setting.class, new EnumArgumentParser<>("preference", settings()));

	/**
	 * @return Preference settings
	 */
	private static Set<Setting> settings() {
		return Arrays.stream(Setting.values()).filter(Setting::isPreference).collect(toSet());
	}

	/**
	 * Constructor.
	 */
	public PreferenceAction() {
		super(Flag.ACTIVE, Flag.OUTSIDE);
	}

	@Override
	public String prefix() {
		return "pref";
	}

	@Override
	public boolean isInductionValid() {
		return true;
	}

	@Override
	protected boolean isValid(Stance stance) {
		return true;
	}

	@Override
	protected boolean isValid(Terrain terrain) {
		return true;
	}

	@Override
	public ArgumentParser.Registry parsers(Entity actor) {
		return PARSERS;
	}

	/**
	 * Lists player preference settings.
	 * @param actor Actor
	 * @return Preferences
	 */
	public Response list(PlayerCharacter actor) {
		final PlayerSettings settings = actor.settings();
		final var prefs = Arrays.stream(Setting.values()).filter(Setting::isPreference).map(e -> describe(settings, e)).collect(toList());
		return new Response.Builder()
			.add(Description.of("preference.header"))
			.add(prefs)
			.build();
	}

	/**
	 * Describes a preference.
	 */
	private static Description describe(PlayerSettings settings, Setting pref) {
		if(pref.isBoolean()) {
			return new Description(TextHelper.join("preference", pref.name(), settings.toBoolean(pref)));
		}
		else {
			return new Description.Builder(TextHelper.join("preference.integer", pref.name())).add("value", settings.toInteger(pref)).build();
		}
	}

	/**
	 * Toggles a boolean setting.
	 * @param actor			Actor
	 * @param setting		Setting to toggle
	 * @return Response
	 * @throws ActionException if the setting is not a toggle preference
	 * @see Setting#isPreference()
	 * @see Setting#isBoolean()
	 */
	public Response toggle(PlayerCharacter actor, Setting setting) throws ActionException {
		// Verify setting
		if(!setting.isBoolean()) throw ActionException.of("preference.invalid.toggle");
		assert setting.isPreference();

		// Toggle setting
		final PlayerSettings settings = actor.settings();
		final boolean flag = !settings.toBoolean(setting);
		settings.set(setting, flag);

		// Build response
		return Response.of(TextHelper.join("preference", setting.name(), String.valueOf(flag)));
	}

	/**
	 * Sets an integer preference.
	 * @param actor			Actor
	 * @param setting		Preference setting
	 * @param value			Value
	 * @return Response
	 * @throws ActionException if the given setting is not an integer preference
	 * @see Setting#isPreference()
	 */
	public Response set(PlayerCharacter actor, Setting setting, Integer value) throws ActionException {
		if(setting.isBoolean()) throw ActionException.of("preference.invalid.integer");
		assert setting.isPreference();
		actor.settings().set(setting, value);
		return Response.OK;
	}
}
