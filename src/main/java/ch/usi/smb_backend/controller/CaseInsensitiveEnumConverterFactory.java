package ch.usi.smb_backend.controller;

import java.util.Locale;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/** Reads enum parameters regardless of case: {@code basis=actual} and {@code type=train} work. */
final class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum<?>> {

	@Override
	public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
		return text -> toEnum(targetType, text);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T extends Enum<?>> T toEnum(Class<T> type, String text) {
		return (T) Enum.valueOf((Class) type, text.strip().toUpperCase(Locale.ROOT));
	}
}
