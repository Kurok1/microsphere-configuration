package io.microsphere.configuration.nacos.spring.annotation;

import io.microsphere.nacos.client.NacosClientConfig;
import io.microsphere.nacos.client.common.config.ConfigClient;
import io.microsphere.nacos.client.common.config.ConfigType;
import io.microsphere.nacos.client.transport.OpenApiClient;
import io.microsphere.nacos.client.transport.OpenApiHttpClient;
import io.microsphere.nacos.client.v2.config.OpenApiConfigClientV2;
import io.microsphere.spring.config.env.support.JsonPropertySourceFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        NacosPropertySourceTest.class,
        NacosPropertySourceTest.Config.class
})
public class NacosPropertySourceTest {

    public static final String TEST_CONNECTION = "localhost:8848";
    public static final String TEST_DATA_ID = "my.json";
    public static final String TEST_NAMESPACE = "";

    @Autowired
    private Environment environment;

    private static ConfigClient configClient;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        NacosClientConfig clientConfig = new NacosClientConfig();
        clientConfig.setServerAddress(TEST_CONNECTION);
        OpenApiClient client = new OpenApiHttpClient(clientConfig);
        configClient = new OpenApiConfigClientV2(client, clientConfig);
        final String beforeValue = "{\"test.prop\":\"value\"}";
        writeConfig(TEST_NAMESPACE, "DEFAULT_GROUP", "my.json", beforeValue);
    }

    @Test
    public void test() throws Exception {
        assertEquals("value", environment.getProperty("test.prop"));
        final String afterValue = "{\"test.prop\":\"value1\"}";
        writeConfig("", "DEFAULT_GROUP", "my.json", afterValue);
        Thread.sleep(10 * 1000);
        assertEquals("value1", environment.getProperty("test.prop"));
    }

    private static void writeConfig(String namespace, String group, String dataId, String value) throws Exception {
        configClient.publishConfigContent(namespace, group, dataId, value, ConfigType.JSON);
    }

    @NacosPropertySource(
            connectString = NacosPropertySourceTest.TEST_CONNECTION,
            path = "my.json",
            namespace = NacosPropertySourceTest.TEST_NAMESPACE,
            factory = JsonPropertySourceFactory.class)
    static class Config {

    }

}
