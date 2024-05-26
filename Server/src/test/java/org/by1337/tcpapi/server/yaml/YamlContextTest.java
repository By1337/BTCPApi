package org.by1337.tcpapi.server.yaml;

import junit.framework.TestCase;
import org.junit.Assert;

import java.util.List;

public class YamlContextTest extends TestCase {
    public void testGet() throws YamlContext.YamlParserException {
        YamlContext context = new YamlContext("""
                map:
                  subMap:
                    param1: '10'
                    param2: test
                    list:
                      - '123'
                      - '123'
                      - '123'
                """);

        Assert.assertEquals(context.get("map.subMap.param1").getAsString(), "10");
        Assert.assertEquals(context.get("map.subMap.param2").getAsString(), "test");
        Assert.assertEquals(context.get("map.subMap.none", "string").getAsString(), "string");
        Assert.assertNull(context.get("map.subMap.none").getValue());

        Assert.assertEquals(context.get("map.subMap.param1").getAsInteger(), (Integer) 10);

        Assert.assertEquals(context.get("map.subMap.list").getAsList(String.class), List.of("123", "123", "123"));


        Assert.assertEquals(context.get("map").getAsYamlContext().get("subMap").getAsYamlContext().get("param1").getAsInteger(), (Integer) 10);

    }

    public void testSet() throws YamlContext.YamlParserException {
        YamlContext context = new YamlContext("""
                map:
                  subMap:
                    param1: '10'
                    param2: test
                    list:
                      - '123'
                      - '123'
                      - '123'
                """);

        context.set("map.map2.param", 10);
        Assert.assertEquals(context.get("map.map2.param").getAsInteger(), (Integer) 10);

        Assert.assertEquals(context.saveToString(), """
                map:
                  subMap:
                    param1: '10'
                    param2: test
                    list:
                    - '123'
                    - '123'
                    - '123'
                  map2:
                    param: 10
                """);
    }
}