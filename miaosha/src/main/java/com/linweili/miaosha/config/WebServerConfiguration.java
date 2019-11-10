package com.linweili.miaosha.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * 当Spring容器没有TomcatEmbeddedServletContainerFactory这个Bean时,会加载此Bean
 */
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {


    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        //使用对应工厂类提供给的接口定制Tomcat Connector
        ((TomcatServletWebServerFactory) factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();

                //定制keepAliveTimeout, 30秒内无请求则服务端断开链接
                protocol.setKeepAliveTimeout(30000);
                //当客户端发送超过10000个请求则自动断开keepAlive链接
                protocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
