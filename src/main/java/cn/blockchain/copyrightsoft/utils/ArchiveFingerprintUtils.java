package cn.blockchain.copyrightsoft.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ArchiveFingerprintUtils {

    private ArchiveFingerprintUtils() {
    }

    public static ArchiveFingerprint buildFingerprints(byte[] archiveBytes) throws IOException {
        Map<String, byte[]> entries = extractEntries(archiveBytes);
        if (entries.isEmpty()) {
            String fileHash = FileHashUtils.calculateSHA256(archiveBytes);
            return new ArchiveFingerprint(fileHash, fileHash, 100);
        }

        String normalizedHash = calculateNormalizedHash(entries);
        String semanticHash = calculateSemanticHash(entries);
        int semanticScore = semanticHash.equals(normalizedHash) ? 100 : 95;
        return new ArchiveFingerprint(normalizedHash, semanticHash, semanticScore);
    }

    public static int calculateSimilarityScore(String semanticHashA, String semanticHashB) {
        if (semanticHashA == null || semanticHashB == null) {
            return 0;
        }
        if (semanticHashA.equals(semanticHashB)) {
            return 100;
        }
        int distance = hammingDistance(hexToBytes(semanticHashA), hexToBytes(semanticHashB));
        int totalBits = Math.max(semanticHashA.length(), semanticHashB.length()) * 4;
        double ratio = 1.0d - ((double) distance / (double) totalBits);
        return (int) Math.max(0, Math.min(100, Math.round(ratio * 100)));
    }

    private static Map<String, byte[]> extractEntries(byte[] archiveBytes) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archiveBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String normalizedPath = normalizePath(entry.getName());
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                entries.put(normalizedPath, output.toByteArray());
            }
        } catch (IOException ignored) {
            // Not a zip package or cannot be read as zip.
        }
        return entries;
    }

    private static String calculateNormalizedHash(Map<String, byte[]> entries) {
        List<String> paths = new ArrayList<>(entries.keySet());
        Collections.sort(paths);
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            builder.append(path).append('|');
            builder.append(Base64.getEncoder().encodeToString(entries.get(path))).append('\n');
        }
        return FileHashUtils.calculateSHA256(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String calculateSemanticHash(Map<String, byte[]> entries) {
        List<String> normalizedChunks = new ArrayList<>();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
            String normalizedContent = normalizeContent(entry.getValue(), entry.getKey());
            normalizedChunks.add(entry.getKey() + "|" + normalizedContent);
        }
        normalizedChunks.sort(Comparator.naturalOrder());
        String joined = String.join("\n", normalizedChunks);
        return FileHashUtils.calculateSHA256(joined.getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizeContent(byte[] bytes, String path) {
        String lowerPath = path.toLowerCase();
        if (!(lowerPath.endsWith(".java")
                || lowerPath.endsWith(".js")
                || lowerPath.endsWith(".ts")
                || lowerPath.endsWith(".vue")
                || lowerPath.endsWith(".xml")
                || lowerPath.endsWith(".json")
                || lowerPath.endsWith(".md")
                || lowerPath.endsWith(".sql")
                || lowerPath.endsWith(".txt")
                || lowerPath.endsWith(".yml")
                || lowerPath.endsWith(".yaml"))) {
            return FileHashUtils.calculateSHA256(bytes);
        }

        String text = new String(bytes, StandardCharsets.UTF_8);
        text = text.replaceAll("(?s)/\\*.*?\\*/", "");
        text = text.replaceAll("(?m)//.*$", "");
        text = text.replaceAll("(?m)#.*$", "");
        text = text.replaceAll("\\s+", "");
        return text;
    }

    private static String normalizePath(String rawPath) {
        return rawPath.replace("\\", "/");
    }

    private static byte[] hexToBytes(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }

    private static int hammingDistance(byte[] a, byte[] b) {
        int len = Math.min(a.length, b.length);
        int distance = 0;
        for (int i = 0; i < len; i++) {
            distance += Integer.bitCount((a[i] ^ b[i]) & 0xFF);
        }
        distance += Math.abs(a.length - b.length) * 8;
        return distance;
    }

    public record ArchiveFingerprint(String normalizedHash, String semanticHash, int similarityBaseline) {
    }
}
