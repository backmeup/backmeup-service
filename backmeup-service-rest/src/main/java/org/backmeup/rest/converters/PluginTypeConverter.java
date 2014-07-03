package org.backmeup.rest.converters;

import org.backmeup.model.dto.PluginDTO.PluginType;
import org.backmeup.model.spi.SourceSinkDescribable.Type;
import org.dozer.CustomConverter;
import org.dozer.MappingException;

public class PluginTypeConverter implements CustomConverter {

	@SuppressWarnings("rawtypes")
	public Object convert(Object destination, Object source, Class destClass, Class sourceClass) {
		if (source == null) {
			return null;
		}

		if (source instanceof Type) {
			Type pluginType = (Type) source;

			switch (pluginType) {
			case Source:
				return PluginType.source;

			case Sink:
				return PluginType.sink;

			case Both:
				return PluginType.sourcesink;

			default:
				throw new IllegalStateException();
			}

		} else if (source instanceof PluginType) {
			PluginType pluginType = (PluginType) source;
			switch (pluginType) {
			case source:
				return Type.Source;

			case sink:
				return Type.Sink;

			case sourcesink:
				return Type.Both;

			case action:
				throw new MappingException(
						"Conversion from 'action' not supported");

			default:
				throw new IllegalStateException();

			}
		} else {
			throw new MappingException("Converter PluginTypeConverter used incorrectly. +"
					+ "Arguments passed in were: " + destination + " and " + source);
		}
	}
}
