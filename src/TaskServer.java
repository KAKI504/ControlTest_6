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

        System.out.println("Регистрация обработчиков...");

        registerGet("/", this::handleCalendar);
        registerGet("/calendar", this::handleCalendar);

        registerGet("/tasks/*", this::handleTasksList);
        registerGet("/add-task", this::handleAddTaskForm);
        registerGet("/edit-task/*", this::handleEditTaskForm);

        registerPost("/add-task", this::handleAddTask);
        registerPost("/edit-task", this::handleEditTask);
        registerPost("/delete-task", this::handleDeleteTask);

        System.out.println("Обработчики зарегистрированы.");
    }

    private Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
            cfg.setDirectoryForTemplateLoading(new File("data"));
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

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue();

        Map<LocalDate, List<Task>> tasksByDate = taskDataModel.getTasksByMonth(year, month);

        System.out.println("Подготовка данных для календаря: " + today);
        System.out.println("Задачи на месяц: " + tasksByDate.size() + " дней");

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

        System.out.println("Запрос списка задач на дату: " + dateStr);

        System.out.println("URL путь: " + path);
        System.out.println("Извлеченная дата: " + dateStr);

        LocalDate date;
        if (dateStr.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr);
                System.out.println("Распарсенная дата: " + date);
            } catch (Exception e) {
                System.out.println("Ошибка при парсинге даты: " + e.getMessage());
                date = LocalDate.now();
            }

        }

        List<Task> tasks = taskDataModel.getTasksByDate(date);
        System.out.println("Найдено задач: " + tasks.size());

        Map<String, Object> data = new HashMap<>();
        data.put("date", date);
        data.put("formattedDate", date.format(DATE_FORMATTER));
        data.put("tasks", tasks);
        data.put("taskTypes", Arrays.asList(Task.TaskType.values()));

        renderTemplate(exchange, "tasks-list.ftlh", data);
    }

    private void handleAddTaskForm(HttpExchange exchange) throws IOException {
        String queryString = exchange.getRequestURI().getQuery();

        System.out.println("Запрос формы добавления задачи с параметрами: " + queryString);

        LocalDate date = LocalDate.now();
        if (queryString != null && queryString.startsWith("date=")) {
            try {
                date = LocalDate.parse(queryString.substring(5));
            } catch (Exception e) {
                System.out.println("Ошибка при парсинге даты из параметра: " + e.getMessage());
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
        String taskId = path.substring("/edit-task/".length());

        System.out.println("Запрос формы редактирования задачи с ID: " + taskId);

        Task task = taskDataModel.getTaskById(taskId);
        if (task == null) {
            System.out.println("Задача не найдена");
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

        System.out.println("Получены данные для добавления задачи: " + formData);

        String title = formData.get("title");
        String description = formData.get("description");
        String dateStr = formData.get("date");
        String typeStr = formData.get("type");

        if (title == null || title.isEmpty() || dateStr == null || dateStr.isEmpty() || typeStr == null || typeStr.isEmpty()) {
            System.out.println("Не все обязательные поля заполнены");
            renderErrorPage(exchange, "Все обязательные поля должны быть заполнены");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            Task.TaskType type = Task.TaskType.valueOf(typeStr);

            taskDataModel.addTask(title, description, date, type);
            System.out.println("Задача успешно добавлена");

            redirect303(exchange, "/tasks/" + dateStr);
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении задачи: " + e.getMessage());
            renderErrorPage(exchange, "Ошибка при создании задачи: " + e.getMessage());
        }
    }

    private void handleEditTask(HttpExchange exchange) throws IOException {
        Map<String, String> formData = parseFormData(exchange);

        System.out.println("Получены данные для редактирования задачи: " + formData);

        String id = formData.get("id");
        String title = formData.get("title");
        String description = formData.get("description");
        String dateStr = formData.get("date");
        String typeStr = formData.get("type");

        if (id == null || id.isEmpty() || title == null || title.isEmpty() || dateStr == null || dateStr.isEmpty() || typeStr == null || typeStr.isEmpty()) {
            System.out.println("Не все обязательные поля заполнены");
            renderErrorPage(exchange, "Все обязательные поля должны быть заполнены");
            return;
        }

        try {
            Task task = taskDataModel.getTaskById(id);
            if (task == null) {
                System.out.println("Задача не найдена");
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
            System.out.println("Задача успешно обновлена");

            redirect303(exchange, "/tasks/" + dateStr);
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении задачи: " + e.getMessage());
            renderErrorPage(exchange, "Ошибка при обновлении задачи: " + e.getMessage());
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Map<String, String> formData = parseFormData(exchange);

        System.out.println("Получены данные для удаления задачи: " + formData);

        String id = formData.get("id");
        String dateStr = formData.get("date");

        if (id == null || id.isEmpty()) {
            System.out.println("Не указан ID задачи");
            renderErrorPage(exchange, "Не указан ID задачи");
            return;
        }

        try {
            taskDataModel.deleteTask(id);
            System.out.println("Задача успешно удалена");

            redirect303(exchange, "/tasks/" + dateStr);
        } catch (Exception e) {
            System.out.println("Ошибка при удалении задачи: " + e.getMessage());
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
                System.err.println("Ошибка при обработке шаблона: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке шаблона: " + e.getMessage());
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
        System.out.println("Перенаправление на: " + path);
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

    private String getMonthName(int month) {
        String[] monthNames = {
                "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        };
        return monthNames[month - 1];
    }
}