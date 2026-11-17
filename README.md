# Лабораторна робота №1 — варіант 20

Основи Java та модифікатори доступу.

![build](https://github.com/Java-Fall2026/lab1-ctalavero/actions/workflows/build.yml/badge.svg)

- Умова: https://github.com/Java-Univer-2025/Java-Univer-2025.github.io/blob/main/lab-01/README.md
- Домени та поля: https://github.com/Java-Univer-2025/Java-Univer-2025.github.io/blob/main/lab-01/tasks_2025_domains.md

## Структура

```
src/main/java/ua/common/BaseEntity.java   готовий клас, не змінюйте
src/main/java/ua/music/model/           ваші сутності
src/main/java/ua/music/util/            ValidationHelper, FormatHelper, MusicUtils
src/main/java/ua/music/Main.java        демонстраційний клас
```

## Як зібрати і запустити

```
./gradlew build
./gradlew run
```

У Windows замість `./gradlew` пишіть `gradlew.bat`.

Після кожного пушу репозиторій збирається автоматично — стежте за бейджем угорі. Червоний бейдж = код не компілюється = робота не перевіряється.

## Чекліст здачі

- [x] усі сутності варіанта успадковують `BaseEntity` і викликають `super()`
- [x] поля `private`, сеттери тільки там, де в профілі стоїть ✎
- [x] два класи створюються через `of(...)`, конструктор у них `private`
- [x] валідація в конструкторах і сеттерах, повідомлення показує очікуване й отримане
- [x] `ValidationHelper` і `FormatHelper` — package-private, `MusicUtils` — public
- [x] два обчислювані методи з профілю
- [x] `equals`, `hashCode`, `toString` у кожній сутності
- [x] `Main` демонструє всі сценарії, зокрема невдалі
- [x] `AI.md` заповнено
- [ ] бейдж зелений

## Здача

Код у цьому репозиторії + захист 5 хв. Без захисту робота не зарахована.