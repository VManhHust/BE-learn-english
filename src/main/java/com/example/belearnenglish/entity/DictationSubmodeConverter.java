package com.example.belearnenglish.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for DictationSubmode enum.
 * Converts between enum and database string value.
 */
@Converter(autoApply = true)
public class DictationSubmodeConverter implements AttributeConverter<DictationSubmode, String> {
    
    @Override
    public String convertToDatabaseColumn(DictationSubmode attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }
    
    @Override
    public DictationSubmode convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return DictationSubmode.fromJson(dbData);
    }
}
