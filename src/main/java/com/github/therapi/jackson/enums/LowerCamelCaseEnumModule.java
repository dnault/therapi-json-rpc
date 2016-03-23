package com.github.therapi.jackson.enums;

/**
 * Serialize enums by converting the name to lower camel case.
 */
public class LowerCamelCaseEnumModule extends EnumRenamingModule {
    @Override protected String getName(Enum<?> value) {
        return CaseFormatHelper.toLowerCamel(value.name());
    }
}
