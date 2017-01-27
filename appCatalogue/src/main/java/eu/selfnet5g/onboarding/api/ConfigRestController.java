package eu.selfnet5g.onboarding.api;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import eu.selfnet5g.onboarding.model.Vim;
import eu.selfnet5g.onboarding.repo.VimRepository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@CrossOrigin
@RestController
@RequestMapping(value="/app-catalogue/config")
public class ConfigRestController {
	
	private Logger log = LoggerFactory.getLogger(ConfigRestController.class);

	@Autowired VimRepository vimRepository;
	
	@RequestMapping(value="/vims", method=RequestMethod.POST)
	@ApiOperation(value = "APP Package VIM Instance Configuration")
	@ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 400, message = "Bad Request")}) 
	public ResponseEntity<String> addVim(@RequestBody Vim vim) throws Exception {
		
		log.info("Received new VIM " + vim.getName() + " configuration request");
		
		Optional<Vim> vimDb = vimRepository.findByName(vim.getName());
		if (vimDb.isPresent()) {
			log.error("Cannot configure new VIM: conflict!");
			return new ResponseEntity<String>("VIM with name " + vim.getName() + " already configured", HttpStatus.CONFLICT);
		}
		
		Vim newVim = new Vim(vim.getName(),
							 vim.getType(),
							 vim.getUrl(),
							 vim.getUsername(),
							 vim.getPassword(),
							 vim.getTenant());
		
		if (!(checkVim(newVim))) {
			log.error("Cannot configure new VIM: missing data");
			return new ResponseEntity<String>("VIM data malformatted", HttpStatus.BAD_REQUEST);
		}
		vimRepository.save(newVim);
		
		log.info("Configured new VIM with name " + vim.getName());
		
		return new ResponseEntity<String>(vim.getName(), HttpStatus.CREATED);
	}
	
	private boolean checkVim(Vim vim) {
		if (vim.getName() == null ||
			vim.getType() == null ||
			vim.getUrl() == null ||
			vim.getUsername() == null ||
			vim.getPassword() == null ||
			vim.getTenant() == null) {
			return false;
		}			
		return true;
	}
	
	@RequestMapping(value="/vims/{vimName}", method=RequestMethod.DELETE)
	@ApiOperation(value = "APP Package VIM Instance Deletion")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Succes"),
        @ApiResponse(code = 404, message = "Not Found")}) 
	public ResponseEntity<String> deleteVim(@PathVariable String vimName) throws Exception {
		Optional<Vim> vimDb = vimRepository.findByName(vimName);
		if (!vimDb.isPresent()) {
			log.error("VIM with name " + vimName + " not found.");
			return new ResponseEntity<String>("VIM not found", HttpStatus.NOT_FOUND);
		}
		
		vimRepository.delete(vimDb.get());
		
		log.info("Deleted VIM with name " + vimName);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(value="/vims/{vimName}", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package VIM Instance Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success", response = Vim.class),
        @ApiResponse(code = 404, message = "Not Found")})
	public ResponseEntity<Vim> getVim(@PathVariable String vimName) throws Exception {
		Optional<Vim> vimDb = vimRepository.findByName(vimName);
		if (!vimDb.isPresent()) {
			log.error("VIM with name " + vimName + " not found.");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<Vim>(vimDb.get(), HttpStatus.OK);
	}
	
	@RequestMapping(value="/vims", method=RequestMethod.GET)
	@ApiOperation(value = "APP Package VIM Instances Query")
	@ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Not Found")})
	public Collection<Vim> getVims() throws Exception {
		return vimRepository.findAll();		
	}
}
