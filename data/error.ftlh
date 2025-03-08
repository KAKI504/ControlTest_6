<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ошибка</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            text-align: center;
        }

        h1 {
            color: #f44336;
        }

        .error-container {
            background-color: #ffebee;
            border-radius: 8px;
            padding: 30px;
            margin: 40px auto;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .error-icon {
            font-size: 72px;
            margin-bottom: 20px;
        }

        .error-message {
            font-size: 18px;
            margin-bottom: 30px;
        }

        .button {
            display: inline-block;
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            font-size: 16px;
        }

        .button:hover {
            background-color: #45a049;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">⚠️</div>
        <h1>Ошибка</h1>
        <div class="error-message">
            ${errorMessage}
        </div>
        <a href="/calendar" class="button">Вернуться на главную</a>
    </div>
</body>
</html>