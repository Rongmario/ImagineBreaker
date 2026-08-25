package zone.rong.imaginebreaker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import zone.rong.imaginebreaker.api.ImagineBreaker;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;

@Isolated
public class ClassPathTest {

    @Test
    public void addToSystemClassPathAppearsInUrls() throws Exception {
        ImagineBreaker ib = Index.get();
        URL[] before = ib.systemClassPath();
        Assertions.assertTrue(before.length > 0);

        File jar = File.createTempFile("imaginebreaker-cp", ".jar");
        jar.deleteOnExit();
        new JarOutputStream(new FileOutputStream(jar)).close();
        URL url = jar.toURI().toURL();
        ib.addToSystemClassPath(url);

        URL[] after = ib.systemClassPath();
        Assertions.assertTrue(after.length >= before.length);
        boolean found = false;
        for (int i = 0; i < after.length; i++) {
            if (after[i].sameFile(url)) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "added URL missing from system classpath");
    }

}
