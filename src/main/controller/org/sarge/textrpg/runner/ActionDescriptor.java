package org.sarge.textrpg.runner;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.textrpg.common.AbstractAction;
import org.sarge.textrpg.common.Carried;
import org.sarge.textrpg.common.EffortAction;
import org.sarge.textrpg.common.EnumAction;
import org.sarge.textrpg.common.RequiresActor;
import org.sarge.textrpg.common.Response;
import org.sarge.textrpg.entity.Entity;
import org.sarge.textrpg.object.WorldObject;
import org.sarge.textrpg.util.ActionException;

/**
 * An <i>action descriptor</i> specifies the properties of an action <b>method</b>.
 * <p>
 * An action descriptor consists of:
 * <ul>
 * <li>the underlying action method and parameter list</li>
 * <li>a list of required objects that are optionally injected into the method</li>
 * <li>flags indicating whether the actor and action-effort arguments are injected</li>
 * </ul>
 * <p>
 * Action method arguments are assumed to be in the following order:
 * <p>
 * <table border="1">
 * <tr>
 *   <th>base type</th>
 *   <th>condition</th>
 *   <th>description</th>
 * </tr>
 * <tr>
 *   <td>Entity</td>
 *   <td>{@link #isActorRequired()}</td>
 *   <td>actor</td>
 * </tr>
 * <tr>
 *   <td><i>constant</i></td>
 *   <td>{@link EnumAction}</td>
 *   <td>enumeration constant</td>
 * </tr>
 * <tr>
 *   <td><i>any</i></td>
 *   <td>action method signature</td>
 *   <td>command arguments</td>
 * </tr>
 * <tr>
 *   <td>WorldObject</td>
 *   <td>{@link RequiredDescriptor#isInjected()}</td>
 *   <td>required object(s)</td>
 * </tr>
 * <tr>
 *   <td>{@link AbstractAction.Effort}</td>
 *   <td>{@link #isEffortAction()</td>
 *   <td>effort</td>
 * </tr>
 * </table>
 * </p>
 * Notes:
 * <ul>
 * <li>All the above are optional, i.e. an action method can have zero arguments</li>
 * <li>Actions methods are processed in order, therefore more specialised methods should be declared first in the action class</li>
 * </ul>
 * <p>
 * @see AbstractAction
 * @author Sarge
 */
public class ActionDescriptor extends AbstractEqualsObject {
	/**
	 * Action method ordering.
	 */
	public static final Comparator<ActionDescriptor> ORDER = Comparator.comparingInt(ActionDescriptor::order);

	/**
	 * Descriptor for an object required to perform this action.
	 */
	public static class RequiredDescriptor {
		private final String cat;
		private final WorldObject.Filter filter;
		private final boolean inject;

		/**
		 * Constructor.
		 * @param cat			Object category
		 * @param inject		Whether the required object is injected into the action method
		 */
		protected RequiredDescriptor(String cat, boolean inject) {
			this.filter = WorldObject.Filter.of(cat);
			this.cat = notEmpty(cat);
			this.inject = inject;
		}

		/**
		 * @return Category filter
		 */
		public WorldObject.Filter filter() {
			return filter;
		}

		/**
		 * @return Expected category
		 */
		public String category() {
			return cat;
		}

		/**
		 * @return Whether the required object is injected into the action method
		 */
		public boolean isInjected() {
			return inject;
		}

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this, that, "filter");
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toStringExclude(this, "filter");
		}
	}

	/**
	 * Wrapper for an action parameter.
	 */
	public static class ActionParameter extends AbstractEqualsObject {
		private final Class<?> type;
		private final Optional<Carried> carried;

		/**
		 * Constructor.
		 * @param type			Parameter type
		 * @param carried		Optional carried annotation
		 */
		public ActionParameter(Class<?> type, Carried carried) {
			this.type = notNull(type);
			this.carried = Optional.ofNullable(carried);
			if(this.carried.isPresent() && !WorldObject.class.isAssignableFrom(type)) throw new IllegalArgumentException("Invalid carried annotation");
		}

		/**
		 * @return Parameter type
		 */
		public Class<?> type() {
			return type;
		}

		/**
		 * @return Carried annotation
		 */
		public Optional<Carried> carried() {
			return carried;
		}

		@Override
		public String toString() {
			final ToStringBuilder builder = new ToStringBuilder(this).append("type", type.getSimpleName());
			carried.ifPresent(c -> builder.append("carried", c.auto()));
			return builder.build();
		}
	}

	private final String name;
	private final AbstractAction action;
	private final Method method;
	private final Optional<Object> constant;
	private final List<RequiredDescriptor> required;
	private final boolean actor;
	private final boolean effort;
	private final List<ActionParameter> params;
	private final int order;

	/**
	 * Constructor.
	 * @param action		Action instance
	 * @param method		Method
	 * @param constant		Optional constant for an enumerated action
	 * @param actor			Whether the actor is injected as a method argument
	 * @param required		Objects required to perform this action
	 * @param effort		Whether the optional action-effort is injected
	 * @param order			Method order
	 */
	private ActionDescriptor(AbstractAction action, Method method, Object constant, boolean actor, List<RequiredDescriptor> required, boolean effort, int order) {
		this.name = name(action, method, constant);
		this.action = notNull(action);
		this.method = notNull(method);
		this.constant = Optional.ofNullable(constant);
		this.actor = actor;
		this.required = List.copyOf(required);
		this.effort = effort;
		this.params = build(action.getClass().getName(), method.getParameters(), actor, constant, required, effort);
		this.order = oneOrMore(order);
	}

	/**
	 * Builds the name of this action.
	 */
	private static String name(AbstractAction action, Method method, Object constant) {
		// Start name
		final StringJoiner name = new StringJoiner(".");
		name.add("action");

		// Add optional prefix
		final String prefix = action.prefix();
		if(prefix != StringUtils.EMPTY) {
			name.add(prefix);
		}

		// Add method name
		if(constant == null) {
			name.add(method.getName());
		}
		else {
			name.add(constant.toString());
		}

		// Build name
		return name.toString().toLowerCase();
	}

	/**
	 * Builds and verifies the parameters for this action.
	 * @param name			Action name
	 * @param params		All action method parameters
	 * @param actor			Whether the actor is injected as a method argument
	 * @param required		Required objects
	 * @param effort		Whether the optional action-effort is injected
	 * @return Parsed parameters for this action
	 */
	private static List<ActionParameter> build(String name, Parameter[] params, boolean actor, Object constant, List<RequiredDescriptor> required, boolean effort) {
		// Verify actor parameter
		int start = 0;
		int len = params.length;
		if(actor) {
			verify(name, params, start, Entity.class);
			++start;
		}

		// Verify enum constant parameter
		if(constant != null) {
			final Class<?> clazz = constant.getClass();
			verify(name, params, start, clazz.isEnum() ? clazz : clazz.getEnclosingClass());
			++start;
		}

		// Verify effort parameter
		if(effort) {
			verify(name, params, params.length - 1, AbstractAction.Effort.class);
			--len;
		}

		// Verify required object parameters
		final int num = (int) required.stream().filter(req -> req.isInjected()).count();
		for(int n = start; n < start + num; ++n) {
			verify(name, params, n, WorldObject.class);
		}

		// Enumerate action parameters
		final List<ActionParameter> args = new ArrayList<>();
		for(int n = start + num; n < len; ++n) {
			final ActionParameter p = new ActionParameter(params[n].getType(), params[n].getAnnotation(Carried.class));
			args.add(p);
		}
		return List.copyOf(args);
	}

	/**
	 * Verifies the expected type of the parameter at the given index.
	 * @param name			Action name
	 * @param params		Parameters
	 * @param index			Index
	 * @param expected		Expected type
	 * @throws IndexOutOfBoundsException if the parameter is not present
	 * @throws IllegalArgumentException if the parameter does match the expected type
	 */
	private static void verify(String name, Parameter[] params, int index, Class<?> expected) {
		// Check index
		if((index < 0) || (index >= params.length)) {
			throw new IndexOutOfBoundsException(String.format("Parameter not present: action=%s index=%d length=%d", name, index, params.length));
		}

		// Check parameter type
		if(!expected.isAssignableFrom(params[index].getType())) {
			throw new IllegalArgumentException(String.format("Incorrect parameter type: action=%s index=%s actual=%s expected=%s", name, index, params[index].getType(), expected));
		}
	}

	/**
	 * @return Action name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Method order
	 */
	public int order() {
		return order;
	}

	/**
	 * @return Action instance
	 */
	public AbstractAction action() {
		return action;
	}

	/**
	 * @return Action method parameters (excluding synthetic or injected arguments)
	 */
	public List<ActionParameter> parameters() {
		return params;
	}

	/**
	 * @return Constant for an enumerated action
	 */
	public Optional<Object> constant() {
		return constant;
	}

	/**
	 * @return Whether the actor is injected into the action method
	 * @see RequiresActor
	 */
	public boolean isActorRequired() {
		return actor;
	}

	/**
	 * @return Whether the effort argument is injected into the action method
	 * @see EffortAction
	 */
	public boolean isEffortAction() {
		return effort;
	}

	/**
	 * @return Objects required to perform this action
	 */
	public List<RequiredDescriptor> required() {
		return required;
	}

	/**
	 * Invokes the action method with the given arguments.
	 * @param args Arguments
	 * @return Response
	 * @throws Exception if the invocation fails
	 * @throws ActionException for an invalid action
	 */
	public Response invoke(List<Object> args) throws Exception {
		assert args.size() == method.getParameterCount();
		return (Response) method.invoke(action, args.toArray());
	}

	@Override
	public String toString() {
		final ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("name", name());
		constant.ifPresent(e -> builder.append("enum", e));
		builder.append("parameters", params);
		builder.append("action", action.getClass().getSimpleName());
		return builder.toString();
	}

	/**
	 * Builder for an action descriptor.
	 */
	public static class Builder {
		private final AbstractAction action;
		private final Method method;
		private Object constant;
		private final List<RequiredDescriptor> required = new StrictList<>();
		private boolean actor;
		private boolean effort;
		private int order = 1;

		/**
		 * Constructor.
		 * @param action Action instance
		 * @param method Method
		 */
		public Builder(AbstractAction action, Method method) {
			this.action = action;
			this.method = method;
		}

		/**
		 * Sets the constant for an enumerated action.
		 * @param constant Enumeration constant
		 */
		public Builder enumeration(Object constant) {
			this.constant = constant;
			return this;
		}

		/**
		 * Adds an object required to perform this action.
		 * @param required Required object descriptor
		 */
		public Builder add(RequiredDescriptor required) {
			this.required.add(required);
			return this;
		}

		/**
		 * Sets whether to inject the actor argument.
		 */
		public Builder requiresActorArgument() {
			actor = true;
			return this;
		}

		/**
		 * Sets whether to inject the action-effort argument.
		 */
		public Builder requiresEffortArgument() {
			effort = true;
			return this;
		}

		/**
		 * Sets the order of this action method.
		 * @param order Action order
		 */
		public Builder order(int order) {
			this.order = order;
			return this;
		}

		/**
		 * Constructs this action descriptor.
		 * @return New descriptor
		 */
		public ActionDescriptor build() {
			return new ActionDescriptor(action, method, constant, actor, required, effort, order);
		}
	}
}
