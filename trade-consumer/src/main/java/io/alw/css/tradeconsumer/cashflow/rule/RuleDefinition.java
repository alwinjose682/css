package io.alw.css.tradeconsumer.cashflow.rule;

public sealed interface RuleDefinition permits CommonRules, OptionRules, MmRules, RepoRules, NdfRules, BondRules {
}
