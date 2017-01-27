package org.openbaton.catalogue.nfvo;

public class VNFCStatistics {
	private String vduHostname;
	private String statistics;
	
	public VNFCStatistics() {
		
	}
	
	public VNFCStatistics(String vduHostname, String statistics) {
		this.vduHostname = vduHostname;
		this.statistics = statistics;
	}

	public String getVduHostname() {
		return vduHostname;
	}

	public String getStatistics() {
		return statistics;
	}

	public void setVduHostname(String vduHostname) {
		this.vduHostname = vduHostname;
	}

	public void setStatistics(String statistics) {
		this.statistics = statistics;
	}
	
	
}
