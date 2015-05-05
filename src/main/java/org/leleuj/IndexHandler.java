package org.leleuj;

import static ratpack.groovy.Groovy.groovyTemplate;

import java.util.HashMap;
import java.util.Map;

import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.http.client.BasicAuthClient;
import org.pac4j.http.client.FormClient;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.TwitterClient;
import org.pac4j.saml.client.Saml2Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.handling.Context;
import ratpack.http.Request;
import ratpack.pac4j.internal.Pac4jProfileHandler;
import ratpack.pac4j.internal.RatpackWebContext;

public class IndexHandler extends Pac4jProfileHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final static String PROFILE = "profile";

    @Override
    public void handle(final Context context) {
        logger.debug("Retrieving user profile...");
        getUserProfile(context)
            .onError(throwable -> {
                logger.debug("Cannot retrieve user profile: {}", throwable.getMessage());
                render(context, "");
            })
            .then(userProfile -> {
                logger.debug("User profile: {}", userProfile);
                if (userProfile.isPresent()) {
                    render(context, userProfile.get());
                } else {
                    render(context, "");
            }

        });
    }

    protected void render(final Context context, final Object profile) {
        final Map<String, Object> model = new HashMap<>();
        model.put(PROFILE, profile);
        final Request request = context.getRequest();
        final Clients clients = request.get(Clients.class);
        final WebContext webContext = new RatpackWebContext(context);

        context.blocking(() -> {
            final FacebookClient fbclient = (FacebookClient) clients.findClient(FacebookClient.class);
            final String fbUrl = fbclient.getRedirectionUrl(webContext);
            logger.debug("fbUrl: {}", fbUrl);
            model.put("facebookUrl", fbUrl);

            final TwitterClient twClient = (TwitterClient) clients.findClient(TwitterClient.class);
            final String twUrl = twClient.getRedirectionUrl(webContext);
            logger.debug("twUrl: {}", twUrl);
            model.put("twitterUrl", twUrl);

            final FormClient fmClient = (FormClient) clients.findClient(FormClient.class);
            final String fmUrl = fmClient.getRedirectionUrl(webContext);
            logger.debug("fmUrl: {}", fmUrl);
            model.put("formUrl", fmUrl);

            final BasicAuthClient baClient = (BasicAuthClient) clients.findClient(BasicAuthClient.class);
            final String baUrl = baClient.getRedirectionUrl(webContext);
            logger.debug("baUrl: {}", baUrl);
            model.put("baUrl", baUrl);

            final CasClient casClient = (CasClient) clients.findClient(CasClient.class);
            final String casUrl = casClient.getRedirectionUrl(webContext);
            logger.debug("casUrl: {}", casUrl);
            model.put("casUrl", casUrl);

            final Saml2Client samlClient = (Saml2Client) clients.findClient(Saml2Client.class);
            final String samlUrl = samlClient.getRedirectionUrl(webContext);
            logger.debug("samlUrl: {}", samlUrl);
            model.put("samlUrl", samlUrl);

            return null;
        }).onError(ex -> {
                throw new TechnicalException("Failed to redirect", ex);
        }).then(ignored -> context.render(groovyTemplate(model, "index.html")));
    }
}
