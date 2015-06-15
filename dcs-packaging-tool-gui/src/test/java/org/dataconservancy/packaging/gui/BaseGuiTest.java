/*
 * Copyright 2014 Johns Hopkins University
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

package org.dataconservancy.packaging.gui;

import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/org/dataconservancy/config/applicationContext-test.xml"})
/**
 * Base test class that sets common presenter and view elements. 
 */
public abstract class BaseGuiTest {
    private static CountDownLatch javafx_setup_latch = null; 
    
    @Autowired
    protected Labels labels;

    @Autowired
    protected Messages messages;
    
    @Autowired
    protected Errors errors;

    @Autowired
    protected Configuration config;
    
    @Autowired
    protected OntologyLabels propertyLabels;

    @Autowired
    protected InternalProperties internalProperties;

    @Autowired
    protected Factory factory;
    
    @Autowired
    protected Help help;
/*
    @Before
    public void generalSetup() {
        labels = new Labels(ResourceBundle.getBundle("bundles/labels"));
        messages = new Messages(ResourceBundle.getBundle("bundles/messages"));
        config = new Configuration();
        factory = new Factory();

        config.setOntologyFile("fakefile");
    }*/
    
    public static class SkeletonApp extends Application {
        public void start(Stage stage) throws Exception {
            javafx_setup_latch.countDown();
        }            
    }
    
    /**
     * Wait until a JavaFX application is launched.
     * 
     * TODO: Use the regular app class and modify it to expose various properties to tests?
     * @throws InterruptedException
     */
    
    @BeforeClass
    public static void setupJavaFX() throws InterruptedException {
        if (javafx_setup_latch != null) {
            return;
        }
        
        javafx_setup_latch = new CountDownLatch(1);
        
        Runnable init = new Runnable() {
            @Override
            public void run() {
                Application.launch(SkeletonApp.class);
            }
        };        
        
        System.out.println("JavaFx initialising...");
        long timeMillis = System.currentTimeMillis();
        
        new Thread(init).start();

        javafx_setup_latch.await();
        
        System.out.println("JavaFx is initialised in " + (System.currentTimeMillis() - timeMillis) + "ms");
    }
}
