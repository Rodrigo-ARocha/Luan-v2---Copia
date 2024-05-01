package br.com.pucminas.aed3.model;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BytesUtils {

    public static <T> byte[] toBytes(T value) {
        if ((value instanceof Integer)) {
            Integer intValue = Objects.nonNull(value) ? (Integer) value : 0;
            return new byte[] {
                    (byte) ((intValue >> 24) & 0xFF),
                    (byte) ((intValue >> 16) & 0xFF),
                    (byte) ((intValue >> 8) & 0xFF),
                    (byte) (intValue & 0xFF)
            };
        } else if (value instanceof Float) {
            Float floatValue = Objects.nonNull(value) ? (Float) value : 0;
            return new byte[] {
                    (byte) ((Float.floatToIntBits(floatValue.floatValue()) >> 24) & 0xFF),
                    (byte) ((Float.floatToIntBits(floatValue.floatValue()) >> 16) & 0xFF),
                    (byte) ((Float.floatToIntBits(floatValue.floatValue()) >> 8) & 0xFF),
                    (byte) (Float.floatToIntBits(floatValue.floatValue()) & 0xFF)
            };

        } else if (value instanceof Long) {
            Long longValue = Objects.nonNull(value) ? (Long) value : 0L;
            return new byte[] {
                    (byte) ((longValue >> 56) & 0xFF),
                    (byte) ((longValue >> 48) & 0xFF),
                    (byte) ((longValue >> 40) & 0xFF),
                    (byte) ((longValue >> 32) & 0xFF),
                    (byte) ((longValue >> 24) & 0xFF),
                    (byte) ((longValue >> 16) & 0xFF),
                    (byte) ((longValue >> 8) & 0xFF),
                    (byte) (longValue & 0xFF)
            };
        } else if (value instanceof Double) {
            Double doubleValue = Objects.nonNull(value) ? (Double) value : 0.0;
            return new byte[] {
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 56) & 0xFF),
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 48) & 0xFF),
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 40) & 0xFF),
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 32) & 0xFF),
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 24) & 0xFF),
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 16) & 0xFF),
                    (byte) ((Double.doubleToLongBits(doubleValue) >> 8) & 0xFF),
                    (byte) (Double.doubleToLongBits(doubleValue) & 0xFF)
            };
        } else if (value instanceof Boolean) {
            Boolean boolValue = Objects.nonNull(value) ? (Boolean) value : false;
            return new byte[] {
                    (byte) (boolValue ? 1 : 0)
            };
        } else if (value instanceof Byte) {
            return new byte[] {
                    Objects.nonNull(value) ? ((Byte) value).byteValue() : 0x00
            };
        } else if ((value instanceof Short) || (value instanceof Character)) {

            int intValue = Objects.nonNull(value)
                    ? value instanceof Short ? ((Short) value).intValue() : ((Character) value).charValue()
                    : 0;
            return new byte[] {
                    (byte) ((intValue >> 8) & 0xFF),
                    (byte) (intValue & 0xFF)
            };
        } else if (value instanceof String) {

            return Objects.nonNull(value) ? ((String) value).getBytes() : "".getBytes();
        }
        return new byte[0];
    }

    public static <T> T fromBytes(byte[] value, Class<T> target) {

        if (Objects.equals(target, Integer.class) || Objects.equals(target, Float.class)
                || Objects.equals(target, Short.class)) {
            int intValue = 0;
            for (byte b : value) {
                intValue = (intValue << 8) + (b & 0xFF);
            }
            if (Objects.equals(target, Short.class)) {
                return target.cast(Short.valueOf(Integer.valueOf(intValue).shortValue()));
            } else if (Objects.equals(target, Integer.class)) {
                return target.cast(Integer.valueOf(intValue));
            } else {
                return target.cast(Float.valueOf(Float.intBitsToFloat(intValue)));
            }
        } else if (Objects.equals(target, Long.class) || Objects.equals(target, Double.class)) {
            long longValue = 0;
            for (byte b : value) {
                longValue = (longValue << 8) + (b & 0xFF);
            }
            if (Objects.equals(target, Long.class)) {
                return target.cast(Long.valueOf(longValue));
            } else {
                return target.cast(Double.longBitsToDouble(longValue));
            }
        } else if (Objects.equals(target, Byte.class)) {
            return target.cast(Byte.valueOf(value[0]));
        } else if (Objects.equals(target, String.class)) {
            return target.cast(new String(value, StandardCharsets.UTF_8));
        } else if (Objects.equals(target, Boolean.class)) {
            return target.cast(Boolean.valueOf(value[0] == 1));
        }
        return null;
    }
}
