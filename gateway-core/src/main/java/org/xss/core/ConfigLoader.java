package org.xss.core;

import lombok.extern.slf4j.Slf4j;
import org.xss.common.utils.PropertiesUtils;


import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author MR.XSS
 * 2023/9/15 14:47
 */
@Slf4j
public class ConfigLoader {
    private static final String CONFIG_FILE = "gateway.properties";
    private static final String ENV_PREFIX = "GATEWAY_";
    private static final String JVM_PREFIX = "gateway.";

    private Config config;

    /**
     * 单例模式
     */
    private static final ConfigLoader INSTANCE = new ConfigLoader();

    private ConfigLoader() {
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    public static Config getConfig() {
        return INSTANCE.config;
    }

    /**
     * <h3>加载配置文件<h3/>
     * 优先级高的会覆盖掉优先级较低的
     * 运行参数 -> jvm参数 -> 环境变量 -> 配置文件 -> 配置对象默认值
     *
     * @param args
     * @return Config
     */
    public Config load(String args[]) {
        //配置对象默认值
        config = new Config();
        //配置文件
        loadFromConfigFile();
        //环境变量
        loadFormEnv();
        //jvm参数
        loadFormJvm();
        //运行参数
        loadFormArgs(args);
        return config;
    }

    /**
     * 加载运行参数
     */
    private void loadFormArgs(String args[]) {
        //--port=8080
        if (args != null & args.length > 0) {
            Properties properties = new Properties();
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    properties.put(arg.substring(2, arg.indexOf("=")), arg.substring(arg.indexOf("=") + 1));
                }
            }
            PropertiesUtils.properties2Object(properties, config);
        }
    }

    /**
     * 加载虚拟机参数
     */
    private void loadFormJvm() {
        //获取虚拟机参数
        Properties properties = System.getProperties();
        PropertiesUtils.properties2Object(properties, config, JVM_PREFIX);
    }


    /**
     * 加载环境变量
     */
    private void loadFormEnv() {
        Map<String, String> env = System.getenv();
        Properties properties = new Properties();
        properties.putAll(env);
        PropertiesUtils.properties2Object(properties, config, ENV_PREFIX);
    }

    /**
     * 加载配置文件信息
     */
    private void loadFromConfigFile() {
        InputStream resourceAsStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (resourceAsStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(resourceAsStream);
                //复制配置文件信息到config
                PropertiesUtils.properties2Object(properties, config);
            } catch (IOException e) {
                log.warn("load config file {} error", CONFIG_FILE, e);
                throw new RuntimeException(e);
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
