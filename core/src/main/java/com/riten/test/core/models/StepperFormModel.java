/*
 * Copyright 2024 Riten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.riten.test.core.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * Sling Model for the dynamic Stepper Form component.
 * <p>
 * Dynamically loads form steps from the {@code items} child node of the
 * component's content resource. Authors can add, remove, or reorder steps in
 * the template {@code .content.xml} without any code changes.
 * </p>
 */
@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class StepperFormModel {

    @SlingObject
    private Resource resource;

    @ValueMapValue
    private String formId;

    private List<StepModel> steps = new ArrayList<>();

    @PostConstruct
    protected void init() {
        Resource itemsResource = resource.getChild("items");
        if (itemsResource != null) {
            int index = 0;
            for (Resource child : itemsResource.getChildren()) {
                String title = child.getValueMap().get("jcr:title", "Step " + (index + 1));
                steps.add(new StepModel(index, title, child.getPath(), child.getName()));
                index++;
            }
        }
    }

    /**
     * @return immutable list of all configured steps
     */
    public List<StepModel> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * @return optional form identifier (used as the HTML {@code id} attribute)
     */
    public String getFormId() {
        return formId;
    }

    /**
     * @return total number of configured steps
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * Represents a single step in the stepper form.
     */
    public static class StepModel {

        private final int index;
        private final String label;
        private final String path;
        private final String nodeName;

        public StepModel(int index, String label, String path, String nodeName) {
            this.index = index;
            this.label = label;
            this.path = path;
            this.nodeName = nodeName;
        }

        /** @return 0-based position of this step */
        public int getIndex() {
            return index;
        }

        /** @return 1-based display number shown in the left panel */
        public int getDisplayIndex() {
            return index + 1;
        }

        /** @return author-configured step title */
        public String getLabel() {
            return label;
        }

        /** @return absolute JCR path of the step resource */
        public String getPath() {
            return path;
        }

        /** @return JCR node name of the step resource (used for relative includes) */
        public String getNodeName() {
            return nodeName;
        }
    }
}
