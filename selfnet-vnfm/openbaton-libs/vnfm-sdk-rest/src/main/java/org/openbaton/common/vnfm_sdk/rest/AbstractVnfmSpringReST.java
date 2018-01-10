/*
 * Copyright (c) 2016 Open Baton (http://www.openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openbaton.common.vnfm_sdk.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openbaton.catalogue.nfvo.messages.Interfaces.NFVMessage;
import org.apache.http.HttpResponse;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.Configuration;
import org.openbaton.catalogue.nfvo.ConfigurationParameter;
import org.openbaton.catalogue.nfvo.VNFCStatistics;
import org.openbaton.catalogue.nfvo.messages.OrVnfmGenericMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmConfigureMessage;
import org.openbaton.catalogue.nfvo.messages.OrVnfmInstantiateMessage;
import org.openbaton.catalogue.nfvo.messages.VnfmOrStatisticsMessage;
import org.openbaton.common.vnfm_sdk.AbstractVnfm;
import org.openbaton.common.vnfm_sdk.exception.BadFormatException;
import org.openbaton.common.vnfm_sdk.exception.NotFoundException;
import org.openbaton.common.vnfm_sdk.rest.configuration.GsonDeserializerNFVMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;

//import javax.validation.Valid;

/**
 * Created by lto on 08/07/15.
 */
@SpringBootApplication
@RestController
public abstract class AbstractVnfmSpringReST extends AbstractVnfm {

  private VnfmRestHelper vnfmRestHelper;
  protected VnfmRabbitHelper vnfmRabbitHelper;
  @Autowired private ConfigurableApplicationContext context;

  @Autowired private Gson gson;

  @Bean
  Gson gson() {
    return new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(NFVMessage.class, new GsonDeserializerNFVMessage())
        .create();
  }

  @Override
  protected void setup() {
	this.vnfmRestHelper = (VnfmRestHelper) context.getBean("vnfmRestHelper");
    this.vnfmHelper = vnfmRestHelper;
    this.vnfmRabbitHelper = (VnfmRabbitHelper) context.getBean("vnfmRabbitHelper");
    super.setup();
  }

  @Override
  @PreDestroy
  protected void unregister() {
    vnfmRestHelper.unregister(vnfmManagerEndpoint);
  }

  @Override
  protected void register() {
    vnfmRestHelper.register(vnfmManagerEndpoint);
  }

  @RequestMapping(
    value = "/core-rest-actions",
    method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void receive(@RequestBody /*@Valid*/ String jsonNfvMessage) {

        log.debug("Received: " + jsonNfvMessage);
        NFVMessage message = null;
        JsonElement action = gson.fromJson(jsonNfvMessage, JsonObject.class).get("action");
        log.debug("json Action is: " + action.getAsString());
        if (action.getAsString().equals("INSTANTIATE"))
            message = gson.fromJson(jsonNfvMessage, OrVnfmInstantiateMessage.class);
        else if (action.getAsString().equals("CONFIGURE"))
                message = gson.fromJson(jsonNfvMessage, OrVnfmConfigureMessage.class);
        else
            message = gson.fromJson(jsonNfvMessage, NFVMessage.class);
        try {
            this.onAction(message, false);
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (BadFormatException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
  }
  
  @RequestMapping(
		  value = "/core-rest-actions/vnf-instance/{vnfId}/configure",
		  method = RequestMethod.POST,
		  consumes = MediaType.APPLICATION_JSON_VALUE)
  //@ResponseStatus(HttpStatus.OK)
  private ResponseEntity<String> configure(@PathVariable String vnfId,
		  							 @RequestBody Configuration configuration) {
      log.debug("Received POST message for vnf instance " + vnfId);
      log.debug("Requested VNF Configuration: " + configuration.toString());

      try {
          VirtualNetworkFunctionRecord vnfr = this.query(vnfId);
          
          OrVnfmConfigureMessage configMsg = new OrVnfmConfigureMessage();
          configMsg.setVnfr(vnfr);
          configMsg.setConfigration(configuration);
          
          this.onAction(configMsg, true);

          return new ResponseEntity<>("OK", HttpStatus.OK);
          
      } catch (Exception e) {
    	  log.error("Error during VNF Configuration: " + e.getMessage());
          return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
      } catch (BadFormatException e) {
    	  log.error("Error during VNF Configuration: " + e.getMessage());
    	  return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
      }
  }
  
  @RequestMapping(
		  value = "/core-rest-actions/vnf-instance/{vnfId}/configure",
		  method = RequestMethod.DELETE,
		  params={"cid"})
  //@ResponseStatus(HttpStatus.OK)
  private ResponseEntity<String> configurationDelete(@PathVariable String vnfId,
		  											 @RequestParam("cid") String cid) {
      log.debug("Received DELETE configuration message for vnf instance " + vnfId);
      log.debug("Requested Delete for VNF Configuration: " + cid);

      try {
          VirtualNetworkFunctionRecord vnfr = this.query(vnfId);
          
          OrVnfmConfigureMessage configMsg = new OrVnfmConfigureMessage();
          configMsg.setVnfr(vnfr);
          
          Configuration configuration = new Configuration();
          configuration.setName("delete-cid-" + cid);
          
          ConfigurationParameter paramCid = new ConfigurationParameter();
          ConfigurationParameter paramDelete = new ConfigurationParameter();
          
          paramCid.setConfKey("CID");
          paramCid.setValue(cid);
          paramDelete.setConfKey("DELETE");
          paramDelete.setValue("true");
          
          Set<ConfigurationParameter> configurationParameters = new HashSet<>();
          configurationParameters.add(paramCid);
          configurationParameters.add(paramDelete);
          
          configuration.setConfigurationParameters(configurationParameters);
          
          
          configMsg.setConfigration(configuration);
          
          this.onAction(configMsg, true);

          return new ResponseEntity<>("OK", HttpStatus.OK);
          
      } catch (Exception e) {
    	  log.error("Error during VNF Configuration: " + e.getMessage());
          return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); 
      } catch (BadFormatException e) {
    	  log.error("Error during VNF Configuration: " + e.getMessage());
    	  return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); 
      }
  }
  
  
  @RequestMapping(
		  value = "/core-rest-actions/vnf-instance/{vnfId}",
		  method = RequestMethod.GET)
  VirtualNetworkFunctionRecord receiveGet(@PathVariable String vnfId) {
      log.debug("Received GET message for vnf instance " + vnfId);

      try {
          return this.query(vnfId);
      } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
      }
  }
  
  @RequestMapping(
		  value = "/core-rest-actions/vnf-instance/{vnfId}/stats",
		  method = RequestMethod.GET)
  VnfmOrStatisticsMessage receiveGetStatistics(@PathVariable String vnfId) {
      log.debug("Received GET message for vnf instance " + vnfId);

      try {
          Set<VNFCStatistics> vnfStats = this.queryStats(vnfId);
          
          return new VnfmOrStatisticsMessage(vnfStats, Action.GET_STATISTICS);
          
      } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
      }
  }
}
