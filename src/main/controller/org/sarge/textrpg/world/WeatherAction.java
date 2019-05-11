package org.sarge.textrpg.world;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.entity.Stance;
import org.springframework.stereotype.Component;

/**
 * Displays the weather in the current location.
 * @author Sarge
 */
@Component
public class WeatherAction extends AbstractAction {
	private static final Response WEATHER_NONE = Response.of("weather.none");

	/**
	 * Constructor.
	 */
	public WeatherAction() {
		super(Flag.OUTSIDE);
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
		switch(terrain) {
		case INDOORS:
		case UNDERGROUND:
			return false;

		default:
			return true;
		}
	}

	/**
	 * Displays the weather at the current location.
	 * @param actor Actor
	 * @return Weather response
	 */
	@RequiresActor
	public Response weather(Entity actor) {
		final Weather weather = actor.location().area().weather();
		if(weather == Weather.NONE) {
			return WEATHER_NONE;
		}
		else {
			return Response.of(weather.describe());
		}
	}
}
