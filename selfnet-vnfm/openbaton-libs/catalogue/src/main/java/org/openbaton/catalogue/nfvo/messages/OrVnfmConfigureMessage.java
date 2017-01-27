package org.openbaton.catalogue.nfvo.messages;

import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.messages.Interfaces.OrVnfmMessage;

public class OrVnfmConfigureMessage extends OrVnfmMessage {
    private VirtualNetworkFunctionRecord vnfr;
    private Configuration configuration;

    public OrVnfmConfigureMessage() {
    	this.action = Action.CONFIGURE;
    }

    public OrVnfmConfigureMessage(VirtualNetworkFunctionRecord vnfr, Action action) {
        this.vnfr = vnfr;
        this.action = action;
    }

    public VirtualNetworkFunctionRecord getVnfr() {
        return vnfr;
    }

    public void setVnfr(VirtualNetworkFunctionRecord vnfr) {
        this.vnfr = vnfr;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public void setConfigration(Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Override
    public String toString() {
        return "OrVnfmConfigureMessage{" +
                "action=" + action +
                ", vnfr=" + vnfr +
                ", configuration=" + configuration +
                '}';
    }
}