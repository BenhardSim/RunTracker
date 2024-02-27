package org.example.controller;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.example.entity.Activity;
import ratpack.core.handling.Context;
import ratpack.core.http.Status;
import ratpack.exec.Blocking;
import ratpack.exec.Promise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ratpack.core.jackson.Jackson.json;

public class ActivityController {

    public static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public static void getAllActivity(Context ctx) {
        Firestore db = getFirestore();
        // get the data inside the database in async
        ApiFuture<QuerySnapshot> future = db.collection("activities").get();

        // take the data from future and add it into a promise
        Promise<QuerySnapshot> promise = Blocking.get(() -> {
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
            String escapedMessage = throwable.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"Failed to read activity: "+ escapedMessage +" \"}");
        }).then(querySnapshot -> {

            if (querySnapshot != null) {
                // map into a list of dictionary
                List<Map<String, Object>> activities = querySnapshot.getDocuments().stream()
                        .map(QueryDocumentSnapshot::getData)
                        .collect(Collectors.toList());

                // convert to json and send
                ctx.render(json(activities));
            } else {
                ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"error\": \"Data not found\"}");
            }
        });
    }

    public static void getActivity(Context ctx){
        Firestore db = getFirestore();
        String activityId = ctx.getPathTokens().get("id");

        DocumentReference docRef = db.collection("activities").document(activityId);

        Promise<DocumentSnapshot> promise = Blocking.get(() -> {
            try {
                ApiFuture<DocumentSnapshot> apiFuture = docRef.get();
                return apiFuture.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        promise.onError(throwable -> {
            // Handle the error
            throwable.printStackTrace();
            String escapedMessage = throwable.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"Failed to read activity: "+ escapedMessage +" \" \n}");
        }).then(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // convert to json and send
                ctx.render(json(documentSnapshot.getData()));
            } else {
                ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such document with ID: "+ activityId +" \" \n}");
            }
        });
    }

    public static void getStatistic(Context ctx) {
        Firestore db = getFirestore();
        // taking data from database asynchronously
        ApiFuture<QuerySnapshot> future = db.collection("activities").get();

        // waiting data from database and wrap it inside promise
        Promise<QuerySnapshot> promise = Blocking.get(() -> {
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
            String escapedMessage = throwable.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"Failed to read activity: "+ escapedMessage +" \" \n}");
        }).then(querySnapshot -> {
            if (querySnapshot != null) {

                // mapping data ke bentuk dictionary
                int totalCalories = 0;
                int totalDistance = 0;
                int totalStep = 0;
                int totalDuration = 0;
                int TotalHBeat = 0;
                int AvgHBeat = 0;
                int total = 0;

                for(QueryDocumentSnapshot document : querySnapshot.getDocuments()){
                    total += 1;
                    totalCalories += document.getLong("calories");
                    totalDistance += document.getLong("distance") ;
                    totalStep += document.getLong("steps") ;
                    totalDuration += document.getLong("duration") ;
                    TotalHBeat += document.getLong("avgHeartBeat") ;

                    AvgHBeat = (TotalHBeat/total);
                }

                Map<String, Object> summary = new HashMap<>();
                summary.put("totalCalories",totalCalories);
                summary.put("totalDistance",totalDistance);
                summary.put("totalStep",totalStep);
                summary.put("totalDuration",totalDuration);
                summary.put("AvgHBeat",AvgHBeat);

                // conver ke json dan kirim
                ctx.render(json(summary));
            } else {
                ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"Failed to get all activities: Operation was unsuccessful.\" \n}");
            }
        });
    }

//    public static void addActivity(Context ctx) {
//        ctx.parse(Activity.class).onError(throwable -> {
//            // Handle parsing errors
//            String escapedMessage = throwable.toString().replace("\"", "\'");
//            escapedMessage = escapedMessage.replace("\n", " ");
//            ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Invalid data format or type "+ escapedMessage +" \" \n}");
//
//        }).then(activity -> {
//
//            String validationError = validateActivity(activity);
//            if (validationError != null) {
//                System.out.println("Data empty..");
//                ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \""+ validationError +" \" \n}");
//                return;
//            }
//
//            Firestore db = getFirestore();
//
//            // mengirim data ke dalam database secara async
//            ApiFuture<WriteResult> future = db.collection("activities").document(String.valueOf(activity.getActivityId())).set(activity);
//
//            Promise<WriteResult> promise = Blocking.get(() -> {
//                try {
//                    return future.get();
//                } catch (Exception e) {
//                    // Handle the exception
//                    throw new RuntimeException(e);
//                }
//            });
//
//            promise.onError(throwable -> {
//                // Handle the error
//                throwable.printStackTrace();
//                String escapedMessage = throwable.toString().replace("\"", "\'");
//                escapedMessage = escapedMessage.replace("\n", " ");
//                ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).contentType("application/json").send("{\"message\": \"Failed to add activity: "+ escapedMessage +" \" \n}");
//            })
//            .then(writeResult -> {
//                if (writeResult != null) {
//                    Map<String, String> SuccesMsg = new HashMap<>();
//                    SuccesMsg.put("status", "Success");
//                    SuccesMsg.put("Message", "Activity added successfully");
//                    ctx.render(json(SuccesMsg));
//                } else {
//                    // Handle the case where the operation did not succeed
//                    ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).send("Failed to add activity: Operation was unsuccessful.");
//                }
//            });
//
//
//
//        });
//    }

    public static void addActivity(Context ctx) {
        ctx.parse(Activity.class).onError(throwable -> {
            // Handle parsing errors
            String escapedMessage = throwable.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Invalid data format or type " + escapedMessage + " \" \n}");
        }).then(activity -> {
            String validationError = validateActivity(activity);
            if (validationError != null) {
                System.out.println("Data empty..");
                ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"" + validationError + " \" \n}");
                return;
            }

            Firestore db = getFirestore();

            // get if the id is already inside database
            DocumentReference docRef = db.collection("activities").document(String.valueOf(activity.getActivityId()));

            // Check if the document with the given ID already exists
            ApiFuture<DocumentSnapshot> future = docRef.get();
            Promise<DocumentSnapshot> promise = Blocking.get( () -> future.get());

            promise.onError(throwable -> {

                // Handle the error during document existence check
                throwable.printStackTrace();
                String escapedMessage = throwable.toString().replace("\"", "\'");
                escapedMessage = escapedMessage.replace("\n", " ");
                ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).contentType("application/json").send("{\"message\": \"Error checking activity existence: " + escapedMessage + " \" \n}");

            }).then(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Document exists, handle accordingly
                    ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Activity with the given ID already exists.\"}");
                } else {
                    // Document does not exist, proceed to add new activity
                    ApiFuture<WriteResult> addFuture = docRef.set(activity);
                    Promise<WriteResult> addPromise = Blocking.get(() -> addFuture.get());

                    addPromise.onError(throwable -> {
                                // Handle the error
                                throwable.printStackTrace();
                                String escapedMessage = throwable.toString().replace("\"", "\'");
                                escapedMessage = escapedMessage.replace("\n", " ");
                                ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).contentType("application/json").send("{\"message\": \"Failed to add activity: " + escapedMessage + " \" \n}");
                            })
                            .then(writeResult -> {
                                Map<String, String> successMsg = new HashMap<>();
                                successMsg.put("status", "Success");
                                successMsg.put("message", "Activity added successfully");
                                ctx.render(json(successMsg));
                            });
                }
            });
        });
    }

    public static void updateActivity(Context ctx) {
        try {
            ctx.parse(Activity.class).onError(throwable -> {
                // Handle parsing errors
                String escapedMessage = throwable.toString().replace("\"", "\'");
                escapedMessage = escapedMessage.replace("\n", " ");
                ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \"Invalid data format or type "+ escapedMessage +" \" \n}");
            }).then(activity -> {

                String validationError = validateActivity(activity);
                if (validationError != null) {
                    ctx.getResponse().status(Status.BAD_REQUEST).contentType("application/json").send("{\"message\": \""+ validationError +" \" \n}");
                    return;
                }

                // ambil key dari endpoint
                String activityId = ctx.getPathTokens().get("id");
                Firestore db = getFirestore();
                // berfungsi untuk mengirim data dari database secara async

                // convert menjadi bentuk dictionary
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("calories", activity.getCalories());
                updateData.put("avgHeartBeat", activity.getAvgHeartBeat());
                updateData.put("date", activity.getDate());
                updateData.put("distance", activity.getDistance());
                updateData.put("duration", activity.getDuration());
                updateData.put("steps", activity.getSteps());

                // update
                ApiFuture<WriteResult> future = db.collection("activities").document(String.valueOf(activityId)).update(updateData);

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
                    ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such document : "+ escapedMessage +" \" \n}");
                }).then(writeResult -> {
                    if (writeResult != null) {
                        Map<String, String> SuccesMsg = new HashMap<>();
                        SuccesMsg.put("status", "Success");
                        SuccesMsg.put("Message", "Activity updated successfully");
                        ctx.render(json(SuccesMsg));
                    } else {
                        ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such document with ID: "+ activityId +" \" \n}");
                    }
                });
            });
        }catch (Exception e) {
            String escapedMessage = e.toString().replace("\"", "\'");
            escapedMessage = escapedMessage.replace("\n", " ");
            ctx.getResponse().status(Status.INTERNAL_SERVER_ERROR).contentType("application/json").send("{\"message\": \"IInternal server error - "+ escapedMessage +" \" \n}");
        }
    }

    public static void deleteActivity(Context ctx) {
        Firestore db = getFirestore();
        // ambil di dari parameter
        String activityId = ctx.getPathTokens().get("id");
        ApiFuture<WriteResult> future = db.collection("activities").document(activityId).delete();
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
            ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such document with ID: "+ activityId +" \" \n}");
        }).then(writeResult -> {
            if (writeResult != null) {
                Map<String, String> SuccesMsg = new HashMap<>();
                SuccesMsg.put("status", "Success");
                SuccesMsg.put("Message", "Activity deleted successfully");
                ctx.render(json(SuccesMsg));
            } else {
                // Handle the case where the operation did not succeed
                ctx.getResponse().status(Status.NOT_FOUND).contentType("application/json").send("{\"message\": \"No such document with ID: "+ activityId +" \" \n}");
            }
        });
    }

    private static String validateActivity(Activity activity) {
        if (activity.getCalories() == 0) {
            return "Calories cannot be empty";
        }
        if (activity.getAvgHeartBeat() == 0) {
            return "Average Heart Beat cannot be empty";
        }
        if (activity.getDate() == null || activity.getDate().isEmpty()) {
            return "Date cannot be empty";
        }
        if (activity.getDistance() == 0) {
            return "Distance cannot be empty";
        }
        if (activity.getDuration() == 0) {
            return "Duration cannot be empty";
        }
        if (activity.getSteps() == 0) {
            return "Steps cannot be empty";
        }
        if (activity.getCalories() <= 0) {
            return "Calories must be a positive integer";
        }
        if (activity.getAvgHeartBeat() <= 0) {
            return "Average Heart Beat must be a positive integer";
        }
        if (activity.getDistance() <= 0) {
            return "Distance must be a positive integer";
        }
        if (activity.getDuration() <= 0) {
            return "Duration must be a positive integer";
        }
        if (activity.getSteps() <= 0) {
            return "Steps must be a positive integer";
        }
        return null;

    }

}
