package org.example.col_stu_ptj_sys.support;

import org.example.col_stu_ptj_sys.entity.UserCredit;

import java.util.Objects;

/**
 * 信用分采用 <strong>百分制 0～100</strong>，与「满分100」口径一致，等级与分数联动：
 * <ul>
 *   <li>90～100：优秀</li>
 *   <li>75～89：良好</li>
 *   <li>60～74：一般</li>
 *   <li>0～59：待提升</li>
 * </ul>
 * 与持久化中的 {@code credit_level} 码值（EXCELLENT/GOOD/NORMAL/RISKY）一一对应。
 */
public final class CreditLevelCalculator {

    private CreditLevelCalculator() {
    }

    public static int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    public static int clampScore(Integer score) {
        if (score == null) {
            return 0;
        }
        return clampScore(score.intValue());
    }

    /**
     * @return EXCELLENT / GOOD / NORMAL / RISKY
     */
    public static String levelCodeForScore(int score) {
        int s = clampScore(score);
        if (s >= 90) {
            return "EXCELLENT";
        }
        if (s >= 75) {
            return "GOOD";
        }
        if (s >= 60) {
            return "NORMAL";
        }
        return "RISKY";
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 以 <strong>score 为唯一真源</strong> 写回 {@link UserCredit} 的分与等级，避免历史库中
     * credit_level 与 score 不一致时界面出现「100 分仍是一般」等矛盾。
     *
     * @return 若实体被修正（与数据库可能不同步，需要 {@code updateById} 持久化）则为 true
     */
    public static boolean alignScoreAndLevel(UserCredit uc) {
        if (uc == null) {
            return false;
        }
        int s = clampScore(uc.getScore());
        String lvl = levelCodeForScore(s);
        String oldLvl = trimToNull(uc.getCreditLevel());
        boolean dirty = uc.getScore() == null
                || uc.getScore() != s
                || !Objects.equals(lvl, oldLvl);
        uc.setScore(s);
        uc.setCreditLevel(lvl);
        return dirty;
    }
}
