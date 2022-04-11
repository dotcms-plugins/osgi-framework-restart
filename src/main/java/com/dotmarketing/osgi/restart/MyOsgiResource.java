package com.dotmarketing.osgi.restart;

import com.dotcms.rest.InitDataObject;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.annotation.NoCache;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.PortletID;
import org.glassfish.jersey.server.JSONP;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/myosgi")
public class MyOsgiResource {

    private final WebResource webResource = new WebResource();
	private final MyOSGIUtil  myOSGIUtil  = MyOSGIUtil.getInstance();

	@POST
	@Path("/restart")
	@JSONP
	@NoCache
	@Produces({MediaType.APPLICATION_JSON, "application/javascript"})
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response doPost(@Context HttpServletRequest request,
						   @Context final HttpServletResponse response)  {

		final InitDataObject initDataObject = new WebResource.InitBuilder(webResource)
				.requiredBackendUser(true)
				.requiredFrontendUser(false)
				.requestAndResponse(request, response)
				.requiredPortlet(PortletID.DYNAMIC_PLUGINS.toString())
				.rejectWhenNoUser(true)
				.init();

		Logger.debug(this, ()-> "Doing OSGI restart cluster wide");
		myOSGIUtil.restartOsgiClusterWide();

		return Response.ok("Doing OSGI restart cluster wide").build();
	}

}
