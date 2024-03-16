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

        Assert.assertEquals(String.valueOf(context.get("map.subMap.param1")), "10");
        Assert.assertEquals(String.valueOf(context.get("map.subMap.param2")), "test");

        Assert.assertEquals(context.getAsInt("map.subMap.param1"), (Integer) 10);

        Assert.assertEquals(context.getListString("map.subMap.list"), List.of("123", "123", "123"));
    }
}