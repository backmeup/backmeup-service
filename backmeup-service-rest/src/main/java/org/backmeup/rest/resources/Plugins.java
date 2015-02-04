package org.backmeup.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.backmeup.model.AuthData;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.PluginConfigInfo;
import org.backmeup.model.Profile;
import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginConfigurationDTO;
import org.backmeup.model.dto.PluginConfigurationDTO.PluginConfigurationType;
import org.backmeup.model.dto.PluginDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.rest.auth.BackmeupPrincipal;

@Path("/plugins")
public class Plugins extends Base {
    public enum PluginSelectionType {
        Source,
        Sink,
        Action,
        All
    }

    @Context
    private SecurityContext securityContext;

    @RolesAllowed("user")
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PluginDTO> listPlugins( 
            @QueryParam("types") @DefaultValue("All") PluginSelectionType pluginType,
            @QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {

        Set<String> pluginIds = new HashSet<>();

        if ((pluginType == PluginSelectionType.Source) || (pluginType == PluginSelectionType.All)) {
            for (PluginDescribable desc : getLogic().getDatasources()) {
                pluginIds.add(desc.getId());
            }
        } else if ((pluginType == PluginSelectionType.Sink) || (pluginType == PluginSelectionType.All)) {
            for(PluginDescribable desc : getLogic().getDatasinks()) {
                pluginIds.add(desc.getId());
            }
        } else if ((pluginType == PluginSelectionType.Action) || (pluginType == PluginSelectionType.All)) {
            for(PluginDescribable desc : getLogic().getActions()) {
                pluginIds.add(desc.getId());
            }
        }

        List<PluginDTO> pluginList= new ArrayList<>();
        for(String pluginId : pluginIds) {
            pluginList.add(getPlugin(pluginId, false));
        }

        return pluginList;
    }

    @RolesAllowed("user")
    @GET
    @Path("/{pluginId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PluginDTO getPlugin(
            @PathParam("pluginId") String pluginId,
            @QueryParam("expandProfiles") @DefaultValue("false") boolean expandProfiles) {

        PluginDescribable pluginDescribable =  getLogic().getPluginDescribable(pluginId);
        PluginDTO pluginDTO = getMapper().map(pluginDescribable, PluginDTO.class);

        // TODO: check why id is not mapped automatically
        pluginDTO.setPluginId(pluginDescribable.getId());

        switch (pluginDescribable.getType()) {
        case Source:
            pluginDTO.setPluginType(PluginType.Source);
            break;

        case Sink:
            pluginDTO.setPluginType(PluginType.Sink);
            break;

        case SourceSink:
            pluginDTO.setPluginType(PluginType.SourceSink);
            break;

        default:
            break;
        }

        Set<Entry<Object, Object>> metadataProperties = pluginDescribable.getMetadata(new Properties()).entrySet();
        for (Entry<Object, Object> e : metadataProperties) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            pluginDTO.addMetadata(key, value);
        } 

        PluginConfigInfo pluginConfigInfo = getLogic().getPluginConfiguration(pluginId, "");
        if(pluginConfigInfo.hasAuthData()) {
            PluginConfigurationDTO pluginConfigDTO = getMapper().map(pluginConfigInfo, PluginConfigurationDTO.class);
            if ((pluginConfigInfo.getRedirectURL() != null) && (pluginConfigInfo.getRedirectURL() != "")) {
                pluginConfigDTO.setConfigType(PluginConfigurationType.oauth);
            } else {
                pluginConfigDTO.setConfigType(PluginConfigurationType.input);
            }
            pluginDTO.setAuthDataDescription(pluginConfigDTO);
        }

        if(pluginConfigInfo.hasConfigData()) {
            getMapper().map(pluginConfigInfo, pluginDTO);
        }

        if(expandProfiles){

        }

        return pluginDTO;
    }

    @RolesAllowed("user")
    @POST
    @Path("/{pluginId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PluginProfileDTO addPluginConfiguration(@PathParam("pluginId") String pluginId, PluginProfileDTO pluginProfile) {	
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        throwIfPluginNotAvailable(pluginId);

        Profile profile = new Profile();
        profile.setPluginId(pluginId);
        profile.setType(pluginProfile.getProfileType());
        profile.setUser(activeUser);

        if(pluginProfile.getAuthData() != null) {
            AuthData authData = getLogic().getPluginAuthData(pluginProfile.getAuthData().getId());
            profile.setAuthData(authData);
        }

        if (pluginProfile.getProperties() != null) {
            Map<String, String> profileProps = new HashMap<>();
            profileProps.putAll(pluginProfile.getProperties());
            profile.setProperties(profileProps);
        }

        if(pluginProfile.getOptions() != null) {
            List<String> profileOptions = new ArrayList<>(pluginProfile.getOptions());
            profile.setOptions(profileOptions);
        }

        profile = getLogic().addPluginProfile(profile);

        PluginProfileDTO profileDTO = getMapper().map(profile, PluginProfileDTO.class);

        return profileDTO;
    }

    @RolesAllowed("user")
    @GET
    @Path("/{pluginId}/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PluginProfileDTO getPluginConfiguration(
            @PathParam("pluginId") String pluginId, 
            @PathParam("profileId") String profileId, 
            @QueryParam("expandConfig") @DefaultValue("false") boolean expandConfig) {

        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        Profile profile = getLogic().getPluginProfile(Long.parseLong(profileId));

        if(!profile.getUser().getUserId().equals(activeUser.getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        PluginProfileDTO profileDTO = getMapper().map(profile, PluginProfileDTO.class);

        return profileDTO;
    }

    @RolesAllowed("user")
    @PUT
    @Path("/{pluginId}/{profileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PluginProfileDTO updatePluginConfiguration(
            @PathParam("pluginId") String pluginId, 
            @PathParam("profileId") String profileId, 
            @QueryParam("updateAuthData") @DefaultValue("false") boolean updateAuthData,
            @QueryParam("updateProperties") @DefaultValue("false") boolean updateProperties,
            @QueryParam("updateOptions") @DefaultValue("false") boolean updateOptions,
            PluginProfileDTO pluginProfile) {	
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();
        Long pId = Long.parseLong(profileId);
        Profile persistentProfile = getLogic().getPluginProfile(pId);
        if((!activeUser.getUserId().equals(persistentProfile.getUser().getUserId())) && 
                (pluginProfile.getProfileId() != Long.parseLong(profileId))) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        if ((!pId.equals(pluginProfile.getProfileId())) && (!pId.equals(persistentProfile.getId()))) {
            throw new WebApplicationException(Status.CONFLICT);
        }

        persistentProfile.setPluginId(pluginProfile.getPluginId());
        persistentProfile.setType(pluginProfile.getProfileType());

        if (updateAuthData) {
            AuthData authData = getLogic().getPluginAuthData(pluginProfile.getAuthData().getId());
            persistentProfile.setAuthData(authData);
        }

        if (updateProperties) {
            persistentProfile.getProperties().clear();
            persistentProfile.getProperties().putAll(pluginProfile.getProperties());
        }

        if (updateOptions) {
            persistentProfile.getOptions().clear();
            persistentProfile.getOptions().addAll(pluginProfile.getOptions());
        }

        persistentProfile = getLogic().updatePluginProfile(persistentProfile);

        PluginProfileDTO profileDTO = getMapper().map(persistentProfile, PluginProfileDTO.class);

        return profileDTO;
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{pluginId}/{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deletePluginConfiguration(@PathParam("pluginId") String pluginId, @PathParam("profileId") String profileId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        throwIfPluginNotAvailable(pluginId);

        Long pId = Long.parseLong(profileId);
        Profile profile = getLogic().getPluginProfile(pId);
        if(!activeUser.getUserId().equals(profile.getUser().getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        if(!profile.getPluginId().equals(pluginId)) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        getLogic().deleteProfile(pId);
    }

    @RolesAllowed("user")
    @POST
    @Path("/{pluginId}/authdata")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthDataDTO addAuthData(@PathParam("pluginId") String pluginId, AuthDataDTO authData) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        throwIfPluginNotAvailable(pluginId);

        // map dto to model class
        AuthData authDataModel = getMapper().map(authData, AuthData.class);
        authDataModel.setUser(activeUser);
        authDataModel.setPluginId(pluginId);

        authDataModel = getLogic().addPluginAuthData(authDataModel);

        AuthDataDTO authDataDTO = getMapper().map(authDataModel, AuthDataDTO.class);
        return authDataDTO;
    }

    @RolesAllowed("user")
    @GET
    @Path("/{pluginId}/authdata/{authdataId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthDataDTO getAuthData(@PathParam("pluginId") String pluginId, @PathParam("authdataId") String authDataId){
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        throwIfPluginNotAvailable(pluginId);

        Long aId = Long.parseLong(authDataId);
        AuthData authDataModel = getLogic().getPluginAuthData(aId);

        if(!authDataModel.getUser().getUserId().equals(activeUser.getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        return getMapper().map(authDataModel, AuthDataDTO.class);

    }

    @RolesAllowed("user")
    @GET
    @Path("/{pluginId}/authdata")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AuthDataDTO> listAuthData(@PathParam("pluginId") String pluginId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        throwIfPluginNotAvailable(pluginId);

        List<AuthData> authDatas = getLogic().listPluginAuthData(activeUser.getUserId());

        List<AuthDataDTO> authDTOS = new ArrayList<>();
        for(AuthData auth : authDatas) {
            if(!auth.getUser().getUserId().equals(activeUser.getUserId())) {
                throw new WebApplicationException(Status.FORBIDDEN);
            }
            if (auth.getPluginId().equals(pluginId)) {
                AuthDataDTO authDTO = getMapper().map(auth, AuthDataDTO.class);
                authDTOS.add(authDTO);
            }
        }
        return authDTOS;
    }

    @RolesAllowed("user")
    @PUT
    @Path("/{pluginId}/authdata/{authdataId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AuthDataDTO updateAuthData(
            @PathParam("pluginId") String pluginId,
            @PathParam("authdataId") String authDataId, 
            AuthDataDTO authData) {
        throw new UnsupportedOperationException();
    }

    @RolesAllowed("user")
    @DELETE
    @Path("/{pluginId}/authdata/{authdataId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAuthData(@PathParam("pluginId") String pluginId,	@PathParam("authdataId") String authDataId) {
        BackMeUpUser activeUser = ((BackmeupPrincipal)securityContext.getUserPrincipal()).getUser();

        throwIfPluginNotAvailable(pluginId);

        Long aId = Long.parseLong(authDataId);
        AuthData authDataModel = getLogic().getPluginAuthData(aId);

        if(!authDataModel.getUser().getUserId().equals(activeUser.getUserId())) {
            throw new WebApplicationException(Status.FORBIDDEN);
        }

        getLogic().deletePluginAuthData(aId);
    }

    private void throwIfPluginNotAvailable(String pluginId) {
        if(!getLogic().isPluginAvailable(pluginId)) {
            LOGGER.error("Plugin not available: " + pluginId);
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
}
