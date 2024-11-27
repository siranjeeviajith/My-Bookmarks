package com.task.bookmarkmanager.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityUtils {

    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    private static String[] getNullPropertyNames(Object source) {
        final PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(source.getClass());

        Set<String> nullPropertyNames = Arrays.stream(propertyDescriptors)
                .filter(pd -> {
                    try {
                        return PropertyAccessorFactory.forBeanPropertyAccess(source).getPropertyValue(pd.getName()) == null;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(PropertyDescriptor::getName)
                .collect(Collectors.toSet());

        return nullPropertyNames.toArray(new String[0]);
    }
}