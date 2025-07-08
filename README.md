# Effective Mobile
### Клонировать репозиторий: ###

- 1.) git clone git@github.com:BulbazavrFromRus/bank-card-system.git
- 2.) cd effective-mobile

### Собрать проект: ###

- ./mvnw clean package

### Загрузить образ из Docker Hub: ###

- 1.) docker pull bulbafromrus/effective-mobile:latest
- 2.) docker compose up -d

### Проверить запуск: ###

- 1.) docker ps
- 2.) docker logs effective-mobile-app-1
- 3.) docker logs effective-mobile-db-1

### Список эндпоинтов приложения ###

- POST  /api/auth/register - Регистрация пользователя
- POST	/api/auth/login - Аутентификация пользователя, возвращает JWT-токен
- GET	/api/user/cards - Получить список карт текущего пользователя (Authorization: Bearer)
- POST	/api/user/cards/{id}/block - Запросить блокировку своей карты  (необходимо передать id карты и Authorization: Bearer)
- POST	/api/user/transfer - Перевод денег между своими картами (необходим JSON с fromCardId, toCardId, amount и Authorization: Bearer)
- GET	/api/admin/cards - Получить список всех карт в системе (Authorization: Bearer admin)
- POST	/api/admin/cards - Создать новую карту для пользователя (admin)
- PUT	/api/admin/cards/{id}/block - Заблокировать указанную карту (ID карты и Authorization: Bearer)
- PUT	/api/admin/cards/{id}/activate - Активировать указанную карту admin (ID карты и Authorization: Bearer)
- DELETE	/api/admin/cards/{id} - Удалить указанную карту (ID карты и Authorization: Bearer)


