package com.fennel.aceinterview.utils;

import java.util.BitSet;

public class SimpleBloomFilter {

    private final BitSet bitSet;
    private final int bitSetSize;   // 位数组的大小
    private final int numHashFunctions; // 哈希函数的数量
    private final long expectedInsertions; // 预期插入数量 (用于日志或参考)
    private final double fpp;             // 期望误判率 (用于日志或参考)

    /**
     * 构造一个简单的布隆过滤器.
     *
     * @param expectedInsertions 预期插入的元素数量.
     * @param fpp                期望的误判率 (例如 0.01 for 1%).
     */
    public SimpleBloomFilter(long expectedInsertions, double fpp) {
        this.expectedInsertions = expectedInsertions;
        this.fpp = fpp;
        // 根据预期插入数量和误判率估算位数组大小和哈希函数数量
        // 这些是经典的估算公式
        this.bitSetSize = optimalM(expectedInsertions, fpp);
        this.numHashFunctions = optimalK(expectedInsertions, bitSetSize);
        this.bitSet = new BitSet(this.bitSetSize);

        System.out.printf(
                "SimpleBloomFilter initialized: expectedInsertions=%d, fpp=%.4f -> bitSetSize=%d, numHashFunctions=%d%n",
                expectedInsertions, fpp, this.bitSetSize, this.numHashFunctions
        );
    }

    /**
     * 估算最佳的位数组大小 (m).
     * m = - (n * ln(p)) / (ln(2)^2)
     * n: 预期插入数量
     * p: 期望误判率
     */
    private static int optimalM(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (int) Math.ceil(- (n * Math.log(p)) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 估算最佳的哈希函数数量 (k).
     * k = (m / n) * ln(2)
     * m: 位数组大小
     * n: 预期插入数量
     */
    private static int optimalK(long n, int m) {
        return Math.max(1, (int) Math.round(((double) m / n) * Math.log(2)));
    }

    /**
     * 将元素添加到布隆过滤器.
     * 注意：此实现不是线程安全的，如果并发添加，需要外部同步.
     *
     * @param element 要添加的元素 (这里假设是Long类型).
     */
    public void add(Long element) {
        if (element == null) {
            return;
        }
        // 为了简单，我们基于元素的toString()和一些变换来生成多个哈希值
        // 在实际应用中，应该使用多个独立的、高质量的哈希函数
        for (int i = 0; i < numHashFunctions; i++) {
            int hash = getHash(element.toString(), i);
            bitSet.set(Math.abs(hash % bitSetSize)); // 取模确保在位数组范围内
        }
    }

    /**
     * 检查元素是否可能存在于布隆过滤器中.
     * 返回 true 表示元素可能存在 (也可能是误判).
     * 返回 false 表示元素绝对不存在.
     *
     * @param element 要检查的元素 (这里假设是Long类型).
     * @return 如果元素可能存在则为 true, 否则为 false.
     */
    public boolean mightContain(Long element) {
        if (element == null) {
            return false; // 或者根据业务逻辑抛出异常
        }
        for (int i = 0; i < numHashFunctions; i++) {
            int hash = getHash(element.toString(), i);
            if (!bitSet.get(Math.abs(hash % bitSetSize))) {
                return false; // 只要有一个位为0，就说明元素肯定不存在
            }
        }
        return true; // 所有对应的位都为1，元素可能存在
    }

    /**
     * 一个非常简单的哈希函数生成器（用于演示）.
     * 基于元素的字符串表示和种子（哈希函数索引i）.
     * !!! 生产环境强烈建议使用更健壮的哈希算法 !!!
     */
    private int getHash(String value, int seed) {
        int h = seed; // 使用 seed 作为初始哈希值的一部分，以产生不同的哈希函数效果
        for (int i = 0; i < value.length(); i++) {
            h = 31 * h + value.charAt(i);
        }
        // 可以再做一些位操作使哈希更均匀，但这里保持简单
        // 例如: h ^= (h >>> 20) ^ (h >>> 12);
        // h = h ^ (h >>> 7) ^ (h >>> 4);
        return h;
    }

    // --- 以下为辅助/日志方法 ---
    public int getBitSetSize() {
        return bitSetSize;
    }

    public int getNumHashFunctions() {
        return numHashFunctions;
    }

    public long getExpectedInsertions() {
        return expectedInsertions;
    }

    public double getFpp() {
        return fpp;
    }

    public long getCurrentCardinality() {
        // BitSet.cardinality() 返回设置的位数，这不直接是元素数量
        // 对于布隆过滤器，估算已插入元素数量比较复杂，这里不实现
        return bitSet.cardinality(); // 返回实际置位的数量
    }
}
