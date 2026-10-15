package com.riten.test.core.models;

import com.day.cq.commons.Externalizer;
import com.day.cq.dam.commons.util.DamUtil;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LinkUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkUtil.class);

    private static final String HTML_SUFFIX = ".html";
    private static final String HTML_ANCHOR = ".html#";
    public static final String ANCHOR = "#";
    public static final String SLASH = "/";
    public static final String DOUBLE_SLASH = "//";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String FTP = "ftp";
    public static final String QUERY_PARAM = "?";
    public static final String DEFAULT_SITENAME = "publish";

    private LinkUtil() {
    }

    /**
     * @param resourceResolver
     * @param url              (internal, external or anchor link)
     * @return valid url or null!
     */
    public static String getLink(final ResourceResolver resourceResolver, final String url) {
        LOGGER.debug("Resolving link [{}]", url);
        if (isAnchor(url)) {
            LOGGER.debug("Detected anchor link: [{}]", url);
            return url;
        }
        if (isInternalUrl(url)) {
            LOGGER.debug("Detected internal URL: [{}], processing as internal link", url);
            return getLink(resourceResolver, url, null);
        } else {
            LOGGER.debug("Detected external URL: [{}], processing as external link", url);
            return getLink(resourceResolver, null, url);
        }

    }

    /**
     * equivalent to getLink(resourceResolver, internalUrl, externalUrl, true, null);
     *
     * @param resourceResolver
     * @param internalUrl
     * @param externalUrl
     * @return url with .html extension or null with internalUrl preferred over externalUrl
     */
    public static String getLink(final ResourceResolver resourceResolver, final String internalUrl, final String externalUrl) {
        return getLink(resourceResolver, internalUrl, externalUrl, true, null);
    }

    /**
     * equivalent to getLink(resourceResolver, internalUrl, externalUrl, true, defaultUrl);
     *
     * @param resourceResolver
     * @param internalUrl
     * @param externalUrl
     * @param defaultUrl
     * @return url with .html extension or 'defaultUrl' with internalUrl preferred over externalUrl
     */
    public static String getLink(final ResourceResolver resourceResolver, final String internalUrl, final String externalUrl, final String defaultUrl) {
        return getLink(resourceResolver, internalUrl, externalUrl, true, defaultUrl);
    }

    /**
     * Verifies whether a url is an anchor - that is startsWith '#' or contains '.html#'
     *
     * @param url
     * @return true if anchor
     */
    public static boolean isAnchor(final String url) {
        return StringUtils.isNotBlank(url) && (url.startsWith(ANCHOR) || url.contains(HTML_ANCHOR));
    }

    /**
     * Verifies whether a url is an internal link - note that 'anchor' links should be checked separately
     *
     * @param url
     * @return
     */
    public static boolean isInternalUrl(final String url) {
        return StringUtils.isNotBlank(url) && (url.startsWith(SLASH) && !url.startsWith(DOUBLE_SLASH));
    }

    /**
     * Verifies whether a url is considered an external url - not that 'anchor' links should be checked separately
     *
     * @param url
     * @return
     */
    public static boolean isExternalUrl(final String url) {
        return StringUtils.isNotBlank(url) && urlContainsExternalIdentifiers(url);
    }

    private static boolean urlContainsExternalIdentifiers(final String url) {
        return url.startsWith(DOUBLE_SLASH) || url.startsWith(HTTP) || url.startsWith(FTP) || url.contains(QUERY_PARAM);
    }

    /**
     * @param resourceResolver
     * @param internalUrl
     * @param externalUrl
     * @param preferInternalOverExternal
     * @param defaultUrl
     * @return url with .html extension or 'defaultUrl' with internalUrl preferred over externalUrl if preferInternalOverExternal is true or the other way around if false
     */
    public static String getLink(final ResourceResolver resourceResolver, final String internalUrl, final String externalUrl, final boolean preferInternalOverExternal, final String defaultUrl) {
        LOGGER.debug("getLink called with internalUrl=[{}], externalUrl=[{}], preferInternalOverExternal=[{}], defaultUrl=[{}]",
            internalUrl, externalUrl, preferInternalOverExternal, defaultUrl);

        String result;
        if (preferInternalOverExternal) {
            String internalLink = getInternalLink(resourceResolver, internalUrl);
            LOGGER.debug("getInternalLink result: [{}]", internalLink);
            String externalLink = getExternalLink(externalUrl);
            LOGGER.debug("getExternalLink result: [{}]", externalLink);
            result = getLinkOrDefault(internalLink, getLinkOrDefault(externalLink, defaultUrl));
            LOGGER.debug("preferInternalOverExternal=true, final result: [{}]", result);
        } else {
            String externalLink = getExternalLink(externalUrl);
            LOGGER.debug("getExternalLink result: [{}]", externalLink);
            String internalLink = getInternalLink(resourceResolver, internalUrl);
            LOGGER.debug("getInternalLink result: [{}]", internalLink);
            result = getLinkOrDefault(externalLink, getLinkOrDefault(internalLink, defaultUrl));
            LOGGER.debug("preferInternalOverExternal=false, final result: [{}]", result);
        }
        return result;
    }

    public static String getResourcePathFromUrl(String url) {
        if (url != null) {
            int index = url.indexOf(Constants.HTML_SUFFIX);
            if (index >= 0) {
                url = url.substring(0, index);
            }
            index = url.indexOf("//");
            if (index >= 0) {
                url = url.substring(index + 2, url.length());
            }
            index = url.indexOf("/");
            if (index >= 0) {
                url = url.substring(index, url.length());
            }
        }
        return url;
    }

    private static String getExternalLink(final String externalUrl) {
        if (isExternalUrl(externalUrl)) {
            LOGGER.debug("URL [{}] is valid external URL, returning as-is", externalUrl);
            return externalUrl;
        }
        LOGGER.debug("URL [{}] is not a valid external URL, returning null", externalUrl);
        return null;
    }

    public static String getInternalLink(final ResourceResolver resourceResolver, final String internalUrl) {
        LOGGER.debug("getInternalLink called with internalUrl=[{}]", internalUrl);
        if (isAnchor(internalUrl)) {
            LOGGER.debug("Anchor detected for internalUrl=[{}], returning as-is", internalUrl);
            return internalUrl;
        }
        if (isInternalUrl(internalUrl)) {
            LOGGER.debug("Internal URL format confirmed for [{}], attempting resource resolution", internalUrl);
            final Resource resource = resourceResolver.getResource(internalUrl);
            if (resource != null) {
                LOGGER.debug("Resource found at path [{}], type=[{}]", internalUrl, resource.getResourceType());
                String result = getValidInternalLinkFromResource(resource, internalUrl);
                LOGGER.debug("getValidInternalLinkFromResource returned: [{}]", result);
                return result;
            } else {
                LOGGER.debug("No resource found at path [{}]", internalUrl);
            }
        } else {
            LOGGER.debug("URL [{}] does not match internal URL format", internalUrl);
        }
        LOGGER.debug("getInternalLink returning null for internalUrl=[{}]", internalUrl);
        return null;
    }

    private static String getLinkOrDefault(final String link, final String defaultLink) {
        if (StringUtils.isNotBlank(link)) {
            return link;
        }
        return defaultLink;
    }

    private static String getValidInternalLinkFromResource(final Resource internalResource, final String internalUrl) {
        if (DamUtil.isAsset(internalResource)) {
            LOGGER.debug("Resource at [{}] is a DAM asset, returning URL as-is", internalUrl);
            return internalUrl;
        }
        final Page page = internalResource.adaptTo(Page.class);
        if (page != null) {
            LOGGER.debug("Resource at [{}] adapted to Page successfully, adding .html suffix", internalUrl);
            return addLinkSuffix(internalUrl);
        }
        LOGGER.debug("Resource at [{}] is neither an asset nor a page, returning null", internalUrl);
        return null;
    }

    private static String addLinkSuffix(final String link) {
        if (link.endsWith(HTML_SUFFIX) || (link.contains(HTML_ANCHOR))) {
            LOGGER.debug("Link [{}] already contains .html suffix or anchor, returning as-is", link);
            return link;
        }
        String result = link + HTML_SUFFIX;
        LOGGER.debug("Adding .html suffix to link [{}], result: [{}]", link, result);
        return result;
    }

    public static String replaceLanguage(final ResourceResolver resourceResolver, final String currentLink, final String currentLanguage, final String newLanguage) {

        String newLink = currentLink.replaceAll("/" + currentLanguage + "/", "/" + newLanguage + "/");
        String link = LinkUtil.getInternalLink(resourceResolver, newLink);

        if (link != null) {
            return link;
        }

        newLink = newLink.substring(0, newLink.indexOf(newLanguage) + newLanguage.length());
        link = LinkUtil.getInternalLink(resourceResolver, newLink);

        if (link != null) {
            return link;
        }

        return null;
    }

    public static boolean isHomePage(Page page, int homepageLevel) {
        if (page != null) {
            Page homePage = page.getAbsoluteParent(homepageLevel);
            if (homePage != null && homePage.getPath().equals(page.getPath())) {
                return true;
            }
        }
        return false;
    }


    public static LinkedHashMap<String, Link> calculateActiveStatesForNavItemsGivenCurrentPage(LinkedHashMap<String, Link> navigationItems, PageManager pageManager, Page currentPage, int homepageLevel, boolean isTargetGroupNav) {
        String activeUrl = null;
        int lowestDepth = 0;
        for (Link navigationItem : navigationItems.values()) {
            LOGGER.debug("Clean url: {}",navigationItem.getCleanUrl());
            Page navigationItemPage = pageManager.getPage(navigationItem.getCleanUrl());
            if (navigationItemPage != null && (currentPage.getPath() + "/").contains((navigationItemPage.getPath() + "/"))) {
                int navigationItemDepth = navigationItemPage.getDepth();
                if (navigationItemDepth > lowestDepth) {
                    lowestDepth = navigationItemDepth;
                    activeUrl = navigationItem.getCleanUrl();
                    LOGGER.debug("active url string &: {}",activeUrl);
                }
            }
        }
        if (activeUrl != null) {
            Link activeLink = navigationItems.get(activeUrl);
            if (!isHomePage(pageManager.getPage(activeUrl), homepageLevel) || isTargetGroupNav || currentPage.getPath().equals(activeUrl)) {
                activeLink.setActive(true);
                LOGGER.debug("active link &: {}",activeLink.getUrl());
                navigationItems.put(activeUrl, activeLink);
            }
        }
        return navigationItems;

    }

    public static String getExternalizerNameOfPage(Resource resource) {
        return DEFAULT_SITENAME;
    }

    public static String getExternalMappedLink(ResourceResolverFactory resolverFactory, Externalizer externalizer, String path) {
        ResourceResolver resolver = null;
        try {
            resolver = getAdminResourceResolver(resolverFactory);
            Resource pathResource = resolver.getResource(path);
            if (pathResource != null) {
                Page page = pathResource.adaptTo(Page.class);
                return externalizer.externalLink(resolver, sites.test.utils.LinkUtil.getExternalizerNameOfPage(page.getContentResource()), path);
            }
        } finally {
            if (resolver != null) {
                resolver.close();
            }
        }
        return path;
    }

    public static String getExternalMappedLink(ResourceResolver resolver, Externalizer externalizer, String path) {
        Resource pathResource = resolver.getResource(path);
        if (pathResource != null) {
            Page page = pathResource.adaptTo(Page.class);
            return externalizer.externalLink(resolver, sites.test.utils.LinkUtil.getExternalizerNameOfPage(page.getContentResource()), path);
        }
        return path;
    }


    public static ResourceResolver getAdminResourceResolver(ResourceResolverFactory resolverFactory) {
        try {
            Map<String, Object> param = new HashMap();
            param.put(ResourceResolverFactory.SUBSERVICE, "readWriteService");
            return resolverFactory.getServiceResourceResolver(param);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot obtain ResourceResolver", e);
        }
    }

}
