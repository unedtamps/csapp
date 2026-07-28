package com.mycomp.csmessage.products.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Attribute {
  private String key;
  private Object value;

  public static <T> Attribute of(AttributeKey key, T value) {
    if (key.isList()) {
      if (!(value instanceof List<?> list)) {
        throw new IllegalArgumentException(
            String.format(
                "Key '%s' expects a List<%s>, but got %s",
                key.getKeyName(),
                key.getElementType().getSimpleName(),
                value.getClass().getSimpleName()));
      }

      for (Object item : list) {
        if (!key.getElementType().isInstance(item)) {
          throw new IllegalArgumentException(
              String.format(
                  "List element in '%s' must be of type %s, but found %s",
                  key.getKeyName(),
                  key.getElementType().getSimpleName(),
                  item.getClass().getSimpleName()));
        }
      }
    } else {
      if (!key.getElementType().isInstance(value)) {
        throw new IllegalArgumentException(
            String.format(
                "Key '%s' expects %s, but got %s",
                key.getKeyName(),
                key.getElementType().getSimpleName(),
                value.getClass().getSimpleName()));
      }
    }

    return new Attribute(key.getKeyName(), value);
  }

  @Getter
  @AllArgsConstructor
  public enum AttributeKey {
    COLOR("color", String.class, false),
    SIZES("sizes", String.class, true),
    FABRIC("fabric", String.class, false),

    ASSEMBLY_REQUIRED("assemblyRequired", Boolean.class, false),
    MATERIAL("material", String.class, false),
    WEIGHT_KG("weightKg", Double.class, false);

    private final String keyName;
    private final Class<?> elementType;
    private final boolean isList;
  }
}
