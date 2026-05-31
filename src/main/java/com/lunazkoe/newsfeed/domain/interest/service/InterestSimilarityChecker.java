package com.lunazkoe.newsfeed.domain.interest.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InterestSimilarityChecker {

    private static final LevenshteinDistance LEVENSHTEIN = new LevenshteinDistance();
    private static final double SIMILARITY_THRESHOLD = 0.80;  // 80%

    /**
     * 두 문자열의 유사도를 계산 (0.0 ~ 1.0)
     * <p>
     * Levenshtein Distance를 기반으로 계산: - 유사도 = 1 - (편집거리 / 최대길이)
     */
    public static double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return str1 == str2 ? 1.0 : 0.0;
        }

        String s1 = str1.toLowerCase().trim();
        String s2 = str2.toLowerCase().trim();

        // 정확히 같으면 100%
        if (s1.equals(s2)) {
            return 1.0;
        }

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = LEVENSHTEIN.apply(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * 두 문자열이 임계값 이상 유사한지 확인
     */
    public static boolean isSimilar(String str1, String str2) {
        return calculateSimilarity(str1, str2) >= SIMILARITY_THRESHOLD;
    }

    /**
     * 기존 이름들 중 유사한 것이 있는지 확인
     */
    public static boolean hasSimilarName(String newName, Iterable<String> existingNames) {
        for (String existingName : existingNames) {
            if (isSimilar(newName, existingName)) {
                return true;
            }
        }
        return false;
    }
}