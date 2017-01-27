package org.openbaton.catalogue.nfvo.messages;

import java.util.Set;

import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.VNFCStatistics;

public class VnfmOrStatisticsMessage extends VnfmOrGenericMessage {
	private Set<VNFCStatistics> vnf_statistics;
	
	public VnfmOrStatisticsMessage() {}
	
	public VnfmOrStatisticsMessage(Set<VNFCStatistics> stats, Action action) {
	    this.vnf_statistics = stats;
	    this.action = action;
	}

	public Set<VNFCStatistics> getVnf_statistics() {
		return vnf_statistics;
	}

	public void setVnf_statistics(Set<VNFCStatistics> vnf_statistics) {
		this.vnf_statistics = vnf_statistics;
	}

}
