/**
 * Copyright (c) 2004-2006 Red Hat, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Red Hat, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Red Hat.
 */
package com.redhat.rhn.frontend.action.channel.manage;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelArchException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelNameException;
import com.redhat.rhn.frontend.xmlrpc.InvalidGPGKeyException;
import com.redhat.rhn.frontend.xmlrpc.InvalidGPGUrlException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.CreateChannelCommand;
import com.redhat.rhn.manager.channel.UpdateChannelCommand;
import com.redhat.rhn.manager.channel.InvalidGPGFingerprintException;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.DynaActionForm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * EditChannelAction
 * @version $Rev: 1 $
 */
public class EditChannelAction extends RhnAction {


    /** {@inheritDoc} */
    public ActionForward execute (ActionMapping mapping,
                                  ActionForm formIn,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
    
        ActionErrors errors = new ActionErrors();
        DynaActionForm form = (DynaActionForm)formIn;
        Map params = makeParamMap(request);
        RequestContext ctx = new RequestContext(request);

        if (!isSubmitted(form)) {
            setupForm(request, form);
            return getStrutsDelegate().forwardParams(
                    mapping.findForward("default"),
                    request.getParameterMap());
        }

        System.out.println("--------------------------------------");
        Enumeration paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            System.out.println((String) paramNames.nextElement());
        }
        System.out.println("--------------------------------------");
        Map map = form.getMap();
        for (Iterator itr = map.keySet().iterator(); itr.hasNext();) {
            System.out.println((String) itr.next());
        }
        System.out.println("--------------------------------------");

/*
channel.edit.jsp.createchannel
channel.edit.jsp.editchannel
*/
        if (ctx.hasParam("create_button")) {
            Long cid = create(form, errors, ctx);
            params.put("cid", cid);
        }
        else if (ctx.hasParam("edit_button")) {
            params.put("cid", ctx.getParam("cid", true));
            edit(form, errors, ctx);
        }

        if (!errors.isEmpty()) {
            addErrors(request, errors);
            prepDropdowns(new RequestContext(request));
            return getStrutsDelegate().forwardParams(
                    mapping.findForward("default"), 
                    params);
        }

        return getStrutsDelegate().forwardParams(
                mapping.findForward("success"), params);
    }
    private void edit(DynaActionForm form,
                      ActionErrors errors,
                      RequestContext ctx) {

        User loggedInUser = ctx.getLoggedInUser();

        // handle submission
        // why can't I just pass in a dictionary? sigh, there are
        // times where python would make this SOOOO much easier.
        UpdateChannelCommand ucc = new UpdateChannelCommand();
        ucc.setArchLabel((String)form.get("arch"));
        ucc.setLabel((String)form.get("label"));
        ucc.setName((String)form.get("name"));
        ucc.setSummary((String)form.get("summary"));
        ucc.setDescription((String)form.get("description"));
        ucc.setParentLabel(null);
        ucc.setUser(loggedInUser);
        ucc.setGpgKeyId((String)form.get("gpg_key_id"));
        ucc.setGpgKeyUrl((String)form.get("gpg_key_url"));
        ucc.setGpgKeyFp((String)form.get("gpg_key_fingerprint"));
        ucc.setMaintainerName((String)form.get("maintainer_name"));
        ucc.setMaintainerEmail((String)form.get("maintainer_email"));
        ucc.setMaintainerPhone((String)form.get("maintainer_phone"));
        ucc.setSupportPolicy((String)form.get("support_policy"));

        String parent = (String)form.get("parent");
        if (parent == null || parent.equals("")) {
            ucc.setParentId(null);
        }
        else {
            ucc.setParentId(Long.valueOf(parent));
        }

        try {
            ucc.update(ctx.getParamAsLong("cid"));
        }
        catch (InvalidGPGFingerprintException borg) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidgpgfp"));
        }
        catch (InvalidGPGKeyException dukat) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidgpgkey"));
        }
        catch (InvalidGPGUrlException khan) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidgpgurl"));
        }
        catch (InvalidChannelNameException ferengi) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidchannelname"));
        }
        catch (InvalidChannelLabelException q) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidchannellabel"));
        }
    }

    private Long create(DynaActionForm form,
                        ActionErrors errors,
                        RequestContext ctx) {

        User loggedInUser = ctx.getLoggedInUser();
        Long cid = null;

        // handle submission
        // why can't I just pass in a dictionary? sigh, there are
        // times where python would make this SOOOO much easier.
        CreateChannelCommand ccc = new CreateChannelCommand();
        ccc.setArchLabel((String)form.get("arch"));
        ccc.setLabel((String)form.get("label"));
        ccc.setName((String)form.get("name"));
        ccc.setSummary((String)form.get("summary"));
        ccc.setDescription((String)form.get("description"));
        ccc.setParentLabel(null);
        ccc.setUser(loggedInUser);
        ccc.setGpgKeyId((String)form.get("gpg_key_id"));
        ccc.setGpgKeyUrl((String)form.get("gpg_key_url"));
        ccc.setGpgKeyFp((String)form.get("gpg_key_fingerprint"));
        ccc.setMaintainerName((String)form.get("maintainer_name"));
        ccc.setMaintainerEmail((String)form.get("maintainer_email"));
        ccc.setMaintainerPhone((String)form.get("maintainer_phone"));
        ccc.setSupportPolicy((String)form.get("support_policy"));

        String parent = (String)form.get("parent");
        if (parent == null || parent.equals("")) {
            ccc.setParentId(null);
        }
        else {
            ccc.setParentId(Long.valueOf(parent));
        }

        try {
            Channel c = ccc.create();
            cid = c.getId();
        }
        catch (InvalidGPGFingerprintException borg) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidgpgfp"));
        }
        catch (InvalidGPGKeyException dukat) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidgpgkey"));
        }
        catch (InvalidGPGUrlException khan) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidgpgurl"));
        }
        catch (InvalidChannelNameException ferengi) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidchannelname"));
        }
        catch (InvalidChannelLabelException q) {
            errors.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("edit.channel.invalidchannellabel"));
        }

        return cid;
    }
    
    private void setupForm(HttpServletRequest request, DynaActionForm form) {
        RequestContext ctx = new RequestContext(request);
        prepDropdowns(ctx);
        Long cid = ctx.getParamAsLong("cid");

        if (cid != null) {
            Channel c = ChannelManager.lookupByIdAndUser(cid,
                                                         ctx.getLoggedInUser());

            form.set("name", c.getName());
            form.set("summary", c.getSummary());
            form.set("description", c.getDescription());
            form.set("org_sharing", c.getAccess());
            form.set("gpg_key_url", c.getGPGKeyUrl());
            form.set("gpg_key_id", c.getGPGKeyId());
            form.set("gpg_key_fingerprint", c.getGPGKeyFp());
            form.set("maintainer_name", c.getMaintainerName());
            form.set("maintainer_phone", c.getMaintainerPhone());
            form.set("maintainer_email", c.getMaintainerEmail());
            form.set("support_policy", c.getSupportPolicy());

            if (c.getParentChannel() != null) {
                request.setAttribute("parent_name",
                                     c.getParentChannel().getName());
            }
            else {
                request.setAttribute("parent_name",
                    LocalizationService.getInstance()
                                       .getMessage("generic.jsp.none"));
            }

            request.setAttribute("channel_label", c.getLabel());
            request.setAttribute("channel_name", c.getName());
            request.setAttribute("channel_arch", c.getChannelArch().getName());
        }
        else {
            request.setAttribute("channel_name", "");
        }
    }

    private void prepDropdowns(RequestContext ctx) {
        User loggedInUser = ctx.getLoggedInUser();
        // populate parent base channels
        List baseChannels = new ArrayList();
        List<Channel> bases = ChannelManager.findAllBaseChannelsForOrg(
                        loggedInUser.getOrg());

        LocalizationService ls = LocalizationService.getInstance();
        addOption(baseChannels, ls.getMessage("generic.jsp.none"), "");
        for (Channel c : bases) {
            addOption(baseChannels, c.getName(), c.getId().toString());
        }
        ctx.getRequest().setAttribute("parentChannels", baseChannels);
        
        // base channel arches
        List channelArches = new ArrayList();
        List<ChannelArch> arches = ChannelManager.getChannelArchitectures();
        for (ChannelArch arch : arches) {
            addOption(channelArches, arch.getName(), arch.getLabel());
        }
        ctx.getRequest().setAttribute("channelArches", channelArches);
    }
    
    /**
     * Utility function to create options for the dropdown.
     * @param options list containing all options.
     * @param key resource bundle key used as the display value.
     * @param value value to be submitted with form.
     */
    private void addOption(List options, String key, String value) {
        Map selection = new HashMap();
        selection.put("label", key);
        selection.put("value", value);
        options.add(selection);
    }

}
