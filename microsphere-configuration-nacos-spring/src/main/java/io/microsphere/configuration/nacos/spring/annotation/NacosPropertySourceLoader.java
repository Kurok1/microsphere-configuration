package io.microsphere.configuration.nacos.spring.annotation;

import io.microsphere.nacos.client.NacosClientConfig;
import io.microsphere.nacos.client.common.config.ConfigClient;
import io.microsphere.nacos.client.common.config.event.ConfigChangedEvent;
import io.microsphere.nacos.client.transport.OpenApiClient;
import io.microsphere.nacos.client.transport.OpenApiHttpClient;
import io.microsphere.nacos.client.v2.config.OpenApiConfigClientV2;
import io.microsphere.spring.config.context.annotation.PropertySourceExtensionLoader;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 1.0
 */
public class NacosPropertySourceLoader extends PropertySourceExtensionLoader<NacosPropertySource, NacosPropertySourceAttributes> {

    private volatile ConfigClient client;

    private final Object CLIENT_GET_LOCK = new Object();


    protected ConfigClient loadConfigClient(NacosPropertySourceAttributes attributes) {
        synchronized (this.CLIENT_GET_LOCK) {
            if (client == null) {
                client = getConfigClient(attributes);
            }
            return this.client;
        }
    }


    @Override
    protected Resource[] resolveResources(NacosPropertySourceAttributes extensionAttributes, String propertySourceName, String resourceValue) throws Throwable {
        ConfigClient client = loadConfigClient(extensionAttributes);


        String namespace = extensionAttributes.getNamespace();
        String group = extensionAttributes.getGroup();
        String[] dataIds = extensionAttributes.getValue();
        Resource[] resources = new Resource[dataIds.length];
        for (int i = 0; i < dataIds.length; i++) {
            String dataId = dataIds[i];
            String content = client.getConfig(namespace, group, dataId).getContent();
            //client.getConfig(namespace, group, dataId);
            resources[i] = new ByteArrayResource(content.getBytes(extensionAttributes.getEncoding()));
        }
        return resources;
    }

    @Override
    protected void configureResourcePropertySourcesRefresher(NacosPropertySourceAttributes extensionAttributes, List<PropertySourceResource> propertySourceResources, CompositePropertySource propertySource, ResourcePropertySourcesRefresher refresher) throws Throwable {
        if (extensionAttributes.isAutoRefreshed()) {
            ConfigClient client = loadConfigClient(extensionAttributes);
            String namespace = extensionAttributes.getNamespace();
            String group = extensionAttributes.getGroup();
            String[] dataIds = extensionAttributes.getValue();
            for (String dataId : dataIds) {
                client.addEventListener(namespace, group, dataId, (event) -> {
                    try {
                        onConfigChanged(event, extensionAttributes.getEncoding(), refresher);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    private void onConfigChanged(ConfigChangedEvent configEvent, String encoding, ResourcePropertySourcesRefresher refresher) throws UnsupportedEncodingException {
        if (configEvent.isModified()) {
            String resourceValue = configEvent.getDataId();
            String content = configEvent.getContent();
            Resource resource = new ByteArrayResource(content.getBytes(encoding));
            try {
                refresher.refresh(resourceValue, resource);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }


    private ConfigClient getConfigClient(NacosPropertySourceAttributes attributes) {
        NacosClientConfig clientConfig = resolveClientConfig(attributes);
        OpenApiClient client = new OpenApiHttpClient(clientConfig);
        return new OpenApiConfigClientV2(client, clientConfig);
    }

    protected NacosClientConfig resolveClientConfig(NacosPropertySourceAttributes attributes) {
        NacosClientConfig clientConfig = new NacosClientConfig();
        clientConfig.setServerAddress(attributes.getConnectString());
        clientConfig.setLongPollingTimeout(5 * 1000);

        attributes.getUsername().ifPresent(clientConfig::setUserName);
        attributes.getPassword().ifPresent(clientConfig::setPassword);
        attributes.getAccessKey().ifPresent(clientConfig::setAccessKey);
        attributes.getSecretKey().ifPresent(clientConfig::setSecretKey);

        return clientConfig;
    }
}

