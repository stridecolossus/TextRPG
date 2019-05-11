package org.sarge.textrpg.common;

import org.sarge.lib.util.Converter;
import org.sarge.textrpg.util.MnemonicConverter;

/**
 * Entity gender.
 */
public enum Gender {
	MALE,
	FEMALE,
	NEUTER;

	private final String mnemonic;

	private Gender() {
		this.mnemonic = Character.toString(Character.toLowerCase(this.name().charAt(0)));
	}

	public static final Converter<Gender> CONVERTER = MnemonicConverter.converter(Gender.class, Gender::mnenomic);

	public String mnenomic() {
		return mnemonic;
	}
}
