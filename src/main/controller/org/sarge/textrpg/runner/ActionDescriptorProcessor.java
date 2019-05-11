package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.ActionOrder;
import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.RequiredObject;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.runner.ActionDescriptor.RequiredDescriptor;

/**
 * Generates action descriptor(s) from an {@link AbstractAction} instance.
 * @author Sarge
 * @see ActionDescriptor
 */
public class ActionDescriptorProcessor {
	private final AbstractAction action;

	/**
	 * Constructor.
	 * @param action Action instance
	 */
	public ActionDescriptorProcessor(AbstractAction action) {
		this.action = notNull(action);
	}

	/**
	 * Constructs action descriptors for the given action.
	 * @param action Action
	 * @return Action descriptors
	 * @throws IllegalArgumentException if the action has no suitable methods
	 */
	public Stream<ActionDescriptor> build() {
		return Arrays.stream(action.getClass().getDeclaredMethods())
			.filter(ActionDescriptorProcessor::isActionMethod)
			.flatMap(this::build);
	}

	/**
	 * @param method Method
	 * @return Whether the given method is an action
	 */
	private static boolean isActionMethod(Method method) {
		if(!Modifier.isPublic(method.getModifiers())) return false;
		if(method.getReturnType() != Response.class) return false;
		return true;
	}

	/**
	 * Creates an action descriptor for the given action method or enumerated action.
	 * @param method Action method
	 * @return Action descriptor(s)
	 */
	private Stream<ActionDescriptor> build(Method method) {
		if(action.getClass().isAnnotationPresent(EnumAction.class)) {
			// Create descriptor for each enum constant
			final ActionDescriptor.Builder builder = buildAction(method);
			final List<ActionDescriptor> list = new ArrayList<>();
			final Class<?> clazz = action.getClass().getAnnotation(EnumAction.class).value();
			for(Object e : clazz.getEnumConstants()) {
				builder.enumeration(e);
				list.add(builder.build());
			}
			return list.stream();
		}
		else {
			// Otherwise build default descriptor
			final ActionDescriptor descriptor = buildAction(method).build();
			return Stream.of(descriptor);
		}
	}

	/**
	 * Build an action descriptor.
	 * @param method Action method
	 * @return Action descriptor builder
	 */
	private ActionDescriptor.Builder buildAction(Method method) {
		// Start builder
		final ActionDescriptor.Builder builder = new ActionDescriptor.Builder(action, method);

		// Check for injected actor argument
		if(isAnnotation(RequiresActor.class, method)) {
			builder.requiresActorArgument();
		}

		// Check for injected action-effort argument
		if(isAnnotation(EffortAction.class, method)) {
			builder.requiresEffortArgument();
		}

		// Add required objects for this method
		Arrays.stream(method.getAnnotationsByType(RequiredObject.class))
			.map(spec -> new RequiredDescriptor(spec.value(), false))
			.forEach(builder::add);

		// Add injected required object parameters
		Arrays.stream(method.getParameters())
			.map(p -> p.getAnnotation(RequiredObject.class))
			.filter(Objects::nonNull)
			.map(spec -> new RequiredDescriptor(spec.value(), true))
			.forEach(builder::add);

		// Add method order
		final ActionOrder order = method.getAnnotation(ActionOrder.class);
		if(order != null) {
			builder.order(order.value());
		}

		return builder;
	}

	/**
	 * @return Whether the given annotation is present on the action class or the given method
	 */
	private boolean isAnnotation(Class<? extends Annotation> annotation, Method method) {
		return action.getClass().isAnnotationPresent(annotation) || method.isAnnotationPresent(annotation);
	}
}
