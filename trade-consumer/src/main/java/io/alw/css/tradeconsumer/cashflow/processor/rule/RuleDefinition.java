package io.alw.css.tradeconsumer.cashflow.processor.rule;

public sealed interface RuleDefinition permits CommonRules, OptionRules, MmRules, RepoRules, NdfRules, BondRules {
}
