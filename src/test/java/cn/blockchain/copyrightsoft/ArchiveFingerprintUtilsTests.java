package cn.blockchain.copyrightsoft;

import cn.blockchain.copyrightsoft.utils.ArchiveFingerprintUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ArchiveFingerprintUtilsTests {

    @Test
    void shouldKeepSemanticHashWhenOnlyCommentChanges() throws Exception {
        byte[] v1 = createZip(Map.of("src/Main.java", "public class Main { // comment A\n int value = 1; }\n"));
        byte[] v2 = createZip(Map.of("src/Main.java", "public class Main { // comment B\n int value = 1; }\n"));

        var fp1 = ArchiveFingerprintUtils.buildFingerprints(v1);
        var fp2 = ArchiveFingerprintUtils.buildFingerprints(v2);

        Assertions.assertNotEquals(fp1.normalizedHash(), fp2.normalizedHash());
        Assertions.assertEquals(fp1.semanticHash(), fp2.semanticHash());
        Assertions.assertEquals(100, ArchiveFingerprintUtils.calculateSimilarityScore(fp1.semanticHash(), fp2.semanticHash()));
    }

    @Test
    void shouldLowerSimilarityWhenCoreLogicChanges() throws Exception {
        byte[] v1 = createZip(Map.of("src/Main.java", "public class Main { int value = 1; }\n"));
        byte[] v2 = createZip(Map.of("src/Main.java", "public class Main { int value = 2; }\n"));

        var fp1 = ArchiveFingerprintUtils.buildFingerprints(v1);
        var fp2 = ArchiveFingerprintUtils.buildFingerprints(v2);
        int score = ArchiveFingerprintUtils.calculateSimilarityScore(fp1.semanticHash(), fp2.semanticHash());

        Assertions.assertTrue(score < 100);
    }

    private byte[] createZip(Map<String, String> files) throws IOException {
        Map<String, String> ordered = new LinkedHashMap<>(files);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> entry : ordered.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}
