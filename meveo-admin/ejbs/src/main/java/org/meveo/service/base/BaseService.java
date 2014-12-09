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
package org.meveo.service.base;

import java.util.Random;

import javax.ejb.EJB;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.jboss.seam.security.Identity;
import org.meveo.model.admin.User;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.UserService;
import org.slf4j.Logger;

public abstract class BaseService {
	private static final Random RANDOM = new Random();

	@Inject
	Identity identity;

	@Inject
	protected Logger log;

	@Inject
	BeanManager beanManager;
	
	@EJB
	UserService userService;

	User currentUser;

	public User getCurrentUser() {
		if (currentUser == null) {
			try {
				currentUser = ((MeveoUser) identity.getUser()).getUser();
			} catch (Exception e) {
				log.warn("getCurrentUser cannot retrieve current user from session identity and currentUser has not been set programmatically");
				currentUser=userService.getSystemUser();
			}
		}
		return currentUser;
	}

	protected String generateRequestId() {
		return "MEVEOADMIN-" + String.valueOf(RANDOM.nextInt());
	}

	@SuppressWarnings("unchecked")
	protected <E> E getManagedBeanInstance(Class<E> beanClazz) {
		Bean<E> bean = (Bean<E>) beanManager.getBeans(beanClazz).iterator()
				.next();
		CreationalContext<E> ctx = beanManager.createCreationalContext(bean);
		return (E) beanManager.getReference(bean, beanClazz, ctx);
	}
}
