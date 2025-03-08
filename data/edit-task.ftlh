<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Редактирование задачи</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f9f9f9;
        }

        h1 {
            color: #444;
            text-align: center;
        }

        .nav-links {
            margin: 20px 0;
        }

        .button {
            display: inline-block;
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            margin-right: 10px;
            text-decoration: none;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }

        .button:hover {
            background-color: #45a049;
        }

        .form-container {
            background-color: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .form-group {
            margin-bottom: 15px;
        }

        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }

        .form-control {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
            font-size: 16px;
        }

        textarea.form-control {
            min-height: 120px;
            resize: vertical;
        }

        .form-actions {
            margin-top: 20px;
            text-align: center;
        }

        .button-save {
            background-color: #2196F3;
        }

        .button-save:hover {
            background-color: #0b7dda;
        }
    </style>
</head>
<body>
    <div class="nav-links">
        <a href="/tasks/${task.date}" class="button">← Вернуться к списку задач</a>
    </div>

    <h1>Редактирование задачи</h1>

    <div class="form-container">
        <form action="/edit-task" method="post">
            <input type="hidden" name="id" value="${task.id}">
            <input type="hidden" name="date" value="${task.date}">

            <div class="form-group">
                <label for="title">Название задачи*:</label>
                <input type="text" id="title" name="title" class="form-control" value="${task.title}" required>
            </div>

            <div class="form-group">
                <label for="description">Описание задачи:</label>
                <textarea id="description" name="description" class="form-control">${task.description!''}</textarea>
            </div>

            <div class="form-group">
                <label for="type">Категория задачи*:</label>
                <select id="type" name="type" class="form-control" required>
                    <#list taskTypes as type>
                        <option value="${type}" <#if task.type == type>selected</#if>>${type.displayName}</option>
                    </#list>
                </select>
            </div>

            <div class="form-actions">
                <button type="submit" class="button button-save">Сохранить изменения</button>
            </div>
        </form>
    </div>
</body>
</html>