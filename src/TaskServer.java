import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskServer extends SimpleServer {
    private final TaskDataModel taskDataModel;
    private final Configuration freemarker;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public TaskServer(String host, int port) throws IOException {
        super(host, port);

        this.taskDataModel = new TaskDataModel();
        this.freemarker = initFreeMarker();

        registerGet("/", this::handleCalendar);
        registerGet("/calendar", this::handleCalendar);

        registerGet("/tasks/", this::handleTasksList);
        registerGet("/tasks/*", this::handleTasksList);
        registerGet("/add-task", this::handleAddTaskForm);
        registerGet("/edit-task/", this::handleEditTaskForm);
        registerGet("/edit-task/*", this::handleEditTaskForm);

        registerPost("/add-task", this::handleAddTask);
        registerPost("/edit-task", this::handleEditTask);
        registerPost("/delete-task", this::handleDeleteTask);
    }

    private Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);

            File templateDir = new File("data");
            cfg.setDirectoryForTemplateLoading(templateDir);
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка инициализации FreeMarker", e);
        }
    }

    private void handleCalendar(HttpExchange exchange) throws IOException {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        String queryString = exchange.getRequestURI().getQuery();

        if (queryString != null && !queryString.isEmpty()) {
            Map<String, String> params = parseQueryString(queryString);

            if (params.containsKey("year")) {
                try {
                    year = Integer.parseInt(params.get("year"));
                } catch (NumberFormatException e) {
                }
            }

            if (params.containsKey("month")) {
                try {
                    month = Integer.parseInt(params.get("month"));
                    if (month < 1) {
                        month = 12;
                        year--;
                    } else if (month > 12) {
                        month = 1;
                        year++;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue();

        Map<LocalDate, List<Task>> tasksByDate = taskDataModel.getTasksByMonth(year, month);

        Map<String, Object> data = new HashMap<>();
        data.put("year", year);
        data.put("month", month);
        data.put("monthName", getMonthName(month));
        data.put("daysInMonth", daysInMonth);
        data.put("firstDayOfWeek", firstDayOfWeek);
        data.put("today", today);
        data.put("tasksByDate", tasksByDate);

        renderTemplate(exchange, "calendar.ftlh", data);
    }

    private void handleTasksList(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String dateStr = "";

        if (path.startsWith("/tasks/")) {
            dateStr = path.substring("/tasks/".length());
        }

        LocalDate date;
        if (dateStr.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                date = LocalDate.now();
            }
        }

        List<Task> tasks = taskDataModel.getTasksByDate(date);

        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("formattedDate", date.format(DATE_FORMATTER));
        data.put("tasks", tasks);
        data.put("taskTypes", Arrays.asList(Task.TaskType.values()));

        renderTemplate(exchange, "tasks-list.ftlh", data);
    }

    private void handleAddTaskForm(HttpExchange exchange) throws IOException {
        String queryString = exchange.getRequestURI().getQuery();

        LocalDate date = LocalDate.now();
        if (queryString != null && queryString.startsWith("date=")) {
            try {
                String dateStr = queryString.substring(5);
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("formattedDate", date.format(DATE_FORMATTER));
        data.put("taskTypes", Arrays.asList(Task.TaskType.values()));

        renderTemplate(exchange, "add-task.ftlh", data);
    }

    private void handleEditTaskForm(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (!path.startsWith("/edit-task/") || path.equals("/edit-task/")) {
            renderErrorPage(exchange, "Некорректный идентификатор задачи");
            return;
        }

        String taskId = path.substring("/edit-task/".length());

        Task task = taskDataModel.getTaskById(taskId);
        if (task == null) {
            renderErrorPage(exchange, "Задача не найдена");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("task", task);
        data.put("formattedDate", task.getDate().format(DATE_FORMATTER));
        data.put("taskTypes", Arrays.asList(Task.TaskType.values()));

        renderTemplate(exchange, "edit-task.ftlh", data);
    }

    private void handleAddTask(HttpExchange exchange) throws IOException {
        Map<String, String> formData = parseFormData(exchange);

        String title = formData.get("title");
        String description = formData.get("description");
        String dateStr = formData.get("date");
        String typeStr = formData.get("type");

        if (title == null || title.isEmpty() || dateStr == null || dateStr.isEmpty() || typeStr == null || typeStr.isEmpty()) {
            renderErrorPage(exchange, "Все обязательные поля должны быть заполнены");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            Task.TaskType type = Task.TaskType.valueOf(typeStr);

            taskDataModel.addTask(title, description, date, type);

            redirect303(exchange, "/tasks/" + dateStr);
        } catch (Exception e) {
            renderErrorPage(exchange, "Ошибка при создании задачи: " + e.getMessage());
        }
    }

    private void handleEditTask(HttpExchange exchange) throws IOException {
        Map<String, String> formData = parseFormData(exchange);

        String id = formData.get("id");
        String title = formData.get("title");
        String description = formData.get("description");
        String dateStr = formData.get("date");
        String typeStr = formData.get("type");

        if (id == null || id.isEmpty() || title == null || title.isEmpty() ||
                dateStr == null || dateStr.isEmpty() || typeStr == null || typeStr.isEmpty()) {
            renderErrorPage(exchange, "Все обязательные поля должны быть заполнены");
            return;
        }

        try {
            Task task = taskDataModel.getTaskById(id);
            if (task == null) {
                renderErrorPage(exchange, "Задача не найдена");
                return;
            }

            LocalDate date = LocalDate.parse(dateStr);
            Task.TaskType type = Task.TaskType.valueOf(typeStr);

            task.setTitle(title);
            task.setDescription(description);
            task.setDate(date);
            task.setType(type);

            taskDataModel.updateTask(task);

            redirect303(exchange, "/tasks/" + dateStr);
        } catch (Exception e) {
            renderErrorPage(exchange, "Ошибка при обновлении задачи: " + e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Map<String, String> formData = parseFormData(exchange);

        String id = formData.get("id");
        String dateStr = formData.get("date");

        if (id == null || id.isEmpty()) {
            renderErrorPage(exchange, "Не указан ID задачи");
            return;
        }

        try {
            taskDataModel.deleteTask(id);

            redirect303(exchange, "/tasks/" + dateStr);
        } catch (Exception e) {
            renderErrorPage(exchange, "Ошибка при удалении задачи: " + e.getMessage());
        }
    }

    private void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) throws IOException {
        try {
            Template template = freemarker.getTemplate(templateFile);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, 0);

            try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.UTF_8)) {
                template.process(dataModel, writer);
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            respond404(exchange);
        }
    }

    private void renderErrorPage(HttpExchange exchange, String errorMessage) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("errorMessage", errorMessage);
        renderTemplate(exchange, "error.ftlh", data);
    }

    private void redirect303(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().add("Location", path);
        exchange.sendResponseHeaders(303, -1);
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String formData = br.lines().collect(Collectors.joining());

        Map<String, String> result = new HashMap<>();

        if (formData != null && !formData.isEmpty()) {
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    result.put(key, value);
                }
            }
        }

        return result;
    }

    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> result = new HashMap<>();

        if (queryString != null && !queryString.isEmpty()) {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    try {
                        String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                        result.put(key, value);
                    } catch (Exception e) {
                    }
                }
            }
        }

        return result;
    }

    private String getMonthName(int month) {
        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };
        return monthNames[month - 1];
    }
}