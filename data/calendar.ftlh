<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Планировщик задач - Календарь</title>
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
            text-align: center;
        }

        .calendar {
            border-collapse: collapse;
            width: 100%;
            margin-top: 20px;
        }

        .calendar th {
            background-color: #f2f2f2;
            padding: 10px;
            text-align: center;
            font-weight: bold;
            border: 1px solid #ddd;
        }

        .calendar td {
            border: 1px solid #ddd;
            padding: 5px;
            height: 100px;
            vertical-align: top;
            width: 14%;
        }

        .date-number {
            font-weight: bold;
            display: block;
            margin-bottom: 5px;
            text-align: right;
        }

        .today {
            background-color: #f0f8ff;
        }

        .day-cell {
            cursor: pointer;
        }

        .day-cell:hover {
            background-color: #f5f5f5;
        }

        .task-indicator {
            display: inline-block;
            width: 10px;
            height: 10px;
            border-radius: 50%;
            margin-right: 5px;
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

        .task-list {
            margin-top: 5px;
            font-size: 12px;
        }

        .task-item {
            margin-bottom: 3px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .legend {
            display: flex;
            justify-content: center;
            margin: 20px 0;
            flex-wrap: wrap;
        }

        .legend-item {
            display: flex;
            align-items: center;
            margin: 0 10px;
        }
    </style>
</head>
<body>
    <h1>Планировщик задач</h1>
    <h2>${monthName} ${year?c}</h2>

    <div class="legend">
        <div class="legend-item">
            <span class="task-indicator task-normal"></span>
            <span>Обычная задача</span>
        </div>
        <div class="legend-item">
            <span class="task-indicator task-urgent"></span>
            <span>Срочное дело</span>
        </div>
        <div class="legend-item">
            <span class="task-indicator task-work"></span>
            <span>Работа</span>
        </div>
        <div class="legend-item">
            <span class="task-indicator task-shopping"></span>
            <span>Покупки</span>
        </div>
        <div class="legend-item">
            <span class="task-indicator task-other"></span>
            <span>Прочее</span>
        </div>
    </div>

    <table class="calendar">
        <thead>
            <tr>
                <th>Пн</th>
                <th>Вт</th>
                <th>Ср</th>
                <th>Чт</th>
                <th>Пт</th>
                <th>Сб</th>
                <th>Вс</th>
            </tr>
        </thead>
        <tbody>
            <#assign day = 1>
            <#list 1..6 as week>
                <#if day <= daysInMonth>
                    <tr>
                        <#list 1..7 as dayOfWeek>
                            <#if (week == 1 && dayOfWeek < firstDayOfWeek) || (day > daysInMonth)>
                                <td></td>
                            <#else>
                                <#-- Создаем объект даты для текущего дня -->
                                <#assign currentDate = year?c + "-" + month?string("00") + "-" + day?string("00")>
                                <#assign currentLocalDate = year + "-" + month + "-" + day>
                                <#assign isToday = day == today.dayOfMonth && month == today.monthValue && year == today.year>

                                <td class="day-cell <#if isToday>today</#if>" onclick="window.location.href='/tasks/${currentDate}'">
                                    <div class="date-number">${day}</div>

                                    <#-- Проверяем, есть ли задачи для текущей даты -->
                                    <#list tasksByDate?keys as dateKey>
                                        <#if dateKey?string == currentLocalDate?string>
                                            <div class="task-list">
                                                <#assign normalCount = 0>
                                                <#assign urgentCount = 0>
                                                <#assign workCount = 0>
                                                <#assign shoppingCount = 0>
                                                <#assign otherCount = 0>

                                                <#list tasksByDate[dateKey] as task>
                                                    <#if task.type.name() == "NORMAL">
                                                        <#assign normalCount = normalCount + 1>
                                                    <#elseif task.type.name() == "URGENT">
                                                        <#assign urgentCount = urgentCount + 1>
                                                    <#elseif task.type.name() == "WORK">
                                                        <#assign workCount = workCount + 1>
                                                    <#elseif task.type.name() == "SHOPPING">
                                                        <#assign shoppingCount = shoppingCount + 1>
                                                    <#elseif task.type.name() == "OTHER">
                                                        <#assign otherCount = otherCount + 1>
                                                    </#if>
                                                </#list>

                                                <#if normalCount gt 0>
                                                    <div class="task-item">
                                                        <span class="task-indicator task-normal"></span>
                                                        <span>${normalCount}</span>
                                                    </div>
                                                </#if>

                                                <#if urgentCount gt 0>
                                                    <div class="task-item">
                                                        <span class="task-indicator task-urgent"></span>
                                                        <span>${urgentCount}</span>
                                                    </div>
                                                </#if>

                                                <#if workCount gt 0>
                                                    <div class="task-item">
                                                        <span class="task-indicator task-work"></span>
                                                        <span>${workCount}</span>
                                                    </div>
                                                </#if>

                                                <#if shoppingCount gt 0>
                                                    <div class="task-item">
                                                        <span class="task-indicator task-shopping"></span>
                                                        <span>${shoppingCount}</span>
                                                    </div>
                                                </#if>

                                                <#if otherCount gt 0>
                                                    <div class="task-item">
                                                        <span class="task-indicator task-other"></span>
                                                        <span>${otherCount}</span>
                                                    </div>
                                                </#if>
                                            </div>
                                            <#break>
                                        </#if>
                                    </#list>
                                </td>

                                <#assign day = day + 1>
                            </#if>
                        </#list>
                    </tr>
                </#if>
            </#list>
        </tbody>
    </table>
</body>
</html>