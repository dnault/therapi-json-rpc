package com.github.therapi.jackson.enums;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Customizes the way Jackson serializes enums.
 */
public abstract class EnumRenamingModule extends SimpleModule {
    public EnumRenamingModule() {
        super("therapi-enum-renaming");
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new EnumNamingAnnotationIntrospector());
    }

    private class EnumNamingAnnotationIntrospector extends NopAnnotationIntrospector {
        @Override
        public String findEnumValue(Enum<?> value) {
            return EnumRenamingModule.this.getName(value);
        }
    }

    /**
     * @param value the enum value to inspect
     * @return the JSON name for the enum value, or {@code null} to delegate to the next introspector
     */
    protected abstract String getName(Enum<?> value);
}
