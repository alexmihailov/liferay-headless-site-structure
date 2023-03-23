package ru.tele2.liferay.headless.internal.jaxrs.application;

import javax.annotation.Generated;

import javax.ws.rs.core.Application;

import org.osgi.service.component.annotations.Component;

/**
 * @author atyutin
 * @generated
 */
@Component(
	property = {
		"liferay.jackson=false",
		"osgi.jaxrs.application.base=/liferay-headless-site-structure",
		"osgi.jaxrs.extension.select=(osgi.jaxrs.name=Liferay.Vulcan)",
		"osgi.jaxrs.name=LiferayHeadlessSiteStructure"
	},
	service = Application.class
)
@Generated("")
public class LiferayHeadlessSiteStructureApplication extends Application {
}