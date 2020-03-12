package com.covid19.repositories.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.AttributeConverter;

public class StringListConverter implements AttributeConverter<List<Integer>, String> {
	@Override
	public String convertToDatabaseColumn(List<Integer> listToPersist) {
		String commaSeparatedStrToPersist = "";
		if (listToPersist == null || listToPersist.isEmpty()) {
			return commaSeparatedStrToPersist;
		}
		commaSeparatedStrToPersist = listToPersist.
				stream()
				.map(num -> String.valueOf(num))
				.collect(Collectors.joining(","));
		return commaSeparatedStrToPersist;
	}

	@Override
	public List<Integer> convertToEntityAttribute(String joinedStringFromDB) {
		if (joinedStringFromDB == null || joinedStringFromDB.isEmpty()) {
			return new ArrayList<Integer>();
		}
		return Stream.of(
				joinedStringFromDB.replaceAll("\\s", "")
				.split(","))
				.peek(s -> s.trim())
				.map(Integer::parseInt)
				.sorted()
				.collect(Collectors.toList());
	}

}
