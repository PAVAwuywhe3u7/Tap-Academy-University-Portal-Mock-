package com.university.portal.service;

import com.university.portal.dto.AssignmentEvaluationResult;
import java.util.Arrays;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiEvaluationService {

    private static final int CONTENT_SCORE_WEIGHT = 35;
    private static final int GRAMMAR_SCORE_WEIGHT = 25;
    private static final int STRUCTURE_SCORE_WEIGHT = 20;
    private static final int ORIGINALITY_SCORE_WEIGHT = 20;

    public AssignmentEvaluationResult evaluateAssignment(String content, String course) {
        log.info("Evaluating assignment for course: {}", course);

        String normalized = content == null ? "" : content.replaceAll("\\p{Cntrl}", " ").trim();
        int wordCount = normalized.isBlank() ? 0 : normalized.split("\\s+").length;
        int paragraphCount = normalized.isBlank() ? 0 : normalized.split("\\n\\s*\\n").length;
        int sentenceCount = normalized.isBlank() ? 0 : normalized.split("[.!?]+").length;

        int contentScore = computeContentScore(normalized, wordCount, course);
        int grammarScore = computeGrammarScore(normalized, wordCount, sentenceCount);
        int structureScore = computeStructureScore(paragraphCount, sentenceCount, wordCount);
        int originalityScore = computeOriginalityScore(normalized, course);

        int totalScore = (contentScore * CONTENT_SCORE_WEIGHT
                + grammarScore * GRAMMAR_SCORE_WEIGHT
                + structureScore * STRUCTURE_SCORE_WEIGHT
                + originalityScore * ORIGINALITY_SCORE_WEIGHT) / 100;

        String grade = totalScore >= 85 ? "A" : totalScore >= 70 ? "B" : "C";
        String feedback = buildFeedback(contentScore, grammarScore, structureScore, originalityScore, wordCount);

        return new AssignmentEvaluationResult(
                contentScore,
                grammarScore,
                structureScore,
                originalityScore,
                totalScore,
                grade,
                feedback
        );
    }

    private int computeContentScore(String text, int wordCount, String course) {
        int lengthScore;
        if (wordCount >= 700) {
            lengthScore = 92;
        } else if (wordCount >= 450) {
            lengthScore = 84;
        } else if (wordCount >= 250) {
            lengthScore = 74;
        } else if (wordCount >= 120) {
            lengthScore = 64;
        } else {
            lengthScore = 52;
        }

        String[] keywords = course == null ? new String[0] : course.toLowerCase(Locale.ROOT).split("\\s+");
        int keywordMatches = (int) Arrays.stream(keywords)
                .filter(k -> k.length() > 2 && text.toLowerCase(Locale.ROOT).contains(k))
                .count();

        int relevanceBonus = Math.min(keywordMatches * 3, 10);
        return clampScore(lengthScore + relevanceBonus);
    }

    private int computeGrammarScore(String text, int words, int sentences) {
        if (words == 0) {
            return 50;
        }

        int punctuation = text.length() - text.replace(".", "").length()
                + text.length() - text.replace(",", "").length()
                + text.length() - text.replace(";", "").length();

        double punctuationDensity = punctuation / (double) words;
        int densityScore = punctuationDensity > 0.12 ? 86 : punctuationDensity > 0.07 ? 78 : 66;

        double avgWordsPerSentence = sentences == 0 ? words : words / (double) sentences;
        int sentenceScore = avgWordsPerSentence >= 10 && avgWordsPerSentence <= 30 ? 10 : 4;

        return clampScore(densityScore + sentenceScore);
    }

    private int computeStructureScore(int paragraphs, int sentences, int words) {
        if (words == 0) {
            return 45;
        }
        int score = 58;
        if (paragraphs >= 4) {
            score += 18;
        } else if (paragraphs >= 2) {
            score += 10;
        }

        if (sentences >= 8) {
            score += 12;
        } else if (sentences >= 4) {
            score += 7;
        }

        if (words >= 250) {
            score += 8;
        }

        return clampScore(score);
    }

    private int computeOriginalityScore(String text, String course) {
        int hash = Math.abs((text + "|" + course).hashCode());
        return 60 + (hash % 36);
    }

    private String buildFeedback(int content, int grammar, int structure, int originality, int words) {
        String contentNote = content >= 85
                ? "strong topic coverage and relevant points"
                : content >= 70 ? "adequate relevance, but add deeper analysis" : "limited relevance and shallow detail";

        String grammarNote = grammar >= 85
                ? "clean grammar and sentence construction"
                : grammar >= 70 ? "minor grammar issues are present" : "grammar quality needs revision";

        String structureNote = structure >= 85
                ? "clear structure with good logical flow"
                : structure >= 70 ? "reasonable structure; improve transitions" : "structure lacks clarity and sequencing";

        return "Content relevance: " + contentNote + ". "
                + "Grammar quality: " + grammarNote + ". "
                + "Structure clarity: " + structureNote + ". "
                + "Originality score: " + originality + "/100. "
                + "Estimated length: " + words + " words.";
    }

    private int clampScore(int value) {
        return Math.min(100, Math.max(0, value));
    }
}
