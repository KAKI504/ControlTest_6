import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskDataModel {
    private static final String DATA_FILE = "data/json/tasks.json";
    private List<Task> tasks;
    private final Gson gson;

    public TaskDataModel() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        gsonBuilder.registerTypeAdapter(Task.class, new TaskDeserializer());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();

        loadTasks();

        if (tasks.isEmpty()) {
            generateSampleTasks();
        }
    }

    private void loadTasks() {
        File dataFile = new File(DATA_FILE);

        File directory = dataFile.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                Type taskListType = new TypeToken<ArrayList<Task>>(){}.getType();
                tasks = gson.fromJson(reader, taskListType);

                if (tasks == null) {
                    tasks = new ArrayList<>();
                }
            } catch (Exception e) {
                System.err.println("Ошибка при загрузке задач: " + e.getMessage());
                e.printStackTrace();
                tasks = new ArrayList<>();
            }
        } else {
            tasks = new ArrayList<>();
        }
    }

    private void generateSampleTasks() {
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 10; i++) {
            int dayOffset = (int) (Math.random() * 28) - 14;
            LocalDate taskDate = today.plusDays(dayOffset);

            Task.TaskType[] types = Task.TaskType.values();
            Task.TaskType type = types[(int) (Math.random() * types.length)];

            String id = UUID.randomUUID().toString();

            String title = Generator.makeName();
            String description = Generator.makeDescription();

            Task task = new Task(id, title, description, taskDate, type);
            tasks.add(task);
        }

        saveTasks();
    }

    public void saveTasks() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении задач: " + e.getMessage());
        }
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public List<Task> getTasksByDate(LocalDate date) {
        return tasks.stream()
                .filter(task -> task.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public Map<LocalDate, List<Task>> getTasksByMonth(int year, int month) {
        Map<LocalDate, List<Task>> result = new HashMap<>();

        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(year, month, day);
            result.put(currentDate, getTasksByDate(currentDate));
        }

        return result;
    }

    public Task getTaskById(String id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addTask(String title, String description, LocalDate date, Task.TaskType type) {
        String id = UUID.randomUUID().toString();
        Task task = new Task(id, title, description, date, type);
        tasks.add(task);
        saveTasks();
    }

    public void deleteTask(String id) {
        tasks.removeIf(task -> task.getId().equals(id));
        saveTasks();
    }

    public void updateTask(Task task) {
        int index = -1;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(task.getId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            tasks.set(index, task);
            saveTasks();
        }
    }

    static class TaskDeserializer implements JsonDeserializer<Task> {
        @Override
        public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String id = jsonObject.get("id").getAsString();
            String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() :
                    (jsonObject.has("title") ? jsonObject.get("title").getAsString() : "");
            String description = jsonObject.has("description") ? jsonObject.get("description").getAsString() : "";

            LocalDate date = context.deserialize(jsonObject.get("date"), LocalDate.class);

            Task.TaskType type;
            if (jsonObject.has("category")) {
                String categoryStr = jsonObject.get("category").getAsString();
                if ("REGULAR".equals(categoryStr)) {
                    type = Task.TaskType.NORMAL;
                } else {
                    try {
                        type = Task.TaskType.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Неизвестная категория: " + categoryStr + ", используем OTHER");
                        type = Task.TaskType.OTHER;
                    }
                }
            } else if (jsonObject.has("type")) {
                type = context.deserialize(jsonObject.get("type"), Task.TaskType.class);
            } else {
                type = Task.TaskType.NORMAL;
            }

            return new Task(id, name, description, date, type);
        }
    }
}

class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(date));
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return LocalDate.parse(json.getAsString(), formatter);
    }
}