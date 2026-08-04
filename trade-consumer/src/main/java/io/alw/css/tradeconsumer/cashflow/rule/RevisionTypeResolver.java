package io.alw.css.tradeconsumer.cashflow.rule;

import io.alw.css.domain.common.RevisionType;
import io.alw.css.domain.common.TradeEventAction;
import io.alw.css.domain.common.TradeEventType;
import io.alw.css.domain.common.TradeType;
import io.alw.css.domain.exception.CategorizedRuntimeException;
import io.alw.css.domain.exception.ExceptionSubCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.alw.css.tradeconsumer.model.constants.ExceptionSubCategoryType.REVISION_TYPE_RESOLUTION_FAILURE;

public final class RevisionTypeResolver {
    private static final Map<CashflowSequence, List<Rule>> commonRules = groupByCashflowOrder(CommonRules.rules);
    private static final Map<CashflowSequence, List<Rule>> mmRules = combineWithCommonRulesAndGroupByCashflowOrder(MmRules.rules);
    private static final Map<CashflowSequence, List<Rule>> ndfRules = combineWithCommonRulesAndGroupByCashflowOrder(NdfRules.rules);
    private static final Map<CashflowSequence, List<Rule>> bondRules = combineWithCommonRulesAndGroupByCashflowOrder(BondRules.rules);
    private static final Map<CashflowSequence, List<Rule>> repoRules = combineWithCommonRulesAndGroupByCashflowOrder(RepoRules.rules);
    private static final Map<CashflowSequence, List<Rule>> optionRules = combineWithCommonRulesAndGroupByCashflowOrder(OptionRules.rules);

    public static RevisionType resolve(boolean isInitialVersion, TradeType tradeType, TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        var tradeTypeSpecificRules = getRulesForTradeType(tradeType);
        Rule matchingRule = findMatchingRule(tradeTypeSpecificRules, isInitialVersion, tradeEventType, tradeEventAction);
        if (matchingRule == null) {
            throw CategorizedRuntimeException.TECHNICAL_UNRECOVERABLE("Unable to determine RevisionType from the given combination of inputs", new ExceptionSubCategory(REVISION_TYPE_RESOLUTION_FAILURE, null));
        }

        return matchingRule.result();
    }

    private static Rule findMatchingRule(Map<CashflowSequence, List<Rule>> tradeTypeSpecificRules, boolean isInitialVersion, TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        List<Rule> rulesForBothCashflowOrder = tradeTypeSpecificRules.get(CashflowSequence.BOTH);
        Rule matchedRule = findMatchingRule(rulesForBothCashflowOrder, tradeEventType, tradeEventAction);
        if (matchedRule == null) {
            CashflowSequence cashflowSequence = isInitialVersion ? CashflowSequence.INITIAL : CashflowSequence.SUBSEQUENT;
            List<Rule> rulesForASpecificCashflowOrder = tradeTypeSpecificRules.get(cashflowSequence);
            return findMatchingRule(rulesForASpecificCashflowOrder, tradeEventType, tradeEventAction);
        } else {
            return matchedRule;
        }
    }

    private static Rule findMatchingRule(List<Rule> rules, TradeEventType tradeEventType, TradeEventAction tradeEventAction) {
        if (rules == null) {
            return null;
        }

        for (Rule rule : rules) {
            for (TradeEventAndAction tea : rule.tradeEventAndActionRecords()) {
                if (tea.event() == tradeEventType && tea.action() == tradeEventAction) {
                    return rule;
                }
            }
        }

        return null;
    }

    private static Map<CashflowSequence, List<Rule>> getRulesForTradeType(TradeType tradeType) {
        return switch (tradeType) {
            case MM_TERM, MM_CALL -> mmRules;
            case PAYMENT -> commonRules;
            case FX -> commonRules;
            case FX_NDF -> ndfRules;
            case BOND -> bondRules;
            case REPO -> repoRules;
            case MM -> mmRules;
            case OPTION -> optionRules;
        };
    }

    private static Map<CashflowSequence, List<Rule>> combineWithCommonRulesAndGroupByCashflowOrder(List<Rule> rules) {
        var combined = new ArrayList<Rule>();
        combined.addAll(rules);
        combined.addAll(CommonRules.rules);
        return groupByCashflowOrder(combined);
    }

    private static Map<CashflowSequence, List<Rule>> groupByCashflowOrder(List<Rule> rules) {
        return rules.stream().collect(Collectors.groupingBy(Rule::cashflowSequence));
    }
}
