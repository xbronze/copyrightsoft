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

    /**
     * 构建压缩包双指纹。
     * <p>
     * 处理流程：
     * 1) 尝试把输入字节按 zip 解包为“路径 -> 文件内容”映射；
     * 2) 生成 normalizedHash（结构+原始内容导向，适合精确比对）；
     * 3) 生成 semanticHash（去注释/空白后导向，适合近似比对）；
     * 4) 若输入并非 zip，退化为对整包字节直接做 SHA-256。
     */
    public static ArchiveFingerprint buildFingerprints(byte[] archiveBytes) throws IOException {
        // zip 可解析时生成结构化指纹；不可解析时退化为原始文件哈希，保证流程可继续。
        Map<String, byte[]> entries = extractEntries(archiveBytes);
        if (entries.isEmpty()) {
            // 非 zip 或损坏 zip：按单文件场景处理，避免业务中断。
            String fileHash = FileHashUtils.calculateSHA256(archiveBytes);
            return new ArchiveFingerprint(fileHash, fileHash, 100);
        }

        // normalizedHash 用于“同包同内容”判定，semanticHash 用于“语义接近”判定。
        // normalizedHash：按“文件路径 + 文件原始内容”计算出来的摘要。
        // 你可以把它理解成“严格版指纹”：哪怕只改了一个空格、注释或文件路径，结果通常都会变。
        // 用途是判断“是不是同一份包内容”。
        String normalizedHash = calculateNormalizedHash(entries);
        // semanticHash：先把文本文件里的注释、空白等“外观差异”去掉，再做摘要。
        // 你可以把它理解成“宽松版指纹”：代码核心内容差不多时，结果更接近。
        // 用途是做相似度判断，减少仅格式改动导致的误判。
        String semanticHash = calculateSemanticHash(entries);
        int semanticScore = semanticHash.equals(normalizedHash) ? 100 : 95;
        return new ArchiveFingerprint(normalizedHash, semanticHash, semanticScore);
    }

    /**
     * 根据两个语义哈希估算相似度（0-100）。
     * <p>
     * 算法基于十六进制哈希转字节后的汉明距离：
     * - 完全相同返回 100；
     * - 差异越大分值越低；
     * - 长度差异会按位数惩罚。
     */
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

    /**
     * 将上传字节尽可能按 zip 解包为“文件路径 -> 文件内容字节”映射。
     * <p>
     * 业务意图：
     * 1) 为后续 normalizedHash / semanticHash 提供稳定输入；
     * 2) 仅提取“文件”而非目录，避免目录节点干扰内容指纹；
     * 3) 对非 zip 或损坏 zip 采用容错降级（返回空映射），由上层退化为整包哈希而不是直接失败。
     */
    private static Map<String, byte[]> extractEntries(byte[] archiveBytes) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archiveBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    // 目录没有实际文件内容，不参与指纹输入，直接跳过。
                    continue;
                }
                // 统一路径分隔符，消除 Windows/Linux 打包差异。
                String normalizedPath = normalizePath(entry.getName());
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = zis.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
                // 按“标准化路径 -> 原始字节”保存，供后续两类哈希复用。
                entries.put(normalizedPath, output.toByteArray());
            }
        } catch (IOException ignored) {
            // 允许非 zip 输入：上层会走退化逻辑，不在这里抛错中断。
        }
        return entries;
    }

    /**
     * 计算“结构化标准哈希”。
     * <p>
     * 对每个文件按路径排序后拼接：path|base64(content)，最后对拼接结果做 SHA-256。
     * 该哈希对路径和原始内容敏感，适合做强一致去重。
     */
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

    /**
     * 计算“语义哈希”。
     * <p>
     * 对文本类文件做去注释、去空白，再按路径拼接计算 SHA-256。
     * 该哈希对格式化和注释变更不敏感，更适合相似度评估。
     */
    private static String calculateSemanticHash(Map<String, byte[]> entries) {
        // 语义哈希强调“代码内容语义近似”，弱化路径/空白/注释差异。
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
        // 非文本类型直接返回原始字节哈希，避免二进制按文本解码导致噪声。
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
        // 去除注释与空白后再计算摘要，降低格式化改动对相似度的影响。
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
