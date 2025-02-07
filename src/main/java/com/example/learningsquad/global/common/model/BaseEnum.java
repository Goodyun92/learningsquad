package com.example.learningsquad.global.common.model;

public interface BaseEnum {
    String getValue();

    static <E extends Enum<E> & BaseEnum> E fromValue(Class<E> enumClass, String value) {
        if (value == null) {
            // null인 경우 기본값을 반환하거나, null 처리
            return null; // 또는 기본값을 반환할 수도 있습니다.
        }

        for (E e : enumClass.getEnumConstants()) {
            if (e.getValue().equalsIgnoreCase(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                String.format("존재하지 않는 value '%s' for enum '%s'", value, enumClass.getSimpleName())
        );
    }
}
