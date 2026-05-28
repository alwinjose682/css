package io.alw.css.fosimulator.template.common;

import io.alw.css.domain.cashflow.FoCashMessage;
import io.alw.css.domain.cashflow.FoCashMessageBuilder;
import io.alw.css.domain.cashflow.TradeLink;
import io.alw.datagen.TestDataGeneratable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface MessageContext extends TestDataGeneratable permits FxCashMessageContext, MmCashMessageContext {
    FoCashMessage rootFoCashMessage();

    void setRootFoCashMessage(FoCashMessage rootFoCashMessage);

    <M extends MessageContext> List<FoCashMessage> mapToCashMessage(List<M> msgCtxs);

    Map<String, List<TradeLink>> allTradeLinks();

    default void addTradeLinks(List<TradeLink> tradeLinks) {
        Map<String, List<TradeLink>> tlsMap = tradeLinks.stream().collect(Collectors.groupingBy(tl -> {
            long tradeID = tl.relatedTradeID();
            int tradeVersion = tl.relatedTradeVersion();
            long foCashflowID = tl.relatedFoCashflowID();
            int foCashflowVersion = tl.relatedFoCashflowVersion();
            return getTradeLinksMapKey(tradeID, tradeVersion, foCashflowID, foCashflowVersion);
        }));


        tlsMap.forEach((id, tls) ->
                allTradeLinks().computeIfAbsent(id, k -> new ArrayList<>()).addAll(tls)
        );
    }

    /// Returns all trade links for root and bdr combined in a single list
    default List<TradeLink> getTradeLinksForRootAnd(FoCashMessageBuilder bdr) {
        String rootId = getTradeLinksMapKey(rootFoCashMessage().tradeID(), rootFoCashMessage().tradeVersion(), rootFoCashMessage().cashflowID(), rootFoCashMessage().cashflowVersion());
        String thisId = getTradeLinksMapKey(bdr.tradeID(), bdr.tradeVersion(), bdr.cashflowID(), bdr.cashflowVersion());
        var tls = new ArrayList<TradeLink>();
        var rootTl = allTradeLinks().get(rootId);
        var thisTl = allTradeLinks().get(thisId);

        if (rootTl != null) {
            tls.addAll(rootTl);
        }
        if (thisTl != null) {
            tls.addAll(thisTl);
        }

        return Collections.unmodifiableList(tls);
    }

    default String getTradeLinksMapKey(long tradeID, int tradeVersion, long foCashflowID, int foCashflowVersion) {
        return tradeID + "-" + tradeVersion + "-" + foCashflowID + "-" + foCashflowVersion;
    }
}
