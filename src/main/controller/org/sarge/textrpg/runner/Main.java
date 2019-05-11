package org.sarge.textrpg.runner;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.sarge.textrpg.util.DurationConverter;
import org.sarge.textrpg.util.Percentile;
import org.sarge.textrpg.util.ServiceComponent;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

/**
 * Application entry-point.
 * @author Sarge
 */
@SpringBootApplication
@ComponentScan("org.sarge.textrpg")
public class Main implements ApplicationListener<ApplicationReadyEvent> {
	@Bean
	public ConversionServiceFactoryBean conversionService() {
		class PercentilePropertyConverter implements Converter<String, Percentile> {
			@Override
			public Percentile convert(String value) {
				return Percentile.CONVERTER.apply(value);
			}
		}

		class DurationPropertyConverter implements Converter<String, Duration> {
			@Override
			public Duration convert(String value) {
				return DurationConverter.CONVERTER.apply(value);
			}
		}

		final ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
	    final Set<Converter<?, ?>> converters = new HashSet<>();
	    converters.add(new PercentilePropertyConverter());
	    converters.add(new DurationPropertyConverter());
	    bean.setConverters(converters);
	    return bean;
	}

	/**
	 * Starts service components once the application has successfully initialised.
	 * @see ServiceComponent
	 */
	@SuppressWarnings("resource")
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		final ApplicationContext ctx = event.getApplicationContext();
		for(String name : ctx.getBeanNamesForType(ServiceComponent.class)) {
			final ServiceComponent c = (ServiceComponent) ctx.getBean(name);
			c.start();
		}
	}
	// TODO - inject list of service-component rather than listener? assumes loaders use post-construct
	// TODO - could just execute after run()? or app runner thingy?

	/**
	 * Entry-point.
	 * @param args Application arguments
	 */
	public static void main(String[] args) {
		ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);

		new SpringApplicationBuilder(Main.class)
			.properties("spring.config.name:application,actions,controllers")
			.build()
			.run(args);
	}
}
