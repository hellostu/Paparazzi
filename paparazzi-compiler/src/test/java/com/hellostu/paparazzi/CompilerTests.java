package com.hellostu.paparazzi;

import com.google.testing.compile.JavaFileObjects;
import com.hellostu.paparazzi.compiler.PaparazziProcessor;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

/**
 * Created by stuartlynch on 20/05/2016.
 */
@RunWith(JUnit4.class)
public class CompilerTests {

    @Test
    public void testerClassListenerCodeGeneration() {
        JavaFileObject javaFileObject = JavaFileObjects.forSourceLines("HelloWorldListener",
                "package com.example.helloworld;",
                "",
                "import com.hellostu.paparazzi.Listener;",
                "import java.util.ArrayList;",
                "",
                "@Listener",
                "public interface HelloWorldListener {",
                "void onHelloWorld(String helloWorld);",
                "}");

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorldListeners", "package com.example.helloworld;",
                "",
                "import java.lang.Override;",
                "import java.lang.String;",
                "import java.util.ArrayList;",
                "",
                "class HelloWorldListeners implements HelloWorldListener {",
                "  private ArrayList<HelloWorldListener> listeners;",
                "",
                "  public HelloWorldListeners() {",
                "    listeners = new ArrayList<>();",
                "  }",
                "",
                "  public void addHelloWorldListener(HelloWorldListener listener) {",
                "    for(HelloWorldListener storedListener : listeners) {",
                "      if(listener == storedListener) {",
                "        return;",
                "      }",
                "    }",
                "    listeners.add(listener);",
                "  }",
                "",
                "  public void removeHelloWorldListener(HelloWorldListener listener) {",
                "    listeners.remove(listener);",
                "  }",
                "",
                "  @Override",
                "  public void onHelloWorld(String helloWorld) {",
                "    for(HelloWorldListener listener : listeners) {",
                "      listener.onHelloWorld(helloWorld);",
                "    }",
                "  }",
                "}");

        assert_().about(javaSource())
                .that(javaFileObject)
                .processedWith(new PaparazziProcessor())
                .compilesWithoutError()
                .and().generatesSources(javaFileObject2);
    }

    @Test
    public void testerClassWeakListenerCodeGeneration() {
        JavaFileObject javaFileObject = JavaFileObjects.forSourceLines("HelloWorldListener",
                "package com.example.helloworld;",
                "",
                "import com.hellostu.paparazzi.WeakListener;",
                "import java.util.ArrayList;",
                "",
                "@WeakListener",
                "public interface HelloWorldListener {",
                "void onHelloWorld(String helloWorld);",
                "}");

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("WeakHelloWorldListeners", "package com.example.helloworld;",
                "",
                "import java.lang.Override;",
                "import java.lang.String;",
                "import java.lang.ref.WeakReference;",
                "import java.util.ArrayList;",
                "import java.util.Iterator;",
                "",
                "class WeakHelloWorldListeners implements HelloWorldListener {",
                "  private ArrayList<WeakReference<HelloWorldListener>> listeners;",
                "",
                "  public WeakHelloWorldListeners() {",
                "    listeners = new ArrayList<>();",
                "  }",
                "",
                "  public void addWeakHelloWorldListener(HelloWorldListener listener) {",
                "    boolean shouldAddNew = true;",
                "    Iterator<WeakReference<HelloWorldListener>> iterator = listeners.iterator();",
                "    while(iterator.hasNext()) {",
                "      HelloWorldListener storedListener = iterator.next().get();",
                "      if(storedListener == null) {",
                "        iterator.remove();",
                "      } else if(storedListener == listener) {",
                "        shouldAddNew = false;",
                "      }",
                "    }",
                "    if(shouldAddNew) {",
                "      listeners.add(new WeakReference(listener));",
                "    }",
                "  }",
                "",
                "  public void removeWeakHelloWorldListener(HelloWorldListener listener) {",
                "    Iterator<WeakReference<HelloWorldListener>> iterator = listeners.iterator();",
                "    while(iterator.hasNext()) {",
                "      HelloWorldListener storedListener = iterator.next().get();",
                "      if(storedListener == null || storedListener == listener) {",
                "        iterator.remove();",
                "      }",
                "    }",
                "  }",
                "",
                "  @Override",
                "  public void onHelloWorld(String helloWorld) {",
                "    Iterator<WeakReference<HelloWorldListener>> iterator = listeners.iterator();",
                "    while(iterator.hasNext()) {",
                "      HelloWorldListener listener = iterator.next().get();",
                "      if(listener != null) {",
                "        listener.onHelloWorld(helloWorld);",
                "      } else {",
                "        iterator.remove();",
                "      }",
                "    }",
                "  }",
                "}");

        assert_().about(javaSource())
                .that(javaFileObject)
                .processedWith(new PaparazziProcessor())
                .compilesWithoutError()
                .and().generatesSources(javaFileObject2);
    }

    @Test
    public void testInnerClassListenerCodeGeneration() {
        JavaFileObject javaFileObject = JavaFileObjects.forSourceLines("HelloWorld",
                "package com.example.helloworld;",
                "",
                "import com.hellostu.paparazzi.Listener;",
                "import java.util.ArrayList;",
                "",
                "public class HelloWorld {",
                "@Listener",
                "public interface HelloWorldListener {",
                "void onHelloWorld(String helloWorld);",
                "}",
                "}");

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorld_HelloWorldListeners", "package com.example.helloworld;",
                "import java.lang.Override;",
                "import java.lang.String;",
                "import java.util.ArrayList;",
                "",
                "class HelloWorld_HelloWorldListeners implements HelloWorld.HelloWorldListener {",
                "  private ArrayList<HelloWorld.HelloWorldListener> listeners;",
                "",
                "  public HelloWorld_HelloWorldListeners() {",
                "    listeners = new ArrayList<>();",
                "  }",
                "",
                "  public void addHelloWorldListener(HelloWorld.HelloWorldListener listener) {",
                "    for(HelloWorld.HelloWorldListener storedListener : listeners) {",
                "      if(listener == storedListener) {",
                "        return;",
                "      }",
                "    }",
                "    listeners.add(listener);",
                "  }",
                "",
                "  public void removeHelloWorldListener(HelloWorld.HelloWorldListener listener) {",
                "    listeners.remove(listener);",
                "  }",
                "",
                "  @Override",
                "  public void onHelloWorld(String helloWorld) {",
                "    for(HelloWorld.HelloWorldListener listener : listeners) {",
                "      listener.onHelloWorld(helloWorld);",
                "    }",
                "  }",
                "}");

        assert_().about(javaSource())
                .that(javaFileObject)
                .processedWith(new PaparazziProcessor())
                .compilesWithoutError()
                .and().generatesSources(javaFileObject2);
    }

}
