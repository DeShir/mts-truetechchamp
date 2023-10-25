# Репозиторий для сервиса по оптимизации облачных затрат



## Начало работы

Вам выдан репозиторий, в который имеете доступ только вы и огранизатор соревнования. Все функции системы git включены и работают. Вы можете склонировать репозиторий на локальную машину и начать работу.

Подробнее про работу с git можно узнать, например, [здесь](https://proglib.io/p/git-cheatsheet)

Задание подразумевает, что ваш проект будет размещен в текущем репозитории, создавать в рамках выданного аккаунта новые репозитории можно, но бессмысленно.

## Структура и запуск проекта

Ваш проект может быть реализован на любом языке программирования, в рамках правил соревнования. Единственное требование - это наличие Dockerfile в корне проекта, который будет собирать образ решения и запускать проверку.

CI/CD в текущем репозитории настроен таким образом, что:
- Запускается на каждый git push в main ветку
- Собирает образ из Dockerfile в корне репозитория
- Деплоит в kubernetes кластер приложение отдельным неймспесом и запускает образ в контейнере


Ваша программа должна работать как фоновый сервис, постоянно работающий в облаке и реализующий логику, описанную в задании.
Доступ к kubernetes кластеру и/или логам работы сервиса в облаке не предоставляется. Для отладки решения вы можете:
- следить за логами сборки и развертывания приложения в разделе Build->Jobs в текущем репозитории
- следить за операциями, проводящимися в облаке с помощью API запросов с локальной машины
- мониторить результат работы сервиса в виде затрачиваемых ресурсов и SLA


В случае вопросов, обращайтесь к организатору.


## Работа с API

Все возможные методы описаны в Swagger документации: https://mts-olimp-cloud.codenrock.com/api/ui/
Там же можно их протестировать. Если запрос требует токен, его можно найти на странице с задачей. Если запрос требует админского токена, вам этот запрос не нужен.


## Пример сервиса

Мы подготовили публичный репозиторий с [примером работающего проекта на языке Kotlin](https://git.codenrock.com/mts-public/cloud-resources-example). В лидерборде его можно найти под называнием "Baseline [OUT OF THE CONTEST]". Вы можете использовать его как пример работы, если чувствуете, что это может быть полезно для вас.


## Результат работы

Результатом работы считается запущенный и работающий в облаке сервис, который проводит операции с API, полный код которого содержится в данном репозитории

## Оценка работы сервиса

Мы собираем 2 показателя, которые оценивают эффективность решения: количество затрачиваемых ресурсов и кол-во минут, которые "сервис" был оффлайн.

Расчет со стороны API происходит каждую минуту: каждый "тик" времени сервис записывает в лидерборд кол-во потраченных за эту минуту "ресурсов" (число) и время, которое сервис был оффлайн (0 или 1). Лидерборд складывает эти данные и показывает итоговые числа по работе системы.

Для исключения разности результатов ввиду разного времени старта работы сервиса, итоговый лидерборд будет строиться отдельно.
После завершения работы над решением (стоп-кодинг), мы отключим возможность обновлять алгоритмы, сбросим лидерборд и в течении 60 минут "засечем" работу всех решений с нуля. Таким образом, сформировав итоговый лидерборд по этапу, на основании которого и будут выявлены лидеры.