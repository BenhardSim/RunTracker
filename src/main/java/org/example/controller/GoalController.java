package org.example.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.example.entity.Goal;
import ratpack.core.handling.Context;
import ratpack.core.http.Status;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ratpack.core.jackson.Jackson.json;

public class GoalController {

    public static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public static CompletableFuture<Integer> totalCalories() {
        Firestore db = getFirestore();

        // berfungsi untuk mengambil data dari database secara async
        ApiFuture<QuerySnapshot> future = db.collection("activities").get();

        // mengambilData dari ApiFuture dan membuat menjadi promise
        Promise<QuerySnapshot> promise = Blocking.get(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                // Handle the exception
                throw new RuntimeException(e);
            }
        });

        // Create a CompletableFuture to hold the result
        CompletableFuture<Integer> resultFuture = new CompletableFuture<>();

        promise.onError(throwable -> {
            // Handle the error
            throwable.printStackTrace();
            resultFuture.completeExceptionally(new RuntimeException("Failed to count calories"));
        }).then(querySnapshot -> {
            if (querySnapshot != null) {

                // mapping data ke bentuk dictionary
                int totalCalories = 0;

                for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                    totalCalories += document.getLong("calories");
                }

                // Complete the CompletableFuture with the result
                resultFuture.complete(totalCalories);
            } else {
                // Handle the case where querySnapshot is null
                resultFuture.completeExceptionally(new RuntimeException("QuerySnapshot is null"));
            }
        });

        return resultFuture;
    }

    public static void getAllGoals(Context ctx) {
        Firestore db = getFirestore();
        // berfungsi untuk mengambil data dari database secara async
        ApiFuture<QuerySnapshot> future = db.collection("goals").get();

        // mengambilData dari ApiFuture dan membuat menjadi promise
        Promise<QuerySnapshot> promise = Blocking.get(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                // Handle the exception
                throw new RuntimeException(e);
            }
        });

        // menghitung jumlah total kalori yang sudah didapat
        System.out.println("count calories...");
        totalCalories().thenAccept(result -> {
            System.out.println("Jumlah total calorie saat ini : " + result);

            promise.onError(throwable -> {
                // Handle the error
                throwable.printStackTrace();
                ctx.getResponse().status(Status.BAD_REQUEST).send("Failed to read goals: " + throwable.getMessage());
            }).then(querySnapshot -> {
                if (querySnapshot != null) {
                    // mapping data ke bentuk dictionary
                    List<Map<String, Object>> processedAct = new ArrayList<>();

                    for(QueryDocumentSnapshot document : querySnapshot.getDocuments()){

                        Map<String, Object> goal = new HashMap<>();
                        String deadLine = document.getString("deadLine");
                        String desc = document.getString("desc");
                        long goalId = document.getLong("goalId");
                        long totalCal = document.getLong("totalCal");
                        String status = document.getString("status");

                        LocalDate targetDate = LocalDate.parse(deadLine, DateTimeFormatter.ISO_DATE);
                        LocalDate currentDate = LocalDate.now();

                        if(result > totalCal && !status.equals("Finish")){
                            status = "Finish";
                        } else if (targetDate.isBefore(currentDate) && !status.equals("Finish")) {
                            status = "DeadLine Exceeded";
                        }

                        // mapping data
                        goal.put("deadLine",deadLine);
                        goal.put("desc",desc);
                        goal.put("goalId",goalId);
                        goal.put("totalCal",totalCal);
                        goal.put("status",status);

                        processedAct.add(goal);
                    }

                    // conver ke json dan kirim
                    ctx.render(json(processedAct));
                } else {
                    ctx.getResponse().status(Status.NOT_FOUND).send("No Data Found");
                }
            });
        }).exceptionally(ex -> {
            ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Error retrieving total calories: " + ex.getMessage());
            return null;
        });
    }

    public static void getGoal(Context ctx){
        Firestore db = getFirestore();
        String goalId = ctx.getPathTokens().get("id");

        DocumentReference docRef = db.collection("goals").document(goalId);

        Promise<DocumentSnapshot> promise = Blocking.get(() -> {
            try {
                ApiFuture<DocumentSnapshot> apiFuture = docRef.get();
                return apiFuture.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // menghitung jumlah total kalori yang sudah didapat
        System.out.println("count calories...");
        totalCalories().thenAccept(result -> {

            promise.onError(throwable -> {
                // Handle the error
                throwable.printStackTrace();
                String escapedMessage = throwable.toString().replace("\"", "\'");
                escapedMessage = escapedMessage.replace("\n", " ");
                ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Failed to read goals: "+ escapedMessage +" \" \n}");
            }).then(querySnapshot -> {
                if(querySnapshot.exists()){
                    Map<String, Object> goal = new HashMap<>();
                    goal = querySnapshot.getData();
                    long totalCal = (long) goal.get("totalCal");
                    String status = (String) goal.get("status");
                    String deadLine = (String) goal.get("deadLine");
                    LocalDate targetDate = LocalDate.parse(deadLine, DateTimeFormatter.ISO_DATE);
                    LocalDate currentDate = LocalDate.now();

                    if(result > totalCal && !status.equals("Finish")){
                        goal.put("status", "Finish");
                    } else if (targetDate.isBefore(currentDate) && !status.equals("Finish")) {
                        goal.put("status", "DeadLine Exceeded");
                    }else {
                        goal.put("status", "DeadLine Exceeded");
                    }

                    ctx.render(json(goal));
                }else {
                    ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such goal with ID: "+ goalId +" \" \n}");
                }
            });

        }).exceptionally(ex -> {
            String escapedMessage = ex.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).contentType("application/json").send("{\"message\": \"Error retrieving total calories: "+ escapedMessage +" \" \n}");
            return null;
        });


    }

    public static void addGoal(Context ctx) {
        ctx.parse(Goal.class).onError(throwable -> {
            // Handle parsing errors
            String escapedMessage = throwable.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Invalid data format or type " + escapedMessage + " \" \n}");
        }).then(goal -> {
            Firestore db = getFirestore();

            String validationError = validateGoal(goal);
            if (validationError != null) {
                ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \""+ validationError +" \" \n}");
                return;
            }

            // Check if goalId exists
            DocumentReference docRef = db.collection("goals").document(String.valueOf(goal.getGoalId()));
            ApiFuture<DocumentSnapshot> future = docRef.get();
            Promise<DocumentSnapshot> checkPromise = Blocking.get(() -> future.get());

            checkPromise.onError(throwable -> {
                // Handle the error
                ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Failed to check goalId: " + throwable.getMessage());
            }).then(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // goalId exists, handle accordingly
                    ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Goal with the given ID already exists.\"}");
                } else {
                    // goalId does not exist, proceed with counting total calories and adding goal
                    totalCalories().thenAccept(result -> {
                        // Compare total calories with goal's calorie target
                        if(result >= goal.getTotalCal()){
                            goal.setStatus("Finished");
                        } else {
                            goal.setStatus("In Progress");
                        }

                        // Proceed to add goal
                        ApiFuture<WriteResult> addFuture = db.collection("goals").document(String.valueOf(goal.getGoalId())).set(goal);
                        Promise<WriteResult> addPromise = Blocking.get(() -> addFuture.get());

                        addPromise.onError(addThrowable -> {
                            // Handle the error
                            ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Failed to add goal: " + addThrowable.getMessage());
                        }).then(writeResult -> {
                            if (writeResult != null) {
                                Map<String, String> successMsg = new HashMap<>();
                                successMsg.put("status", "Success");
                                successMsg.put("message", "Goal added successfully");
                                ctx.render(json(successMsg));
                            } else {
                                ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Failed to add goal: Operation was unsuccessful.");
                            }
                        });
                    }).exceptionally(ex -> {
                        ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Error retrieving total calories: " + ex.getMessage());
                        return null;
                    });
                }
            });
        });
    }


    public static void updateGoal(Context ctx) {

        ctx.parse(Goal.class).onError(throwable -> {
            // Handle parsing errors
            String escapedMessage = throwable.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Invalid data format or type "+ escapedMessage +" \" \n}");
        }).then(goal -> {

            String validationError = validateGoal(goal);
            if (validationError != null) {
                ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \""+ validationError +" \" \n}");
                return;
            }

            // ambil key dari endpoint
            String goalId = ctx.getPathTokens().get("id");
            Firestore db = getFirestore();
            // berfungsi untuk mengirim data dari database secara async

            // convert menjadi bentuk dictionary
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("deadLine", goal.getDeadLine());
            updateData.put("totalCal", goal.getTotalCal());
            updateData.put("desc", goal.getDesc());

            // mengecek status pada saat di update
            String deadLine = goal.getDeadLine();
            LocalDate targetDate = LocalDate.parse(deadLine, DateTimeFormatter.ISO_DATE);
            LocalDate currentDate = LocalDate.now();

            // menghitung jumlah total kalori yang sudah didapat
            System.out.println("count calories...");
            totalCalories().thenAccept(result -> {
                System.out.println(result);

                // membandingkan jumlah kalori total saat ini dengan goals
                if(result >= goal.getTotalCal()){
                    goal.setStatus("Finish");
                }else if(targetDate.isAfter(currentDate)){
                    goal.setStatus("OnProgress"); // Replace 'defaultStatus' with your desired default value
                } else if (targetDate.isBefore(currentDate) && goal.getStatus() != "Finish") {
                    goal.setStatus("DeadLine Exceeded");
                } else {
                    goal.setStatus("Finish");
                }
                updateData.put("status",goal.getStatus());

                // update
                ApiFuture<WriteResult> future = db.collection("goals").document(String.valueOf(goalId)).update(updateData);
                Promise<WriteResult> promise = Blocking.get(() -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        // Handle the exception
                        throw new RuntimeException(e);
                    }
                });

                // check activity
                promise.onError(throwable -> {
                    // Handle the error
                    throwable.printStackTrace();
                    String escapedMessage = throwable.toString().replace("\"", "\'");
                    escapedMessage = escapedMessage.replace("\n", " ");
                    ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such goal - : "+ escapedMessage +" \" \n}");
                }).then(writeResult -> {
                    if (writeResult != null) {
                        Map<String, String> SuccesMsg = new HashMap<>();
                        SuccesMsg.put("status", "Success");
                        SuccesMsg.put("Message", "Goal updated successfully");
                        ctx.render(json(SuccesMsg));
                    } else {
                        // Handle the case where the operation did not succeed
                        ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such goal with ID: "+ goalId +" \" \n}");
                    }
                });
            }).exceptionally(ex -> {
                ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Error retrieving total calories: " + ex.getMessage());
                return null;
            });;
        });
    }


    public static void deleteGoal(Context ctx) {
        Firestore db = getFirestore();
        // ambil di dari parameter
        String goalId = ctx.getPathTokens().get("id");
        ApiFuture<WriteResult> future = db.collection("goals").document(goalId).delete();
        Promise<WriteResult> promise = Blocking.get(() -> {
            try {
                return future.get();
            } catch (Exception e) {
                // Handle the exception
                throw new RuntimeException(e);
            }
        });

        promise.onError(throwable -> {
            // Handle the error
            throwable.printStackTrace();
            ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such goal with ID: "+ goalId +" \" \n}");
        }).then(writeResult -> {
            if (writeResult != null) {
                Map<String, String> SuccesMsg = new HashMap<>();
                SuccesMsg.put("status", "Success");
                SuccesMsg.put("Message", "Goal deleted successfully");
                ctx.render(json(SuccesMsg));
            } else {
                // Handle the case where the operation did not succeed
                ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such goal with ID: "+ goalId +" \" \n}");
            }
        });
    }

    private static String validateGoal(Goal goal) {
        if (goal.getTotalCal() == 0) {
            return "Total Calories cannot be empty";
        }
        if (goal.getTotalCal() <= 0) {
            return "Total calories must be a positive integer";
        }
        if (goal.getDeadLine() == null || goal.getDeadLine().isEmpty()) {
            return "Date cannot be empty";
        }
        LocalDate currentDate = LocalDate.now();
        LocalDate deadlineDate = LocalDate.parse(goal.getDeadLine(), DateTimeFormatter.ISO_LOCAL_DATE);
        if (deadlineDate.isBefore(currentDate)) {
            return "Deadline cannot be in the past";
        }


        return null;
    }
}




