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
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f9f9f9;
        }

        h1, h2 {
            color: #444;
            text-align: center;
        }

        .month-navigation {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin: 20px 0;
        }

        .month-navigation button {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 8px 16px;
            font-size: 16px;
            cursor: pointer;
            border-radius: 4px;
        }

        .month-navigation button:hover {
            background-color: #45a049;
        }

        .calendar-wrapper {
            background-color: white;
            border-radius: 8px;
            padding: 15px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .calendar {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 8px;
        }

        .calendar-header {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            gap: 8px;
            margin-bottom: 8px;
        }

        .calendar-header div {
            text-align: center;
            font-weight: bold;
            padding: 10px 0;
            background-color: #f2f2f2;
            border-radius: 4px;
        }

        .day-button {
            aspect-ratio: 1/1;
            background-color: white;
            border: 1px solid #ddd;
            border-radius: 4px;
            cursor: pointer;
            position: relative;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 5px;
            transition: background-color 0.2s;
        }

        .day-button:hover {
            background-color: #f5f5f5;
        }

        .day-number {
            font-weight: bold;
            margin-bottom: 5px;
        }

        .today {
            background-color: #e6f7ff;
            border-color: #91d5ff;
        }

        .task-indicators {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 2px;
        }

        .task-indicator {
            width: 8px;
            height: 8px;
            border-radius: 50%;
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

        .legend {
            display: flex;
            justify-content: center;
            margin: 20px 0;
            flex-wrap: wrap;
            gap: 15px;
        }

        .legend-item {
            display: flex;
            align-items: center;
        }

        .legend-item .task-indicator {
            width: 12px;
            height: 12px;
            margin-right: 5px;
        }

        .empty-day {
            background-color: #f9f9f9;
            border: none;
        }
    </style>
</head>
<body>
    <h1>Планировщик задач</h1>

    <div class="month-navigation">
        <button onclick="window.location.href='/calendar?month=${month-1}&year=${(month==1)?then(year-1, year)}'">← Пред</button>
        <h2>${monthName} ${year?c}</h2>
        <button onclick="window.location.href='/calendar?month=${month+1}&year=${(month==12)?then(year+1, year)}'">След →</button>
    </div>

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

    <div class="calendar-wrapper">
        <div class="calendar-header">
            <div>Пн</div>
            <div>Вт</div>
            <div>Ср</div>
            <div>Чт</div>
            <div>Пт</div>
            <div>Сб</div>
            <div>Вс</div>
        </div>

        <div class="calendar">
            <#assign day = 1>
            <#list 1..6 as week>
                <#if day <= daysInMonth>
                    <#list 1..7 as dayOfWeek>
                        <#if (week == 1 && dayOfWeek < firstDayOfWeek) || (day > daysInMonth)>
                            <div class="day-button empty-day"></div>
                        <#else>
                            <#assign currentDate = year?c + "-" + month?string("00") + "-" + day?string("00")>
                            <#assign currentLocalDate = year + "-" + month + "-" + day>
                            <#assign isToday = day == today.dayOfMonth && month == today.monthValue && year == today.year>

                            <button class="day-button <#if isToday>today</#if>" onclick="window.location.href='/tasks/${currentDate}'">
                                <div class="day-number">${day}</div>

                                <#list tasksByDate?keys as dateKey>
                                    <#if dateKey?string == currentLocalDate?string>
                                        <div class="task-indicators">
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

                                            <#list 1..normalCount as i>
                                                <span class="task-indicator task-normal" title="Обычная задача"></span>
                                            </#list>

                                            <#list 1..urgentCount as i>
                                                <span class="task-indicator task-urgent" title="Срочное дело"></span>
                                            </#list>

                                            <#list 1..workCount as i>
                                                <span class="task-indicator task-work" title="Работа"></span>
                                            </#list>

                                            <#list 1..shoppingCount as i>
                                                <span class="task-indicator task-shopping" title="Покупки"></span>
                                            </#list>

                                            <#list 1..otherCount as i>
                                                <span class="task-indicator task-other" title="Прочее"></span>
                                            </#list>
                                        </div>
                                        <#break>
                                    </#if>
                                </#list>
                            </button>

                            <#assign day = day + 1>
                        </#if>
                    </#list>
                </#if>
            </#list>
        </div>
    </div>
</body>
</html>