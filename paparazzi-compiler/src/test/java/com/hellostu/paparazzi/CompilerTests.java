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
                "public class HelloWorldListeners implements HelloWorldListener {",
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
    public void testListenerWithGenericsGeneration() {
        JavaFileObject javaFileObject = JavaFileObjects.forSourceLines("HelloWorldListener",
                "package com.example.helloworld;",
                "",
                "import com.hellostu.paparazzi.Listener;",
                "import java.util.ArrayList;",
                "",
                "@Listener",
                "public interface HelloWorldListener<T> {",
                "void onHelloWorld(T helloWorld);",
                "}");

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorldListeners", "package com.example.helloworld;",
                "",
                "import java.lang.Override;",
                "import java.util.ArrayList;",
                "",
                "public class HelloWorldListeners<T> implements HelloWorldListener<T> {",
                "  private ArrayList<HelloWorldListener<T>> listeners;",
                "",
                "  public HelloWorldListeners() {",
                "    listeners = new ArrayList<>();",
                "  }",
                "",
                "  public void addHelloWorldListener(HelloWorldListener<T> listener) {",
                "    for(HelloWorldListener storedListener : listeners) {",
                "      if(listener == storedListener) {",
                "        return;",
                "      }",
                "    }",
                "    listeners.add(listener);",
                "  }",
                "",
                "  public void removeHelloWorldListener(HelloWorldListener<T> listener) {",
                "    listeners.remove(listener);",
                "  }",
                "",
                "  @Override",
                "  public void onHelloWorld(T helloWorld) {",
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

}
