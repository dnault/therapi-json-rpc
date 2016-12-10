package com.github.therapi.jackson.enums;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Customizes the way Jackson serializes enums.
 */
public abstract class EnumRenamingModule extends SimpleModule {
    private boolean overrideExistingNames;

    public EnumRenamingModule() {
        super("therapi-enum-renaming");
    }

    /**
     * Configures the module to clobber any enum names set by
     * a previous annotation introspector.
     */
    public EnumRenamingModule overrideExistingNames() {
        this.overrideExistingNames = true;
        return this;
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new EnumNamingAnnotationIntrospector());
    }

    private class EnumNamingAnnotationIntrospector extends NopAnnotationIntrospector {
        public String[] findEnumValues(Class<?> enumType, Enum<?>[] enumValues, String[] names) {
            for (int i = 0; i < enumValues.length; i++) {
                if (names[i] == null || overrideExistingNames) {
                    names[i] = EnumRenamingModule.this.getName(enumValues[i]);
                }
            }
            return names;
        }
    }

    /**
     * @param value the enum value to inspect
     * @return the JSON name for the enum value, or {@code null} to delegate to the next introspector
     */
    protected abstract String getName(Enum<?> value);
}
