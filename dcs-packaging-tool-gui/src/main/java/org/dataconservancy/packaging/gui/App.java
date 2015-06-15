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

import java.io.File;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;

import javafx.scene.text.Font;

/**
 * Entry point for application.
 */
public class App extends Application {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] {"classpath*:org/dataconservancy/config/applicationContext.xml",
        "classpath*:applicationContext.xml"});

        // min supported size is 800x600
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        Factory factory = (Factory)context.getBean("factory");
        factory.setStage(stage);

        Font.loadFont(App.class.getResource("/fonts/OpenSans-Regular.ttf").toExternalForm(), 14);
        
        Configuration config = factory.getConfiguration();
        CmdLineParser parser = new CmdLineParser(config);

        try {
            parser.parseArgument(getParameters().getRaw().toArray(new String[0]));
        } catch (CmdLineException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage());
            Platform.exit();
            return;
        }
        
        //Check if the package generation parameters file was found in the home directory 
        if (!getParameters().getRaw().contains("-gp") && !getParameters().getRaw().contains("--genparams")) {
            File userParamsFile = new File(System.getProperty("user.home"), ".packageToolParams");
            if (userParamsFile.exists()) {
                config.setPackageGenerationParamsFile(userParamsFile.getPath());
            }
        }

        Controller controller = factory.getController();

        controller.startApp();

        // Default size to 800x800, but shrink if screen is too small
        double sceneHeight = 800;

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        if (screen.getHeight() < 800) {
            sceneHeight = screen.getHeight()-50;
            if (sceneHeight < 550) sceneHeight = 550;
        }

        Scene scene = new Scene(controller.asParent(), 800, sceneHeight);
        scene.getStylesheets().add("/css/app.css");

        stage.getIcons().add(new Image("/images/DCPackageTool-icon.png"));
        stage.setTitle("DC Package Tool");
        stage.setScene(scene);
        stage.show();
    }
}
