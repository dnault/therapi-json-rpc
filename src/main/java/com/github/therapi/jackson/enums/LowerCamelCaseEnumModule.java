package com.github.therapi.jackson.enums;

public class LowerCamelCaseEnumModule extends EnumRenamingModule {
    @Override protected String getName(Enum<?> value) {
        return CaseFormatHelper.toLowerCamel(value.name());
    }
}
