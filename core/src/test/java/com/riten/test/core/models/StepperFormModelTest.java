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

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import com.riten.test.core.testcontext.AppAemContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StepperFormModel}.
 */
@ExtendWith(AemContextExtension.class)
class StepperFormModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private Page page;
    private Resource stepperResource;

    @BeforeEach
    void setUp() {
        page = context.create().page("/content/testpage");

        // Create the stepper-form resource
        stepperResource = context.create().resource(page, "stepper",
            "sling:resourceType", "test/components/stepper-form",
            "formId", "myForm");

        // Create items container with three steps
        Resource items = context.create().resource(stepperResource, "items");
        context.create().resource(items, "step1", "jcr:title", "Personal Info");
        context.create().resource(items, "step2", "jcr:title", "Address Details");
        context.create().resource(items, "step3", "jcr:title", "Review & Submit");
    }

    @Test
    void testStepsLoadedFromItems() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model, "Model must adapt from resource");

        List<StepperFormModel.StepModel> steps = model.getSteps();
        assertEquals(3, steps.size(), "Should load exactly 3 steps");
    }

    @Test
    void testStepLabels() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);

        List<StepperFormModel.StepModel> steps = model.getSteps();
        assertEquals("Personal Info", steps.get(0).getLabel());
        assertEquals("Address Details", steps.get(1).getLabel());
        assertEquals("Review & Submit", steps.get(2).getLabel());
    }

    @Test
    void testStepIndices() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);

        List<StepperFormModel.StepModel> steps = model.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            assertEquals(i, steps.get(i).getIndex(), "0-based index must match position");
            assertEquals(i + 1, steps.get(i).getDisplayIndex(), "Display index must be 1-based");
        }
    }

    @Test
    void testStepPaths() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);

        List<StepperFormModel.StepModel> steps = model.getSteps();
        assertTrue(steps.get(0).getPath().endsWith("/step1"));
        assertTrue(steps.get(0).getNodeName().equals("step1"));
    }

    @Test
    void testFormId() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        assertEquals("myForm", model.getFormId());
    }

    @Test
    void testFormActionDefault() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        // no formAction set on the resource → should fall back to "#"
        assertEquals("#", model.getFormAction());
    }

    @Test
    void testFormMethodDefault() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        // no formMethod set → should fall back to "POST"
        assertEquals("POST", model.getFormMethod());
    }

    @Test
    void testFormActionAndMethodOverride() {
        Resource customStepperResource = context.create().resource(page, "customstepper",
            "sling:resourceType", "test/components/stepper-form",
            "formAction", "/bin/submitForm",
            "formMethod", "GET");

        StepperFormModel model = customStepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        assertEquals("/bin/submitForm", model.getFormAction());
        assertEquals("GET", model.getFormMethod());
    }

    @Test
    void testStepCount() {
        StepperFormModel model = stepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        assertEquals(3, model.getStepCount());
    }

    @Test
    void testEmptyItemsReturnsNoSteps() {
        Resource emptyStepperResource = context.create().resource(page, "emptystepper",
            "sling:resourceType", "test/components/stepper-form");
        // No items child created

        StepperFormModel model = emptyStepperResource.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        assertTrue(model.getSteps().isEmpty(), "Without items node, steps should be empty");
    }

    @Test
    void testStepWithoutTitleFallsBackToDefault() {
        Resource noTitleStepper = context.create().resource(page, "notitlestepper",
            "sling:resourceType", "test/components/stepper-form");
        Resource noTitleItems = context.create().resource(noTitleStepper, "items");
        context.create().resource(noTitleItems, "stepA");  // no jcr:title

        StepperFormModel model = noTitleStepper.adaptTo(StepperFormModel.class);
        assertNotNull(model);
        assertEquals(1, model.getStepCount());
        assertEquals("Step 1", model.getSteps().get(0).getLabel(),
            "Missing title should fall back to 'Step N'");
    }
}
