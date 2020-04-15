import com.company.Player;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class IlyaPoteminCodeObfuscatedTemplate implements Player {

    private static ImplementationClassLoader implementationClassLoaderLoader;
    private Player impl = implementationClassLoaderLoader.loadedPlayer();

    public IlyaPoteminCodeObfuscatedTemplate() {
        super();
    }

    @Override
    public void reset() {
        impl.reset();
    }

    @Override
    public int move(int opponentLastMove, int xA, int xB, int xC) {
        return impl.move(opponentLastMove, xA, xB, xC);
    }

    public String getEmail() {
        return impl.getEmail();
    }

    static {
        implementationClassLoaderLoader = new ImplementationClassLoader();
    }

    private static class ImplementationClassLoader extends ClassLoader {
        private Map<String, byte[]> bytecodeMap = new HashMap<>();
        private Map<String, Class<?>> classMap = new HashMap<>();

        public ImplementationClassLoader() {
            super();
            List<Field> classFields = new ArrayList<>(40);
            for (int i = 0; ; i++) {
                try {
                    Class<?> cl = Class.forName("Implementation$Implementation" + i);
                    classFields.add(cl.getDeclaredFields()[0]);
                } catch (ClassNotFoundException e) {
                    break;
                }
            }
            try {
                for (Field field : classFields) {
                    String className = new String(field.getAnnotation(CompressedClassName.class).value());
                    byte[] bytes = (byte[]) field.get(null);
                    bytecodeMap.put(className, bytes);
                }
                for (String className : bytecodeMap.keySet()) {
                    loadClass(className);
                }
            } catch (IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (bytecodeMap.containsKey(name)) {
                byte[] classBytes = bytecodeMap.get(name);
                Class<?> loadedClass = defineClass(name, classBytes, 0, classBytes.length);
                classMap.put(name, loadedClass);
                return loadedClass;
            }
            return super.findClass(name);
        }

        public Player loadedPlayer() {
            try {
                Class<?> playerClass = Objects.requireNonNull(classMap.get("IlyaPoteminCode"));
                return (Player) playerClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return null;
            }
        }
    }
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface CompressedClassName {
    byte[] value();
}

class Implementation {
    //@formatter:off
/*IMPLEMENTATION*/ //@formatter:on
}
