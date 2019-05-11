package org.sarge.textrpg.entity;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.MnemonicConverter;
import org.sarge.textrpg.util.ValueModifier;

/**
 * Entity attributes.
 * @author Sarge
 */
public enum Attribute implements ValueModifier.Key {
	STRENGTH("str"),
	ENDURANCE("end"),
	INTELLIGENCE("int"),
	WILL,
	AGILITY,
	PERCEPTION("per"),
	SKILL,
	LUCK;

	/**
	 * Converter.
	 */
	public static final Converter<Attribute> CONVERTER = MnemonicConverter.converter(Attribute.class, Attribute::mnemonic);

	/**
	 * Attribute mnemonics.
	 */
	public static final Map<String, Attribute> MNEMONICS = Arrays.stream(values()).collect(toMap(Attribute::mnemonic, Function.identity()));

	private final String mnemonic;

	private Attribute() {
		mnemonic = name().toLowerCase();
	}

	private Attribute(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	/**
	 * @return Mnemonic (or short-name) of this attribute
	 */
	public String mnemonic() {
		return mnemonic;
	}
}
