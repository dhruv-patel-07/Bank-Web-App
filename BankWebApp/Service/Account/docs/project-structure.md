## 📁 Project Structure

```
account-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── yourcompany/
│   │   │           └── account/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── repo/
│   │   │               ├── model/
│   │   │               ├── dto/
│   │   │               ├── kafka/
│   │   │               ├── validation/
│   │   │               └── AccountApplication.java
│   └── resources/
│       ├── application.yml
│       ├── static/
│       └── templates/
├── test/
│   └── java/
│       └── com/
│           └── yourcompany/
│               └── account/
│                   ├── controller/
│                   ├── service/
│                   └── Account

```
## Explanation of Key Folders

- **controller**: Handles HTTP requests and responses.
- **service**: Contains business logic services.
- **repo**: Data access layer using Spring Data JPA.
- **model**: Entity classes representing database tables.
- **dto**: Objects used to transfer data between layers.
- **validation**: custom validation .
- **config**: Configuration classes for rest template.
- **kafka**: Classes to produce Kafka messages.
