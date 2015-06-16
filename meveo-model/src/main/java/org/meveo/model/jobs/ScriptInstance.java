/*
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.model.jobs;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.meveo.model.BusinessEntity;
import org.meveo.model.ExportIdentifier;

@Entity
@ExportIdentifier({ "code" })
@Table(name = "MEVEO_SCRIPT_INSTANCE", uniqueConstraints = @UniqueConstraint(columnNames = { "CODE" }))
@SequenceGenerator(name = "ID_GENERATOR", sequenceName = "MEVEO_SCRIPT_INSTANCE_SEQ")
public class ScriptInstance extends BusinessEntity  {

	private static final long serialVersionUID = -5517252645289726288L;

	@Column(name = "SCRIPT", nullable = false, length = 10000)
	@Size(max = 10000)
	private String script;

	@Enumerated(EnumType.STRING)
	@Column(name = "SRC_TYPE")
	ScriptTypeEnum scriptTypeEnum;


	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "MEVEO_SCRIPT_INSTANCE_PARAMS") 
	private Map<String, String> params = new HashMap<String, String>();

	public ScriptInstance(){

	}

	/**
	 * @return the params
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * @return the scriptTypeEnum
	 */
	public ScriptTypeEnum getScriptTypeEnum() {
		return scriptTypeEnum;
	}


	/**
	 * @param scriptTypeEnum the scriptTypeEnum to set
	 */
	public void setScriptTypeEnum(ScriptTypeEnum scriptTypeEnum) {
		this.scriptTypeEnum = scriptTypeEnum;
	}



	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}



	/**
	 * @param script the script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}




}