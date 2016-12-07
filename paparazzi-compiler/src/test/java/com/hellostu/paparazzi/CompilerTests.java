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

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorldListeners", "package com.example.helloworld;\n" +
                "\n" +
                "import com.hellostu.paparazzi.Executor;\n" +
                "import java.lang.Override;\n" +
                "import java.lang.Runnable;\n" +
                "import java.lang.String;\n" +
                "import java.util.ArrayList;\n" +
                "\n" +
                "public class HelloWorldListeners implements HelloWorldListener {\n" +
                "  private ArrayList<HelloWorldListener> listeners;\n" +
                "\n" +
                "  private Executor executor;\n" +
                "\n" +
                "  public HelloWorldListeners(Executor executor) {\n" +
                "    this.listeners = new ArrayList<>();\n" +
                "    this.executor = executor;\n" +
                "  }\n" +
                "\n" +
                "  public void addHelloWorldListener(HelloWorldListener listener) {\n" +
                "    for(HelloWorldListener storedListener : listeners) {\n" +
                "      if(listener == storedListener) {\n" +
                "        return;\n" +
                "      }\n" +
                "    }\n" +
                "    listeners.add(listener);\n" +
                "  }\n" +
                "\n" +
                "  public void removeHelloWorldListener(HelloWorldListener listener) {\n" +
                "    listeners.remove(listener);\n" +
                "  }\n" +
                "\n" +
                "  @Override\n" +
                "  public void onHelloWorld(final String helloWorld) {\n" +
                "    for(HelloWorldListener listener : listeners) {\n" +
                "      this.executor.execute(new Runnable() {\n" +
                "        public void run() {\n" +
                "          listener.onHelloWorld(helloWorld);\n" +
                "        }\n" +
                "      }\n" +
                "      );\n" +
                "    }\n" +
                "  }\n" +
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

        JavaFileObject javaFileObject2 = JavaFileObjects.forSourceLines("HelloWorldListeners", "package com.example.helloworld;\n" +
                "\n" +
                "import com.hellostu.paparazzi.Executor;\n" +
                "import java.lang.Override;\n" +
                "import java.lang.Runnable;\n" +
                "import java.util.ArrayList;\n" +
                "\n" +
                "public class HelloWorldListeners<T> implements HelloWorldListener<T> {\n" +
                "  private ArrayList<HelloWorldListener<T>> listeners;\n" +
                "\n" +
                "  private Executor executor;\n" +
                "\n" +
                "  public HelloWorldListeners(Executor executor) {\n" +
                "    this.listeners = new ArrayList<>();\n" +
                "    this.executor = executor;\n" +
                "  }\n" +
                "\n" +
                "  public void addHelloWorldListener(HelloWorldListener<T> listener) {\n" +
                "    for(HelloWorldListener storedListener : listeners) {\n" +
                "      if(listener == storedListener) {\n" +
                "        return;\n" +
                "      }\n" +
                "    }\n" +
                "    listeners.add(listener);\n" +
                "  }\n" +
                "\n" +
                "  public void removeHelloWorldListener(HelloWorldListener<T> listener) {\n" +
                "    listeners.remove(listener);\n" +
                "  }\n" +
                "\n" +
                "  @Override\n" +
                "  public void onHelloWorld(final T helloWorld) {\n" +
                "    for(HelloWorldListener listener : listeners) {\n" +
                "      this.executor.execute(new Runnable() {\n" +
                "        public void run() {\n" +
                "          listener.onHelloWorld(helloWorld);\n" +
                "        }\n" +
                "      }\n" +
                "      );\n" +
                "    }\n" +
                "  }\n" +
                "}");

        assert_().about(javaSource())
                .that(javaFileObject)
                .processedWith(new PaparazziProcessor())
                .compilesWithoutError()
                .and().generatesSources(javaFileObject2);

    }

}
