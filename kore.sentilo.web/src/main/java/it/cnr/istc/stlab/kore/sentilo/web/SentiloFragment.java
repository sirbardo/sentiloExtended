/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.cnr.istc.stlab.kore.sentilo.web;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import it.cnr.istc.stlab.kore.sentilo.web.resources.SentiloResource;
import it.cnr.istc.stlab.kore.sentilo.web.writers.TheJitSerializer;
import it.cnr.istc.stlab.tipalo.web.LinkResource;
import it.cnr.istc.stlab.tipalo.web.NavigationLink;
import it.cnr.istc.stlab.tipalo.web.ScriptResource;
import it.cnr.istc.stlab.tipalo.web.WebFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Implementation of WebFragment for the FRED end-point.
 * 
 * @author anuzzolese
 * 
 */

@Component(immediate = true, metatype = true)
@Service(WebFragment.class)
public class SentiloFragment implements WebFragment {

    private static final String NAME = "sentilo";

    public static final String STATIC_RESOURCE_PATH = "/it/cnr/istc/stlab/kore/sentilo/web/static";

    private static final String TEMPLATE_PATH = "/it/cnr/istc/stlab/kore/sentilo/web/templates";
    
    private BundleContext bundleContext;
    
    @Override
    public String getName() {
        return NAME;
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        this.bundleContext = ctx.getBundleContext();
    }

    @Override
    public Set<Class<?>> getJaxrsResourceClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        // resources
        classes.add(SentiloResource.class);
        return classes;
    }

    @Override
    public Set<Object> getJaxrsResourceSingletons() {
        return Collections.emptySet();
    }

    @Override
    public String getStaticResourceClassPath() {
        return STATIC_RESOURCE_PATH;
    }

    @Override
    public TemplateLoader getTemplateLoader() {
        return new ClassTemplateLoader(getClass(), TEMPLATE_PATH);
    }

    @Override
    public List<LinkResource> getLinkResources() {
        List<LinkResource> resources = new ArrayList<LinkResource>();
        return resources;
    }

    @Override
    public List<ScriptResource> getScriptResources() {
        List<ScriptResource> resources = new ArrayList<ScriptResource>();
        resources.add(new ScriptResource("text/javascript", "scripts/prototype.js", this, 10));
        resources.add(new ScriptResource("text/javascript", "scripts/prototypeCors.js", this, 15));
        resources.add(new ScriptResource("text/javascript", "scripts/canviz.js", this, 20));
        resources.add(new ScriptResource("text/javascript", "scripts/path.js", this, 30));
        resources.add(new ScriptResource("text/javascript", "scripts/canvas2svg.js", this, 30));
//resources.add(new ScriptResource("text/javascript", "scripts/myCanviz.js", this, 30));
        
        return resources;
    }

    @Override
    public List<NavigationLink> getNavigationLinks() {
        List<NavigationLink> links = new ArrayList<NavigationLink>();
        links.add(new NavigationLink("sentilo", "sentilo", "/imports/sentilo_description.ftl", 50));
        return links;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

}
