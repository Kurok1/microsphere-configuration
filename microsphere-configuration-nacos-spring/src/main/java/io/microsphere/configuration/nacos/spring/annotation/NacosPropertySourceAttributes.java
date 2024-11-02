package io.microsphere.configuration.nacos.spring.annotation;

import io.microsphere.spring.config.context.annotation.PropertySourceExtensionAttributes;
import org.springframework.core.env.PropertyResolver;

import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 1.0
 */
public final class NacosPropertySourceAttributes extends PropertySourceExtensionAttributes<NacosPropertySource> {

    public NacosPropertySourceAttributes(Map<String, Object> another, Class<NacosPropertySource> annotationType, PropertyResolver propertyResolver) {
        super(another, annotationType, propertyResolver);
    }

    public String getConnectString() {
        return getString("connectString");
    }

    public String getNamespace() {
        return getString("namespace");
    }

    public String getGroup() {
        return getString("group");
    }

    public Optional<String> getUsername() {
        String value = getString("username");
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    public Optional<String> getPassword() {
        String value = getString("password");
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    public Optional<String> getAccessKey() {
        String value = getString("accessKey");
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    public Optional<String> getSecretKey() {
        String value = getString("secretKey");
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

}
