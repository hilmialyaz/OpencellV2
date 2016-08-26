package org.meveo.api.wf;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.payment.WFActionDto;
import org.meveo.api.dto.payment.WFTransitionDto;
import org.meveo.api.dto.payment.WFTransitionRuleDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.admin.User;
import org.meveo.model.crm.Provider;
import org.meveo.model.wf.WFTransition;
import org.meveo.model.wf.WFTransitionRule;
import org.meveo.model.wf.Workflow;
import org.meveo.service.wf.WFTransitionService;
import org.meveo.service.wf.WFTransitionRuleService;
import org.meveo.service.wf.WorkflowService;

@Stateless
public class WFTransitionApi extends BaseApi {

	@Inject
	private WorkflowService workflowService;
	
	@Inject
	private WFActionApi wfActionApi;
	
	@Inject
	private WFTransitionService wfTransitionService;

    @Inject
    private WFTransitionRuleService wfTransitionRuleService;

	/**
	 * s
	 * @param wfTransitionDto
	 * @param currentUser
	 * @throws MissingParameterException
	 * @throws EntityDoesNotExistsException
	 * @throws EntityAlreadyExistsException
	 * @throws BusinessException
	 */
	public void create(WFTransitionDto wfTransitionDto, User currentUser) throws MissingParameterException, EntityDoesNotExistsException, EntityAlreadyExistsException, BusinessException  {
        validateDto(wfTransitionDto);
		Workflow workflow = workflowService.findByCode(wfTransitionDto.getWorkflowCode(), currentUser.getProvider());
		if(workflow == null) {
			throw new EntityDoesNotExistsException(Workflow.class, wfTransitionDto.getWorkflowCode());
		}		
		WFTransition wfTransition = wfTransitionService.findWFTransition(wfTransitionDto.getFromStatus(), wfTransitionDto.getToStatus(), wfTransitionDto.getPriority(), workflow.getCode(), currentUser.getProvider());
		if(wfTransition != null){
			throw new EntityAlreadyExistsException(WFTransition.class.getName() + "with workflow=" + workflow+
					" and FromStatus =" +wfTransitionDto.getFromStatus() + 
					" and ToStatus =" +wfTransitionDto.getToStatus() +
                    " and Priority =" +wfTransitionDto.getPriority());
		}

        Set<WFTransitionRule> wfTransitionRuleList = new HashSet<>();
        if (CollectionUtils.isNotEmpty(wfTransitionDto.getListWFTransitionRuleDto())) {
            for (WFTransitionRuleDto wfTransitionRuleDto : wfTransitionDto.getListWFTransitionRuleDto()) {
                WFTransitionRule wfTransitionRule = wfTransitionRuleService.getWFTransitionRule(wfTransitionRuleDto.getName(),
                        wfTransitionRuleDto.getValue(), wfTransitionRuleDto.getPriority(), wfTransitionRuleDto.getType(), currentUser.getProvider());
                if (wfTransitionRule == null) {
                    throw new EntityDoesNotExistsException(WFTransitionRule.class.getName() + "with name=" + wfTransitionRuleDto.getName() +
                            " and value =" + wfTransitionRuleDto.getValue() +
                            " and priority =" + wfTransitionRuleDto.getPriority() +
                            " and type =" + wfTransitionRuleDto.getType());
                }
                wfTransitionRuleList.add(wfTransitionRule);
            }
        }
		wfTransition = wfTransitionDto.fromDto(wfTransition);
		wfTransition.setWorkflow(workflow);
        wfTransition.setWfTransitionRules(wfTransitionRuleList);
		wfTransitionService.create(wfTransition, currentUser);
		if(wfTransitionDto.getListWFActionDto() != null && !wfTransitionDto.getListWFActionDto().isEmpty()){
			for(WFActionDto wfActionDto : wfTransitionDto.getListWFActionDto()){
				wfActionDto.setWfTransitionDto(wfTransitionDto);
				wfActionApi.create(wfActionDto, currentUser);
			}			
		}
	}

    /**
     * 	
     * @param wfTransitionDto
     * @param currentUser
     * @throws MissingParameterException
     * @throws EntityDoesNotExistsException
     * @throws EntityAlreadyExistsException
     * @throws BusinessException
     */
	public void update(WFTransitionDto wfTransitionDto, User currentUser) throws MissingParameterException, EntityDoesNotExistsException, EntityAlreadyExistsException, BusinessException  {
        validateDto(wfTransitionDto);		
		Workflow workflow = workflowService.findByCode(wfTransitionDto.getWorkflowCode(), currentUser.getProvider());
		if(workflow == null) {
			throw new EntityDoesNotExistsException(Workflow.class, wfTransitionDto.getWorkflowCode());
		}		
		WFTransition wfTransition = wfTransitionService.findWFTransition(wfTransitionDto.getFromStatus(), wfTransitionDto.getToStatus(), wfTransitionDto.getPriority(), workflow.getCode(),currentUser.getProvider());
		if(wfTransition == null){
			throw new EntityDoesNotExistsException(WFTransition.class.getName() + "with workflow=" + workflow+
					" and FromStatus =" +wfTransitionDto.getFromStatus() + 
					" and ToStatus =" +wfTransitionDto.getToStatus() +
                    " and Priority =" +wfTransitionDto.getPriority());
		}
        Set<WFTransitionRule> wfTransitionRuleList = new HashSet<>();
        if (CollectionUtils.isNotEmpty(wfTransitionDto.getListWFTransitionRuleDto())) {
            for (WFTransitionRuleDto wfTransitionRuleDto : wfTransitionDto.getListWFTransitionRuleDto()) {
                WFTransitionRule wfTransitionRule = wfTransitionRuleService.getWFTransitionRule(wfTransitionRuleDto.getName(),
                        wfTransitionRuleDto.getValue(), wfTransitionRuleDto.getPriority(), wfTransitionRuleDto.getType(), currentUser.getProvider());
                if (wfTransitionRule == null) {
                    throw new EntityDoesNotExistsException(WFTransitionRule.class.getName() + "with name=" + wfTransitionRuleDto.getName() +
                            " and value =" + wfTransitionRuleDto.getValue() +
                            " and priority =" + wfTransitionRuleDto.getPriority() +
                            " and type =" + wfTransitionRuleDto.getType());
                }
                wfTransitionRuleList.add(wfTransitionRule);
            }
        }
		wfTransition = wfTransitionDto.fromDto(wfTransition);
		wfTransition.setWorkflow(workflow);
        wfTransition.setWfTransitionRules(wfTransitionRuleList);
		wfTransitionService.update(wfTransition, currentUser);
		if(wfTransitionDto.getListWFActionDto() != null && !wfTransitionDto.getListWFActionDto().isEmpty()){
			for(WFActionDto wfActionDto : wfTransitionDto.getListWFActionDto()){
				wfActionDto.setWfTransitionDto(wfTransitionDto);
				wfActionApi.createOrUpdate(wfActionDto, currentUser);
			}			
		}
	}

	/**
	 * 
	 * @param workflowCode
	 * @param fromStatus
	 * @param toStatus
     * @param priority
	 * @param currentUser
	 * @return
	 * @throws MissingParameterException
	 * @throws EntityDoesNotExistsException
	 */
	public WFTransitionDto find(String workflowCode , String fromStatus, String toStatus, Integer priority, User currentUser) throws MissingParameterException, EntityDoesNotExistsException{
		WFTransitionDto wfTransitionDto = new WFTransitionDto(find(workflowCode, fromStatus, toStatus, priority, currentUser.getProvider()));
		return wfTransitionDto;
	}
	
	/**
	 * 
	 * @param workflowCode
	 * @param fromStatus
	 * @param toStatus
	 * @param currentUser
	 * @throws MeveoApiException
	 */
	public void remove(String workflowCode, String fromStatus, String toStatus, Integer priority, User currentUser) throws MeveoApiException{
		wfTransitionService.remove(find(workflowCode, fromStatus, toStatus, priority, currentUser.getProvider()));
	}

	/**
	 * 
	 * @param wfTransitionDto
	 * @param currentUser
	 * @throws MissingParameterException
	 * @throws EntityDoesNotExistsException
	 * @throws EntityAlreadyExistsException
	 * @throws BusinessException
	 */
	public void createOrUpdate(WFTransitionDto wfTransitionDto, User currentUser) throws MissingParameterException, EntityDoesNotExistsException, EntityAlreadyExistsException, BusinessException {		
		WFTransition wfTransition  = wfTransitionService.findWFTransition(wfTransitionDto.getFromStatus(), wfTransitionDto.getToStatus(), wfTransitionDto.getPriority(), wfTransitionDto.getWorkflowCode(),currentUser.getProvider());
		if(wfTransition == null) {
			create(wfTransitionDto, currentUser);
		} else {
			update(wfTransitionDto, currentUser);
		}
	}
	
	/**
	 * 
	 * @param wfTransitionDto
	 * @throws MissingParameterException
	 */
	public void validateDto(WFTransitionDto wfTransitionDto) throws MissingParameterException{
		if(wfTransitionDto == null){
			missingParameters.add("WFTransitionDto");
			handleMissingParameters();
		}		
		if (StringUtils.isBlank(wfTransitionDto.getFromStatus())) {
			missingParameters.add("FromStatus");
		}
		if (StringUtils.isBlank(wfTransitionDto.getToStatus())) {
			missingParameters.add("ToStatus");
		}			
		if (StringUtils.isBlank(wfTransitionDto.getWorkflowCode())) {
			missingParameters.add("WorkflowCode");
		}
        if (StringUtils.isBlank(wfTransitionDto.getPriority())) {
            missingParameters.add("Priority");
        }
        handleMissingParameters();
	}
	
	/**
	 * 
	 * @param workflowCode
	 * @param fromStatus
	 * @param toStatus
	 * @param provider
	 * @return
	 * @throws MissingParameterException
	 * @throws EntityDoesNotExistsException
	 */
	public WFTransition find(String workflowCode , String fromStatus, String toStatus, Integer priority, Provider provider) throws MissingParameterException, EntityDoesNotExistsException{
		if(StringUtils.isBlank(workflowCode)){
			missingParameters.add("workflowCode");
		}
		if(StringUtils.isBlank(fromStatus)){
			missingParameters.add("fromStatus");
		}
		if(StringUtils.isBlank(toStatus )){
			missingParameters.add("toStatus");
		}
        if(StringUtils.isBlank(priority)){
            missingParameters.add("priority");
        }
        handleMissingParameters();
		
		Workflow workflow = workflowService.findByCode(workflowCode,provider);
		if(workflow == null) {
			throw new EntityDoesNotExistsException(Workflow.class, workflowCode);
		}		
		WFTransition wfTransition  = wfTransitionService.findWFTransition(fromStatus, toStatus, priority, workflow.getCode(),provider);
		if(wfTransition == null){
			throw new EntityDoesNotExistsException(WFTransition.class.getName() + "with workflowCode=" + workflowCode +
					" and fromStatus =" + fromStatus + 
					" and toStatus =" + toStatus +
                    " and priority =" + priority);
		}		
		return wfTransition;
	}
}


