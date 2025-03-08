<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Задачи на ${formattedDate}</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }

        h1, h2 {
            color: #444;
        }

        .nav-links {
            margin: 20px 0;
        }

        .button {
            display: inline-block;
            background-color: #4CAF50;
            color: white;
            padding: 8px 16px;
            margin-right: 10px;
            text-decoration: none;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            font-size: 14px;
        }

        .button:hover {
            background-color: #45a049;
        }

        .task-list {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        .task-list th {
            background-color: #f2f2f2;
            padding: 10px;
            text-align: left;
            font-weight: bold;
            border: 1px solid #ddd;
        }

        .task-list td {
            border: 1px solid #ddd;
            padding: 10px;
            vertical-align: top;
        }

        .task-list tr:hover {
            background-color: #f5f5f5;
        }

        .empty-tasks {
            text-align: center;
            padding: 40px;
            background-color: #f9f9f9;
            border-radius: 4px;
            margin-top: 20px;
        }

        .task-tag {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            color: white;
            font-size: 12px;
            margin-bottom: 5px;
        }

        .task-normal {
            background-color: #4CAF50;
        }

        .task-urgent {
            background-color: #f44336;
        }

        .task-work {
            background-color: #2196F3;
        }

        .task-shopping {
            background-color: #FF9800;
        }

        .task-other {
            background-color: #9E9E9E;
        }

        .action-buttons {
            display: flex;
            gap: 10px;
        }

        .button-delete {
            background-color: #f44336;
        }

        .button-delete:hover {
            background-color: #d32f2f;
        }

        .button-edit {
            background-color: #2196F3;
        }

        .button-edit:hover {
            background-color: #0b7dda;
        }
    </style>
</head>
<body>
    <div class="nav-links">
        <a href="/calendar" class="button">← Вернуться к календарю</a>
        <a href="/add-task?date=${date}" class="button">+ Добавить задачу</a>
    </div>

    <h1>Задачи на ${formattedDate}</h1>

    <#if tasks?size gt 0>
        <table class="task-list">
            <thead>
                <tr>
                    <th>Название</th>
                    <th>Описание</th>
                    <th>Категория</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody>
                <#list tasks as task>
                    <tr>
                        <td><strong>${task.title}</strong></td>
                        <td>${task.description!''}</td>
                        <td>
                            <span class="task-tag task-${task.type?lower_case}">${task.type.displayName}</span>
                        </td>
                        <td class="action-buttons">
                            <a href="/edit-task/${task.id}" class="button button-edit">Редактировать</a>
                            <form action="/delete-task" method="post" style="display: inline;">
                                <input type="hidden" name="id" value="${task.id}">
                                <input type="hidden" name="date" value="${date}">
                                <button type="submit" class="button button-delete">Удалить</button>
                            </form>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    <#else>
        <div class="empty-tasks">
            <p>На этот день пока нет задач.</p>
            <a href="/add-task?date=${date}" class="button">Создать задачу</a>
        </div>
    </#if>
</body>
</html>