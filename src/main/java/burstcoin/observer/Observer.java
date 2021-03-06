/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 by luxe - https://github.com/de-luxe - BURST-LUXE-RED2-G6JW-H4HG5
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package burstcoin.observer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.Socks4Proxy;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class Observer
{
  private static Log LOG = LogFactory.getLog(Observer.class);

  @Bean
  protected ServletContextListener listener()
  {
    return new ServletContextListener()
    {
      @Override
      public void contextInitialized(ServletContextEvent sce)
      {
        LOG.info("ServletContext initialized");
      }

      @Override
      public void contextDestroyed(ServletContextEvent sce)
      {
        LOG.info("ServletContext destroyed");
      }
    };
  }

  @Bean(name = "networkTaskExecutor")
  public SimpleAsyncTaskExecutor roundPool()
  {
    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    return taskExecutor;
  }

  @Bean
  public HttpClient httpClient()
  {
    HttpClient client = new HttpClient(new SslContextFactory(true));
    client.setFollowRedirects(true);
    try
    {
      client.start();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return client;
  }

  @Bean
  @Qualifier("ProxyHttpClient")
  public HttpClient proxyHttpClient()
  {
    HttpClient client = new HttpClient(new SslContextFactory(true));

    if(ObserverProperties.isEnableProxy())
    {
      if(ObserverProperties.isUseSocksProxy())
      {
        client.getProxyConfiguration().getProxies().add(new Socks4Proxy(ObserverProperties.getProxyHost(), ObserverProperties.getProxyPort()));
      }
      else
      {
        client.getProxyConfiguration().getProxies().add(new HttpProxy(ObserverProperties.getProxyHost(), ObserverProperties.getProxyPort()));
      }
    }

    try
    {
      client.start();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return client;
  }

  @Bean
  public ObjectMapper objectMapper()
  {
    return new ObjectMapper();
  }

  public static void main(String[] args)
    throws Exception
  {
    LOG.info("Starting the engines ... please wait!");

    // overwritten by application.properties
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("server.port", ObserverProperties.getObserverPort());
    properties.put("spring.mail.protocol", ObserverProperties.getMailProtocol());
    properties.put("spring.mail.host", ObserverProperties.getMailHost());
    properties.put("spring.mail.port", ObserverProperties.getMailPort());
    properties.put("spring.mail.username", ObserverProperties.getMailUsername());
    properties.put("spring.mail.password", ObserverProperties.getMailPassword());
    properties.put("spring.thymeleaf.cache",ObserverProperties.isEnableTemplateCaching());

    new SpringApplicationBuilder(Observer.class)
      .properties(properties)
      .build(args)
      .run();
  }


}
