package org.example;

import org.example.controller.ActivityController;
import org.example.controller.GoalController;
import org.example.entity.Goal;
import org.example.firebaseConfig.FirebaseInitializer;
import org.example.util.CorsHandler;
import org.example.util.MyClientErrorHandler;
import org.example.util.MyServerErrorHandler;
import ratpack.core.error.ClientErrorHandler;
import ratpack.core.error.ServerErrorHandler;
import ratpack.core.handling.Context;
import ratpack.core.server.RatpackServer;
import ratpack.core.server.ServerConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String... args) throws Exception {
        FirebaseInitializer.initialize();
        new Main().runServer();
    }

    private void runServer() throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(ServerConfig.builder()
                        .port(5050) // Define the port here
                        .build())
                .registryOf(registry -> registry
                        .add(ClientErrorHandler.class, new MyClientErrorHandler())
                        .add(ServerErrorHandler.class, new MyServerErrorHandler())
                )
                .handlers(chain -> chain
                        .all(new CorsHandler())
                        // runing the User interface
                        .get("", ctx -> renderHtml(ctx, file("index.html")))

                        // get activities and goal data
                        .get("allactivity", ActivityController::getAllActivity)
                        .get("allgoals", GoalController::getAllGoals)

                        // get activities data summary
                        .get("summary", ActivityController::getStatistic)

                        // get activity and goal by id
                        .get("activity/:id",ActivityController::getActivity)
                        .get("goal/:id", GoalController::getGoal)

                        // post activities and goal data
                        .post("addactivity", ActivityController::addActivity)
                        .post("addgoal", GoalController::addGoal)

                        // update activities and goal data
                        .put("updateactivity/:id", ActivityController::updateActivity)
                        .put("updategoal/:id", GoalController::updateGoal)

                        // delete activities and goal data
                        .delete("deleteactivity/:id", ActivityController::deleteActivity)
                        .delete("deletegoal/:id", GoalController::deleteGoal)
                )
        );
    }


    private static String file(String fileName) {
        try {
            Path path = Paths.get(fileName);
            return Files.readString(path);
        } catch (Exception e) {
            throw new RuntimeException("Error loading file: " + fileName, e);
        }
    }

    private static void renderHtml(Context ctx, String content) {
        ctx.getResponse().contentType("text/html");
        ctx.render(content);
    }
}


