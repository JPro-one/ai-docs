package one.jpro.platform.aidocs.gradle;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A dependency module with its resolved documentation artifacts, discovered at
 * configuration time and passed into the task as a serialized string input.
 * The POM chain holds the module's own POM first, then its parents upward.
 */
record ModuleSpec(String group, String name, String version,
                  File doc, File sources, File changelog, List<File> pomChain) {

    String encode() {
        String poms = pomChain.stream().map(ModuleSpec::encFile)
                .reduce((a, b) -> a + ";" + b).orElse("");
        return String.join("\t",
                enc(group), enc(name), enc(version),
                encFile(doc), encFile(sources), encFile(changelog), poms);
    }

    static ModuleSpec decode(String encoded) {
        String[] f = encoded.split("\t", -1);
        List<File> poms = f[6].isEmpty() ? List.of()
                : Stream.of(f[6].split(";")).map(ModuleSpec::decFile).toList();
        return new ModuleSpec(dec(f[0]), dec(f[1]), dec(f[2]),
                decFile(f[3]), decFile(f[4]), decFile(f[5]), poms);
    }

    List<File> files() {
        List<File> result = new ArrayList<>();
        if (doc != null) result.add(doc);
        if (sources != null) result.add(sources);
        if (changelog != null) result.add(changelog);
        result.addAll(pomChain);
        return result;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String dec(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String encFile(File f) {
        return f == null ? "" : URLEncoder.encode(f.getAbsolutePath(), StandardCharsets.UTF_8);
    }

    private static File decFile(String s) {
        return s.isEmpty() ? null : new File(URLDecoder.decode(s, StandardCharsets.UTF_8));
    }
}
