package build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Minifier implements Supplier<String> {
    private List<String> ignoreClassesList;

    public Minifier(String[] ignoreClasses) {
        ignoreClassesList = List.of(ignoreClasses);
        System.out.println("Minifier constructed");
    }

    public byte[] readAllFileAsBytes(String path) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path));
        byte[] bytes = fis.readAllBytes();
        fis.close();
        return bytes;
    }

    public String readAllFileAsString(String path) throws IOException {
        return new String(readAllFileAsBytes(path));
    }

    public String getClassName(File file) {
        return file.getName().replace(".class", "");
    }

    public String getClassNameAsByteArrayString(File file) {
        byte[] classNameBytes = getClassName(file).getBytes();
        StringBuilder stringBuilder = new StringBuilder(classNameBytes.length * 4).append("{");
        for (byte b : classNameBytes) {
            stringBuilder.append(b).append(",");
        }
        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).append("}").toString();
    }

    public String createMinifiedClass() throws Throwable {
        String ilyaPoteminSourcePath = "src/obfuscation/java/IlyaPoteminCodeObfuscatedTemplate.java";
        String ilyaPoteminSourceStr = readAllFileAsString(ilyaPoteminSourcePath);
        File[] classFiles = Objects.requireNonNull(new File("build/classes/java/main/").listFiles());
        ilyaPoteminSourceStr = ilyaPoteminSourceStr.replace(
                "IlyaPoteminCodeObfuscatedTemplate", "IlyaPoteminCodeMinified");
        StringBuilder implementationBuilder = new StringBuilder(1000);
        int counter = 0;
        for (File classFile : classFiles) {
            if (ignoreClassesList.contains(classFile.getName())) {
                System.out.println("Filtered out " + classFile.getAbsolutePath());
                continue;
            }
            FileInputStream classFis = new FileInputStream(classFile);
            byte[] classBytes = classFis.readAllBytes();
            classFis.close();
            StringBuilder builder = new StringBuilder(128 + classBytes.length * 4);
            builder.append("    public static class Implementation").append(counter).append(" {\n");
            builder.append("        ").append("@SuppressWarnings(\"unused\") @CompressedClassName(")
                    .append(getClassNameAsByteArrayString(classFile)).append(")\n");
            builder.append("        ").append("public static final byte[] b").append(" = {");
            for (byte b : classBytes) {
                builder.append(b).append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("};\n");
            builder.append("    }\n");
            implementationBuilder.append(builder);
            counter++;
        }
        return ilyaPoteminSourceStr.replace("/*IMPLEMENTATION*/", implementationBuilder);
    }

    @Override
    public String get() {
        try {
            return createMinifiedClass();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
