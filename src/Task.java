import java.time.LocalDate;

public class Task {
    private String id;
    private String title;
    private String description;
    private LocalDate date;
    private TaskType type;

    public Task(String id, String title, String description, LocalDate date, TaskType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public enum TaskType {
        NORMAL("Обычная задача"),
        URGENT("Срочное дело"),
        WORK("Работа"),
        SHOPPING("Покупки"),
        OTHER("Прочее");

        private final String displayName;

        TaskType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", type=" + type +
                '}';
    }
}